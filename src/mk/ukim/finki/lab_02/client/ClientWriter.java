package mk.ukim.finki.lab_02.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

public class ClientWriter implements Runnable {
    BufferedReader bufferedReader;

    public ClientWriter(BufferedReader br) {
        this.bufferedReader = br;
    }

    public void run() {
        System.out.printf("%s Started listener for client.\n", new Date());
        while (true) {
            String incoming;
            try {
                incoming = bufferedReader.readLine();
                System.out.println("Server said: " + incoming);
            } catch (IOException e) {
                System.out.printf("%s Listener thread failed.\n", new Date());
                System.exit(1);
            }
        }
    }
}
