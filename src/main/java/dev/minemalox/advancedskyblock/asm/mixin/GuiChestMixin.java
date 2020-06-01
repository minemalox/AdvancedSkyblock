package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.gui.elements.CraftingPatternSelection;
import dev.minemalox.advancedskyblock.utils.*;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import dev.minemalox.advancedskyblock.utils.nifty.StringUtil;
import dev.minemalox.advancedskyblock.utils.npc.NPCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Mixin({GuiChest.class})
public abstract class GuiChestMixin extends GuiContainer {

    private static GuiTextField textFieldMatch = null;
    private static GuiTextField textFieldExclusions = null;
    private static CraftingPatternSelection craftingPatternSelection = null;
    private static Backpack backpack = null;

    @Shadow
    @Final
    public IInventory lowerChestInventory;

    @Shadow
    @Final
    private IInventory upperChestInventory;

    public GuiChestMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void drawString(FontRenderer fontRendererIn, String textIn, int x, int y, int color) {
        if (backpack != null) {
            fontRendererIn.drawString(textIn, x, y, backpack.getBackpackColor().getInventoryTextColor());
            return;
        }
        fontRendererIn.drawString(textIn, x, y, color);
    }

    @Override
    public void updateScreen() {
        if (textFieldMatch != null && textFieldExclusions != null) {
            textFieldMatch.updateCursorCounter();
            textFieldExclusions.updateCursorCounter();
        }
    }

    @Override
    public void onGuiClosed() {
        EnumUtils.InventoryType.resetCurrentInventoryType();
        if (textFieldMatch != null && textFieldExclusions != null) {
            Keyboard.enableRepeatEvents(false);
        }
    }

    @Inject(method = "drawScreen(IIF)V", at = {@At("RETURN")})
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        EnumUtils.InventoryType inventoryType = EnumUtils.InventoryType.getCurrentInventoryType();

        if (textFieldMatch != null &&
                (inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE ||
                        inventoryType == EnumUtils.InventoryType.REFORGE_ANVIL)) {

            GlStateManager.color(1F, 1F, 1F);
            AdvancedSkyblock main = AdvancedSkyblock.getInstance();
            String inventoryMessage = inventoryType.getMessage();
            int defaultBlue = main.getUtils().getDefaultBlue(255);
            GlStateManager.pushMatrix();
            float scale = 0.75F;
            GlStateManager.scale(scale, scale, 1);
            int x = guiLeft - 160;

            if (x < 0) {
                x = 20;
            }

            fontRenderer.drawString(Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), Math.round(x / scale), Math.round((guiTop + 40) / scale), defaultBlue);
            fontRenderer.drawString(Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x / scale), Math.round((guiTop + 50) / scale), defaultBlue);
            fontRenderer.drawString(Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), Math.round(x / scale), Math.round((guiTop + 70) / scale), defaultBlue);
            fontRenderer.drawString(Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), Math.round(x / scale), Math.round((guiTop + 110) / scale), defaultBlue);

            GlStateManager.popMatrix();

            textFieldMatch.drawTextBox();
            if (StringUtil.isEmpty(textFieldMatch.getText())) {
                drawString(Minecraft.getMinecraft().fontRenderer, "ex. \"prot, feather\"", x + 4, guiTop + 86, ChatFormatting.DARK_GRAY.getCode());
            }
            textFieldExclusions.drawTextBox();
            if (StringUtil.isEmpty(textFieldExclusions.getText())) {
                drawString(Minecraft.getMinecraft().fontRenderer, "ex. \"proj, blast\"", x + 4, guiTop + 126, ChatFormatting.DARK_GRAY.getCode());
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        if (!AdvancedSkyblock.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        EnumUtils.InventoryType inventoryType = EnumUtils.InventoryType.getCurrentInventoryType(guiName);

        if (inventoryType != null) {

            if (inventoryType == EnumUtils.InventoryType.CRAFTING_TABLE) {
                if (AdvancedSkyblock.getInstance().getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {
                    craftingPatternSelection = new CraftingPatternSelection(Minecraft.getMinecraft(), Math.max(guiLeft - CraftingPatternSelection.ICON_SIZE - 2, 10), guiTop + 1);
                }
                return;
            }

            int xPos = guiLeft - 160;
            if (xPos < 0) {
                xPos = 20;
            }
            int yPos = guiTop + 80;
            textFieldMatch = new GuiTextField(2, fontRenderer, xPos, yPos, 120, 20);
            textFieldMatch.setMaxStringLength(500);
            List<String> lockedEnchantments = AdvancedSkyblock.getInstance().getUtils().getEnchantmentMatches();
            StringBuilder enchantmentBuilder = new StringBuilder();
            int i = 1;
            for (String enchantment : lockedEnchantments) {
                enchantmentBuilder.append(enchantment);
                if (i < lockedEnchantments.size()) {
                    enchantmentBuilder.append(",");
                }
                i++;
            }
            String text = enchantmentBuilder.toString();
            if (text.length() > 0) {
                textFieldMatch.setText(text);
            }
            yPos += 40;
            textFieldExclusions = new GuiTextField(2, fontRenderer, xPos, yPos, 120, 20);
            textFieldExclusions.setMaxStringLength(500);
            lockedEnchantments = AdvancedSkyblock.getInstance().getUtils().getEnchantmentExclusions();
            enchantmentBuilder = new StringBuilder();
            i = 1;
            for (String enchantment : lockedEnchantments) {
                enchantmentBuilder.append(enchantment);
                if (i < lockedEnchantments.size()) {
                    enchantmentBuilder.append(",");
                }
                i++;
            }
            text = enchantmentBuilder.toString();
            if (text.length() > 0) {
                textFieldExclusions.setText(text);
            }
            Keyboard.enableRepeatEvents(true);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if ((EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.ENCHANTMENT_TABLE ||
                EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.REFORGE_ANVIL)) {
            if (keyCode != Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() || (!textFieldMatch.isFocused() && !textFieldExclusions.isFocused())) {
                processTextFields(typedChar, keyCode);
                super.keyTyped(typedChar, keyCode);
                return;
            }
            processTextFields(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void processTextFields(char typedChar, int keyCode) {
        if (textFieldMatch != null) {
            textFieldMatch.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> enchantments = new LinkedList<>(Arrays.asList(textFieldMatch.getText().split(",")));
            AdvancedSkyblock.getInstance().getUtils().setEnchantmentMatches(enchantments);
            enchantments = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            AdvancedSkyblock.getInstance().getUtils().setEnchantmentExclusions(enchantments);
        }
    }

    @Override
    public void handleMouseClick(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().getEnchantmentMatches().size() > 0) {
            if (slotIn != null && !slotIn.inventory.equals(Minecraft.getMinecraft().player.inventory) && slotIn.getHasStack()) {
                if (slotIn.getSlotIndex() == 13 && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.ENCHANTMENT_TABLE) {
                    ItemStack[] enchantBottles = {inventorySlots.getSlot(29).getStack(), inventorySlots.getSlot(31).getStack(), inventorySlots.getSlot(33).getStack()};
                    for (ItemStack bottle : enchantBottles) {
                        if (bottle != null && bottle.hasDisplayName()) {
                            if (bottle.getDisplayName().startsWith(ChatFormatting.GREEN + "Enchant Item")) {
                                Minecraft mc = Minecraft.getMinecraft();
                                List<String> toolip = bottle.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
                                if (toolip.size() > 2) {
                                    String[] lines = toolip.get(2).split(Pattern.quote("* "));

                                    if (lines.length > 1) {
                                        String enchantLine = lines[1];
                                        if (main.getUtils().enchantReforgeMatches(enchantLine)) {
                                            main.getUtils().playLoudSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1);
                                            return;
                                        }
                                    }
                                }
                            } else if (bottle.getDisplayName().startsWith(ChatFormatting.RED + "Enchant Item")) {
                                // Stop player from removing item before the enchants have even loaded.
                                return;
                            }
                        }
                    }
                } else if (slotIn.getSlotIndex() == 22 && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.REFORGE_ANVIL) {
                    Slot itemSlot = inventorySlots.getSlot(13);
                    if (itemSlot != null && itemSlot.getHasStack()) {
                        ItemStack item = itemSlot.getStack();
                        if (item.hasDisplayName()) {
                            String reforge = main.getUtils().getReforgeFromItem(item);
                            if (reforge != null) {
                                if (main.getUtils().enchantReforgeMatches(reforge)) {
                                    main.getUtils().playLoudSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                lowerChestInventory.hasCustomName() && NPCUtils.isFullMerchant(lowerChestInventory.getDisplayName().getUnformattedText())
                && slotIn != null && slotIn.inventory instanceof InventoryPlayer) {
            if (!main.getUtils().getItemDropChecker().canDropItem(slotIn)) {
                return;
            }
        }
        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (textFieldMatch != null) {
            textFieldMatch.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (craftingPatternSelection != null && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE) {
            craftingPatternSelection.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Redirect(method = "drawGuiContainerBackgroundLayer(FII)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.color(FFFF)V"))
    protected void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        // Draw here to make sure it's in the background of the GUI and items overlay it.
        if (EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE && craftingPatternSelection != null) {
            craftingPatternSelection.draw();
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)
                && lowerChestInventory.hasCustomName()) {
            if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                backpack = Backpack.getFromItem(mc.player.getHeldItem(EnumHand.MAIN_HAND));
                if (backpack != null) {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    return;
                }
            } else if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Bank")) {
                ItemStack item = mc.player.getHeldItem(EnumHand.MAIN_HAND); // easter egg question mark
                if (item != null && item.hasDisplayName() && item.getDisplayName().contains("Piggy Bank")) {
                    BackpackColor color = BackpackColor.PINK;
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                }
                return;
            }
        }
        backpack = null;
        GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
    }

    /**
     * Draw the GuiChet Header and Footer Strings.
     *
     * @param mouseX takes the x-Coordinate of the Mouse.
     * @param mouseY takes the y-Coordinate of the Mouse.
     * @author MineMalox
     */
    @Overwrite
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        fontRenderer.drawString(this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

}
