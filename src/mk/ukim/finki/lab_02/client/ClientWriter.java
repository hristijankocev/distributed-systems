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
                System.out.println(new Date() + " Server said:\n" + incoming.getContent());
            } catch (IOException e) {
                System.out.printf("%s Listener thread exited.\n", new Date());
                System.out.printf("%s Server probably closed the connection.\n", new Date());
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.printf("%s I cannot understand what the server sent me :(", new Date());
            }
        }
    }
}
