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

    @Override
    public String toString() {
        return "Client{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;

        Client client = (Client) o;

        if (getPort() != client.getPort()) return false;
        return getAddress().equals(client.getAddress());
    }

    @Override
    public int hashCode() {
        int result = getAddress().hashCode();
        result = 31 * result + getPort();
        return result;
    }
}
