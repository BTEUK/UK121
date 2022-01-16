package net.bteuk.uk121.world.gen.surfacedecoration.geojson.geometry;

import com.google.common.collect.Iterators;
import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.BoundingBox;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;

import java.util.Iterator;

import static java.lang.Math.*;

/**
 * @author DaPorkchop_
 */
@Data
public final class MultiPoint implements Geometry, Iterable<Point> {
    @NonNull
    protected final Point[] points;

    @Override
    public Iterator<Point> iterator() {
        return Iterators.forArray(this.points);
    }

    @Override
    public MultiPoint project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        Point[] out = this.points.clone();
        for (int i = 0; i < out.length; i++) {
            out[i] = out[i].project(projection);
        }
        return new MultiPoint(out);
    }

    @Override
    public BoundingBox bounds() {
        if (this.points.length == 0) {
            return null;
        }

        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        for (Point point : this.points) {
            minLon = min(minLon, point.lon);
            maxLon = max(maxLon, point.lon);
            minLat = min(minLat, point.lat);
            maxLat = max(maxLat, point.lat);
        }
        return BoundingBox.of(minLon, maxLon, minLat, maxLat);
    }
}
