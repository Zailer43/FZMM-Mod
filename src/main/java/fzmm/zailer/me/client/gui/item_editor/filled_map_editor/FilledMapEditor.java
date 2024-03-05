package fzmm.zailer.me.client.gui.item_editor.filled_map_editor;

import fzmm.zailer.me.builders.FilledMapBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.filled_map_editor.components.MapComponent;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FilledMapEditor implements IItemEditorScreen {
    public static final Path SAVE_MAP_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "filled_map");
    private RequestedItem mapRequested = null;
    private List<RequestedItem> requestedItems = null;
    private List<FilledMapBuilder.FilledMapData> mapStates;
    private final FilledMapBuilder builder = FilledMapBuilder.builder();
    private MapComponent previewMap;
    private ConfigTextBox mapIdTextBox;
    private FlowLayout contentLayout;
    private LabelComponent currentPageLabel;
    private final int maxMapsByPage = 135;
    private int currentPage;



    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.mapRequested = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof FilledMapItem,
                this::selectItemAndUpdateParameters,
                null,
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
        // preview
        FlowLayout previewLayout = editorLayout.childById(FlowLayout.class, "preview");
        this.previewMap = new MapComponent();
        previewLayout.child(this.previewMap);

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
        savePngButton.onPress(savePngButtonComponent -> this.saveMapAsPng(this.previewMap));

        ButtonComponent saveNbtButton = editorLayout.childById(ButtonComponent.class, "save-nbt");
        BaseFzmmScreen.checkNull(saveNbtButton, "button", "save-nbt");
        saveNbtButton.onPress(saveNbtButtonComponent -> this.saveMapAsNbt(this.previewMap));

        // content
        this.contentLayout = editorLayout.childById(FlowLayout.class, "map-content");
        BaseFzmmScreen.checkNull(contentLayout, "flow-layout", "map-content");
        Insets previewMargin = this.previewMap.margins().get();
        int columns = 9;
        int rows = (int) Math.ceil(this.maxMapsByPage / (float) columns);
        this.contentLayout.sizing(
                Sizing.fixed(columns * MapComponent.HORIZONTAL_SIZING + previewMargin.horizontal()),
                Sizing.fixed(rows * MapComponent.VERTICAL_SIZING + previewMargin.vertical())
        );

        this.mapStates = FilledMapBuilder.getSortedMapStates();

        for (int i = 0; i < this.maxMapsByPage; i++) {
            MapComponent component = new MapComponent();
            component.mouseDown().subscribe((mouseX, mouseY, button) -> {
                component.getMapData().ifPresent(filledMapData -> this.mapIdTextBox.setText(String.valueOf(filledMapData.id())));
                return true;
            });
            this.contentLayout.child(component);
        }

        // page buttons
        this.currentPageLabel = editorLayout.childById(LabelComponent.class, "page-label");
        BaseFzmmScreen.checkNull(this.currentPageLabel, "label", "page-label");

        ButtonComponent previousPageButton = editorLayout.childById(ButtonComponent.class, "previous-page");
        BaseFzmmScreen.checkNull(previousPageButton, "button", "previous-page");
        previousPageButton.onPress(previousPageButtonComponent -> this.setPage(this.currentPage - 1));

        ButtonComponent nextPageButton = editorLayout.childById(ButtonComponent.class, "next-page");
        BaseFzmmScreen.checkNull(nextPageButton, "button", "next-page");
        nextPageButton.onPress(nextPageButtonComponent -> this.setPage(this.currentPage + 1));

        this.currentPage = -1;
        this.setPage(0);

        return editorLayout;
    }

    @Override
    public String getId() {
        return "filled_map";
    }

    @Override
    public void updateItemPreview() {
        Optional<FilledMapBuilder.FilledMapData> data = this.builder.data();
        if (data.isEmpty())
            this.previewMap.clearMap();
        else
            this.previewMap.setMapState(data.get());

        this.mapRequested.setStack(this.builder.get());
        this.mapRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.builder.of(stack.isEmpty() ? Items.FILLED_MAP.getDefaultStack() : stack);
        this.builder.id().ifPresent(integer -> this.mapIdTextBox.setText(String.valueOf(integer)));
    }

    private void setPage(int page) {
        int maxPage = this.mapStates.size() / this.maxMapsByPage;
        page = MathHelper.clamp(page, 0, maxPage);
        if (page == this.currentPage)
            return;

        this.currentPage = page;
        this.currentPageLabel.text(Text.translatable("fzmm.gui.itemEditor.filled_map.label.page",
                (this.currentPage + 1), (maxPage + 1)));

        int startIndex = this.currentPage * this.maxMapsByPage;
        List<Component> children = this.contentLayout.children();
        for (int i = 0; i < this.maxMapsByPage; i++) {
            int index = startIndex + i;
            this.updateMap(children.get(i), index);
        }
    }

    private void updateMap(Component component, int index) {
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
