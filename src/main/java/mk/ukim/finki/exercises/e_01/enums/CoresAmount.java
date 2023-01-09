package mk.ukim.finki.exercises.e_01.enums;

public enum CoresAmount {
    CORES_AMOUNT_1(2), CORES_AMOUNT_2(4), CORES_AMOUNT_3(8), CORES_AMOUNT_4(16), CORES_AMOUNT_5(32);
    private final int amount;

    CoresAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
