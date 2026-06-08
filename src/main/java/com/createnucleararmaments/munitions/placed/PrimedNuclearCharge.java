package com.createnucleararmaments.munitions.placed;

import com.createnucleararmaments.index.CNAPlacedNuclearDevices;
import com.createnucleararmaments.munitions.NuclearDetonation;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PrimedNuclearCharge extends Entity {
    public static final int FUSE_TICKS = 1200;

    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(PrimedNuclearCharge.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(PrimedNuclearCharge.class, EntityDataSerializers.INT);

    public PrimedNuclearCharge(EntityType<? extends PrimedNuclearCharge> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public PrimedNuclearCharge(Level level, double x, double y, double z, NuclearTier tier) {
        this(CNAPlacedNuclearDevices.PRIMED_NUCLEAR_CHARGE.get(), level);
        this.setPos(x, y, z);
        this.setFuse(FUSE_TICKS);
        this.setTier(tier);
        double randomOffset = level.random.nextDouble() * (float) (Math.PI * 2D);
        this.setDeltaMovement(-Math.sin(randomOffset) * 0.02D, 0.2F, -Math.cos(randomOffset) * 0.02D);
        this.setNoGravity(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE, FUSE_TICKS);
        builder.define(DATA_TIER, NuclearTier.T1.ordinal());
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int fuse = this.getFuse() - 1;
        this.setFuse(fuse);
        if (fuse <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                NuclearDetonation.detonate((ServerLevel) this.level(), this.position(), getTier());
            }
            return;
        }

        this.level().addParticle(
                ParticleTypes.SMOKE,
                this.getX(),
                this.getY() + 0.5D,
                this.getZ(),
                0.0D,
                0.0D,
                0.0D
        );
        if (fuse % 20 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public BlockState getBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE);
    }

    public void setFuse(int fuse) {
        this.entityData.set(DATA_FUSE, fuse);
    }

    public NuclearTier getTier() {
        return NuclearTier.VALUES[this.entityData.get(DATA_TIER)];
    }

    public void setTier(NuclearTier tier) {
        this.entityData.set(DATA_TIER, tier.ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setFuse(tag.getInt("Fuse"));
        this.setTier(NuclearTier.VALUES[tag.getInt("Tier")]);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fuse", this.getFuse());
        tag.putInt("Tier", this.getTier().ordinal());
    }
}
