package net.bteuk.uk121;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.gen.GeneratorOptions;

import java.util.Properties;

public interface WorldTypeGenerator {
    // return null for no handling, otherwise return GeneratorOptions for your level type
    Event<WorldTypeGenerator> GET_GENERATOR_OPTIONS = EventFactory.createArrayBacked(WorldTypeGenerator.class,
            (listeners) -> (levelType,seed,properties) -> {
                for (WorldTypeGenerator listener : listeners) {
                    GeneratorOptions result = listener.interact(levelType,seed,properties);

                    if(result != null) {
                        return result;
                    }
                }

                return null;
            });

    GeneratorOptions interact(String levelType, long seed, Properties properties);

}
