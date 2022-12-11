package mk.ukim.finki.testing.tut_4;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class EmitLog {
    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                String severity = SeverityType.values()[random.nextInt(SeverityType.values().length)].getSeverity();
                String message = severity + ": Hello World!";

                channel.basicPublish(EXCHANGE_NAME, severity,
                        null, message.getBytes(StandardCharsets.UTF_8));

                System.out.println(" [x] Sent '" + message + "'");

                Thread.sleep(500);
            }

        }
    }
}
