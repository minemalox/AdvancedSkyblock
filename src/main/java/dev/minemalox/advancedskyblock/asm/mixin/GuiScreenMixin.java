package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.asm.utils.BackpackFreezeHelper;
import dev.minemalox.advancedskyblock.utils.Backpack;
import dev.minemalox.advancedskyblock.utils.CooldownManager;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public class GuiScreenMixin {

    private static final int MADDOX_BATPHONE_COOLDOWN = 20 * 1000;

    private boolean isFreezeKeyDown(AdvancedSkyblock main) {
        if (main.getFreezeBackpackKey().isKeyDown()) return true;
        if (main.getFreezeBackpackKey().isPressed()) return true;
        try {
            if (Keyboard.isKeyDown(main.getFreezeBackpackKey().getKeyCode())) return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    @Inject(method = "renderToolTip(Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    protected void shouldRenderRedirect(ItemStack stack, int x, int y, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (stack.getItem().equals(Items.SKULL) && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }

            Container playerContainer = Minecraft.getMinecraft().player.openContainer;
            if (playerContainer instanceof ContainerChest) { // Avoid showing backpack preview in auction stuff.
                IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chestInventory.hasCustomName()) {
                    String chestName = chestInventory.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                        // Show preview for backpacks in player inventory if enabled.
                        if (!main.getConfigValues().isEnabled(Feature.BACKPACK_PREVIEW_AH)) {
                            return;
                        }

                        /*
                        If the backpack is in the auction house window, ignore it.
                        Empty backpacks can't be listed in the auction.
                         */
                        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
                            if (ItemStack.areItemStackTagsEqual(chestInventory.getStackInSlot(i), stack)) {
                                return;
                            }
                        }
                    }
                }
            }

            Backpack backpack = Backpack.getFromItem(stack);
            if (backpack != null) {
                backpack.setX(x);
                backpack.setY(y);
                if (isFreezeKeyDown(main) && (System.currentTimeMillis() - BackpackFreezeHelper.getLastBackpackFreezeKey()) > 500) {
                    BackpackFreezeHelper.setLastBackpackFreezeKey(System.currentTimeMillis());
                    BackpackFreezeHelper.setFreezeBackpack(!BackpackFreezeHelper.isFreezeBackpack());
                    main.getUtils().setBackpackToRender(backpack);
                }
                if (!BackpackFreezeHelper.isFreezeBackpack()) {
                    main.getUtils().setBackpackToRender(backpack);
                }
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, ITooltipFlag.TooltipFlags.NORMAL));
                callbackInfo.cancel();
            }
        }
        if (BackpackFreezeHelper.isFreezeBackpack()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleComponentClick(Lnet/minecraft/util/text/ITextComponent;)Z", at = @At("HEAD"))
    public void handleComponentClick(ITextComponent component, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock() && component != null && "§2§l[OPEN MENU]".equals(component.getUnformattedText()) &&
                !CooldownManager.isOnCooldown(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME)) {// The prompt when Maddox picks up the phone.
            CooldownManager.put(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME, MADDOX_BATPHONE_COOLDOWN);
        }
    }

}
