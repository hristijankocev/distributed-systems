package mk.ukim.finki.lab_01.server;

import mk.ukim.finki.lab_01.ccmp.CCMP;
import mk.ukim.finki.lab_01.ccmp.CCMPPacket;
import mk.ukim.finki.lab_01.ccmp.exceptions.InvalidCCMPPacketException;
import mk.ukim.finki.lab_01.ccmp.exceptions.MessageIntegrityViolationException;
import mk.ukim.finki.lab_01.config.CCMPConfig;
import mk.ukim.finki.lab_01.config.ProtoConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerWorker implements Runnable {
    private final Hashtable<String, List<String>> users;
    private final DatagramSocket socket;
    private final DatagramPacket packet;

    public ServerWorker(Hashtable<String, List<String>> users, DatagramSocket socket, DatagramPacket packet) {
        this.users = users;
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        handlePacket();
    }

    private synchronized void handlePacket() {
        String clientMessage;

        if (CCMPConfig.DATA.isPROTOCOL_ENABLED()) {
            byte[] message = new byte[this.packet.getLength()];
            System.arraycopy(this.packet.getData(), 0, message, 0, this.packet.getLength());

            try {
                byte[] decryptedDataHex = CCMP.getDecryptedPayload(message);
                clientMessage = new String(decryptedDataHex, StandardCharsets.UTF_8);
            } catch (InvalidCCMPPacketException | MessageIntegrityViolationException e) {
                e.printStackTrace();
                return;
            }

        } else {
            clientMessage = new String(this.packet.getData(), this.packet.getOffset(), this.packet.getLength());
        }

        // Print out the received message from the client
        System.out.print(new Date() + " " + this.packet.getAddress() + " on port " + this.packet.getPort() + " said: ");
        System.out.println(clientMessage);

        // Handle the message
        handleMessage(clientMessage);
    }

    private void handleMessage(@NotNull String clientMessage) {
        if (clientMessage.equals(ProtoConfig.DATA.getTEST_CONNECTION_MSG())) {
            // Hello message
            sendMessage("Welcome to the chat! " + ProtoConfig.DATA.getSERVER_HELP_MSG(),
                    this.packet.getAddress(), this.packet.getPort());
        } else if (clientMessage.equals("help")) {
            // Help message
            sendMessage(ProtoConfig.DATA.getSERVER_COMMANDS(),
                    this.packet.getAddress(), this.packet.getPort());
        } else if (clientMessage.startsWith("login")) {
            // Validate the syntax for the login command
            String user = getUserFromLoginCommand(clientMessage);
            if (user == null) {
                sendMessage("Invalid login syntax. Expected 'login:\"username\"'",
                        this.packet.getAddress(), this.packet.getPort());
            } else {
                // Check if that username is already taken or if the user is already logged in
                if (isLoggedIn()) {
                    Map.Entry<String, List<String>> existingUser = getUserBasedOnPacket();
                    if (existingUser != null) {
                        sendMessage("You are already logged in as \"" + existingUser.getKey() + "\"!",
                                this.packet.getAddress(), this.packet.getPort());
                    } else {
                        sendMessage("Username already taken!",
                                this.packet.getAddress(), this.packet.getPort());
                    }
                } else {
                    if (this.users.keySet().stream().anyMatch(u -> u.equals(user))) {
                        sendMessage("Username already taken!",
                                this.packet.getAddress(), this.packet.getPort());
                    } else {
                        // Store the user in the hashtable
                        this.users.put(user, List.of(this.packet.getAddress().getHostAddress(), String.valueOf(this.packet.getPort())));
                        sendMessage("Welcome back " + user + "! You are successfully logged in.",
                                this.packet.getAddress(), this.packet.getPort());
                    }
                }
            }
        } else if (clientMessage.equals("get-users")) {
            // In order to get the users, we first need to check if the user requesting the info is logged in
            if (isLoggedIn()) {
                String loggedInUsers = String.join(", ", this.users.keySet());
                sendMessage("Logged-in users: \n" + loggedInUsers,
                        this.packet.getAddress(), this.packet.getPort());
            } else {
                sendMessage("You need to be logged in order to do that!",
                        this.packet.getAddress(), this.packet.getPort());
            }
        } else if (clientMessage.startsWith("message")) {
            if (isLoggedIn()) {
                sendMessageFromClientToClient(clientMessage);
            } else {
                sendMessage("You need to be logged in order to do that!",
                        this.packet.getAddress(), this.packet.getPort());
            }
        } else if (clientMessage.equals(ProtoConfig.DATA.getLOGOUT_MSG())) {
            if (isLoggedIn()) {
                String user = getUserBasedOnPacket().getKey();
                if (user != null) {
                    this.users.remove(user);
                    sendMessage("Logged out successfully.",
                            this.packet.getAddress(), this.packet.getPort());
                }
            } else {
                sendMessage("Can't logout when you haven't even been logged in.",
                        this.packet.getAddress(), this.packet.getPort());
            }
        } else {
            // The server doesn't understand the message
            sendMessage("Unknown command. " + ProtoConfig.DATA.getSERVER_COMMANDS(),
                    this.packet.getAddress(), this.packet.getPort());
        }

    }

    private void sendMessageFromClientToClient(String packetContent) {
        String[] command = packetContent.split(":");

        // Validate the command
        if (command.length == 3 && command[0].equals("message")) {
            String toClient = command[1];
            // Check if the client we need to send the message is logged in
            if (this.users.containsKey(toClient)) {
                List<String> userToSendTo = this.users.get(toClient);
                try {
                    Map.Entry<String, List<String>> user = getUserBasedOnPacket();
                    String transitiveMessage = "You got a message from '" + user.getKey() + "' saying: " + command[2];

                    InetAddress outgoingAddress = InetAddress.getByName(userToSendTo.get(0));
                    int outgoingPort = Integer.parseInt(userToSendTo.get(1));

                    sendMessage(transitiveMessage, outgoingAddress, outgoingPort);

                    // Tell the original client that the message was sent successfully
                    sendMessage("Your message was sent successfully!",
                            this.packet.getAddress(), this.packet.getPort());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    sendMessage("Error occurred while trying to send a message to " + toClient,
                            this.packet.getAddress(), this.packet.getPort());
                }
            } else {
                sendMessage("Client " + toClient + " does not exist in the logged in list.",
                        this.packet.getAddress(), this.packet.getPort());
            }
        } else {
            sendMessage("Invalid message sending syntax. Expected 'message:toUser:messageContent'",
                    this.packet.getAddress(), this.packet.getPort());
        }
    }

    private Map.Entry<String, List<String>> getUserBasedOnPacket() {
        return this.users.entrySet().stream()
                .filter(e -> e.getValue().get(0).equals(this.packet.getAddress().getHostAddress())
                        && e.getValue().get(1).equals(String.valueOf(this.packet.getPort())))
                .findFirst()
                .orElse(null);
    }

    private boolean isLoggedIn() {
        String clientAddress = this.packet.getAddress().getHostAddress();
        String clientPort = String.valueOf(this.packet.getPort());

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

    private void sendMessage(@NotNull String message, InetAddress address, int port) {
        try {
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            // Create a CCMP packet if protocol is enabled
            if (CCMPConfig.DATA.isPROTOCOL_ENABLED()) {
                CCMPPacket packet = new CCMPPacket(messageBytes, new byte[6]);
                messageBytes = packet.bytes();
            }

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, address, port);

            this.socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}