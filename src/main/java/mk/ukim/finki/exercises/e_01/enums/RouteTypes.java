package mk.ukim.finki.exercises.e_01.enums;

public enum RouteTypes {
    R1(VMType.COMPUTE.getType() + ".*.*.*"), R2(VMType.STORAGE.getType() + ".*.*.*");
    private final String route;

    RouteTypes(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
