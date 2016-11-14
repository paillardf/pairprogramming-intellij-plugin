package com.sertook.pairprogramming.files;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.HashMap;
import com.sertook.pairprogramming.files.vcs.IgnoreFileProvider;
import com.sertook.pairprogramming.files.vcs.impl.GitignoreFileType;
import com.sertook.pairprogramming.files.vcs.impl.SvnignoreFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Created by florian on 14/11/2016.
 * <p>
 * Handle ignore file cache
 */
public class FileIgnoreStore {

    private Project project;

    private ConcurrentMap<PsiFile, Set<Pattern>> map = ContainerUtil.newConcurrentMap();
    private final HashMap<VirtualFile, Status> statuses = new HashMap<>();

    /**
     * Status of the file.
     */
    private enum Status {
        IGNORED, UNIGNORED, UNTOUCHED
    }

    public FileIgnoreStore(Project project) {
        this.project = project;
    }


    public void clearCache() {
        map.clear();
        statuses.clear();
    }


    public void init() {
        ArrayList<VirtualFile> ignoreFiles = new ArrayList<>();
        List<IgnoreFileProvider> ignoreFileProviders = Arrays.asList(
                GitignoreFileType.INSTANCE,
                SvnignoreFileType.INSTANCE
        );

        Utils.findFiles(project.getBaseDir(), ignoreFileProviders, ignoreFiles);

        for (VirtualFile ignoreFile : ignoreFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(ignoreFile);
            if (file != null) {

                for (IgnoreFileProvider ignoreFileProvider : ignoreFileProviders) {
                    if (file.getName().equals(ignoreFileProvider.getFileName())) {
                        add(file, ignoreFileProvider.ignoredFolder());
                    }
                }
            }
        }
    }


    /**
     * Adds new {@link PsiFileImpl} to the cache and builds its hashCode and patterns sets.
     *
     * @param file         to add
     * @param ignoreFolder
     */
    private void add(@NotNull final PsiFile file, List<String> ignoreFolder) {
        if (file.isDirectory()) {
            throw new IllegalStateException("Ignored file '" + file.getName() + "' can't be a directory");
        }
        final Set<Pattern> set = ContainerUtil.newHashSet();
        ignoreFolder.forEach(s -> set.add(Pattern.compile(s)));
        runVisitorInReadAction(file, new IgnoreVisitor() {
            @Override
            public void visitLine(String line) {
                set.add(Pattern.compile(Glob.createRegex(line, false)));
            }
        });
        map.put(file, set);
    }


    public boolean isFileIgnored(@NotNull VirtualFile file) {
        Status status = statuses.get(file);

        if (status == null || status.equals(Status.UNTOUCHED)) {
            status = getParentStatus(file);
            statuses.put(file, status);
        }

        for (final PsiFile ignoreFile : map.keySet()) {
            final VirtualFile ignoreFileParent = ignoreFile.getVirtualFile().getParent();

            if (Utils.isUnder(file, ignoreFileParent)) {

                final String path = Utils.getRelativePath(ignoreFileParent, file);
                if (StringUtil.isEmpty(path)) {
                    continue;
                }

                Set<Pattern> patterns = map.get(ignoreFile);
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(path).matches()) {
                        status = Status.IGNORED;
                        break;
                    }
                }

                if (!status.equals(Status.UNTOUCHED)) {
                    break;
                }
            }
        }

        statuses.put(file, status);
        return status.equals(Status.IGNORED);
    }


    @NotNull
    private Status getParentStatus(@NotNull VirtualFile file) {
        VirtualFile parent = file.getParent();
        VirtualFile vcsRoot = ProjectLevelVcsManager.getInstance(project).getVcsRootFor(file);

        while (parent != null && !parent.equals(project.getBaseDir()) && (vcsRoot == null || !vcsRoot.equals(parent))) {
            final Status status = statuses.get(parent);
            if (status != null) {
                return status;
            }
            parent = parent.getParent();
        }
        return Status.UNTOUCHED;
    }

    /**
     * Simple wrapper for running read action
     *
     * @param file    {@link PsiFileImpl} to run visitor on it
     * @param visitor {@link IgnoreVisitor}
     */
    private void runVisitorInReadAction(@NotNull final PsiFile file, @NotNull final IgnoreVisitor visitor) {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                VirtualFile virtualFile = file.getVirtualFile();
                if (virtualFile != null) {
                    file.acceptChildren(visitor);
                }
            }
        });
    }
}
