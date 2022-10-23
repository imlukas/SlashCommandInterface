package SlashCommandInterface;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

public interface ISlashCommandContext {

    default Guild getGuild() {
        return this.getEvent().getGuild();
    }

    SlashCommandInteractionEvent getEvent();

    default Channel getChannel() {
        return this.getEvent().getChannel();
    }

    default TextChannel getTextChannel() {
        return this.getEvent().getChannel().asTextChannel();
    }

    default String getName() {
        return this.getEvent().getName();
    }

    default User getUser() {
        return this.getEvent().getUser();
    }

    default Member getMember() {
        return this.getEvent().getMember();
    }

    default JDA getJDA() {
        return this.getEvent().getJDA();
    }

    default ShardManager getShardManager() {
        return this.getJDA().getShardManager();
    }

    default User getSelfUser() {
        return this.getJDA().getSelfUser();
    }

    default Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

}
