package dev.minemalox.advancedskyblock.asm.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSpawnMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "handleSpawnMob", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", target = "net/minecraft/network/play/server/SPacketSpawnMob.getDataManagerEntries()Ljava/util/List;", shift = At.Shift.BY, by = 2))
    public void handleSpawnMob(SPacketSpawnMob packetIn, CallbackInfo ci, double d0, double d1, double d2, float f, float f1, EntityLivingBase entitylivingbase, Entity[] aentity, List<EntityDataManager.DataEntry<?>> list) {

        /*if (entitylivingbase instanceof EntityEnderman) {
            EntityEnderman enderman = (EntityEnderman) entitylivingbase;
            /*float health = 0.0f;
            if (list.get(7) != null) {

                health = Float.parseFloat(list.get(7).getValue().toString());
            }

            entitylivingbase.setCustomNameTag(String.valueOf(health));

            float health = 0.0f;

            for (EntityDataManager.DataEntry<?> test :
                    list) {

                if (test.getKey().getId() == 7) {
                    health = Float.parseFloat(test.getValue().toString());
                }
                // System.out.println("[DEBUG] " + entitylivingbase.getName() + " | " + test.getKey().getId() + " getValue: " + test.getValue());
            }

            entitylivingbase.setCustomNameTag(String.valueOf(health));

            System.out.println("[DEBUG] " + entitylivingbase.getName() + " | " + health + " getValue: " + enderman.getCustomNameTag());

        }

*/
    }

}
