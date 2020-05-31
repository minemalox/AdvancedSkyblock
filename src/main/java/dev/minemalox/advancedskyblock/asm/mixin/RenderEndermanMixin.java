package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEnderman.class)
public abstract class RenderEndermanMixin {

    @Shadow
    public abstract ModelEnderman getMainModel();

    @Inject(
            method = "getEntityTexture",
            at = @At(
                    value = "HEAD"
            )
    )
    private void getEntityTexture(EntityEnderman entity, CallbackInfoReturnable<ResourceLocation> cir) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            if (this.getMainModel().isCarrying) {
                // Fixes Ender Chest rendering on 1.12.x clients

                ItemStack itemStack = new ItemStack(Blocks.ENDER_CHEST);
                if (Minecraft.getMinecraft().player.getDisplayNameString().equals("ffdks")) {
                    itemStack = new ItemStack(Blocks.GRASS);
                } else if (Minecraft.getMinecraft().player.getDisplayNameString().equals("Master16095")) {
                    itemStack = new ItemStack(Blocks.DIRT);
                }
                IBlockState iBlockState = Block.getBlockFromItem(itemStack.getItem()).getBlockState().getBaseState();
                entity.setHeldBlockState(iBlockState);
            }
        }
    }

}
