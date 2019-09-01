package developingalex.com.waxtradeapp.objects;

public class StandardItem {

    private final int id, internal_app_id, def_id, sku, trade_hold_expires, pattern_index, paint_index, wear_tier_index, time_created, time_updated;
    private final double wear, suggested_price, suggested_price_floor;
    private final boolean tradeable, is_trade_restricted, instant_sell_enabled;
    private final String name, market_name, category, rarity, type, color, inspect, eth_inspect;
    private final Object image, preview_urls, assets, attributes;
    private boolean isSelected;

    public StandardItem(
            int id, int internal_app_id, int def_id, int sku, double wear, boolean tradable, boolean is_trade_restricted, int trade_hold_expires,
            String name, String market_name, String category, String rarity, String type, String color, Object image,
            int suggested_price, int suggested_price_floor, boolean instant_sell_enabled, Object preview_urls, Object assets,
            String inspect, String eth_inspect, int pattern_index, int paint_index, int wear_tier_index, int time_created, int time_updated, Object attributes
    ) {
        this.id = id;
        this.internal_app_id = internal_app_id;
        this.def_id = def_id;
        this.sku = sku;
        this.wear = wear;
        this.tradeable = tradable;
        this.is_trade_restricted = is_trade_restricted;
        this.trade_hold_expires = trade_hold_expires;
        this.name = name;
        this.market_name = market_name;
        this.category = category;
        this.rarity = rarity;
        this.type = type;
        this.color = color;
        this.image = image;
        this.suggested_price = (double) suggested_price / 100.0;
        this.suggested_price_floor=  (double) suggested_price_floor / 100.0;
        this.instant_sell_enabled = instant_sell_enabled;
        this.preview_urls = preview_urls;
        this.assets = assets;
        this.inspect = inspect;
        this.eth_inspect = eth_inspect;
        this.pattern_index = pattern_index;
        this.paint_index = paint_index;
        this.wear_tier_index = wear_tier_index;
        this.time_created = time_created;
        this.time_updated = time_updated;
        this.attributes = attributes;
        this.isSelected = false;
    }

    public int getId() {
        return id;
    }

    public int getInternal_app_id() {
        return internal_app_id;
    }

    public int getDef_id() {
        return def_id;
    }

    public int getSku() {
        return sku;
    }

    public int getTrade_hold_expires() {
        return trade_hold_expires;
    }

    public double getSuggested_price() {
        return suggested_price;
    }

    public double getSuggested_price_floor() {
        return suggested_price_floor;
    }

    public int getPattern_index() {
        return pattern_index;
    }

    public int getPaint_index() {
        return paint_index;
    }

    public int getWear_tier_index() {
        return wear_tier_index;
    }

    public int getTime_created() {
        return time_created;
    }

    public int getTime_updated() {
        return time_updated;
    }

    public double getWear() {
        return wear;
    }

    public boolean isTradeable() {
        return tradeable;
    }

    public boolean isIs_trade_restricted() {
        return is_trade_restricted;
    }

    public boolean isInstant_sell_enabled() {
        return instant_sell_enabled;
    }

    public String getName() {
        return name;
    }

    public String getMarket_name() {
        return market_name;
    }

    public String getCategory() {
        return category;
    }

    public String getRarity() {
        return rarity;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public String getInspect() {
        return inspect;
    }

    public String getEth_inspect() {
        return eth_inspect;
    }

    public Object getImage() {
        return image;
    }

    public Object getPreview_urls() {
        return preview_urls;
    }

    public Object getAssets() {
        return assets;
    }

    public Object getAttributes() {
        return attributes;
    }

    public boolean isHighlighted() { return isSelected; }

    public void toggleHighlighted() { isSelected = !isSelected; }
}
