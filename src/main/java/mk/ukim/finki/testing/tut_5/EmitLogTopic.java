package mk.ukim.finki.testing.tut_5;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class EmitLogTopic {
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            Random random = new Random();

            for (int i = 0; i < 10; i++) {
                String routingKey = getRandomRouting(random);
                String message = "Message: " + routingKey;

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            }

        }
    }

    // Routing key consists of: <speed>.<colour>.<species>
    private static String getRandomRouting(Random random) {
        String routing = "";

        String speed = SpeedType.values()[random.nextInt(SpeedType.values().length)].getType();
        if (speed != null) routing += speed;

        String color = ColorType.values()[random.nextInt(ColorType.values().length)].getType();
        if (color != null) routing += "." + color;

        String species = SpeciesType.values()[random.nextInt(SpeciesType.values().length)].getType();
        if (species != null) routing += "." + species;

        // Remove trailing and leading dots
        return routing.replaceAll("^\\.", "").replaceAll("\\.$", "");
    }
}
