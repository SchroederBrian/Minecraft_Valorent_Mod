package com.bobby.valorant.client.render;

import com.bobby.valorant.client.render.state.CorpseRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public class CorpseHumanoidModel extends HumanoidModel<CorpseRenderState> {
    public CorpseHumanoidModel(ModelPart root) { super(root); }

    @Override
    public void setupAnim(CorpseRenderState state) {
        super.setupAnim(state);
        final float D2R = (float)Math.PI / 180.0F;

        this.head.xRot = state.headX * D2R;
        this.head.yRot = state.headY * D2R;
        this.head.zRot = state.headZ * D2R;

        this.rightArm.yRot = state.rArmY * D2R;
        this.rightArm.zRot = state.rArmZ * D2R;

        this.leftArm.yRot = state.lArmY * D2R;
        this.leftArm.zRot = state.lArmZ * D2R;

        this.rightLeg.xRot = 0.0F;
        this.rightLeg.yRot = state.rLegY * D2R;
        this.rightLeg.zRot = state.rLegZ * D2R;

        this.leftLeg.xRot = 0.0F;
        this.leftLeg.yRot = state.lLegY * D2R;
        this.leftLeg.zRot = state.lLegZ * D2R;
    }
}
