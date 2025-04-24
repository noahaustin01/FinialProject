package org.example;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author babafemi.sorinolu
 */
public class MainServer {

    //arraylist to keep track of all the connected clients
    static ArrayList<ChatClientHandler> clientsList;


    public static void main(String[] args) throws IOException {

        //creates a serversocket connection at port 6000
        ServerSocket serverSocket= new ServerSocket(6000);

        //instantiates the arraylist
        clientsList= new ArrayList<>();

        System.out.println("Server connected");


        while (true) {
//            Accept incoming client socket connection
            Socket s= serverSocket.accept();

            //Spawn a thread to handle the client socket
            ChatClientHandler cch= new ChatClientHandler(s);
            Thread thread= new Thread(cch);
            thread.start();

            //add the client to the arraylist
            clientsList.add(cch);

        }

    }
}
