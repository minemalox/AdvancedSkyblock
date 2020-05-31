package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.EnumUtils;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonSocial extends GuiButton {

    private AdvancedSkyblock main;

    private EnumUtils.Social social;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSocial(double x, double y, AdvancedSkyblock main, EnumUtils.Social social) {
        super(0, (int) x, (int) y, "");
        this.main = main;
        this.width = 20;
        this.height = 20;
        this.social = social;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        float alphaMultiplier = 1F;
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / fadeMilis;
            }
        }

        hovered = mouseX >= x && mouseY >= y && mouseX < x +
                width && mouseY < y + height;
        GlStateManager.enableBlend();

        if (hovered) {
            GlStateManager.color(1, 1, 1, alphaMultiplier * 1);
        } else {
            GlStateManager.color(1, 1, 1, alphaMultiplier * 0.7F);
        }

        mc.getTextureManager().bindTexture(social.getResourceLocation());
        drawModalRectWithCustomSizedTexture(x,
                y, 0, 0, width, height, width, height);
    }


    public EnumUtils.Social getSocial() {
        return social;
    }
}
