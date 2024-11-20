package pszerszenowicz.b2c2proxyserver.requests;

import java.util.List;

public class SubscribeRequest {

    private final String event = "subscribe";
    private String instrument;
    private String currency;
    private List<Integer> levels;
    private String tag;

    public SubscribeRequest(String instrument, String currency, List<Integer> levels, String tag) {
        this.instrument = instrument;
        this.currency = currency;
        this.levels = levels;
        this.tag = tag;
    }
}
