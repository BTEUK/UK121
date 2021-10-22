package net.bteuk.uk121.mixin;

import net.bteuk.uk121.Config;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionType.class)
public class IncreaseWorldHeight {

    @Mutable
    @Shadow @Final private int minimumY;

    @Mutable
    @Shadow @Final private int height;

    @Mutable
    @Shadow @Final private int logicalHeight;

    @Inject(method = "<init>(Ljava/util/OptionalLong;ZZZZDZZZZZIIILnet/minecraft/world/biome/source/BiomeAccessType;Lnet/minecraft/util/Identifier;Lnet/minecraft/util/Identifier;F)V", at = @At("RETURN"))
    private void DimensionTypeEditor(CallbackInfo info) {
        Config config = new Config();
        config.load();

        this.minimumY = config.yMin;
        this.height = config.height;
        this.logicalHeight = config.height;
    }

}

