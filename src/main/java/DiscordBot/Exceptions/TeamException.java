package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class TeamException extends Exception{

    public TeamException(String message, MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: TEAM ERROR :exclamation:")
                .addField("There was a problem...", message, false);
        channel.sendMessage(eb.build()).queue();
    }

}
