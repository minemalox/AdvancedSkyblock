package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonModify extends ButtonText {

    private AdvancedSkyblock main;

    private Feature feature;

    /**
     * Create a button for adding or subtracting a number.
     */
    public ButtonModify(double x, double y, int width, int height, String buttonText, AdvancedSkyblock main, Feature feature) {
        super(0, (int) x, (int) y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int boxColor;
        int boxAlpha = 100;
        if (hovered && !hitMaximum()) {
            boxAlpha = 170;
        }
        if (hitMaximum()) {
            boxColor = ChatFormatting.GRAY.getColor(boxAlpha).getRGB();
        } else {
            if (feature == Feature.ADD) {
                boxColor = ChatFormatting.GREEN.getColor(boxAlpha).getRGB();
            } else {
                boxColor = ChatFormatting.RED.getColor(boxAlpha).getRGB();
            }
        }
        GlStateManager.enableBlend();
        int fontColor = new Color(224, 224, 224, 255).getRGB();
        if (hovered && !hitMaximum()) {
            fontColor = new Color(255, 255, 160, 255).getRGB();
        }
        drawButtonBoxAndText(boxColor, 1, fontColor);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!hitMaximum()) {
            super.playPressSound(soundHandlerIn);
        }
    }

    private boolean hitMaximum() {
        return (feature == Feature.SUBTRACT && main.getConfigValues().getWarningSeconds() == 1) ||
                (feature == Feature.ADD && main.getConfigValues().getWarningSeconds() == 99);
    }
}
