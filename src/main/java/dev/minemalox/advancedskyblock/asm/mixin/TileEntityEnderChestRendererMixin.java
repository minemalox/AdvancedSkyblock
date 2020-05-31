package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Location;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(TileEntityEnderChestRenderer.class)
public class TileEntityEnderChestRendererMixin {

    private static final ResourceLocation BLANK_ENDERCHEST = new ResourceLocation("advancedskyblock", "enderchest.png");

    @Redirect(method = "render(Lnet/minecraft/tileentity/TileEntityEnderChest;DDDFIF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer.bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    public void bindTexture(TileEntityEnderChestRenderer chestRenderer, ResourceLocation location) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                (main.getUtils().getLocation() == Location.THE_END || main.getUtils().getLocation() == Location.DRAGONS_NEST)) {

            chestRenderer.bindTexture(BLANK_ENDERCHEST);
        } else {
            chestRenderer.bindTexture(location);
        }
    }

    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntityEnderChest;DDDFIF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/model/ModelChest.renderAll()V", shift = At.Shift.BY, by = -3))
    public void render(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                (main.getUtils().getLocation() == Location.THE_END || main.getUtils().getLocation() == Location.DRAGONS_NEST)) {
            Color color = main.getConfigValues().getColor(Feature.MAKE_ENDERCHESTS_GREEN_IN_END);
            if (color.getRGB() == ChatFormatting.GREEN.getRGB()) {
                GlStateManager.color(0, 1, 0); // classic lime green
            } else {
                GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255);
            }
        }
    }

}
