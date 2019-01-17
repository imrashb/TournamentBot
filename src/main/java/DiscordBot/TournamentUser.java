package DiscordBot;

import RiotAPI.Summoner.Summoner;
import net.dv8tion.jda.api.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TournamentUser {

    private User discordUser;
    private Summoner summoner = null;
    private Team team = null;
    private Database database;
    private String region;
    private String summonerId;

    public TournamentUser(User discordUser, Database database) {
        this.discordUser = discordUser;
        this.database = database;
        region = getRegion();
        summonerId = getSummonerId();
        setSummonerFromDatabase();
    }

    public String getSummonerId() {
        database.connect();
        Statement statement = database.getStatement();
        try {
            ResultSet result = statement.executeQuery("SELECT * FROM users WHERE _id='" + discordUser.getId() + "'");
            result.next();
            summonerId = result.getString("summonerId");
        } catch (SQLException e) {
            System.out.println("Something wrong happened : " + e.getMessage());
        }
        database.close();
        return summonerId;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
        database.connect();
        Statement statement = database.getStatement();
        try {
            statement.execute("UPDATE users SET summonerId='" + summonerId + "' WHERE _id='"+discordUser.getId()+"'");
        } catch (SQLException e) {
            System.out.println("Something wrong happened : " + e.getMessage());
        }
        database.close();
    }

    public String getRegion() {
        database.connect();
        Statement statement = database.getStatement();
        try {
            ResultSet result = statement.executeQuery("SELECT * FROM users WHERE _id='" + discordUser.getId() + "'");
            result.next();
            region = result.getString("region");
        } catch (SQLException e) {
            System.out.println("Something wrong happened : " + e.getMessage());
        }
        database.close();
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
        database.connect();
        Statement statement = database.getStatement();
        try {
            statement.execute("UPDATE users SET region='"+region+"' WHERE _id='"+discordUser.getId()+"'");
        } catch (SQLException e) {
            System.out.println("Something wrong happened : " + e.getMessage());
        }
        database.close();
    }

    public User getDiscordUser() {
        return discordUser;
    }

    public void setDiscordUser(User discordUser) {
        this.discordUser = discordUser;
    }

    public Summoner getSummoner() {
        return summoner;
    }

    public void setSummoner(Summoner summoner) {
        this.summoner = summoner;
        setSummonerId(summoner.getId());
    }

    public Team getTeam() {
        return team;
    }

    public void addTeam(Team team) {
        this.team = team;
        if(team.getLeader() != this) {
            team.addMember(this);
        }
    }

    public void removeTeam() {
        team = null;
    }

    public void setSummonerFromDatabase() {

        Summoner summoner = new Summoner().getFromId(region, summonerId);
        if (summoner != null)
            this.summoner = summoner;
        else
            this.summoner = null;
    }
}
