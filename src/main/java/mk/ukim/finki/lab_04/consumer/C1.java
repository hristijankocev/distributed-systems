package mk.ukim.finki.lab_04.consumer;

import mk.ukim.finki.lab_04.enums.RouteTypes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Date;
import java.util.regex.Pattern;

public class C1 extends Consumer {

    public C1(String topic, String brokers, String group) {
        super(topic, brokers, group);
    }

    @Override
    public void consume() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.kafkaSetup.getProps());

        Pattern pattern = Pattern.compile(this.topic);

        consumer.subscribe(pattern);

        long pollTime = 1000;

        System.out.println(new Date() + " [*] Waiting for messages. To exit press CTRL+C");

        //noinspection InfiniteLoopStatement
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTime));

            for (ConsumerRecord<String, String> record : records) {
                System.out.println(constructMessage(record));
            }
        }
    }

    private static String constructMessage(ConsumerRecord<String, String> record) {
        // Record value example: Tue Jan 03 14:38:00 CET 2023
        // Record topic example: '192029.Joseph.student.19.office'

        String[] parts = record.topic().split("\\.");
        return new Date() + " [*] Student '" + parts[1] + "' entered office number " + parts[3] + " at " + record.value();
    }

    public static void main(String[] args) {
        String brokers = "kafkaserver.devops.mk:9092";
        String group = "192029-01";

        new C1(RouteTypes.R1.getType(), brokers, group).run();
    }
}

