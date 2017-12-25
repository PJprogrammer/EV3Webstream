package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestPilot;


import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Main extends Application {

    //EV3 Variables
    private RemoteRequestEV3 ev3;
    private RemoteRequestPilot pilot;

    //Networking
    private ServerSocket ss;

    //Webcam
    private BufferedImage image;
    private ImageView imageView;
    private byte[] buffer;
    private BufferedInputStream bis;
    private Boolean stopped = false;

    //Constants
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int NUM_PIXELS = WIDTH * HEIGHT;
    private static final int BUFFER_SIZE = NUM_PIXELS * 2;
    private static final int PORT = 55555;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Get References
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root, 800, 600);
        imageView = (ImageView) scene.lookup("#Hello");

        //SetUpWebcam
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        ss = new ServerSocket(PORT);
        Socket sock = ss.accept();
        buffer = new byte[BUFFER_SIZE];
        bis = new BufferedInputStream(sock.getInputStream());

        startWebStream();

        primaryStage.setTitle("Web Stream");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private int convertYUVtoARGB(int y, int u, int v) {
        int c = y - 16;
        int d = u - 128;
        int e = v - 128;
        int r = (298*c+409*e+128)/256;
        int g = (298*c-100*d-208*e+128)/256;
        int b = (298*c+516*d+128)/256;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }

    private void startWebStream() {
        Task t = new Task(){
            @Override
            protected Object call() {
                while(!stopped){
                    try {
                        int offset = 0;
                        while (offset < BUFFER_SIZE) {
                            offset += bis.read(buffer, offset, BUFFER_SIZE - offset);
                        }
                        for(int i=0;i<BUFFER_SIZE;i+=4) {
                            int y1 = buffer[i] & 0xFF;
                            int y2 = buffer[i+2] & 0xFF;
                            int u = buffer[i+1] & 0xFF;
                            int v = buffer[i+3] & 0xFF;
                            int rgb1 = convertYUVtoARGB(y1,u,v);
                            int rgb2 = convertYUVtoARGB(y2,u,v);
                            image.setRGB((i % (WIDTH * 2)) / 2, i / (WIDTH * 2), rgb1);
                            image.setRGB((i % (WIDTH * 2)) / 2 + 1, i / (WIDTH * 2), rgb2);
                        }

                        //Convert from BufferedImage to Image
                        final WritableImage Image = new WritableImage(WIDTH,HEIGHT);
                        SwingFXUtils.toFXImage(image,Image);

                        Platform.runLater(() -> {
                            //This updates the imageview to newly created Image
                            imageView.setImage(Image);
                        });

                        //Sleep for 100 millisecond
                        Thread.sleep(100);
                    } catch (Exception ex) {
                        //print stack trace or do other stuffs
                    }
                }
                return null;
            }

        };
        new Thread(t).start();
    }

    private void stopWebStream() {
        stopped = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
