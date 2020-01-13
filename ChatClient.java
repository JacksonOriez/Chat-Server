import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private final String server;
    private final String username;
    private final int port;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private ChatClient(String server, int port, String username) {
        this.username = username;
        this.port = port;
        this.server = server;
    }

    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String username;
        int port;
        String serverAddress;

        if (args.length == 0) {
            username = "Anonymous";
            port = 1500;
            serverAddress = "localHost";
        } else if (args.length == 1) {
            username = args[0];
            port = 1500;
            serverAddress = "localHost";
        } else if (args.length == 2) {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverAddress = "localHost";
        } else if (args.length == 3) {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverAddress = args[2];
        } else {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverAddress = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(serverAddress, port, username);
        client.start();

        // Send an empty message to the server
        int type = 0;
        ChatMessage cm;
        while (type == 0 || type == 2) {
            Scanner scanner = new Scanner(System.in);
            String message = scanner.nextLine();
            if (message.equals("/logout")) {
                type = 1;
                message = "";
                cm = new ChatMessage(type, message);
                System.out.println("Server has closed the connection");
            } else if (message.length() > 4) {
                String[] words = message.split(" ");
                if (words.length < 3) {
                    type = 0;
                    cm = new ChatMessage(type, message);
                } else {
                    if (words[0].equals("/msg")) {
                        type = 2;
                        String recipient = words[1];
                        message = "";
                        for (int i = 2; i < words.length; i++) {
                            message = message + words[i] + " ";
                        }
                        cm = new ChatMessage(type, message, recipient);
                    } else {
                        type = 0;
                        cm = new ChatMessage(type, message);
                    }
                }
            } else {
                type = 0;
                cm = new ChatMessage(type, message);
            }
            client.sendMessage(cm);
            if (type == 1) {
                try {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {

            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            boolean again = true;
            while(again) {
                try {
                    {
                        String msg = (String) sInput.readObject();
                        System.out.print(msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    again = false;
                }
            }
        }
    }
}
