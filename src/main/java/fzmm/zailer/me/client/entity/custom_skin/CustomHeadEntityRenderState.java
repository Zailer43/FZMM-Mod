package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class CustomHeadEntityRenderState extends LivingEntityRenderState {
    public CustomHeadEntityRenderState(Identifier texture) {
        this.texture = texture;
    }

    public Identifier texture;
}
