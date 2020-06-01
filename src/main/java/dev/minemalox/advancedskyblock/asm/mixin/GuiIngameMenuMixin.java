package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.EnumUtils;
import dev.minemalox.advancedskyblock.utils.Feature;
import dev.minemalox.advancedskyblock.utils.discord.DiscordRPCManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiIngameMenu.class)
public class GuiIngameMenuMixin extends GuiScreen {

    @Inject(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isIntegratedServerRunning()Z", shift = At.Shift.BEFORE))
    protected void actionPerformed(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        DiscordRPCManager discordRPCManager = AdvancedSkyblock.getInstance().getDiscordRPCManager();
        if (discordRPCManager.isActive()) {
            discordRPCManager.stop();
        }
    }

    @Inject(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", at = @At("HEAD"))
    protected void actionPerformed2(GuiButton button, CallbackInfo callbackInfo) throws IOException {
        if (button.id == 53) {
            AdvancedSkyblock advancedSkyblock = AdvancedSkyblock.getInstance();
            advancedSkyblock.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    public void initGui(CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.ADVANCED_SKYBLOCK_BUTTON_IN_PAUSE_MENU)) {
            buttonList.add(new GuiButton(53, width - 120 - 5, height - 20 - 5, 120, 20, "AdvancedSkyblockGui Menu"));
        }
    }

}
