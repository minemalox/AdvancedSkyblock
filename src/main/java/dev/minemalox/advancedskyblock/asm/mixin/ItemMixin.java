package dev.minemalox.advancedskyblock.asm.mixin;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.CooldownManager;
import dev.minemalox.advancedskyblock.utils.Feature;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Redirect(method = "showDurabilityBar(Lnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.isItemDamaged()Z"))
    public boolean isItemDamaged(ItemStack itemStack) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            if (CooldownManager.isOnCooldown(itemStack)) {
                return true;
            }
        }
        return itemStack.isItemDamaged();
    }

    @Inject(method = "getDurabilityForDisplay(Lnet/minecraft/item/ItemStack;)D", at = @At("HEAD"), cancellable = true, remap = false)
    public void getDurabilityForDisplay(ItemStack stack, CallbackInfoReturnable<Double> callbackInfoReturnable) {
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            if (CooldownManager.isOnCooldown(stack)) {
                callbackInfoReturnable.setReturnValue(CooldownManager.getRemainingCooldownPercent(stack));
            }
        }
    }

}
