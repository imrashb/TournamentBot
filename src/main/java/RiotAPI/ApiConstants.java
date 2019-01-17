package RiotAPI;

public class ApiConstants {


    /*
     * Platforms Constants
     * https://developer.riotgames.com/regional-endpoints.html
     */
    public final static String PLATFORM_BR = "br1.api.riotgames.com";
    public final static String PLATFORM_EUNE = "eun1.api.riotgames.com";
    public final static String PLATFORM_EUW = "euw1.api.riotgames.com";
    public final static String PLATFORM_JP = "jp1.api.riotgames.com";
    public final static String PLATFORM_KR = "kr.api.riotgames.com";
    public final static String PLATFORM_LAN = "la1.api.riotgames.com";
    public final static String PLATFORM_LAS = "la2.api.riotgames.com";
    public final static String PLATFORM_NA = "na1.api.riotgames.com";
    public final static String PLATFORM_OCE = "oc1.api.riotgames.com";
    public final static String PLATFORM_TR = "tr1.api.riotgames.com";
    public final static String PLATFORM_RU = "ru.api.riotgames.com";
    public final static String PLATFORM_PBE = "pbe1.api.riotgames.com";

    // For tournaments, closest proxy to me
    public final static String REGIONAL_PROXY = "americas.api.riotgames.com";

    // regions for tournaments
    public final static String REGION_BR = "BR";
    public final static String REGION_EUNE = "EUNE";
    public final static String REGION_EUW = "EUW";
    public final static String REGION_JP = "JP";
    public final static String REGION_KR = "KR";
    public final static String REGION_LAN = "LAN";
    public final static String REGION_LAS = "LAS";
    public final static String REGION_NA = "NA";
    public final static String REGION_OCE = "OCE";
    public final static String REGION_TR = "TR";
    public final static String REGION_RU = "RU";
    public final static String REGION_PBE = "PBE";


   /*
    * Api Links
    * https://developer.riotgames.com/api-methods/
    */
    public final static String SERVICES_STATUS = "/lol/status/v3/shard-data"; // Gets services status

    //https://developer.riotgames.com/api-methods/#tournament-stub-v4
    public final static String TOURNAMENT_CREATE_CODE = "/lol/tournament-stub/v4/codes";
    public final static String TOURNAMENT_GET_EVENTS = "/lol/tournament-stub/v4/lobby-events/by-code/";
    public final static String TOURNAMENT_CREATE_PROVIDER = "/lol/tournament-stub/v4/providers";
    public final static String TOURNAMENT_CREATE_TOURNAMENT = "/lol/tournament-stub/v4/tournaments";
    public final static String GET_SUMMONER_BY_NAME = "/lol/summoner/v4/summoners/by-name/";
    public final static String GET_SUMMONER_BY_ID = "/lol/summoner/v4/summoners/";


}
