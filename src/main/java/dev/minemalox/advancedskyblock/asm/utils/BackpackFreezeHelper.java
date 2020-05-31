package dev.minemalox.advancedskyblock.asm.utils;

public class BackpackFreezeHelper {

    /**
     * The last time the backpack preview freeze key was pressed.
     * This is to stop multiple methods that handle similar logic from
     * performing the same actions multiple times.
     */
    private static long lastBackpackFreezeKey = -1;
    /**
     * This controls whether or not the backpack preview is frozen- allowing you
     * to hover over a backpack's contents in full detail!
     */
    private static boolean freezeBackpack = false;

    public static long getLastBackpackFreezeKey() {
        return lastBackpackFreezeKey;
    }

    public static void setLastBackpackFreezeKey(long lastBackpackFreezeKey) {
        BackpackFreezeHelper.lastBackpackFreezeKey = lastBackpackFreezeKey;
    }

    public static boolean isFreezeBackpack() {
        return freezeBackpack;
    }

    public static void setFreezeBackpack(boolean freezeBackpack) {
        BackpackFreezeHelper.freezeBackpack = freezeBackpack;
    }

}
