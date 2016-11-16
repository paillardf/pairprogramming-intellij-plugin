package com.sertook.pairprogramming.files;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.sertook.pairprogramming.files.vcs.IgnoreFileProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 14/11/2016.
 */
public class Utils {

    @Nullable
    public static String getRelativePath(@NotNull VirtualFile directory, @NotNull VirtualFile file) {
        if (directory.getPath().equals(file.getPath()))
            return "";
        String relativePath = VfsUtilCore.getRelativePath(file, directory, File.separatorChar);
        if (relativePath == null) {
            relativePath = "";
        }
        String path = relativePath + (file.isDirectory() ? File.separatorChar : "");
        return path;
    }

    public static boolean isUnder(@NotNull VirtualFile file, @NotNull VirtualFile directory) {
        VirtualFile parent = file.getParent();
        while (parent != null) {
            if (directory.equals(parent)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static void findFiles(VirtualFile file, List<IgnoreFileProvider> ignoreFileProviders, ArrayList<VirtualFile> ignoreFiles) {
        if (file.isDirectory()) {

            for (IgnoreFileProvider ignoreFileProvider : ignoreFileProviders) {
                for (String folderName : ignoreFileProvider.ignoredFolder()) {
                    if (file.getName().equals(folderName)) {
                        return;
                    }
                }
            }

            for (VirtualFile v : file.getChildren()) {
                findFiles(v, ignoreFileProviders, ignoreFiles);
            }
        } else {
            for (IgnoreFileProvider ignoreFileProvider : ignoreFileProviders) {
                if (ignoreFileProvider.getFileName().equals(file.getName())) {
                    ignoreFiles.add(file);
                    return;
                }
            }
        }
    }
}
