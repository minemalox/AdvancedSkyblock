package dev.minemalox.advancedskyblock.asm.utils;

public class MinecraftHelper {

    public static long lastLockedSlotItemChange = -1;

    public static long getLastLockedSlotItemChange() {
        return MinecraftHelper.lastLockedSlotItemChange;
    }
}
