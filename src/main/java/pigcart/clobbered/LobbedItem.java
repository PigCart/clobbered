package pigcart.clobbered;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
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
import pigcart.clobbered.mixin.PlayerAccessor;

import static pigcart.clobbered.config.ConfigManager.config;

public class LobbedItem extends AbstractArrow {

    public Entity impaledEntity;
    public int timesSkippedOnWater = 0;

    public static final EntityDataAccessor<Integer> IMPALED_ENTITY = defineData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Vector3fc> IMPALE_OFFSET = defineData(EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Float> ROTATION_YAW = defineData(EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> ROTATION_PITCH = defineData(EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IMPALING = defineData(EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> HURLED = defineData(EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> RENDERED_ITEM = defineData(EntityDataSerializers.ITEM_STACK);

    private static <T> EntityDataAccessor<T> defineData(EntityDataSerializer<T> type) {
        return SynchedEntityData.defineId(LobbedItem.class, type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IMPALED_ENTITY, -1);
        builder.define(IMPALE_OFFSET, new Vector3f());
        builder.define(IMPALING, false);
        builder.define(HURLED, false);
        builder.define(ROTATION_YAW, 0F);
        builder.define(ROTATION_PITCH, 0F);
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
        /*output.store("RotationOffsetW", Codec.FLOAT, entityData.get(IMPALE_ROTATION_OFFSET));
        output.store("RotationOffsetX", Codec.FLOAT, entityData.get(IMPALE_ROTATION_OFFSET).x());
        output.store("RotationOffsetY", Codec.FLOAT, entityData.get(IMPALE_ROTATION_OFFSET).y());
        output.store("RotationOffsetZ", Codec.FLOAT, entityData.get(IMPALE_ROTATION_OFFSET).z());*/
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        setHurled(input.read("Hurled", Codec.BOOL).orElse(false));
        /*entityData.set(IMPALE_ROTATION_OFFSET, new Quaternionf(
                input.read("RotationOffsetX", Codec.FLOAT).orElse(0F),
                input.read("RotationOffsetY", Codec.FLOAT).orElse(0F),
                input.read("RotationOffsetZ", Codec.FLOAT).orElse(0F),
                input.read("RotationOffsetW", Codec.FLOAT).orElse(0F)
        ));*/
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return Items.BARRIER.getDefaultInstance();
    }
    public void setRenderItemStack(ItemStack itemStack) { this.entityData.set(RENDERED_ITEM, itemStack); }
    public ItemStack getRenderItemStack() { return this.entityData.get(RENDERED_ITEM); }
    public float getRotation() { return entityData.get(ROTATION_YAW); }
    public boolean isImpaling() { return this.entityData.get(IMPALING); }
    public boolean isImpalingEntity() { return this.entityData.get(IMPALED_ENTITY) != -1; }
    public Vec3 getImpaleOffset() { return new Vec3(entityData.get(IMPALE_OFFSET)); }
    public boolean isHurled() { return this.entityData.get(HURLED); }
    public void setHurled(boolean value) { this.entityData.set(HURLED, value); }
    //public void setItem(final ItemStack source) { this.getEntityData().set(ITEM_STACK, source); }
    //public ItemStack getItem() { return this.getEntityData().get(ITEM_STACK); }

    public LobbedItem(EntityType<LobbedItem> type, Level level) { super(type, level); }
    public LobbedItem(Level level, double x, double y, double z, Vec3 velocity, boolean hurled, ItemStack itemStack, Entity owner) {
        this(Clobbered.LOBBED_ITEM, level);
        this.setPos(x, y, z);
        this.setDeltaMovement(velocity);
        this.entityData.set(ROTATION_YAW, this.getDeltaMovement().rotation().y);
        this.setPickupItemStack(itemStack);
        this.setRenderItemStack(itemStack);
        setHurled(hurled);
        this.setOwner(owner);
        this.setSoundEvent(SoundEvents.EMPTY);
    }

    @Override
    protected boolean tryPickup(Player player) {
        if (config.automaticItemPickUp) {
            return super.tryPickup(player);
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
        if (!isImpaling()) {
            Vec2 rotation = this.getDeltaMovement().rotation();
            this.setRot(rotation.y, rotation.x);
        }
        if (this.level().isClientSide()) return;
        if (isImpalingEntity()) {
            Vec3 entityPos = new Vec3(impaledEntity.getX(), impaledEntity.getY(), impaledEntity.getZ());
            this.setPos(entityPos.add(getImpaleOffset()));
            if (!impaledEntity.isAlive() || impaledEntity.isRemoved()) {
                drop(10);
            }
        }
    }

    /*@Override
    protected Item getDefaultItem() {
        return Items.BARRIER;
    }*/

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
        if (getPickupItem().is(Clobbered.EXPLODES)) {
            level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, Level.ExplosionInteraction.MOB);
            this.discard();
        } else {
            super.onHit(hitResult);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        final Vec3 hitPos = hitResult.getLocation();
        if (isHurled() && this.getPickupItem().is(Clobbered.SHARP)) {
            float normalOffset = 0.1F;
            impaleBlock(hitPos.add(hitResult.getDirection().getUnitVec3().multiply(normalOffset, normalOffset, normalOffset)));
        } else {
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
            damage *= Math.pow(hardness, 0.2);
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
            System.out.println(damageSource.is(DamageTypeTags.IS_PROJECTILE));
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
        setDeltaMovement(0, 0, 0);
        this.setNoGravity(true);
        this.entityData.set(IMPALE_OFFSET, impalePos.subtract(entity.position()).toVector3f());
        this.entityData.set(ROTATION_YAW, impaleRotation.y);
        this.entityData.set(IMPALED_ENTITY, entity.getId());
        this.entityData.set(IMPALING, true);
        if (entity instanceof Mob mob) mob.setPersistenceRequired();
    }

    public void impaleBlock(Vec3 impalePos) {
        this.entityData.set(ROTATION_YAW, this.getDeltaMovement().rotation().y);
        this.entityData.set(IMPALING, true);
        //setDeltaMovement(0, 0, 0);
        //this.setNoGravity(true);
        //setPos(impalePos);
    }

    @Override
    public boolean isPickable() {
        return true; // allows to be picked in raycasts, enabling interaction
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.getPickupItem().is(Clobbered.UNCATCHABLE)) return InteractionResult.PASS;
        if (!level().isClientSide()) {
            if (player.getItemInHand(hand).isEmpty()) {
                player.setItemInHand(hand, this.getPickupItem());
                this.discard();
            } else {
                this.drop(0);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
