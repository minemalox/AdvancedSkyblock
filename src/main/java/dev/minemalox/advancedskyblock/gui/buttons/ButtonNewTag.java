package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

public class ButtonNewTag extends GuiButton {

    public ButtonNewTag(int x, int y) {
        super(0, x, y, "NEW");

        width = 25;
        height = 11;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        drawRect(x, y, x + width, y + height, ChatFormatting.RED.getRGB());
        drawString(Minecraft.getMinecraft().fontRenderer, displayString, x + 4, y + 2, ChatFormatting.WHITE.getRGB());
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }
}
