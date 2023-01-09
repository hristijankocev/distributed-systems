package mk.ukim.finki.exercises.e_01.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import mk.ukim.finki.exercises.e_01.Exchange;
import mk.ukim.finki.exercises.e_01.enums.CoresAmount;
import mk.ukim.finki.exercises.e_01.enums.RAMAmount;
import mk.ukim.finki.exercises.e_01.enums.VMType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Producer {
    public static void main(String[] args) {
        final String exchangeName = Exchange.EXCHANGE_NAME;
        final String host = Exchange.EXCHANGE_HOST;

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);

        try {
            System.out.println(new Date() + " [*] Connecting to RabbitMQ server...");
            Connection connection = connectionFactory.newConnection();
            System.out.println(new Date() + " [*] Connected to server.");
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(exchangeName, Exchange.EXCHANGE_TYPE);

            Random random = new Random();

            for (int i = 0; i < 10; i++) {
                String randomRoute = getRandomRoute(random);
                String message = new Date().toString();

                channel.basicPublish(exchangeName, randomRoute, null, message.getBytes(StandardCharsets.UTF_8));

                System.out.println(message + " [*] Requested VM of type '" + randomRoute.split("\\.")[0] + "' with routing key " + randomRoute);
            }
            System.out.printf("%s [*] Finished producing requests.", new Date());
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static String getRandomRoute(Random random) {
        /*
        Routes consist of the following info:
        - type: storage/compute
        - RAM: in megabytes
        - number of cores
        - execution time
        Example route: storage.2048.4.180
         */
        String vmType = VMType.values()[random.nextInt(VMType.values().length)].getType();
        String ramAmount = String.valueOf(RAMAmount.values()[random.nextInt(RAMAmount.values().length)].getAmount());
        String numCores = String.valueOf(CoresAmount.values()[random.nextInt(CoresAmount.values().length)].getAmount());
        String execTime = String.valueOf(2 + random.nextInt(10 - 2 + 1));

        return vmType + "." + ramAmount + "." + numCores + "." + execTime;
    }
}
