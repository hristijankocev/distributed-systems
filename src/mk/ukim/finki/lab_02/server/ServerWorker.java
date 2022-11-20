package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;
import mk.ukim.finki.lab_02.classes.Message;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerWorker implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream outputWriter;
    private ObjectInputStream inputReader;

    public ServerWorker(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            this.outputWriter = getOutputWriter();
            this.inputReader = getInputReader();

            Message inputObject;
            Message outputObject;
            Client client = new Client(this.clientSocket.getLocalAddress(), this.clientSocket.getLocalPort());
            while ((inputObject = (Message) this.inputReader.readObject()) != null) {
                System.out.printf("%s Received a message from %s on port %d: %s\n", new Date(),
                        this.clientSocket.getInetAddress().getHostAddress(), this.clientSocket.getPort(), inputObject.getContent());

                if (inputObject.getContent().equals("disconnect")) {
                    closeConnection();
                } else {
                    // Echo the message
                    String message = "I, the server: " + inputObject.getContent();
                    outputObject = new Message(message, client);
                    this.outputWriter.writeObject(outputObject);
                    this.outputWriter.flush();
                }
            }
        } catch (IOException e) {
            System.out.printf("%s Either failed to get an input stream or the client has closed the connection.\n", new Date());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() throws IOException {
        this.outputWriter.close();
        this.inputReader.close();
        this.clientSocket.close();
    }

    private ObjectOutputStream getOutputWriter() throws IOException {
        OutputStream outputStream = this.clientSocket.getOutputStream();
        return new ObjectOutputStream(outputStream);
    }

    private ObjectInputStream getInputReader() throws IOException {
        InputStream inputStream = this.clientSocket.getInputStream();
        return new ObjectInputStream(inputStream);
    }

}
