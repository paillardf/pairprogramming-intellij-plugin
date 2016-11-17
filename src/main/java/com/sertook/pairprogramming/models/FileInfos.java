package com.sertook.pairprogramming.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sertook.pairprogramming.files.Utils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by florian on 14/11/2016.
 */
public class FileInfos implements Serializable {

    private static final long serialVersionUID = -4168012624313327915L;
    private byte[] md5;
    private long timeStamp;
    private boolean exist;
    private String path;

    public FileInfos(Project project, VirtualFile virtualFile) {
        path = Utils.getRelativePath(project.getBaseDir(), virtualFile);
        exist = virtualFile.exists();
        if (exist) {
            timeStamp = virtualFile.getTimeStamp();
            try {
                md5 = DigestUtils.md5(virtualFile.contentsToByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            timeStamp = System.currentTimeMillis();
        }
    }


    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileInfos && path != null && path.equals(((FileInfos) obj).path);
    }

    public boolean isIdentical(FileInfos fileInfos) {
        return fileInfos.equals(this) && Arrays.equals(md5, fileInfos.md5) && exist == fileInfos.exist;
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


    public static boolean listEquals(List<FileInfos> files, List<FileInfos> files2) {
        if (files == null || files2 == null || files.size() != files2.size())
            return false;
        for (int i = 0; i < files.size(); i++) {
            FileInfos infos1 = files.get(i);
            FileInfos infos2 = files2.get(i);
            if (!infos1.isIdentical(infos2)) {
                return false;
            }
        }
        return true;
    }
}
