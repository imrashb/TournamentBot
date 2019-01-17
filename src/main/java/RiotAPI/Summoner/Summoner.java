package RiotAPI.Summoner;

import RiotAPI.LeagueServicesStatus.LeagueServicesStatus;
import RiotAPI.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.client.RestTemplate;

import static RiotAPI.ApiConstants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Summoner {

    private int profileIconId;
    private String name;
    private String puuid;
    private long summonerLevel;
    private String accountId;
    private String id;
    private long revisionDate;


    public Summoner() {

    }

    public int getProfileIconId() {
        return profileIconId;
    }

    public void setProfileIconId(int profileIconId) {
        this.profileIconId = profileIconId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public long getSummonerLevel() {
        return summonerLevel;
    }

    public void setSummonerLevel(long summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(long revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Summoner getFromName(String platform, String username) {
        try {
            return new RestTemplate().getForObject(Utils.createURI(platform, GET_SUMMONER_BY_NAME + username.replaceAll(" ", "_") + "?api_key="), Summoner.class);
        } catch(Exception e) {
            return null;
        }
    }

    public Summoner getFromId(String platform, String summonerId) {
        try {
            return new RestTemplate().getForObject(Utils.createURI(platform, GET_SUMMONER_BY_ID + summonerId + "?api_key="), Summoner.class);
        } catch(Exception e) {
            return null;
        }
    }
}
