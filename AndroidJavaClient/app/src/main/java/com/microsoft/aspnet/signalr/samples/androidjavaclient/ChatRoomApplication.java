package com.microsoft.aspnet.signalr.samples.androidjavaclient;

import android.app.Application;

import com.microsoft.aspnet.signalr.Action1;
import com.microsoft.aspnet.signalr.Action2;
import com.microsoft.aspnet.signalr.HubConnection;
import com.microsoft.aspnet.signalr.HubConnectionState;

import java.util.function.Consumer;

public class ChatRoomApplication extends Application {
    private HubConnection hubConnection = new HubConnection("https://chatroomignite1.azurewebsites.net/chat");
    private String username = "Android Client";

    public void setUsername(String name) {
        username = name;
    }

    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }

    public void connect() throws Exception {
        if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
            hubConnection.start();
            hubConnection.send("broadcastMessage", "_SYSTEM_", username + " JOINED");
        }
    }

    public void onClosed(Action1<Exception> callback) {
        hubConnection.onClosed(new Consumer<Exception>() {
            @Override
            public void accept(Exception e) {
                callback.invoke(e);
            }
        });
    }

    public void onMessage(Action2<String, String> callback) {
        hubConnection.on("broadcastMessage", callback, String.class, String.class);
    }

    public void sendMessage(String message) throws Exception {
        hubConnection.send("broadcastMessage", username, message);
    }
}
