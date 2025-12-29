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
import fzmm.zailer.me.client.logic.minecraft_heads.MinecraftHeadsResources;
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
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

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
        this.licenseLabel.text(Component.translatable("fzmm.gui.headGallery.label.license", MchTier.NO_LICENSE.message()));

        // preview at right
        FlowLayout previewLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "preview-layout");
        this.frontEntityPreview = new CustomHeadEntity(this.minecraft.level);
        this.backEntityPreview = new CustomHeadEntity(this.minecraft.level);

        EntityComponent<CustomHeadEntity> backEntityPreview = EComponents.entity(Sizing.fixed(48), this.backEntityPreview)
                .allowMouseRotation(true);
        backEntityPreview.onMouseDrag(new MouseButtonEvent(0, 0, new MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0)), 160, 0);
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
        OverlayContainer<FlowLayout> tagOverlay = UIContainers.overlay(layout);
        this.addOverlay(tagOverlay);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        this.filter.initFocus(focusHandler);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.filter.searchTextBox().getValue().isEmpty()) {
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
        EFlowLayout layout = this.getModel().expandTemplate(EFlowLayout.class, "ask-api-key-overlay", Map.of());
        OverlayContainer<EFlowLayout> overlay = UIContainers.overlay(layout);

        layout.childByIdOrThrow(EButtonComponent.class, "license-types-button")
                .onPress(buttonComponent -> {
                    assert this.minecraft.screen != null;
                    ConfirmLinkScreen.confirmLinkNow(this.minecraft.screen, MINECRAFT_HEADS_LICENSE_TYPES_LINK, true);
                });

        layout.childByIdOrThrow(EButtonComponent.class, "api-key-button")
                .onPress(buttonComponent -> {
                    assert this.minecraft.screen != null;
                    ConfirmLinkScreen.confirmLinkNow(this.minecraft.screen, MINECRAFT_HEADS_API_KEY_LINK, true);
                });

        TextBoxComponent apiKeyTextBox = layout.childByIdOrThrow(TextBoxComponent.class, "api-key");
        CheckboxComponent dontAskAgainCheckbox = layout.childByIdOrThrow(CheckboxComponent.class, "dont-ask-again-checkbox");

        Component tagsMinTierText = MchTier.minTierRequired(MchTier.TAG_GENERAL_REQUEST).message();
        layout.childByIdOrThrow(ELabelComponent.class, "tags-note")
                .text(Component.translatable("fzmm.gui.headGallery.overlay.askApiKey.label.tagsNote", tagsMinTierText));

        layout.childByIdOrThrow(EButtonComponent.class, "done-button").onPress(buttonComponent -> {
            FzmmClient.CONFIG.minecraftHeads.apiKey(apiKeyTextBox.getValue());
            FzmmClient.CONFIG.minecraftHeads.askForApiKey(!dontAskAgainCheckbox.selected());
            FzmmClient.CONFIG.save();

            this.fetchMinecraftHeads();

            overlay.remove();
        });

        return overlay;
    }

    private OverlayContainer<EFlowLayout> initAskFetchOverlay() {
        EFlowLayout layout = this.getModel().expandTemplate(EFlowLayout.class, "ask-fetch-overlay", Map.of());
        OverlayContainer<EFlowLayout> overlay = UIContainers.overlay(layout);

        CheckboxComponent dontAskAgainCheckbox = layout.childByIdOrThrow(CheckboxComponent.class, "automatically-fetch-checkbox");

        layout.childByIdOrThrow(EButtonComponent.class, "load-heads-button")
                .onPress(buttonComponent -> {
                    if (dontAskAgainCheckbox.selected()) {
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
        AtomicBoolean isFirstPage = new AtomicBoolean(true);
        MCH_RESOURCES.onHeadsUpdate(heads -> this.minecraft.execute(() -> {
            if (isFirstPage.getAndSet(false)) {
                this.updateEssential();
            }

            this.filter.updateEntries(List.copyOf(MCH_RESOURCES.heads()));
            this.updateContentWithFilters();
        }));

        MCH_RESOURCES.fetchEssential().whenComplete((ignored, throwable) -> this.minecraft.execute(() -> {
            this.updateContentWithFilters();
            this.updateEssential();
            this.notifyNewHeads();
        }));
    }

    @Override
    public void removed() {
        super.removed();
        MCH_RESOURCES.clearObserver();
    }

    private void updateEssential() {
        Component licenseText = MCH_RESOURCES.licenseDetected().message();
        this.licenseLabel.text(Component.translatable("fzmm.gui.headGallery.label.license", licenseText));
        this.filter.init();
    }

    private void notifyNewHeads() {
        if (MCH_RESOURCES.heads().isEmpty()) return;

        Pair<Integer, List<MchHead>> newHeads = MCH_RESOURCES.searchNewHeads();
        int lastIdSaved = FzmmClient.CONFIG.minecraftHeads.lastHeadId();
        int size = newHeads.getB().size();
        if (size == 0 && !MinecraftHeadsResources.isDebug()) return;

        if (lastIdSaved != newHeads.getA()) {
            FzmmClient.CONFIG.minecraftHeads.lastHeadId(newHeads.getA());
            FzmmClient.CONFIG.save();

            // -1 is default value, so is equivalent to never checked before
            if (lastIdSaved == -1 && !MinecraftHeadsResources.isDebug()) return;
        }

        String baseKey = "fzmm.gui.headGallery.snack_bar.newHeads.";

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_NEW_HEADS_ID)
                .title(Component.translatable(baseKey + "title"))
                .details(Component.translatable(baseKey + "message", Component.literal(String.valueOf(size)).withStyle(ChatFormatting.BOLD)))
                .button(snackBar -> {
                    EButtonComponent button = EComponents.button(Component.translatable(baseKey + "button"));

                    button.onPress(buttonComponent -> {
                        GalleryFilterController.ID_GREATER_FILTER.value(lastIdSaved).serialize()
                                .ifPresent(s -> this.filter.searchTextBox().text(s));
                        snackBar.close();
                    });

                    return button;
                }).backgroundColor(EStyles.ALERT_TIP_COLOR)
                .expandDetails()
                .closeButton()
                .build()
        );
    }

    private void minecraftHeadsLinkExecute(ButtonComponent button) {
        assert this.minecraft.screen != null;
        ConfirmLinkScreen.confirmLinkNow(this.minecraft.screen, AbstractMchApi.URL, true);
    }

    private void updatePreview(ItemStack stack) {
        HeadUtils.getSkinTextures(stack).whenComplete((skinTextures, throwable) -> {
            if (throwable == null && skinTextures.isPresent()) {
                this.updatePreview(skinTextures.get());
            }
        });
    }

    private void updatePreview(PlayerSkin textures) {
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
