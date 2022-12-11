package mk.ukim.finki.testing.tut_5;

public enum ColorType {
    NULL(null), ORANGE("orange"), BROWN("brown"), PINK("pink");
    private final String type;

    ColorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
