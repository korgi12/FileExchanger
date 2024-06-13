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

import java.io.*;
import java.net.Socket;
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
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());

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

                oos.writeObject("SEND");
                oos.writeObject(file.getName());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, bytesRead);
                }
                oos.flush();

                // Read response from the server
                String serverResponse = (String) ois.readObject();
                statusLabel.setText("Status: " + serverResponse);
            } catch (IOException | ClassNotFoundException e) {
                statusLabel.setText("Status: Error sending file.");
                e.printStackTrace();
            }
        }
    }

    private void receiveFile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Void> future = executor.submit(() -> {
            try {
                oos.writeObject("RECEIVE");

                String fileName = (String) ois.readObject();
                long fileSize =  ois.readLong();
                File receivedFile = new File("received_Client_" + fileName);
                try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                    byte[] buffer = new byte[4096];
                    long totalBytesRead = 0;
                    int bytesRead;
                    while (totalBytesRead < fileSize && (bytesRead = ois.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                    }
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
            } catch (IOException | ClassNotFoundException e) {
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
}
