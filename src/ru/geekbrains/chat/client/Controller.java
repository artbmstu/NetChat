package ru.geekbrains.chat.client;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller{

    public TextArea chatArea;
    public TextField msgField;
    public HBox upperPanel;
    public TextField loginField;
    public PasswordField passwordField;
    public HBox bottomPanel;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADDRESS = "localhost";
    final int PORT = 22111;

    private boolean isAuthorized;

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        upperPanel.setVisible(!isAuthorized);
        upperPanel.setManaged(!isAuthorized);

        bottomPanel.setVisible(isAuthorized);
        bottomPanel.setManaged(isAuthorized);
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            new Thread(() -> {
                try {

                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/quite")) {
                            out.writeUTF("/quite");
                            break;
                        }
                        if (str.startsWith("/authok")) {
                            setAuthorized(true);
                            break;
                        } else {
                            chatArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if(str.equals("/serverclosed")) {
                            out.writeUTF("/quite");
                            break;
                        }
                        chatArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                        //Platform.exit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    setAuthorized(false);
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
