package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    private static final ResourceLocation BLANK = new ResourceLocation("advancedskyblock", "blank.png");

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;)V",
            at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/RenderItem.renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void renderItem(ItemStack stack, IBakedModel model, CallbackInfo callbackInfo) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();

        if (main.getConfigValues().isEnabled(Feature.TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON) && main.getInventoryUtils().isUsingToxicArrowPoison()
                && Items.BOW.equals(stack.getItem()) && main.getUtils().itemIsInHotbar(stack)) {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(514);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(768, 1);
            textureManager.bindTexture(BLANK);
            GlStateManager.matrixMode(5890);

            GlStateManager.pushMatrix();

            Minecraft.getMinecraft().getRenderItem().renderModel(model, 0x201cba41);
            GlStateManager.popMatrix();

            GlStateManager.matrixMode(5888);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableLighting();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(true);
            textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        }
    }
}
