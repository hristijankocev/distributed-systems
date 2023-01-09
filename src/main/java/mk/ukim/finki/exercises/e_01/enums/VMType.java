package mk.ukim.finki.exercises.e_01.enums;

public enum VMType {
    STORAGE("storage"), COMPUTE("compute");

    private final String type;

    VMType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
