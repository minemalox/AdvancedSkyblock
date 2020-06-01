package dev.minemalox.advancedskyblock.constants.game;

/**
 * Skyblock item rarity definitions
 */
public enum Rarity {
    COMMON("§f§lCOMMON"),
    UNCOMMON("§a§lUNCOMMON"),
    RARE("§9§lRARE"),
    EPIC("§5§lEPIC"),
    LEGENDARY("§6§lLEGENDARY"),
    SPECIAL("§d§lSPECIAL");

    private final String tag;

    Rarity(String s) {
        this.tag = s;
    }

    public String getTag() {
        return this.tag;
    }
}
