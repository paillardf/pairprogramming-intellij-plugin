package com.sertook.pairprogramming.service;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.sertook.pairprogramming.ActionDelegate;
import com.sertook.pairprogramming.CreateARoomAction;
import com.sertook.pairprogramming.files.SyncFileManager;
import com.sertook.pairprogramming.models.FileInfos;
import com.sertook.pairprogramming.models.FileWrapper;
import com.sertook.pairprogramming.models.ProjectStatus;
import com.sertook.pairprogramming.network.CommunicatioHelper;
import icons.PairProgrammingIcons;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 13/11/2016.
 */
public class PairProgrammingServiceImpl implements PairProgrammingService {
    private final Project project;
    private final SyncFileManager syncFileManager;
    private CommunicatioHelper communicatioHelper = new CommunicatioHelper();

    private Logger logger = Logger.getInstance(this.getClass());


    public PairProgrammingServiceImpl(Project project) {
        this.project = project;
        syncFileManager = new SyncFileManager(project);

    }

    public boolean isStarted() {
        return communicatioHelper.isOpen();
    }


    public List<FileInfos> lastRemotesFiles;
    List<String> remoteNeededFile = ContainerUtil.newArrayList();


    public void start(String ip) {


        communicatioHelper.open(ip, new CommunicatioHelper.CommunicationListener() {


            public void onMessageReceived(Object message) {
                if (message instanceof ProjectStatus) {
                    ProjectStatus remoteProjectStatus = (ProjectStatus) message;
                    List<FileInfos> files = remoteProjectStatus.getFiles();

                    if (!FileInfos.listEquals(files, lastRemotesFiles)) {
                        lastRemotesFiles = files;
                        sendProjectInfos();
                    }
                    if (remoteProjectStatus.getNeededFiles() != null && !remoteProjectStatus.getNeededFiles().isEmpty()) {
                        remoteNeededFile = ContainerUtil.newArrayList(remoteProjectStatus.getNeededFiles());
                    }

                } else if (message instanceof FileWrapper) {
                    ((FileWrapper) message).write(project, new ActionDelegate<VirtualFile>() {
                        @Override
                        public void onSuccess(VirtualFile result) {
                            sendProjectInfos();
                        }

                        @Override
                        public void onError(Exception error) {
                            logger.error("Can write received file", error);
                        }
                    });
                }
            }


            boolean lock = false;

            @Override
            public void onMessageSent() {

                if (!communicatioHelper.isSending() && !lock && !remoteNeededFile.isEmpty()) {
                    lock = true;
                    String path = remoteNeededFile.remove(remoteNeededFile.size() - 1);
                    logger.info(project.getName() + " send : " + path);
                    VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(path);
                    FileWrapper.create(project, virtualFile, new ActionDelegate<FileWrapper>() {
                        @Override
                        public void onSuccess(FileWrapper result) {
                            lock = false;
                            communicatioHelper.sendMessage(result);
                        }

                        @Override
                        public void onError(Exception error) {
                            lock = false;
                            remoteNeededFile.add(path);
                            logger.error("can't write file", error);
                        }
                    });
                }

            }

            public void onServerError(Throwable throwable) {
                if (throwable instanceof SocketException) {
                    stop();
                    new Notification("PairProgramming", "Oups", "We get a network issue", NotificationType.ERROR).notify(project);
                }

            }

            public void onStatusChanged(String title, String message) {

                new Notification("PairProgramming", title, message, NotificationType.INFORMATION).notify(project);

                AnAction action = ActionManager.getInstance().getAction(CreateARoomAction.ID);
                action.setDefaultIcon(false);


                Presentation templatePresentation = action.getTemplatePresentation();
                if (communicatioHelper.isOpen() && communicatioHelper.isConnected()) {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON_ON);
                    templatePresentation.setText("Session On");

                    ProjectStatus projectStatus = new ProjectStatus();
                    projectStatus.setFiles(syncFileManager.getCurrentFilesInfos());
                    communicatioHelper.sendMessage(projectStatus);

                    sendProjectInfos();


                } else if (communicatioHelper.isOpen()) {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON_WAITING);
                    templatePresentation.setText("Waiting a bro");
                } else {
                    templatePresentation.setIcon(PairProgrammingIcons.PAIR_ICON);
                    templatePresentation.setText("Pair programming");
                }
            }
        });

        syncFileManager.start(new SyncFileManager.FileChangeListener() {
            @Override
            public void filesChanged(List<FileInfos> infos) {

                sendProjectInfos();
            }
        });

    }

    public void sendProjectInfos() {

        logger.info(project.getName() + " send project infos");

        List<String> neededFiles = new ArrayList<String>();
        if (lastRemotesFiles != null) {
            neededFiles.addAll(syncFileManager.getNeededFiles(lastRemotesFiles));
        }
        if (communicatioHelper.canSent()) {
            ArrayList<FileInfos> filesInfos = syncFileManager.getCurrentFilesInfos();
            ProjectStatus projectStatus = new ProjectStatus();
            projectStatus.setFiles(filesInfos);
            projectStatus.setNeededFiles(neededFiles);
            communicatioHelper.sendMessage(projectStatus);
        }

    }

    public void stop() {
        lastRemotesFiles = null;
        remoteNeededFile = null;
        communicatioHelper.stop();
        syncFileManager.stop();
    }
}
