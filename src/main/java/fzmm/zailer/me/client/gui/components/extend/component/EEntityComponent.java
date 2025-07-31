package fzmm.zailer.me.client.gui.components.extend.component;

import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.renderstate.EntityElementRenderState;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class EEntityComponent<T extends LivingEntity> extends EntityComponent<T> {

    public EEntityComponent(Sizing sizing, T entity) {
        super(sizing, entity);
    }

    /**
     * Copy of {@link EntityComponent#draw(OwoUIDrawContext, int, int, float, float)} with workaround to owo-lib in 1.21.6 - 1.21.8
     */
    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrix = new Matrix4f();
        float scale = this.scale * 0.95f;
        matrix.scale(75 * scale * this.width / 64f, -75 * scale * this.height / 64f, -75 * scale);

        // if translate is applied, all the other entities will be translated too, w h y?
        // is better to use scale instead
        matrix.translate(0, 0.1f, 0);
        this.transform.accept(matrix);

        // remove lookAtMouse, is not needed
        matrix.rotate(RotationAxis.POSITIVE_X.rotationDegrees(35));
        matrix.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-45 + this.mouseRotation));

        var entityState = this.dispatcher.getRenderer(this.entity).createRenderState();

        // replace partialTicks to 0f to fix shaking
        ((EntityRenderer)this.dispatcher.getRenderer(this.entity)).updateRenderState(this.entity, entityState, 0f);
        context.state.addSpecialElement(new EntityElementRenderState(
                entityState,
                matrix,
                new ScreenRect(this.x, this.y, this.width, this.height),
                context.scissorStack.peekLast()
        ));
    }
}
