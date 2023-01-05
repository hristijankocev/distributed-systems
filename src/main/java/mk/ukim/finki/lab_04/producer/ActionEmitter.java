package mk.ukim.finki.lab_04.producer;

import mk.ukim.finki.lab_04.enums.PersonNames;
import mk.ukim.finki.lab_04.enums.PersonType;
import mk.ukim.finki.lab_04.enums.RoomType;
import mk.ukim.finki.lab_04.kafka.KafkaSetup;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Date;
import java.util.Random;

public class ActionEmitter implements Runnable {
    private final KafkaSetup kafkaSetup;

    public ActionEmitter(String brokers) {
        this.kafkaSetup = new KafkaSetup(brokers, null);
    }

    public void produce() {
        Producer<String, String> producer = new KafkaProducer<>(this.kafkaSetup.getProps());

        System.out.println("Starting emitter...");

        Random random = new Random();

        int i = 0;
        while (i < 10) {
            String randomTopic = getRandomTopic(random);
            String date = new Date().toString();

            producer.send(new ProducerRecord<>(randomTopic, date));

            System.out.println(date + " [x] Sent to topic: " + randomTopic);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }

        System.out.println("Emitter finished.");
    }

    public static String getRandomTopic(Random random) {
        // Routes have the form of: <personName>.<userType>.<roomID>.<roomType>
        String personName = PersonNames.values()[random.nextInt(PersonNames.values().length)].getName();

        String userType = PersonType.values()[random.nextInt(PersonType.values().length)].getPerson();

        String roomType = RoomType.values()[random.nextInt(RoomType.values().length)].getRoom();

        return "192029" + "." + personName + "." + userType + "." + random.nextInt(20) + "." + roomType;
    }

    @Override
    public void run() {
        produce();
    }

    public static void main(String[] args) {
        String brokers = "kafkaserver.devops.mk:9092";

        new ActionEmitter(brokers).run();
    }
}
