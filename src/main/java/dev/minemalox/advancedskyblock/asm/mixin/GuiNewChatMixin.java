package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin {

    @Redirect(method = "printChatMessageWithOptionalDeletion", at = @At(value = "INVOKE", target = "net/minecraft/util/text/ITextComponent.getUnformattedText()Ljava/lang/String;"))
    public String getUnformattedText(ITextComponent iTextComponent) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        ICommandSender player = Minecraft.getMinecraft().player;

        if (main != null && (player != null && player.getName().equals("MineMalox"))) {
            return iTextComponent.getFormattedText().replace('§', '$'); // makes it easier for debugging
        }
        return iTextComponent.getUnformattedText();
    }


}
