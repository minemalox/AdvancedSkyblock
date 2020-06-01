package dev.minemalox.advancedskyblock.gui;

import dev.minemalox.advancedskyblock.gui.buttons.ButtonColorBox;
import dev.minemalox.advancedskyblock.gui.buttons.ButtonSlider;
import dev.minemalox.advancedskyblock.gui.elements.CheckBox;
import dev.minemalox.advancedskyblock.utils.ChromaManager;
import dev.minemalox.advancedskyblock.utils.EnumUtils;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Message;
import dev.minemalox.advancedskyblock.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ColorSelectionGui extends GuiScreen {

    private static final ResourceLocation COLOR_PICKER = new ResourceLocation("advancedskyblock", "colorpicker.png");
    private BufferedImage COLOR_PICKER_IMAGE;

    // The feature that this color is for.
    private Feature feature;

    // Previous pages for when they return.
    private EnumUtils.GUIType lastGUI;
    private EnumUtils.GuiTab lastTab;
    private int lastPage;

    private int imageX;
    private int imageY;

    private GuiTextField hexColorField;

    private CheckBox chromaCheckbox;

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature  The feature that this color is for.
     * @param lastTab  The previous tab that you came from.
     * @param lastPage The previous page.
     */
    ColorSelectionGui(Feature feature, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        this.feature = feature;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;

        try {
            COLOR_PICKER_IMAGE = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(COLOR_PICKER).getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws the default text at the top at bottoms of the GUI.
     *
     * @param gui The gui to draw the text on.
     */
    public void drawDefaultTitleText(GuiScreen gui, int alpha) {
        int defaultBlue = dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getUtils().getDefaultBlue(alpha);

        drawScaledString(gui, "AdvancedSkyblockGui", 28, defaultBlue, 2.5F, 0);
        drawScaledString(gui, "v" + dev.minemalox.advancedskyblock.AdvancedSkyblock.VERSION + " by Biscut", 49, defaultBlue, 1.3, 50);

        if (gui instanceof AdvancedSkyblockGui) {
            drawScaledString(gui, "Special Credits: InventiveTalent - Magma Boss Timer API", gui.height - 22, defaultBlue, 1, 0);
        }
    }

    /**
     * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
     *
     * @param text    The text to draw.
     * @param y       The y level to draw the text/
     * @param color   The text color.
     * @param scale   The scale to draw the text.
     * @param xOffset The offset from the center x that the text should be drawn at.
     */
    public void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        drawCenteredString(Minecraft.getMinecraft().fontRenderer, text, (int) Math.round((float) guiScreen.width / 2 / scale) + xOffset, (int) Math.round((float) y / scale), color);
        GlStateManager.popMatrix();
    }

    @Override
    public void initGui() {
        chromaCheckbox = new CheckBox(mc, width / 2 + 88, 170, 12, "Chroma", false);
        chromaCheckbox.setValue(dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature));

        chromaCheckbox.setOnToggleListener(value -> {
            ChromaManager.setFeature(feature, value);
            ColorSelectionGui.this.removeChromaButtons();
            if (value) {
                ColorSelectionGui.this.addChromaButtons();
            }
        });

        hexColorField = new GuiTextField(0, fontRenderer,
                width / 2 + 110 - 50, 220, 100, 15);
        hexColorField.setMaxStringLength(7);
        hexColorField.setFocused(true);

        // Set the current color in the text box after creating it.
        setTextBoxHex(dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getColor(feature));

        if (feature.getGuiFeatureData().isColorsRestricted()) {

            // This creates the 16 buttons for all the color codes.

            int collumn = 1;
            int x = width / 2 - 160;
            int y = 120;

            for (ChatFormatting chatFormatting : ChatFormatting.values()) {
                if (chatFormatting.isFormat() || chatFormatting == ChatFormatting.RESET) continue;

                buttonList.add(new ButtonColorBox(x, y, chatFormatting));

                if (collumn < 6) { // 6 buttons per row.
                    collumn++; // Go to the next collumn once the 6 are over.
                    x += ButtonColorBox.WIDTH + 15; // 15 spacing.
                } else {
                    y += ButtonColorBox.HEIGHT + 20; // Go to next row.
                    collumn = 1; // Reset the collumn.
                    x = width / 2 - 160; // Reset the x vlue.
                }
            }
        }

        if (dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
            ColorSelectionGui.this.addChromaButtons();
        }

        Keyboard.enableRepeatEvents(true);

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Draw background and default text.
        int startColor = new Color(0, 0, 0, 128).getRGB();
        int endColor = new Color(0, 0, 0, 192).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        drawDefaultTitleText(this, 255);

        int defaultBlue = dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getUtils().getDefaultBlue(255);

        if (feature.getGuiFeatureData() != null) {
            if (feature.getGuiFeatureData().isColorsRestricted()) {
                drawScaledString(this, Message.MESSAGE_CHOOSE_A_COLOR.getMessage(), 90,
                        defaultBlue, 1.5, 0);

            } else {
                int pickerWidth = COLOR_PICKER_IMAGE.getWidth();
                int pickerHeight = COLOR_PICKER_IMAGE.getHeight();

                imageX = width / 2 - 200;
                imageY = 90;

                if (dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature)) { // Fade out color picker if chroma enabled
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 0.7F);
                    GlStateManager.enableBlend();
                }

                // Draw the color picker with no scaling so the size is the exact same.
                mc.getTextureManager().bindTexture(COLOR_PICKER);
                Gui.drawModalRectWithCustomSizedTexture(imageX, imageY, 0, 0, pickerWidth, pickerHeight, pickerWidth, pickerHeight);

                drawScaledString(this, Message.MESSAGE_SELECTED_COLOR.getMessage(), 120, defaultBlue, 1.5, 75);
                drawRect(width / 2 + 90, 140, width / 2 + 130, 160, dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getColor(feature).getRGB());

                if (chromaCheckbox != null) chromaCheckbox.draw();

                if (!dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature)) { // Disabled cause chroma is enabled
                    drawScaledString(this, Message.MESSAGE_SET_HEX_COLOR.getMessage(), 200, defaultBlue, 1.5, 75);
                    hexColorField.drawTextBox();
                }

                if (dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
                    drawScaledString(this, Message.SETTING_CHROMA_SPEED.getMessage(), 170 + 25, defaultBlue, 1, 110);

                    drawScaledString(this, Message.SETTING_CHROMA_FADE_WIDTH.getMessage(), 170 + 35 + 25, defaultBlue, 1, 110);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!feature.getGuiFeatureData().isColorsRestricted() && !dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
            int xPixel = mouseX - imageX;
            int yPixel = mouseY - imageY;

            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE.getWidth() &&
                    yPixel > 0 && yPixel < COLOR_PICKER_IMAGE.getHeight()) {

                // Get the color of the clicked pixel.
                Color selectedColor = new Color(COLOR_PICKER_IMAGE.getRGB(xPixel, yPixel), true);

                // Choose this color.
                if (selectedColor.getAlpha() == 255) {
                    dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().setColor(feature, selectedColor.getRGB());
                    setTextBoxHex(selectedColor);

                    dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getUtils().playSound(SoundEvents.UI_BUTTON_CLICK, 0.25, 1);
                }
            }

            hexColorField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (chromaCheckbox != null) chromaCheckbox.onMouseClick(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setTextBoxHex(Color color) {
        hexColorField.setText(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (hexColorField.isFocused()) {
            hexColorField.textboxKeyTyped(typedChar, keyCode);

            String text = hexColorField.getText();
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1);
            }

            if (text.length() == 6) {
                int typedColor;
                try {
                    typedColor = Integer.parseInt(text, 16); // Try to read the hex value and put it in an integer.
                } catch (NumberFormatException ex) {
                    ex.printStackTrace(); // This just means it wasn't in the format of a hex number- that's fine!
                    return;
                }

                dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().setColor(feature, typedColor);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ButtonColorBox) {
            ButtonColorBox colorBox = (ButtonColorBox) button;
            dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getConfigValues().setColor(feature, colorBox.getColor().getRGB());
            this.mc.displayGuiScreen(null);
        }

        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        hexColorField.updateCursorCounter();

        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance().getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, feature);
    }

    private void removeChromaButtons() {
        this.buttonList.removeIf(button -> button instanceof ButtonSlider);
    }

    private void addChromaButtons() {
        dev.minemalox.advancedskyblock.AdvancedSkyblock main = dev.minemalox.advancedskyblock.AdvancedSkyblock.getInstance();
        buttonList.add(new ButtonSlider(width / 2 + 76, 170 + 35, 70, 15, main, main.getConfigValues().getChromaSpeed(),
                0.1F, 10, 0.5F, new ButtonSlider.OnSliderChangeCallback() {
            @Override
            public void sliderUpdated(float value) {
                main.getConfigValues().setChromaSpeed(value);
            }
        }));

        buttonList.add(new ButtonSlider(width / 2 + 76, 170 + 35 + 35, 70, 15, main, main.getConfigValues().getChromaFadeWidth(),
                1, 42, 1, new ButtonSlider.OnSliderChangeCallback() {
            @Override
            public void sliderUpdated(float value) {
                main.getConfigValues().setChromaFadeWidth(value);
            }
        }));
    }
}
