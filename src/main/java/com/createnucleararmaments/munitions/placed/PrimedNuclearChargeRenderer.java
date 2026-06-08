package com.createnucleararmaments.munitions.placed;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class PrimedNuclearChargeRenderer extends EntityRenderer<PrimedNuclearCharge> {
    private static final float BLINK_PERIOD_TICKS = 10.0F;
    private static final float CRITICAL_BLINK_PERIOD_TICKS = 5.0F;
    private static final float MIN_ALPHA = 0.28F;
    private static final float MAX_ALPHA = 0.92F;

    public PrimedNuclearChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public void render(
            PrimedNuclearCharge entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        float fuse = entity.getFuse() - partialTick;
        boolean critical = fuse <= PrimedNuclearCharge.CRITICAL_FUSE_TICKS;
        float red = 1.0F;
        float green = critical ? 0.12F : 1.0F;
        float blue = critical ? 0.12F : 1.0F;

        float period = critical ? CRITICAL_BLINK_PERIOD_TICKS : BLINK_PERIOD_TICKS;
        float phase = (entity.tickCount + partialTick) * (Mth.TWO_PI / period);
        float pulse = 0.5F + 0.5F * Mth.sin(phase);
        float alpha = Mth.lerp(pulse, MIN_ALPHA, MAX_ALPHA);

        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        DebugRenderer.renderFilledBox(
                poseStack,
                buffer,
                0.03D,
                0.0D,
                0.03D,
                0.97D,
                0.98D,
                0.97D,
                red,
                green,
                blue,
                alpha
        );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedNuclearCharge entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }
}
