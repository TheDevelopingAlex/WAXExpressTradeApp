package developingalex.com.waxtradeapp;

public class OfferItem {

    private String itemID, name, wearName, price, image, wear, itemColor;
    private boolean isSelected;

    public OfferItem(String itemID, String name, String wearName, String wear, String price, String image, String itemColor, boolean isSelected) {
        this.itemID = itemID;
        this.name = name;
        this.wearName = wearName;
        this.wear = wear;
        this.price = price;
        this.image = image;
        this.itemColor = itemColor;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public String getWearName() {
        return wearName;
    }

    public String getWear() {
        return wear;
    }

    public String getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getItemID() {
        return itemID;
    }

    public String getItemColor() { return itemColor; }

    public boolean isHighlighted() { return isSelected; }

    public void toggleHighlighted() { isSelected = !isSelected; }
}
