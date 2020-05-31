package dev.minemalox.advancedskyblock.boss;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BossStatus {
    public static float healthScale;
    public static int statusBarTime;
    public static String bossName;
    public static boolean hasColorModifier;

    public static void setBossStatus(EntityDragon displayData, boolean hasColorModifierIn) {
        healthScale = displayData.getHealth() / displayData.getMaxHealth();
        statusBarTime = 100;
        bossName = displayData.getDisplayName().getFormattedText();
        hasColorModifier = hasColorModifierIn;
    }
}