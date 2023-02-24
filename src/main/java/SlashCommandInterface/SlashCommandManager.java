package SlashCommandInterface;

import SlashCommandInterface.constants.CommandType;
import SlashCommandInterface.interfaces.ISlashCommand;
import SlashCommandInterface.wrapper.OptionTypeWrapper;
import me.imlukas.slashcommands.annotations.Option;
import me.imlukas.slashcommands.annotations.SlashCommand;
import me.imlukas.slashcommands.annotations.SlashCommandHandler;
import me.imlukas.slashcommands.annotations.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandManager {

    private final List<ISlashCommand> slashCommands = new ArrayList<>();

    public void registerCommand(ISlashCommand... commands) {
        for (ISlashCommand command : commands) {
            registerCommand(command);
        }
    }

    public void registerCommand(ISlashCommand command) {
        slashCommands.add(command);
    }

    public void registerCommands(List<ISlashCommand> commands) {
        slashCommands.addAll(commands);
    }

    /**
     * Method responsible for setting up
     * all the registered slash commands.
     * This method will set up both subcommands and commands as
     * well as their options, if any.
     *
     * @param guild the guild to set up the commands for
     * @param type  the type of commands to set up {@link CommandType}
     */
    public void init(Guild guild, CommandType type) {
        List<CommandData> guildCommands = new ArrayList<>();
        List<CommandData> globalCommands = new ArrayList<>();

        for (ISlashCommand command : getSlashCommands()) {

            Class<?> clazz = command.getClass();

            Method[] methods = clazz.getMethods();

            List<Option> optionAnnotations = new ArrayList<>();

            SlashCommandData commandData = Commands.slash(command.getName(), command.getDescription());

            for (Method method : methods) {
                Parameter[] parameters = method.getParameters();
                // handle subcommands
                if (method.isAnnotationPresent(SubCommand.class)) {
                    SubCommand subCommand = method.getAnnotation(SubCommand.class);
                    SubcommandData subcommandData = new SubcommandData(subCommand.name(), subCommand.description());
                    for (Parameter parameter : parameters) {
                        if (parameter.isAnnotationPresent(Option.class)) {
                            optionAnnotations.add(parameter.getAnnotation(Option.class));
                        }
                    }
                    if (!(optionAnnotations.isEmpty())) {
                        for (Option option : optionAnnotations) {
                            subcommandData.addOption(option.type(), option.name(), option.description(), option.required(), option.autoComplete());
                        }
                    }

                    commandData.addSubcommands(subcommandData);
                    optionAnnotations.clear();
                    continue;
                }
                // Handle main command method's options
                if (method.isAnnotationPresent(SlashCommandHandler.class)) {
                    for (Parameter parameter : parameters) {
                        if (parameter.isAnnotationPresent(Option.class)) {
                            optionAnnotations.add(parameter.getAnnotation(Option.class));
                        }
                    }
                }

            }
            // slash command creation
            if (!(optionAnnotations.isEmpty())) {
                for (Option option : optionAnnotations) {
                    commandData.addOption(option.type(), option.name(), option.description(), option.required(), option.autoComplete());
                }
            }

            // Handle command type
            CommandType commandType = type;
            if (clazz.isAnnotationPresent(SlashCommand.class)) {
                commandType = clazz.getAnnotation(SlashCommand.class).type();
            }
            if (commandType == CommandType.GUILD) {
                guildCommands.add(commandData.setDefaultPermissions(command.getPermission()));
                continue;
            }
            globalCommands.add(commandData.setDefaultPermissions(command.getPermission()));
        }
        updateCommands(guildCommands, globalCommands, guild);
    }


    /**
     * Method responsible for updating the slash commands
     * in the guild.
     *
     * @param guildCommands  commands to be updated on the guild.
     * @param globalCommands commands to be updated globally.
     * @param guild          guild to update the commands on.
     */
    public void updateCommands(List<CommandData> guildCommands, List<CommandData> globalCommands, Guild guild) {

        guild.updateCommands().addCommands(guildCommands).queue();

        for (CommandData commandData : globalCommands) {
            guild.upsertCommand(commandData).queue();
        }

    }

    /**
     * Returns a list with all the registered slash commands.
     * This list can be empty if no slash commands have been registered.
     *
     * @return all the registered slash commands
     */
    public List<ISlashCommand> getSlashCommands() {
        return slashCommands;
    }

    /**
     * Gets a command based on the name.
     *
     * @param commandName name of the command
     * @return Desired SlashCommand or null if not found
     */
    @Nullable
    public ISlashCommand getCommand(String commandName) {

        String searchLower = commandName.toLowerCase();
        for (ISlashCommand cmd : getSlashCommands()) {
            if (cmd.getName().equalsIgnoreCase(searchLower)) {
                return cmd;
            }
        }

        return null;

    }


    /**
     * Method responsible for running the slashcommand
     * This method takes in consideration the arguments and options
     * of the slash command and executes the command / subcommand.
     *
     * @param instance instance of the slash command
     * @param method   method to be executed, this represents the command / subcommand
     * @param context  context of the slash command
     * @param options  options of the slash command if any
     */
    public void run(ISlashCommand instance, Method method, SlashCommandContext context, Map<String, Object> options) {

        // handle method arguments and options
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int index = 0; index < parameters.length; index++) {

            Parameter parameter = parameters[index];

            if (parameter.isAnnotationPresent(Option.class)) {
                args[index] = options.get(parameter.getAnnotation(Option.class).name());
            } else if (parameter.getType() == SlashCommandContext.class) {
                args[index] = context;
            }
        }

        try {
            method.invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the slash command interaction.
     * This method should be called on {@link SlashCommandInteractionEvent}
     * trigger.
     *
     * @param event the event {@link SlashCommandInteractionEvent}
     * @see SlashCommandInteractionEvent
     */
    public void handle(SlashCommandInteractionEvent event) {

        ISlashCommand command = getCommand(event.getName());

        if (command == null) {
            return;
        }

        SlashCommandContext context = new SlashCommandContext(event);
        Map<String, Object> options = new HashMap<>();

        event.getOptions().forEach(option -> options.put(option.getName(), OptionTypeWrapper.fromType(option.getType()).get(option)));

        Class<?> clazz = command.getClass();

        Method[] methods = clazz.getMethods();
        if (event.getSubcommandName() != null) {
            for (Method method : methods) {
                if (!(method.isAnnotationPresent(SubCommand.class))) {
                    continue;
                }

                SubCommand subCommand = method.getAnnotation(SubCommand.class);
                if (subCommand.name().equalsIgnoreCase(event.getSubcommandName())) {
                    run(command, method, context, options);
                    return;
                }
            }
        }
        for (Method method : methods) {
            if (method.isAnnotationPresent(SlashCommandHandler.class)) {
                run(command, method, context, options);
                break;
            }
        }
    }


}
