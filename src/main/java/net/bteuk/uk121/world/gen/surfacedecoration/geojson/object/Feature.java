package net.bteuk.uk121.world.gen.surfacedecoration.geojson.object;

import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.GeoJsonObject;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.Geometry;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Data
public final class Feature implements GeoJsonObject {
    @NonNull
    protected final Geometry geometry;
    protected final Map<String, String> properties;
    protected final String id;
}
