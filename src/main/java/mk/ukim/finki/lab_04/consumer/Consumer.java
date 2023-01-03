package mk.ukim.finki.lab_04.consumer;


import mk.ukim.finki.lab_04.kafka.KafkaSetup;

public class Consumer implements Runnable {
    protected KafkaSetup kafkaSetup;
    protected String topic;

    public Consumer(String topic, String brokers, String group) {
        this.kafkaSetup = new KafkaSetup(brokers, group);
        this.topic = topic;
    }

    public void consume() {

    }

    @Override
    public void run() {
        consume();
    }
}
