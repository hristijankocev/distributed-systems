package mk.ukim.finki.exercises.e_01.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import mk.ukim.finki.exercises.e_01.Exchange;
import mk.ukim.finki.exercises.e_01.VM;
import mk.ukim.finki.exercises.e_01.enums.RouteTypes;
import mk.ukim.finki.exercises.e_01.enums.VMType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Consumer implements Runnable {
    private final String exchangeName;
    private final String exchangeType;
    private final String host;
    private final int id;
    private final String route;

    public Consumer(String exchangeName, String exchangeType, String host, int id, String route) {
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        this.host = host;
        this.id = id;
        this.route = route;
    }

    @Override
    public void run() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);

        try {
            System.out.printf("%s [*] Consumer %d Connecting to RabbitMQ server...\n", new Date(), this.id);
            Connection connection = connectionFactory.newConnection();
            System.out.printf("%s [*] Consumer %d connected to server.\n", new Date(), this.id);

            Channel channel = connection.createChannel();

            String queueName = channel.queueDeclare().getQueue();

            channel.exchangeDeclare(this.exchangeName, this.exchangeType);

            channel.queueBind(queueName, this.exchangeName, this.route);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.printf("%s [*] Consumer %d received VM request '%s' on %s\n",
                        new Date(), this.id, delivery.getEnvelope().getRoutingKey(), message);

                String[] parts = delivery.getEnvelope().getRoutingKey().split("\\.");
                String vmType = parts[0];
                int amountRAM = Integer.parseInt(parts[1]);
                int numCores = Integer.parseInt(parts[2]);
                int executionTime = Integer.parseInt(parts[3]);

                if (vmType.equals(VMType.COMPUTE.getType())) {
                    UUID uuid = UUID.randomUUID();
                    new Thread(new VM(uuid.toString(), VMType.COMPUTE, amountRAM, numCores, executionTime)).start();
                } else if (vmType.equals(VMType.STORAGE.getType())) {
                    UUID uuid = UUID.randomUUID();
                    new Thread(new VM(uuid.toString(), VMType.STORAGE, amountRAM, numCores, executionTime)).start();
                } else {
                    System.out.printf("%s [*] Consumer %d Unknown VM type '%s'\n", new Date(), this.id, vmType);
                }
            };

            System.out.printf("%s [*] Consumer %d waiting for messages...\n", new Date(), this.id);

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final String exchangeName = Exchange.EXCHANGE_NAME;
        final String exchangeType = Exchange.EXCHANGE_TYPE;
        final String host = Exchange.EXCHANGE_HOST;

        new Thread(new Consumer(exchangeName, exchangeType, host, 1, RouteTypes.R1.getRoute())).start();
        new Thread(new Consumer(exchangeName, exchangeType, host, 2, RouteTypes.R2.getRoute())).start();
    }
}
