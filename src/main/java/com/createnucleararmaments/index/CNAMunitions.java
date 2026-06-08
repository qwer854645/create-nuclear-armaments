package com.createnucleararmaments.index;

import com.cainiao1053.cbcmoreshells.index.CBCMSMunitionPropertiesHandlers;
import com.cainiao1053.cbcmoreshells.munitions.big_cannon.CannonTorpedoProjectileRenderer;
import com.cainiao1053.cbcmoreshells.munitions.racked_projectile.RackedProjectileRenderer;
import com.createnucleararmaments.munitions.racked.NuclearBombBlockItem;
import com.createnucleararmaments.munitions.racked.NuclearRocketBlockItem;
import com.createnucleararmaments.munitions.torpedo.NuclearTorpedoBlockItem;
import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.munitions.MunitionKind;
import com.createnucleararmaments.munitions.NuclearTier;
import com.createnucleararmaments.munitions.big_cannon.NuclearShellBlock;
import com.createnucleararmaments.munitions.big_cannon.NuclearShellBlockItem;
import com.createnucleararmaments.munitions.big_cannon.NuclearShellProjectile;
import com.createnucleararmaments.munitions.racked.NuclearBombBlock;
import com.createnucleararmaments.munitions.racked.NuclearBombProjectile;
import com.createnucleararmaments.munitions.racked.NuclearRocketBlock;
import com.createnucleararmaments.munitions.racked.NuclearRocketProjectile;
import com.createnucleararmaments.munitions.torpedo.NuclearTorpedoBlock;
import com.createnucleararmaments.munitions.torpedo.NuclearTorpedoProjectile;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import rbasamoyai.createbigcannons.index.CBCMunitionPropertiesHandlers;
import rbasamoyai.createbigcannons.multiloader.EntityTypeConfigurator;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;
import rbasamoyai.createbigcannons.munitions.config.MunitionPropertiesHandler;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class CNAMunitions {
    public static final Map<MunitionKind, Map<NuclearTier, BlockEntry<?>>> BLOCKS = new EnumMap<>(MunitionKind.class);
    public static final Map<MunitionKind, Map<NuclearTier, EntityEntry<?>>> ENTITIES = new EnumMap<>(MunitionKind.class);
    private static final List<BlockEntry<?>> ALL_MUNITION_BLOCKS = new ArrayList<>();

    private CNAMunitions() {
    }

    public static void register() {
        for (MunitionKind kind : MunitionKind.values()) {
            Map<NuclearTier, BlockEntry<?>> blocks = new EnumMap<>(NuclearTier.class);
            Map<NuclearTier, EntityEntry<?>> entities = new EnumMap<>(NuclearTier.class);
            BLOCKS.put(kind, blocks);
            ENTITIES.put(kind, entities);

            for (NuclearTier tier : NuclearTier.VALUES) {
                registerPair(kind, tier, blocks, entities);
            }
        }
        CNABlockEntities.register(ALL_MUNITION_BLOCKS);
    }

    @SuppressWarnings("unchecked")
    private static void registerPair(
            MunitionKind kind,
            NuclearTier tier,
            Map<NuclearTier, BlockEntry<?>> blocks,
            Map<NuclearTier, EntityEntry<?>> entities
    ) {
        String id = kind.idFor(tier);
        ResourceLocation visualModel = ResourceLocation.parse(kind.visualModel());
        DeferredSupplier<BlockState> renderedState = new DeferredSupplier<>();

        EntityEntry<?> entity = switch (kind) {
            case SHELL -> registerShellEntity(id, tier, renderedState);
            case TORPEDO -> registerTorpedoEntity(id, tier, renderedState);
            case BOMB -> registerRackedEntity(id, tier, renderedState);
            case ROCKET -> registerRocketEntity(id, tier, renderedState);
        };

        BlockEntry<?> block = switch (kind) {
            case SHELL -> CNArmaments.REGISTRATE
                    .block(id, props -> new NuclearShellBlock(props, () -> (EntityType<? extends NuclearShellProjectile>) entity.get()))
                    .properties(p -> p.mapColor(MapColor.METAL).strength(0.2F).noCollission())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), prov.models().getExistingFile(visualModel)))
                    .lang(langName(kind, tier))
                    .item((b, props) -> new NuclearShellBlockItem(b, props, entity::get))
                    .tag(CNATags.BIG_CANNON_PROJECTILES)
                    .build()
                    .register();
            case TORPEDO -> CNArmaments.REGISTRATE
                    .block(id, props -> new NuclearTorpedoBlock(props, () -> (EntityType<? extends NuclearTorpedoProjectile>) entity.get()))
                    .properties(p -> p.mapColor(MapColor.METAL).strength(0.2F).noCollission())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), prov.models().getExistingFile(visualModel)))
                    .lang(langName(kind, tier))
                    .item((b, props) -> new NuclearTorpedoBlockItem(b, props, entity::get))
                    .build()
                    .register();
            case BOMB -> CNArmaments.REGISTRATE
                    .block(id, props -> new NuclearBombBlock(props, () -> (EntityType<? extends NuclearBombProjectile>) entity.get()))
                    .properties(p -> p.mapColor(MapColor.METAL).strength(0.2F).noCollission())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), prov.models().getExistingFile(visualModel)))
                    .lang(langName(kind, tier))
                    .item((b, props) -> new NuclearBombBlockItem(b, props, entity::get))
                    .build()
                    .register();
            case ROCKET -> CNArmaments.REGISTRATE
                    .block(id, props -> new NuclearRocketBlock(props, () -> (EntityType<? extends NuclearRocketProjectile>) entity.get()))
                    .properties(p -> p.mapColor(MapColor.METAL).strength(0.2F).noCollission())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .blockstate((ctx, prov) -> prov.directionalBlock(ctx.get(), prov.models().getExistingFile(visualModel)))
                    .lang(langName(kind, tier))
                    .item((rocketBlock, props) -> new NuclearRocketBlockItem((NuclearRocketBlock) rocketBlock, props, () -> (EntityType<? extends NuclearRocketProjectile>) entity.get()))
                    .build()
                    .register();
        };

        renderedState.set(block::getDefaultState);
        blocks.put(tier, block);
        entities.put(tier, entity);
        ALL_MUNITION_BLOCKS.add(block);
    }

    private static EntityEntry<NuclearShellProjectile> registerShellEntity(String id, NuclearTier tier, Supplier<BlockState> renderedState) {
        return CNArmaments.REGISTRATE
                .entity(id, (EntityType<NuclearShellProjectile> type, Level level) -> new NuclearShellProjectile(type, level, renderedState), MobCategory.MISC)
                .properties(b -> EntityTypeConfigurator.of(b)
                        .size(0.8f, 0.8f)
                        .fireImmune()
                        .updateInterval(1)
                        .updateVelocity(false)
                        .trackingRange(16))
                .renderer(() -> BigCannonProjectileRenderer::new)
                .tag(CNATags.PRECISE_MOTION)
                .onRegister(type -> {
                    MunitionPropertiesHandler.registerProjectileHandler(type, CBCMunitionPropertiesHandlers.COMMON_SHELL_BIG_CANNON_PROJECTILE);
                    tier.bindEntity(type);
                })
                .register();
    }

    private static EntityEntry<NuclearTorpedoProjectile> registerTorpedoEntity(String id, NuclearTier tier, Supplier<BlockState> renderedState) {
        return CNArmaments.REGISTRATE
                .entity(id, (EntityType<NuclearTorpedoProjectile> type, Level level) -> new NuclearTorpedoProjectile(type, level, renderedState), MobCategory.MISC)
                .properties(b -> EntityTypeConfigurator.of(b)
                        .size(0.8f, 0.8f)
                        .fireImmune()
                        .updateInterval(1)
                        .updateVelocity(false)
                        .trackingRange(16))
                .renderer(() -> CannonTorpedoProjectileRenderer::new)
                .tag(CNATags.PRECISE_MOTION)
                .onRegister(type -> {
                    MunitionPropertiesHandler.registerProjectileHandler(type, CBCMSMunitionPropertiesHandlers.TORPEDO_PROJECTILE);
                    tier.bindEntity(type);
                })
                .register();
    }

    private static EntityEntry<NuclearBombProjectile> registerRackedEntity(String id, NuclearTier tier, Supplier<BlockState> renderedState) {
        return CNArmaments.REGISTRATE
                .entity(id, (EntityType<NuclearBombProjectile> type, Level level) -> new NuclearBombProjectile(type, level, renderedState), MobCategory.MISC)
                .properties(b -> EntityTypeConfigurator.of(b)
                        .size(0.8f, 0.8f)
                        .fireImmune()
                        .updateInterval(1)
                        .updateVelocity(false)
                        .trackingRange(16))
                .renderer(() -> RackedProjectileRenderer::new)
                .tag(CNATags.PRECISE_MOTION)
                .onRegister(type -> {
                    MunitionPropertiesHandler.registerProjectileHandler(type, CBCMSMunitionPropertiesHandlers.RACKED_PROJECTILE);
                    tier.bindEntity(type);
                })
                .register();
    }

    private static EntityEntry<NuclearRocketProjectile> registerRocketEntity(String id, NuclearTier tier, Supplier<BlockState> renderedState) {
        return CNArmaments.REGISTRATE
                .entity(id, (EntityType<NuclearRocketProjectile> type, Level level) -> new NuclearRocketProjectile(type, level, renderedState), MobCategory.MISC)
                .properties(b -> EntityTypeConfigurator.of(b)
                        .size(0.8f, 0.8f)
                        .fireImmune()
                        .updateInterval(1)
                        .updateVelocity(false)
                        .trackingRange(16))
                .renderer(() -> RackedProjectileRenderer::new)
                .tag(CNATags.PRECISE_MOTION)
                .onRegister(type -> {
                    MunitionPropertiesHandler.registerProjectileHandler(type, CBCMSMunitionPropertiesHandlers.RACKED_ROCKET);
                    tier.bindEntity(type);
                })
                .register();
    }

    private static String langName(MunitionKind kind, NuclearTier tier) {
        return switch (kind) {
            case SHELL -> "Nuclear HE Shell " + tier.name();
            case TORPEDO -> "Nuclear Torpedo " + tier.name();
            case BOMB -> "Nuclear Bomb " + tier.name();
            case ROCKET -> "Nuclear Rocket " + tier.name();
        };
    }

    private static final class DeferredSupplier<T> implements Supplier<T> {
        private Supplier<T> delegate = () -> {
            throw new IllegalStateException("Deferred supplier not bound yet");
        };

        @Override
        public T get() {
            return delegate.get();
        }

        public void set(Supplier<T> delegate) {
            this.delegate = delegate;
        }
    }
}
