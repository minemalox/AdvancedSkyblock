package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.asm.utils.MinecraftHelper;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    private static String lastItemName = null;
    private static long lastDrop = Minecraft.getSystemTime();
    private static float lastUpdate = -1;

    @Inject(method = "dropItem(Z)Lnet/minecraft/entity/item/EntityItem;", at = @At("HEAD"), cancellable = true)
    public void dropItem(boolean dropAll, CallbackInfoReturnable<EntityItem> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItemStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

        if ((main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)) {
                int slot = mc.player.inventory.currentItem + 36;
                if (main.getConfigValues().getLockedSlots().contains(slot) && (slot >= 9 || mc.player.openContainer instanceof ContainerPlayer && slot >= 5)) {
                    main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                    AdvancedSkyblock.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }

                if (System.currentTimeMillis() - MinecraftHelper.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                    AdvancedSkyblock.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SWITCHED_SLOTS.getMessage());
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }
            }

            if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (!main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true)) {
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) + Message.MESSAGE_CANCELLED_DROPPING.getMessage());
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }

                if (System.currentTimeMillis() - MinecraftHelper.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                    AdvancedSkyblock.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SWITCHED_SLOTS.getMessage());
                    callbackInfoReturnable.setReturnValue(null);
                    return;
                }
            }
        }

        if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.DROP_CONFIRMATION) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer()
                || main.getConfigValues().isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES))) {
            lastDrop = Minecraft.getSystemTime();

            String heldItemName = heldItemStack.hasDisplayName() ? heldItemStack.getDisplayName() : heldItemStack.getTranslationKey();

            if (lastItemName == null || !lastItemName.equals(heldItemName) || Minecraft.getSystemTime() - lastDrop >= 3000L) {
                AdvancedSkyblock.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_DROP_CONFIRMATION.getMessage());
                lastItemName = heldItemName;
                callbackInfoReturnable.setReturnValue(null);
                return;
            }
        }
    }

    @Inject(method = "setPlayerSPHealth", at = @At("HEAD"))
    public void setPlayerSPHealth(float health, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMBAT_TIMER_DISPLAY)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (health < lastUpdate || health == lastUpdate && mc.player.isPotionActive(MobEffects.ABSORPTION)) {

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
        lastUpdate = health;
    }

}
