package mk.ukim.finki.testing.tut_5;

public enum RouteTypes {
    R1("*.orange.*"), R2("*.*.rabbit"), R3("lazy.#");
    private final String type;

    RouteTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
