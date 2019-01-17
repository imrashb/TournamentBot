package DiscordBot;

import java.util.List;

public class Checks {

    public static boolean isTeamLeader(TournamentUser tournamentUser) {
        if(tournamentUser.getTeam().getLeader() == tournamentUser)
            return true;
        else
            return false;
    }

    public static boolean hasTeam(TournamentUser tournamentUser) {
        if(tournamentUser.getTeam()  == null)
            return false;
        else
            return true;
    }

    public static boolean hasTournament(TournamentUser tournamentUser) {
        if(tournamentUser.getTeam().getTournament() == null)
            return false;
        else
            return true;
    }

    public static boolean canTeamControlTournament(TournamentUser tournamentUser) {
        if(tournamentUser.getTeam() == tournamentUser.getTeam().getTournament().getControllerTeam())
            return true;
        else
            return false;
    }

    public static boolean hasOpponentInTournament(TournamentUser tournamentUser) {
        if(tournamentUser.getTeam().getTournament().getOpponentTeam() == null)
            return false;
        else
            return true;
    }

    public static boolean isTeamNameUnique(String teamName, List<Team> teams) {
        for(int i = 0;i<teams.size(); i++) {
            if(teams.get(i).getName().toLowerCase().equals(teamName.toLowerCase()))
                return false;
        }
        return true;
    }




}
