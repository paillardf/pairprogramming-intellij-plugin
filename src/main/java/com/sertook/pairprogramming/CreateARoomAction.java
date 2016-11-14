package com.sertook.pairprogramming;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.sertook.pairprogramming.service.PairProgrammingService;

/**
 * Created by florian on 12/11/2016.
 */
public class CreateARoomAction extends AnAction {

    public static final String ID = "PairProgramming.CreateARoomAction";

    public void actionPerformed(AnActionEvent anActionEvent) {
        PairProgrammingService service = ServiceManager.getService(anActionEvent.getProject(), PairProgrammingService.class);
        if (!service.isStarted()) {
            service.start(null);
        } else {
            service.stop(null);
        }
    }


}
