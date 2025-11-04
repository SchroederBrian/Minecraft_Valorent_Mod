package com.bobby.valorant.client.render;

import com.bobby.valorant.Config;
import com.bobby.valorant.client.render.state.CorpseRenderState;
import com.bobby.valorant.world.agent.Agent;
import com.bobby.valorant.world.entity.CorpseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class CorpseRenderer extends LivingEntityRenderer<
        CorpseEntity,
        CorpseRenderState,
        CorpseHumanoidModel> {

    public CorpseRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CorpseHumanoidModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.0F);
    }

    @Override
    public CorpseRenderState createRenderState() {
        return new CorpseRenderState();
    }

    @Override
    public void extractRenderState(@Nonnull CorpseEntity entity,
                                   @Nonnull CorpseRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        // Blickrichtung vom Todeszeitpunkt
        state.deathYawDeg = Mth.wrapDegrees(entity.getDeathYaw());

        // Agent/Texture
        state.agentId = entity.getAgentId();

        // Neutralisieren (wir drehen über Matrizen, nicht über Vanilla-Werte)
        state.yRot    = 0.0F;
        state.xRot    = 0.0F;
        state.bodyRot = 0.0F;

        state.deathTime     = 0.0F;
        state.hasRedOverlay = false;

        // --- Gliedmaßen-Winkel (Grad) aus EntityData übernehmen ---
        state.headX = entity.getHeadX();
        state.headY = entity.getHeadY();
        state.headZ = entity.getHeadZ();

        state.rArmY = entity.getRArmY();
        state.rArmZ = entity.getRArmZ();

        state.lArmY = entity.getLArmY();
        state.lArmZ = entity.getLArmZ();

        state.rLegY = entity.getRLegY();
        state.rLegZ = entity.getRLegZ();

        state.lLegY = entity.getLLegY();
        state.lLegZ = entity.getLLegZ();
    }

    @Override
    public void render(CorpseRenderState state,
                       PoseStack pose,
                       MultiBufferSource buffer,
                       int packedLight) {
        pose.pushPose();

        // 1) Corpse exakt in Blickrichtung vom Todesmoment drehen
        pose.mulPose(Axis.YP.rotationDegrees(-state.deathYawDeg));

        // 2) Auf den Rücken legen (Face-Up). Falls -90° bei dir schöner war: hier anpassen.
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));

        // 3) Leichter Offset gegen Z-Fighting
        pose.translate(0.0D, -1.40D, 0.0D);

        super.render(state, pose, buffer, packedLight);
        pose.popPose();
    }

    @Override
    protected float getShadowRadius(CorpseRenderState state) {
        return 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(@Nonnull CorpseRenderState state) {
        String agentId = state.agentId.isEmpty() ? Config.COMMON.agentSkinsDefaultAgent.get() : state.agentId;
        return Agent.byId(agentId).getTextureLocation();
    }
}
