//package org.example;
//
//import java.io.*;
//import java.net.Socket;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class ClientHandler implements Runnable {
//    private Socket clientSocket;
//    private ObjectInputStream ois;
//    private ObjectOutputStream oos;
//
//    public ClientHandler(Socket socket) {
//        this.clientSocket = socket;
//        try {
//            this.ois  = new ObjectInputStream(clientSocket.getInputStream());
//            this.oos =  new ObjectOutputStream(clientSocket.getOutputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void run() {
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        executor.submit(this::handleRead);
//        executor.submit(this::handleWrite);
//    }
//
//    public void handleRead() {
//        try {
//            String message;
//            while (true) {
//                while ((message = in.readLine()) != null) {
//                    Request request = parseRequest(message);
//                    Response response = handler.handle(request);
//                    if (messageListener != null) {
//                        messageListener.onMessage(response);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void handleWrite() {
//        while (true) {
//            try {
//
//                Response message = messageQueue.take();
//                System.out.println("Получено" + message.toJson());
//                message.setPlayerId(playerId);
//                out.write(message.toJson());
//                out.newLine();
//                out.flush();
//            } catch (InterruptedException | IOException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
//
//    @Override
//    public void run() {
//        try () {
//
//            String fileName = dis.readUTF();
//            long fileSize = dis.readLong();
//
//            File file = new File("received_" + fileName);
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                byte[] buffer = new byte[4096];
//                int read;
//                long totalRead = 0;
//                while ((read = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) > 0) {
//                    totalRead += read;
//                    fos.write(buffer, 0, read);
//                }
//            }
//
//            System.out.println("File " + fileName + " received successfully.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
