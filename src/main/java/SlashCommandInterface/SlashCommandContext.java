package SlashCommandInterface;

import SlashCommandInterface.interfaces.ISlashCommandCtx;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandContext implements ISlashCommandCtx {
    private final SlashCommandInteractionEvent event;

    public SlashCommandContext(SlashCommandInteractionEvent event) {
        this.event = event;
    }

    @Override
    public Guild getGuild() {
        return this.getEvent().getGuild();
    }

    @Override
    public SlashCommandInteractionEvent getEvent() {
        return this.event;
    }
}
