package mk.ukim.finki.testing.tut_5;

public enum SpeedType {
    NULL(null), QUICK("quick"), LAZY("lazy");
    private final String type;

    SpeedType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
