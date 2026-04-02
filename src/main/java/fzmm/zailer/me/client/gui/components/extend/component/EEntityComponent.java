package fzmm.zailer.me.client.gui.components.extend.component;

import com.mojang.math.Axis;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.renderstate.EntityElementRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

public class EEntityComponent<T extends LivingEntity> extends EntityComponent<T> {

    public EEntityComponent(Sizing sizing, T entity) {
        super(sizing, entity);
    }

    /**
     * Copy of {@link EntityComponent#draw(OwoUIGraphics, int, int, float, float)} with workaround to owo-lib in 1.21.6 - 1.21.8
     */
    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        var matrix = new Matrix4f();
        float scale = this.scale * 0.95f;
        matrix.scale(75 * scale * this.width / 64f, -75 * scale * this.height / 64f, -75 * scale);

        // if translate is applied, all the other entities will be translated too, w h y?
        // is better to use scale instead
        matrix.translate(0, 0.1f, 0);
        this.transform.accept(matrix);

        // remove lookAtMouse, is not needed
        matrix.rotate(Axis.XP.rotationDegrees(35));
        matrix.rotate(Axis.YP.rotationDegrees(-45 + this.mouseRotation));

        EntityRenderState entityState = this.manager.getRenderer(this.entity).createRenderState();

        // replace partialTicks to 0f to fix shaking
        ((EntityRenderer) this.manager.getRenderer(this.entity)).extractRenderState(this.entity, entityState, 0f);
        graphics.guiRenderState.addPicturesInPictureState(new EntityElementRenderState(
                entityState,
                matrix,
                new ScreenRectangle(this.x, this.y, this.width, this.height),
                graphics.scissorStack.peek()
        ));
    }
}
