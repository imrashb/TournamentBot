package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class UnexistingCommandException extends Exception{

    public UnexistingCommandException(TextChannel textChannel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: UNKNOWN COMMAND :exclamation:")
                .addField("This command doesn't exist", "To get information about all the possible commands, try **$tb help**", false);
        textChannel.sendMessage(eb.build()).queue();
    }

}
