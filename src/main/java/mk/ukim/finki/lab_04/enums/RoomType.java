package mk.ukim.finki.lab_04.enums;


public enum RoomType {
    CLASSROOM("classroom"), LABORATORY("laboratory"), OFFICE("office");

    private final String room;

    RoomType(String room) {
        this.room = room;
    }

    public String getRoom() {
        return room;
    }
}
