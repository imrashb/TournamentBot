package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class SummonerException extends Exception {

    public SummonerException(String message, TextChannel textChannel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: SUMMONER ERROR :exclamation:")
                .addField("There was a problem...", message, false);
        textChannel.sendMessage(eb.build()).queue();
    }

}
