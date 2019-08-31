package developingalex.com.waxtradeapp.interfaces;

import org.json.JSONObject;

public interface TradeInterface {
    JSONObject getOffers(String type, String state, String sort) throws Exception;
    JSONObject getOffer(Integer offer_id) throws Exception;
    boolean acceptOffer(Integer offer_id, String twofactor_code) throws Exception;
    boolean cancelOffer(Integer offer_id) throws Exception;
    String sendOffer(String trade_url, String twofactor_code, String items_to_send, String items_to_receive, String message) throws Exception;
    JSONObject getApps() throws Exception;
    JSONObject getUserInventory(String user_id, Integer app_id) throws Exception;
    String getErrorMessage();
}
