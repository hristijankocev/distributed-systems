package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;
import mk.ukim.finki.lab_02.classes.Job;
import mk.ukim.finki.lab_02.classes.Message;
import mk.ukim.finki.lab_02.classes.TransientMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerWorker implements Runnable {
    private final Socket clientSocket;
    private final Hashtable<String, Client> users;
    private ObjectOutputStream outputWriter;
    private ObjectInputStream inputReader;
    private final Lock lock = new ReentrantLock();

    public ServerWorker(Socket socket, Hashtable<String, Client> users) {
        this.clientSocket = socket;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            this.outputWriter = getOutputWriter();
            this.inputReader = getInputReader();

            // Create the helper thread worker
            new Thread(new ServerWorkerHelper(Integer.parseInt(Thread.currentThread().getName()), this.lock, this.outputWriter, this.users))
                    .start();

            Message inputObject;
            Client client = new Client(this.clientSocket.getLocalAddress(), this.clientSocket.getLocalPort());
            while ((inputObject = (Message) this.inputReader.readObject()) != null) {
                System.out.printf("%s Received a message from %s on port %d: %s\n", new Date(),
                        this.clientSocket.getInetAddress().getHostAddress(), this.clientSocket.getPort(), inputObject.getContent());

                String clientMessage = inputObject.getContent();

                if (clientMessage.equals("logout")) {
                    if (isAlreadyLoggedIn(inputObject)) {
                        logoutUser();
                        sendMessage(client, "Successfully logged out.");
                    } else {
                        sendMessage(client, "You are not even logged in...");
                    }
                } else if (clientMessage.equals("help")) {
                    sendMessage(client, "Possible commands:\n" +
                            "\tlogin:<username>\t\t\t\t\t\t- login to the server\n" +
                            "\tget-users\t\t\t\t\t\t\t\t- get a list of online users\n" +
                            "\tmessage:<to-user>:<message-content> \t- send a message to another user\t\n" +
                            "\tlogout\t\t\t\t\t\t\t\t\t- logout from the server \n" +
                            "\tend\t\t\t\t\t\t\t\t\t\t- close the client\n" +
                            "\thelp\t\t\t\t\t\t\t\t\t- receive a list of commands");
                } else if (clientMessage.startsWith("login")) {
                    handleLogin(inputObject);
                } else if (clientMessage.equals("get-users")) {
                    if (isAlreadyLoggedIn(inputObject)) {
                        sendMessage(client, getUserList(getUsernameByClient(inputObject.getClient())));
                    } else {
                        sendMessage(client, "You need to be logged-in in order to do that!");
                    }
                } else if (clientMessage.startsWith("message:")) {
                    if (isAlreadyLoggedIn(inputObject)) {
                        sendMessageToAnotherUser(inputObject);
                    } else {
                        sendMessage(client, "You need to be logged-in in order to do that!");
                    }
                } else {
                    sendMessage(client, "The server didn't understand your message! For possible commands send \"help\"");
                }
            }

            removeThreadEntries();

            closeConnection();
        } catch (IOException e) {
            logoutUser();
            try {
                removeThreadEntries();

                closeConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void removeThreadEntries() {
        // Remove the workerThread entry
        Server.workerThreads.entrySet().stream()
                .filter(e -> e.getKey().getAddress().equals(this.clientSocket.getInetAddress())
                        && e.getKey().getPort() == this.clientSocket.getPort())
                .findFirst().ifPresent(entryToBeRemoved -> Server.workerThreads.remove(entryToBeRemoved.getKey()));

        // Remove the entry for the thread job
        Server.threadJobs.entrySet().stream()
                .filter(e -> e.getKey().getName().equals(Thread.currentThread().getName()))
                .findFirst()
                .ifPresent(entryToBeRemoved -> Server.threadJobs.remove(entryToBeRemoved.getKey()));
    }

    private void sendMessageToAnotherUser(Message inputObject) throws IOException {
        this.lock.lock();
        String[] command = inputObject.getContent().split(":");
        if (command[0].equals("message") && command.length == 3) {
            String user = command[1];
            if (this.users.containsKey(user)) {
                // Check if that user is still logged in (could've logged out in the meantime I guess)
                Map.Entry<String, Client> wanted = this.users.entrySet().stream()
                        .filter((e) -> (e.getKey().equals(user))).findFirst().orElse(null);

                if (wanted != null) {
                    String message = command[2];

                    // Find the thread responsible for the communication tunnel with the user
                    Map.Entry<Client, Thread> theChosenEntry = Server.workerThreads.entrySet().stream()
                            .filter((e) -> (e.getKey().getPort() == wanted.getValue().getPort()
                                    && e.getKey().getAddress().equals(wanted.getValue().getAddress())))
                            .findFirst()
                            .orElse(null);

                    if (theChosenEntry != null) {
                        Thread theChosenThread = theChosenEntry.getValue();

                        TransientMessage transientMessage = new TransientMessage(inputObject.getClient(),
                                theChosenEntry.getKey(), message);

                        Job threadJob = new Job(true, transientMessage);

                        synchronized (Server.threadJobs) {
                            Server.threadJobs.put(theChosenThread, threadJob);
                            // Issue Semaphore tickets and tell my ServerWorkerHelper thread that
                            // he can operate with input stream (unlock the lock)
                            // The amount of tickets released will be the amount of active worker threads
                            // This means that every worker helper thread will check if there is a job for him
                            Server.semaphore.release(Server.workerThreads.size());
                        }
                        sendMessage(inputObject.getClient(), "Message to " + user + " sent successfully!");
                    } else {
                        System.out.println("The thread couldn't be found :(");
                    }
                } else {
                    sendMessage(inputObject.getClient(), "Couldn't send message to user " + user);
                }
            } else {
                sendMessage(inputObject.getClient(), "The specified user does not exist!");
            }
        } else {
            sendMessage(inputObject.getClient(), "Invalid send message syntax. Please use message:<to-user>:<message-content>");
        }
        this.lock.unlock();
    }

    private String getUserList(String excludeUsername) {
        StringJoiner stringJoiner = new StringJoiner(",");
        this.users.keySet().forEach(k -> {
            if (!k.equals(excludeUsername)) {
                stringJoiner.add(k);
            }
        });
        return "Online users:\n" + stringJoiner;
    }

    private void logoutUser() {
        // Logout the user based on the IP address and port
        InetAddress clientSocketInetAddress = this.clientSocket.getInetAddress();
        int clientSocketPort = this.clientSocket.getPort();

        if (this.users.entrySet().
                removeIf(e -> e.getValue().getAddress().equals(clientSocketInetAddress)
                        && e.getValue().getPort() == clientSocketPort)) {
            System.out.printf("%s Logged out user %s on port %d.\n", new Date(), clientSocketInetAddress, clientSocketPort);
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

    public void sendMessage(Client client, String message) throws IOException {
        this.lock.lock();
        Message outputObject;
        outputObject = new Message(message, client);
        this.outputWriter.writeObject(outputObject);
        this.outputWriter.flush();
        this.lock.unlock();
    }

    private void closeConnection() throws IOException {
        System.out.printf("%s Connection closed for port %d.\n", new Date(), this.clientSocket.getPort());

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
