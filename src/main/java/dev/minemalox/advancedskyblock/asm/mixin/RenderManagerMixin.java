package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Location;
import dev.minemalox.advancedskyblock.utils.npc.NPCUtils;
import dev.minemalox.advancedskyblock.utils.npc.Tag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class RenderManagerMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            Location currentLocation = main.getUtils().getLocation();

            if (entityIn instanceof EntityItem &&
                    entityIn.getRidingEntity() instanceof EntityArmorStand && entityIn.getRidingEntity().isInvisible()) { // Conditions for skeleton helmet flying bones
                if (main.getConfigValues().isEnabled(Feature.HIDE_BONES)) {
                    callbackInfoReturnable.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS)) {
                if (entityIn instanceof EntityOtherPlayerMP && NPCUtils.isNearAnyNPCWithTag(entityIn, Tag.IMPORTANT) && !NPCUtils.isNPC(entityIn)) {
                    callbackInfoReturnable.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (currentLocation == Location.VILLAGE || currentLocation == Location.AUCTION_HOUSE ||
                        currentLocation == Location.BANK) {
                    if ((entityIn instanceof EntityOtherPlayerMP /* TODO: || entityIn instanceof EntityFX*/ || entityIn instanceof EntityItemFrame) &&
                            entityIn.getDistance(Minecraft.getMinecraft().player) > 7) {
                        callbackInfoReturnable.setReturnValue(false);
                    }
                }
            }
        }
    }

}
