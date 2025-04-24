package org.group.server;


import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author babafemi.sorinolu
 */
class ChatClientHandler implements Runnable {

    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String username;

    public ChatClientHandler(Socket s) {
        try {
            this.socket = s;
            //instantiates the input and output stream
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //store the first input from the client socket as username
            username = bufferedReader.readLine();

//            broadcast notification to clients - new user joining
            String notification=username + " has joined the chat";
            System.out.println(notification);
            sendMessageReceivedToOtherClients(username);
        } catch (IOException ex) {
            Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

//        as long as the socket connection is active, keep getting message from client
//        broadcast to the other clients
        while (!socket.isClosed()) {

            try {
                //retrieve message from client
                String messageFromClient = bufferedReader.readLine();

                //disconnect if the clients sends the exit keyword
                if (messageFromClient.equalsIgnoreCase("exit")) {
                    closeConnection();
                    break;
                }

                //broadcast the incoming client message to the other clients
                sendMessageReceivedToOtherClients(username + ":" + messageFromClient);

            } catch (IOException ex) {
//                in the advent of an error, close socket connection
                try {
                    closeConnection();
                    break;
                } catch (IOException ex1) {
                    Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }

        }

    }

    private void sendMessageReceivedToOtherClients(String messageFromClient) {
        try{
//        System.out.println(messageFromClient);
        String[] s_m = messageFromClient.split(":");
        String[] split_message = s_m[1].split(" ");
//        System.out.println(Arrays.toString(split_message));
//        System.out.println(split_message[0]);

        System.out.println("At message received from " + username);

        String to_user = "All";

        if (isAtMessageReceived(split_message[0])) {
            to_user = checkAtWhoMessage(split_message[0]);
            System.out.println("Message sending to " + to_user);
        }
            switch (to_user) {
                case "Server" -> {
                    // send message to llm
                }
                case "All" -> {
//              loop through the clientsLists and send message to the clients except the sender
                    for (ChatClientHandler client : MainServer.clientsList) {
                        try {
//                skip the sender
                            if (client.username.equals(username)) {
                                continue;
                            }
//                send the message to the client
                            client.bufferedWriter.write(messageFromClient);
                            client.bufferedWriter.newLine();
                            client.bufferedWriter.flush();
                        } catch (IOException ex) {
                            Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                default -> {
                    for (ChatClientHandler client : MainServer.clientsList) {
                        try {
//                skip the sender
                            if (client.username.equals(to_user)) {
                                client.bufferedWriter.write(messageFromClient);
                                client.bufferedWriter.newLine();
                                client.bufferedWriter.flush();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("error");;
        }
    }


    private String checkAtWhoMessage(String s) {

        s = s.substring(1);
        System.out.println(s + " is who I am sending to");

        if (s.equalsIgnoreCase("All")){
            return "All";
        } else if (s.equalsIgnoreCase("Server")) {
            return "Server";
        }

        for (ChatClientHandler client : MainServer.clientsList) {
            if (s.equalsIgnoreCase(client.username)) {
                return client.username;
            }
        }
        return "All";
    }

    private void closeConnection () throws IOException {
            //broadcast notification to clients - user leaving the chat
            String notification = username + " has left the chat";
            System.out.println(notification);
            sendMessageReceivedToOtherClients(notification);

            //remove the client socket from the list
            MainServer.clientsList.remove(this);

            //close input and output streams
            bufferedReader.close();
            bufferedWriter.close();

            //close socket connection
            if (!socket.isClosed()) {
                socket.close();
            }

        }

        private boolean isAtMessageReceived (String s){
            return s.startsWith("@");
        }
        
    }
