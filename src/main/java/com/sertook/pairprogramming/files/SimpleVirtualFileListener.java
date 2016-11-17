package com.sertook.pairprogramming.files;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by florian on 14/11/2016.
 */
public abstract class SimpleVirtualFileListener implements VirtualFileListener {


    private final Project project;

    public SimpleVirtualFileListener(Project project) {
        this.project = project;
    }

    abstract void onFileEvent(VirtualFileEvent event);

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
        VirtualFile file = virtualFileEvent.getFile();
        if (file.isDirectory() || Utils.isUnder(file, project.getBaseDir()))
            onFileEvent(virtualFileEvent);
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
        VirtualFile file = virtualFileEvent.getFile();
        if (file.isDirectory() || Utils.isUnder(file, project.getBaseDir()))
            onFileEvent(virtualFileEvent);
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
        VirtualFile file = virtualFileEvent.getFile();
        if (file.isDirectory() || Utils.isUnder(file, project.getBaseDir()))
            onFileEvent(virtualFileEvent);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
        VirtualFile file = virtualFileMoveEvent.getFile();
        if (file.isDirectory() || Utils.isUnder(file, project.getBaseDir()))
            onFileEvent(virtualFileMoveEvent);
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
        VirtualFile file = virtualFileCopyEvent.getFile();
        if (file.isDirectory() || Utils.isUnder(file, project.getBaseDir()))
            onFileEvent(virtualFileCopyEvent);
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
}
