package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Message;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

import java.math.BigDecimal;

public class ButtonGuiScale extends ButtonFeature {

    private float sliderValue;
    private boolean dragging;

    private AdvancedSkyblock main;

    public ButtonGuiScale(double x, double y, int width, int height, AdvancedSkyblock main, Feature feature) {
        super(0, (int) x, (int) y, "", feature);
        this.sliderValue = main.getConfigValues().getGuiScale(feature, false);
        this.displayString = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getGuiScale(feature))));
        this.main = main;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
        }
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, main.getUtils().getDefaultColor(boxAlpha));
        this.mouseDragged(mc, mouseX, mouseY);
        int j = 14737632;
        if (packedFGColour != 0) {
            j = packedFGColour;
        } else if (!this.enabled) {
            j = 10526880;
        } else if (this.hovered) {
            j = 16777120;
        }
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
                main.getConfigValues().setGuiScale(feature, sliderValue);
                this.displayString = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getGuiScale(feature))));
            }

            mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 1, this.y, this.x + (int) (this.sliderValue * (float) (this.width - 8)) + 7, this.y + this.height, ChatFormatting.GRAY.getRGB());
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.x + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0F, 1.0F);
            main.getConfigValues().setGuiScale(feature, sliderValue);
            this.displayString = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getGuiScale(feature))));
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}

