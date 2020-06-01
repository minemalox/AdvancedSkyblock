package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;

public class GuiFeatureData {

    private ChatFormatting defaultColor = null;
    private CoordsPair defaultPos = null;
    private CoordsPair defaultBarSize = null;
    private EnumUtils.AnchorPoint defaultAnchor = null;
    private EnumUtils.DrawType drawType = null;

    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br>
     * <p>
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    private boolean colorsRestricted;

    GuiFeatureData(ChatFormatting defaultColor) {
        this(defaultColor, false);
    }

    GuiFeatureData(ChatFormatting defaultColor, boolean colorsRestricted) {
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, int... positionThenSizes) {
        this(drawType, defaultColor, defaultAnchor, false, positionThenSizes);
    }

    GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... positionThenSizes) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new CoordsPair(positionThenSizes[0], positionThenSizes[1]);
        if (positionThenSizes.length > 2) {
            this.defaultBarSize = new CoordsPair(positionThenSizes[2], positionThenSizes[3]);
        }
        this.defaultAnchor = defaultAnchor;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, int... position) {
        this(drawType, defaultAnchor, false, position);
    }

    private GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... position) {
        this.drawType = drawType;
        this.defaultAnchor = defaultAnchor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new CoordsPair(position[0], position[1]);
    }

    public ChatFormatting getDefaultColor() {
        return this.defaultColor;
    }

    public CoordsPair getDefaultPos() {
        return this.defaultPos;
    }

    public CoordsPair getDefaultBarSize() {
        return this.defaultBarSize;
    }

    public EnumUtils.AnchorPoint getDefaultAnchor() {
        return this.defaultAnchor;
    }

    public EnumUtils.DrawType getDrawType() {
        return this.drawType;
    }

    public boolean isColorsRestricted() {
        return this.colorsRestricted;
    }
}
