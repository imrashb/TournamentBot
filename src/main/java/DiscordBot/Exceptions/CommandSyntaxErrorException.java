package DiscordBot.Exceptions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class CommandSyntaxErrorException extends Exception{

    public CommandSyntaxErrorException(String syntax, TextChannel textChannel) {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(":exclamation: SYNTAX ERROR :exclamation:")
                .addField(syntax, "This is the correct usage.", false);
        textChannel.sendMessage(eb.build()).queue();
    }

}
