package org.example;

import org.json.simple.JSONObject;
import java.util.Base64;

public class Response {
    private String action;
    private String nameFile;
    private byte[] data;

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

    public String toJson() {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("action", action);
        bodyJson.put("nameFile", nameFile);

        // Encoding byte[] data to Base64
        String base64Data = Base64.getEncoder().encodeToString(data);
        bodyJson.put("data", base64Data);

        return bodyJson.toString();
    }
}