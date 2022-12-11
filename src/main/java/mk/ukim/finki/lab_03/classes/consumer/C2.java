package mk.ukim.finki.lab_03.classes.consumer;

import com.rabbitmq.client.*;
import mk.ukim.finki.lab_03.classes.exchange.TopicExchange;
import mk.ukim.finki.lab_03.enums.RouteTypes;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * This consumer will write to a file the information of which professor entered which room(roomId, roomType) at a specific time.
 */
public class C2 {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(TopicExchange.EXCHANGE_HOST);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(TopicExchange.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, TopicExchange.EXCHANGE_NAME, RouteTypes.R2.getType());

        System.out.println(new Date() + " [*] Waiting for messages. To exit press CTRL+C");

        // Received messages will be written in a csv file
        PrintStream file = new PrintStream(new FileOutputStream("professors-unlocks.csv", true));

        // Store current System.out before assigning a new value
        PrintStream console = System.out;

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // CSV file rows form: 'professorName', 'roomType', 'date'

            // Assign file to output stream using setOut() method
            System.setOut(file);

            // Write message only
            // Routes have the form of: <personName>.<userType>.<roomID>.<roomType>
            String routeKey = delivery.getEnvelope().getRoutingKey();

            String csvRow = generateRowForCSV(routeKey);
            System.out.println(csvRow);

            // Use stored value for output stream
            System.setOut(console);

            // Display message only
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            System.out.println(new Date() + " [x] Received '" +
                    routeKey + "'Message: '" + message + "'");
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }

    private static String generateRowForCSV(String routeKey) {
        String[] separated = routeKey.split("\\.");

        String name = separated[0];
        String roomId = separated[2];
        String roomType = separated[3];

        // Example: John, office, Sun Dec 11 21:20:00 CET 2022
        return name + "," + roomType + "," + roomId + "," + new Date();
    }
}
