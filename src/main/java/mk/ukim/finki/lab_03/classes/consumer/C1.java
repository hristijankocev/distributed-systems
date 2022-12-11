package mk.ukim.finki.lab_03.classes.consumer;

import com.rabbitmq.client.*;
import mk.ukim.finki.lab_03.classes.exchange.TopicExchange;
import mk.ukim.finki.lab_03.enums.RouteTypes;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class C1 {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(TopicExchange.EXCHANGE_HOST);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(TopicExchange.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, TopicExchange.EXCHANGE_NAME, RouteTypes.R1.getType());

        System.out.println(new Date() + " [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            System.out.println(new Date() + " [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "'Message: '" + message + "'");
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
    }
}
