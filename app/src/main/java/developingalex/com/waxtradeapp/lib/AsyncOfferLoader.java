package developingalex.com.waxtradeapp.lib;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import developingalex.com.waxtradeapp.interfaces.OfferListener;
import developingalex.com.waxtradeapp.interfaces.TradeInterface;
import developingalex.com.waxtradeapp.objects.StandardItem;
import developingalex.com.waxtradeapp.objects.StandardTradeOffer;

public class AsyncOfferLoader extends AsyncTask<String, Void, JSONArray> {

    private String requestType;
    private TradeInterface tradeInterface;
    private String type, states, sort;
    private int offer_id;
    private OfferListener callback;

    public AsyncOfferLoader(Context context, String type, String states, String sort, OfferListener callback) {
        this.requestType = "OFFERS";
        tradeInterface = new TradeImplementation(context);
        this.type = type;
        this.states =  states;
        this.sort = sort;
        this.callback = callback;
    }

    public AsyncOfferLoader(Context context, int offer_id, OfferListener callback) {
        this.requestType = "OFFER";
        tradeInterface = new TradeImplementation(context);
        this.offer_id = offer_id;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONArray doInBackground(String... params) {

        JSONArray offers = new JSONArray();
        JSONObject output;

        try {

            if (this.requestType.equals("OFFERS")) {
                output = tradeInterface.getOffers(this.type, this.states, this.sort);

                if (output != null && Integer.parseInt(output.getString("total")) != 0) {
                    offers = output.getJSONArray("offers");
                }
            }

            if (this.requestType.equals("OFFER")) {
                output = tradeInterface.getOffer(this.offer_id);

                if (output != null) {
                    offers.put(output.getJSONObject("offer"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return offers;
    }

    @Override
    protected void onPostExecute(JSONArray offers) {

        ArrayList<StandardTradeOffer> offersList = new ArrayList<>();

        if (offers.length() != 0) {
            for (int i = 0; i < offers.length(); i++) {
                try {
                    JSONObject offer = offers.getJSONObject(i);
                    offersList.add(
                            new StandardTradeOffer(
                                    offer.has("id") && !offer.isNull("id") ? offer.getInt("id") : -1,
                                    getOfferInformation(offer.getJSONObject("sender")),
                                    getOfferInformation(offer.getJSONObject("recipient")),
                                    offer.has("state") && !offer.isNull("state") ? offer.getInt("state"): -1,
                                    offer.has("state_name") && !offer.isNull("state_name") ? offer.getString("state_name"): "",
                                    offer.has("time_created") && !offer.isNull("time_created") ? offer.getInt("time_created") : -1,
                                    offer.has("time_updated") && !offer.isNull("time_updated") ? offer.getInt("time_updated") : -1,
                                    offer.has("time_expires") && !offer.isNull("time_expires") ? offer.getInt("time_expires"): -1,
                                    offer.has("message") && !offer.isNull("message") ? offer.getString("message"): "",
                                    offer.getBoolean("is_gift"),
                                    offer.getBoolean("is_case_opening"),
                                    offer.getBoolean("sent_by_you")
                            )
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            callback.onSuccess(offersList);
        } else {
            callback.onFailure("Offers empty");
        }
    }

    private StandardTradeOffer.StandardTradeOfferInformation getOfferInformation(JSONObject offerInformation) throws JSONException {
        return new StandardTradeOffer.StandardTradeOfferInformation(
                offerInformation.has("uid") && !offerInformation.isNull("uid") ? offerInformation.getInt("uid") : -1,
                offerInformation.has("steam_id") && !offerInformation.isNull("steam_id") ? offerInformation.getString("steam_id") : "",
                offerInformation.has("display_name") && !offerInformation.isNull("display_name") ? offerInformation.getString("display_name") : "",
                offerInformation.has("avatar") && !offerInformation.isNull("avatar") ? offerInformation.getString("avatar") : "",
                offerInformation.getBoolean("verified"),
                getItems(offerInformation.getJSONArray("items"))
        );
    }

    private ArrayList<StandardItem> getItems(JSONArray items) throws JSONException {

        ArrayList<StandardItem> itemsList = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            itemsList.add(
                    new StandardItem(
                            item.has("id") && !item.isNull("id") ? item.getInt("id") : -1,
                            item.has("internal_app_id") && !item.isNull("internal_app_id") ? item.getInt("internal_app_id") : -1,
                            item.has("def_id") && !item.isNull("def_id") ? item.getInt("def_id") : -1,
                            item.has("sku") && !item.isNull("sku") ? item.getInt("sku") : -1,
                            item.has("wear") && !item.isNull("wear") ? item.getDouble("wear") : 0.0,
                            item.getBoolean("tradable"),
                            item.getBoolean("is_trade_restricted"),
                            item.has("trade_hold_expires") && !item.isNull("trade_hold_expires") ? item.getInt("trade_hold_expires") : 0,
                            item.has("name") && !item.isNull("name") ? item.getString("name") : "",
                            item.has("market_name") && !item.isNull("market_name") ? item.getString("market_name") : "",
                            item.has("category") && !item.isNull("category") ? item.getString("category") : "",
                            item.has("rarity") && !item.isNull("rarity") ? item.getString("rarity") : "",
                            item.has("type") && !item.isNull("type") ? item.getString("type") : "",
                            item.has("color") && !item.isNull("color") ? item.getString("color") : "",
                            item.has("image") && !item.isNull("image") ? item.get("image") : null,
                            item.has("suggested_price") && !item.isNull("suggested_price") ? item.getInt("suggested_price") : 0,
                            item.has("suggested_price_floor") && !item.isNull("suggested_price_floor") ? item.getInt("suggested_price_floor") : 0,
                            item.getBoolean("instant_sell_enabled"),
                            item.has("preview_urls") && !item.isNull("preview_urls") ? item.get("preview_urls") : null,
                            item.has("assets") && !item.isNull("assets") ? item.get("assets") : null,
                            item.has("inspect") && !item.isNull("inspect") ? item.getString("inspect") : "",
                            item.has("eth_inspect") && !item.isNull("eth_inspect") ? item.getString("eth_inspect") : "",
                            item.has("pattern_index") && !item.isNull("pattern_index") ? item.getInt("pattern_index") : 0,
                            item.has("paint_index") && !item.isNull("paint_index") ? item.getInt("paint_index") : 0,
                            item.has("wear_tier_index") && !item.isNull("wear_tier_index") ? item.getInt("wear_tier_index") : 0,
                            item.has("time_created") && !item.isNull("time_created") ? item.getInt("time_created") : 0,
                            item.has("time_updated") && !item.isNull("time_updated") ? item.getInt("time_updated") : 0,
                            item.has("attributes") && !item.isNull("attributes") ? item.get("attributes") : null
                    )
            );
        }

        return itemsList;
    }

}
