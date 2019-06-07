package developingalex.com.waxtradeapp.Adapters;

public class RecentTradePartners {

    private final String username, avatar, tradeURL;

    public RecentTradePartners(String username, String avatar, String tradeURL) {
        this.username = username;
        this.avatar = avatar;
        this.tradeURL = tradeURL;
    }

    String getUsername() {
        return username;
    }

    String getAvatar() {
        return avatar;
    }

    public String getTradeURL() {
        return tradeURL;
    }
}
