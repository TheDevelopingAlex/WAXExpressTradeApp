package developingalex.com.waxtradeapp;

public class Offer {

    private int id;
    private String username;
    private String your_info;
    private String their_info;
    private String image;
    private String state_name;

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
