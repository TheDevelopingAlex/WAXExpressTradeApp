package developingalex.com.waxtradeapp.Adapters;

public class Offer {

    private final int id;
    private final String username, your_info, their_info, image, state_name;

    public Offer(int id, String username, String your_info, String their_info, String image, String state) {
        this.id = id;
        this.username = username;
        this.your_info = your_info;
        this.their_info = their_info;
        this.image = image;
        this.state_name = state;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getYourOffer() {
        return your_info;
    }

    public String getTheirOffer() {
        return their_info;
    }

    public String getImage() {
        return image;
    }

    public String getStateName() { return state_name; }

}
