package com.sertook.pairprogramming.network;

import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by florian on 13/11/2016.
 */
public class CommunicatioHelper {

    private static Logger logger = Logger.getInstance(CommunicatioHelper.class);

    private ExecutorService threadPool;
    private ServerRunnable serverRunnable;
    private boolean isSending;
    private CommunicationListener listener;

    public CommunicatioHelper() {


    }

    public void open(String ip, CommunicationListener listener) {
        if (isOpen())
            throw new IllegalStateException("connection already opened");

        this.listener = new ListenerWrapper(listener);
        serverRunnable = new ServerRunnable(ip, listener);
        threadPool = Executors.newFixedThreadPool(2);
        threadPool.submit(serverRunnable);
    }

    public void sendMessage(final Object message) {
        if (!canSent()) {
            throw new IllegalStateException("connection closed");
        }

        isSending = true;
        threadPool.submit(new Runnable() {
            public void run() {
                try {
                    serverRunnable.getOutput().writeObject(message);
                } catch (IOException e) {
                    logger.error("Can't send object " + message.toString(), e);
                    if (listener != null)
                        listener.onServerError(e);
                }
                isSending = false;
                if (listener != null)
                    listener.onMessageSent();

            }
        });
    }

    public boolean isSending() {
        return isSending;
    }

    public void stop() {
        serverRunnable = null;
        if (serverRunnable != null)
            serverRunnable.finish();
        if (threadPool != null)
            threadPool.shutdown();
        threadPool = null;
    }

    public boolean isOpen() {
        return serverRunnable != null && threadPool != null;
    }

    public boolean isConnected() {
        return serverRunnable != null && serverRunnable.getOutput() != null;
    }

    public boolean canSent() {
        return isOpen() && isConnected();
    }


    public static class ServerRunnable implements Runnable {

        private final CommunicationListener listener;
        private String ip;
        private int port;
        private ObjectOutputStream output;
        ServerSocket serverSocket = null;
        private boolean finish;

        public ServerRunnable(String ip, CommunicationListener listener) {
            this.listener = listener;
            if (ip != null) {
                String[] ipSplit = ip.split(":");
                this.ip = ipSplit[0];
                this.port = Integer.parseInt(ipSplit[1]);
            }
        }

        public void finish() {
            this.finish = true;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    serverSocket = null;
                } catch (IOException e) {

                }
            }
        }


        public void run() {
            ObjectInputStream socketRead = null;
            Socket socket = null;
            try {

                if (ip == null) {
                    serverSocket = new ServerSocket(0);
                    listener.onStatusChanged("Server started", "Your bro can join on: " + InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort());
                    socket = serverSocket.accept(); //TODO unlock
                } else {
                    socket = new Socket(ip, port);
                }
                OutputStream ostream = socket.getOutputStream();
                output = new ObjectOutputStream(ostream);
                // receiving the contents from server.  Uses input stream
                InputStream istream = socket.getInputStream();
                socketRead = new ObjectInputStream(istream);
                listener.onStatusChanged("Bro connected", "Let's program");

                Object message;
                while ((message = socketRead.readObject()) != null && !finish) // reading line-by-line
                {
                    listener.onMessageReceived(message);
                }
            } catch (final Exception e) {
                listener.onServerError(e);
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (socketRead != null)
                        socketRead.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    logger.error("release stream error", e);
                    listener.onServerError(e);
                }
            }
            listener.onStatusChanged("Server stopped", "Time to work alone :p");
        }

        ObjectOutputStream getOutput() {
            return output;
        }

    }


    private static class ListenerWrapper implements CommunicationListener {
        private final CommunicationListener listener;

        public ListenerWrapper(CommunicationListener listener) {
            this.listener = listener;
        }

        public void onMessageReceived(final Object message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onMessageReceived(message);
                }
            });
        }

        @Override
        public void onMessageSent() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onMessageSent();
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

        public void onStatusChanged(String title, String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    listener.onStatusChanged(title, message);
                }
            });
        }
    }

    public interface CommunicationListener {
        void onMessageReceived(Object message);

        void onMessageSent();

        void onServerError(Throwable throwable);

        void onStatusChanged(String title, String message);
    }

}
