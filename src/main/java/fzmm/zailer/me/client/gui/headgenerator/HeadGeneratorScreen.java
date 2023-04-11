package fzmm.zailer.me.client.gui.headgenerator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.containers.VerticalGridLayout;
import fzmm.zailer.me.client.gui.components.image.mode.SkinMode;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.headgenerator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.headgenerator.components.AbstractHeadListEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.headgenerator.components.HeadCompoundComponentEntry;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.gui.utils.IMementoScreen;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.headGenerator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.headGenerator.TextureOverlap;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.FzmmWikiConstants;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.list.IListEntry;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends BaseFzmmScreen implements IMementoScreen {
    private static final int COMPOUND_HEAD_LAYOUT_WIDTH = 60;
    public static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String OVERLAP_HAT_LAYER_ID = "overlapHatLayer";
    private static final String SEARCH_ID = "search";
    private static final String HEAD_GRID_ID = "head-grid";
    private static final String HEADS_LAYOUT_ID = "heads-layout";
    private static final String CONTENT_LAYOUT_ID = "content-layout";
    private static final String COMPOUND_HEADS_LAYOUT_ID = "compound-heads-layout";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String HEAD_CATEGORY_ID = "head-category-collapsible";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private static HeadGeneratorMemento memento = null;
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageRowsElements skinElements;
    private TextFieldWidget headNameField;
    private BooleanButton overlapHatLayerButton;
    private TextFieldWidget searchField;
    private List<HeadComponentEntry> headComponentEntries;
    private List<HeadCompoundComponentEntry> headCompoundComponentEntries;
    private VerticalGridLayout headGridLayout;
    private FlowLayout compoundHeadsLayout;
    private ButtonWidget toggleFavoriteList;
    private boolean showFavorites;
    private BufferedImage baseSkin;
    private BufferedImage gridBaseSkin;
    private String previousSkinName;
    private IHeadCategory selectedCategory;
    private ButtonComponent giveButton;
    private Animation.Composed compoundExpandAnimation;

    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
        this.favoritesHeadsOnOpenScreen = Set.copyOf(FzmmClient.CONFIG.headGenerator.favoriteSkins());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.headComponentEntries = new ArrayList<>();
        this.headCompoundComponentEntries = new ArrayList<>();
        this.baseSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.gridBaseSkin = this.baseSkin;
        //general
        this.skinElements = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, SkinMode.NAME);
        this.skinElements.imageButton().setButtonCallback(this::imageCallback);
        this.previousSkinName = "";
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "", 512);
        this.skinElements.valueField().onChanged().subscribe(this::onChangeSkinField);
        this.headGridLayout = rootComponent.childById(VerticalGridLayout.class, HEAD_GRID_ID);
        checkNull(this.headGridLayout, "vertical-grid-layout", HEAD_GRID_ID);
        this.compoundHeadsLayout = rootComponent.childById(FlowLayout.class, COMPOUND_HEADS_LAYOUT_ID);
        checkNull(this.compoundHeadsLayout, "flow-layout", COMPOUND_HEADS_LAYOUT_ID);

        FlowLayout contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_LAYOUT_ID);
        checkNull(contentLayout, "flow-layout", CONTENT_LAYOUT_ID);
        FlowLayout headsLayout = rootComponent.childById(FlowLayout.class, HEADS_LAYOUT_ID);
        checkNull(headsLayout, "flow-layout", HEADS_LAYOUT_ID);
        // owo-lib doesn't let to make Sizing.fill and Sizing.fill animations,
        // so I have to remove the percentage of compoundHeadsWidth on the screen size
        // note: this means that if the screen resolution changes it will be wrongly resized while expanded
        Window window = this.client.getWindow();
        int contentGap = (int) Math.floor(window.getScaleFactor() * contentLayout.gap());
        int newHeadsLayoutWidth = 99 - (int) Math.floor(((COMPOUND_HEAD_LAYOUT_WIDTH + contentGap * contentLayout.children().size() - 1) / (float) window.getScaledWidth()) * 100);
        Animation<Sizing> headsLayoutAnimation = headsLayout.horizontalSizing().animate(800, Easing.CUBIC, Sizing.fill(newHeadsLayoutWidth));
        Animation<Sizing> compoundHeadsLayoutAnimation = this.compoundHeadsLayout.horizontalSizing().animate(800, Easing.CUBIC, Sizing.fixed(COMPOUND_HEAD_LAYOUT_WIDTH));
        this.compoundExpandAnimation = Animation.compose(headsLayoutAnimation, compoundHeadsLayoutAnimation);

        //bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));

        // nav var
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", 128, s -> this.applyFilters());
        this.overlapHatLayerButton = BooleanRow.setup(rootComponent, OVERLAP_HAT_LAYER_ID, FzmmClient.CONFIG.headGenerator.defaultOverlapHatLayer(), button -> {
            if (this.skinElements.imageButton().hasImage())
                this.client.execute(this::updatePreviews);
        });
        this.overlapHatLayerButton.setContentHorizontalSizing();
        CollapsibleContainer headCategoryCollapsible = rootComponent.childById(CollapsibleContainer.class, HEAD_CATEGORY_ID);
        checkNull(headCategoryCollapsible, "collapsible", HEAD_CATEGORY_ID);
        DropdownComponent headCategoryDropdown = Components.dropdown(Sizing.content());

        for (var category : IHeadCategory.NATURAL_CATEGORIES) {
            headCategoryDropdown.button(Text.translatable(category.getTranslationKey()), dropdownComponent -> {
                this.selectedCategory = category;
                this.applyFilters();
            });
        }
        this.selectedCategory = IHeadCategory.NATURAL_CATEGORIES[0];
        headCategoryCollapsible.child(headCategoryDropdown);
        headCategoryDropdown.zIndex(1000);

        this.toggleFavoriteList = ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, buttonComponent -> this.toggleFavoriteListExecute());
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = Math.max(this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_DISABLED_TEXT), this.textRenderer.getWidth(HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, buttonComponent -> this.wikiExecute());

        this.tryLoadHeadEntries(rootComponent);
    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null)
            return;


        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = ImageUtils.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase);
            this.skinElements.imageButton().setImage(skinBase);
        }

        if (ImageUtils.isAlexModel(1, skinBase))
            skinBase = ImageUtils.convertInSteveModel(skinBase, 1);

        this.baseSkin = skinBase;
        this.gridBaseSkin = this.baseSkin;

        this.client.execute(this::updatePreviews);
    }

    private void tryLoadHeadEntries(FlowLayout rootComponent) {
        if (this.headGridLayout.children().isEmpty()) {
            List<AbstractHeadEntry> headEntriesList = HeadResourcesLoader.getPreloaded();

            if (headEntriesList.size() == 0) {
                this.addNoResultsMessage(rootComponent);
                return;
            }

            List<HeadComponentEntry> headEntries = new ArrayList<>(headEntriesList.size());
            for (AbstractHeadEntry entry : headEntriesList) {
                headEntries.add(new HeadComponentEntry(entry, this));
            }

            this.headComponentEntries.addAll(headEntries);
            this.applyFilters();
        }

        this.updatePreviews();
    }

    private void addNoResultsMessage(FlowLayout parent) {
        Component label = Components.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                        .setStyle(Style.EMPTY.withColor(0xD83F27)))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.fill(100), Sizing.content())
                .margins(Insets.top(4));
        FlowLayout layout = parent.childById(FlowLayout.class, "no-results-label-layout");
        checkNull(layout, "flow-layout", "no-results-label-layout");
        layout.child(label);
    }


    private void updatePreviews() {
        assert this.client != null;

        this.client.execute(() -> {
            TextureOverlap textureOverlap = new TextureOverlap(this.baseSkin, this.overlapHatLayer());
            if (!this.overlapHatLayer())
                textureOverlap.removeHatLayer();

            BufferedImage previousPreview  = textureOverlap.getHeadTexture();

            for (var headEntry : this.headCompoundComponentEntries) {
                headEntry.update(previousPreview);
                previousPreview = headEntry.getPreview();
            }
            this.gridBaseSkin = previousPreview;

            for (var headEntry : this.headComponentEntries) {
                headEntry.update(this.gridBaseSkin);
            }
        });

    }

    public BufferedImage getGridBaseSkin() {
        return this.gridBaseSkin;
    }

    private void closeTextures() {
        if (this.headGridLayout == null)
            return;

        assert this.client != null;
        this.client.execute(() -> {
            this.closeTextures(this.headComponentEntries);
            this.closeTextures(this.headCompoundComponentEntries);
        });
    }

    private void closeTextures(List<? extends AbstractHeadListEntry> entries) {
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

        List<HeadComponentEntry> newResults = new ArrayList<>(this.headComponentEntries);
        newResults.removeIf(HeadComponentEntry::isHide);
        this.headGridLayout.clearChildren();
        this.headGridLayout.children(newResults);
    }

    public void giveHead(BufferedImage image, String textureName) {
        assert this.client != null;
        this.client.execute(() -> {
            this.setUndefinedDelay();
            String headName = this.getHeadName();

            new HeadUtils().uploadHead(image, headName + " + " + textureName).thenAccept(headUtils -> {
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                HeadBuilder builder = headUtils.getBuilder();
                if (!headName.isBlank())
                    builder.headName(headName);

                FzmmUtils.giveItem(builder.get());
                this.client.execute(() -> this.setDelay(delay));
            });
        });
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

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> this.updateButton(HeadComponentEntry.GIVE_BUTTON_TEXT, true));
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

    public void addCompound(AbstractHeadEntry headData) {
        assert this.client != null;

        List<Component> compoundHeads = this.compoundHeadsLayout.children();
        if (compoundHeads.isEmpty()) {
            this.compoundExpandAnimation.forwards();
            this.compoundHeadsLayout.surface(Surface.DARK_PANEL);
        }

        HeadCompoundComponentEntry entry = new HeadCompoundComponentEntry(headData, this.compoundHeadsLayout, this);

        this.headCompoundComponentEntries.add(entry);
        this.compoundHeadsLayout.child(entry);
        this.updatePreviews();
    }

    public void removeCompound(HeadCompoundComponentEntry entry) {
        assert this.parent != null;
        entry.close();
        this.compoundHeadsLayout.removeChild(entry);
        this.headCompoundComponentEntries.remove(entry);

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

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK);

            this.client.setScreen(this);
        }, FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK, true));
    }

    public boolean overlapHatLayer() {
        return this.overlapHatLayerButton.enabled();
    }

    public void upCompoundEntry(AbstractHeadListEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadListEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.upEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    public void downCompoundEntry(AbstractHeadListEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadListEntry headEntry) {
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

        if (!this.favoritesHeadsOnOpenScreen.equals(FzmmClient.CONFIG.headGenerator.favoriteSkins()))
            FzmmClient.CONFIG.save();
    }

    private void onChangeSkinField(String value) {
        EnumWidget mode = this.skinElements.mode();
        if (mode == null)
            return;
        if (((SkinMode) mode.getValue()).isHeadName() && this.headNameField.getText().equals(this.previousSkinName)) {
            this.headNameField.setText(value);
            this.headNameField.setCursor(0);
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
                (SkinMode) this.skinElements.mode().getValue(),
                this.skinElements.valueField().getText(),
                this.showFavorites,
                this.overlapHatLayerButton.enabled(),
                this.selectedCategory,
                this.searchField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        HeadGeneratorMemento memento = (HeadGeneratorMemento) mementoObject;
        this.skinElements.mode().setValue(memento.skinMode);
        this.skinElements.valueField().setText(memento.skinRowValue);
        this.skinElements.valueField().setCursor(0);
        this.headNameField.setText(memento.headName);
        this.headNameField.setCursor(0);
        if (memento.showFavorites)
            this.toggleFavoriteListExecute();
        this.overlapHatLayerButton.enabled(memento.overlapHatLayer);
        this.selectedCategory = memento.category;
        this.searchField.setText(memento.search);
        this.searchField.setCursor(0);
    }

    private record HeadGeneratorMemento(String headName, SkinMode skinMode, String skinRowValue, boolean showFavorites,
                                        boolean overlapHatLayer, IHeadCategory category,
                                        String search) implements IMementoObject {
    }
}