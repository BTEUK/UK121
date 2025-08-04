package net.bteuk.uk121.mod.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Unnecessary
@Mixin(TitleScreen.class)
public class UK121Mixin {
	@Inject(method = "init()V", at = @At("HEAD"))
	private void onInit(CallbackInfo info) {
		//UK121.LOGGER.info("UK121 test message!");
	}
}
