package com.createnucleararmaments.munitions.big_cannon;

import com.createnucleararmaments.munitions.NuclearMunitionTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import rbasamoyai.createbigcannons.munitions.FuzedProjectileBlockItem;

import java.util.List;
import java.util.function.Supplier;

public class NuclearShellBlockItem extends FuzedProjectileBlockItem {
    private final Supplier<? extends EntityType<?>> entityType;

    public NuclearShellBlockItem(Block block, Item.Properties properties, Supplier<? extends EntityType<?>> entityType) {
        super(block, properties);
        this.entityType = entityType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        NuclearMunitionTooltips.appendShellShiftStats(stack, context, tooltip, flag, entityType.get());
    }
}
