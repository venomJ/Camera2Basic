package com.example.android.camera2basic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
//import javax.xml.bind.DatatypeConverter;

/**
 * Created by cem on 10/28/2015.
 * Test Java server.
 */
public class JavaTCPServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket welcomeSocket = new ServerSocket(8888);
        Socket connectionSocket = welcomeSocket.accept();
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        byte[] imageData = new byte[4];//DatatypeConverter.parseHexBinary("ffffffff");
        imageData[0] = 0;
        imageData[1] = 0;
        imageData[2] = 0;
        imageData[3] = 0;


        //Send 2 images
        for(int i = 0; i< 2;i++) {
            outToClient.writeInt(4);
            outToClient.write(imageData);
            Thread.sleep(500);
        }

        //Receive images
        DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
        int imageSize = inFromClient.readInt();
        byte[] receivedData = new byte[imageSize];
        inFromClient.read(receivedData);

        //System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(receivedData));
        outToClient.close();
        inFromClient.close();
        connectionSocket.close();
    }
}
