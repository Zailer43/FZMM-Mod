package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class CustomHeadEntityModel extends EntityModel<CustomHeadEntityRenderState> implements HeadedModel {

    private final ModelPart head;

    public CustomHeadEntityModel(ModelPart root) {
        super(root);
        this.head = root.getChild(PartNames.HEAD);
        ModelPart hat = this.head.getChild(PartNames.HAT);
        hat.visible = true;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        PartDefinition headModelPartData = modelPartData.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.rotation(0.0f, 0.0f, 0.0f));

        headModelPartData.addOrReplaceChild(PartNames.HAT, CubeListBuilder.create()
                .texOffs(32, 0)
                .addBox(-4.0f, 12.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.45f)), PartPose.ZERO);

        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void setupAnim(CustomHeadEntityRenderState state) {
        super.setupAnim(state);
        this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}
