package RiotAPI.Match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchTeams {

    private String win;

    public MatchTeams() {
    }


    public String getWin() {
        return win;
    }

    public void setWin(String win) {
        this.win = win;
    }
}
