package mk.ukim.finki.config;

public enum ProtoConfig {
    DATA;
    private final int SERVER_PORT = 4444;
    private final int UDP_PACKET_SIZE = 4096;
    private final String TEST_CONNECTION_MSG = "hello";
    private final String LOGOUT_MSG = "logout";
    private final String SERVER_HELP_MSG = "To get a list of possible commands send \"help\"";
    private final String SERVER_COMMANDS = "Possible commands: hello, help, login:username, get-users, message:toUser:messageContent, logout, end(to close the client)";


    public int getSERVER_PORT() {
        return SERVER_PORT;
    }

    public int getUDP_PACKET_SIZE() {
        return UDP_PACKET_SIZE;
    }

    public String getTEST_CONNECTION_MSG() {
        return TEST_CONNECTION_MSG;
    }

    public String getSERVER_HELP_MSG() {
        return SERVER_HELP_MSG;
    }

    public String getSERVER_COMMANDS() {
        return SERVER_COMMANDS;
    }

    public String getLOGOUT_MSG() {
        return LOGOUT_MSG;
    }
}
