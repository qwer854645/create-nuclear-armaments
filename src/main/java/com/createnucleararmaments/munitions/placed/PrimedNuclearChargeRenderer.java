package com.createnucleararmaments.munitions.placed;

import com.createnucleararmaments.CNArmaments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class PrimedNuclearChargeRenderer extends EntityRenderer<PrimedNuclearCharge> {
    private final BlockRenderDispatcher blockRenderer;

    public PrimedNuclearChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
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
        poseStack.pushPose();
        poseStack.translate(-0.5F, 0.0F, -0.5F);

        int fuse = entity.getFuse() - (int) partialTick;
        boolean flash = fuse / 5 % 2 == 0;
        boolean critical = fuse <= PrimedNuclearCharge.CRITICAL_FUSE_TICKS;

        this.blockRenderer.renderSingleBlock(
                entity.getBlockState(),
                poseStack,
                buffer,
                flash ? LightTexture.FULL_BRIGHT : packedLight,
                OverlayTexture.NO_OVERLAY
        );

        if (flash) {
            int overlay = critical
                    ? OverlayTexture.pack(1.0F, true)
                    : OverlayTexture.pack(1.0F, false);
            this.blockRenderer.renderSingleBlock(
                    entity.getBlockState(),
                    poseStack,
                    buffer,
                    LightTexture.FULL_BRIGHT,
                    overlay
            );
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedNuclearCharge entity) {
        return CNArmaments.id("block/nuclear_charge");
    }
}
