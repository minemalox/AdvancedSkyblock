package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonToggle extends ButtonFeature {

    private static final int CIRCLE_PADDING_LEFT = 5;
    private static final int ANIMATION_SLIDE_DISTANCE = 12;
    private static final int ANIMATION_SLIDE_TIME = 150;
    private static final ResourceLocation TOGGLE_INSIDE_CIRCLE = new ResourceLocation("advancedskyblock", "toggleinsidecircle.png");
    private static final ResourceLocation TOGGLE_BORDER = new ResourceLocation("advancedskyblock", "toggleborder.png");
    private static final ResourceLocation TOGGLE_INSIDE_BACKGROUND = new ResourceLocation("advancedskyblock", "toggleinsidebackground.png");
    private AdvancedSkyblock main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    private long animationButtonClicked = -1;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonToggle(double x, double y, AdvancedSkyblock main, Feature feature) {
        super(0, (int) x, (int) y, "", feature);
        this.main = main;
        this.feature = feature;
        this.width = 31;
        this.height = 15;
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
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, alphaMultiplier * 0.7F);
        if (hovered) {
            GlStateManager.color(1, 1, 1, 1);
        }

        main.getUtils().bindRGBColor(0xFF1e252e);
        mc.getTextureManager().bindTexture(TOGGLE_BORDER);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        boolean enabled = main.getConfigValues().isEnabled(feature);
        boolean remoteDisabled = main.getConfigValues().isRemoteDisabled(feature);

        if (enabled) {
            main.getUtils().bindColorInts(36, 255, 98, remoteDisabled ? 25 : 255); // Green
        } else {
            main.getUtils().bindColorInts(222, 68, 76, remoteDisabled ? 25 : 255); // Red
        }

        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_BACKGROUND);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        int startingX = getStartingPosition(enabled);
        int slideAnimationOffset = 0;

        if (animationButtonClicked != -1) {
            startingX = getStartingPosition(!enabled); // They toggled so start from the opposite side.

            int timeSinceOpen = (int) (System.currentTimeMillis() - animationButtonClicked);
            int animationTime = ANIMATION_SLIDE_TIME;
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime;
            }

            slideAnimationOffset = ANIMATION_SLIDE_DISTANCE * timeSinceOpen / animationTime;
        }

        startingX += enabled ? slideAnimationOffset : -slideAnimationOffset;

        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE);
        drawModalRectWithCustomSizedTexture(startingX, y + 3, 0, 0, 9, 9, 9, 9);
    }

    private int getStartingPosition(boolean enabled) {
        if (!enabled) {
            return x + CIRCLE_PADDING_LEFT;
        } else {
            return getStartingPosition(false) + ANIMATION_SLIDE_DISTANCE;
        }
    }

    public void onClick() {
        this.animationButtonClicked = System.currentTimeMillis();
    }
}
