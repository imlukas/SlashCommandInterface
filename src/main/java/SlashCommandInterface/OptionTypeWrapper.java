package SlashCommandInterface;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

public enum OptionTypeWrapper {
    STRING(OptionMapping::getAsString),
    INTEGER(OptionMapping::getAsInt),
    BOOLEAN(OptionMapping::getAsBoolean),
    USER(OptionMapping::getAsUser),
    CHANNEL(OptionMapping::getAsChannel),
    ROLE(OptionMapping::getAsRole),
    MENTIONABLE(OptionMapping::getAsMentionable),
    NUMBER(OptionMapping::getAsDouble);

    private final Function<OptionMapping, Object> function;

    OptionTypeWrapper(Function<OptionMapping, Object> function) {
        this.function = function;
    }

    public static OptionTypeWrapper fromType(OptionType type) {
        return valueOf(type.name());
    }

    public Object get(OptionMapping mapping) {
        return function.apply(mapping);
    }
}
