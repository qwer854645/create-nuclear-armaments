package com.createnucleararmaments.config;

import com.createnucleararmaments.munitions.NuclearTier;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CNAConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        var pair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = pair.getLeft();
        SERVER_SPEC = pair.getRight();
    }

    private CNAConfig() {
    }

    public static final class Server {
        public final TierValues t1;
        public final TierValues t2;
        public final TierValues t3;
        public final ModConfigSpec.IntValue falloutColumnsPerTick;
        public final ModConfigSpec.IntValue edgeRealExplodesPerTick;
        public final ModConfigSpec.IntValue edgeVisualFracturesPerTick;

        private Server(ModConfigSpec.Builder builder) {
            builder.push("tiers");
            t1 = new TierValues(builder, "t1", 5, 12, 320.0D, 28.0D, 1600, 2);
            t2 = new TierValues(builder, "t2", 25, 48, 560.0D, 40.0D, 2400, 3);
            t3 = new TierValues(builder, "t3", 100, 192, 960.0D, 52.0D, 3200, 4);
            builder.pop();

            builder.comment("Secondary effect budgets. Primary crater still clears in one tick.")
                    .push("performance");
            falloutColumnsPerTick = builder
                    .comment("Max fallout columns processed per server tick.")
                    .defineInRange("falloutColumnsPerTick", 4096, 256, 32768);
            edgeRealExplodesPerTick = builder
                    .comment("Max real TNT-style rim explosions per tick for edge fracture.")
                    .defineInRange("edgeRealExplodesPerTick", 16, 0, 64);
            edgeVisualFracturesPerTick = builder
                    .comment("Max cheap visual rim breaks per tick for edge fracture.")
                    .defineInRange("edgeVisualFracturesPerTick", 90, 0, 256);
            builder.pop();
        }

        public TierValues values(NuclearTier tier) {
            return switch (tier) {
                case T1 -> t1;
                case T2 -> t2;
                case T3 -> t3;
            };
        }
    }

    public static final class TierValues {
        public final ModConfigSpec.IntValue yieldKilotons;
        public final ModConfigSpec.IntValue billetCost;
        public final ModConfigSpec.DoubleValue entityExplosionPower;
        public final ModConfigSpec.DoubleValue blastRadius;
        public final ModConfigSpec.IntValue radiationDurationTicks;
        public final ModConfigSpec.IntValue radiationAmplifier;

        private TierValues(
                ModConfigSpec.Builder builder,
                String path,
                int yieldKilotons,
                int billetCost,
                double entityExplosionPower,
                double blastRadius,
                int radiationDurationTicks,
                int radiationAmplifier
        ) {
            builder.push(path);
            this.yieldKilotons = builder.defineInRange("yieldKilotons", yieldKilotons, 1, 10000);
            this.billetCost = builder.defineInRange("billetCost", billetCost, 1, 1024);
            this.entityExplosionPower = builder.defineInRange("entityExplosionPower", entityExplosionPower, 1.0D, 2000.0D);
            this.blastRadius = builder
                    .comment("Primary crater radius in blocks. Radiation radius is 2x this value.")
                    .defineInRange("blastRadius", blastRadius, 4.0D, 256.0D);
            this.radiationDurationTicks = builder.defineInRange("radiationDurationTicks", radiationDurationTicks, 20, 72000);
            this.radiationAmplifier = builder.defineInRange("radiationAmplifier", radiationAmplifier, 0, 10);
            builder.pop();
        }
    }
}
