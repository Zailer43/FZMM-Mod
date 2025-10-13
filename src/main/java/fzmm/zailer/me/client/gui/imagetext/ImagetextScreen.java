package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.algorithms.ImagetextBrailleAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.algorithms.ImagetextCharactersAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.tabs.*;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen implements IMemento {

    private static final double DEFAULT_SIZE_VALUE = 32;
    public static final double MAX_SIMILARITY_THRESHOLD = 10d;
    private static final long PREVIEW_UPDATE_DELAY_MILLIS = 20L;
    private final ImagetextLogic imagetextLogic;
    private ImageRowsElements imageElements;
    private EBooleanButton preserveImageAspectRatioToggle;
    private SmallCheckboxComponent showResolutionCheckbox;
    private SmallCheckboxComponent smoothImageCheckbox;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private SliderWidget similarityThreshold;
    private EFlowLayout previewLayout;
    private Animation.Composed smallGuiAnimation;
    private CompletableFuture<Void> scheduledUpdatePreview = CompletableFuture.completedFuture(null);
    private TabContainer algorithmTabContainer;
    private TabContainer modeTabContainer;


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        FzmmConfig.Imagetext config = FzmmClient.CONFIG.imagetext;

        // image options
        ImageRows imageRows = new ImageRows(this.getBaseScreenTranslationKey(), "image", "imageSourceType", true);
        this.imageElements = ImageRows.setup(imageRows, "image", "imageSourceType", ImageMode.URL);

        FlowLayout imageTextBoxLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "image-textbox");
        imageTextBoxLayout.child(this.imageElements.valueField().sizing(Sizing.expand(100), Sizing.fixed(16)));

        FlowLayout imageButtonLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "image-buttons");
        List<Component> imageButtonList = new ArrayList<>();

        for (var value : ImageMode.values()) {
            ButtonComponent modeButton = this.imageElements.imageModeButtons().get(value);
            modeButton.sizing(Sizing.fixed(16));
            imageButtonList.add(modeButton);
        }
        imageButtonList.add(Components.spacer().verticalSizing(Sizing.fixed(1)));
        imageButtonList.add(this.imageElements.imageButton().verticalSizing(Sizing.fixed(16)).margins(Insets.none()));

        ImageButtonComponent imageButton = this.imageElements.imageButton();

        this.preserveImageAspectRatioToggle = rootComponent.childByIdOrThrow(EBooleanButton.class, "preserveImageAspectRatio");
        this.preserveImageAspectRatioToggle.enabled(config.defaultPreserveImageAspectRatio());
        this.showResolutionCheckbox = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "showResolution");
        this.showResolutionCheckbox.checked(false);
        this.smoothImageCheckbox = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "smoothImage");
        this.smoothImageCheckbox.checked(true);

        this.widthSlider = SliderRow.setup(rootComponent, "width", DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 3,
                aDouble -> this.onResolutionChanged(this.widthSlider, this.heightSlider, true)
        );
        this.heightSlider = SliderRow.setup(rootComponent, "height", DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 3,
                aDouble -> this.onResolutionChanged(this.heightSlider, this.widthSlider, false)
        );
        this.similarityThreshold = SliderRow.setup(rootComponent, "similarityThreshold",
                config.defaultSimilarityThreshold(), 0d, MAX_SIMILARITY_THRESHOLD, Double.class,
                1, 0.1d, null
        );
        this.similarityThreshold.message(s -> Text.literal(s + "%"));

        imageButtonLayout.children(imageButtonList);

        // algorithm options
        List<IImagetextAlgorithm> algorithmTabs = List.of(new ImagetextCharactersAlgorithm(), new ImagetextBrailleAlgorithm());
        ContextMenuButton algorithmButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "algorithm-button");
        this.algorithmTabContainer = rootComponent.childByIdOrThrow(TabContainer.class, "algorithm-tabs");
        this.algorithmTabContainer.addParsedTabs(algorithmTabs).onSelect(tab -> {
            algorithmButton.setMessage(this.getAlgorithmText());
            this.scheduleUpdatePreview();
        });
        algorithmButton.setContextMenuOptions(contextMenu -> {
            for (var algorithm : algorithmTabs) {
                contextMenu.button(algorithm.getButtonText(), dropdown -> {
                    algorithmButton.removeContextMenu();
                    algorithm.clearCache();
                    this.algorithmTabContainer.selectTab(algorithm);
                    this.scheduleUpdatePreview();
                    this.onResolutionChanged(this.widthSlider, this.heightSlider, true);
                });
            }
        });
        for (var algorithm : algorithmTabs) {
            algorithm.setupComponents(rootComponent);
        }
        this.algorithmTabContainer.selectTab();

        // image mode
        List<IImagetextTab> modeTabs = List.of(new ImagetextLoreTab(), new ImagetextBookPageTab(), new ImagetextBookTooltipTab(),
                new ImagetextTextDisplayTab(), new ImagetextSignTab(), new ImagetextHologramTab(), new ImagetextCopyTab()
        );
        ContextMenuButton modeButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "mode-button");
        this.modeTabContainer = rootComponent.childByIdOrThrow(TabContainer.class, "mode-tabs");
        this.modeTabContainer.addParsedTabs(modeTabs).onSelect(tab -> {
            modeButton.setMessage(this.getModeText());
            this.scheduleUpdatePreview();
        });
        modeButton.setContextMenuOptions(contextMenu -> {
            for (var mode : modeTabs) {
                contextMenu.button(mode.getButtonText(), dropdown -> {
                    modeButton.removeContextMenu();
                    this.modeTabContainer.selectTab(mode);
                });
            }
        });
        for (var tab : modeTabs) {
            tab.setupComponents(rootComponent);
        }
        this.modeTabContainer.selectTab();

        // preview
        this.previewLayout = rootComponent.childByIdOrThrow(EFlowLayout.class, "preview-layout");

        this.widthSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.heightSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.similarityThreshold.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.showResolutionCheckbox.onChanged().subscribe(buttonComponent -> this.scheduleUpdatePreview());
        this.smoothImageCheckbox.onChanged().subscribe(buttonComponent -> {
            ((IImagetextAlgorithm) this.algorithmTabContainer.selectedTab()).clearCache();
            this.scheduleUpdatePreview();
        });

        for (var tab : algorithmTabs) {
            tab.setUpdatePreviewCallback(this::scheduleUpdatePreview);
        }

        //bottom buttons
        ButtonComponent executeButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "execute-button");
        executeButton.active(false);
        executeButton.onPress(button -> this.execute());
        imageButton.setButtonCallback(image -> {
            executeButton.active = image.isPresent();
            if (image.isPresent()) {
                ((IImagetextAlgorithm) this.algorithmTabContainer.selectedTab()).clearCache();
                this.scheduleUpdatePreview();
                this.updateAspectRatio(image.get());
            }
        });

        // animation of small gui
        FlowLayout imageOptionsLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "image-options-layout");
        FlowLayout algorithmOptionsLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "algorithm-options-layout");
        FlowLayout imageModeLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "image-mode-layout");

        Animation<Sizing> imageLayoutAnimation = imageOptionsLayout.horizontalSizing().animate(100, Easing.LINEAR, Sizing.expand(100));
        Animation<Sizing> algorithmLayoutAnimationHorizontal = algorithmOptionsLayout.horizontalSizing().animate(100, Easing.LINEAR, Sizing.expand(100));
        Animation<Sizing> algorithmLayoutAnimationVertical = algorithmOptionsLayout.verticalSizing().animate(100, Easing.LINEAR, Sizing.content());
        // workaround to fix alignment issue (owo-lib sizing rounds up related)
        Animation<Sizing> imageModeFixAnimation = imageModeLayout.horizontalSizing().animate(100, Easing.LINEAR, Sizing.expand(100));
        this.smallGuiAnimation = Animation.compose(imageLayoutAnimation, algorithmLayoutAnimationHorizontal, algorithmLayoutAnimationVertical, imageModeFixAnimation);

        // animation of expand preview
        ScrollContainer<?> leftOptionsScroll = rootComponent.childByIdOrThrow(ScrollContainer.class, "left-options-scroll");
        ButtonComponent expandPreviewButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "expand-preview-button");

        Animation<Sizing> leftOptionsAnimation = leftOptionsScroll.horizontalSizing().animate(100, Easing.CUBIC, Sizing.expand(0));
        AtomicBoolean isExpanded = new AtomicBoolean(false);
        expandPreviewButton.onPress(buttonComponent -> {
            if (isExpanded.getAndSet(!isExpanded.get())) {
                leftOptionsAnimation.reverse();
                expandPreviewButton.setMessage(Text.translatable("fzmm.gui.button.arrow2.left"));
            } else {
                leftOptionsAnimation.forwards();
                expandPreviewButton.setMessage(Text.translatable("fzmm.gui.button.arrow2.right"));
            }
        });

        this.setSmallGuiAnimation(this.width);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.imageElements.valueField(), Component.FocusSource.MOUSE_CLICK);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.setSmallGuiAnimation(width);
    }

    private boolean isSmallGuiDesign(int width) {
        // 60% is left options width and 30% is image options + algorithm options width
        return (width * 0.3f) < 200;
    }

    private void setSmallGuiAnimation(int width) {
        if (this.isSmallGuiDesign(width)) {
            this.smallGuiAnimation.forwards();
        } else {
            this.smallGuiAnimation.backwards();
        }
    }

    private Text getAlgorithmText() {
        return Text.translatable("fzmm.gui.imagetext.tab.algorithm", this.algorithmTabContainer.selectedTab().getButtonText());
    }

    private Text getModeText() {
        return Text.translatable("fzmm.gui.imagetext.tab.mode", this.modeTabContainer.selectedTab().getButtonText());
    }

    private void onResolutionChanged(SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (!this.imageElements.imageButton().hasImage() || !this.preserveImageAspectRatioToggle.enabled()) return;

        Optional<BufferedImage> imageOptional = this.imageElements.imageButton().getImage();
        if (imageOptional.isEmpty()) return;
        BufferedImage image = imageOptional.get();
        int width = image.getWidth();
        int height = image.getHeight();

        int value;
        int valueToChange;
        float algorithmAspectRatio;
        IImagetextAlgorithm algorithm = this.algorithmTabContainer.selectedTab();
        if (isWidth) {
            value = width;
            valueToChange = height;
            algorithmAspectRatio = algorithm.widthRatio();
        } else {
            value = height;
            valueToChange = width;
            algorithmAspectRatio = algorithm.heightRatio();
        }

        this.preserveAspectRatio(config, configToChange, value, valueToChange, algorithmAspectRatio);
    }

    private void preserveAspectRatio(SliderWidget config, SliderWidget configToChange, int value, int valueToChange, float algorithmAspectRatio) {
        valueToChange = (int) (valueToChange * algorithmAspectRatio);

        int configValue = (int) config.parsedValue();
        int newValue = ImagetextLogic.getResizedAspectRatio(value, valueToChange, configValue);
        newValue = (int) MathHelper.clamp(newValue, configToChange.min(), configToChange.max());

        configToChange.setDiscreteValueWithoutCallback(newValue);
    }

    public void execute() {
        CompletableFuture.runAsync(() -> {
            Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
            if (image.isEmpty()) return;

            this.buildImagetext(image.get(), true);
            ((IImagetextTab) this.modeTabContainer.selectedTab()).execute(this.imagetextLogic);
        }).handle((unused, throwable) -> {
            if (throwable != null) {
                FzmmClient.LOGGER.error("[ImagetextScreen] Error in imagetext give", throwable);
                MinecraftClient.getInstance().execute(() -> SnackBarManager.getInstance().add(
                        BaseSnackBarComponent.builder(SnackBarManager.IMAGETEXT_ID)
                                .title(Text.literal("fzmm.giveItem.error"))
                                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                                .closeButton()
                                .build()
                ));
            }
            return null;
        });
    }

    public void scheduleUpdatePreview() {
        if (this.scheduledUpdatePreview != null && !this.scheduledUpdatePreview.isDone()) return;

        this.scheduledUpdatePreview = CompletableFuture.runAsync(() -> this.updatePreview(false),
                CompletableFuture.delayedExecutor(PREVIEW_UPDATE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
        );
        this.scheduledUpdatePreview.handle((unused, throwable) -> {
            if (throwable != null) {
                FzmmClient.LOGGER.error("[ImagetextScreen] Error updating preview", throwable);
            }
            return null;
        });
    }

    public void updatePreview(boolean isExecute) {
        Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
        if (image.isEmpty()) return;

        this.buildImagetext(image.get(), isExecute);
        List<Text> imagetext = this.imagetextLogic.text();

        MutableText tooltipText = Text.empty().setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        tooltipText.append(Text.translatable("fzmm.gui.imagetext.label.textLength", this.imagetextLogic.textLength()));

        if (this.modeTabContainer.selectedTab() instanceof IImagetextTooltip metadata) {
            tooltipText.append("\n");
            tooltipText.append(metadata.getTooltip(this.imagetextLogic));
        }

        assert this.client != null;
        this.client.execute(() -> this.previewLayout.<EFlowLayout>configure(layout -> {
            // Wrapping text is very expensive in memory allocation because (reasons) and (more reasons)
            // updatePreview is a hot spot, so it is better to avoid wrapping
            //
            // To avoid this, is better to use a label for each line of text
            layout.clearChildren();
            layout.children(imagetext.stream().map(EComponents::label).toList());
            layout.tooltip(tooltipText);
        }));
    }

    private void buildImagetext(BufferedImage image, boolean isExecute) {
        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = this.smoothImageCheckbox.checked();
        boolean showResolution = this.showResolutionCheckbox.checked();
        double similarityThreshold = (double) this.similarityThreshold.parsedValue();

        IImagetextAlgorithm algorithm = this.algorithmTabContainer.selectedTab();
        ImagetextData data = new ImagetextData(image, width, height, smoothScaling, similarityThreshold);
        this.modeTabContainer.<IImagetextTab>selectedTab().build(algorithm, this.imagetextLogic, data, isExecute);

        if (showResolution) {
            this.imagetextLogic.addResolution();
        }
    }

    private void updateAspectRatio(BufferedImage image) {
        if (!this.preserveImageAspectRatioToggle.enabled()) return;

        int width = image.getWidth();
        int height = image.getHeight();

        if (height > width) {
            this.onResolutionChanged(this.heightSlider, this.widthSlider, false);
        } else {
            this.onResolutionChanged(this.widthSlider, this.heightSlider, true);
        }
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.imageElements.valueField().getText());
        output.writeObject(this.imageElements.mode().get());
        output.writeInt((int) this.widthSlider.parsedValue());
        output.writeInt((int) this.heightSlider.parsedValue());
        output.writeBoolean(this.smoothImageCheckbox.checked());
        output.writeBoolean(this.showResolutionCheckbox.checked());
        output.writeBoolean(this.preserveImageAspectRatioToggle.enabled());
        output.writeDouble((double) this.similarityThreshold.parsedValue());
        this.modeTabContainer.backup(output);
        this.algorithmTabContainer.backup(output);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.imageElements.valueField().text((String) input.readObject());
        this.imageElements.imageModeButtons().get((ImageMode) input.readObject()).onPress();
        this.widthSlider.setFromDiscreteValue(input.readInt());
        this.heightSlider.setFromDiscreteValue(input.readInt());
        this.smoothImageCheckbox.checked(input.readBoolean());
        this.showResolutionCheckbox.checked(input.readBoolean());
        this.preserveImageAspectRatioToggle.enabled(input.readBoolean());
        this.similarityThreshold.setFromDiscreteValue(input.readDouble());
        this.modeTabContainer.restore(input);
        this.algorithmTabContainer.restore(input);
    }
}
