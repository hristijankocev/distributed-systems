package mk.ukim.finki.lab_02.classes;

public class Job {
    private boolean hasJob;
    private final TransientMessage job;

    public void setHasJob(boolean hasJob) {
        this.hasJob = hasJob;
    }

    public Job(boolean hasJob, TransientMessage job) {
        this.hasJob = hasJob;
        this.job = job;
    }

    public boolean hasJob() {
        return hasJob;
    }

    public TransientMessage getJob() {
        return job;
    }
}
