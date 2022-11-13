package mk.ukim.finki.server;

import mk.ukim.finki.config.ProtoConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ServerWorker extends Thread {
    private final int port;
    private final Hashtable<String, List<String>> users;

    public ServerWorker(int port) {
        this.port = port;
        this.users = new Hashtable<>();
    }

    @Override
    public void run() {
        try {
            DatagramSocket ds = new DatagramSocket(this.port);

            System.out.println("Server started, listening on port " + this.port);
            //noinspection InfiniteLoopStatement
            while (true) {
                listen(ds);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void listen(@NotNull DatagramSocket socket) throws IOException {
        // Max UDP buffer size
        var buffer = new byte[ProtoConfig.DATA.getUDP_PACKET_SIZE()];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        // Thread is blocked until data is received
        socket.receive(packet);
        byte[] data = packet.getData();

        String clientMessage = new String(data, packet.getOffset(), packet.getLength());

        // Print out the received message from the client
        Date date = new Date();

        System.out.print(date + " " + packet.getAddress() + " on port " + packet.getPort() + " said: ");
        System.out.println(clientMessage);

        // Handle the message
        handleMessage(clientMessage, socket, packet);
    }

    private void handleMessage(@NotNull String clientMessage, @NotNull DatagramSocket socket, @NotNull DatagramPacket packet) {
        if (clientMessage.equals(ProtoConfig.DATA.getTEST_CONNECTION_MSG())) {
            // Hello message
            sendMessage(packet, socket, "Welcome to the chat! " + ProtoConfig.DATA.getSERVER_HELP_MSG());
        } else if (clientMessage.equals("help")) {
            // Help message
            sendMessage(packet, socket, ProtoConfig.DATA.getSERVER_COMMANDS());
        } else if (clientMessage.startsWith("login")) {
            // Validate the syntax for the login command
            String user = getUserFromLoginCommand(clientMessage);
            if (user == null) {
                sendMessage(packet, socket, "Invalid login syntax. Expected 'login:\"username\"'");
            } else {
                // Check if that username is already taken or if the user is already logged in
                if (isLoggedIn(packet)) {
                    Map.Entry<String, List<String>> existingUser = getUserBasedOnPacket(packet);
                    if (existingUser != null) {
                        sendMessage(packet, socket, "You are already logged in as \"" + existingUser.getKey() + "\"!");
                    } else {
                        sendMessage(packet, socket, "Username already taken!");
                    }
                } else {
                    if (this.users.keySet().stream().anyMatch(u -> u.equals(user))) {
                        sendMessage(packet, socket, "Username already taken!");
                    } else {
                        // Store the user in the hashtable
                        this.users.put(user, List.of(packet.getAddress().getHostAddress(), String.valueOf(packet.getPort())));
                        sendMessage(packet, socket, "Welcome back " + user + "! You are successfully logged in.");
                    }
                }
            }
        } else if (clientMessage.equals("get-users")) {
            // In order to get the users, we first need to check if the user requesting the info is logged in
            if (isLoggedIn(packet)) {
                String loggedInUsers = String.join(", ", this.users.keySet());
                sendMessage(packet, socket, "Logged-in users: \n" + loggedInUsers);
            } else {
                sendMessage(packet, socket, "You need to be logged in order to do that!");
            }
        } else if (clientMessage.startsWith("message")) {
            if (isLoggedIn(packet)) {
                sendMessageFromClientToClient(clientMessage, packet, socket);
            } else {
                sendMessage(packet, socket, "You need to be logged in order to do that!");
            }
        } else if (clientMessage.equals(ProtoConfig.DATA.getLOGOUT_MSG())) {
            if (isLoggedIn(packet)) {
                String user = getUserBasedOnPacket(packet).getKey();
                if (user != null) {
                    this.users.remove(user);
                    sendMessage(packet, socket, "Logged out successfully.");
                }
            } else {
                sendMessage(packet, socket, "Can't logout when you haven't even been logged in.");
            }
        } else {
            // The server doesn't understand the message
            sendMessage(packet, socket, "Unknown command. " + ProtoConfig.DATA.getSERVER_COMMANDS());
        }

    }

    private void sendMessageFromClientToClient(String packetContent, DatagramPacket packet, DatagramSocket socket) {
        String[] command = packetContent.split(":");

        // Validate the command
        if (command.length == 3 && command[0].equals("message")) {
            String toClient = command[1];
            // Check if the client we need to send the message is logged in
            if (this.users.containsKey(toClient)) {
                List<String> userToSendTo = this.users.get(toClient);
                try {
                    Map.Entry<String, List<String>> user = getUserBasedOnPacket(packet);
                    String transitiveMessage = "You got a message from '" + user.getKey() + "' saying: " + command[2];
                    byte[] transitiveMessageBytes = transitiveMessage.getBytes();

                    InetAddress outgoingAddress = InetAddress.getByName(userToSendTo.get(0));
                    int outgoingPort = Integer.parseInt(userToSendTo.get(1));

                    DatagramPacket dp = new DatagramPacket(transitiveMessageBytes, transitiveMessageBytes.length, outgoingAddress, outgoingPort);

                    socket.send(dp);

                    // Tell the original client that the message was sent successfully
                    String message = "Your message was sent successfully!";
                    sendMessage(packet, socket, message);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    sendMessage(packet, socket, "Error occurred while trying to send a message to " + toClient);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sendMessage(packet, socket, "Client " + toClient + " does not exist in the logged in list.");
            }
        } else {
            sendMessage(packet, socket, "Invalid message sending syntax. Expected 'message:toUser:messageContent'");
        }
    }

    private Map.Entry<String, List<String>> getUserBasedOnPacket(DatagramPacket packet) {
        return this.users.entrySet().stream()
                .filter(e -> e.getValue().get(0).equals(packet.getAddress().getHostAddress())
                        && e.getValue().get(1).equals(String.valueOf(packet.getPort())))
                .findFirst()
                .orElse(null);
    }

    private boolean isLoggedIn(@NotNull DatagramPacket packet) {
        String clientAddress = packet.getAddress().getHostAddress();
        String clientPort = String.valueOf(packet.getPort());

        return this.users.values()
                .stream()
                .anyMatch(list -> list.get(0).equals(clientAddress) && list.get(1).equals(clientPort));
    }

    private @Nullable String getUserFromLoginCommand(@NotNull String clientMessage) {
        String[] command = clientMessage.split(":");

        // Validate the command
        if (command.length == 2 && command[0].equals("login")) {
            return command[1];
        }
        return null;
    }

    private void sendMessage(@NotNull DatagramPacket packet, @NotNull DatagramSocket socket, @NotNull String message) {
        try {

            byte[] messageBytes = message.getBytes();

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, packet.getAddress(), packet.getPort());

            socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = ProtoConfig.DATA.getSERVER_PORT();

        ServerWorker serverWorker = new ServerWorker(port);
        serverWorker.start();
    }
}

