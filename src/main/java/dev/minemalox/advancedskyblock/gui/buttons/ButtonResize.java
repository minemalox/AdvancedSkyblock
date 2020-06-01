package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonResize extends ButtonFeature {

    private int lastMouseX;
    private int lastMouseY;

    private Corner corner;

    public ButtonResize(int x, int y, Feature feature, Corner corner) {
        super(0, x, y, "", feature);
        this.corner = corner;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        float scale = AdvancedSkyblock.getInstance().getConfigValues().getGuiScale(feature);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        hovered = mouseX >= (x - 3) * scale && mouseY >= (y - 3) * scale && mouseX < (x + 3) * scale && mouseY < (y + 3) * scale;
        int color = hovered ? ChatFormatting.WHITE.getRGB() : ChatFormatting.WHITE.getColor(127).getRGB();
        drawRect(x - 3, y - 3, x + 3, y + 3, color);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }

    public int getLastMouseX() {
        return this.lastMouseX;
    }

    public int getLastMouseY() {
        return this.lastMouseY;
    }

    public Corner getCorner() {
        return this.corner;
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
