package DiscordBot;

import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private TournamentUser leader = null;
    private String name = null;
    private List<TournamentUser> members = new ArrayList<>();
    private String region;
    private Guild guild;
    private Tournament tournament = null;

    public Team() {
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void addTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public void removeTournament() {
        this.tournament = null;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public TournamentUser getLeader() {
        return leader;
    }

    public void setLeader(TournamentUser leader) {
        this.leader = leader;
        leader.addTeam(this);
        region = leader.getRegion();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TournamentUser> getMembers() {
        return members;
    }

    public void addMember(TournamentUser member) {
        for(int i = 0; i<members.size(); i++) {
            if(member == members.get(i)) {
                return;
            }
        }
        members.add(member);
        member.addTeam(this);

        adjustTeamSize();
    }

    public void removeMember(TournamentUser member) {
        for(int i = 0; i<members.size(); i++) {
            if(member == members.get(i)) {
                members.remove(member);
                member.removeTeam();
                return;
            }
        }
        adjustTeamSize();
    }


    public void adjustTeamSize() {
        if(tournament != null && tournament.getControllerTeam().getMembers().size()+1 > tournament.getOpponentTeam().getMembers().size()+1)
            tournament.getTournamentBuilder().setTeamSize(tournament.getControllerTeam().getMembers().size()+1);
        else if(tournament != null && tournament.getControllerTeam().getMembers().size()+1 < tournament.getOpponentTeam().getMembers().size()+1)
            tournament.getTournamentBuilder().setTeamSize(tournament.getOpponentTeam().getMembers().size()+1);
        else if(tournament != null)
            tournament.getTournamentBuilder().setTeamSize(members.size()+1);

        checkMapTeamSize();
    }

    public void checkMapTeamSize() {
        if(tournament.getTournamentBuilder().getTeamSize() > 3 && tournament.getTournamentBuilder().getMapType().equals("TWISTED_TREELINE"))
            tournament.getTournamentBuilder().setMapType("SUMMONERS_RIFT");
    }

    public void dispose() {

        while(members.size() != 0) {
            removeMember(members.get(0));
        }

        leader.removeTeam();
        leader = null;
        name = null;
        guild = null;
        region = null;
        members = null;
    }
}
