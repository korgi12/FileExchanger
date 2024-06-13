package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Создаем новый поток для обработки клиента
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.ois = new ObjectInputStream(clientSocket.getInputStream());
                this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String command = (String) ois.readObject();

                    if ("SEND".equals(command)) {
                        receiveFile();
                    } else if ("RECEIVE".equals(command)) {
                        sendFile();
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        private void receiveFile() throws IOException, ClassNotFoundException {
            String fileName = (String) ois.readObject();
            long fileSize = ois.readLong();
            try (FileOutputStream fos = new FileOutputStream("received_Server_" + fileName)) {
                byte[] buffer = new byte[4096];
                long totalBytesRead = 0;
                int bytesRead;
                while (totalBytesRead < fileSize && (bytesRead = ois.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
            System.out.println("File received successfully.");
        }

        private void sendFile() throws IOException {
            File file = new File("file_to_send.txt");
            try (FileInputStream fis = new FileInputStream(file)) {
                oos.writeObject(file.getName());
                oos.writeLong(file.length());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, bytesRead);
                }

                oos.flush();
            }
            System.out.println("File sent successfully.");
        }
    }
}
