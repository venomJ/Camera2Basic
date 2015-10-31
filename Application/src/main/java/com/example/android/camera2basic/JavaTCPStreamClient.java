package com.example.android.camera2basic;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cem on 10/27/2015.
 * Two way image streaming client.
 * Uses separate threads for sending and receiving images. Stores the images
 * in blocking queues to decouple the user of this class with TCP logic.
 *
 * TODO: Check if large images would be problematic in terms of memory.
 */
public class JavaTCPStreamClient implements TwoWayImageStreamClient {

    private final int byffer_size = 5;
    public static void main(String[] args){
        TwoWayImageStreamClient client = new JavaTCPStreamClient();
        client.setup("127.0.0.1", 8888);

        //Test send
        byte[] payload = new byte[4];//javax.xml.bind.DatatypeConverter.parseHexBinary("ABCD");
        payload[0] = 0;
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;
        client.sendImage(payload);

        //Test receive
        byte[] receivedImage;
        for(int i = 0; i< 2;i++) {

           receivedImage = client.receiveImage();
           //System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(receivedImage));
        }
    }



    //The images which needs to be sent to the server is to be put here
    //TODO: change the size
    BlockingQueue outGoingQueue = new ArrayBlockingQueue<ByteBuffer>(byffer_size);

    // The images which are received from the server are here
    BlockingQueue incomingQueue = new ArrayBlockingQueue<ByteBuffer>(byffer_size);

    Socket socket = null;

    //Outgoing stream
    OutputStream out = null;
    DataOutputStream dos = null;

    //Incoming Stream
    InputStream in = null;
    DataInputStream dosi = null;


    /**
     * Establishes the connections and starts the worker threads
     * @param serverIP
     * @param serverPort
     */
    @Override
    public void setup(String serverIP, int serverPort){
        connect(serverIP, serverPort);
        startWorkerThreads();
    }

    /**
     * Starts the worker threads that send and receive images
     */
    private void startWorkerThreads(){

        Thread sender = new Thread(new Sender());
        Thread receiver = new Thread(new Receiver());
        //sender.setDaemon(true);
        //receiver.setDaemon(true);
        sender.start();
        receiver.start();

    }

    /**
     * Sends the image to the server.(Puts it to send queue)
     * @param imageBytes
     */
    @Override
    public void sendImage(byte[] imageBytes){
        try {
            outGoingQueue.put(ByteBuffer.wrap(imageBytes));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the image from the server.(Retrieves from the queue)
     * @return the byte of the image
     */
    @Override
    public byte[] receiveImage() {
        try {
            return ((ByteBuffer)incomingQueue.take()).array();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Regular TCP connect method. Also initializes streams.
     * @param serverIP
     * @param serverPort
     */
    private void connect(String serverIP, int serverPort)  {
        try {
            socket = new Socket(serverIP, serverPort);
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
            in = socket.getInputStream();
            dosi = new DataInputStream(in);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
    /**
     *  Write bytes to the server
     */
    private void tcpSendBytes(byte[] bytes){
        try {
            dos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes an image to the server. Currently the protocol only
     * sends an integer before the image that denotes the size of it.
     * @param imageBytes
     */
    private void tcpSendImage(byte[] imageBytes) {
        try {
            dos.writeInt(imageBytes.length);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
        tcpSendBytes(imageBytes);
    }

    /**
     * Receives the image from the server.
     * Assumes each image is prepended with one integer that represent its size.
     * @return
     */
    private byte[] tcpReceiveImage() {
        //Assume first byte is the size of the image
        int imageSize;
        try {
            int readBytes;
            byte[] imageData;

            try {
                imageSize = dosi.readInt();
            }catch(EOFException e){
                return null;
            }
            imageData = new byte[imageSize];
            in.read(imageData,0,imageData.length);

            return  imageData;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }

    /**
     * Sender worker thread class
     * TODO: Might implement a signal to make them stop
     */
    public class Sender implements Runnable
    {

        @Override
        public void run() {
            try {
                while (true) {
                    tcpSendImage(((ByteBuffer) outGoingQueue.take()).array());
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Receiver worker thread class
     * TODO: Might implement a signal to make them stop
     */
    public class Receiver implements Runnable
    {

        @Override
        public void run() {
            try {
                byte[] image;
                while((image = tcpReceiveImage()) != null){
                    incomingQueue.put(ByteBuffer.wrap(image));

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

}
