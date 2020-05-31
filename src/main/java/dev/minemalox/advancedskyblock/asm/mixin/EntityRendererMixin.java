package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.npc.NPCUtils;
import dev.minemalox.advancedskyblock.utils.npc.Tag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "getMouseOver(F)V", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "net/minecraft/client/multiplayer/WorldClient.getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;", shift = At.Shift.BY, by = 2))
    public void getMouseOver(float partialTicks, CallbackInfo ci, Entity entityIn, double d0, Vec3d vec3d, boolean flag, int i, double d1, Vec3d vec3d1, Vec3d vec3d2, Vec3d vec3d3, float f, List<Entity> list) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            if (!GuiScreen.isCtrlKeyDown() && main.getConfigValues().isEnabled(Feature.IGNORE_ITEM_FRAME_CLICKS)) {
                list.removeIf(listEntity -> listEntity instanceof EntityItemFrame &&
                        (((EntityItemFrame) listEntity).getDisplayedItem() != null || Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND) == null));
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS)) {
                list.removeIf(entity -> entity instanceof EntityOtherPlayerMP && NPCUtils.isNearAnyNPCWithTag(entityIn, Tag.IMPORTANT) && !NPCUtils.isNPC(entityIn));
            }
        }
    }

    @Inject(method = "getNightVisionBrightness(Lnet/minecraft/entity/EntityLivingBase;F)F", at = @At("HEAD"), cancellable = true)
    private void getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getConfigValues().isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)) {
            callbackInfoReturnable.setReturnValue(1.0F);
        }

    }

}
