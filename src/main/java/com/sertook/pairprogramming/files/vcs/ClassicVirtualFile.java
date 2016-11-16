package com.sertook.pairprogramming.files.vcs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by florian on 15/11/2016.
 */
public class ClassicVirtualFile extends VirtualFile {

    private final File file;

    public ClassicVirtualFile(String path) {
        this.file = new File(path);
    }

    @NotNull
    @Override
    public String getName() {
        return file.getName();
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return null;
    }

    @NotNull
    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        String parent = file.getParent();
        if (parent != null)
            return new ClassicVirtualFile(parent);
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        throw new IllegalStateException("not implemented");
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public long getTimeStamp() {
        return file.lastModified();
    }

    @Override
    public long getLength() {
        return file.length();
    }

    @Override
    public void refresh(boolean b, boolean b1, @Nullable Runnable runnable) {
        throw new IllegalStateException("not implemented");

    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new IllegalStateException("not implemented");
    }
}
