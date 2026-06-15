package pigcart.clobbered.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import pigcart.clobbered.Clobbered;
import pigcart.clobbered.LobbedItem;
import pigcart.clobbered.PlayerDropExtension;

import static pigcart.clobbered.config.ConfigManager.config;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerDropExtension {

    @Shadow
    public abstract void awardStat(Stat<?> stat, int count);

    @Unique
    int lobStrength;

    @Override
    public void clobbered$setLobStrength(int i) {
        lobStrength = i;
    }

    @WrapOperation(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemEntity wrapDrop(ServerPlayer player, ItemStack itemStack, boolean randomly, boolean thrownFromHand, Operation<ItemEntity> original) {
        boolean hurled = lobStrength == config.maximumDropStrength;
        final ServerLevel level = player.level();
        if (itemStack.is(Clobbered.CONSUMED) && hurled) {
            itemStack.use(level, player, InteractionHand.MAIN_HAND);
            return null;
        }
        if (itemStack.isEmpty()
                || itemStack.is(Clobbered.BLACKLISTED)
                || (config.useWhitelist && !config.playerWhitelist.contains(player.getPlainTextName()))
                || (config.useBlacklist && config.playerBlacklist.contains(player.getPlainTextName()))
        ) {
            return original.call(player, itemStack, randomly, thrownFromHand);
        }
        if (thrownFromHand) {
            this.awardStat(Stats.ITEM_DROPPED.get(itemStack.getItem()), itemStack.getCount());
            this.awardStat(Stats.CUSTOM.get(Stats.DROP), 1);
        }

        float strength = Math.clamp(lobStrength, config.minimumDropStrength, config.maximumDropStrength) / 20F;
        double yHandPos = player.getEyeY() - 0.3;
        RandomSource rand = level.getRandom();
        Vec3 velocity = player.getHeadLookAngle().normalize();
        if (hurled) {
            player.causeFoodExhaustion(0.1F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (rand.nextFloat() * 0.4F + 0.8F));
        } else {
            velocity = velocity.add(
                    rand.nextFloat() * 0.1F - 0.05F,
                    rand.nextFloat() * 0.1F - 0.05F,
                    rand.nextFloat() * 0.1F - 0.05F);
        }
        LobbedItem lobbedItem = new LobbedItem(level,
                player.getX(), yHandPos, player.getZ(),
                velocity.multiply(strength, strength, strength).add(player.getKnownMovement()),
                hurled,
                itemStack,
                player);
        level.addFreshEntity(lobbedItem);
        return null; // return value unused by minecraft
    }
}
