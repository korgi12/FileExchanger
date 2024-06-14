package org.example;

import org.json.simple.JSONObject;
import java.util.Base64;

public class Request {
    private String action;
    private String nameFile;
    private byte[] data;

    public Request(String action, String nameFile) {
        this.action = action;
        this.nameFile = nameFile;
    }

    // Конструктор с параметрами
    public Request(String action, String nameFile, byte[] data) {
        this.action = action;
        this.nameFile = nameFile;
        this.data = data;
    }

    // Пустой конструктор
    public Request() {
    }

    // Геттеры и сеттеры
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // Метод для преобразования объекта в JSON-строку
    public String toJson() {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("action", action);
        if(!action.equals("RECEIVE")) {
        bodyJson.put("nameFile", nameFile);

            // Кодирование массива байтов в строку Base64
            String base64Data = Base64.getEncoder().encodeToString(data);
            bodyJson.put("data", base64Data);
        }
        return bodyJson.toString();
    }
}
