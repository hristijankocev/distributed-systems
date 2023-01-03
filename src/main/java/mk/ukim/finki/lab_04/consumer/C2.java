package mk.ukim.finki.lab_04.consumer;


import mk.ukim.finki.lab_04.enums.RouteTypes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * This consumer will write to a file the information of which professor entered which room(roomId, roomType) at a specific time.
 */
public class C2 extends Consumer {

    public C2(String topic, String brokers, String group) {
        super(topic, brokers, group);
    }


    @Override
    public void consume() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(this.kafkaSetup.getProps());

        Pattern pattern = Pattern.compile(this.topic);

        consumer.subscribe(pattern);

        long pollTime = 1000;

        System.out.println(new Date() + " [*] Waiting for messages. To exit press CTRL+C");

        try {
            // Received messages will be written in a csv file
            PrintStream file = new PrintStream(new FileOutputStream("professors-unlocks.csv", true));

            // Store current System.out before assigning a new value
            PrintStream console = System.out;

            // CSV file rows form: 'professorName', 'roomType', 'date'

            //noinspection InfiniteLoopStatement
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(pollTime));

                for (ConsumerRecord<String, String> record : records) {
                    // Assign file to output stream using setOut() method
                    System.setOut(file);

                    // Write message only
                    // Routes have the form of: <personName>.<userType>.<roomID>.<roomType>

                    String csvRow = generateRowForCSV(record);
                    System.out.println(csvRow);

                    // Assign the console for the output stream
                    System.setOut(console);

                    // Display message only
                    System.out.println(constructMessage(record));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static String constructMessage(ConsumerRecord<String, String> record) {

        String[] parts = record.topic().split("\\.");

        return new Date() + " [*] " + parts[2].substring(0, 1).toUpperCase() + parts[2].substring(1).toLowerCase() + " '" + parts[1] + "' entered " + parts[4] + " number " + parts[3] + " at " + record.value();
    }


    private static String generateRowForCSV(ConsumerRecord<String, String> record) {
        String[] separated = record.topic().split("\\.");

        String name = separated[1];
        String roomId = separated[3];
        String roomType = separated[4];

        // Example: John, office, Sun Dec 11 21:20:00 CET 2022
        return name + "," + roomType + "," + roomId + "," + record.value();
    }


    public static void main(String[] args) {
        String brokers = "kafkaserver.devops.mk:9092";
        String group = "192029-02";

        new C2(RouteTypes.R2.getType(), brokers, group).run();
    }
}