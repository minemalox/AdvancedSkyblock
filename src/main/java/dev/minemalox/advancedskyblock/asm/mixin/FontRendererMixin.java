package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.ChromaManager;
import dev.minemalox.advancedskyblock.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(FontRenderer.class)
public class FontRendererMixin {

    @Inject(method = "renderChar(CZ)F", at = @At(value = "INVOKE", target = "java/lang/String.indexOf(I)I", shift = At.Shift.AFTER))
    private void renderChar(char ch, boolean italic, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (ChromaManager.isColoringTextChroma()) {
            FontRenderer fontRenderer = ((FontRenderer) MinecraftReflection.FontRenderer.getFontRenderer());

            float[] HSB = Color.RGBtoHSB((int) (fontRenderer.red * 255), (int) (fontRenderer.green * 255), (int) (fontRenderer.blue * 255), null);

            float chromaWidth = (AdvancedSkyblock.getInstance().getUtils().denormalizeScale(AdvancedSkyblock.getInstance().getConfigValues().getChromaFadeWidth(), 1, 42, 1) / 360) % 1F;
            int newColorRGB = Color.HSBtoRGB(HSB[0] + chromaWidth, HSB[1], HSB[2]);

            fontRenderer.red = (float) (newColorRGB >> 16 & 255) / 255.0F;
            fontRenderer.green = (float) (newColorRGB >> 8 & 255) / 255.0F;
            fontRenderer.blue = (float) (newColorRGB & 255) / 255.0F;

            // Swap blue & green because they are swapped in FontRenderer's color model.
            GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha);
        }
    }

}
