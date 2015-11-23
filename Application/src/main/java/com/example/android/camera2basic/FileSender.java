package com.example.android.camera2basic;
import java.io.File;
import java.io.InputStream;
import java.lang.*;
import java.io.OutputStream;
import java.net.Socket;


public class FileSender {
    // host and port of receiver
    private static final int    port = 8888;
//    private static final String host = "localhost";
    private static final String host = "128.2.213.223";


    public static void main(String[] args) {
        try {
            Socket       socket = new Socket(host, port);
            OutputStream os     = socket.getOutputStream();
            InputStream is     = socket.getInputStream();

            int cnt_files = args.length;
            int windowSize = 5;
            // How many files?
            ByteStream.toStream(os,windowSize);

            int cur_file = 0;
            for (; cur_file< 5; cur_file++) {

                ByteStream.toStream(os, new File(args[cur_file]));
                System.out.println("File " + cur_file + " sent.");
            }

            ByteStream.toInt(is);
            ByteStream.toFile(is,new File("recv_"+ cur_file+".jpg"));
            System.out.println("Processed image  " + cur_file/5 + " received.");

            ByteStream.toStream(os, windowSize);
            for (; cur_file< 10; cur_file++) {
                ByteStream.toStream(os, new File(args[cur_file]));
                System.out.println("File " + cur_file + " sent.");

            }
            ByteStream.toInt(is);
            ByteStream.toFile(is,new File("recv_"+ cur_file+".jpg"));
            System.out.println("Processed image  " + cur_file / 5 + " received.");

            ByteStream.toStream(os, windowSize);
            for (; cur_file< 15; cur_file++) {
                ByteStream.toStream(os, new File(args[cur_file]));
                System.out.println("File " + cur_file + " sent.");

            }
            ByteStream.toInt(is);
            ByteStream.toFile(is,new File("recv_"+ cur_file+".jpg"));
            System.out.println("Processed image  " + cur_file / 5 + " received.");


            System.out.println("Sending terminate");

            ByteStream.toStream(os, -1);
            System.out.println("Terminate success. Bye!");


        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
