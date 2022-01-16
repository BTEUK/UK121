package net.bteuk.uk121.world.gen.surfacedecoration.geojson.object;

import com.google.common.collect.Iterators;
import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.GeoJsonObject;

import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Data
public final class FeatureCollection implements GeoJsonObject, Iterable<Feature> {
    @NonNull
    protected final Feature[] features;

    @Override
    public Iterator<Feature> iterator() {
        return Iterators.forArray(this.features);
    }
}
