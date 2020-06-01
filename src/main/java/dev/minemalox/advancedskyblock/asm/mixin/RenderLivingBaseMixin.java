package dev.minemalox.advancedskyblock.asm.mixin;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.List;

@Mixin(RenderLivingBase.class)
public class RenderLivingBaseMixin {


    // net.minecraft.client.renderer.entity.Render - L374 - deadmau5

    @Redirect(method = "applyRotations", at = @At(value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean applyRotationsEquals(String name, Object obj) {
        List<String> coolPeople = Arrays.asList("Dinnerbone", "MineMalox", "ffdks", "Master16095");
        return coolPeople.contains(obj);
    }

    @Redirect(method = "applyRotations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", ordinal = 0))
    private boolean applyRotationsIsWearing(EntityPlayer entityPlayer, EnumPlayerModelParts parts) {
        List<String> coolPeople = Arrays.asList("Dinnerbone", "MineMalox", "ffdks", "Master16095");
        return (coolPeople.contains(entityPlayer.getDisplayNameString()) /*&& !entityPlayer.isWearing(parts)*/);
    }

}
