package fzmm.zailer.me.client.gui.head_gallery;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.head_gallery.controller.GalleryContentController;
import fzmm.zailer.me.client.gui.head_gallery.controller.GalleryFilterController;
import fzmm.zailer.me.client.gui.head_gallery.controller.GalleryTagController;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.minecraft_heads.api.AbstractMchApi;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static fzmm.zailer.me.client.FzmmClient.MCH_RESOURCES;

public class HeadGalleryScreen extends BaseFzmmScreen implements IMemento {
    private static final String MINECRAFT_HEADS_LICENSE_TYPES_LINK = AbstractMchApi.URL + "/wiki/minecraft-heads/api-v2-license-types";
    private static final String MINECRAFT_HEADS_API_KEY_LINK = AbstractMchApi.URL + "/settings/api";
    private final GalleryFilterController filter;
    private final GalleryContentController content;
    private CustomHeadEntity frontEntityPreview;
    private CustomHeadEntity backEntityPreview;
    private ELabelComponent licenseLabel;

    public HeadGalleryScreen(@Nullable Screen parent) {
        super("head_gallery", "headGallery", parent);
        this.filter = new GalleryFilterController(this::onFilterChange, null);
        this.content = new GalleryContentController(this::updatePreview);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        assert this.client != null;
        FzmmConfig.MinecraftHeads config = FzmmClient.CONFIG.minecraftHeads;
        GalleryTagController tagManager = new GalleryTagController(
                (includeTags, excludeTags) -> this.updateContentWithFilters()
        );
        tagManager.setupComponents(rootComponent, this.getModel(), this::openTagsOverlay);
        this.filter.tagManager(tagManager);
        this.filter.setupComponents(rootComponent);
        this.content.configureComponent(rootComponent);

        // footer (license at left)
        rootComponent.childByIdOrThrow(ButtonComponent.class, "minecraft-heads-button").onPress(this::minecraftHeadsLinkExecute);
        this.licenseLabel = rootComponent.childByIdOrThrow(ELabelComponent.class, "license-label");
        this.licenseLabel.text(Text.translatable("fzmm.gui.headGallery.label.license", MchTier.NO_LICENSE.message()));

        // preview at right
        FlowLayout previewLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "preview-layout");
        this.frontEntityPreview = new CustomHeadEntity(this.client.world);
        this.backEntityPreview = new CustomHeadEntity(this.client.world);

        EntityComponent<CustomHeadEntity> backEntityPreview = EComponents.entity(Sizing.fixed(48), this.backEntityPreview)
                .allowMouseRotation(true);
        backEntityPreview.onMouseDrag(new Click(0, 0, new MouseInput(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), 160, 0);
        backEntityPreview.allowMouseRotation(false);

        previewLayout.child(EComponents.entity(Sizing.fixed(48), this.frontEntityPreview));
        previewLayout.child(backEntityPreview);

        // fetch content or ask to fetch
        if (MCH_RESOURCES.heads().isEmpty() && (!config.fetchHeadsAutomatically() || config.apiKey().isBlank())) {
            OverlayContainer<EFlowLayout> openOverlay = this.initOpenOverlay(config.askForApiKey() && config.apiKey().isBlank());
            openOverlay.closeOnClick(false);
            openOverlay.mouseDown().subscribe((input, doubled) -> true); // prevent click-through

            this.addOverlay(openOverlay);
        } else {
            this.fetchMinecraftHeads();
        }
    }

    public void onFilterChange(List<MchHead> heads, boolean pageReset) {
        this.content.apply(heads, pageReset);
    }

    private void openTagsOverlay(EFlowLayout layout) {
        OverlayContainer<FlowLayout> tagOverlay = Containers.overlay(layout);
        this.addOverlay(tagOverlay);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        this.filter.initFocus(focusHandler);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (this.filter.searchTextBox().getText().isEmpty()) {
            if (input.isLeft()) {
                UISounds.playButtonSound();
                return this.content.addPage(-1);
            } else if (input.isRight()) {
                UISounds.playButtonSound();
                return this.content.addPage(1);
            }
        }

        return super.keyPressed(input);
    }

    private OverlayContainer<EFlowLayout> initOpenOverlay(boolean askForApiKey) {
        return askForApiKey ? this.initAskApiKeyOverlay() : this.initAskFetchOverlay();
    }

    private OverlayContainer<EFlowLayout> initAskApiKeyOverlay() {
        assert this.client != null;
        EFlowLayout layout = this.getModel().expandTemplate(EFlowLayout.class, "ask-api-key-overlay", Map.of());
        OverlayContainer<EFlowLayout> overlay = Containers.overlay(layout);

        layout.childByIdOrThrow(EButtonComponent.class, "license-types-button")
                .onPress(buttonComponent -> ConfirmLinkScreen.open(this.client.currentScreen, MINECRAFT_HEADS_LICENSE_TYPES_LINK, true));

        layout.childByIdOrThrow(EButtonComponent.class, "api-key-button")
                .onPress(buttonComponent -> ConfirmLinkScreen.open(this.client.currentScreen, MINECRAFT_HEADS_API_KEY_LINK, true));

        TextBoxComponent apiKeyTextBox = layout.childByIdOrThrow(TextBoxComponent.class, "api-key");
        CheckboxComponent dontAskAgainCheckbox = layout.childByIdOrThrow(CheckboxComponent.class, "dont-ask-again-checkbox");

        Text tagsMinTierText = MchTier.minTierRequired(MchTier.TAG_GENERAL_REQUEST).message();
        layout.childByIdOrThrow(ELabelComponent.class, "tags-note")
                .text(Text.translatable("fzmm.gui.headGallery.overlay.askApiKey.label.tagsNote", tagsMinTierText));

        layout.childByIdOrThrow(EButtonComponent.class, "done-button").onPress(buttonComponent -> {
            FzmmClient.CONFIG.minecraftHeads.apiKey(apiKeyTextBox.getText());
            FzmmClient.CONFIG.minecraftHeads.askForApiKey(!dontAskAgainCheckbox.isChecked());
            FzmmClient.CONFIG.save();

            this.fetchMinecraftHeads();

            overlay.remove();
        });

        return overlay;
    }

    private OverlayContainer<EFlowLayout> initAskFetchOverlay() {
        EFlowLayout layout = this.getModel().expandTemplate(EFlowLayout.class, "ask-fetch-overlay", Map.of());
        OverlayContainer<EFlowLayout> overlay = Containers.overlay(layout);

        CheckboxComponent dontAskAgainCheckbox = layout.childByIdOrThrow(CheckboxComponent.class, "automatically-fetch-checkbox");

        layout.childByIdOrThrow(EButtonComponent.class, "load-heads-button")
                .onPress(buttonComponent -> {
                    if (dontAskAgainCheckbox.isChecked()) {
                        FzmmClient.CONFIG.minecraftHeads.fetchHeadsAutomatically(true);
                        FzmmClient.CONFIG.save();
                    }

                    this.fetchMinecraftHeads();


                    overlay.remove();
                });

        layout.childByIdOrThrow(EButtonComponent.class, "cancel-button")
                .onPress(buttonComponent -> overlay.remove());

        return overlay;
    }

    private void updateContentWithFilters() {
        this.filter.onChange(false); // filterOption#onChange -> content#apply
    }

    private void fetchMinecraftHeads() {
        assert this.client != null;
        AtomicBoolean isFirstPage = new AtomicBoolean(true);
        MCH_RESOURCES.onHeadsUpdate(heads -> this.client.execute(() -> {
            if (isFirstPage.getAndSet(false)) {
                this.updateEssential();
            }

            this.filter.updateEntries(List.copyOf(MCH_RESOURCES.heads()));
            this.updateContentWithFilters();
        }));

        MCH_RESOURCES.fetchEssential().whenComplete((ignored, throwable) -> this.client.execute(() -> {
            this.updateContentWithFilters();
            this.updateEssential();
            this.processNewHeads();
        }));
    }

    @Override
    public void removed() {
        super.removed();
        MCH_RESOURCES.clearObserver();
    }

    private void updateEssential() {
        Text licenseText = MCH_RESOURCES.licenseDetected().message();
        this.licenseLabel.text(Text.translatable("fzmm.gui.headGallery.label.license", licenseText));
        this.filter.init();
    }

    private void processNewHeads() {
        if (MCH_RESOURCES.heads().isEmpty()) return;
        if (!MCH_RESOURCES.licenseDetected().hasPermission(MchTier.HEADS_BASIC_DATA)) {
            FzmmClient.LOGGER.warn("[HeadGalleryScreen] Not enough permissions to check for new heads");
            return;
        }
        int lastIdSaved = FzmmClient.CONFIG.minecraftHeads.lastHeadId();
        List<MchHead> newHeadsList = MCH_RESOURCES.sinceId(lastIdSaved);
        int lastId = newHeadsList.stream().mapToInt(MchHead::id).max().orElse(lastIdSaved);
        if (newHeadsList.isEmpty() || lastId == lastIdSaved) return;

        FzmmClient.CONFIG.minecraftHeads.lastHeadId(lastId);
        FzmmClient.CONFIG.save();

        // -1 is default value, so is equivalent to never checked before
        if (lastIdSaved == -1) return;
        this.notifyNewHeads(newHeadsList.size(), lastIdSaved);
    }

    private void notifyNewHeads(int amount, int lastIdSaved) {
        String baseKey = "fzmm.gui.headGallery.snack_bar.newHeads.";
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_NEW_HEADS_ID)
                .title(Text.translatable(baseKey + "title"))
                .details(Text.translatable(baseKey + "message", Text.literal(String.valueOf(amount)).formatted(Formatting.BOLD)))
                .button(snackBar -> {
                    EButtonComponent button = EComponents.button(Text.translatable(baseKey + "button"));

                    return button.onPress(buttonComponent -> {
                        GalleryFilterController.ID_GREATER_FILTER.value(lastIdSaved).serialize()
                                .ifPresent(s -> this.filter.searchTextBox().text(s));
                        snackBar.close();
                    });
                }).backgroundColor(EStyles.ALERT_TIP_COLOR)
                .expandDetails()
                .closeButton()
                .build()
        );
    }

    private void minecraftHeadsLinkExecute(ButtonComponent button) {
        assert this.client != null;

        ConfirmLinkScreen.open(this.client.currentScreen, AbstractMchApi.URL, true);
    }

    private void updatePreview(ItemStack stack) {
        HeadUtils.getSkinTextures(stack).whenComplete((skinTextures, throwable) -> {
            if (throwable == null && skinTextures.isPresent()) {
                this.updatePreview(skinTextures.get());
            }
        });
    }

    private void updatePreview(SkinTextures textures) {
        this.frontEntityPreview.skin(textures);
        this.backEntityPreview.skin(textures);
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        this.filter.backup(output);
        this.content.backup(output);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.filter.restore(input);
        this.content.restore(input);
    }
}
