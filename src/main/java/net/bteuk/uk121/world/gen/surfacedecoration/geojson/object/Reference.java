package net.bteuk.uk121.world.gen.surfacedecoration.geojson.object;

import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.GeoJsonObject;

/**
 * Non-standard GeoJSON object: represents a reference to another URL containing a GeoJSON object that this object should be substituted with.
 *
 * @author DaPorkchop_
 */
@Data
public final class Reference implements GeoJsonObject {
    @NonNull
    protected final String location;
}
