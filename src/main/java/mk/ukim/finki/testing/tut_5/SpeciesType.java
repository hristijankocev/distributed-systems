package mk.ukim.finki.testing.tut_5;

public enum SpeciesType {
    ELEPHANT("elephant"), FOX("fox"), RABBIT("rabbit"), NULL(null);
    private final String type;

    SpeciesType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
