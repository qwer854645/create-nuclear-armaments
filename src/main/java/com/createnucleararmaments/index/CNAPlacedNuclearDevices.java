package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.munitions.NuclearTier;
import com.createnucleararmaments.munitions.placed.NuclearChargeBlockItem;
import com.createnucleararmaments.munitions.placed.PlacedNuclearDeviceBlock;
import com.createnucleararmaments.munitions.placed.PrimedNuclearCharge;
import com.createnucleararmaments.munitions.placed.PrimedNuclearChargeRenderer;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.EnumMap;
import java.util.Map;

public final class CNAPlacedNuclearDevices {
    public static EntityEntry<PrimedNuclearCharge> PRIMED_NUCLEAR_CHARGE;
    public static final Map<NuclearTier, BlockEntry<PlacedNuclearDeviceBlock>> BLOCKS = new EnumMap<>(NuclearTier.class);

    private CNAPlacedNuclearDevices() {
    }

    public static void register() {
        PRIMED_NUCLEAR_CHARGE = CNArmaments.REGISTRATE
                .entity(
                        "primed_nuclear_charge",
                        (EntityType<PrimedNuclearCharge> type, net.minecraft.world.level.Level level) -> new PrimedNuclearCharge(type, level),
                        MobCategory.MISC
                )
                .properties(properties -> properties.fireImmune().sized(0.98F, 0.98F))
                .renderer(() -> PrimedNuclearChargeRenderer::new)
                .register();

        for (NuclearTier tier : NuclearTier.VALUES) {
            BLOCKS.put(tier, registerBlock(tier));
        }
    }

    private static BlockEntry<PlacedNuclearDeviceBlock> registerBlock(NuclearTier tier) {
        String id = "nuclear_charge_" + tier.suffix();
        ResourceLocation blockModel = CNArmaments.id("block/nuclear_charge");
        ResourceLocation itemModel = CNArmaments.id("item/nuclear_charge");
        return CNArmaments.REGISTRATE
                .block(id, props -> new PlacedNuclearDeviceBlock(props, tier))
                .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.TNT))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().getExistingFile(blockModel)))
                .lang(langName(tier))
                .item((block, props) -> new NuclearChargeBlockItem(block, props, tier))
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), itemModel))
                .build()
                .register();
    }

    private static String langName(NuclearTier tier) {
        return switch (tier) {
            case T1 -> "Compact Nuclear Charge";
            case T2 -> "Standard Nuclear Charge";
            case T3 -> "High-Yield Nuclear Charge";
        };
    }
}
