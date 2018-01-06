package ru.geekbrains.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;
    boolean isOut = false;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/quite")) {
                            out.writeUTF("/serverclosed");
                            break;
                        }
                        if (str.startsWith("/auth")) { // /auth login72 pass72
                            String[] token = str.split(" ");
                            String newNick = AuthService.getNickByLoginAndPass(token[1], token[2]);
                            if (newNick != null) {
                                sendMsg("/authok");
                                nick = newNick;
                                server.subscribe(this);
                                break;
                            } else {
                                sendMsg("Неверный логин или пароль");
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end") || str.equals("/quite")) {
                            out.writeUTF("/serverclosed");
                            break;
                        }
                        String[] tokenHW = str.split(" ");
                        if (tokenHW.length != 1) {
                            if (tokenHW[0].equals("/w") && tokenHW[1] != null) {
                                server.broadcastMsgToNick(this, getNick() + ": " + str, tokenHW[1]);
                            }
                        } else server.broadcastMsg(getNick() + ": " + str);

                        System.out.println("Client : " + str);
                        //out.writeUTF("echo: " + str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    server.unsubscribe(this);
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (nick == null) {
            try {
                out.writeUTF("/quite");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
