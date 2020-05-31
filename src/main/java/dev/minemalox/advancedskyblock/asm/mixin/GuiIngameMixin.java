package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class GuiIngameMixin {

    protected dev.minemalox.advancedskyblock.boss.GuiBossOverlay hypixelBossOverlay;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void GuiIngame(Minecraft mcIn, CallbackInfo callbackInfo) {
        hypixelBossOverlay = new dev.minemalox.advancedskyblock.boss.GuiBossOverlay(mcIn);
    }

    @Redirect(method = "renderBossHealth", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/GuiBossOverlay.renderBossHealth()V"))
    private void renderGameOverlay(GuiBossOverlay guiBossOverlay) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            hypixelBossOverlay.renderBossHealth();
        } else {
            guiBossOverlay.renderBossHealth();
        }

    }

}
