package SlashCommandInterface.interfaces;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public interface ISlashCommand {


    String getName();

    default DefaultMemberPermissions getPermission() {
        return DefaultMemberPermissions.ENABLED;
    }

    String getDescription();


}
