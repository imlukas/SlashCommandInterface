 # SlashCommandInterface
Interface made to make implementing SlashCommands in JDA much easier to make.

I thought making SlashCommands was a bit of a hassle because of the amount of code it was necessary 
to manually add options, subcommands, and even the commands itself.

This interface is made with the use of Java Reflection.

You have two example commands attached on the project so you can have a look at how it works without needing to read the whole README.

<details><summary>Annotations</summary>

### Annotations

**@Option** (name, description, type, required, autocomplete) is a parameter annotation which defines the options of your Command and/or Subcommands. 
You can't have options on the main command if you have subcommands as they override each other.

> An option serves basically as an input for the user to use when using the command.

**Parameters**
> name: The name of the option, this is what the user will see when they use the command.
> 
> description: The descrip  tion of the option, this is what the user will see when they use the command.
> 
> type: This is an OptionType enum, this defines the type of the option. (STRING, INTEGER, BOOLEAN, USER, CHANNEL, ROLE, MENTIONABLE). By default it's STRING.
> 
> required: This defines if the option is required or not. By default, it's false.
> 
> autocomplete: This defines if the option should have autocomplete or not. By default, it's false. Autocomplete most be handled by you. See: [Interaction Wiki](https://jda.wiki/using-jda/interactions/).
 
**@SlashCommand** is a class annotation which is where you define if this slash command should be global or not. This annotation is **optional**

> If you use this annotation on a class, it's obligatory to define which type you want.
> This will also override the init method type you defined on your Listeners.
> This is useful to test individual commands

**@SlashCommandHandler** is a method annotation that defines the "main" method of your command. 
This annotation is required in order for the command to work, but it's optional if you have subcommands.<br> 

**@SubCommand** (name, description) is a method annotation that defines your command's SubCommands.

> Neither the name nor the description can be empty.

</details>

<details><summary>Classes</summary>

**ISlashCommand** is the necessary interface for your command to be seen as a slash command. you'll need to implement both getName() and getDescription() methods.
There's an optional method for permissions, getPermissions() by there's no restriction.

**SlashCommandContext** is a class used to get context when writing the slash command's commands and subcommands.
This class is obligatory in order for you to have access to certain things like guild, user and others.
</details>

### Your command class should implement ISlashCommand

Example code: First of all let's create our command, this a basic welcome slash command using this utility.

```java

@SlashCommand(type = CommandType.GUILD) // this defines this command as a guild command.
public class WelcomeCommand implements ISlashCommand { 
    
    @SlashCommandHandler // this defines the main method of the command.
    private void run(SlashCommandContext context) {
        context.getEvent().reply("This is my command's main method!");
    }
    
    @SubCommand(name = "name", description = "person to welcome") // this defines a subcommand.
    private void welcome(@Option(name = "name", description = "insert your name", required = true) String name,
                         SlashCommandContext context) {
                         
        context.getEvent().reply("Hello, " + name);
    }
    
    @Override
    public String getName() {
        return "welcome";
    }

    @Override
    public String getDescription() {
        return "welcome someone";
    }
}
    
```

Now you need to register your commands, this can be done before the init method or on the bot initialization method.

> registerCommand() doesn't register the command as a guild or global command.

```java
public class Bot {
    private SlashCommandManager slashCommandManager;

    public Bot() throws LoginException {
        // ... bot builder
        slashCommandManager = new SlashCommandManager();
        slashCommandManager.registerCommand(new WelcomeCommand());
        /**
         * You can also register multiple commands at once
         * slashCommandManager.registerCommand(new WelcomeCommand(), new AnotherCommand());
         * slashCommandManager.registerCommand(List.of(new WelcomeCommand(), new AnotherCommand()));
         */
    }

    public static void main(String[] args) {
        try {
            Bot bot = new Bot();
        } catch (LoginException e) {
            System.out.println("Invalid token");
        }
    }
}
    
```

Finishing up - **It's recommended you initalize all your command at once on the guild initialization / join listeners.**<br> 

> the init method initializes commands based on the parameter you pass in
> (guild or global) but @SlashCommand annotation on a command overrides the type.
> 
> Note: Only registered commands will be initialized.
```java
public class GuildJoinReadyListener extends ListenerAdapter {

    private final SlashCommandManager slashCommandManager = new SlashCommandManager();
    
    // You need to specify the command type
    
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        slashCommandManager.init(event.getGuild(), CommandType.GUILD);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        slashCommandManager.init(event.getGuild(), CommandType.GUILD);
    }
}

```
If you have any ideas or suggestions, feel free to open an issue or a pull request. Thanks.


