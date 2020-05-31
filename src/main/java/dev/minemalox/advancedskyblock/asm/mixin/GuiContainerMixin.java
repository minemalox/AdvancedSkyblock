package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.asm.utils.BackpackFreezeHelper;
import dev.minemalox.advancedskyblock.utils.*;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin({GuiContainer.class})
public abstract class GuiContainerMixin extends GuiScreen {

    private final static ResourceLocation LOCK = new ResourceLocation("advancedskyblock", "lock.png");
    private final static ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final static int OVERLAY_RED = ChatFormatting.RED.getColor(127).getRGB();
    private final static int OVERLAY_GREEN = ChatFormatting.GREEN.getColor(127).getRGB();
    private static EnchantPair reforgeToRender = null;
    private static Set<EnchantPair> enchantsToRender = new HashSet<>();
    @Shadow
    private Slot hoveredSlot;

    @Inject(method = "drawSlot(Lnet/minecraft/inventory/Slot;)V", locals = LocalCapture.CAPTURE_FAILHARD, at = {@At(value = "INVOKE", target = "net/minecraft/client/renderer/RenderItem.renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V", shift = At.Shift.AFTER)})
    public void showEnchantments(Slot slotIn, CallbackInfo ci, int x, int y, ItemStack item, boolean flag, boolean flag1, ItemStack itemstack1, String s) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getConfigValues().isEnabled(Feature.SHOW_ENCHANTMENTS_REFORGES)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (item != null && item.hasDisplayName()) {
                if (item.getDisplayName().startsWith(ChatFormatting.GREEN + "Enchant Item")) {
                    List<String> toolip = item.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
                    if (toolip.size() > 2) {
                        String enchantLine = toolip.get(2);
                        String[] lines = enchantLine.split(Pattern.quote("* "));
                        if (lines.length >= 2) {
                            String toMatch = lines[1];
                            String enchant;
                            if (!main.getUtils().getEnchantmentMatches().isEmpty() &&
                                    main.getUtils().enchantReforgeMatches(toMatch)) {
                                enchant = ChatFormatting.RED + toMatch;
                            } else {
                                enchant = ChatFormatting.YELLOW + toMatch;
                            }
                            float yOff;
                            if (slotIn.slotNumber == 29 || slotIn.slotNumber == 33) {
                                yOff = 26;
                            } else {
                                yOff = 36;
                            }
                            float scaleMultiplier = 1 / 0.75F;
                            float halfStringWidth = fontRenderer.getStringWidth(enchant) / 2F;
                            x += 8; // to center it
                            enchantsToRender.add(new EnchantPair(x * scaleMultiplier - halfStringWidth, y * scaleMultiplier + yOff, enchant));
                        }
                    }
                } else if ("Reforge Item".equals(slotIn.inventory.getDisplayName().getUnformattedText()) && slotIn.slotNumber == 13) {
                    String reforge = main.getUtils().getReforgeFromItem(item);
                    if (reforge != null) {
                        if (!main.getUtils().getEnchantmentMatches().isEmpty() &&
                                main.getUtils().enchantReforgeMatches(reforge)) {
                            reforge = ChatFormatting.RED + reforge;
                        } else {
                            reforge = ChatFormatting.YELLOW + reforge;
                        }
                        x -= 28;
                        y += 22;
                        float halfStringWidth = fontRenderer.getStringWidth(reforge) / 2F;
                        reforgeToRender = new EnchantPair(x - halfStringWidth, y, reforge);
                    }
                }
            }
            if (slotIn.slotNumber == 53) {
                GlStateManager.pushMatrix();

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                if (reforgeToRender != null) {
                    fontRenderer.drawString(reforgeToRender.getEnchant(), reforgeToRender.getX(), reforgeToRender.getY(), ChatFormatting.WHITE.getCode(), true);
                    reforgeToRender = null;
                }
                GlStateManager.scale(0.75, 0.75, 1);
                Iterator<EnchantPair> enchantPairIterator = enchantsToRender.iterator();
                while (enchantPairIterator.hasNext()) {
                    EnchantPair enchant = enchantPairIterator.next();
                    fontRenderer.drawString(enchant.getEnchant(), enchant.getX(), enchant.getY(), ChatFormatting.WHITE.getCode(), true);
                    enchantPairIterator.remove();
                }
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();

                GlStateManager.popMatrix();
            }
        }
    }

    @Inject(method = "keyTyped(CI)V", at = {@At(value = "INVOKE", target = "net/minecraft/client/gui/inventory/GuiContainer.checkHotbarKeys(I)Z", shift = At.Shift.BEFORE)}, cancellable = true)
    protected void keyTyped2(char typedChar, int keyCode, CallbackInfo callbackInfo) throws IOException {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
            BackpackFreezeHelper.setFreezeBackpack(false);
            main.getUtils().setBackpackToRender(null);
        }
        if (keyCode == main.getFreezeBackpackKey().getKeyCode() && BackpackFreezeHelper.isFreezeBackpack() &&
                System.currentTimeMillis() - BackpackFreezeHelper.getLastBackpackFreezeKey() > 500) {
            BackpackFreezeHelper.setLastBackpackFreezeKey(System.currentTimeMillis());
            BackpackFreezeHelper.setFreezeBackpack(false);
        }
    }

    @Inject(method = "drawScreen(IIF)V", at = @At("RETURN"))
    public void drawBackpacks(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();
        Minecraft mc = Minecraft.getMinecraft();
        if (backpack != null) {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI) {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length / 9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 300);
                int textColor = 4210752;
                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)) {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    textColor = color.getInventoryTextColor();
                }
                drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                fontRenderer.drawString(backpack.getBackpackName(), x + 8, y + 6, textColor);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                ItemStack toRenderOverlay = null;
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = x + 8 + ((i % 9) * 18);
                        int itemY = y + 18 + ((i / 9) * 18);
                        RenderItem renderItem = mc.getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(fontRenderer, item, itemX, itemY, null);
                        if (BackpackFreezeHelper.isFreezeBackpack() && mouseX > itemX && mouseX < itemX + 16 && mouseY > itemY && mouseY < itemY + 16) {
                            toRenderOverlay = item;
                        }
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
                if (toRenderOverlay != null) {
                    drawHoveringText(toRenderOverlay.getTooltip(null, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL),
                            mouseX, mouseY);
                }
            } else {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, 0, 300);
                Gui.drawRect(x, y, x + (16 * 9) + 3, y + (16 * (length / 9)) + 3, ChatFormatting.DARK_GRAY.getColor(250).getRGB());
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = x + ((i % 9) * 16);
                        int itemY = y + ((i / 9) * 16);
                        RenderItem renderItem = mc.getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(fontRenderer, item, itemX, itemY, null);
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            if (!BackpackFreezeHelper.isFreezeBackpack()) {
                main.getUtils().setBackpackToRender(null);
            }
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }


    @Inject(method = "drawScreen(IIF)V", at = {@At(value = "INVOKE", target = "net/minecraft/client/renderer/OpenGlHelper.setLightmapTextureCoords(IFF)V", shift = At.Shift.BEFORE)})
    public void setLastSlot(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
        AdvancedSkyblock.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @Redirect(method = "drawScreen(IIF)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V",
            ordinal = 0
    ))
    private void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor) {
        if (BackpackFreezeHelper.isFreezeBackpack()) return;
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Container container = Minecraft.getMinecraft().player.openContainer;
        if (hoveredSlot != null) {
            int slotNum = hoveredSlot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                drawGradientRect(left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
                return;
            }
        }
        drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Inject(method = "drawScreen(IIF)V", locals = LocalCapture.CAPTURE_FAILHARD, at = {@At(value = "INVOKE", target = "net/minecraft/client/gui/inventory/GuiContainer.drawSlot(Lnet/minecraft/inventory/Slot;)V", shift = At.Shift.AFTER)})
    public void drawSlot(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, int i, int j, int k, int l, int i1, Slot slot) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        Container container = mc.player.openContainer;

        if (slot != null) {
            // Draw crafting pattern overlays inside the crafting grid.
            if (main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS) && main.getUtils().isOnSkyblock()
                    && slot.inventory.getDisplayName().getUnformattedText().equals(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME)
                    && main.getPersistentValues().getSelectedCraftingPattern() != CraftingPattern.FREE) {

                int craftingGridIndex = CraftingPattern.slotToCraftingGridIndex(slot.getSlotIndex());
                if (craftingGridIndex >= 0) {
                    int slotLeft = slot.xPos;
                    int slotTop = slot.yPos;
                    int slotRight = slotLeft + 16;
                    int slotBottom = slotTop + 16;
                    if (main.getPersistentValues().getSelectedCraftingPattern().isSlotInPattern(craftingGridIndex)) {
                        if (!slot.getHasStack()) {
                            drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, OVERLAY_GREEN, OVERLAY_GREEN);
                        }
                    } else {
                        if (slot.getHasStack()) {
                            drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, OVERLAY_RED, OVERLAY_RED);
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock()) {
                int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
                if (main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1, 1, 1, 0.4F);
                    GlStateManager.enableBlend();
                    mc.getTextureManager().bindTexture(LOCK);
                    mc.ingameGUI.drawTexturedModalRect(slot.xPos, slot.yPos, 0, 0, 16, 16);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }


    @Inject(method = "keyTyped(CI)V", at = {@At("HEAD")})
    protected void keyTyped(char typedChar, int keyCode, CallbackInfo callbackInfo) throws IOException {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())) {
                int slot = main.getUtils().getLastHoveredSlot();

                if (mc.player.inventory.getItemStack() == null && hoveredSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                        }
                    }
                }
                if (slot >= 9 || mc.player.openContainer instanceof ContainerPlayer && slot >= 5) {
                    if (main.getConfigValues().getLockedSlots().contains(slot)) {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        } else {
                            main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                            callbackInfo.cancel();
                            return;
                        }
                    } else {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (!main.getUtils().getItemDropChecker().canDropItem(hoveredSlot)) callbackInfo.cancel();
            }
        }
    }


}
