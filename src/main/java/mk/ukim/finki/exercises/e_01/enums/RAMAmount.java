package mk.ukim.finki.exercises.e_01.enums;

public enum RAMAmount {
    RAM_AMOUNT_1(1024), RAM_AMOUNT_2(2048), RAM_AMOUNT_3(4096), RAM_AMOUNT_4(8192), RAM_AMOUNT_5(16384);
    private final int amount;

    RAMAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
