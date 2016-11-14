package com.sertook.pairprogramming.files;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * Created by florian on 14/11/2016.
 */
public class IgnoreVisitor extends PsiElementVisitor {

    @Override
    public void visitElement(PsiElement element) {
        super.visitElement(element);
        if(element.getLanguage().getID().equals("TEXT")){
            String[] split = element.getText().split(System.getProperty("line.separator"));
            for(String line : split){
                visitLine(line);
            };
        }
    }

    public void visitLine(String line) {
    }
}
