package com.createnucleararmaments.munitions.at;

import com.createnucleararmaments.munitions.NuclearDetonation;
import com.createnucleararmaments.munitions.NuclearTier;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.AbstractMediumRocket;
import com.dsvv.cbcat.cannon.medium_rocketpod.munitions.medium_he_rocket.MediumHERocket;
import net.minecraft.core.Position;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NuclearMediumMissile extends MediumHERocket {
    public NuclearMediumMissile(EntityType<? extends AbstractMediumRocket> type, Level level) {
        super(type, level);
    }

    @Override
    protected void detonate(Position pos) {
        NuclearDetonation.detonate(level(), new Vec3(pos.x(), pos.y(), pos.z()), NuclearTier.T3);
        discard();
    }
}
