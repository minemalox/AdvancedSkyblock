package dev.minemalox.advancedskyblock.utils;

public class EnchantPair {

    private float x;
    private float y;
    private String enchant;

    public EnchantPair(float x, float y, String enchant) {
        this.x = x;
        this.y = y;
        this.enchant = enchant;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public String getEnchant() {
        return this.enchant;
    }
}
