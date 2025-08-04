package net.bteuk.uk121.mod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bteuk.uk121.mod.UK121;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    private static final Identifier MINECRAFT_TITLE_TEXTURE = new Identifier("textures/gui/title/minecraft.png");
    private static final Identifier EDITION_TITLE_TEXTURE = new Identifier("textures/gui/title/edition.png");

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        int j = this.width / 2 - 137;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, UK121.TITLE_SCREEN);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float g = 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        if ((l & -67108864) != 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, MINECRAFT_TITLE_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, g);
            this.drawWithOutline(j, 30, (x, y) -> {
                this.drawTexture(matrices, x, y, 0, 0, 99, 44);
                this.drawTexture(matrices, x + 99, y, 129, 0, 27, 44);
                this.drawTexture(matrices, x + 99 + 26, y, 126, 0, 3, 44);
                this.drawTexture(matrices, x + 99 + 26 + 3, y, 99, 0, 26, 44);
                this.drawTexture(matrices, x + 155, y, 0, 45, 155, 44);
            });

            RenderSystem.setShaderTexture(0, EDITION_TITLE_TEXTURE);
            drawTexture(matrices, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);

            String string = "Minecraft " + SharedConstants.getGameVersion().getName();

            string = string + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType());


            if (this.client.isModded()) {
                string = string + I18n.translate("menu.modded", new Object[0]);
            }

            int copyrightTextWidth = this.textRenderer.getWidth("Copyright Mojang AB. Do not distribute!");
            int copyrightTextX = this.width - copyrightTextWidth - 2;

            drawStringWithShadow(matrices, this.textRenderer, string, 2, this.height - 10, 16777215 | l);
            drawStringWithShadow(matrices, this.textRenderer, "Copyright Mojang AB. Do not distribute!", copyrightTextX, this.height - 10, 16777215 | l);

            Iterator var12 = this.children().iterator();

            while (var12.hasNext()) {
                Element element = (Element) var12.next();
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget) element).setAlpha(g);
                }
            }

            super.render(matrices, mouseX, mouseY, delta);

        }
    }
}

