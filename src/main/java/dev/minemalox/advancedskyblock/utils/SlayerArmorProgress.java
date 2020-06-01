package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class SlayerArmorProgress {

    /**
     * The itemstack that this progress is representing.
     */
    private final ItemStack itemStack;

    /**
     * The current slayer progress % of the item.
     */
    private String percent;

    /**
     * The current slayer defence reward of the item.
     */
    private String defence;

    public SlayerArmorProgress(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack.getItem()); // Cloned because we change the helmet color later.
        this.percent = "55";
        this.defence = "§a40❈";

        setHelmetColor();
    }

    SlayerArmorProgress(ItemStack itemStack, String percent, String defence) {
        this.itemStack = itemStack;
        this.percent = percent;
        this.defence = defence;
    }

    private void setHelmetColor() {
        if (itemStack.getItem().equals(Items.LEATHER_HELMET)) {
            ((ItemArmor) itemStack.getItem()).setColor(itemStack, ChatFormatting.BLACK.getRGB());
        }
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public String getPercent() {
        return this.percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getDefence() {
        return this.defence;
    }

    public void setDefence(String defence) {
        this.defence = defence;
    }
}
