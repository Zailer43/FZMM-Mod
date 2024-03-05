package fzmm.zailer.me.client.gui.item_editor.filled_map_editor.components;

import fzmm.zailer.me.builders.FilledMapBuilder;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class MapComponent extends BaseComponent {
    @Nullable
    private FilledMapBuilder.FilledMapData mapData = null;
    private final MapRenderer mapRenderer;
    private static final float scaleX = 0.25f;
    private static final float scaleY = 0.25f;
    public static final int HORIZONTAL_SIZING = (int) (scaleX * FilledMapItem.field_30907);
    public static final int VERTICAL_SIZING = (int) (scaleY * FilledMapItem.field_30908);

    public MapComponent() {
        super();
        this.sizing(Sizing.fixed(HORIZONTAL_SIZING), Sizing.fixed(VERTICAL_SIZING));
        this.margins(Insets.of(1));

        this.mapRenderer = MinecraftClient.getInstance().gameRenderer.getMapRenderer();
    }

    public void setMapState(FilledMapBuilder.FilledMapData mapData) {
        this.mapData = mapData;
        this.tooltip(Text.literal(String.valueOf(mapData.id())));
        this.cursorStyle(CursorStyle.HAND);
    }

    public void clearMap() {
        this.mapData = null;
        this.tooltip(new ArrayList<TooltipComponent>());
        this.cursorStyle(CursorStyle.POINTER);
    }

    public Optional<FilledMapBuilder.FilledMapData> getMapData() {
        return Optional.ofNullable(this.mapData);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.mapData != null) {
            int mapId = this.mapData.id();
            MapState mapState = this.mapData.mapState();
            MatrixStack matrices = context.getMatrices();

            context.push();
            context.translate(this.x(), this.y(), 200F);
            context.scale(scaleX, scaleY, 1F);
            this.mapRenderer.draw(matrices, context.getVertexConsumers(), mapId, mapState, true,
                    LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
            context.pop();
            context.draw();

            if (this.isInBoundingBox(mouseX, mouseY))
                context.drawRectOutline(this.x(), this.y(), this.width(), this.height(), 0xFFFFFFFF);
        }
    }
}
