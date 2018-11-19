package developingalex.com.waxtradeapp;

public class AppsData {

    String app_text, app_image;

    public AppsData(String app_text, String app_image){
        this.app_text = app_text;
        this.app_image = app_image;
    }

    public String getText(){
        return app_text;
    }

    public String getImage(){
        return app_image;
    }

}
