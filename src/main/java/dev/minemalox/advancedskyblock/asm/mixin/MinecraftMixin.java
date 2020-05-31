package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.asm.utils.MinecraftHelper;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Message;
import dev.minemalox.advancedskyblock.utils.npc.NPCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    private static final ResourceLocation currentLocation = new ResourceLocation("advancedskyblock", "bars.png");

    private static long lastProfileMessage = -1;

    @Shadow
    private IReloadableResourceManager resourceManager;

    private boolean isItemBow(ItemStack item) {
        return item != null && item.getItem() != null && item.getItem().equals(Items.BOW);
    }

    @Inject(method = "refreshResources()V", at = @At("RETURN"))
    private void onRefreshResources(CallbackInfo callbackInfo) {
        boolean usingOldPackTexture = false;
        boolean usingDefaultTexture = true;
        try {
            IResource currentResource = resourceManager.getResource(currentLocation);
            String currentHash = DigestUtils.md5Hex(currentResource.getInputStream());

            InputStream oldStream = AdvancedSkyblock.class.getClassLoader().getResourceAsStream("assets/advancedskyblock/imperialoldbars.png");
            if (oldStream != null) {
                String oldHash = DigestUtils.md5Hex(oldStream);
                usingOldPackTexture = currentHash.equals(oldHash);
            }

            InputStream barsStream = AdvancedSkyblock.class.getClassLoader().getResourceAsStream("assets/advancedskyblock/bars.png");
            if (barsStream != null) {
                String barsHash = DigestUtils.md5Hex(barsStream);
                usingDefaultTexture = currentHash.equals(barsHash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingOldSkyBlockTexture(usingOldPackTexture);
            main.getUtils().setUsingDefaultBarTextures(usingDefaultTexture);
        }
    }

    @Inject(method = "rightClickMouse()V", at = @At(value = "FIELD", target = "net/minecraft/client/Minecraft.rightClickDelayTimer:I", shift = At.Shift.AFTER), cancellable = true)
    private void rightClickMouse(CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;
                if (main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
                    if (entityIn instanceof EntityOtherPlayerMP && !NPCUtils.isNPC(entityIn)) {
                        ItemStack item = mc.player.inventory.getCurrentItem();
                        ItemStack itemInUse = mc.player.getActiveItemStack();
                        if ((isItemBow(item) || isItemBow(itemInUse))) {
                            if (System.currentTimeMillis() - lastProfileMessage > 20000) {
                                lastProfileMessage = System.currentTimeMillis();
                                main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DONT_OPEN_PROFILES_WITH_BOW) +
                                        Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                            }
                            callbackInfo.cancel();
                            return;
                        }
                    }
                }
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame) entityIn).getDisplayedItem() == null) {
                    int slot = mc.player.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        callbackInfo.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "processKeyBinds()V", at = @At(value = "FIELD", target = "net/minecraft/entity/player/InventoryPlayer.currentItem:I", shift = At.Shift.AFTER))
    private void processKeyBinds(CallbackInfo callbackInfo) {
        Minecraft mc = Minecraft.getMinecraft();
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = mc.player.inventory.currentItem + 36;
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.player.openContainer instanceof ContainerPlayer && slot >= 5)) {

                MinecraftHelper.lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)
                    && !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true, false)) {

                MinecraftHelper.lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }
}
