package mk.ukim.finki.lab_01.config;

public enum CCMPConfig {
    DATA;
    private final String SECRET_KEY = "this is a very secret key";
    private final boolean PROTOCOL_ENABLED = true;

    public String getSECRET_KEY() {
        return SECRET_KEY;
    }

    public boolean isPROTOCOL_ENABLED() {
        return PROTOCOL_ENABLED;
    }
}
