package mk.ukim.finki.testing.tut_4;

public enum SeverityType {
    INFO("info"), WARNING("warning"), ERROR("error");
    private final String severity;

    SeverityType(String severity) {
        this.severity = severity;
    }

    public String getSeverity() {
        return severity;
    }
}
