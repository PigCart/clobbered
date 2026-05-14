package pigcart.clobbered.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerAccessor {

    @Invoker
    void callDamageStatsAndHearts(final Entity entity, final float oldLivingEntityHealth);

    @Invoker
    void callItemAttackInteraction(final Entity entity, final ItemStack attackingItemStack, final DamageSource damageSource, final boolean applyToTarget);
}
