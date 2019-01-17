package DiscordBot.Events;

import DiscordBot.*;
import DiscordBot.Exceptions.*;
import RiotAPI.Summoner.Summoner;
import RiotAPI.Tournament.TournamentBuilder;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static DiscordBot.Exceptions.ExceptionConstants.*;
import static RiotAPI.ApiConstants.*;

public class ChatCommands extends ListenerAdapter {

    private JDA jda = null;
    private Database database = new Database();
    private String caller = "!tb";
    private ArrayList<TournamentUser> tournamentUsers = new ArrayList<>();

    private List<Team> teams = new ArrayList<>();
    private List<BiMap<Message, Team>> teamInvitations = new ArrayList<>();
    private List<BiMap<Message, Tournament>> tournamentInvitations = new ArrayList<>();

    private List<Tournament> tournaments = new ArrayList<>();

    private final int DELAY = 3000;
    private HashMap<User, Long> userDelays = new HashMap<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        if (jda == null) {
            jda = e.getJDA();
            database.update(jda);
            jda.getPresence().setActivity(Activity.of(Activity.ActivityType.DEFAULT, "Commands - !tb help"));
        }

        if(e.getAuthor().isBot())
            return;

        String[] message = e.getMessage().getContentRaw().split(" ");
        if (message[0].toLowerCase().equals(caller)) {

            User author = e.getAuthor();
            TextChannel textChannel = e.getChannel();

            boolean isAlreadyIn = false;
            for (int i = 0; i < tournamentUsers.size(); i++) {
                if (tournamentUsers.get(i).getDiscordUser() == author) {
                    isAlreadyIn = true;
                    i = tournamentUsers.size();
                }
            }
            if (!isAlreadyIn) {
                tournamentUsers.add(new TournamentUser(author, database));
            }

            try {
                switch (message[1].toLowerCase()) {

                    case "summoner":
                        if(message.length == 2) {
                            helpSummoner(e);
                            return;
                        }
                        switch(message[2].toLowerCase()) {
                            case "link":
                                if(!hasRespectedDelay(author)) {
                                    try {
                                        throw new DelayException(textChannel);
                                    } catch (DelayException e1) {
                                        return;
                                    }
                                }
                                linkSummoner(e);
                                userDelays.put(author, System.currentTimeMillis());
                                break;
                            case "info":
                                if(!hasRespectedDelay(author)) {
                                    try {
                                        throw new DelayException(textChannel);
                                    } catch (DelayException e1) {
                                        return;
                                    }
                                }
                                infoSummoner(e);
                                userDelays.put(author, System.currentTimeMillis());
                                break;
                            case "help":
                                helpSummoner(e);
                                break;
                            default:
                                try {
                                    throw new UnexistingCommandException(textChannel);
                                } catch (UnexistingCommandException e1) { }
                        }
                        break;
                    case "help":
                        help(e);
                        break;
                    case "team":
                        if(message.length == 2) {
                            helpTeam(e);
                            return;
                        }
                        switch(message[2].toLowerCase()) {
                            case "create":
                                createTeam(e);
                                break;
                            case "invite":
                                inviteTeam(e);
                                break;
                            case "info":
                                infoTeam(e);
                                break;
                            case "leave":
                                leaveTeam(e);
                                break;
                            case "disband":
                                disbandTeam(e);
                                break;
                            case "kick":
                                kickTeam(e);
                                break;
                            case "help":
                                helpTournament(e);
                                break;
                            default:
                                try {
                                    throw new UnexistingCommandException(textChannel);
                                } catch (UnexistingCommandException e1) { }
                        }
                        break;
                    case "tournament":
                        if(message.length == 2) {
                            helpTournament(e);
                            return;
                        }
                        switch(message[2].toLowerCase()) {
                            case "create":
                                createTournament(e);
                                break;
                            case "info":
                                infoTournament(e);
                                break;
                            case "invite":
                                inviteTournament(e);
                                break;
                            case "kick":
                                kickTournament(e);
                                break;
                            case "settings":
                                settingsTournament(e);
                                break;
                            case "help":
                                helpTournament(e);
                                break;
                            default:
                                try {
                                    throw new UnexistingCommandException(textChannel);
                                } catch (UnexistingCommandException e1) { }
                        }
                        break;
                    default:
                        try {
                            throw new UnexistingCommandException(textChannel);
                        } catch (UnexistingCommandException e1) {}
                }
            } catch (ArrayIndexOutOfBoundsException e1) {
                try {
                    throw new UnexistingCommandException(textChannel);
                } catch (UnexistingCommandException e2) {}
            } catch (CommandSyntaxErrorException e1) {
            } catch (RegionException e1) {
            } catch (SummonerException e1) {
            } catch (TeamException e1) {
            } catch (TournamentException e1) {
            }
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        String msgId = e.getMessageId();
        Message msg;
        for(int i = 0; i<teamInvitations.size(); i++) {
            for (Message m : teamInvitations.get(i).inverse().values()) {
                if (m.getId().equals(msgId)) {
                    msg = m;
                    if (e.getReactionEmote().getName().equals("✅")) {
                        for(Team t : teamInvitations.get(i).values()) {
                            if(t.getMembers().size()+1 > 5) {
                                msg.editMessage(":exclamation: The team you are attempting to join is already full... Better luck next time :exclamation:").queue();
                                teamInvitations.remove(i);
                                return;
                            }
                            TournamentUser tournamentUser = getTournamentUser(e.getUser());
                            t.addMember(tournamentUser);
                            t.getLeader().getDiscordUser().openPrivateChannel().queue(channel -> {
                                channel.sendMessage("**" + tournamentUser.getDiscordUser().getName() + "#" + tournamentUser.getDiscordUser().getDiscriminator() + "** has accepted your team invitation!").queue();
                            });

                        }
                        msg.editMessage("✅ Successfully accepted the invitation! ✅").queue();
                        teamInvitations.remove(i);
                    } else if (e.getReactionEmote().getId().equals("❌")) {
                        msg.editMessage("❌ Successfully declined the invitation! ❌").queue();
                        teamInvitations.remove(i);
                    }
                }
            }
        }

        for(int i = 0; i<tournamentInvitations.size(); i++) {
            for (Message m : tournamentInvitations.get(i).inverse().values()) {
                if (m.getId().equals(msgId)) {
                    msg = m;
                    if (e.getReactionEmote().getName().equals("✅")) {
                        for(Tournament t : tournamentInvitations.get(i).values()) {
                            if(t.getOpponentTeam() != null) {
                                msg.editMessage(":exclamation: The tournament you are trying to join is already full... Better luck next time :exclamation:").queue();
                                tournamentInvitations.remove(i);
                            }
                            TournamentUser tournamentUser = getTournamentUser(e.getUser());
                            t.addOpponentTeam(tournamentUser.getTeam());
                            t.getControllerTeam().getLeader().getDiscordUser().openPrivateChannel().queue(channel -> {
                                channel.sendMessage("The team **"+tournamentUser.getTeam().getName()+"** has accepted your challenge in the tournament **"+t.getTournamentBuilder().getTournamentName()+"**!").queue();
                            });
                        }
                        msg.editMessage("✅ Successfully accepted the challenge! ✅").queue();

                        tournamentInvitations.remove(i);
                    } else if (e.getReactionEmote().getId().equals("❌")) {
                        msg.editMessage("❌ Successfully declined the challenge! ❌").queue();
                        tournamentInvitations.remove(i);
                    }
                }
            }
        }

    }


    private boolean hasRespectedDelay(User user) {
        if(!userDelays.containsKey(user))
            return true;
        if(System.currentTimeMillis()-userDelays.get(user) < DELAY)
            return false;
        else
            return true;
    }

    private void help(GuildMessageReceivedEvent e) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(":blue_book: HELP COMMANDS :blue_book:")
                .addField("!tb summoner help", "Returns summoner commands information", false)
                .addField("!tb team help", "Returns team commands information", false)
                .addField("!tb tournament help", "Returns tournament commands information", false)
                .setColor(Color.BLUE);
        e.getChannel().sendMessage(eb.build()).queue();
    }


    private void helpSummoner(GuildMessageReceivedEvent e) {

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(":bust_in_silhouette: SUMMONER COMMANDS :bust_in_silhouette:")
                .setColor(Color.BLUE)
                .addField("!tb summoner link [region] [user_name]", "Links your League of Legends account to your Discord Account.", false)
                .addField("!tb summoner info [region] [user_name]", "Sends information about a League of Legends account", false);

        e.getChannel().sendMessage(eb.build()).queue();

    }

    private void helpTeam(GuildMessageReceivedEvent e) {

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(":video_game:  TEAM COMMANDS :video_game:")
                .setColor(Color.BLUE)
                .addField("!tb team create [name]", "Creates a team", false)
                .addField("!tb team invite [@user1] [@user2] ...", "Invites a discord user to your team", false)
                .addField("!tb team info", "Returns information about your team", false)
                .addField("!tb team leave", "Leaves your team", false)
                .addField("!tb team disband", "Disbands your team (Team leader only)", false)
                .addField("!tb team kick", "Kick a user from your team (Team leader only)", false);

        e.getChannel().sendMessage(eb.build()).queue();

    }

    private void helpTournament(GuildMessageReceivedEvent e) {

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(":trophy: TOURNAMENT COMMANDS :trophy:")
                .setColor(Color.BLUE)
                .addField("!tb tournament create [tournament_name]", "Creates a tournament", false)
                .addField("!tb tournament invite [team name]", "Invites a team to your tournament", false)
                .addField("!tb tournament info", "Returns information about the tournament", false);

        e.getChannel().sendMessage(eb.build()).queue();

    }

    private void linkSummoner(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, RegionException, SummonerException {
        String[] message = e.getMessage().getContentRaw().split(" ");
        TextChannel textChannel = e.getChannel();
        User author = e.getAuthor();

        if(message.length < 5)
            throw new CommandSyntaxErrorException("!tb summoner link [region] [user_name]", textChannel);

        String platform = getRegionApi(message[3].toLowerCase());

        if(platform == null)
            throw new RegionException(UNKNOWN_REGION_EXCEPTION, textChannel);

        String username = "";
        for(int i = 4; i<message.length; i++) {
            username+=message[i];
            if(i != message.length-1)
                username+="_";
        }

        Summoner summoner = new Summoner().getFromName(platform, username);
        if(summoner == null)
            throw new SummonerException(UNKNOWN_SUMMONER_EXCEPTION, textChannel);

        database.update(jda);
        TournamentUser tournamentUser = getTournamentUser(author);
        tournamentUser.setRegion(platform);
        tournamentUser.setSummoner(summoner);

        // Success
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setAuthor(summoner.getName(), "https://"+message[3].toLowerCase()+".op.gg/summoner/userName="+summoner.getName().replaceAll(" ", "+"), "http://ddragon.leagueoflegends.com/cdn/9.1.1/img/profileicon/"+summoner.getProfileIconId()+".png")
                .setTitle(":white_check_mark: "+summoner.getName()+" is now linked to your discord account :white_check_mark:");
        textChannel.sendMessage(eb.build()).queue();
    }

    private void infoSummoner(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, RegionException, SummonerException {
        String[] message = e.getMessage().getContentRaw().split(" ");
        TextChannel textChannel = e.getChannel();
        User author = e.getAuthor();

        if(message.length < 5)
            throw new CommandSyntaxErrorException("!tb summoner info [region] [user_name]", textChannel);

        String platform = getRegionApi(message[3].toLowerCase());

        if(platform == null)
            throw new RegionException(UNKNOWN_REGION_EXCEPTION, textChannel);

        String username = "";
        for(int i = 4; i<message.length; i++) {
            username+=message[i];
            if(i != message.length-1)
                username+="_";
        }

        Summoner summoner = new Summoner().getFromName(platform, username);
        if(summoner == null)
            throw new SummonerException(UNKNOWN_SUMMONER_EXCEPTION, textChannel);

        String summonerId = summoner.getId();
        String discordId = null;
        database.connect();
        Statement statement = database.getStatement();
        try {
            ResultSet results = statement.executeQuery("SELECT * FROM users WHERE summonerId='" + summonerId + "'");
            if (results.next())
                discordId = results.getString("_id");
        } catch(SQLException e1) {
            EmbedBuilder eb = new EmbedBuilder().setColor(Color.RED)
                    .addField(":exclamation: There was a problem with my database... :exclamation:", "Try again later", false);
            textChannel.sendMessage(eb.build()).queue();
            return;
        }
        database.close();
        List<User> users = jda.getUsers();
        User user = null;
        for(int i = 0; i<users.size(); i++) {
            if(users.get(i).getId().equals(discordId)) {
                user = users.get(i);
                i = users.size();
            }
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.BLUE)
                .addField("Summoner Level",summoner.getSummonerLevel()+"", false)
                .setAuthor(summoner.getName(), "https://"+message[3].toLowerCase()+".op.gg/summoner/userName="+summoner.getName().replaceAll(" ", "+"), "http://ddragon.leagueoflegends.com/cdn/9.1.1/img/profileicon/"+summoner.getProfileIconId()+".png")
                .setImage("http://ddragon.leagueoflegends.com/cdn/9.1.1/img/profileicon/"+summoner.getProfileIconId()+".png");
        if(user != null) {
            eb.addField("Linked to",user.getName()+"#"+user.getDiscriminator() , false);
            eb.setImage(user.getAvatarUrl());
        }
        textChannel.sendMessage(eb.build()).queue();
    }

    private void createTeam(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, SummonerException, TeamException {

        User author = e.getAuthor();
        String[] message = e.getMessage().getContentRaw().split(" ");
        TextChannel textChannel = e.getChannel();

        TournamentUser tournamentUser = getTournamentUser(author);

        if(message.length < 4)
            throw new CommandSyntaxErrorException("!tb team create [team name]",textChannel);

        if(tournamentUser.getSummoner() == null)
            throw new SummonerException(SUMMONER_NOT_LINKED_EXCEPTION, textChannel);

        if(tournamentUser.getTeam() != null)
            throw new TeamException(ALREADY_IN_TEAM_EXCEPTION, textChannel);

        StringBuilder teamName = new StringBuilder("");
        for(int i = 3; i<message.length; i++) {
            teamName.append(message[i]+" ");
        }
        teamName.deleteCharAt(teamName.length()-1);


        if(!teamName.toString().matches("[a-zA-z ]*"))
            throw new TeamException(BAD_TEAM_NAME_EXCEPTION, textChannel);

        if(teamName.length() > 20 || teamName.length() < 3)
            throw new TeamException(TEAM_NAME_LENGTH_EXCEPTION, textChannel);

        if(!Checks.isTeamNameUnique(teamName.toString(), teams))
            throw new TeamException(TEAM_NAME_ALREADY_TAKEN_EXCEPTION,textChannel);

        Team team = new Team();
        team.setGuild(e.getGuild());
        team.setName(teamName.toString());
        team.setLeader(tournamentUser);

        teams.add(team);

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":white_check_mark: Successfully created team "+teamName+"! :white_check_mark:")
                .addField("You can now invite players in your team!", "!tb team invite [discord_user_name]", false);
        textChannel.sendMessage(eb.build()).queue();
    }

    private void inviteTeam(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, TeamException {
        User author = e.getAuthor();
        TextChannel textChannel = e.getChannel();
        String[] message = e.getMessage().getContentRaw().split(" ");
        TournamentUser tournamentUser = getTournamentUser(author);

        if(message.length < 4)
            throw new CommandSyntaxErrorException("!tb team invite [@user1] [@user2] ...", textChannel);

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(!Checks.isTeamLeader(tournamentUser))
            throw new TeamException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        Team team = tournamentUser.getTeam();

        if(team.getMembers().size()+1 > 5)
            throw new TeamException(OVER_TEAM_LIMIT_EXCEPTION, textChannel);

        List<User> invitedUsers = new ArrayList<>();
        List<User> userList = jda.getUsers();
        for(int i = 3; i<message.length; i++) {
            for(int k = 0; k<userList.size(); k++) {
                if(message[i].contains(userList.get(k).getId())) {
                    if (getTournamentUser(userList.get(k)) == null) {
                        tournamentUsers.add(new TournamentUser(userList.get(k), database));
                        k--;
                        continue;
                    } else if(userList.get(k).isBot()){
                        try {
                            throw new TeamException(INVITE_BOT_TO_TEAM_EXCEPTION, textChannel);
                        } catch (TeamException e1) {
                            k = userList.size();
                            continue;
                        }
                    }
                    else if (getTournamentUser(userList.get(k)).getSummoner() == null) {
                        try {
                            throw new SummonerException("You can't invite <@" + userList.get(k).getId() + "> to your team because he hasn't linked his League of Legends account to his Discord account.", textChannel);
                        } catch (SummonerException e1) {
                            k = userList.size();
                            continue;
                        }
                    } else if (!getTournamentUser(userList.get(k)).getRegion().equals(tournamentUser.getRegion())) {
                        try {
                            throw new RegionException("You can't invite <@" + userList.get(k).getId() + "> since he isn't your region.", textChannel);
                        } catch (RegionException e1) {
                            k = userList.size();
                            continue;
                        }
                    } else if (getTournamentUser(userList.get(k)).getTeam() != null) {
                        try {
                            throw new TeamException("You can't invite <@" + userList.get(k).getId() + "> because he already has a team.", textChannel);
                        } catch (TeamException e1) {
                            k = userList.size();
                            continue;
                        }
                    }
                    invitedUsers.add(userList.get(k));
                    userList.get(k).openPrivateChannel().queue(channel -> {
                        channel.sendMessage("**" + author.getName() + "#" + author.getDiscriminator() + "** has invited you to his team **" + team.getName() + "** in the server **" + e.getGuild().getName() + "**. " +
                                "React to this message with :white_check_mark: to accept the invitation or with :x: to decline the invitation.").queue(new Consumer<Message>() {
                            @Override
                            public void accept(Message t) {
                                t.addReaction("✅");
                                t.addReaction("❌");
                                BiMap<Message, Team> map = HashBiMap.create();
                                map.put(t, team);
                                teamInvitations.add(map);
                            }
                        });
                    });
                }
            }
        }

        StringBuilder sb = new StringBuilder(":white_check_mark: Successfully invited ");
        for(int i = 0; i<invitedUsers.size(); i++) {
            sb.append(invitedUsers.get(i).getName()+"#"+invitedUsers.get(i).getDiscriminator());
            if(i < invitedUsers.size()-1)
                sb.append(", ");
        }
        sb.append(" to your team! :white_check_mark:");

        if(invitedUsers.size() != 0) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle(sb.toString());
            textChannel.sendMessage(eb.build()).queue();
        }
    }

    private void infoTeam(GuildMessageReceivedEvent e) throws TeamException {
        TextChannel textChannel = e.getChannel();
        Team team = getTournamentUser(e.getAuthor()).getTeam();

        if(team == null)
            throw new TeamException(NOT_IN_TEAM_EXCEPTION,textChannel);

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.BLUE)
                .setTitle(team.getName())
                .addField("Leader", team.getLeader().getDiscordUser().getName()+"#"+team.getLeader().getDiscordUser().getDiscriminator(), false);
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<team.getMembers().size(); i++) {
            sb.append(team.getMembers().get(i).getDiscordUser().getName()+"#"+team.getMembers().get(i).getDiscordUser().getDiscriminator()+"\n");
        }
        if(team.getMembers().size() != 0)
            eb.addField("Members", sb.toString(), false);

        textChannel.sendMessage(eb.build()).queue();

    }

    private void disbandTeam(GuildMessageReceivedEvent e) throws TeamException {
        TextChannel textChannel = e.getChannel();

        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(!Checks.isTeamLeader(tournamentUser))
            throw new TeamException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        Team team = tournamentUser.getTeam();

        for(int i = 0; i<teams.size(); i++) {
            if(teams.get(i) == team)
            {
                teams.remove(i);
                team.dispose();
                EmbedBuilder eb = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("✅ Successfully disbanded your team ✅");
                textChannel.sendMessage(eb.build()).queue();
            }
        }
    }

    private void leaveTeam(GuildMessageReceivedEvent e) throws TeamException {

        TextChannel textChannel = e.getChannel();

        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(tournamentUser.getTeam().getLeader() == getTournamentUser(e.getAuthor()))
            throw new TeamException(LEADER_LEAVING_TEAM_EXCEPTION, textChannel);

        Team team = tournamentUser.getTeam();
        team.removeMember(tournamentUser);

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("✅ Successfully left team "+team.getName());
        textChannel.sendMessage(eb.build()).queue();
    }

    private void kickTeam(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, TeamException {
        TextChannel textChannel = e.getChannel();
        String[] message = e.getMessage().getContentRaw().split(" ");

        if(message.length != 4)
            throw new CommandSyntaxErrorException("!tb team kick [@user]",textChannel);

        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(!Checks.isTeamLeader(tournamentUser))
            throw new TeamException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        Team team = tournamentUser.getTeam();

        if(message[3].contains(team.getLeader().getDiscordUser().getId()))
            throw new TeamException(TRY_KICKING_HIMSELF_EXCEPTION, textChannel);

        TournamentUser memberToKick = null;
        for(int i = 0; i<team.getMembers().size(); i++) {
            if(message[3].contains(team.getMembers().get(i).getDiscordUser().getId()))
                memberToKick = team.getMembers().get(i);
        }

        if(memberToKick == null)
            throw new TeamException("I couldn't find this user in your team, make sure you tag him.\nFor example, **!tb team kick <@"+e.getAuthor().getId()+">**",textChannel);

        team.removeMember(memberToKick);
        memberToKick.getDiscordUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(":wave: **"+team.getLeader().getDiscordUser().getName()+"#"+team.getLeader().getDiscordUser().getDiscriminator()+"" +
                    " kicked you off the team "+team.getName()+".").queue();
        });

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":white_check_mark: Successfully kicked "+memberToKick.getDiscordUser().getName()+"#"+memberToKick.getDiscordUser().getDiscriminator()+"" +
                        " off your team! :white_check_mark:");
        textChannel.sendMessage(eb.build()).queue();
    }

    private void createTournament(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, TournamentException, TeamException {
        TextChannel textChannel = e.getChannel();
        String[] message = e.getMessage().getContentRaw().split(" ");

        if(message.length < 4)
            throw new CommandSyntaxErrorException("!tb tournament create [tournament_name]",textChannel);

        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(!Checks.isTeamLeader(tournamentUser))
            throw new TournamentException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        StringBuilder sb = new StringBuilder();
        for(int i = 3; i<message.length; i++) {
            sb.append(message[i]);
            if(i != message.length-1)
                sb.append(" ");
        }

        if(sb.length() < 3 || sb.length() > 30)
            throw new TournamentException(TOURNAMENT_NAME_LENGTH_EXCEPTION,textChannel);

        Tournament tournament = new Tournament(tournamentUser.getTeam());
        tournament.getTournamentBuilder().setTournamentName(sb.toString());
        tournaments.add(tournament);
        tournamentUser.getTeam().addTournament(tournament);

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":white_check_mark: Successfully created tournament **"+sb.toString()+"**!");
        textChannel.sendMessage(eb.build()).queue();
    }

    private void infoTournament(GuildMessageReceivedEvent e) throws TournamentException {

        TextChannel textChannel = e.getChannel();
        String[] message = e.getMessage().getContentRaw().split(" ");
        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(tournamentUser.getTeam() == null && message.length == 3)
            throw new TournamentException(NOT_IN_TEAM_EXCEPTION, textChannel);

        TournamentBuilder tb;
        if(message.length == 3) {
            if(tournamentUser.getTeam().getTournament() == null)
                throw new TournamentException(NOT_IN_TOURNAMENT_EXCEPTION, textChannel);
            tb = tournamentUser.getTeam().getTournament().getTournamentBuilder();
        } else {
            Tournament t = getTournamentFromId(message[3]);
            if(t == null)
                throw new TournamentException(TOURNAMENT_ID_DOESNT_EXIST_EXCEPTION, textChannel);
            else {
                tb = t.getTournamentBuilder();
            }

        }
        Tournament t;
        if(message.length == 4)
            t = getTournamentFromId(message[3]);
        else
            t = tournamentUser.getTeam().getTournament();
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.BLUE)
                .addField(tb.getTournamentName()+" ("+t.getTournamentId()+")","Map: "+tb.getMapType()+"\nPick type: "+tb.getPickType()+"" +
                        "\nSpectating type: "+tb.getSpectateMode()+"\nMode: Best of "+tb.getBestOf(), false);
        StringBuilder sb = new StringBuilder();

        List<TournamentUser> controllerTeam = t.getControllerTeam().getMembers();
        sb.append(t.getControllerTeam().getLeader().getSummoner().getName()+"\n");
        for(int i = 0; i<controllerTeam.size(); i++) {
            sb.append(controllerTeam.get(i).getSummoner().getName()+"\n");
        }
        eb.addField(t.getControllerTeam().getName(), sb.toString(), true);
        Team opponentTeam = t.getOpponentTeam();
        sb = new StringBuilder();
        if(opponentTeam != null) {
            sb.append(opponentTeam.getLeader().getSummoner().getName()+"\n");
            for(int i = 0; i<opponentTeam.getMembers().size(); i++) {
                sb.append(opponentTeam.getMembers().get(i).getSummoner().getName()+"\n");
            }
        } else {
            sb.append("Waiting for invite/join");
        }

        String opponentTeamName;
        if(opponentTeam == null)
            opponentTeamName = "TEAM 2";
        else
            opponentTeamName = opponentTeam.getName();
        eb.addField(opponentTeamName, sb.toString(), true);

        if(opponentTeam == null)
            eb.addField("Fighting arrangement: "+tb.getTeamSize()+"v"+tb.getTeamSize(),"", false);
        else
            eb.addField("Fighting arrangement: "+(controllerTeam.size()+1)+"v"+(opponentTeam.getMembers().size()+1),"", false);

        textChannel.sendMessage(eb.build()).queue();

    }

    private void inviteTournament(GuildMessageReceivedEvent e) throws CommandSyntaxErrorException, TournamentException, TeamException {

        TextChannel textChannel = e.getChannel();
        String[] message = e.getMessage().getContentRaw().split(" ");

        if(message.length < 4)
            throw new CommandSyntaxErrorException("!tb tournament invite [team name]",textChannel);

        if(getTournamentUser(e.getAuthor()).getTeam() == null)
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(getTournamentUser(e.getAuthor()).getTeam().getLeader() != getTournamentUser(e.getAuthor()))
            throw new TeamException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        if(getTournamentUser(e.getAuthor()).getTeam().getTournament() == null)
            throw new TournamentException(NOT_IN_TOURNAMENT_EXCEPTION,textChannel);

        Tournament tournament = getTournamentUser(e.getAuthor()).getTeam().getTournament();

        if(tournament.getControllerTeam() != getTournamentUser(e.getAuthor()).getTeam())
            throw new TournamentException(NOT_LEADER_OF_CONTROLLER_TEAM_EXCEPTION, textChannel);

        StringBuilder sb = new StringBuilder();
        for(int i = 3; i<message.length; i++) {
            sb.append(message[i]);
            if(i != message.length-1)
                sb.append(" ");
        }

        Team teamToInvite = getTeamByName(sb.toString());

        if(teamToInvite == null)
            throw new TeamException(TEAM_DOESNT_EXIST, textChannel);

        teamToInvite.getLeader().getDiscordUser().openPrivateChannel().queue(channel -> {
            channel.sendMessage("The team **"+tournament.getControllerTeam().getName()+" has challenged your team in the tournament "+tournament.getTournamentBuilder().getTournamentName()+". " +
                    "If you accept the challenge, react to this message with ✅, and if you decline, react to this message with ❌.").queue(new Consumer<Message>() {
                @Override
                public void accept(Message t) {
                    t.addReaction("✅");
                    t.addReaction("❌");
                    BiMap<Message, Tournament> map = HashBiMap.create();
                    map.put(t, tournament);
                    tournamentInvitations.add(map);
                }
            });
        });

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("✅ Successfully invited team **"+teamToInvite.getName()+"** ✅");

        textChannel.sendMessage(eb.build()).queue();
    }

    private void kickTournament(GuildMessageReceivedEvent e) throws TeamException, TournamentException {

        TextChannel textChannel = e.getChannel();
        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(tournamentUser.getTeam().getLeader() != tournamentUser)
            throw new TournamentException(NOT_TEAM_LEADER_EXCEPTION, textChannel);

        if(!Checks.hasTournament(tournamentUser))
            throw new TournamentException(NOT_IN_TOURNAMENT_EXCEPTION, textChannel);

        if(tournamentUser.getTeam().getTournament().getControllerTeam() != tournamentUser.getTeam())
            throw new TournamentException(NOT_LEADER_OF_CONTROLLER_TEAM_EXCEPTION, textChannel);

        if(tournamentUser.getTeam().getTournament().getOpponentTeam() == null)
            throw new TournamentException(NO_OPPONENT_TEAM_IN_TOURNAMENT_EXCEPTION,textChannel);

        Tournament tournament = tournamentUser.getTeam().getTournament();

        tournament.getOpponentTeam().getLeader().getDiscordUser().openPrivateChannel().queue(channel -> {
            channel.sendMessage(":wave: Your team has been kicked off the tournament **"+tournament.getTournamentBuilder().getTournamentName()+"**.");
        });

        tournament.removeOpponentTeam();

        EmbedBuilder eb= new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":white_check_mark: Successfully kicked opponent team off the tournament. :white_check_mark:");

        textChannel.sendMessage(eb.build()).queue();

    }

    private void settingsTournament(GuildMessageReceivedEvent e) throws TeamException, TournamentException, CommandSyntaxErrorException {
        TextChannel textChannel = e.getChannel();
        TournamentUser tournamentUser = getTournamentUser(e.getAuthor());
        String[] message = e.getMessage().getContentRaw().split(" ");
        boolean isHelped = false;

        if(message.length < 4)
            throw new CommandSyntaxErrorException("!tb tournament settings [help/setting to modify] [new setting]",textChannel);
        EmbedBuilder eb = new EmbedBuilder();
        switch(message[3].toLowerCase()) {
            case "help":

                eb.setColor(Color.BLUE)
                        .setTitle(":tools:  TOURNAMENT SETTINGS COMMANDS :tools:")
                        .addField("!tb tournament settings map [map_type]", "Sets the tournament map.\nSupported maps :" +
                                "SUMMONERS_RIFT, TWISTED_TREELINE, HOWLING_ABYSS.", false)
                        .addField("!tb tournament settings pick [pick_type]", "Sets the tournament picking type.\nSupported picking types: " +
                                "BLIND_PICK, DRAFT_PICK, ALL_RANDOM, TOURNAMENT_DRAFT", false)
                        .addField("!tb tournament settings spectate [spectate_type]","Sets the spectating type.\nSupported spectating types: " +
                                "NONE, LOBBYONLY, ALL", false)
                        .addField("!tb tournament settings bestof [amount]", "Sets the tournament winning condition, so it is a best of <amount>.\nSupported amounts: " +
                                "1, 3, 5, 7", false);
                isHelped = true;
                break;
            case "info":
                eb.setColor(Color.BLUE)
                        .setTitle("Try using **!tb tournament info** instead.");
                isHelped = true;
                break;
        }

        if(isHelped) {
            textChannel.sendMessage(eb.build()).queue();
            return;
        }

        if(!Checks.hasTeam(tournamentUser))
            throw new TeamException(NOT_IN_TEAM_EXCEPTION, textChannel);

        if(!Checks.isTeamLeader(tournamentUser))
            throw new TeamException(NOT_TEAM_LEADER_EXCEPTION,textChannel);

        if(!Checks.hasTournament(tournamentUser))
            throw new TournamentException(NOT_IN_TOURNAMENT_EXCEPTION, textChannel);

        if(!Checks.canTeamControlTournament(tournamentUser))
            throw new TournamentException(NOT_LEADER_OF_CONTROLLER_TEAM_EXCEPTION, textChannel);

        TournamentBuilder tb = tournamentUser.getTeam().getTournament().getTournamentBuilder();
        eb.setColor(Color.GREEN);

        switch(message[3].toLowerCase()) {
            case "map":
                if(message.length != 5)
                    throw new CommandSyntaxErrorException("!tb tournament settings map [map type]",textChannel);

                String map = message[4].toUpperCase();
                if(map.equals("SUMMONERS_RIFT") || map.equals("RIFT") || map.equals("SUMMONERS")) {
                    tb.setMapType("SUMMONERS_RIFT");
                    eb.setTitle("✅ Successfully set the map to Summoner's Rift ✅");
                } else if((map.equals("TWISTED_TREELINE")  || map.equals("TWISTED") || map.equals("TREELINE")) && tb.getTeamSize() <= 3) {
                    tb.setMapType("TWISTED_TREELINE");
                    eb.setTitle("✅ Successfully set the map to Twisted Treeline ✅");
                } else if (map.equals("HOWLING_ABYSS") || map.equals("HOWLING") || map.equals("ABYSS")) {
                    tb.setMapType("HOWLING_ABYSS");
                    eb.setTitle("✅ Successfully set the map to Howling Abyss ✅");
                } else {
                    throw new TournamentException("Either you put a wrong map name or you tried setting the map to Twisted Treeline when the time size is over 3...\n" +
                            "Supported maps: SUMMONERS_RIFT, TWISTED_TREELINE, HOWLING_ABYSS", textChannel);
                }
                break;

            case "pick":
                if(message.length != 5)
                    throw new CommandSyntaxErrorException("!tb tournament settings pick [pick type]",textChannel);

                String pick = message[4].toUpperCase();
                if(pick.equals("BLIND_PICK") || pick.equals("BLIND")) {
                    tb.setPickType("BLIND_PICK");
                    eb.setTitle("✅ Successfully set the pick type to Blind Pick ✅");
                } else if(pick.equals("DRAFT_PICK") || pick.equals("DRAFT")) {
                    tb.setPickType("DRAFT_PICK");
                    eb.setTitle("✅ Successfully set the picking type to Draft Pick ✅");
                } else if (pick.equals("ALL_RANDOM") || pick.equals("RANDOM")) {
                    tb.setPickType("ALL_RANDOM");
                    eb.setTitle("✅ Successfully set the picking type to All Random ✅");
                } else if (pick.equals("TOURNAMENT_DRAFT") || pick.equals("TOURNAMENT")) {
                    tb.setPickType("TOURNAMENT_DRAFT");
                    eb.setTitle("✅ Successfully set the picking type to Tournament Draft ✅");
                } else {
                    throw new TournamentException("You tried setting an unknown picking type...\nSupported pick types: BLIND_PICK, DRAFT_PICK, ALL_RANDOM, TOURNAMENT_DRAFT",textChannel);
                }
                break;
            case "spectate":
                if(message.length != 5)
                    throw new CommandSyntaxErrorException("!tb tournament settings pick [pick type]",textChannel);

                String spectate = message[4].toUpperCase();
                if(spectate.equals("LOBBYONLY") || spectate.equals("LOBBY")) {
                    tb.setSpectateMode("LOBBYONLY");
                    eb.setTitle("✅ Successfully set the spectating type to lobby only ✅");
                } else if(spectate.equals("NONE")) {
                    tb.setSpectateMode("NONE");
                    eb.setTitle("✅ Successfully set the spectating type to none ✅");
                } else if (spectate.equals("ALL")) {
                    tb.setSpectateMode("ALL");
                    eb.setTitle("✅ Successfully set the spectating type to all ✅");
                }  else {
                    throw new TournamentException("You tried setting an unknown spectating type...\nSupported spectating types: NONE, LOBBYONLY, ALL", textChannel);
                }
                break;
            case "bestof":
                if(message.length != 5)
                    throw new CommandSyntaxErrorException("!tb tournament settings bestof [amount]",textChannel);
                if(!message[4].matches("[0-9]+"))
                    throw new CommandSyntaxErrorException("!tb tournament settings bestof [amount]",textChannel);

                int bestof = Integer.parseInt(message[4]);
                if(bestof == 1) {
                    tb.setBestOf(1);
                    eb.setTitle("✅ Successfully set the tournament to a best of 1 ✅");
                } else if(bestof == 3) {
                    tb.setBestOf(3);
                    eb.setTitle("✅ Successfully set the tournament to a best of 3 ✅");
                } else if (bestof == 5) {
                    tb.setBestOf(5);
                    eb.setTitle("✅ Successfully set the tournament to a best of 5 ✅");
                } else if (bestof == 7) {
                    tb.setBestOf(7);
                    eb.setTitle("✅ Successfully set the tournament to a best of 7 ✅");
                }
                else {
                    throw new TournamentException("You can only make the tournament a best of 1, 3, 5 or 7. ", textChannel);
                }
                break;
            default:
                throw new CommandSyntaxErrorException("!tb tournament settings [help/setting to modify] [setting]",textChannel);

        }

        textChannel.sendMessage(eb.build()).queue();

    }

    private Tournament getTournamentFromId(String id) {
        for(int i = 0; i<tournaments.size(); i++) {
            if(tournaments.get(i).getTournamentId().equals(id))
                return tournaments.get(i);
        }
        return null;
    }

    private Team getTeamByName(String teamName) {
        for(int i = 0; i<teams.size(); i++) {
            if(teams.get(i).getName().toLowerCase().equals(teamName.toLowerCase()))
                return teams.get(i);
        }
        return null;
    }

    private TournamentUser getTournamentUser(User user) {
        for(int i = 0;i<tournamentUsers.size(); i++){
            if(tournamentUsers.get(i).getDiscordUser() == user)
                return tournamentUsers.get(i);
        }
        return null;
    }

    private String getRegionApi(String region) {
        String platform;
        switch(region.toLowerCase()) {
            case "br":
                platform = PLATFORM_BR;
                break;
            case "eune":
                platform = PLATFORM_EUNE;
                break;
            case "euw":
                platform = PLATFORM_EUW;
                break;
            case "jp":
                platform = PLATFORM_JP;
                break;
            case "kr":
                platform = PLATFORM_KR;
                break;
            case "lan":
                platform = PLATFORM_LAN;
                break;
            case "las":
                platform = PLATFORM_LAS;
                break;
            case "na":
                platform = PLATFORM_NA;
                break;
            case "oce":
                platform = PLATFORM_OCE;
                break;
            case "tr":
                platform = PLATFORM_TR;
                break;
            case "ru":
                platform = PLATFORM_RU;
                break;
            case "pbe":
                platform = PLATFORM_PBE;
                break;
            default:
                platform = null;
        }
        return platform;
    }

}
