package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class DelayException extends Exception {
    public DelayException(TextChannel textChannel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: DELAY ERROR :exclamation:")
                .addField("You have to wait atleast 3 seconds inbetween commands that send a request to the Riot API.",
                        "This is because there is a maximum number of requests per minute that the API allows.", false);
        textChannel.sendMessage(eb.build()).queue();
    }
}
