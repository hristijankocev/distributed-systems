package mk.ukim.finki.lab_04.enums;

public enum PersonType {
    PROFESSOR("professor"), STUDENT("student");

    private final String person;

    PersonType(String person) {
        this.person = person;
    }

    public String getPerson() {
        return person;
    }
}
