package fzmm.zailer.me.client.gui.item_editor.filled_map_editor;

import fzmm.zailer.me.builders.FilledMapBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.selectable.SelectableEditor;
import fzmm.zailer.me.client.gui.item_editor.filled_map_editor.components.MapComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FilledMapEditor extends SelectableEditor<MapComponent> {
    public static final Path SAVE_MAP_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "filled_map");
    private RequestedItem mapRequested = null;
    private List<RequestedItem> requestedItems = null;
    private List<FilledMapBuilder.FilledMapData> mapStates;
    private final FilledMapBuilder builder = FilledMapBuilder.builder();
    private ConfigTextBox mapIdTextBox;


    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.mapRequested = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof FilledMapItem,
                this::selectItemAndUpdateParameters,
                null,
                Items.FILLED_MAP.getDefaultStack(),
                Text.translatable("fzmm.gui.itemEditor.filled_map.title"),
                true
        );

        this.requestedItems = List.of(this.mapRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.FILLED_MAP.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {

        this.mapStates = FilledMapBuilder.getSortedMapStates();

        editorLayout = super.getLayout(baseScreen, editorLayout);

        // preview options
        this.mapIdTextBox = editorLayout.childById(ConfigTextBox.class, "id-text-box");
        BaseFzmmScreen.checkNull(this.mapIdTextBox, "text-box", "id-text-box");
        this.mapIdTextBox.configureForNumber(Integer.class);
        this.mapIdTextBox.onChanged().subscribe(value -> {
            if (!this.mapIdTextBox.isValid())
                return;

            this.builder.id((int) this.mapIdTextBox.parsedValue());
            this.updateItemPreview();
        });

        ButtonComponent savePngButton = editorLayout.childById(ButtonComponent.class, "save-png");
        BaseFzmmScreen.checkNull(savePngButton, "button", "save-png");
        savePngButton.onPress(savePngButtonComponent -> this.saveMapAsPng(this.previewComponent));

        ButtonComponent saveNbtButton = editorLayout.childById(ButtonComponent.class, "save-nbt");
        BaseFzmmScreen.checkNull(saveNbtButton, "button", "save-nbt");
        saveNbtButton.onPress(saveNbtButtonComponent -> this.saveMapAsNbt(this.previewComponent));

        return editorLayout;
    }

    @Override
    protected int getMaxByPage() {
        return 100;
    }

    @Override
    protected int getSelectableSize() {
        return this.mapStates.size();
    }

    @Override
    protected void select(MapComponent component) {
        component.getMapData().ifPresent(filledMapData -> {
            this.mapIdTextBox.setText(String.valueOf(filledMapData.id()));
            this.mapIdTextBox.setCursorToStart(false);
        });
    }

    @Override
    protected MapComponent emptyComponent() {
        return new MapComponent();
    }


    @Override
    public String getId() {
        return "filled_map";
    }

    @Override
    public void updateItemPreview() {
        Optional<FilledMapBuilder.FilledMapData> data = this.builder.data();
        if (data.isEmpty())
            this.previewComponent.clearMap();
        else
            this.previewComponent.setMapState(data.get());

        this.mapRequested.setStack(this.builder.get());
        this.mapRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack);
        this.builder.id().ifPresent(integer -> {
            this.mapIdTextBox.setText(String.valueOf(integer));
            this.mapIdTextBox.setCursorToStart(false);
        });
    }
    @Override
    protected void updateComponent(Component component, int index) {
        if (!(component instanceof MapComponent mapComponent))
            return;

        if (index < this.mapStates.size())
            mapComponent.setMapState(this.mapStates.get(index));
        else
            mapComponent.clearMap();
    }

    private void saveMapAsPng(MapComponent mapComponent) {
        this.saveMap("png", mapComponent, (file, mapState) -> {
            try {
                BufferedImage image = new BufferedImage(FilledMapItem.field_30907, FilledMapItem.field_30908, BufferedImage.TYPE_INT_ARGB);
                this.fillImage(mapState, image);
                ImageIO.write(image, "png", file);
                FzmmClient.LOGGER.info("[FilledMapEditor] File saved as " + file.getAbsolutePath());
            } catch (IOException e) {
                FzmmClient.LOGGER.error("[FilledMapEditor] Failed to save map", e);
            }
        });
    }

    private void saveMapAsNbt(MapComponent mapComponent) {
        this.saveMap("nbt", mapComponent, (file, mapState) -> {
            mapState.save(file);
            FzmmClient.LOGGER.info("[FilledMapEditor] File saved as " + file.getAbsolutePath());
        });
    }

    private void saveMap(String fileExtension, MapComponent mapComponent, SaveMapCallback saveMapCallback) {
        Optional<FilledMapBuilder.FilledMapData> dataOptional = mapComponent.getMapData();
        if (dataOptional.isEmpty())
            return;

        File file = this.getFile(dataOptional.get(), fileExtension);

        if (file.getParentFile().mkdirs())
            FzmmClient.LOGGER.info("[FilledMapEditor] Created folder " + file.getParentFile().getAbsolutePath());

        saveMapCallback.save(file, dataOptional.get().mapState());
    }

    private File getFile(FilledMapBuilder.FilledMapData data, String fileExtension) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        String fileName = data.id() + "_" + client.world.getRegistryKey().getValue();
        String folder;

        if (client.isInSingleplayer()) {
            folder = "singleplayer";
        } else {
            ServerInfo serverInfo = client.getCurrentServerEntry();
            if (serverInfo == null)
                folder = "multiplayer";
            else
                folder = serverInfo.address.replace(":", "_");

        }

        String validNameRegex = "[^a-zA-Z0-9._-]";
        fileName = fileName.replaceAll(validNameRegex, "_");
        folder = folder.replaceAll(validNameRegex, "_");

        if (fileName.length() > 32)
            fileName = fileName.substring(0, 32);

        return SAVE_MAP_FOLDER_PATH.resolve(folder).resolve(fileName + "." + fileExtension).toFile();
    }

    private void fillImage(MapState mapState, BufferedImage image) {
        for (int y = 0; y < FilledMapItem.field_30908; ++y) {
            for (int x = 0; x < FilledMapItem.field_30907; ++x) {
                int k = x + y * FilledMapItem.field_30907;
                Color color = new Color(MapColor.getRenderColor(mapState.colors[k]), true);
                int alpha = color.getAlpha();
                int blue = color.getRed();
                int green = color.getGreen();
                int red = color.getBlue();

                image.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
            }
        }
    }

    private interface SaveMapCallback {
        void save(File file, MapState mapState);
    }
}
