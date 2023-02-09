package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadLayerComponentEntry;
import fzmm.zailer.me.client.logic.headGenerator.texture.HeadTextureEntry;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.client.logic.headGenerator.HeadGeneratorResources;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class HeadGeneratorScreen extends BaseFzmmScreen {
    private static final String HEAD_GENERATOR_WIKI_LINK = "https://github.com/Zailer43/FZMM-Mod/wiki/Head-Generator-Wiki";
    private static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String OVERLAP_HAT_LAYER_ID = "overlapHatLayer";
    private static final String SEARCH_ID = "search";
    private static final String HEAD_LIST_ID = "head-list";
    private static final String LAYER_LIST_ID = "layer-list";
    private static final String GIVE_MERGED_HEAD_ID = "give";
    private static final String SAVE_SKIN_ID = "save-skin";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String HEAD_GENERATION_METHOD_ID = "head-generation-method";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageButtonComponent skinButton;
    private TextFieldWidget headNameField;
    private ConfigToggleButton overlapHatLayerButton;
    private TextFieldWidget searchField;
    private FlowLayout headListLayout;
    private FlowLayout layerListLayout;
    private ButtonWidget giveMergedHeadButton;
    private ButtonWidget toggleFavoriteList;
    private EnumWidget headGenerationMethod;
    private EnumWidget skinMode;
    private boolean showFavorites;
    private BufferedImage baseSkin;

    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
        this.favoritesHeadsOnOpenScreen = Set.copyOf(FzmmClient.CONFIG.headGenerator.favoriteSkins());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        this.skinButton = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, SkinMode.NAME);
        this.skinButton.setButtonCallback(this::imageCallback);
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "", 512);
        this.skinMode = rootComponent.childById(EnumWidget.class, EnumRow.getEnumId(SKIN_SOURCE_TYPE_ID));
        rootComponent.childById(TextFieldWidget.class, ImageButtonRow.getImageValueFieldId(SKIN_ID))
                .setChangedListener(this::onChangeSkinField);
        this.overlapHatLayerButton = BooleanRow.setup(rootComponent, OVERLAP_HAT_LAYER_ID, FzmmClient.CONFIG.headGenerator.defaultOverlapHatLayer(), button -> this.client.execute(this::updatePreviews));
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", 128, s -> this.applyFilters());
        this.headListLayout = rootComponent.childById(FlowLayout.class, HEAD_LIST_ID);
        this.layerListLayout = rootComponent.childById(FlowLayout.class, LAYER_LIST_ID);
        checkNull(this.headListLayout, "flow-layout", HEAD_LIST_ID);
        checkNull(this.layerListLayout, "flow-layout", LAYER_LIST_ID);
        //bottom buttons
        this.giveMergedHeadButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(GIVE_MERGED_HEAD_ID), true, button -> this.getMergedHead().ifPresent(this::giveHead));
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(SAVE_SKIN_ID), true, this::saveSkinExecute);
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));
        //other buttons
        this.headGenerationMethod = rootComponent.childById(EnumWidget.class, HEAD_GENERATION_METHOD_ID);
        checkNull(this.headGenerationMethod, "enum-option", HEAD_GENERATION_METHOD_ID);
        this.headGenerationMethod.init(HeadGenerationMethod.TEXTURE);
        this.headGenerationMethod.onPress(buttonComponent -> this.applyFilters());
        this.headGenerationMethod.select(HeadGenerationMethod.TEXTURE.ordinal());
        int maxHeadGenerationMethodWidth = 0;
        for (var method : HeadGenerationMethod.values())
            maxHeadGenerationMethodWidth = Math.max(maxHeadGenerationMethodWidth, this.textRenderer.getWidth(Text.translatable(method.getTranslationKey())) + BaseFzmmScreen.BUTTON_TEXT_PADDING);
        this.headGenerationMethod.horizontalSizing(Sizing.fixed(maxHeadGenerationMethodWidth));

        this.toggleFavoriteList = ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, buttonComponent -> this.toggleFavoriteListExecute());
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = Math.max(this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_DISABLED_TEXT), this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, buttonComponent -> this.wikiExecute());

    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null)
            return;


        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = ImageUtils.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase, false);
            this.skinButton.setImage(skinBase);
        }

        if (ImageUtils.isAlexModel(1, skinBase))
            skinBase = ImageUtils.convertInSteveModel(skinBase, 1);

        this.baseSkin = skinBase;

        this.client.execute(() -> {
            this.tryFirstSkinLoad();
            this.updatePreviews();
        });
    }

    private void tryFirstSkinLoad() {
        if (this.headListLayout.children().isEmpty()) {
            Set<AbstractHeadEntry> headDataSet = new HashSet<>();
            headDataSet.addAll(HeadGeneratorResources.loadHeadsTextures());
            headDataSet.addAll(HeadGeneratorResources.loadHeadsModels());

            if (headDataSet.size() == 0) {
                Component label = Components.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                                .setStyle(Style.EMPTY.withColor(0xD83F27)))
                        .horizontalTextAlignment(HorizontalAlignment.CENTER)
                        .sizing(Sizing.fill(100), Sizing.content())
                        .margins(Insets.top(4));
                this.headListLayout.child(label);
                return;
            }

            List<Component> headEntries = headDataSet.stream()
                    .sorted(Comparator.comparing(AbstractHeadEntry::getDisplayName))
                    .map(entry -> (Component) new HeadComponentEntry(entry, this))
                    .collect(Collectors.toList());

            this.headListLayout.children(headEntries);
            this.applyFilters();
        }

        if (this.layerListLayout.children().isEmpty()) {
            BufferedImage emptyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            HeadTextureEntry baseSkinData = new HeadTextureEntry(emptyImage, Text.translatable("fzmm.gui.headGenerator.label.baseSkin").getString(), "base");
            this.addLayer(baseSkinData, false);
        }

    }

    private void updatePreviews() {
        for (var component : this.headListLayout.children()) {
            if (component instanceof HeadComponentEntry headEntry)
                headEntry.update(this.baseSkin, this.overlapHatLayerButton());
        }

        for (var component : this.layerListLayout.children()) {
            if (component instanceof HeadLayerComponentEntry layerEntry)
                layerEntry.update(this.baseSkin, this.overlapHatLayerButton());
        }
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();
        List<Component> componentList = this.headListLayout.children();
        HeadGenerationMethod generationMethod = (HeadGenerationMethod) this.headGenerationMethod.getValue();

        for (var component : componentList) {
            if (component instanceof HeadComponentEntry headEntry)
                headEntry.filter(searchValue, this.showFavorites, generationMethod);
        }
    }

    public void giveHead(BufferedImage image) {
        assert this.client != null;
        this.client.execute(() -> {
            this.setUndefinedDelay();
            String headName = this.getHeadName();

            new HeadUtils().uploadHead(image, headName).thenAccept(headUtils -> {
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                HeadBuilder builder = headUtils.getBuilder();
                if (!headName.isBlank())
                    builder.headName(headName);

                FzmmUtils.giveItem(builder.get());
                this.client.execute(() -> this.setDelay(delay));
            });
        });
    }

    private Optional<BufferedImage> getMergedHead() {
        Optional<BufferedImage> optionalSkin = this.skinButton.getImage();
        if (optionalSkin.isEmpty())
            return Optional.empty();
        boolean onlyBaseSkin = this.layerListLayout.children().size() == 1;
        HeadGenerator headGenerator;
        BufferedImage skin = optionalSkin.get();
        if (onlyBaseSkin) {
            headGenerator = new HeadGenerator().addTexture(skin);
        } else {
            headGenerator = new HeadGenerator(skin, (boolean) this.overlapHatLayerButton.parsedValue());
        }

        for (var entry : this.layerListLayout.children()) {
            // it's a flowlayout so there can be any type of component here
            if (!(entry instanceof HeadLayerComponentEntry layerEntry))
                continue;

            layerEntry.getHeadSkin().ifPresent(headGenerator::addTexture);
        }

        return Optional.of(headGenerator.getHeadTexture());
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

    public void addLayer(AbstractHeadEntry headData) {
        this.addLayer(headData, true);
    }

    public void addLayer(AbstractHeadEntry headData, boolean active) {
        HeadLayerComponentEntry entry = new HeadLayerComponentEntry(headData, this.layerListLayout);
        entry.setEnabled(active);
        entry.update(this.baseSkin, this.overlapHatLayerButton());
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

    public BufferedImage getBaseSkin() {
        return this.baseSkin;
    }

    private void toggleFavoriteListExecute() {
        this.showFavorites = !this.showFavorites;
        this.updateToggleFavoriteText();
        this.applyFilters();
    }

    private void updateToggleFavoriteText() {
        this.toggleFavoriteList.setMessage(this.showFavorites ? HeadComponentEntry.FAVORITE_ENABLED_TEXT : HeadComponentEntry.FAVORITE_DISABLED_TEXT);
    }

    private void wikiExecute() {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(HEAD_GENERATOR_WIKI_LINK);

            this.client.setScreen(this);
        }, HEAD_GENERATOR_WIKI_LINK, true));
    }

    public boolean overlapHatLayerButton() {
        return (boolean) this.overlapHatLayerButton.parsedValue();
    }

    @Override
    public void close() {
        super.close();

        if (!this.favoritesHeadsOnOpenScreen.equals(FzmmClient.CONFIG.headGenerator.favoriteSkins()))
            FzmmClient.CONFIG.save();
    }

    private void onChangeSkinField(String value) {
        if (this.skinMode == null)
            return;

        if (((SkinMode) this.skinMode.getValue()).isHeadName())
            this.headNameField.setText(value);
    }
}