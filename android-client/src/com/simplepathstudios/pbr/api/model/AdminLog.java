package com.simplepathstudios.pbr.api.model;

public class AdminLog {
    public AdminLog(String message, String clientId){
        this.message = message;
        this.clientId = clientId;
    }
    public String message;
    public String clientId;
}
