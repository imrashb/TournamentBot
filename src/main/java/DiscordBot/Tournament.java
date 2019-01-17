package DiscordBot;

import RiotAPI.Tournament.TournamentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tournament {

    TournamentBuilder tournamentBuilder;
    Team controllerTeam = null;
    Team opponentTeam = null;
    String tournamentId;

    public Tournament(Team controllerTeam){
        this.controllerTeam = controllerTeam;
        tournamentBuilder = new TournamentBuilder()
                .setTournamentName("Tournament")
                .setRegion(controllerTeam.getRegion())
                .setMapType("SUMMONERS_RIFT")
                .setSpectateMode("LOBBYONLY")
                .setTeamSize(controllerTeam.getMembers().size()+1)
                .setPickType("TOURNAMENT_DRAFT");
        generateTournamentId();
    }

    public void setTournamentToTeams() {

        controllerTeam.addTournament(this);
        if(opponentTeam != null)
            opponentTeam.addTournament(this);

    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void generateTournamentId() {
        int leftLimit = 65; // letter 'a'
        int rightLimit = 90; // letter 'z'
        int targetStringLength = 30;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        this.tournamentId = buffer.toString();
    }

    public TournamentBuilder getTournamentBuilder() {
        return tournamentBuilder;
    }

    public void setTournamentBuilder(TournamentBuilder tournamentBuilder) {
        this.tournamentBuilder = tournamentBuilder;
    }

    public Team getControllerTeam() {
        return controllerTeam;
    }

    public Team getOpponentTeam() {
        return opponentTeam;
    }

    public void addOpponentTeam(Team opponentTeam) {
        if(this.opponentTeam != null)
            this.opponentTeam.removeTournament();
        this.opponentTeam = opponentTeam;
        setTournamentToTeams();
    }

    public void removeOpponentTeam() {
        if(opponentTeam != null)
            opponentTeam.removeTournament();
        opponentTeam = null;
        setTournamentToTeams();
    }

}
