package fzmm.zailer.me.client.gui.head_generator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.head_generator.components.AbstractHeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.gui.head_generator.components.HeadCompoundComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.options.ISkinPreEdit;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.utils.*;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.util.FocusHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

public class HeadGeneratorScreen extends BaseFzmmScreen implements IMemento {
    private static final int COMPOUND_HEAD_LAYOUT_WIDTH = 60;
    private static final int HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS = 1;
    public static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageRowsElements skinElements;
    private TextBoxComponent headNameField;
    private HashMap<SkinPreEditOption, EButtonComponent> skinPreEditButtons;
    private SkinPreEditOption selectedSkinPreEdit;
    private TextBoxComponent searchField;
    private List<HeadComponentEntry> headComponentEntries;
    private List<HeadCompoundComponentEntry> compoundEntries;
    private FlowLayout contentLayout;
    private EFlowLayout compoundHeadsLayout;
    private ButtonComponent toggleFavoriteList;
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
    protected void setup(EFlowLayout rootComponent) {
        this.headComponentEntries = new ArrayList<>();
        this.compoundEntries = new ArrayList<>();
        this.baseSkin = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        //general
        this.skinElements = ImageRows.setup(rootComponent, "skin", "skinSourceType", ImageMode.NAME);
        this.skinElements.imageButton().setButtonCallback(skinOptional -> skinOptional.ifPresent(this::skinCallback));
        this.previousSkinName = "";
        // ProfileComponent.PACKET_CODEC max size is 16
        this.headNameField = TextBoxRow.setup(rootComponent, "headName", "", 16);
        this.skinElements.valueField().onChanged().subscribe(this::onChangeSkinField);
        this.contentLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "content");
        this.compoundHeadsLayout = rootComponent.childByIdOrThrow(EFlowLayout.class, "compound-heads-layout");

        int animationDuration = 800;
        Animation<Insets> headsLayoutMarginAnimation = this.compoundHeadsLayout.margins()
                .animate(animationDuration, Easing.CUBIC, Insets.of(0, 0, 0, 6));
        Animation<Sizing> compoundHeadsLayoutAnimation = this.compoundHeadsLayout.horizontalSizing()
                .animate(animationDuration, Easing.CUBIC, Sizing.fixed(COMPOUND_HEAD_LAYOUT_WIDTH));
        Animation<Insets> compoundHeadsLayoutPaddingAnimation = this.compoundHeadsLayout.padding()
                .animate(animationDuration, Easing.CUBIC, Insets.of(6));
        this.compoundExpandAnimation = Animation.compose(compoundHeadsLayoutAnimation, headsLayoutMarginAnimation, compoundHeadsLayoutPaddingAnimation);

        //bottom buttons
        ButtonComponent openSkinFolderButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "open-folder-button");
        openSkinFolderButton.onPress(button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));

        // nav var
        this.searchField = TextBoxRow.setup(rootComponent, "search", "", 128, s -> this.applyFilters());

        this.skinPreEditButtons = new HashMap<>();
        for (SkinPreEditOption preEditOption : SkinPreEditOption.values()) {
            FlowLayout skinPreEditButtonLayout = rootComponent.childByIdOrThrow(FlowLayout.class, preEditOption.getId());
            this.setupPreEditButton(skinPreEditButtonLayout, preEditOption, this.skinPreEditButtons, skinPreEditOption -> {
                this.selectedSkinPreEdit = skinPreEditOption;

                if (this.skinElements.imageButton().hasImage()) {
                    this.updateContentPreviews();
                }
            });
        }
        this.skinPreEditButtons.get(SkinPreEditOption.OVERLAP).onPress();

        this.headCategoryButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "head-category-button");

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

        this.toggleFavoriteList = rootComponent.childByIdOrThrow(ButtonComponent.class, "toggle-favorite-list");
        this.toggleFavoriteList.onPress(buttonComponent -> this.toggleFavoriteListExecute());
        this.showFavorites = false;
        int toggleFavoriteListWidth = FzmmUtils.getMaxWidth(List.of(HeadComponentEntry.FAVORITE_DISABLED_TEXT, HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonComponent wikiButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "wiki-button");
        wikiButton.onPress(buttonComponent -> this.wikiExecute());

        this.tryLoadHeadEntries(rootComponent);
        this.updateContentPreviews();
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

    private void skinCallback(BufferedImage skinBase) {
        if (ImageUtils.isEquals(skinBase, this.baseSkin)) {
            return;
        }
        this.hasUnusedPixels = ImageUtils.hasUnusedPixel(skinBase);

        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = InternalModels.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase, this.hasUnusedPixels);
            this.skinElements.imageButton().setImage(skinBase);
        }

        this.baseSkin = skinBase;

        this.updateCompoundPreviews(0);
        this.updateContentPreviews();
    }

    private void tryLoadHeadEntries(EFlowLayout rootComponent) {
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

    private void addNoResultsMessage(EFlowLayout parent) {
        FzmmClient.LOGGER.warn("[HeadGeneratorScreen] No head entries found");
        Component label = EComponents.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                        .setStyle(Style.EMPTY.withColor(EStyles.TEXT_ERROR_COLOR.rgb())))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.expand(100), Sizing.content())
                .margins(Insets.top(4));
        FlowLayout layout = parent.childByIdOrThrow(FlowLayout.class, "no-results-label-layout");
        layout.child(label);
    }


    public void updateContentPreviews() {
        assert this.client != null;

        //noinspection resource
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        boolean editingBody = this.isEditingBody();

        // generate pre-edit textures for all entries
        SkinPreEditOption skinPreEditOption = this.skinPreEdit();
        boolean forcePreEditNone = FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels();
        boolean isSlim = ImageUtils.isSlimSimpleCheck(this.baseSkin);
        BufferedImage selectedPreEdit = this.preEditContent(skinPreEditOption, editingBody);
        BufferedImage bodyTexturePreEdit = editingBody ? selectedPreEdit : this.preEditContent(skinPreEditOption, true);
        BufferedImage nonePreEdit = this.preEditContent(SkinPreEditOption.NONE, editingBody);

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

    public void updateCompoundPreviews(HeadCompoundComponentEntry modifiedEntry, int indexOffset) {
        this.updateCompoundPreviews(this.compoundEntries.indexOf(modifiedEntry) + indexOffset);
    }

    private void updateCompoundPreviews(int index) {
        if (index < 0 || index >= this.compoundEntries.size()) {
            return;
        }

        BufferedImage texture = this.getBaseTextureOfCompound(index);

        boolean isEditingBody = this.isEditingBody();
        BufferedImage textureCopy = new BufferedImage(texture.getWidth(), texture.getHeight(), BufferedImage.TYPE_INT_ARGB);
        textureCopy.getGraphics().drawImage(texture, 0, 0, null);
        Graphics2D graphics = textureCopy.createGraphics();
        ISkinPreEdit none = SkinPreEditOption.NONE.getPreEdit();
        ISkinPreEdit overlap = SkinPreEditOption.OVERLAP.getPreEdit();

        // overlap previous entry
        SkinPreEditOption.OVERLAP.getPreEdit().apply(graphics, textureCopy, isEditingBody);

        for (int i = index; i != this.compoundEntries.size(); i++) {
            HeadCompoundComponentEntry compoundEntry = this.compoundEntries.get(i);
            compoundEntry.basePreview(textureCopy, this.hasUnusedPixels);

            // add head texture and overlap to next entry
            none.apply(graphics, compoundEntry.getPreview());
            overlap.apply(graphics, textureCopy, isEditingBody);
        }

        graphics.dispose();
        textureCopy.flush();
    }

    private void updateCompoundSkinFormat() {
        boolean isEditingBody = this.isEditingBody();
        boolean isSlim = ImageUtils.isSlimSimpleCheck(this.baseSkin);
        for (var compoundEntry : this.compoundEntries) {
            compoundEntry.setBodyPreview(isEditingBody || compoundEntry.getValue().isEditingSkinBody());
            compoundEntry.updateModel(isSlim);
        }
    }

    private BufferedImage getBaseTextureOfCompound(int index) {
        if (index == 0) {
            return this.baseSkin;
        } else {
            return this.compoundEntries.get(index - 1).getPreview();
        }
    }

    private boolean isEditingBody() {
        return this.compoundEntries.stream()
                .anyMatch(entry -> entry.getValue().isEditingSkinBody());
    }

    public BufferedImage preEdit(AbstractHeadComponentEntry entry, SkinPreEditOption skinPreEditOption, boolean editBody) {
        if (entry instanceof HeadCompoundComponentEntry) {
            BufferedImage baseTexture = this.getBaseTextureOfCompound(this.compoundEntries.indexOf(entry));
            return this.preEditCompound(baseTexture, skinPreEditOption, editBody);
        }

        return this.preEditContent(skinPreEditOption, editBody);
    }

    public BufferedImage preEditCompound(BufferedImage preview, SkinPreEditOption skinPreEditOption, boolean editBody) {
        BufferedImage result = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();

        skinPreEditOption.getPreEdit().apply(graphics, preview, SkinPart.HEAD);
        (editBody ? skinPreEditOption : SkinPreEditOption.NONE).getPreEdit().apply(graphics, preview, SkinPart.BODY_PARTS);

        if (this.hasUnusedPixels) {
            ImageUtils.copyUnusedPixels(preview, graphics);
        }

        graphics.dispose();
        return result;
    }

    private BufferedImage preEditContent(SkinPreEditOption skinPreEditOption, boolean editBody) {
        BufferedImage texture = this.baseSkin;

        // use last compound entry texture as base
        if (!this.compoundEntries.isEmpty()) {
            texture = this.compoundEntries.get(this.compoundEntries.size() - 1).getPreview();

            Graphics2D graphics = texture.createGraphics();
            skinPreEditOption.getPreEdit().apply(graphics, texture);
            graphics.dispose();
        }

        return this.preEditCompound(texture, skinPreEditOption, editBody);
    }

    public boolean hasUnusedPixels() {
        return this.hasUnusedPixels;
    }

    public void setupPreEditButton(FlowLayout preEditLayout, SkinPreEditOption preEditOption,
                                   HashMap<SkinPreEditOption, EButtonComponent> skinPreEditButtons,
                                   Consumer<SkinPreEditOption> selectPreEditCallback) {
        preEditLayout.tooltip(Text.translatable(preEditOption.getTranslationKey() + ".tooltip"));

        EButtonComponent preEditButton = EComponents.button(Text.empty());
        preEditButton.onPress(button -> {
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
            this.closeTextures(this.compoundEntries);
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
                    .backgroundColor(EStyles.ALERT_LOADING_COLOR)
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
                    .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                    .startTimer();
        } else if (headUtils.getHttpResponseCode() == 403) {
            snackBar.title(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey"))
                    .details(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey.description"))
                    .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                    .keepOnLimit()
                    .button(iSnackBarComponent -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                            buttonComponent -> this.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this))))
                    .highTimer()
                    .closeButton();
        } else {
            String translationKey = headUtils.getHttpResponseCode() / 500 == 5 ? "external" : "internal";

            snackBar.title(Text.translatable("fzmm.gui.headGenerator.snack_bar.error." + translationKey))
                    .details(Text.translatable("fzmm.gui.headGenerator.snack_bar.error." + translationKey + ".description", headUtils.getHttpResponseCode()))
                    .backgroundColor(EStyles.ALERT_ERROR_COLOR)
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

        this.compoundEntries.add(entry);
        this.compoundHeadsLayout.child(entry);
        this.updateCompoundSkinFormat();
        this.updateContentPreviews();
    }

    public void removeCompound(HeadCompoundComponentEntry entry) {
        int index = this.compoundEntries.indexOf(entry);
        this.compoundEntries.remove(entry);
        entry.remove();

        if (this.compoundEntries.isEmpty()) {
            this.compoundExpandAnimation.backwards();
            this.compoundHeadsLayout.surface(Surface.BLANK);
        }

        this.updateCompoundSkinFormat();
        this.updateCompoundPreviews(index);
        this.updateContentPreviews();
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

        ConfirmLinkScreen.open(this.client.currentScreen, FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK, true);
    }

    public SkinPreEditOption skinPreEdit() {
        return this.selectedSkinPreEdit;
    }

    public void upCompoundEntry(HeadCompoundComponentEntry entry) {
        List<AbstractHeadComponentEntry> list = this.compoundHeadsLayout.children().stream()
                .map(component -> (AbstractHeadComponentEntry) component)
                .toList();

        ListUtils.upEntry(list, entry);
        this.updateCompoundPreviews(entry, -1);
        this.updateContentPreviews();
    }

    public void downCompoundEntry(HeadCompoundComponentEntry entry) {
        List<AbstractHeadComponentEntry> list = this.compoundHeadsLayout.children().stream()
                .map(component -> (AbstractHeadComponentEntry) component)
                .toList();

        ListUtils.downEntry(list, entry);
        this.updateCompoundPreviews(entry, 0);
        this.updateContentPreviews();
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
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.skinElements.mode().get());
        output.writeObject(this.skinElements.valueField().getText());
        output.writeObject(this.headNameField.getText());
        output.writeBoolean(this.showFavorites);
        output.writeObject(this.skinPreEdit());
        output.writeObject(this.selectedCategory);
        output.writeObject(this.searchField.getText());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.skinElements.imageModeButtons().get((ImageMode) input.readObject()).onPress();
        this.skinElements.valueField().text((String) input.readObject());
        this.headNameField.text((String) input.readObject());
        if (input.readBoolean()) { // showFavorites
            this.toggleFavoriteListExecute();
        }
        this.skinPreEditButtons.get((SkinPreEditOption) input.readObject()).onPress();
        this.updateCategory((IHeadCategory) input.readObject());
        this.searchField.text((String) input.readObject());
    }
}