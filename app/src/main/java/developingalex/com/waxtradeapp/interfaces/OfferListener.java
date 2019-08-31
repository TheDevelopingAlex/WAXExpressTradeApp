package developingalex.com.waxtradeapp.interfaces;

import java.util.ArrayList;

import developingalex.com.waxtradeapp.objects.StandardTradeOffer;

public interface OfferListener {
    void onSuccess(ArrayList<StandardTradeOffer> offers);
    void onFailure(String text);
}
