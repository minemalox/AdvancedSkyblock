package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.discord.DiscordRPCManager;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public class GuiDisconnectedMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(GuiScreen screen, String reasonLocalizationKey, ITextComponent chatComp, CallbackInfo callbackInfo) {
        DiscordRPCManager discordRPCManager = AdvancedSkyblock.getInstance().getDiscordRPCManager();
        if (discordRPCManager.isActive()) {
            discordRPCManager.stop();
        }
    }

}
