package mk.ukim.finki.lab_04.enums;

public enum PersonNames {
    N1("John"), N2("Jane"), N3("Joseph"), N4("Walter"), N5("Peter"), N6("Astrid"), N7("Tony"), N8("AJ"), N9("Meadow");
    private final String name;

    PersonNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
