package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;

public class ButtonToggleTitle extends ButtonToggle {

    private AdvancedSkyblock main;

    public ButtonToggleTitle(double x, double y, String buttonText, AdvancedSkyblock main, Feature feature) {
        super(x, y, main, feature);
        displayString = buttonText;
        this.main = main;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);
        int fontColor = main.getUtils().getDefaultBlue(255);
        MinecraftReflection.FontRenderer.drawCenteredString(displayString, x + width / 2, y - 10, fontColor);
    }
}
