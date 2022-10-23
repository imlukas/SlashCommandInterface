package SlashCommandInterface.annotations;


import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Option {

    String name() default "";

    String description() default "";

    OptionType type() default OptionType.STRING;

    boolean required() default false;

}
