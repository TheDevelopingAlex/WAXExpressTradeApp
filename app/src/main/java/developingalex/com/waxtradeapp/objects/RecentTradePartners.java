package developingalex.com.waxtradeapp.objects;

public class RecentTradePartners {

    private final String username, avatar, tradeURL;

    public RecentTradePartners(String username, String avatar, String tradeURL) {
        this.username = username;
        this.avatar = avatar;
        this.tradeURL = tradeURL;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getTradeURL() {
        return tradeURL;
    }
}
