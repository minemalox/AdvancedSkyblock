package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin({EntityLivingBase.class})
public class EntityLivingBaseMixin {

    @Inject(method = "handleStatusUpdate(B)V", at = @At(value = "FIELD", target = "net/minecraft/entity/EntityLivingBase.hurtTime:I", shift = At.Shift.AFTER))
    public void handleStatusUpdate(byte id, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMBAT_TIMER_DISPLAY)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player.isPotionActive(MobEffects.ABSORPTION)) {

            List<Entity> nearEntities = mc.world.getEntitiesWithinAABB(Entity.class,
                    new AxisAlignedBB(
                            mc.player.posX - 2,
                            mc.player.posY - 2,
                            mc.player.posZ - 2,
                            mc.player.posX + 2,
                            mc.player.posY + 2,
                            mc.player.posZ + 2
                    ));
            boolean foundPossibleAttacker = false;

            for (Entity entity : nearEntities) {
                if (entity instanceof EntityMob || entity instanceof EntityWolf || entity instanceof IProjectile) {
                    foundPossibleAttacker = true;
                    break;
                }
            }

            if (foundPossibleAttacker) {
                AdvancedSkyblock.getInstance().getUtils().setLastDamaged(System.currentTimeMillis());
            }
        }
    }
}
