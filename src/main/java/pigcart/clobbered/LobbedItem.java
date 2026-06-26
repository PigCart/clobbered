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

import static pigcart.clobbered.config.ConfigManager.config;

public class LobbedItem extends AbstractArrow {

    public Entity impaledEntity;
    public int timesSkippedOnWater = 0;
    public boolean boomerangReturning = false;
    public Vec3 boomerangReturnPos;

    public static final EntityDataAccessor<Integer> IMPALED_ENTITY = defineData(EntityDataSerializers.INT);
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
        builder.define(IMPALED_ENTITY, -1);
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
        impaledEntity = level().getEntity(entityData.get(IMPALED_ENTITY));
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Hurled", Codec.BOOL, isHurled());
        /*output.store("InEntity", Codec.STRING, level().getEntity(entityData.get(IMPALED_ENTITY)).getStringUUID());
        System.out.println("saving entity id " + entityData.get(IMPALED_ENTITY));

         */
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        setHurled(input.read("Hurled", Codec.BOOL).orElse(false));
        /*entityData.set(IMPALED_ENTITY, level().getEntity(
                UUID.fromString(input.read("InEntity", Codec.STRING).orElse(""))
        ).getId());
        if (isInEntity()) {
            impaledEntity = level().getEntity(entityData.get(IMPALED_ENTITY));
            System.out.println("impaled entity is " + impaledEntity + " with id " + entityData.get(IMPALED_ENTITY));
            if (impaledEntity == null) {
                entityData.set(IMPALED_ENTITY, -1);
            }
        }*/
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return Items.BARRIER.getDefaultInstance();
    }
    public ItemStack getRenderItemStack() { return this.entityData.get(RENDERED_ITEM); }
    public boolean isImpaling() { return isInGround() || isInEntity(); }
    public boolean isInEntity() { return this.entityData.get(IMPALED_ENTITY) != -1; }
    public Vec3 getImpaleOffset() { return new Vec3(entityData.get(IMPALE_OFFSET)); }
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
            // attempt to re attach to entity after it becomes unloaded
            if (impaledEntity != null
                    && impaledEntity.isRemoved()
                    && impaledEntity.getRemovalReason() == RemovalReason.DISCARDED
            ) {
                impaledEntity = level().getEntity(impaledEntity.getUUID());
            }
            return;
        }
        if (isInEntity()) {

            float impaledBodyRot = impaledEntity.getYRot();
            Vec3 offset = Vec3.applyLocalCoordinatesToRotation(new Vec2(0, impaledBodyRot), getImpaleOffset());
            this.setPos(impaledEntity.position().add(offset));

            if (!impaledEntity.isAlive() || impaledEntity.isRemoved()) {
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
        this.impaledEntity = entity;
        setImpaleOffset(impalePos);
        setDeltaMovement(Vec3.ZERO);
        this.setNoPhysics(true);
        this.setNoGravity(true);
        this.entityData.set(IMPALED_ENTITY, entity.getId());
        this.entityData.set(IMPALE_ROT_X, impaleRotation.x);
        this.entityData.set(IMPALE_ROT_Y, impaleRotation.y - entity.getYRot());
        if (entity instanceof Mob mob) mob.setPersistenceRequired();
    }

    public void setImpaleOffset(Vec3 impalePos) {
        Vec3 localImpalePos = impalePos.subtract(impaledEntity.position());
        Vec3 impaleOffset = Vec3.applyLocalCoordinatesToRotation(new Vec2(0, -impaledEntity.getYRot()), localImpalePos);
        this.entityData.set(IMPALE_OFFSET, impaleOffset.toVector3f());
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

    @Override
    public void setPos(double x, double y, double z) {
        if (this.isInEntity()) {
            //setImpaleOffset(new Vec3(x, y, z));
        }
        super.setPos(x, y, z);
    }
}
