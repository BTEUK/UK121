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
public final class Polygon implements Geometry {
    protected final LineString outerRing;
    protected final LineString[] innerRings;

    public Polygon(@NonNull LineString outerRing, @NonNull LineString[] innerRings) {
     //   checkArg(outerRing.isLinearRing(), "outerRing is not a linear ring!");
        for (int i = 0; i < innerRings.length; i++) {
         //   checkArg(innerRings[i].isLinearRing(), "innerRings[%d] is not a linear ring!", i);
        }
        this.outerRing = outerRing;
        this.innerRings = innerRings;
    }

    @Override
    public Polygon project(@NonNull ProjectionFunction projection) throws OutOfProjectionBoundsException {
        LineString outerRing = this.outerRing.project(projection);
        LineString[] innerRings = this.innerRings.clone();
        for (int i = 0; i < innerRings.length; i++) {
            innerRings[i] = innerRings[i].project(projection);
        }
        return new Polygon(outerRing, innerRings);
    }

    @Override
    public BoundingBox bounds() {
        return this.outerRing.bounds();
    }
}
