package dev.minemalox.advancedskyblock.gui.buttons;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ButtonBanner extends GuiButton {

    private static final int WIDTH = 130;
    private static ResourceLocation banner = null;
    private static BufferedImage bannerImage = null;

    private static boolean grabbedBanner = false;
    private AdvancedSkyblock main;
    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonBanner(double x, double y, AdvancedSkyblock main) {
        super(0, (int) x, (int) y, "");
        this.main = main;

        if (!grabbedBanner) {
            grabbedBanner = true;
            bannerImage = null;
            banner = null;

            new Thread(() -> {
                try {
                    URL url = new URL("https://github.com/MineMalox/AdvancedSkyblock/blob/1.12/src/main/resources/assets/skyblockaddons/featuredbanner.png?raw=true");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(5000);
                    connection.addRequestProperty("User-Agent", Utils.USER_AGENT);

                    bannerImage = TextureUtil.readBufferedImage(connection.getInputStream());

                    connection.disconnect();

                    this.width = bannerImage.getWidth();
                    this.height = bannerImage.getHeight();
                } catch (IOException ex) {
                    FMLLog.info("[AdvancedSkyblockGui] Couldn't grab main menu banner image from URL, falling back to local banner.");

                    banner = new ResourceLocation("advancedskyblock", "featuredbanner.png");
                    try {
                        bannerImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(banner).getInputStream());

                        this.width = bannerImage.getWidth();
                        this.height = bannerImage.getHeight();
                    } catch (IOException ex1) {
                        ex1.printStackTrace();
                    }
                }
            }).start();
        }

        x -= WIDTH / 2;

        if (bannerImage != null) {
            this.width = bannerImage.getWidth();
            this.height = bannerImage.getHeight();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (bannerImage != null && banner == null) { // This means it was just loaded from the URL above.
            banner = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("banner", new DynamicTexture(bannerImage));
        }

        if (banner != null) { // Could have not been loaded yet.
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }

            float scale = (float) WIDTH / bannerImage.getWidth(); // max width

            hovered = mouseX >= x && mouseY >= y && mouseX < x +
                    WIDTH && mouseY < y + bannerImage.getHeight() * scale;
            GlStateManager.enableBlend();

            if (hovered) {
                GlStateManager.color(1, 1, 1, alphaMultiplier * 1);
            } else {
                GlStateManager.color(1, 1, 1, alphaMultiplier * 0.8F);
            }

            mc.getTextureManager().bindTexture(banner);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            drawModalRectWithCustomSizedTexture(Math.round(x / scale),
                    Math.round(y / scale), 0, 0, width, height, width, height);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }
}
