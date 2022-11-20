package mk.ukim.finki.lab_02.client;

import mk.ukim.finki.lab_02.classes.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

public class ClientWriter implements Runnable {
    ObjectInputStream objectInputStream;

    public ClientWriter(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public void run() {
        System.out.printf("%s Started listener for client.\n", new Date());
        while (true) {
            try {
                Message incoming = (Message) this.objectInputStream.readObject();
                System.out.println("Server said: " + incoming.getContent());
            } catch (IOException e) {
                System.out.printf("%s Listener thread failed.\n", new Date());
                e.printStackTrace();
                System.exit(1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
