package net.bteuk.uk121.mod;

import com.google.gson.Gson;
import net.bteuk.uk121.Projections.ModifiedAirocean;

public class TerraConstants {
    public static final ModifiedAirocean projection = new ModifiedAirocean();

    public static final Gson GSON = new Gson();

/*    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS, true)
            .configure(JsonReadFeature.ALLOW_TRAILING_COMMA, true)
            .addMixIn(Biome.class, BiomeDeserializeMixin.class)
            .addMixIn(BlockState.class, BlockStateDeserializeMixin.class)
            .build();
*/
    /**
     * Earth's circumference around the equator, in meters.
     */
    public static final double EARTH_CIRCUMFERENCE = 40075017;

    /**
     * Earth's circumference around the poles, in meters.
     */
    public static final double EARTH_POLAR_CIRCUMFERENCE = 40008000;

    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
}
