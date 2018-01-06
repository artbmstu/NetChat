package ru.geekbrains.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {

    private Vector<ClientHandler> clients;

    public Server() {

        clients = new Vector<>();
        ServerSocket server = null;
        try {
            AuthService.connect();
            server = new ServerSocket(22111);
            System.out.println("Сервер запущен");

            Socket socket = null;

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (server != null) server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public void broadcastMsgToNick(ClientHandler client, String msg, String nick){
        boolean isSent = false;
        for (ClientHandler clientTo : clients) {
            if (clientTo.getNick().equals(nick)){
                clientTo.sendMsg(msg);
                isSent = true;
                if (clientTo != client) client.sendMsg(msg);
            }
        }
        if (isSent == false) broadcastMsg(msg);

    }
    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
}
