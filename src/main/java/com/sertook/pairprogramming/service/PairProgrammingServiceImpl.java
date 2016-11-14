package com.sertook.pairprogramming.service;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiManager;
import com.sertook.pairprogramming.ActionDelegate;
import com.sertook.pairprogramming.CommunicatioHelper;
import com.sertook.pairprogramming.CreateARoomAction;
import com.sertook.pairprogramming.files.FileIgnoreStore;
import icons.PairProgrammingIcons;
import org.jetbrains.annotations.NotNull;

/**
 * Created by florian on 13/11/2016.
 */
public class PairProgrammingServiceImpl implements PairProgrammingService {
    private final Project project;
    private CommunicatioHelper communicatioHelper = new CommunicatioHelper();
    private FileIgnoreStore fileIgnoreStore;


    public PairProgrammingServiceImpl(Project project) {
        this.project = project;
    }

    public boolean isStarted() {
        return communicatioHelper.isOpen();
    }

    public void start(ActionDelegate<String> delegate) {
        fileIgnoreStore = new FileIgnoreStore(project);
        fileIgnoreStore.init();

        drawFileTree(project.getBaseDir(), fileIgnoreStore);


        PsiManager.getInstance(project);
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

            }

            @Override
            public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
                System.out.println(virtualFileEvent.getFileName());
            }

            @Override
            public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {

            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {

            }

            @Override
            public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

            }

            @Override
            public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {

            }

            @Override
            public void beforePropertyChange(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

            }

            @Override
            public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

            }

            @Override
            public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

            }

            @Override
            public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

            }
        });


        communicatioHelper.open(null, new CommunicatioHelper.CommunicationListener() {
            public void onMessageReceived(String msg) {

            }

            public void onServerError(Throwable throwable) {

            }

            public void onStatusChanged() {

                AnAction action = ActionManager.getInstance().getAction(CreateARoomAction.ID);
                action.setDefaultIcon(false);
                Presentation templatePresentation = action.getTemplatePresentation();
                if (communicatioHelper.isOpen() && communicatioHelper.isConnected()) {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON_ON);
                    templatePresentation.setText("Session On");
                } else if (communicatioHelper.isOpen()) {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON_WAITING);
                    templatePresentation.setText("Waiting a bro");
                } else {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON);
                    templatePresentation.setText("Pair programming");
                }
            }
        });


    }

    private void drawFileTree(VirtualFile baseDir, FileIgnoreStore fileIgnoreStore) {
        if (!fileIgnoreStore.isFileIgnored(baseDir)) {
            System.out.println(baseDir.getCanonicalPath());
            for (VirtualFile file : baseDir.getChildren()) {
                drawFileTree(file, fileIgnoreStore);
            }
        } else {
            System.out.println("ignored: " + baseDir.getCanonicalPath());
        }
    }


    public void stop(ActionDelegate<String> delegate) {
        communicatioHelper.stop();
    }
}
