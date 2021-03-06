package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static dev.minemalox.advancedskyblock.gui.AdvancedSkyblockGui.BUTTON_MAX_WIDTH;

public class ButtonSolid extends ButtonText {

    private AdvancedSkyblock main;

    private Feature feature;
    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid(double x, double y, int width, int height, String buttonText, AdvancedSkyblock main, Feature feature) {
        super(0, (int) x, (int) y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (feature == Feature.TEXT_STYLE) {
            displayString = main.getConfigValues().getTextStyle().getMessage();
        } else if (feature == Feature.CHROMA_MODE) {
            displayString = main.getConfigValues().getChromaMode().getMessage();
        } else if (feature == Feature.WARNING_TIME) {
            displayString = main.getConfigValues().getWarningSeconds() + "s";
        }
        int alpha;
        float alphaMultiplier = 1F;
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / fadeMilis;
            }
            alpha = (int) (255 * alphaMultiplier);
        } else {
            alpha = 255;
        }
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int boxAlpha = 100;
        if (hovered && feature != Feature.WARNING_TIME) boxAlpha = 170;
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        boxAlpha *= alphaMultiplier;
        int boxColor = main.getUtils().getDefaultColor(boxAlpha);
        if (this.feature == Feature.RESET_LOCATION) {
            boxColor = main.getUtils().getColorWithAlpha(0xFF7878, boxAlpha);
        }
        GlStateManager.enableBlend();
        if (alpha < 4) alpha = 4;
        int fontColor = new Color(224, 224, 224, alpha).getRGB();
        if (hovered && feature != Feature.WARNING_TIME) {
            fontColor = new Color(255, 255, 160, alpha).getRGB();
        }
        float scale = 1;
        int stringWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString);
        float widthLimit = BUTTON_MAX_WIDTH - 10;
        if (feature == Feature.WARNING_TIME) {
            widthLimit = 90;
        }
        if (stringWidth > widthLimit) {
            scale = 1 / (stringWidth / widthLimit);
        }
        drawButtonBoxAndText(boxColor, scale, fontColor);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (feature != Feature.WARNING_TIME) super.playPressSound(soundHandlerIn);
    }
}
