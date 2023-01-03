package mk.ukim.finki.lab_04.enums;

/**
 * Routes have the form of: "personName"."userType"."roomType"."roomId"
 */
public enum RouteTypes {

    R1("192029.*." + PersonType.STUDENT.getPerson() + ".*." + RoomType.OFFICE.getRoom()),
    R2("192029.*." + PersonType.PROFESSOR.getPerson() + ".*.*"),
    R3("192029.*." + PersonType.STUDENT.getPerson() + ".*.*");
    private final String type;

    RouteTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}