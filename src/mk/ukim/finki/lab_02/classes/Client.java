package mk.ukim.finki.lab_02.classes;

import java.io.Serializable;
import java.net.InetAddress;

public class Client implements Serializable {
    private InetAddress address;
    private int port;

    public Client(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public Client() {
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
