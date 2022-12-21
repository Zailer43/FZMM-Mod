package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadLayerComponentEntry;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.client.logic.headGenerator.HeadGeneratorResources;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends BaseFzmmScreen {
    private static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String SEARCH_ID = "search";
    private static final String HEAD_LIST_ID = "head-list";
    private static final String LAYER_LIST_ID = "layer-list";
    private static final String GIVE_MERGED_HEAD_ID = "give";
    private static final String SAVE_SKIN_ID = "save-skin";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private ImageButtonWidget skinButton;
    private TextFieldWidget headNameField;
    private TextFieldWidget searchField;
    private FlowLayout headListLayout;
    private FlowLayout layerListLayout;
    private ButtonWidget giveMergedHeadButton;


    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
    }

    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "head-generator-option-list",
                this.newImageRow(SKIN_ID),
                this.newEnumRow(SKIN_SOURCE_TYPE_ID),
                this.newTextFieldRow(HEAD_NAME_ID),
                this.newTextFieldRow(SEARCH_ID)
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        this.skinButton = this.setupImage(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, SkinMode.NAME);
        this.skinButton.setImageLoadedEvent(this::onLoadSkin);
        this.headNameField = this.setupTextField(rootComponent, HEAD_NAME_ID, "");
        rootComponent.childById(TextFieldWidget.class, this.getImageValueFieldId(SKIN_ID)).setChangedListener(this.headNameField::setText);
        this.searchField = this.setupTextField(rootComponent, SEARCH_ID, "", s -> this.applyFilters());
        this.headListLayout = rootComponent.childById(FlowLayout.class, HEAD_LIST_ID);
        this.layerListLayout = rootComponent.childById(FlowLayout.class, LAYER_LIST_ID);
        this.checkNull(this.headListLayout, "flow-layout", HEAD_LIST_ID);
        this.checkNull(this.layerListLayout, "flow-layout", LAYER_LIST_ID);
        //bottom buttons
        this.giveMergedHeadButton = this.setupButton(rootComponent, this.getButtonId(GIVE_MERGED_HEAD_ID), true, button -> this.getMergedHead().ifPresent(this::giveHead));
        this.setupButton(rootComponent, this.getButtonId(SAVE_SKIN_ID), true, this::saveSkinExecute);
        this.setupButton(rootComponent, this.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));
    }

    private ImageStatus onLoadSkin(BufferedImage skinBase) {
        assert this.client != null;
        this.client.execute(() -> {
            List<Component> headList = this.headListLayout.children();

            for (var component : headList) {
                if (component instanceof HeadComponentEntry headEntry)
                    headEntry.update(skinBase);
            }

            if (headList.isEmpty()) {
                List<HeadData> headDataList = HeadGeneratorResources.getHeadTexturesOf(skinBase);
                List<Component> headEntries = headDataList.stream()
                        .map(headData -> (Component) new HeadComponentEntry(headData, this))
                        .sorted(Comparator.comparing(component -> ((HeadComponentEntry) component).getName()))
                        .toList();

                this.headListLayout.children(headEntries);
                this.applyFilters();
            }

            for (var component : this.layerListLayout.children()) {
                if (component instanceof HeadLayerComponentEntry layerEntry)
                    layerEntry.update(skinBase);
            }

            if (this.layerListLayout.children().isEmpty()) {
                HeadLayerComponentEntry layerEntry = new HeadLayerComponentEntry(new HeadData(skinBase, Text.translatable("fzmm.gui.headGenerator.label.baseSkin").getString()), this.layerListLayout);
                layerEntry.setEnabled(false);
                this.layerListLayout.child(layerEntry);
            }
        });
        return ImageStatus.IMAGE_LOADED;
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();
        List<Component> componentList = this.headListLayout.children();

        for (var component : componentList) {
            if (component instanceof HeadComponentEntry headEntry)
                headEntry.filter(searchValue);
        }
    }

    public void giveHead(BufferedImage image) {
        new Thread(() -> {
            try {
                this.setUndefinedDelay();
                String headName = this.getHeadName();
                if (headName == null)
                    headName = "NULL";

                HeadUtils headUtils = new HeadUtils().uploadHead(image, headName);
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                ItemStack head = headUtils.getHead(headName);
                FzmmUtils.giveItem(head);
                this.setDelay(delay);
            } catch (IOException ignored) {
            }
        }, "Head generator").start();
    }

    private Optional<BufferedImage> getMergedHead() {
        HeadGenerator headGenerator = null;

        for (var entry : this.layerListLayout.children()) {
            if (!(entry instanceof HeadLayerComponentEntry layerEntry))
                continue;

            if (headGenerator == null)
                headGenerator = new HeadGenerator(layerEntry.getPreviewImage());
            else
                layerEntry.getHeadTextureByName().ifPresent(headGenerator::addTexture) ;
        }

        return headGenerator == null ? Optional.empty() : Optional.of(headGenerator.getHeadTexture());
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButtons(this.getHeadEntries(), waitMessage, false);
    }

    private List<HeadComponentEntry> getHeadEntries() {
        return this.headListLayout.children().stream().map(component -> (HeadComponentEntry) component).toList();
    }

    public void setDelay(int seconds) {
        List<HeadComponentEntry> entryList = this.getHeadEntries();

        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButtons(entryList, message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> this.updateButtons(entryList, HeadComponentEntry.GIVE_BUTTON_TEXT, true));
    }

    public void updateButtons(List<HeadComponentEntry> entryList, Text message, boolean active) {
        this.giveMergedHeadButton.active = active;
        this.giveMergedHeadButton.setMessage(message);
        for (var entry : entryList)
            entry.updateGiveButton(active, message);
    }

    public String getHeadName() {
        return this.headNameField.getText();
    }

    public void addLayer(HeadData headData) {
        HeadLayerComponentEntry entry = new HeadLayerComponentEntry(headData, this.layerListLayout);
        this.layerListLayout.child(entry);
    }

    public void saveSkinExecute(ButtonWidget button) {
        Optional<BufferedImage> optionalSkin = this.getMergedHead();
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        if (optionalSkin.isEmpty()) {
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.thereIsNoSkin")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return;
        }

        BufferedImage skin = optionalSkin.get();
        this.addBody(skin);
        File file = SKIN_SAVE_FOLDER_PATH.toFile();
        if (file.mkdirs())
            FzmmClient.LOGGER.info("Skin save folder created");

        file = ScreenshotRecorder.getScreenshotFilename(file);
        try {
            ImageIO.write(skin, "png", file);
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saved")
                    .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR)));
        } catch (IOException e) {
            e.printStackTrace();
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saveError")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }
    }

    private void addBody(BufferedImage head) {
        this.skinButton.getImage().ifPresent(image -> {
                    Graphics2D g2d = head.createGraphics();
                    g2d.drawImage(image, 0, 16, 64, 64, 0, 16, 64, 64, null);
                    g2d.dispose();
                }
        );
    }
}