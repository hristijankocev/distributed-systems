package mk.ukim.finki.lab_03.classes.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import mk.ukim.finki.lab_03.classes.exchange.TopicExchange;
import mk.ukim.finki.lab_03.enums.PersonNames;
import mk.ukim.finki.lab_03.enums.PersonType;
import mk.ukim.finki.lab_03.enums.RoomType;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

public class ActionEmitter {

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(TopicExchange.EXCHANGE_HOST);

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(TopicExchange.EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            Random random = new Random();

            for (int i = 0; i < 10; i++) {
                String routeKey = getRandomRouting(random);

                String message = new Date().toString();

                channel.basicPublish(TopicExchange.EXCHANGE_NAME, routeKey, null, message.getBytes(StandardCharsets.UTF_8));

                System.out.println(new Date() + " [x] Sent '" + message + "', with routing key '" + routeKey + "'");
            }
        }
    }

    public static String getRandomRouting(Random random) {
        // Routes have the form of: <personName>.<userType>.<roomID>.<roomType>
        String personName = PersonNames.values()[random.nextInt(PersonNames.values().length)].getName();

        String userType = PersonType.values()[random.nextInt(PersonType.values().length)].getPerson();

        String roomType = RoomType.values()[random.nextInt(RoomType.values().length)].getRoom();

        return personName + "." + userType + "." + random.nextInt(20) + "." + roomType;
    }
}
