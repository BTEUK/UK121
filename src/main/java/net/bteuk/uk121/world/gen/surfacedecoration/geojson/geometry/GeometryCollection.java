package net.bteuk.uk121.world.gen.surfacedecoration.geojson.geometry;

import com.google.common.collect.Iterators;
import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.BoundingBox;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author DaPorkchop_
 */
@Data
public final class GeometryCollection implements Geometry, Iterable<Geometry> {
    @NonNull
    protected final Geometry[] geometries;

    @Override
    public Iterator<Geometry> iterator() {
        return Iterators.forArray(this.geometries);
    }

    @Override
    public Geometry project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Geometry[] out = this.geometries.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new GeometryCollection(out);
    }

    @Override
    public BoundingBox bounds() {
        return Arrays.stream(this.geometries).map(Geometry::bounds).reduce(BoundingBox::union).orElse(null);
    }
}
