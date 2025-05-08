package org.group.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    ArrayList<String[]> groups = new ArrayList<>();
    String[] groupmembers;


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
        String message = messageFromClient;

        if (isAtMessageReceived(split_message[0])) {
            to_user = checkAtWhoMessage(split_message[0]);
            if (to_user.equalsIgnoreCase("Group")) {
                groupmembers = findGroup(split_message[1]);
                message = String.join(" ", Arrays.copyOfRange(split_message, 2, split_message.length));
            }
            else{
                message = String.join(" ", Arrays.copyOfRange(split_message, 1, split_message.length));
            }
            System.out.println("Message sending to " + to_user);
        }
            switch (to_user) {
                // case @All is used in the case that some sends a message with to everyone
                case "@All" -> {
//              loop through the clientsLists and send message to the clients except the sender
                    for (ChatClientHandler client : MainServer.clientsList) {
                        try {
//                 send the message to the client if it is not the person sending
                            if (!client.username.equals(username)) {
                                client.bufferedWriter.write(s_m[0] + message);
                                client.bufferedWriter.newLine();
                                client.bufferedWriter.flush();
                            }

                        } catch (IOException ex) {
                            Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                // case Group will send the incoming message to all the members of a particular group
                case "Group" -> {
//                    System.out.println("looking at a group: " + groupmembers.toString());
                    for (String member : groupmembers) {
//                        System.out.println("member: " + member);
                        for (ChatClientHandler client : MainServer.clientsList) {
                            try {
//                 send the message to the client if it is not the person sending
                                if (!client.username.equals(member)) {
                                    client.bufferedWriter.write(s_m[0] + message);
                                    client.bufferedWriter.newLine();
                                    client.bufferedWriter.flush();
                                }

                                } catch (IOException ex) {
                                    Logger.getLogger(ChatClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                // case Makegroup is used to make a group for the client to use later
                case "Makegroup" -> {
                    makeGroup(split_message);
                }
                // The defalt case is used in the case that there is no @ message received and is just a message for everyone on the server
                default -> {
                    for (ChatClientHandler client : MainServer.clientsList) {
                        try {
//                skip the sender
                            if (client.username.equals(to_user)) {
                                client.bufferedWriter.write(message);
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

    private void makeGroup(String[] people) {

        // String groupname = people[1]; // gets the group name from the message
        // String[] members = Arrays.copyOfRange(people, 2, people.length); // gets a list of all the members of the new group

        // this will add an Array of the groupname and the members of the new group being created and store it in groups.
        // It will be stored in the form of [groupname, member1, member2, ..., memberN]
        groups.add(Arrays.copyOfRange(people, 1, people.length));

//        System.out.println("New group made");
    }
// This function is used to find all of the members of the group the incoming message is being sent to.
    private String[] findGroup(String groupname) {
        String[] groupmembers = null;
//        System.out.println("looking for the group: " + groupname);
        for (String[] group : groups) {
//            System.out.println(group[0]);
            if (group[0].equalsIgnoreCase(groupname)) {
//                System.out.println("found group");
                groupmembers = Arrays.copyOfRange(group, 1, group.length);
            }
        }
        return groupmembers;
    }

    // This function will check to see who the incoming message is being sent to
    private String checkAtWhoMessage(String s) {

        s = s.substring(1); // removes the @ sign from the string
//        System.out.println(s + " is who I am sending to");

        if (s.equalsIgnoreCase("All")){
            return "@All";
        } else if (s.equalsIgnoreCase("Server")) {
            return "Server";
        } else if (s.equalsIgnoreCase("Group")) {
            return "Group";
        } else if (s.equalsIgnoreCase("Makegroup")) {
            return "Makegroup";
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
