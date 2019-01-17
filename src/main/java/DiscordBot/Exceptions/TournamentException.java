package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;

public class TournamentException extends Exception {

    public TournamentException(String message, MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: TOURNAMENT ERROR :exclamation:")
                .addField("There was a problem...", message, false);
        channel.sendMessage(eb.build()).queue();
    }

}
