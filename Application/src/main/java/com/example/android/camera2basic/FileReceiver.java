import java.io.OutputStream;
import java.lang.*;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class FileReceiver implements Runnable {

    private static final int port = 8888;
    private AtomicInteger receivedFiles = new AtomicInteger(0);
    private final int windowSize = 5;

    private Socket socket;

    public static void main(String[] _) {
        try {
            System.out.println("File Receiver running...");
            ServerSocket listener = new ServerSocket(port);

            while (true) {
                FileReceiver file_rec = new FileReceiver();
                file_rec.socket = listener.accept();
                System.out.println("Connection received.");


                new Thread(file_rec).start();
            }
        }
        catch (java.lang.Exception ex) {
            ex.printStackTrace(System.out);
        }
    }

    public void run() {
        InputStream in;
        boolean jobDone = false;
        int nof_files = 0;
        while(!jobDone) {
            try {
                System.out.println("------------Start processing image batch-------------");
                in = socket.getInputStream();

                nof_files = ByteStream.toInt(in);
                if(nof_files == -1) {
                    jobDone = true;
                    continue;
                }
                System.out.println("Number of files in this batch: " + nof_files);
                for (int cur_file = 0; cur_file < nof_files; cur_file++) {
                    //String file_name = ByteStream.toString(in);

                    DecimalFormat formatter = new DecimalFormat("000");
                    String formatted = formatter.format(receivedFiles);
                    File file = new File("out-" + formatted + ".jpg");

                    System.out.println("Reading file from socket.");
                    ByteStream.toFile(in, file);
                    System.out.println("File " + file.getName() + " saved to disk.");
                    receivedFiles.incrementAndGet();

                }
                System.out.println("-------------This batch of images processed------------");
                if (receivedFiles.get() == windowSize) {
                    System.out.println("Executing superres... ");
                    ProcessBuilder pb  = new ProcessBuilder("./parallelSuperres.sh", "2", "20", "2", "out",  ""+ windowSize,"8");
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    Process p= pb.start();
                    int rc = p.waitFor();
                    System.out.printf("Superres executed with exit code %d\n", rc);

                    OutputStream os  = socket.getOutputStream();


                    // How many files?
                    ByteStream.toStream(os, 1);
                    ByteStream.toStream(os,new File("./joined/out-003.jpg"));
                    System.out.println("Processed file sent back.");
                    receivedFiles.set(0);
                    jobDone = true;
                }
            } catch (java.lang.Exception ex) {
                ex.printStackTrace(System.out);
            }
        }

    }
}