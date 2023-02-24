package SlashCommandInterface.wrapper;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.function.Function;

/**
 * Wrapper for {@link OptionType} to get the value of an {@link OptionMapping}
 */
public enum OptionTypeWrapper {
    STRING(OptionMapping::getAsString),
    INTEGER(OptionMapping::getAsInt),
    BOOLEAN(OptionMapping::getAsBoolean),
    USER(OptionMapping::getAsUser),
    CHANNEL(OptionMapping::getAsChannel),
    ROLE(OptionMapping::getAsRole),
    MENTIONABLE(OptionMapping::getAsMentionable),
    NUMBER(OptionMapping::getAsDouble),
    ATTACHMENT(OptionMapping::getAsAttachment);

    private final Function<OptionMapping, Object> function;

    OptionTypeWrapper(Function<OptionMapping, Object> function) {
        this.function = function;
    }

    public Object get(OptionMapping mapping) {
        return function.apply(mapping);
    }

    public static OptionTypeWrapper fromType(OptionType type) {
        return valueOf(type.name());
    }
}
