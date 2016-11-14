package com.sertook.pairprogramming;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by florian on 13/11/2016.
 */
public class CommunicatioHelper {

    private ExecutorService threadPool;
    private ServerRunnable serverRunnable;

    public CommunicatioHelper() {


    }

    public void open(String ip, CommunicationListener listener) {
        if (isOpen())
            throw new IllegalStateException("connection already opened");

        serverRunnable = new ServerRunnable(ip, listener);
        threadPool = Executors.newFixedThreadPool(2);
        threadPool.submit(serverRunnable);
    }

    public void sendMessage(final String msg) {
        if (isOpen() && isConnected()) {
            throw new IllegalStateException("connection closed");
        }
        threadPool.submit(new Runnable() {
            public void run() {
                serverRunnable.getPrintWriter().write(msg);
            }
        });
    }

    public void stop() {
        serverRunnable = null;
        if (threadPool != null)
            threadPool.shutdownNow();
        threadPool = null;
    }

    public boolean isOpen() {
        return serverRunnable != null && threadPool != null;
    }

    public boolean isConnected() {
        return serverRunnable != null && serverRunnable.getPrintWriter() != null;
    }


    public static class ServerRunnable implements Runnable {

        private final CommunicationListener listener;
        private final String ip;
        private PrintWriter printWriter;

        public ServerRunnable(String ip, CommunicationListener listener) {
            this.listener = new ListenerWrapper(listener);
            this.ip = ip;
        }

        public void run() {
            listener.onStatusChanged();

            BufferedReader socketRead = null;
            Socket socket = null;
            try {
                ServerSocket serverSocket = null;
                if (ip == null) {
                    serverSocket = new ServerSocket(0);
                    socket = serverSocket.accept();
                } else {
                    socket = new Socket(ip, 0);
                }
                OutputStream ostream = socket.getOutputStream();
                printWriter = new PrintWriter(ostream, true);
                // receiving the contents from server.  Uses input stream
                InputStream istream = socket.getInputStream();
                socketRead = new BufferedReader(new InputStreamReader(istream));
                listener.onStatusChanged();

                String str;
                while ((str = socketRead.readLine()) != null) // reading line-by-line
                {
                    listener.onMessageReceived(str);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                listener.onServerError(e);
            } finally {
                try {
                    if (printWriter != null)
                        printWriter.close();
                    if (socketRead != null)
                        socketRead.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onServerError(e);
                }
            }
            listener.onStatusChanged();
        }

        PrintWriter getPrintWriter() {
            return printWriter;
        }

    }


    private static class ListenerWrapper implements CommunicationListener {
        private final CommunicationListener listener;

        public ListenerWrapper(CommunicationListener listener) {
            this.listener = listener;
        }

        public void onMessageReceived(final String msg) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onMessageReceived(msg);
                }
            });
        }

        public void onServerError(final Throwable throwable) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onServerError(throwable);

                }
            });
        }

        public void onStatusChanged() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onStatusChanged();
                }
            });
        }
    }

    public interface CommunicationListener {
        void onMessageReceived(String msg);

        void onServerError(Throwable throwable);

        void onStatusChanged();
    }

}
