package RiotAPI.Tournament;

import RiotAPI.Utils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static RiotAPI.ApiConstants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentBuilder {

    private String region = null;
    private ArrayList<String> playerIds = new ArrayList<>();
    private String spectateMode = null;
    private int teamSize = 5;
    private String pickType = null;
    private String mapType = null;
    private String tournamentName = null;

    private int tournamentId;
    private int providerId;
    private String tournamentCode;

    private int bestOf = 3;

    public TournamentBuilder() {

    }

    public int getBestOf() {
        return bestOf;
    }

    public void setBestOf(int bestOf) {
        this.bestOf = bestOf;
    }

    public TournamentBuilder setRegion(String region) {
        String actualRegion;
        if(region.contains("br1"))
            actualRegion = "BR";
        else if(region.contains("eun1"))
            actualRegion = "EUNE";
        else if(region.contains("euw1"))
            actualRegion = "EUW";
        else if(region.contains("jp1"))
            actualRegion = "JP";
        else if(region.contains("kr"))
            actualRegion = "KR";
        else if(region.contains("la1"))
            actualRegion = "LAN";
        else if(region.contains("la2"))
            actualRegion = "LAS";
        else if(region.contains("na1"))
            actualRegion = "NA";
        else if(region.contains("oc1"))
            actualRegion = "OCE";
        else if(region.contains("tr1"))
            actualRegion = "TR";
        else if(region.contains("ru"))
            actualRegion = "RU";
        else if(region.contains("pbe1"))
            actualRegion = "PBE";
        else
            actualRegion = null;
        this.region = actualRegion;
        return this;
    }

    public TournamentBuilder setSpectateMode(String spectateMode) {
        this.spectateMode = spectateMode;
        return this;
    }

    public TournamentBuilder setTeamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public TournamentBuilder addPlayer(String playerId) {
        for(int i = 0; i<playerIds.size(); i++) {
            if(playerIds.get(i).equals(playerId))
                return this;
        }
        playerIds.add(playerId);
        return this;
    }

    public TournamentBuilder removePlayer(String playerId) {
        for(int i = 0; i<playerIds.size(); i++) {
            if(playerIds.get(i).equals(playerId)) {
                playerIds.remove(i);
                return this;
            }
        }
        return this;
    }

    public TournamentBuilder setTournamentName(String name) {
        this.tournamentName = name;
        return this;
    }

    public TournamentBuilder setPickType(String pickType) {
        this.pickType = pickType;
        return this;
    }

    public TournamentBuilder setMapType(String mapType) {
        this.mapType = mapType;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public ArrayList<String> getPlayerIds() {
        return playerIds;
    }

    public String getSpectateMode() {
        return spectateMode;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public String getPickType() {
        return pickType;
    }

    public String getMapType() {
        return mapType;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public int getTournamentId() {
        return tournamentId;
    }

    public int getProviderId() {
        return providerId;
    }

    public String getTournamentCode() {
        return tournamentCode;
    }

    private void createProviderCode() throws Exception{
        if(!isReady()) {
            System.out.println("Tournament isn't ready. Make sure you have set everything up.");
            throw new Exception("Tournament isn't ready. Make sure you have set everything up.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        JSONObject request = new JSONObject();
        request.put("region", region);
        request.put("url", "https://www.discord.gg");

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        try {
            providerId = restTemplate.postForObject(Utils.createURI(REGIONAL_PROXY, TOURNAMENT_CREATE_PROVIDER+"?api_key="), entity, Integer.class);
        } catch(Exception e) {
            System.out.println("There was a problem with getting providerId");
            throw new Exception("There was a problem with getting providerId");
        }
    }

    private void createTournamentId() throws Exception{
        if(!isReady()) {
            System.out.println("Tournament isn't ready. Make sure you have set everything up.");
            throw new Exception("Tournament isn't ready. Make sure you have set everything up.");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        JSONObject request = new JSONObject();
        request.put("name", tournamentName);
        request.put("providerId", providerId);

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        try {
            tournamentId = restTemplate.postForObject(Utils.createURI(REGIONAL_PROXY, TOURNAMENT_CREATE_TOURNAMENT+"?api_key="), entity, Integer.class);
        } catch(Exception e) {
            System.out.println("There was a problem with getting tournamentId");
            throw new Exception("There was a problem with getting tournamentId");
        }
    }

    private void createTournamentCode() throws Exception{
        if(!isReady()) {
            System.out.println("Tournament isn't ready. Make sure you have set everything up.");
            throw new Exception("Tournament isn't ready. Make sure you have set everything up.");
        }


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        JSONArray players = new JSONArray();
        for(int i = 0; i<playerIds.size(); i++) {
            players.put(playerIds.get(i));
        }

        JSONObject request = new JSONObject();
        request.put("allowedSummonerIds", players);
        request.put("mapType", mapType);
        request.put("pickType", pickType);
        request.put("spectatorType", spectateMode);
        request.put("teamSize", teamSize);

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

        try {
            tournamentCode = restTemplate.postForObject(Utils.createURI(REGIONAL_PROXY, TOURNAMENT_CREATE_CODE+"?tournamentId="+tournamentId+"&api_key="), entity, String.class)
                    .replaceAll("\"", "")
                    .replaceAll("\\[", "")
                    .replaceAll("\\]","");
        } catch(Exception e) {
            System.out.println("There was a problem with getting tournamentCode");
            throw new Exception("There was a problem with getting tournamentCode");
        }
    }

    public void build() throws Exception {
        createProviderCode();
        createTournamentId();
        createTournamentCode();
    }

    public boolean isReady() {
        if(playerIds.size() > 0 && region != null && pickType != null && mapType != null && tournamentName != null && spectateMode != null)
            return true;
        else
            return false;
    }


}
