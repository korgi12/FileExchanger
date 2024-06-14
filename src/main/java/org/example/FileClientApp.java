package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.*;

public class FileClientApp extends Application {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Label statusLabel;
    private TextArea messageArea;

    public static void main(String[] args) {
        launch(args);
    }

    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        primaryStage.setTitle("File Transfer Client");

        Label label = new Label("Choose an action:");
        Button sendButton = new Button("Send File");
        Button receiveButton = new Button("Receive File");
        statusLabel = new Label("Status: Waiting for action...");
        messageArea = new TextArea();
        messageArea.setEditable(false);

        sendButton.setOnAction(e -> sendFile(primaryStage));
        receiveButton.setOnAction(e -> receiveFile());

        VBox vbox = new VBox(10, label, sendButton, receiveButton, statusLabel, messageArea);
        vbox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Request req = new Request();
                req.setAction("SEND");
                req.setNameFile(file.getName());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    req.setData(buffer);
                }
                out.write(req.toJson());
                out.newLine();
                out.flush();

                // Read response from the server
//                String serverResponse = (String) ois.readObject();
//                statusLabel.setText("Status: " + serverResponse);
            } catch (IOException e) {
                statusLabel.setText("Status: Error sending file.");
                e.printStackTrace();
            }
        }
    }

    private void receiveFile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Void> future = executor.submit(() -> {
            try {
                Request req = new Request();
                req.setAction("RECEIVE");
                out.write(req.toJson());
                out.newLine();
                out.flush();

                String jsonClient = in.readLine();
                Request request  = parseRequest(jsonClient);


                File receivedFile = new File("received_Client_" + request.getNameFile());
                try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                    fos.write(request.getData());
                }

                // Read the file content to display in the TextArea
                StringBuilder fileContent = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(receivedFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        fileContent.append(line).append("\n");
                    }
                }

                Platform.runLater(() -> {
                    messageArea.setText(fileContent.toString());
                    statusLabel.setText("Status: File received successfully.");
                });
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Status: Error receiving file."));
                e.printStackTrace();
            }
            return null;
        });

        try {
            future.get(10, TimeUnit.SECONDS); // Set timeout here
        } catch (TimeoutException e) {
            Platform.runLater(() -> statusLabel.setText("Status: Error receiving file (timeout)."));
            future.cancel(true); // Cancel the task
        } catch (InterruptedException | ExecutionException e) {
            Platform.runLater(() -> statusLabel.setText("Status: Error receiving file."));
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
    private Request parseRequest(String requestJson) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestJson);

            String action = (String) json.get("action");
            String nameFile = (String) json.get("nameFile");
            String base64Data = (String) json.get("data");
            byte[] data = Base64.getDecoder().decode(base64Data);
            return new Request(action,nameFile, ArrayUtils.removeAllOccurences(data, (byte) 0));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
