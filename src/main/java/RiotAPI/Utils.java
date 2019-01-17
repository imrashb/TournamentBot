package RiotAPI;

import static RiotAPI.ApiConstants.*;

public class Utils {
    public static String createURI(String platform, String apiRequest) {
        return "https://"+platform+apiRequest+ApiConfig.API_KEY;
    }
}
