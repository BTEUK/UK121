package net.bteuk.uk121.world.gen.surfacedecoration.geojson.geometry;

import lombok.Data;
import lombok.NonNull;
import net.bteuk.uk121.world.gen.surfacedecoration.BoundingBox;
import net.bteuk.uk121.world.gen.surfacedecoration.geojson.Geometry;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.projection.ProjectionFunction;

/**
 * @author DaPorkchop_
 */
@Data
public final class Point implements Geometry {
    protected final double lon;
    protected final double lat;

    @Override
    public Point project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        double[] proj = projection.project(this.lon, this.lat);
        return new Point(proj[0], proj[1]);
    }

    @Override
    public BoundingBox bounds()
    {
        return BoundingBox.of(this.lon, this.lon, this.lat, this.lat);
    }
}
