package mk.ukim.finki.exercises.e_01;

import com.rabbitmq.client.BuiltinExchangeType;

public class Exchange {
    public static String EXCHANGE_NAME = "example-name";
    // Public exchange host IP from FCSE: 51.83.68.66
    public static String EXCHANGE_HOST = "localhost";
    public static String EXCHANGE_TYPE = BuiltinExchangeType.TOPIC.getType();
}
