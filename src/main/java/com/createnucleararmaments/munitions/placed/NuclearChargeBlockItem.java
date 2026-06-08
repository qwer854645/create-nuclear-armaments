package com.createnucleararmaments.munitions.placed;

import com.createnucleararmaments.munitions.NuclearMunitionTooltips;
import com.createnucleararmaments.munitions.NuclearTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class NuclearChargeBlockItem extends BlockItem {
    private final NuclearTier tier;

    public NuclearChargeBlockItem(Block block, Item.Properties properties, NuclearTier tier) {
        super(block, properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        NuclearMunitionTooltips.appendChargeShiftStats(stack, context, tooltip, flag, tier);
    }
}
