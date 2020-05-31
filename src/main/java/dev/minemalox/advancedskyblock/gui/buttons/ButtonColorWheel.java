package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonColorWheel extends ButtonFeature {

    private static final ResourceLocation COLOR_WHEEL = new ResourceLocation("advancedskyblock", "colorwheel.png");
    private static final int SIZE = 10;

    public ButtonColorWheel(int x, int y, Feature feature) {
        super(0, x, y, "", feature);
        width = SIZE;
        height = SIZE;
    }

    public static int getSize() {
        return SIZE;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        float scale = AdvancedSkyblock.getInstance().getConfigValues().getGuiScale(feature);
        this.hovered = mouseX >= this.x * scale && mouseY >= this.y * scale &&
                mouseX < this.x * scale + this.width * scale && mouseY < this.y * scale + this.height * scale;
        GlStateManager.color(1, 1, 1, hovered ? 1 : 0.5F);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(COLOR_WHEEL);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 10, 10, 10, 10);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        float scale = AdvancedSkyblock.getInstance().getConfigValues().getGuiScale(feature);
        return mouseX >= this.x * scale && mouseY >= this.y * scale &&
                mouseX < this.x * scale + this.width * scale && mouseY < this.y * scale + this.height * scale;
    }
}
