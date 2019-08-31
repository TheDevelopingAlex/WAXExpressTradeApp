package developingalex.com.waxtradeapp.interfaces;

public interface OAuthInterface {
    boolean accountSetup(String code) throws Exception;
    boolean logout();
    boolean checkAuthStatus();
    String getAuthURL();
    String getRedirectUri();
    String getUserID();
    String getUserProfileUsername();
    String getUserProfilePicture();
    String getUserTradeURL() throws Exception;
}
