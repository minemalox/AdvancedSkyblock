package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.renderer.GlStateManager;

class ButtonText extends ButtonFeature {

    /**
     * Create a button that displays text.
     */
    ButtonText(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText, feature);
    }

    void drawButtonBoxAndText(int boxColor, float scale, int fontColor) {
        drawRect(x, y, x + this.width, y + this.height, boxColor);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        MinecraftReflection.FontRenderer.drawCenteredString(displayString, ((x + width / 2) / scale), ((y + (this.height - (8 * scale)) / 2) / scale), fontColor);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
