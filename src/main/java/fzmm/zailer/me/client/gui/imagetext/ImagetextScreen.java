package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.algorithms.ImagetextAlgorithms;
import fzmm.zailer.me.client.gui.imagetext.tabs.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.tabs.IImagetextTooltip;
import fzmm.zailer.me.client.gui.imagetext.tabs.ImagetextMode;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen implements IMementoScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    public static final double MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS = 10d;
    private static ImagetextMode selectedMode = ImagetextMode.LORE;
    private static ImagetextAlgorithms selectedAlgorithm = ImagetextAlgorithms.CHARACTERS;
    private static ImagetextMemento memento = null;
    private final ImagetextLogic imagetextLogic;
    private final HashMap<String, IScreenTab> algorithmsTabs;
    private ImageRowsElements imageElements;
    private EBooleanButton preserveImageAspectRatioToggle;
    private SmallCheckboxComponent showResolutionCheckbox;
    private SmallCheckboxComponent smoothImageCheckbox;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private SliderWidget percentageOfSimilarityToCompress;
    private LabelComponent previewLabel;
    private Animation.Composed smallGuiAnimation;
    private CompletableFuture<Void> scheduledUpdatePreview = CompletableFuture.completedFuture(null);


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
        this.algorithmsTabs = new HashMap<>();
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
            FlowLayout buttonLayout = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
            buttonLayout.tooltip(Text.translatable(value.getTranslationKey() + ".tooltip"));
            ButtonComponent button = this.imageElements.imageModeButtons().get(value);
            button.sizing(Sizing.fixed(16));

            buttonLayout.child(button);
            imageButtonList.add(buttonLayout);
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
        this.percentageOfSimilarityToCompress = SliderRow.setup(rootComponent, "percentageOfSimilarityToCompress",
                config.defaultPercentageOfSimilarityToCompress(), 0d, MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS, Double.class,
                1, 0.1d, null
        );
        this.percentageOfSimilarityToCompress.message(s -> Text.literal(s + "%"));

        imageButtonLayout.children(imageButtonList);

        // algorithm options
        ContextMenuButton algorithmButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "algorithm-button");
        algorithmButton.setContextMenuOptions(contextMenu -> {
            for (var algorithm : ImagetextAlgorithms.values()) {
                contextMenu.button(algorithm.getText(this.getBaseScreenTranslationKey()), dropdown -> {
                            algorithmButton.removeContextMenu();
                            this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
                            selectedAlgorithm = algorithm;
                            algorithmButton.setMessage(this.getAlgorithmText());
                            this.selectTab(rootComponent, algorithm, this.algorithmsTabs);
                            this.scheduleUpdatePreview();
                            this.onResolutionChanged(this.widthSlider, this.heightSlider, true);
                        }
                );
            }
        });
        algorithmButton.setMessage(this.getAlgorithmText());
        this.setTabs(rootComponent, selectedAlgorithm, ImagetextAlgorithms.values(), this.algorithmsTabs);

        // image mode
        ContextMenuButton modeButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "mode-button");
        modeButton.setContextMenuOptions(contextMenu -> {
            for (var mode : ImagetextMode.values()) {
                contextMenu.button(mode.getText(this.getBaseScreenTranslationKey()), dropdown -> {
                            modeButton.removeContextMenu();
                            selectedMode = mode;
                            modeButton.setMessage(this.getModeText());
                            this.selectTab(rootComponent, mode, this.tabs);
                            this.scheduleUpdatePreview();
                        }
                );
            }
        });

        modeButton.setMessage(this.getModeText());
        this.setTabs(rootComponent, selectedMode, ImagetextMode.values(), this.tabs);

        // preview
        this.previewLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "preview-label");

        this.widthSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.heightSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.percentageOfSimilarityToCompress.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.showResolutionCheckbox.onChanged().subscribe(buttonComponent -> this.scheduleUpdatePreview());
        this.smoothImageCheckbox.onChanged().subscribe(buttonComponent -> {
            this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
            this.scheduleUpdatePreview();
        });

        for (var imagetextTab : ImagetextAlgorithms.values()) {
            IImagetextAlgorithm tab = this.getTab(imagetextTab, IImagetextAlgorithm.class, this.algorithmsTabs);
            tab.setUpdatePreviewCallback(this::scheduleUpdatePreview);
        }

        //bottom buttons
        ButtonComponent executeButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "execute-button");
        executeButton.active(false);
        executeButton.onPress(button -> this.execute());
        imageButton.setButtonCallback(image -> {
            executeButton.active = image.isPresent();
            if (image.isPresent()) {
                this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
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
        ScrollContainer<?> leftOptionsScroll =  rootComponent.childByIdOrThrow(ScrollContainer.class, "left-options-scroll");
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

    @SuppressWarnings("unchecked")
    private void setTabs(EFlowLayout rootComponent, ITabsEnum selectedTab, ITabsEnum[] enumValues, HashMap<String, IScreenTab> tabsHashMap) {
        Enum<? extends ITabsEnum> selectedTabEnum = (Enum<? extends ITabsEnum>) selectedTab;
        this.setTabs(tabsHashMap, selectedTabEnum);
        for (var imagetextTab : enumValues) {
            IScreenTab tab = this.getTab(imagetextTab, IImagetextTab.class, tabsHashMap);
            tab.setupComponents(rootComponent);
        }
        this.selectTab(rootComponent, selectedTab, tabsHashMap);
    }

    @SuppressWarnings("unchecked")
    private void selectTab(FlowLayout rootComponent, ITabsEnum selectedTab, HashMap<String, IScreenTab> tabsHashMap) {
        Enum<? extends ITabsEnum> selectedTabEnum = (Enum<? extends ITabsEnum>) selectedTab;
        this.selectScreenTab(rootComponent, selectedTab, selectedTabEnum, tabsHashMap, false);
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
        return Text.translatable("fzmm.gui.imagetext.tab.algorithm", selectedAlgorithm.getText(this.getBaseScreenTranslationKey()));
    }

    private Text getModeText() {
        return Text.translatable("fzmm.gui.imagetext.tab.mode", selectedMode.getText(this.getBaseScreenTranslationKey()));
    }

    private void onResolutionChanged(SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (!this.imageElements.imageButton().hasImage() || !this.preserveImageAspectRatioToggle.enabled()) {
            return;
        }

        Optional<BufferedImage> imageOptional = this.imageElements.imageButton().getImage();
        if (imageOptional.isEmpty()) {
            return;
        }
        BufferedImage image = imageOptional.get();
        int width = image.getWidth();
        int height = image.getHeight();

        int value;
        int valueToChange;
        float algorithmAspectRatio;
        IImagetextAlgorithm algorithm = (IImagetextAlgorithm) this.algorithmsTabs.get(selectedAlgorithm.getId());
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
            if (image.isEmpty()) {
                return;
            }

            this.generateImagetext(image.get(), true);
            this.getTab(selectedMode, IImagetextTab.class).execute(this.imagetextLogic);
        });
    }

    public void scheduleUpdatePreview() {
        if (this.scheduledUpdatePreview != null && !this.scheduledUpdatePreview.isDone()) {
            this.scheduledUpdatePreview.cancel(true);
            this.scheduledUpdatePreview = null;
        }

        // FIXME: frequent updating of the preview consumes a significant amount of memory,
        //  leading to frequent garbage collection calls and resulting in noticeable latency spikes
        int delay = FzmmClient.CONFIG.imagetext.previewUpdateDelayInMillis();
        this.scheduledUpdatePreview = CompletableFuture.runAsync(() -> this.updatePreview(false),
                CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
        );
    }

    public void updatePreview(boolean isExecute) {
        Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
        if (image.isEmpty()) {
            return;
        }

        this.generateImagetext(image.get(), isExecute);
        Text text = this.imagetextLogic.getText();
        List<Text> wrappedText = this.imagetextLogic.getWrappedText();

        ItemStack placeholderStack = DisplayBuilder.builder().addLore(wrappedText).get();
        String nbtSize = ItemUtils.getLengthInKB(ItemUtils.getLengthInBytes(placeholderStack));
        String textSize = ItemUtils.getLengthInKB(Text.Serialization.toJsonString(text).length());

        MutableText tooltipText = Text.empty().setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        tooltipText.append(Text.translatable("fzmm.gui.imagetext.label.imagetextSize", nbtSize, textSize));

        if (this.getTab(selectedMode, IImagetextTab.class) instanceof IImagetextTooltip metadata) {
            tooltipText.append("\n");
            tooltipText.append(metadata.getTooltip(this.imagetextLogic));
        }

        assert this.client != null;
        this.client.execute(() -> {
            this.previewLabel.text(text);
            this.previewLabel.tooltip(tooltipText);
        });
    }

    private void generateImagetext(BufferedImage image, boolean isExecute) {
        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = this.smoothImageCheckbox.checked();
        boolean showResolution = this.showResolutionCheckbox.checked();
        double percentageOfSimilarityToCompress = (double) this.percentageOfSimilarityToCompress.parsedValue();

        IImagetextAlgorithm algorithm = (IImagetextAlgorithm) this.algorithmsTabs.get(selectedAlgorithm.getId());
        ImagetextData data = new ImagetextData(image, width, height, smoothScaling, percentageOfSimilarityToCompress);
        this.getTab(selectedMode, IImagetextTab.class).generate(algorithm, this.imagetextLogic, data, isExecute);

        if (showResolution) {
            this.imagetextLogic.addResolution();
        }
    }

    private void updateAspectRatio(BufferedImage image) {
        if (!this.preserveImageAspectRatioToggle.enabled()) {
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (height > width) {
            this.onResolutionChanged(this.heightSlider, this.widthSlider, false);
        } else {
            this.onResolutionChanged(this.widthSlider, this.heightSlider, true);
        }
    }

    @Override
    public void setMemento(IMementoObject memento) {
        ImagetextScreen.memento = (ImagetextMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new ImagetextMemento(this.imageElements.valueField().getText(),
                this.imageElements.mode().get(),
                (int) this.widthSlider.parsedValue(),
                (int) this.heightSlider.parsedValue(),
                this.smoothImageCheckbox.checked(),
                this.showResolutionCheckbox.checked(),
                this.preserveImageAspectRatioToggle.enabled(),
                (double) this.percentageOfSimilarityToCompress.parsedValue(),
                this.createMementoTabs(this.tabs),
                this.createMementoTabs(this.algorithmsTabs)
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        ImagetextMemento memento = (ImagetextMemento) mementoObject;
        this.imageElements.valueField().text(memento.imageRowValue);
        this.imageElements.imageModeButtons().get(memento.imageMode).onPress();
        this.widthSlider.setFromDiscreteValue(memento.width);
        this.heightSlider.setFromDiscreteValue(memento.height);
        this.smoothImageCheckbox.checked(memento.smoothScaling);
        this.showResolutionCheckbox.checked(memento.showResolution);
        this.preserveImageAspectRatioToggle.enabled(memento.preserveImageAspectRatio);
        this.percentageOfSimilarityToCompress.setFromDiscreteValue(memento.percentageOfSimilarityToCompress);
        this.restoreMementoTabs(memento.mementoTabHashMap, this.tabs);
        this.restoreMementoTabs(memento.mementoAlgorithmTabHashMap, this.algorithmsTabs);
    }

    private record ImagetextMemento(String imageRowValue, ImageMode imageMode, int width, int height,
                                    boolean smoothScaling, boolean showResolution, boolean preserveImageAspectRatio,
                                    double percentageOfSimilarityToCompress,
                                    HashMap<String, IMementoObject> mementoTabHashMap,
                                    HashMap<String, IMementoObject> mementoAlgorithmTabHashMap) implements IMementoObject {
    }
}
