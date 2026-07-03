package pigcart.clobbered;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import pigcart.clobbered.config.ConfigData;
import pigcart.clobbered.mixin.PlayerAccessor;

import java.util.Optional;
import java.util.UUID;

import static pigcart.clobbered.config.ConfigManager.config;

public class LobbedItem extends AbstractArrow {

    public Entity impaledEntity;
    public UUID impaledEntityUUID;
    public int timesSkippedOnWater = 0;
    public boolean boomerangReturning = false;
    public Vec3 boomerangReturnPos;

    public static final EntityDataAccessor<String> IMPALED_ENTITY = defineData(EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Vector3fc> IMPALE_OFFSET = defineData(EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Float> IMPALE_ROT_X = defineData(EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> IMPALE_ROT_Y = defineData(EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> HURLED = defineData(EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> BOOMERANG = defineData(EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> RENDERED_ITEM = defineData(EntityDataSerializers.ITEM_STACK);

    private static <T> EntityDataAccessor<T> defineData(EntityDataSerializer<T> type) {
        return SynchedEntityData.defineId(LobbedItem.class, type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IMPALED_ENTITY, "");
        builder.define(IMPALE_OFFSET, new Vector3f());
        builder.define(HURLED, false);
        builder.define(BOOMERANG, false);
        builder.define(IMPALE_ROT_X, 0F);
        builder.define(IMPALE_ROT_Y, 0F);
        builder.define(RENDERED_ITEM, getDefaultPickupItem());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        final String stringUUID = entityData.get(IMPALED_ENTITY);
        if (stringUUID.isEmpty()) {
            impaledEntityUUID = null;
            impaledEntity = null;
        } else {
            impaledEntityUUID = UUID.fromString(stringUUID);
            impaledEntity = level().getEntity(impaledEntityUUID);
        }
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Hurled", Codec.BOOL, isHurled());
        output.store("InEntity", Codec.STRING, impaledEntity == null ? "" : impaledEntity.getStringUUID());
        output.store("impaleRotY", Codec.FLOAT, entityData.get(IMPALE_ROT_Y));
        output.store("impaleRotX", Codec.FLOAT, entityData.get(IMPALE_ROT_X));
        Vector3fc impaleOff = entityData.get(IMPALE_OFFSET);
        output.store("impaleOffX", Codec.FLOAT, impaleOff.x());
        output.store("impaleOffY", Codec.FLOAT, impaleOff.y());
        output.store("impaleOffZ", Codec.FLOAT, impaleOff.z());
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        setHurled(input.read("Hurled", Codec.BOOL).orElse(false));
        entityData.set(IMPALE_OFFSET, new Vector3f(
                input.read("impaleOffX", Codec.FLOAT).orElse(0F),
                input.read("impaleOffY", Codec.FLOAT).orElse(0F),
                input.read("impaleOffZ", Codec.FLOAT).orElse(0F)
        ));
        entityData.set(IMPALE_ROT_X, input.read("impaleRotX", Codec.FLOAT).orElse(0F));
        entityData.set(IMPALE_ROT_Y, input.read("impaleRotY", Codec.FLOAT).orElse(0F));
        final String stringUUID = input.read("InEntity", Codec.STRING).orElse("");
        if (!stringUUID.isEmpty()) impaledEntityUUID = UUID.fromString(stringUUID);
        entityData.set(IMPALED_ENTITY, stringUUID);
        // level does not contain entities at this point, entity will be gotten when first queried
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return Items.BARRIER.getDefaultInstance();
    }
    public ItemStack getRenderItemStack() { return this.entityData.get(RENDERED_ITEM); }
    public boolean isImpaling() { return isInGround() || isInEntity(); }
    public boolean isInEntity() { return impaledEntityUUID != null; }
    public void setInEntity(Entity entity) {
        impaledEntity = entity;
        impaledEntityUUID = entity.getUUID();
        entityData.set(IMPALED_ENTITY, entity.getStringUUID());
    }
    public Entity getInEntity() {
        // fetch entity from level if we've just loaded in
        if (impaledEntity == null) impaledEntity = level().getEntity(impaledEntityUUID);
        return impaledEntity;
    }
    public Vec3 getImpaleOffset() { return new Vec3(entityData.get(IMPALE_OFFSET)); }
    public void setImpaleOffset(Vec3 impalePos, Vec3 entityPos, float entityRot) {
        Vec3 localImpalePos = impalePos.subtract(entityPos);
        Vec3 impaleOffset = Vec3.applyLocalCoordinatesToRotation(new Vec2(0, -entityRot), localImpalePos);
        this.entityData.set(IMPALE_OFFSET, impaleOffset.toVector3f());
    }
    public boolean isHurled() { return this.entityData.get(HURLED); }
    public void setHurled(boolean value) { this.entityData.set(HURLED, value); }
    public boolean isBoomerang() { return this.entityData.get(BOOMERANG); }
    public void setBoomerang(boolean value) { this.entityData.set(BOOMERANG, value); }

    public LobbedItem(EntityType<LobbedItem> type, Level level) { super(type, level); }
    public LobbedItem(Level level, double x, double y, double z, Vec3 velocity, boolean hurled, ItemStack itemStack, Entity owner) {
        this(Clobbered.LOBBED_ITEM, level);
        this.setPos(x, y, z);
        this.setDeltaMovement(velocity);
        this.setPickupItemStack(itemStack);
        setHurled(hurled);
        this.setOwner(owner);
        this.setSoundEvent(SoundEvents.EMPTY);
        setBoomerang(itemStack.is(Clobbered.BOOMERANG));
        this.boomerangReturning = isBoomerang();
        this.boomerangReturnPos = new Vec3(x, y, z);
        if (boomerangReturning && hurled) this.setNoGravity(true);
    }

    @Override
    protected void setPickupItemStack(ItemStack itemStack) {
        this.entityData.set(RENDERED_ITEM, itemStack);
        super.setPickupItemStack(itemStack);
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (config.itemPickUp == ConfigData.PickUpMethod.AUTO) {
            return player.getInventory().add(this.getPickupItem());
        } else {
            return false;
        }
    }

    @Override
    public float getPickRadius() {
        return config.interactionRadius;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isImpaling() && isHurled()) {
            // vanilla arrow already does this but in a weird inverted way
            Vec2 rot = this.getDeltaMovement().rotation();
            this.setYRot(rot.y);
            this.setXRot(rot.x);
            // same as trident loyalty enchantment
            if (boomerangReturning) {
                int loyalty = 1;
                Vec3 vec = boomerangReturnPos.subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec.y * 0.015 * loyalty, this.getZ());
                double accel = 0.05 * loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec.normalize().scale(accel)));
                if (this.blockPosition().equals(BlockPos.containing(boomerangReturnPos))) {
                    boomerangReturning = false;
                    this.setNoGravity(false);
                }
            }
        }

        if (this.level().isClientSide()) {
            // attempt to re attach to entity after it becomes unloaded on the client
            if (impaledEntity != null
                    && impaledEntity.isRemoved()
                    && impaledEntity.getRemovalReason() == RemovalReason.DISCARDED
            ) {
                impaledEntity = level().getEntity(impaledEntityUUID);
            }
            return;
        }
        // apply impale offset on the server
        if (isInEntity()) {
            Entity inEntity = getInEntity();
            float impaledBodyRot = inEntity.getYRot();
            Vec3 offset = Vec3.applyLocalCoordinatesToRotation(new Vec2(0, impaledBodyRot), getImpaleOffset());
            this.setPos(inEntity.position().add(offset));

            if (!inEntity.isAlive() || inEntity.isRemoved()) {
                drop(10);
            }
        }
    }

    @Override
    protected boolean updateFluidInteraction() {
        boolean wasntInWater = !isInWater();
        boolean isInFluid = super.updateFluidInteraction();
        if (wasntInWater && isInWater() && getPickupItem().is(Clobbered.SKIPPABLE)) {
            timesSkippedOnWater++;
            final Vec3 velocity = this.getDeltaMovement();
            this.setDeltaMovement(velocity.add(0, velocity.length() * 0.5F, 0));
        }
        return isInFluid;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (level().isClientSide() || isImpaling()) return;
        if (isHurled()) {
            ItemStack item = getPickupItem();
            if (item.is(Clobbered.EXPLODES)) {
                level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, Level.ExplosionInteraction.MOB);
                this.discard();
            } else if (item.isDamageableItem()) {
                if (item.nextDamageWillBreak()) {
                    level().playSound(null, blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS);
                    this.discard();
                } else {
                    item.setDamageValue(getPickupItem().getDamageValue() + 1);
                    setPickupItemStack(item);
                }
            } else if (config.itemBreakChance > random.nextFloat()) {
                Item brokenItem = config.brokenItems.get(item.getItem());
                if (brokenItem != null) {
                    ItemStack brokenStack = brokenItem.getDefaultInstance();
                    brokenStack.setCount(item.getCount());
                    setPickupItemStack(brokenStack);
                    level().playSound(null, blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS);
                }
            }
        }
        super.onHit(hitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        // super method introduces weird rotation...
        super.onHitBlock(hitResult);
        if (!isHurled() || !this.getPickupItem().is(Clobbered.SHARP)) {
            // prevent dropped items getting stuck in walls
            Vec3 normal = hitResult.getDirection().getUnitVec3();
            float scale = 0.25F;
            final Vec3 hitPos = hitResult.getLocation().add(normal.multiply(scale, scale, scale));
            drop(10, hitPos.x, hitPos.y, hitPos.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        final Entity entity = hitResult.getEntity();
        if (entity.is(EntityType.ENDERMAN)) { // workaround: damage type tag not working
            entity.hurtOrSimulate(damageSources().arrow(this, this), 0);
            return;
        }
        if (!(entity instanceof LivingEntity)
                || (this.tickCount < 6 && entity instanceof Player)
        ) return;
        double damage = this.getDeltaMovement().length();
        ItemStack itemStack = this.getPickupItem();
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            float destroyTime = blockItem.getBlock().defaultDestroyTime();
            float resistance = blockItem.getBlock().getExplosionResistance();
            float hardness = (destroyTime + resistance) / 2;
            // BLOCK  - /2     /1   /3
            // cobble - 4      8    2.6
            // planks - 2.5    5    1.6
            // obsidian - 625  1250 416
            damage *= Math.pow(hardness, config.damageScalingPower);
            // cobble - 1.3    1.5  1.2
            // planks - 1.2    1.4  1.1
            // obsidian - 3.6  4.2  3.3
        }
        final boolean canDamage = !itemStack.is(Clobbered.SOFT);
        final Vec3 hitPos = hitResult.getLocation();
        final ServerLevel level = (ServerLevel) level();
        if (isHurled() && canDamage && itemStack.is(Clobbered.SHARP)) {
            impaleEntity(entity, hitPos, this.getDeltaMovement().rotation());
            DamageSource damageSource = itemStack.getDamageSource((LivingEntity) this.getOwner(),
                    () -> this.damageSources().playerAttack((Player) this.getOwner()));
            damage = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                    .compute(Attributes.ATTACK_DAMAGE, damage, EquipmentSlot.MAINHAND);
            damage += itemStack.getItem().getAttackDamageBonus(entity, (float) damage, damageSource);
            damage = EnchantmentHelper.modifyDamage(level, itemStack, entity, damageSource, (float) damage);
            EnchantmentHelper.doPostAttackEffectsWithItemSource(level, entity, damageSource, itemStack);
            if (entity instanceof LivingEntity mob && this.getOwner() instanceof LivingEntity attacker) {
                if (itemStack.hurtEnemy(mob, attacker)) // true if item is weapon used by player
                    itemStack.postHurtEnemy(mob, attacker);
            }
            hurt(entity, damageSource, damage);
        } else if (damage > 0.5) {
            DamageSource damageSource = new DamageSource(
                    level().registryAccess()
                            .lookupOrThrow(Registries.DAMAGE_TYPE)
                            .get(Clobbered.DAMAGE_TYPE.identifier()).orElseThrow()
            );
            if (canDamage) hurt(entity, damageSource, damage);
            if (entity.is(Clobbered.STICKY)) {
                impaleEntity(entity, hitPos, this.getRotationVector());
            } else {
                drop(10, hitPos.x, hitPos.y, hitPos.z);
            }
        }
    }

    public void hurt(Entity entity, DamageSource damageSource, double damage) {
        final boolean wasHurt = entity.hurtOrSimulate(damageSource, (float) damage);
        if (wasHurt && getOwner() instanceof Player player) {
            if (entity instanceof LivingEntity livingTarget) {
                float oldLivingEntityHealth = livingTarget.getHealth();
                ((PlayerAccessor)player).callDamageStatsAndHearts(entity, oldLivingEntityHealth);
            }
            player.setLastHurtMob(entity);
        }
    }

    public void drop(int pickUpDelay) {
        drop(pickUpDelay, this.getX(), this.getY(), this.getZ());
    }
    public void drop(int pickUpDelay, double x, double y, double z) {
        ItemEntity itemEntity = new ItemEntity(this.level(), x, y, z, getPickupItem());
        itemEntity.setPickUpDelay(pickUpDelay);
        this.level().addFreshEntity(itemEntity);
        this.discard();
    }

    public void impaleEntity(Entity entity, Vec3 impalePos, Vec2 impaleRotation) {
        setInEntity(entity);
        setImpaleOffset(impalePos, entity.position(), entity.getYRot());
        setDeltaMovement(Vec3.ZERO);
        this.setNoPhysics(true);
        this.setNoGravity(true);
        this.entityData.set(IMPALE_ROT_X, impaleRotation.x);
        this.entityData.set(IMPALE_ROT_Y, impaleRotation.y - entity.getYRot());
        if (entity instanceof Mob mob) mob.setPersistenceRequired();
    }

    @Override
    public boolean isPickable() {
        return config.itemPickUp == ConfigData.PickUpMethod.INTERACT
            || (config.itemPickUp == ConfigData.PickUpMethod.MOB_DEATH && !isInEntity());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.getPickupItem().is(Clobbered.UNCATCHABLE)) return InteractionResult.PASS;
        if (player.getItemInHand(hand).isEmpty()) {
            if (level().isClientSide()) { // do pick-up animation
                EntityRenderState itemState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(this, 1.0F);
                Minecraft.getInstance().particleEngine.add(
                        new ItemPickupParticle((ClientLevel) this.level(), itemState, player, this.getDeltaMovement())
                );
            } else {
                player.setItemInHand(hand, this.getPickupItem());
                this.discard();
            }
        } else {
            if (!level().isClientSide()) this.drop(0);
        }

        return InteractionResult.SUCCESS;
    }

    /// usually used for redirectable projectiles - server kicks for "attempting to attack invalid entity" when false.
    @Override
    public boolean isAttackable() {
        return true;
    }


    @Override
    public boolean skipAttackInteraction(Entity source) {
        return true;
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflectingEntity, @Nullable EntityReference<Entity> newOwner, boolean byAttack) {
        return super.deflect(deflection, deflectingEntity, newOwner, byAttack);
    }

    @Override
    protected void tickDespawn() {
        // dont
    }
}
