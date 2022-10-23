package SlashCommandInterface;

import SlashCommandInterface.annotations.Option;
import SlashCommandInterface.annotations.SlashCommand;
import SlashCommandInterface.annotations.SlashCommandHandler;
import SlashCommandInterface.annotations.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class SlashCommandManager {

    private final List<ISlashCommand> commands = new ArrayList<>();

    public SlashCommandManager() {
        // comamnds.add(new BanCommand());
    }

    public void registerCommand(ISlashCommand command) {
        commands.add(command);
    }

    public void registerCommands(List<ISlashCommand> commands) {
        this.commands.addAll(commands);
    }

    // registers all the commands, according to the annotations
    public void init(Guild guild, CommandType type) {
        List<CommandData> commands = new ArrayList<>();
        for (ISlashCommand command : getCommands()) {

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
                            subcommandData.addOption(option.type(), option.name(), option.description(), option.required());
                        }
                    }

                    commandData.addSubcommands(subcommandData);
                    optionAnnotations.clear();
                    continue;
                }
                // Handle main command method's options
                for (Parameter parameter : parameters) {
                    if (parameter.isAnnotationPresent(Option.class)) {
                        optionAnnotations.add(parameter.getAnnotation(Option.class));
                    }

                }
            }
            // slash command creation
            if (!(optionAnnotations.isEmpty())) {
                for (Option option : optionAnnotations) {
                    commandData.addOption(option.type(), option.name(), option.description(), option.required());
                }
            }
            // handles SlashCommand annotation
            if (clazz.isAnnotationPresent(SlashCommand.class)) {
                CommandType commandType = clazz.getAnnotation(SlashCommand.class).type();
                updateCommand(guild, commandData, commandType);
                continue;
            }
            commands.add(commandData.setDefaultPermissions(command.getPermission()));

        }
        if (type == CommandType.GUILD) {
            updateCommands(guild, commands, type);
        } else {
            updateCommands(guild, commands, type);
        }
    }

    // updates the commands on the guild

    public void updateCommand(Guild guild, CommandData command, CommandType type) {
        if (type == CommandType.GUILD) {
            guild.updateCommands().addCommands(command).queue();
        } else {
            guild.upsertCommand(command).queue();
        }
    }

    public void updateCommands(Guild guild, List<CommandData> commands, CommandType type) {
        if (type == CommandType.GUILD) {
            guild.updateCommands()
                    .addCommands(commands)
                    .queue();
            return;
        }
        for (CommandData command : commands) {
            guild.upsertCommand(command)
                    .queue();
        }
    }


    public List<ISlashCommand> getCommands() {
        return commands;
    }

    @Nullable
    public ISlashCommand getCommand(String search) {

        String searchLower = search.toLowerCase();

        for (ISlashCommand cmd : this.commands) {
            if (cmd.getName().equalsIgnoreCase(searchLower)) {
                return cmd;
            }
        }

        return null;

    }

    // runs the command
    public void run(ISlashCommand instance, Method method, SlashCommandContext context, Map<String, Object> options) {

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

    // handles the slash command parameters
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

        event.deferReply().queue();

    }


}
