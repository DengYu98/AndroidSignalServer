package com.example.androidsignalserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String IP = "";
    public static final int PORT = 12345;
    private static final String NAMESPACE = "/chat";
    private SocketIOServer socketIOServer;
    private ConnectListener connectListener;
    private DataListener<String> dataListener;
    private SocketIONamespace socketIONamespace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Configuration config = getConfig(IP, PORT);
        socketIOServer = new SocketIOServer(config);
        socketIONamespace = socketIOServer.addNamespace(NAMESPACE);
        socketIOServer.addConnectListener(client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            String roomId = handshakeData.getSingleUrlParam("roomId");
            client.joinRoom(roomId);
            client.sendEvent("connected", "200");
        });

        socketIOServer.addEventListener("message", String.class, (client, data, ackSender) -> {
            // 解析data
            // 一个用户仅仅只有一个房间,如果一个用户在多房间内，遍历set
            // sendEvent(eventname,data) 向本广播对象中的全体客户端发送广播。
            // sendEvent(eventname,excludeSocketIOClient,data) 排除指定客户端广播。
            socketIONamespace.getRoomOperations(client.getAllRooms().iterator().next()).sendEvent("offer",client,data);
        });
        new Thread(() -> {
            socketIOServer.start();
        }).start();
    }


    private Configuration getConfig(String ip, int port) {
        Configuration config = new Configuration();
        config.setHostname(ip);
        config.setPort(port);
        config.setOrigin(null); // 允许跨域
        return config;
    }
}