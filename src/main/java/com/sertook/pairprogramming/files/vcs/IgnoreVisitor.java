package com.sertook.pairprogramming.files.vcs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

/**
 * Created by florian on 14/11/2016.
 */
public class IgnoreVisitor extends PsiElementVisitor {


    @Override
    public void visitElement(PsiElement element) {
        super.visitElement(element);

        String separator = System.getProperty("line.separator");
        if(element instanceof PsiFile){
            separator = ((PsiFile) element).getVirtualFile().getDetectedLineSeparator();
        }

        if(element.getLanguage().getID().equals("TEXT")){
            String[] split = element.getText().split(separator);
            for(String line : split){
                visitLine(line);
            };
        }
    }

    public void visitLine(String line) {
    }
}
