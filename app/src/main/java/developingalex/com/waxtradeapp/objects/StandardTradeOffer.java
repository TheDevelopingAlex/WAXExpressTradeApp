package developingalex.com.waxtradeapp.objects;

import java.util.ArrayList;

public class StandardTradeOffer {

    private final int id, state, time_created, time_updated, time_expires;
    private final boolean is_gift, is_case_opening, sent_by_you;
    private final String state_name, message;
    private final StandardTradeOfferInformation sender, recipient;

    public StandardTradeOffer(int id, StandardTradeOfferInformation sender, StandardTradeOfferInformation recipient, int state, String state_name, int time_created, int time_updated, int time_expires, String message, boolean is_gift, boolean is_case_opening, boolean sent_by_you) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.state = state;
        this.state_name = state_name;
        this.time_created = time_created;
        this.time_updated = time_updated;
        this.time_expires = time_expires;
        this.message = message;
        this.is_gift = is_gift;
        this.is_case_opening = is_case_opening;
        this.sent_by_you = sent_by_you;
    }

    public int getId() {
        return id;
    }

    public int getState() {
        return state;
    }

    public int getTime_created() {
        return time_created;
    }

    public int getTime_updated() {
        return time_updated;
    }

    public int getTime_expires() {
        return time_expires;
    }

    public boolean isIs_gift() {
        return is_gift;
    }

    public boolean isIs_case_opening() {
        return is_case_opening;
    }

    public boolean isSent_by_you() {
        return sent_by_you;
    }

    public String getState_name() {
        return state_name;
    }

    public String getMessage() {
        return message;
    }

    public StandardTradeOfferInformation getSender() {
        return sender;
    }

    public StandardTradeOfferInformation getRecipient() {
        return recipient;
    }

    public static class StandardTradeOfferInformation {
        private final int uid;
        private final String steam_id, display_name, avatar;
        private final boolean verified;
        private final ArrayList<StandardItem> items;

        public StandardTradeOfferInformation(int uid, String steam_id, String display_name, String avatar, boolean verified, ArrayList<StandardItem> items) {
            this.uid = uid;
            this.steam_id = steam_id;
            this.display_name = display_name;
            this.avatar = avatar;
            this.verified = verified;
            this.items = items;
        }

        public int getUid() {
            return uid;
        }

        public String getSteam_id() {
            return steam_id;
        }

        public String getDisplay_name() {
            return display_name;
        }

        public String getAvatar() {
            return avatar;
        }

        public boolean isVerified() {
            return verified;
        }

        public ArrayList<StandardItem> getItems() {
            return items;
        }
    }
}
