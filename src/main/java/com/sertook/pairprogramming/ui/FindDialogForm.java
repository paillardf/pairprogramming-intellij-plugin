package com.sertook.pairprogramming.ui;

import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * Created by florian on 14/11/2016.
 */
public class FindDialogForm {
    private JPanel root;
    private JTextField textField;
    private JProgressBar progressBar;


    public FindDialogForm(Project project) {
        progressBar.setMaximum(100);
    }

    public JPanel getRoot() {
        return root;
    }

    public String getIp() {
        return textField.getText();
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }
}
