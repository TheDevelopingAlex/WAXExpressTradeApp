package developingalex.com.waxtradeapp;

public class RecentTradePartners {

    private String username, avatar, tradeURL;

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
