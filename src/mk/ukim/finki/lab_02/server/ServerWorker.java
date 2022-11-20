package mk.ukim.finki.lab_02.server;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerWorker implements Runnable {
    private final Socket clientSocket;
    private PrintWriter outputWriter;
    private BufferedReader inputReader;

    public ServerWorker(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            this.inputReader = getInputReader();
            this.outputWriter = getOutputWriter();

            String input;
            while ((input = this.inputReader.readLine()) != null) {
                System.out.printf("%s Received a message from %s on port %d: %s\n", new Date(),
                        this.clientSocket.getInetAddress().getHostAddress(), this.clientSocket.getPort(), input);
                if (input.equals("disconnect")) {
                    closeConnection();
                } else {
                    // Echo the message
                    this.outputWriter.println("I, the server: " + input);
                }
            }

        } catch (IOException e) {
            System.out.printf("%s Either failed to get an input stream or the client has closed the connection.\n", new Date());
        }
    }

    private void closeConnection() throws IOException {
        this.outputWriter.close();
        this.inputReader.close();
        this.clientSocket.close();
    }

    private PrintWriter getOutputWriter() throws IOException {
        return new PrintWriter(this.clientSocket.getOutputStream(), true);
    }

    private BufferedReader getInputReader() throws IOException {
        InputStream inputStream = this.clientSocket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }

}
