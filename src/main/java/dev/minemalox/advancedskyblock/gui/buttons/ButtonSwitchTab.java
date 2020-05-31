package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.EnumUtils;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonSwitchTab extends GuiButton {

    private AdvancedSkyblock main;
    private EnumUtils.GuiTab currentTab;
    private EnumUtils.GuiTab tab;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSwitchTab(double x, double y, int width, int height, String buttonText, AdvancedSkyblock main,
                           EnumUtils.GuiTab tab, EnumUtils.GuiTab currentTab) {
        super(0, (int) x, (int) y, width, height, buttonText);
        this.main = main;
        this.width = width;
        this.height = height;
        this.currentTab = currentTab;
        this.tab = tab;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }
            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            if (currentTab == tab) hovered = false;
            if (alphaMultiplier < 0.1) alphaMultiplier = 0.1F;
            int boxColor = main.getUtils().getDefaultBlue((int) (alphaMultiplier * 50));
            int fontColor;
            if (currentTab != tab) {
                fontColor = main.getUtils().getDefaultBlue((int) (alphaMultiplier * 255));
            } else {
                fontColor = main.getUtils().getDefaultBlue((int) (alphaMultiplier * 127));
            }
            if (hovered) {
                fontColor = new Color(255, 255, 160, (int) (alphaMultiplier * 255)).getRGB();
            }
            drawRect(x, y, x + width, y + height, boxColor);
            float scale = 1.4F;
            float scaleMultiplier = 1 / scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.enableBlend();
            MinecraftReflection.FontRenderer.drawCenteredString(displayString, (int) ((x + width / 2) * scaleMultiplier), (int) ((y + (this.height - (8 / scaleMultiplier)) / 2) * scaleMultiplier),
                    fontColor);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (currentTab != tab) super.playPressSound(soundHandlerIn);
    }

    public EnumUtils.GuiTab getTab() {
        return tab;
    }
}
