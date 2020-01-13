import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private final String fileName;


    private ChatServer(int port, String fileName) {
        if (port != 1500) {
            this.port = 1500;
        } else {
            this.port = port;
        }
        if (!fileName.equals("badwords.txt")) {
            this.fileName = "badwords.txt";
        } else {
            this.fileName = fileName;
        }

    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        String fileName;
        int port;
        if (args.length == 0) {
            port = 1500;
            fileName = "badwords.txt";
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
            fileName = "badwords.txt";
        } else {
            port = Integer.parseInt(args[0]);
            fileName = args[1];
        }
        ChatServer server = new ChatServer(port, fileName);
        server.start();
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Server waiting for clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                if (clients.size() == 0) {
                    Thread t = new Thread(r);
                    clients.add((ClientThread) r);
                    System.out.println(((ClientThread) r).username + " just connected.");
                    t.start();
                } else {
                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).username.equals(((ClientThread) r).username)) {
                            continue;
                        } else {

                            Thread t = new Thread(r);
                            clients.add((ClientThread) r);
                            System.out.println(((ClientThread) r).username + " just connected.");
                            t.start();
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */

        private synchronized void broadcast(String message) {
            Date now = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
            ChatFilter cf = new ChatFilter();
            message = cf.ChatFilter(fileName, message);
            message = simpleDateFormat.format(now) + " " + message;
            System.out.println(message);
            writeMessage(message);
        }

        private synchronized void directMessage(String message, String username) {
            Date now = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
            ChatFilter cf = new ChatFilter();
            message = cf.ChatFilter(fileName, message);
            message = simpleDateFormat.format(now) + " " + this.username + " -> " + username + ": "+ message;
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).username.equals(username) && !this.username.equals(username)) {
                    System.out.println(message);
                    writeDirectMessage(message, clients.get(i));
                }
            }
        }

        private boolean writeMessage(String message) {
            for(int i = 0; i < clients.size(); i++) {
                if (socket.isConnected()) {
                    try {
                        clients.get(i).sOutput.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return socket.isConnected();
        }

        private boolean writeDirectMessage(String message, ClientThread ct) {
                if (socket.isConnected()) {
                    try {
                        ct.sOutput.writeObject(message);
                        sOutput.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            return socket.isConnected();
        }
        private boolean writeListMessage(String message) {
            if (socket.isConnected()) {
                try {
                    sOutput.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return socket.isConnected();
        }

        private synchronized void remove(int id) {
            clients.remove(id);
        }

        public void close() {

            try {
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // Read the username sent to you by client
            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (Exception e) {
                    continue;
                }
                if (cm != null) {
                    if (cm.getType() == 1) {
                        remove(id);
                        close();
                        System.out.println(username + " disconnected with a LOGOUT message.");
                    } else if (cm.getMsg().equals("/list")) {
                        String output = "";
                        for (int i = 0; i < clients.size(); i++) {
                            if (clients.get(i).id != id) {
                                output = output + clients.get(i).username + "\n";
                            }
                        }
                        writeListMessage(output);
                    } else if (cm.getType() == 2) {
                        directMessage(cm.getMsg(), cm.getRecipient());
                    } else {
                        String message = username + ": " + cm.getMsg();
                        broadcast(message);
                    }
                }
            }
        }
    }
}
