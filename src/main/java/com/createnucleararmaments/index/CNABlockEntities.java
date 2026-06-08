package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.createnucleararmaments.munitions.NuclearFuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntityRenderer;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockVisual;

import java.util.Collection;

public final class CNABlockEntities {
    public static BlockEntityEntry<NuclearFuzedBlockEntity> FUZED_BLOCK;

    private CNABlockEntities() {
    }

    public static void register(Collection<BlockEntry<?>> munitionBlocks) {
        var builder = CNArmaments.REGISTRATE
                .blockEntity("fuzed_block", NuclearFuzedBlockEntity::new)
                .visual(() -> FuzedBlockVisual::new)
                .renderer(() -> FuzedBlockEntityRenderer::new);

        for (BlockEntry<?> block : munitionBlocks) {
            builder = builder.validBlock(block);
        }

        FUZED_BLOCK = builder.register();
    }
}
