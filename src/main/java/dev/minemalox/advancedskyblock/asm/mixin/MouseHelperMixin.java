package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHelper.class)
public class MouseHelperMixin {

    @Redirect(method = "ungrabMouseCursor()V", at = @At(value = "INVOKE", target = "org/lwjgl/input/Mouse.setCursorPosition(II)V"))
    private void ungrabMouseCursor(int x, int y) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getConfigValues().isDisabled(Feature.DONT_RESET_CURSOR_INVENTORY) || main.getPlayerListener().shouldResetMouse()) {
            Mouse.setCursorPosition(x, y);
        }
    }

}
