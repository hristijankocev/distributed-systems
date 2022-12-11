package mk.ukim.finki.lab_03.enums;

/**
 * Routes have the form of: "personName"."userType"."roomType"."roomId"
 */
public enum RouteTypes {

    R1("*." + PersonType.STUDENT.getPerson() + ".*." + RoomType.OFFICE.getRoom()),
    R2("*." + PersonType.PROFESSOR.getPerson() + ".*.*"),
    R3("*." + PersonType.STUDENT.getPerson() + ".*.*");
    private final String type;

    RouteTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
