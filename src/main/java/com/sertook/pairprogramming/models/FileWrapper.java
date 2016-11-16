package com.sertook.pairprogramming.models;

import com.intellij.diff.comparison.ComparisonMergeUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sertook.pairprogramming.ActionDelegate;
import com.sertook.pairprogramming.files.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * Created by florian on 15/11/2016.
 */
public class FileWrapper implements Serializable {


    private static final long serialVersionUID = 7642893216346643960L;
    private String path;
    private byte[] data;
    public String fileType;
    private String charset;
    private String fileName;
    private boolean binary;

    public static void create(Project project, VirtualFile virtualFile, final ActionDelegate<FileWrapper> delegate) {

        ApplicationManager.getApplication().runReadAction(new Runnable() {

            @Override
            public void run() {
                try {
                    FileWrapper fileWrapper = new FileWrapper();
                    fileWrapper.path = Utils.getRelativePath(project.getBaseDir(), virtualFile.getParent());
                    fileWrapper.fileName = virtualFile.getName();
                    fileWrapper.data = virtualFile.contentsToByteArray();
                    fileWrapper.fileType = virtualFile.getFileType().getName();
                    fileWrapper.binary = virtualFile.getFileType().isBinary();
                    fileWrapper.charset = virtualFile.getCharset().toString();
                    delegate.onSuccess(fileWrapper);
                } catch (IOException e) {
                    delegate.onError(e);
                }
            }
        });
    }

    @Override
    public String toString() {
        return "File " + path + File.separator + fileName;
    }

    public void write(Project project, final ActionDelegate<VirtualFile> delegate) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        VirtualFile file = project.getBaseDir().findFileByRelativePath(path + File.separator + fileName);
                        if (file != null && file.exists()) {
                            if (file.getFileType().equals(fileType) && !binary) {
                                Document document = FileDocumentManager.getInstance().getDocument(file);
                                String oldText = document.getText();
                                CharSequence result = ComparisonMergeUtil.tryResolveConflict(oldText, oldText, new String(data, Charset.forName(charset)));
                                if (result == null) {
                                    result = oldText;
                                }
                                document.setText(result);
                                FileDocumentManager.getInstance().saveDocument(document);
                            }
                        } else if (file == null) {
                            try {
                                VfsUtil.createDirectoryIfMissing(project.getBaseDir(), path);
                                VirtualFile childData = project.getBaseDir().findFileByRelativePath(path).createChildData(this, fileName);
                                childData.setBinaryContent(data);
                                childData.refresh(true, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        data = null;
                    }
                });

            }
        });

    }
}
