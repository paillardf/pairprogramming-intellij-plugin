package com.sertook.pairprogramming.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import com.sertook.pairprogramming.files.vcs.ClassicVirtualFile;
import com.sertook.pairprogramming.files.vcs.FileIgnoreStore;
import com.sertook.pairprogramming.models.FileInfos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by florian on 14/11/2016.
 */
public class SyncFileManager {

    private final Project project;
    private final FileIgnoreStore ignoreStore;
    private Map<String, FileInfos> listenFiles = ContainerUtil.newHashMap();
    private FileChangeListener listener;
    private SimpleVirtualFileListener virtualFileListener;

    public SyncFileManager(Project project) {
        this.project = project;
        this.ignoreStore = new FileIgnoreStore(project);
    }

    public void start(FileChangeListener listener) {
        init();
        this.listener = listener;


        virtualFileListener = new SimpleVirtualFileListener(project) {

            @Override
            void onFileEvent(VirtualFileEvent event) {
                VirtualFile file = event.getFile();
                if (ignoreStore.isIgnoreFile(file)) {
                    ignoreStore.clearCache();
                    init();
                }

                if (!ignoreStore.isFileIgnored(file)) {
                    addFile(file);
                }
                notifyChange();
            }
        };

        VirtualFileManager.getInstance().addVirtualFileListener(
                virtualFileListener
        );
        notifyChange();
    }

    public void stop() {
        VirtualFileManager.getInstance().removeVirtualFileListener(virtualFileListener);
        ignoreStore.clearCache();
    }

    private void notifyChange() {
        if (this.listener != null) {
            this.listener.filesChanged(getCurrentFilesInfos());
        }

    }

    private void init() {
        ignoreStore.init();
        analyseFiles(project.getBaseDir());
    }

    private void analyseFiles(VirtualFile file) {
        boolean ignored = ignoreStore.isFileIgnored(file);
        if (!ignored) {
            addFile(file);
        }
        if (!ignored && file.isDirectory()) {
            for (VirtualFile child : file.getChildren()) {
                analyseFiles(child);
            }
        }
    }

    private void addFile(VirtualFile file) {
        if (!file.isDirectory()) {
            FileInfos infos = new FileInfos(project, file);
            listenFiles.put(infos.getPath(), infos);
        }
    }

    public List<String> getNeededFiles(List<FileInfos> files) {
        List<String> neededFilePath = new ArrayList<>();
        for (FileInfos infosRemote : files) {
            VirtualFile localFile = new ClassicVirtualFile(project.getBasePath() + File.separator + infosRemote.getPath());
            if (ignoreStore.isFileIgnored(localFile)) {
                continue;
            }
            if (listenFiles.containsKey(infosRemote.getPath())) {
                FileInfos infosLocal = listenFiles.get(infosRemote.getPath());
                if (!infosRemote.isExist()) {
                    if (infosLocal.getTimeStamp() < infosRemote.getTimeStamp()) {
                        try {
                            project.getBaseDir().findFileByRelativePath(infosLocal.getPath()).delete(this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (infosLocal.getTimeStamp() < infosRemote.getTimeStamp() && infosLocal.getLenght() != infosRemote.getLenght()) {
                        neededFilePath.add(infosRemote.getPath());
                    }
                }
            } else {
                neededFilePath.add(infosRemote.getPath());
            }
        }
        return neededFilePath;
    }

    public ArrayList<FileInfos> getCurrentFilesInfos() {
        return new ArrayList<FileInfos>(listenFiles.values());
    }


    public interface FileChangeListener {

        void filesChanged(List<FileInfos> infos);

    }

}
