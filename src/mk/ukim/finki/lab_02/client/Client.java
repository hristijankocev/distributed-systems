package mk.ukim.finki.lab_02.client;

import mk.ukim.finki.lab_02.classes.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;

public class Client implements Runnable {
    private Socket socket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;

    public Client(InetAddress serverAddress, int serverPort) {
        try {
            this.socket = new Socket();
            this.socket.setReuseAddress(true);
            this.socket.connect(new InetSocketAddress(serverAddress, serverPort));
        } catch (IOException e) {
            System.out.printf("Failed to connect to the server \"%s\" on port \"%d\"\n", serverAddress.getHostAddress(), serverPort);
        }
    }

    @Override
    public void run() {
        System.out.printf("%s I am using the port %d.\n", new Date(), this.socket.getLocalPort());
        System.out.printf("%s Started writer for client.\n", new Date());
        try {
            this.writer = getWriter();
            startListener();
            Scanner scanner = new Scanner(System.in);
            String input;

            mk.ukim.finki.lab_02.classes.Client client = new mk.ukim.finki.lab_02.classes.Client(this.socket.getLocalAddress(), this.socket.getLocalPort());

            // Send a hello('help') just so the user knows what he can do
            sendMessage("help", client);

            while (!(input = scanner.nextLine()).equals("end")) {
                sendMessage(input, client);
            }

            this.socket.close();

            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(String messageContent, mk.ukim.finki.lab_02.classes.Client client) throws IOException {
        Message message = new Message(messageContent, client);
        this.writer.writeObject(message);
        this.writer.flush();
    }

    private void startListener() {
        try {
            this.reader = getReader();
            new Thread(new ClientWriter(this.reader)).start();
        } catch (IOException e) {
            System.out.printf("%s Failed to create a listener thread.\n", new Date());
        }
    }

    private ObjectOutputStream getWriter() throws IOException {
        OutputStream outputStream = new BufferedOutputStream(this.socket.getOutputStream());
        return new ObjectOutputStream(outputStream);
    }

    private ObjectInputStream getReader() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        return new ObjectInputStream(inputStream);
    }


    public static void main(String[] args) {
        try {
            System.out.printf("%s Starting client...\n", new Date());

            int serverPort = 6666;
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");

            new Thread(new Client(serverAddress, serverPort)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
