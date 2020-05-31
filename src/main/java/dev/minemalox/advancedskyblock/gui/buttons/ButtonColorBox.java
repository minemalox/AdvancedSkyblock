package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
public class ButtonColorBox extends GuiButton {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    private ChatFormatting color;

    public ButtonColorBox(int x, int y, ChatFormatting color) {
        super(0, x, y, null);

        this.width = 40;
        this.height = 20;

        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;

        if (hovered) {
            drawRect(x, y, x + width, y + height, color.getRGB());
        } else {
            drawRect(x, y, x + width, y + height, color.getColor(127).getRGB());
        }
    }

    public ChatFormatting getColor() {
        return color;
    }
}
