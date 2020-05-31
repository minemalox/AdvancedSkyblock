package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundManager.class)
public abstract class SoundManagerMixin {

    @Shadow
    public abstract float getClampedVolume(ISound sound);

    // Modifying master volume for a single sound is very complex, don't do it.

    // This makes sure our warning plays at full volume (only affected by master volume switch).
    @Redirect(
            method = "playSound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/audio/SoundManager;getClampedVolume(Lnet/minecraft/client/audio/ISound;)F",
                    ordinal = 0
            )
    )
    private float getNormalizedVolumeBypass(SoundManager soundManager, ISound sound) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main != null && main.getUtils() != null && main.getUtils().isPlayingSound()) {
            return 1;
        } else if (sound.getSound().getSoundLocation().equals(SoundEvents.ENTITY_ENDERMEN_DEATH.getSoundName())) {
            return Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.HOSTILE);
        } else {
            return getClampedVolume(sound);
        }
    }
}
