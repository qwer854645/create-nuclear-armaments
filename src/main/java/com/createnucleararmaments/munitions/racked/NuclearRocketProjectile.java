package com.createnucleararmaments.munitions.racked;

import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.he_rocket.HERocketProjectile;
import com.createnucleararmaments.munitions.NuclearDetonation;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class NuclearRocketProjectile extends HERocketProjectile {
    private final Supplier<BlockState> renderedState;

    public NuclearRocketProjectile(EntityType<? extends NuclearRocketProjectile> type, Level level, Supplier<BlockState> renderedState) {
        super(type, level);
        this.renderedState = renderedState;
    }

    @Override
    protected void detonate(Position pos) {
        NuclearDetonation.detonate(level(), new Vec3(pos.x(), pos.y(), pos.z()), NuclearTier.fromEntity(getType()));
        discard();
    }

    @Override
    public BlockState getRenderedBlockState() {
        return renderedState.get();
    }
}
