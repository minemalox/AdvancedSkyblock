package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {

    /**
     * Cooldown between playing error sounds to avoid stacking up
     */
    private static final int CRAFTING_PATTERN_SOUND_COOLDOWN = 400;

    private static final Set<Location> DEEP_CAVERNS_LOCATIONS = EnumSet.of(Location.DEEP_CAVERNS, Location.GUNPOWDER_MINES,
            Location.LAPIS_QUARRY, Location.PIGMAN_DEN, Location.SLIMEHILL, Location.DIAMOND_RESERVE, Location.OBSIDIAN_SANCTUARY);

    private static final Set<Block> DEEP_CAVERNS_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.EMERALD_ORE,
            Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.OBSIDIAN, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE));

    private static final Set<Block> NETHER_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.GLOWSTONE, Blocks.QUARTZ_ORE, Blocks.NETHER_WART));

    private static long lastCraftingSoundPlayed = 0;
    private static long lastStemMessage = -1;
    private static long lastUnmineableMessage = -1;

    /**
     * Checks if an item is being dropped and if an item is being dropped, whether it is allowed to be dropped.
     * This check works only for mouse clicks, not presses of the "Drop Item" key.
     *
     * @param mouseButton the mouse Button
     * @param slotNum     the number of the slot that was clicked on
     * @param heldStack   the item stack the player is holding with their mouse
     * @return {@code true} if the action should be cancelled, {@code false} otherwise
     */
    public boolean checkItemDrop(int mouseButton, int slotNum, ItemStack heldStack) {
        // Is this a left or right click?
        if ((mouseButton == Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode() || mouseButton == Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode())) {
            // Is the player clicking outside their inventory?
            if (slotNum == -999) {
                // Is the player holding an item stack with their mouse?
                if (heldStack != null) {
                    return !AdvancedSkyblock.getInstance().getUtils().getItemDropChecker().canDropItem(heldStack);
                }
            }
        }

        // The player is not dropping an item. Don't cancel this action.
        return false;
    }

    @Inject(method = "clickBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", at = @At("HEAD"), cancellable = true)
    public void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.player;
        ItemStack heldItem = p.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem != null) {
            Block block = mc.world.getBlockState(loc).getBlock();
            long now = System.currentTimeMillis();

            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS) && (block.equals(Blocks.MELON_STEM) || block.equals(Blocks.PUMPKIN_STEM))) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_BREAKING_STEMS) && now - lastStemMessage > 20000) {
                    lastStemMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.AVOID_BREAKING_STEMS) + Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                callbackInfoReturnable.setReturnValue(null);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && DEEP_CAVERNS_LOCATIONS.contains(main.getUtils().getLocation())
                    && main.getUtils().isPickaxe(heldItem.getItem()) && !DEEP_CAVERNS_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                callbackInfoReturnable.setReturnValue(null);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_VALUABLES_NETHER) && Location.BLAZING_FORTRESS.equals(main.getUtils().getLocation()) &&
                    main.getUtils().isPickaxe(heldItem.getItem()) && !NETHER_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_NETHER) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_VALUABLES_NETHER) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                callbackInfoReturnable.setReturnValue(null);
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                if ((block.equals(Blocks.LOG) || block.equals(Blocks.LOG2))
                        && p.getHeldItem(EnumHand.MAIN_HAND) != null) {

                    final boolean holdingJungleAxeOnCooldown = InventoryUtils.JUNGLE_AXE_DISPLAYNAME.equals(p.getHeldItem(EnumHand.MAIN_HAND).getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.JUNGLE_AXE_DISPLAYNAME);
                    final boolean holdingTreecapitatorOnCooldown = InventoryUtils.TREECAPITATOR_DISPLAYNAME.equals(p.getHeldItem(EnumHand.MAIN_HAND).getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.TREECAPITATOR_DISPLAYNAME);

                    if (holdingJungleAxeOnCooldown || holdingTreecapitatorOnCooldown) {
                        callbackInfoReturnable.setReturnValue(null);
                    }
                }
            }
        }
    }

    @Inject(method = "onPlayerDestroyBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"))
    public void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem != null) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (main.getUtils().isOnSkyblock()
                    && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)
                    && (block.equals(Blocks.LOG) || block.equals(Blocks.LOG2))) {
                if (InventoryUtils.JUNGLE_AXE_DISPLAYNAME.equals(heldItem.getDisplayName())
                        || InventoryUtils.TREECAPITATOR_DISPLAYNAME.equals(heldItem.getDisplayName())) {
                    CooldownManager.put(heldItem);
                }
            }
        }
    }

    @Inject(method = "windowClick(IIILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    public void windowClick(int windowId, int slotNum, int mouseButton, ClickType type, EntityPlayer player, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            callbackInfoReturnable.setReturnValue(null);
            return;
        }

        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        final int slotId = slotNum;
        ItemStack itemStack = player.inventory.getItemStack();

        if (main.getUtils().isOnSkyblock()) {
            // Prevent dropping rare items
            if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (checkItemDrop(mouseButton, slotNum, itemStack)) {
                    callbackInfoReturnable.setReturnValue(null);
                }
            }

            if (player.openContainer != null) {
                slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);

                // Prevent clicking on locked slots.
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)
                        && main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)) {
                    main.getUtils().playLoudSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                    callbackInfoReturnable.setReturnValue(null);
                }

                // Crafting patterns
                final Container slots = player.openContainer;

                Slot slotIn;
                try {
                    slotIn = slots.getSlot(slotId);
                } catch (IndexOutOfBoundsException e) {
                    slotIn = null;
                }

                if (slotIn != null && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE
                        && main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {

                    final CraftingPattern selectedPattern = main.getPersistentValues().getSelectedCraftingPattern();
                    final ItemStack clickedItem = slotIn.getStack();
                    if (selectedPattern != CraftingPattern.FREE && clickedItem != null) {
                        final ItemStack[] craftingGrid = new ItemStack[9];
                        for (int i = 0; i < CraftingPattern.CRAFTING_GRID_SLOTS.size(); i++) {
                            int slotIndex = CraftingPattern.CRAFTING_GRID_SLOTS.get(i);
                            craftingGrid[i] = slots.getSlot(slotIndex).getStack();
                        }

                        final CraftingPatternResult result = selectedPattern.checkAgainstGrid(craftingGrid);

                        if (slotIn.inventory.equals(Minecraft.getMinecraft().player.inventory)) {
                            if (result.isFilled() && !result.fitsItem(clickedItem) && type == ClickType.QUICK_MOVE) {
                                // cancel shift-clicking items from the inventory if the pattern is already filled
                                if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                    main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                                    lastCraftingSoundPlayed = System.currentTimeMillis();
                                }
                                callbackInfoReturnable.setReturnValue(null);
                            }
                        } else {
                            if (slotIn.getSlotIndex() == CraftingPattern.CRAFTING_RESULT_INDEX
                                    && !result.isSatisfied()
                                    && main.getPersistentValues().isBlockCraftingIncompletePatterns()) {
                                // cancel clicking the result if the pattern isn't satisfied
                                if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                    main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                                    lastCraftingSoundPlayed = System.currentTimeMillis();
                                }
                                callbackInfoReturnable.setReturnValue(null);
                            }
                        }
                    }
                }
            }
        } else {
            if (checkItemDrop(mouseButton, slotNum, itemStack)) {
                callbackInfoReturnable.setReturnValue(null);
            }
        }
    }
}
