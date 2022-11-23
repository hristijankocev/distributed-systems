package mk.ukim.finki.lab_02.server;

import mk.ukim.finki.lab_02.classes.Client;
import mk.ukim.finki.lab_02.classes.Job;
import mk.ukim.finki.lab_02.classes.Message;
import mk.ukim.finki.lab_02.classes.TransientMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * What is my purpose ?
 * - You pass butter.
 * Oh my god...
 */
public class ServerWorkerHelper implements Runnable {
    // Tells the thread for which thread its working for
    private final int workingForThread;
    private final Lock lock;
    private final ObjectOutputStream objectOutputStream;
    private final Hashtable<String, Client> users;

    public ServerWorkerHelper(int workingForThread, Lock lock, ObjectOutputStream objectOutputStream, Hashtable<String, Client> users) {
        this.workingForThread = workingForThread;
        this.lock = lock;
        this.objectOutputStream = objectOutputStream;
        this.users = users;
    }

    @Override
    public void run() {
        // When you get your chance, finish your job (if there is one for you)
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Server.semaphore.acquire();

                // Get the thread we are working for
                Map.Entry<Client, Thread> bossEntry = Server.workerThreads.entrySet().stream()
                        .filter(e -> Integer.parseInt(e.getValue().getName()) == (workingForThread))
                        .findFirst().orElse(null);

                if (bossEntry != null) {
                    // Do your job.
                    synchronized (Server.threadJobs) {
                        Map.Entry<Thread, Job> itsMe = Server.threadJobs.entrySet().stream()
                                .filter((e) -> e.getKey().getName().equals(bossEntry.getValue().getName()))
                                .findFirst()
                                .orElse(null);
                        // Check if there is job for me
                        if (itsMe != null && itsMe.getValue().hasJob()) {
                            TransientMessage job = itsMe.getValue().getJob();

                            // Get the username from the client that send the message
                            Map.Entry<String, Client> fromUser = this.users.entrySet().stream()
                                    .filter(e -> e.getValue().equals(job.getFrom()))
                                    .findFirst()
                                    .orElse(null);


                            String sentFromUsername;
                            if (fromUser != null) {
                                sentFromUsername = fromUser.getKey();
                            } else {
                                sentFromUsername = ":: An unknown person ::";
                            }
                            Message messageSer = new Message(sentFromUsername + " sent you a message: " + job.getMessageContent(), job.getFrom());

                            this.lock.lock();
                            this.objectOutputStream.writeObject(messageSer);
                            this.lock.unlock();

                            itsMe.getValue().setHasJob(false);
                        }
                    }

                } else {
                    // Be bored
                    System.out.println(new Date() + " Worker helper for thread on port" + this.workingForThread + " got nothing to do :(");
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
