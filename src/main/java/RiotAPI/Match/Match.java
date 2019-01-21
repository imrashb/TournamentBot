package RiotAPI.Match;

import RiotAPI.ApiConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {

    private List<MatchTeams> teams = new ArrayList<>();

    public Match() {
    }

    public List<MatchTeams> getTeams() {
        return teams;
    }

    public void setTeams(List<MatchTeams> teams) {
        this.teams = teams;
    }

    public static Match get(String platform, long matchId) {
        return new RestTemplate().getForObject("https://"+platform+"/lol/match/v4/matches/"+matchId+"?api_key="+ ApiConfig.API_KEY, Match.class);
    }

}
