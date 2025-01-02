package fzmm.zailer.me.client.gui.head_generator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.head_generator.components.AbstractHeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.gui.head_generator.components.HeadCompoundComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.options.ISkinPreEdit;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.utils.*;
import fzmm.zailer.me.utils.list.IListEntry;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.util.FocusHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HeadGeneratorScreen extends BaseFzmmScreen implements IMementoScreen {
    private static final int COMPOUND_HEAD_LAYOUT_WIDTH = 60;
    private static final int HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS = 1;
    public static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String SEARCH_ID = "search";
    private static final String CONTENT_ID = "content";
    private static final String COMPOUND_HEADS_LAYOUT_ID = "compound-heads-layout";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String HEAD_CATEGORY_ID = "head-category-button";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private static HeadGeneratorMemento memento = null;
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageRowsElements skinElements;
    private TextBoxComponent headNameField;
    private HashMap<SkinPreEditOption, ButtonComponent> skinPreEditButtons;
    private SkinPreEditOption selectedSkinPreEdit;
    private TextBoxComponent searchField;
    private List<HeadComponentEntry> headComponentEntries;
    private List<HeadCompoundComponentEntry> headCompoundComponentEntries;
    private FlowLayout contentLayout;
    private StyledFlowLayout compoundHeadsLayout;
    private ButtonWidget toggleFavoriteList;
    private boolean showFavorites;
    private BufferedImage baseSkin;
    private boolean hasUnusedPixels;
    private String previousSkinName;
    private IHeadCategory selectedCategory;
    private ButtonComponent giveButton;
    private Animation.Composed compoundExpandAnimation;
    private ContextMenuButton headCategoryButton;


    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
        this.favoritesHeadsOnOpenScreen = Set.copyOf(FzmmClient.CONFIG.headGenerator.favoriteSkins());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setup(FlowLayout rootComponent) {
        this.headComponentEntries = new ArrayList<>();
        this.headCompoundComponentEntries = new ArrayList<>();
        this.baseSkin = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        //general
        this.skinElements = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, ImageMode.NAME);
        this.skinElements.imageButton().setButtonCallback(this::imageCallback);
        this.previousSkinName = "";
        // ProfileComponent.PACKET_CODEC max size is 16
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "", 16);
        this.skinElements.valueField().onChanged().subscribe(this::onChangeSkinField);
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);
        this.compoundHeadsLayout = rootComponent.childById(StyledFlowLayout.class, COMPOUND_HEADS_LAYOUT_ID);
        checkNull(this.compoundHeadsLayout, "flow-layout", COMPOUND_HEADS_LAYOUT_ID);

        int animationDuration = 800;
        Animation<Insets> headsLayoutMarginAnimation = this.compoundHeadsLayout.margins()
                .animate(animationDuration, Easing.CUBIC, Insets.of(0, 0, 0, 6));
        Animation<Sizing> compoundHeadsLayoutAnimation = this.compoundHeadsLayout.horizontalSizing()
                .animate(animationDuration, Easing.CUBIC, Sizing.fixed(COMPOUND_HEAD_LAYOUT_WIDTH));
        Animation<Insets> compoundHeadsLayoutPaddingAnimation = this.compoundHeadsLayout.padding()
                .animate(animationDuration, Easing.CUBIC, Insets.of(3));
        this.compoundExpandAnimation = Animation.compose(compoundHeadsLayoutAnimation, headsLayoutMarginAnimation, compoundHeadsLayoutPaddingAnimation);

        //bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));

        // nav var
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", 128, s -> this.applyFilters());

        this.skinPreEditButtons = new HashMap<>();
        for (SkinPreEditOption preEditOption : SkinPreEditOption.values()) {
            FlowLayout skinPreEditButtonLayout = rootComponent.childById(FlowLayout.class, preEditOption.getId());
            checkNull(skinPreEditButtonLayout, "flow-layout", preEditOption.getId());
            this.setupPreEditButton(skinPreEditButtonLayout, preEditOption, this.skinPreEditButtons, skinPreEditOption -> {
                this.selectedSkinPreEdit = skinPreEditOption;

                if (this.skinElements.imageButton().hasImage()) {
                    this.updatePreviews();
                }
            });
        }
        this.skinPreEditButtons.get(SkinPreEditOption.OVERLAP).onPress();

        this.headCategoryButton = rootComponent.childById(ContextMenuButton.class, HEAD_CATEGORY_ID);
        checkNull(this.headCategoryButton, "label", HEAD_CATEGORY_ID);

        this.selectedCategory = IHeadCategory.NATURAL_CATEGORIES[0];
        this.updateCategoryTitle(this.selectedCategory);
        int maxCategoryHorizontalSizing = FzmmUtils.getMaxWidth(Arrays.asList(IHeadCategory.NATURAL_CATEGORIES),
                this::getCategoryText) + BUTTON_TEXT_PADDING;

        this.headCategoryButton.horizontalSizing(Sizing.fixed(maxCategoryHorizontalSizing));
        this.headCategoryButton.setContextMenuOptions(contextMenu -> {
            for (var category : IHeadCategory.NATURAL_CATEGORIES) {
                contextMenu.button(Text.translatable(category.getTranslationKey()), dropdown -> this.updateCategory(category));
            }
        });

        this.toggleFavoriteList = ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, buttonComponent -> this.toggleFavoriteListExecute());
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = FzmmUtils.getMaxWidth(List.of(HeadComponentEntry.FAVORITE_DISABLED_TEXT, HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, buttonComponent -> this.wikiExecute());

        this.tryLoadHeadEntries(rootComponent);
        this.updatePreviews();
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.skinElements.valueField(), Component.FocusSource.MOUSE_CLICK);
    }

    private void updateCategory(IHeadCategory category) {
        this.selectedCategory = category;
        this.applyFilters();
        this.updateCategoryTitle(category);
        this.updateTogglePreEdit();
    }

    private void updateCategoryTitle(IHeadCategory category) {
        this.headCategoryButton.setMessage(this.getCategoryText(category));
    }

    private void updateTogglePreEdit() {
        if (!FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels()) {
            return;
        }

        for (var preEditOption : this.skinPreEditButtons.keySet()) {
            this.skinPreEditButtons.get(preEditOption).active = !this.selectedCategory.isModel() && preEditOption != this.selectedSkinPreEdit;
        }
    }

    @SuppressWarnings("All")
    private MutableText getCategoryText(IHeadCategory category) {
        return Text.translatable("fzmm.gui.headGenerator.label.category", Text.translatable(category.getTranslationKey()));
    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null) {
            return;
        }

        if (ImageUtils.isEquals(skinBase, this.baseSkin)) {
            return;
        }
        this.hasUnusedPixels = ImageUtils.hasUnusedPixel(skinBase);

        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = InternalModels.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase, this.hasUnusedPixels);
            this.skinElements.imageButton().setImage(skinBase);
        }

        this.baseSkin = skinBase;

        this.updatePreviews();
    }

    private void tryLoadHeadEntries(FlowLayout rootComponent) {
        if (!this.contentLayout.children().isEmpty()) {
            return;
        }

        List<HeadComponentEntry> headComponentList = HeadResourcesLoader.getLoaded().stream()
                .map(entry -> new HeadComponentEntry(entry, this))
                .toList();

        if (headComponentList.isEmpty()) {
            this.addNoResultsMessage(rootComponent);
            return;
        }

        this.headComponentEntries.addAll(headComponentList);
        this.applyFilters();
    }

    private void addNoResultsMessage(FlowLayout parent) {
        FzmmClient.LOGGER.warn("[HeadGeneratorScreen] No head entries found");
        Component label = StyledComponents.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                        .setStyle(Style.EMPTY.withColor(FzmmStyles.TEXT_ERROR_COLOR.rgb())))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.expand(100), Sizing.content())
                .margins(Insets.top(4));
        FlowLayout layout = parent.childById(FlowLayout.class, "no-results-label-layout");
        checkNull(layout, "flow-layout", "no-results-label-layout");
        layout.child(label);
    }


    public void updatePreviews() {
        assert this.client != null;

        //noinspection resource
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        boolean editingBody = this.headCompoundComponentEntries.stream()
                .anyMatch(entry -> entry.getValue().isEditingSkinBody());

        // generate pre-edit textures for all entries
        SkinPreEditOption skinPreEditOption = this.skinPreEdit();
        boolean forcePreEditNone = FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels();
        boolean isSlim = ImageUtils.isSlimSimpleCheck(this.baseSkin);
        BufferedImage selectedPreEdit = this.skinPreEdit(this.baseSkin, skinPreEditOption, isSlim, editingBody);
        BufferedImage bodyTexturePreEdit = editingBody ? selectedPreEdit : this.skinPreEdit(this.baseSkin, skinPreEditOption, isSlim, true);
        BufferedImage nonePreEdit = this.skinPreEdit(this.baseSkin, SkinPreEditOption.NONE, isSlim, editingBody);

        // update head previews in client thread with 1ms of delay between each
        AtomicInteger index = new AtomicInteger(1);
        for (int i = 0; i != this.headComponentEntries.size(); i++) {
            HeadComponentEntry entry = this.headComponentEntries.get(i);

            scheduler.schedule(() -> {
                // components must be updated in the client thread otherwise it may cause a crash
                this.client.execute(() -> {
                    BufferedImage baseTexture;
                    if (forcePreEditNone && entry.getValue() instanceof HeadModelEntry) {
                        baseTexture = nonePreEdit;
                    } else if (entry.getValue().isEditingSkinBody()) {
                        baseTexture = bodyTexturePreEdit;
                    } else {
                        baseTexture = selectedPreEdit;
                    }
                    // FIXME: ConcurrentModificationException: in INestedParameters.getNestedParameters(INestedParameters.java:21)
                    entry.basePreview(baseTexture, this.hasUnusedPixels);
                    entry.updateModel(isSlim);
                });
            }, (long) HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS * index.getAndIncrement(), TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(() -> {
            selectedPreEdit.flush();
            bodyTexturePreEdit.flush();
            nonePreEdit.flush();
        }, (this.headComponentEntries.size() + 2) * HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    private void addCompoundEntriesToTexture(Graphics2D graphics, BufferedImage texture, boolean isSlim, boolean editBody) {
        ISkinPreEdit none = SkinPreEditOption.NONE.getPreEdit();
        ISkinPreEdit overlap = SkinPreEditOption.OVERLAP.getPreEdit();

        for (var headEntry : this.headCompoundComponentEntries) {
            MinecraftClient.getInstance().execute(() -> {
                headEntry.basePreview(texture, this.hasUnusedPixels);
                headEntry.updateModel(isSlim);
            });

            none.apply(graphics, headEntry.getPreview());
            overlap.apply(graphics, texture, editBody);
        }
    }

    public BufferedImage skinPreEdit(SkinPreEditOption skinPreEditOption, boolean editBody) {
        return this.skinPreEdit(this.baseSkin, skinPreEditOption, ImageUtils.isSlimSimpleCheck(this.baseSkin), editBody);
    }

    public BufferedImage skinPreEdit(BufferedImage preview, SkinPreEditOption skinPreEditOption, boolean isSlim, boolean editBody) {
        BufferedImage result = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();

        skinPreEditOption.getPreEdit().apply(graphics, preview, SkinPart.HEAD);
        (editBody ? skinPreEditOption : SkinPreEditOption.NONE).getPreEdit().apply(graphics, preview, SkinPart.BODY_PARTS);

        if (this.hasUnusedPixels) {
            ImageUtils.copyUnusedPixels(preview, graphics);
        }

        this.addCompoundEntriesToTexture(graphics, result, isSlim, editBody);

        graphics.dispose();
        return result;
    }

    public boolean hasUnusedPixels() {
        return this.hasUnusedPixels;
    }

    public void setupPreEditButton(FlowLayout preEditLayout, SkinPreEditOption preEditOption,
                                   HashMap<SkinPreEditOption, ButtonComponent> skinPreEditButtons,
                                   Consumer<SkinPreEditOption> selectPreEditCallback) {
        preEditLayout.tooltip(Text.translatable(preEditOption.getTranslationKey() + ".tooltip"));

        ButtonComponent preEditButton = Components.button(Text.empty(), button -> {
            selectPreEditCallback.accept(preEditOption);

            for (var option : skinPreEditButtons.keySet()) {
                if (option != preEditOption)
                    skinPreEditButtons.get(option).active = true;
            }
            button.active = false;
        });
        preEditButton.horizontalSizing(Sizing.fixed(20));
        preEditButton.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            preEditOption.getIcon().render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });

        skinPreEditButtons.put(preEditOption, preEditButton);
        preEditLayout.child(preEditButton);
    }

    private void closeTextures() {
        if (this.contentLayout == null)
            return;

        assert this.client != null;
        this.client.execute(() -> {
            this.closeTextures(this.headComponentEntries);
            this.closeTextures(this.headCompoundComponentEntries);
        });
    }

    private void closeTextures(List<? extends AbstractHeadComponentEntry> entries) {
        for (var entry : entries) {
            entry.close();
        }
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();

        for (var entry : this.headComponentEntries) {
            entry.filter(searchValue, this.showFavorites, this.selectedCategory);
        }

        List<Component> newResults = new ArrayList<>(this.headComponentEntries);
        newResults.removeIf(component -> component instanceof HeadComponentEntry entry && entry.isHide());
        this.contentLayout.clearChildren();
        this.contentLayout.children(newResults);
    }

    public void giveHead(BufferedImage image, String textureName) {
        assert this.client != null;
        this.client.execute(() -> {
            this.setUndefinedDelay();
            String headName = this.getHeadName();

            ISnackBarComponent snackBar = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GENERATOR_ID)
                    .title(Text.translatable("fzmm.gui.headGenerator.snack_bar.loading"))
                    .backgroundColor(FzmmStyles.ALERT_LOADING_COLOR)
                    .keepOnLimit()
                    .build();
            this.addSnackBar(snackBar);

            new HeadUtils().uploadHead(image, headName + " + " + textureName).thenAccept(headUtils -> {
                HeadBuilder builder = headUtils.getBuilder();
                if (!headName.isBlank()) {
                    builder.headName(headName);
                }

                boolean generated = ItemUtils.give(builder.get());

                this.client.execute(() -> {
                    this.setDelay(headUtils.getDelayForNext(TimeUnit.SECONDS));
                    snackBar.close();
                    if (generated) {
                        this.addStatusSnackBar(headUtils, image, textureName);
                    }
                });
            });
        });
    }

    private void addStatusSnackBar(HeadUtils headUtils, BufferedImage image, String textureName) {
        SnackBarBuilder snackBar = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GENERATOR_ID);
        if (headUtils.isSkinGenerated()) {
            snackBar.title(Text.translatable("fzmm.gui.headGenerator.snack_bar.success"))
                    .lowTimer()
                    .backgroundColor(FzmmStyles.ALERT_SUCCESS_COLOR)
                    .startTimer();
        } else if (headUtils.getHttpResponseCode() == 403) {
            snackBar.title(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey"))
                    .details(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey.description"))
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .keepOnLimit()
                    .button(iSnackBarComponent -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                            buttonComponent -> this.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this))))
                    .highTimer()
                    .closeButton();
        } else {
            String translationKey = headUtils.getHttpResponseCode() / 500 == 5 ? "external" : "internal";

            snackBar.title(Text.translatable("fzmm.gui.headGenerator.snack_bar.error." + translationKey))
                    .details(Text.translatable("fzmm.gui.headGenerator.snack_bar.error." + translationKey + ".description", headUtils.getHttpResponseCode()))
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .keepOnLimit()
                    .button(iSnackBarComponent -> Components.button(Text.translatable("fzmm.gui.headGenerator.snack_bar.error.button.retry"), buttonComponent -> {
                        this.giveHead(image, textureName);
                        iSnackBarComponent.close();
                    }))
                    .highTimer()
                    .closeButton();
        }

        this.addSnackBar(snackBar.build());
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButton(waitMessage, false);
    }

    public void setDelay(int seconds) {
        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButton(message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS)
                .execute(() -> this.updateButton(HeadComponentOverlay.GIVE_BUTTON_TEXT, true));
    }

    public void updateButton(Text message, boolean active) {
        if (this.giveButton != null) {
            this.giveButton.setMessage(message);
            this.giveButton.active = active;
        }
    }

    public void setCurrentGiveButton(ButtonComponent currentGiveButton) {
        if (this.giveButton != null) {
            Text message = this.giveButton.getMessage();
            boolean active = this.giveButton.active;
            this.giveButton = currentGiveButton;
            this.updateButton(message, active);
        } else {
            this.giveButton = currentGiveButton;
        }
    }

    public String getHeadName() {
        return this.headNameField.getText();
    }

    public void addCompound(AbstractHeadEntry headData, BufferedImage currentPreview) {
        assert this.client != null;

        List<Component> compoundHeads = this.compoundHeadsLayout.children();
        if (compoundHeads.isEmpty()) {
            this.compoundExpandAnimation.forwards();
            this.compoundHeadsLayout.surface(this.compoundHeadsLayout.styledPanel());
        }

        HeadCompoundComponentEntry entry = new HeadCompoundComponentEntry(headData, this.compoundHeadsLayout, this, currentPreview);

        this.headCompoundComponentEntries.add(entry);
        this.compoundHeadsLayout.child(entry);
        this.updatePreviews();
    }

    public void removeCompound(HeadCompoundComponentEntry entry) {
        assert this.parent != null;
        this.headCompoundComponentEntries.remove(entry);
        entry.remove();

        if (this.headCompoundComponentEntries.isEmpty()) {
            this.compoundExpandAnimation.backwards();
            this.compoundHeadsLayout.surface(Surface.BLANK);
        }

        this.updatePreviews();
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

        ConfirmLinkScreen.open(this.client.currentScreen, FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK);
    }

    public SkinPreEditOption skinPreEdit() {
        return this.selectedSkinPreEdit;
    }

    public void upCompoundEntry(AbstractHeadComponentEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadComponentEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.upEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    public void downCompoundEntry(AbstractHeadComponentEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadComponentEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.downEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    @Override
    public void close() {
        super.close();
        this.closeTextures();
    }

    @Override
    public void removed() {
        super.removed();

        if (!this.favoritesHeadsOnOpenScreen.equals(FzmmClient.CONFIG.headGenerator.favoriteSkins())) {
            FzmmClient.CONFIG.save();
        }
    }

    private void onChangeSkinField(String value) {
        AtomicReference<ImageMode> mode = this.skinElements.mode();

        if (mode.get().isHeadName() && this.headNameField.getText().equals(this.previousSkinName)) {
            this.headNameField.text(value);
        }

        this.previousSkinName = value;
    }

    @Override
    public void setMemento(IMementoObject memento) {
        HeadGeneratorScreen.memento = (HeadGeneratorMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new HeadGeneratorMemento(
                this.headNameField.getText(),
                this.skinElements.mode().get(),
                this.skinElements.valueField().getText(),
                this.showFavorites,
                this.skinPreEdit(),
                this.selectedCategory,
                this.searchField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        HeadGeneratorMemento memento = (HeadGeneratorMemento) mementoObject;
        this.skinElements.imageModeButtons().get(memento.skinMode).onPress();
        this.skinElements.valueField().text(memento.skinRowValue);
        this.headNameField.text(memento.headName);
        if (memento.showFavorites)
            this.toggleFavoriteListExecute();
        this.skinPreEditButtons.get(memento.skinPreEditOption).onPress();
        this.updateCategory(memento.category);
        this.searchField.text(memento.search);
    }

    private record HeadGeneratorMemento(String headName, ImageMode skinMode, String skinRowValue, boolean showFavorites,
                                        SkinPreEditOption skinPreEditOption, IHeadCategory category,
                                        String search) implements IMementoObject {
    }
}