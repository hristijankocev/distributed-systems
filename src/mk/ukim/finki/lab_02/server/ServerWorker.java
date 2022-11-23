package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;
import mk.ukim.finki.lab_02.classes.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class ServerWorker implements Runnable {
    private final Socket clientSocket;
    private final Hashtable<String, Client> users;
    private ObjectOutputStream outputWriter;
    private ObjectInputStream inputReader;

    public ServerWorker(Socket socket, Hashtable<String, Client> users) {
        this.clientSocket = socket;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            this.outputWriter = getOutputWriter();
            this.inputReader = getInputReader();

            Message inputObject;
            Client client = new Client(this.clientSocket.getLocalAddress(), this.clientSocket.getLocalPort());
            while ((inputObject = (Message) this.inputReader.readObject()) != null) {
                System.out.printf("%s Received a message from %s on port %d: %s\n", new Date(),
                        this.clientSocket.getInetAddress().getHostAddress(), this.clientSocket.getPort(), inputObject.getContent());

                String clientMessage = inputObject.getContent();

                if (clientMessage.equals("logout")) {
                    logoutUser();
                    sendMessage(client, "Successfully logged out.");
                } else if (clientMessage.equals("help")) {
                    sendMessage(client, "Possible commands: help, login:<username>, get-users, message:<toUser>:<messageContent>, logout, disconnect(to close the client's terminal)");
                } else if (clientMessage.startsWith("login")) {
                    handleLogin(inputObject);
                }
                // TODO: get-users
                // TODO: Handle messaging between clients
                else {
                    sendMessage(client, "The server didn't understand your message! For possible commands send \"help\"");
                }
                System.out.println(this.users);
            }
            closeConnection();
        } catch (IOException e) {
            logoutUser();
            try {
                closeConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void logoutUser() {
        // Logout the user based on the IP address and port
        InetAddress clientSocketInetAddress = this.clientSocket.getInetAddress();
        int clientSocketPort = this.clientSocket.getPort();

        if (this.users.entrySet().
                removeIf(e -> e.getValue().getAddress().equals(clientSocketInetAddress)
                        && e.getValue().getPort() == clientSocketPort)) {
            System.out.printf("Logged out user %s on port %d.\n", clientSocketInetAddress, clientSocketPort);
        }
    }

    private void handleLogin(Message inputObject) throws IOException {
        String username = getUserFromLoginCommand(inputObject.getContent());
        Client client = inputObject.getClient();
        if (username != null) {
            if (isAlreadyLoggedIn(inputObject)) {
                String u = getUsernameByClient(client);
                sendMessage(client, "You are already logged in as " + u + ".");
            } else {
                if (isUsernameTaken(username)) {
                    sendMessage(client, "Username " + username + " is already taken.");
                } else {
                    // Finally, log the g0d4mn man in…
                    this.users.put(username, client);
                    sendMessage(client, "Successfully logged in as " + username + "!");
                }
            }
        } else {
            sendMessage(client, "Invalid login syntax. login:<username>");
        }
    }

    private boolean isUsernameTaken(String username) {
        return this.users.containsKey(username);
    }

    private boolean isAlreadyLoggedIn(Message messageObj) {
        // Check to see if the address and port match any records in the users hashtable
        InetAddress incomingAddress = messageObj.getClient().getAddress();
        int incomingPort = messageObj.getClient().getPort();

        String username = getUsernameByClient(messageObj.getClient());

        return this.users.entrySet().stream()
                .anyMatch(client -> client.getKey().equals(username)
                        && client.getValue().getAddress().equals(incomingAddress)
                        && client.getValue().getPort() == incomingPort);
    }

    private String getUsernameByClient(Client client) {
        return this.users.entrySet().stream()
                .filter(c -> c.getValue().getPort() == client.getPort()
                        && c.getValue().getAddress().equals(client.getAddress()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String getUserFromLoginCommand(String clientMessage) {
        String[] command = clientMessage.split(":");

        // Validate the command
        if (command.length == 2 && command[0].equals("login")) {
            return command[1];
        }
        return null;
    }

    private void sendMessage(Client client, String message) throws IOException {
        Message outputObject;
        outputObject = new Message(message, client);
        this.outputWriter.writeObject(outputObject);
        this.outputWriter.flush();
    }

    private void closeConnection() throws IOException {
        System.out.printf("%s Connection closed for port %d. ", new Date(), this.clientSocket.getPort());

        if (!this.clientSocket.isClosed())
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
