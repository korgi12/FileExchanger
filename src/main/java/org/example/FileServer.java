package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.lang3.ArrayUtils;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

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
        private BufferedWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        private Request parseRequest(String requestJson) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(requestJson);

                String action = (String) json.get("action");
                String nameFile = (String) json.get("nameFile");
                if(!action.equals("RECEIVE")) {
                    String base64Data = (String) json.get("data");
                    byte[] data = Base64.getDecoder().decode(base64Data);
                    return new Request(action,nameFile,ArrayUtils.removeAllOccurences(data, (byte) 0));
                }
                return new Request(action,nameFile);
             } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        public void run() {
            while (true) {
                try {
                    String jsonClient = in.readLine();
                    Request request  = parseRequest(jsonClient);
                    if (request.getAction().equals("SEND")) {
                        receiveFile(request);
                    } else if (request.getAction().equals("RECEIVE")) {
                        sendFile(request);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        private void receiveFile(Request request) throws IOException, ClassNotFoundException {
            try (FileOutputStream fos = new FileOutputStream("received_Server_" + request.getNameFile()+".txt")) {
//                byte[] buffer = new byte[4096];
//                long totalBytesRead = 0;
//                int bytesRead;
//                while (totalBytesRead < fileSize && (bytesRead = ois.read(buffer)) != -1) {
//                    fos.write(buffer, 0, bytesRead);
//                    totalBytesRead += bytesRead;
//                }
                fos.write(request.getData());
            }
            System.out.println("File received successfully.");
        }

        private void sendFile(Request request) throws IOException {
            File file = new File("file_to_send");
            try (FileInputStream fis = new FileInputStream(file)) {
                Request req = new Request();
                req.setNameFile(file.getName());
                req.setAction("SEND");
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    req.setData(buffer);
                }

                out.write(req.toJson());
                out.newLine();
                out.flush();
            }
            System.out.println("File sent successfully.");
        }
    }
}
