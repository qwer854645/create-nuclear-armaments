package com.createnucleararmaments.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Oversized translucent smoke billboard so the pixel smoke atlas is actually readable in-world.
 */
public final class MushroomSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private MushroomSmokeParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.friction = 0.96F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        // Big quads: texture finally reads as a puff, not a speck.
        this.quadSize = 4.5F + this.random.nextFloat() * 3.5F;
        this.lifetime = 100 + this.random.nextInt(80);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.rCol = 0.75F + this.random.nextFloat() * 0.2F;
        this.gCol = this.rCol;
        this.bCol = this.rCol * 0.98F;
        this.alpha = 0.92F;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        // Soft fade in last third
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = life < 0.7F ? 0.92F : 0.92F * (1.0F - (life - 0.7F) / 0.3F);
        // Gentle expansion
        this.quadSize *= 1.0045F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Stay readable in crater shadows / night.
        return 0xF000F0;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new MushroomSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
