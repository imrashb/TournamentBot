package DiscordBot.Exceptions;

public class ExceptionConstants {
    public static final String UNKNOWN_REGION_EXCEPTION = "This region doesn't exist!\nTry again with one of these regions: BR, EUNE, EUW, JP, KR, LAN, LAS, NA, OCE, TR, RU, PBE.";
    public static final String UNKNOWN_SUMMONER_EXCEPTION = "I can't find this League of Legends account!\nMake sure you selected the right region and you typed the correct summoner name.";
    public static final String SUMMONER_NOT_LINKED_EXCEPTION = "You need to link your League of Legends account to your Discord account before doing that!\nTo do that, use this command > **!tb summoner link [region] [user_name]**";
    public static final String ALREADY_IN_TEAM_EXCEPTION = "You already have a team! If you want to create a new team, try leaving your current team.\nTo do that, use this command > **!tb team leave**";
    public static final String NOT_TEAM_LEADER_EXCEPTION = "Only the team leader has access to that command.";
    public static final String OVER_TEAM_LIMIT_EXCEPTION = "You already have 5 team members.";
    public static final String NOT_IN_TEAM_EXCEPTION = "You aren't in any team.\n To create a team, use this command > **!tb team create [name]**";
    public static final String BAD_TEAM_NAME_EXCEPTION = "The team name can't have special characters or numbers. Make sure it is only letters.";
    public static final String TEAM_NAME_LENGTH_EXCEPTION = "The team name should be between 3 and 20 characters.";
    public static final String TEAM_NAME_ALREADY_TAKEN_EXCEPTION = "This team name is already taken. Try another one.";
    public static final String INVITE_BOT_TO_TEAM_EXCEPTION = "You can't invite a bot to your team!";
    public static final String LEADER_LEAVING_TEAM_EXCEPTION = "You can't leave your team if you are the leader. Instead, try disbanding your team.\nTo do that, use this command > **!tb team disband**";
    public static final String TRY_KICKING_HIMSELF_EXCEPTION = "You can't kick yourself off your own team.";
    public static final String TOURNAMENT_NAME_LENGTH_EXCEPTION = "The tournament name has to be between 3 and 30 characters.";
    public static final String NOT_IN_TOURNAMENT_EXCEPTION = "You have to create a tournament before doing that. To do that, use this command > **!tb tournament create [name]**";
    public static final String TOURNAMENT_ID_DOESNT_EXIST_EXCEPTION = "This tournament ID doesn't exist. Make sure it is the series of 30 characters in parentheses next to the tournament name.";
    public static final String NOT_LEADER_OF_CONTROLLER_TEAM_EXCEPTION = "You have to be the tournament host to do that.";
    public static final String TEAM_DOESNT_EXIST = "This team doesn't exist. Make sure you didn't make any mistakes while typing it.";
    public static final String NO_OPPONENT_TEAM_IN_TOURNAMENT_EXCEPTION = "There is no other team in your tournament. To invite another team, use this command > **!tb tournament invite [team name]**";

}
