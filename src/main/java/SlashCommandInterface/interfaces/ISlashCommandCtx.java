package SlashCommandInterface.interfaces;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

public interface ISlashCommandCtx {

    /**
     * Returns the guild of the event
     *
     * @return The guild the event was triggered in
     */
    default Guild getGuild() {
        return this.getEvent().getGuild();
    }

    /**
     * Returns the event
     *
     * @return The event
     * @see SlashCommandInteractionEvent
     */
    SlashCommandInteractionEvent getEvent();

    /**
     * Returns the channel of the event
     *
     * @return The channel the event was triggered in
     */
    default Channel getChannel() {
        return this.getEvent().getChannel();
    }

    /**
     * Returns the channel as a text channel
     *
     * @return The text channel the event was triggered in
     */
    default TextChannel getTextChannel() {
        return this.getEvent().getChannel().asTextChannel();
    }

    /**
     * Returns the command name
     *
     * @return The command name
     */
    default String getName() {
        return this.getEvent().getName();
    }

    /**
     * Returns the user invoking the command
     *
     * @return The user invoking the command
     */
    default User getUser() {
        return this.getEvent().getUser();
    }

    /**
     * Returns the member invoking the command
     *
     * @return The member invoking the command
     */
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
