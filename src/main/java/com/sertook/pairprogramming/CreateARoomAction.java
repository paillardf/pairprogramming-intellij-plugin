package com.sertook.pairprogramming;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.sertook.pairprogramming.service.PairProgrammingService;

/**
 * Created by florian on 12/11/2016.
 */
public class CreateARoomAction extends AnAction {

    public static final String ID = "PairProgramming.CreateARoomAction";

    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = getEventProject(anActionEvent);
        PairProgrammingService service = ServiceManager.getService(project, PairProgrammingService.class);
        if (!service.isStarted()) {
            String ip = project.getUserData(PairProgrammingService.EXTRA_PAIR_PROGRAMMING_KEY);
            service.start(ip);
        } else {
            service.stop();
        }
    }


}
