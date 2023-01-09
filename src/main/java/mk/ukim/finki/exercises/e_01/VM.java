package mk.ukim.finki.exercises.e_01;

import mk.ukim.finki.exercises.e_01.enums.VMType;

import java.util.Date;

public class VM implements Runnable {
    private final String id;
    private final VMType vmType;
    private final int amountRam;
    private final int numCores;
    private final int executionTime;

    public VM(String id, VMType vmType, int amountRam, int numCores, int executionTime) {
        this.id = id;
        this.vmType = vmType;
        this.amountRam = amountRam;
        this.numCores = numCores;
        this.executionTime = executionTime;
    }

    @Override
    public void run() {
        System.out.printf("%s [*] Starting %s VM with ID: %s, RAM: %d, Cores: %d\n",
                new Date(), this.vmType.getType(), this.id, this.amountRam, this.numCores);

        try {
            Thread.sleep(this.executionTime * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.printf("%s [*] Finished execution VM with ID %s\n", new Date(), this.id);
    }

}
