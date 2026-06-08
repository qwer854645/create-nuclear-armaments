package com.createnucleararmaments.munitions.racked;

import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.FuzedRackedProjectileBlockItem;
import com.createnucleararmaments.munitions.NuclearMunitionTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Supplier;

public class NuclearBombBlockItem extends FuzedRackedProjectileBlockItem {
    private final Supplier<? extends EntityType<?>> entityType;

    public NuclearBombBlockItem(Block block, Item.Properties properties, Supplier<? extends EntityType<?>> entityType) {
        super(block, properties);
        this.entityType = entityType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        NuclearMunitionTooltips.appendBombShiftStats(stack, context, tooltip, flag, entityType.get());
    }
}
