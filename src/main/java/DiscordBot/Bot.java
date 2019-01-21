package DiscordBot;

import DiscordBot.Events.ChatCommands;
import RiotAPI.Match.Match;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

import static DiscordBot.BotConfig.BOT_TOKEN;
import static RiotAPI.ApiConstants.PLATFORM_NA;

public class Bot {

    public static void main(String args[]) {

        try {
            JDA jda = new JDABuilder(BOT_TOKEN)
                    .addEventListeners(new ChatCommands())
                    .build();
        } catch(LoginException e) {
            System.out.println("Couldn't login to discord bot");

        }

    }

}
