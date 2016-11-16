package com.sertook.pairprogramming.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sertook.pairprogramming.files.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by florian on 14/11/2016.
 */
public class FileInfos implements Serializable {

    private static final long serialVersionUID = -4168012624313327915L;
    private long lenght;
    private long timeStamp;
    private boolean exist;
    private String path;

    public FileInfos(Project project, VirtualFile virtualFile) {
        path = Utils.getRelativePath(project.getBaseDir(), virtualFile);
        exist = virtualFile.exists();
        timeStamp = virtualFile.getTimeStamp();
        lenght = virtualFile.getLength();
    }


    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileInfos && path != null && path.equals(((FileInfos) obj).path);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isExist() {
        return exist;
    }

    public String getPath() {
        return path;
    }

    public long getLenght() {
        return lenght;
    }

    public static boolean listEquals(List<FileInfos> files, List<FileInfos> files2) {
        if (files == null || files2 == null || files.size() != files2.size())
            return false;
        for (int i = 0; i < files.size(); i++) {
            FileInfos infos1 = files.get(i);
            FileInfos infos2 = files2.get(i);
            if (!infos2.path.equals(infos1.path) || infos1.lenght != infos2.lenght && infos1.exist != infos2.exist) {
                return false;
            }
        }
        return true;
    }
}
