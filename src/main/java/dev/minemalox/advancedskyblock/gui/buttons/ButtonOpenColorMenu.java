package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static dev.minemalox.advancedskyblock.gui.AdvancedSkyblock.BUTTON_MAX_WIDTH;

public class ButtonOpenColorMenu extends ButtonText {

    private AdvancedSkyblock main;

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, AdvancedSkyblock main, Feature feature) {
        super(0, (int) x, (int) y, buttonText, feature);
        this.main = main;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int boxColor;
        int fontColor = new Color(224, 224, 224, 255).getRGB();
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
            fontColor = new Color(255, 255, 160, 255).getRGB();
        }
        boxColor = main.getConfigValues().getColor(feature, boxAlpha).getRGB();
        // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
        GlStateManager.enableBlend();
        float scale = 1;
        int stringWidth = MinecraftReflection.FontRenderer.getStringWidth(displayString);
        float widthLimit = BUTTON_MAX_WIDTH - 10;
        if (stringWidth > widthLimit) {
            scale = 1 / (stringWidth / widthLimit);
        }
        drawButtonBoxAndText(boxColor, scale, fontColor);
    }
}
