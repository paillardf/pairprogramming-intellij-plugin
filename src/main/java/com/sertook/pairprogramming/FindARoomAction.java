package com.sertook.pairprogramming;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.sertook.pairprogramming.service.PairProgrammingService;
import com.sertook.pairprogramming.ui.FindDialogForm;

import java.io.File;

import static com.sertook.pairprogramming.service.PairProgrammingService.EXTRA_PAIR_PROGRAMMING_KEY;

/**
 * Created by florian on 12/11/2016.
 */
public class FindARoomAction extends AnAction {


    public void actionPerformed(AnActionEvent anActionEvent) {


        final FindDialogForm form = new FindDialogForm(getEventProject(anActionEvent));

        DialogBuilder builder = new DialogBuilder(getEventProject(anActionEvent));
        builder.setCenterPanel(form.getRoot());
        builder.setDimensionServiceKey("FrameSwitcherCloseProjects");
        builder.setTitle("Close Projects");
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();

        boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
        if (isOk) {
            openPairProgrammingProject(form.getIp());
            /*StartupManager.getInstance(project).runWhenProjectIsInitialized((DumbAwareRunnable) () -> {

                ProjectManager.getInstance().reloadProject(project);
            });*/

//            ProjectManager.getInstance().loadAndOpenProject("")
            /*List<Project> checkProjects = form.getCheckProjects();
            for (Project checkProject : checkProjects) {
                if (!checkProject.isDisposed()) {
                    ProjectUtil.closeAndDispose(checkProject);
                }
            }
            FrameSwitcherUtils.getRecentProjectsManagerBase().updateLastProjectPath();*/
        }

    }

    public static void openPairProgrammingProject(String ip) {
        File file = new File(System.getProperty("user.home") + File.separator + "pair" + File.separator + ip);
        if (!file.exists())
            file.mkdirs();

        //Project project = ProjectManager.getInstance().createProject("pairprogramming", file.getPath());


        Project project = ProjectUtil.openOrImport(file.getPath(), null, true);
        project.putUserData(EXTRA_PAIR_PROGRAMMING_KEY, ip);
        PairProgrammingService service = ServiceManager.getService(project, PairProgrammingService.class);
        service.start(ip);
    }


}
