package com.createnucleararmaments.munitions.racked;

import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.AbstractRackedRocketBlockItem;
import com.createnucleararmaments.munitions.NuclearMunitionTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Supplier;

public class NuclearRocketBlockItem extends AbstractRackedRocketBlockItem<NuclearRocketProjectile> {
    private final Supplier<? extends EntityType<? extends NuclearRocketProjectile>> entityType;

    public NuclearRocketBlockItem(NuclearRocketBlock block, Item.Properties properties, Supplier<? extends EntityType<? extends NuclearRocketProjectile>> entityType) {
        super(block, properties);
        this.entityType = entityType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        NuclearMunitionTooltips.appendRocketShiftStats(stack, context, tooltip, flag, entityType.get());
    }

    @Override
    public EntityType<? extends NuclearRocketProjectile> getAssociatedEntityType() {
        return entityType.get();
    }
}
