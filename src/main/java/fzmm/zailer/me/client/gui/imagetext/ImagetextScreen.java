package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.algorithms.ImagetextAlgorithms;
import fzmm.zailer.me.client.gui.imagetext.tabs.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.tabs.ImagetextTabs;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen implements IMementoScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    public static final double MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS = 10d;
    private static final String IMAGE_ID = "image";
    private static final String IMAGE_SOURCE_TYPE_ID = "imageSourceType";
    private static final String WIDTH_ID = "width";
    private static final String HEIGHT_ID = "height";
    private static final String PRESERVE_IMAGE_ASPECT_RATIO_ID = "preserveImageAspectRatio";
    private static final String SHOW_RESOLUTION_ID = "showResolution";
    private static final String SMOOTH_IMAGE_ID = "smoothImage";
    private static final String PERCENTAGE_OF_SIMILARITY_TO_COMPRESS_ID = "percentageOfSimilarityToCompress";
    private static ImagetextTabs selectedTab = ImagetextTabs.LORE;
    private static ImagetextAlgorithms selectedAlgorithm = ImagetextAlgorithms.CHARACTERS;
    private static ImagetextMemento memento = null;
    private final ImagetextLogic imagetextLogic;
    private ImageRowsElements imageElements;
    private BooleanButton preserveImageAspectRatioToggle;
    private BooleanButton showResolutionToggle;
    private BooleanButton smoothImageToggle;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private SliderWidget percentageOfSimilarityToCompress;
    private final HashMap<String, IScreenTab> algorithmsTabs;


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
        this.algorithmsTabs = new HashMap<>();
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FzmmConfig.Imagetext config = FzmmClient.CONFIG.imagetext;
        //general
        this.imageElements = ImageRows.setup(rootComponent, IMAGE_ID, IMAGE_SOURCE_TYPE_ID, ImageMode.URL);
        ImageButtonComponent imageButton = this.imageElements.imageButton();
        this.preserveImageAspectRatioToggle = BooleanRow.setup(rootComponent, PRESERVE_IMAGE_ASPECT_RATIO_ID, config.defaultPreserveImageAspectRatio());
        SliderWidget widthSlider = rootComponent.childById(SliderWidget.class, SliderRow.getSliderId(WIDTH_ID));
        SliderWidget heightSlider = rootComponent.childById(SliderWidget.class, SliderRow.getSliderId(HEIGHT_ID));
        ButtonWidget.PressAction onWidthChange = button -> this.onResolutionChanged(imageButton, this.preserveImageAspectRatioToggle, widthSlider, heightSlider, true);
        this.widthSlider = SliderRow.setup(rootComponent, WIDTH_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 1,
                aDouble -> onWidthChange.onPress(null)
        );
        this.heightSlider = SliderRow.setup(rootComponent, HEIGHT_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 1,
                aDouble -> this.onResolutionChanged(imageButton, this.preserveImageAspectRatioToggle, heightSlider, widthSlider, false)
        );
        this.showResolutionToggle = BooleanRow.setup(rootComponent, SHOW_RESOLUTION_ID, false);
        this.smoothImageToggle = BooleanRow.setup(rootComponent, SMOOTH_IMAGE_ID, true);
        this.percentageOfSimilarityToCompress = SliderRow.setup(rootComponent, PERCENTAGE_OF_SIMILARITY_TO_COMPRESS_ID, config.defaultPercentageOfSimilarityToCompress(), 0d, MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS, Double.class, 1, 0.05d, null);
        //tabs
        this.setTabs(rootComponent, selectedTab, ImagetextTabs.values(), "tabs", anEnum -> selectedTab = (ImagetextTabs) anEnum, this.tabs);
        this.setTabs(rootComponent, selectedAlgorithm, ImagetextAlgorithms.values(), "algorithms", anEnum -> selectedAlgorithm = (ImagetextAlgorithms) anEnum, this.algorithmsTabs);
        //bottom buttons
        ButtonWidget executeButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId("execute"), false, button -> this.execute());
        ButtonWidget previewButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId("preview"), false, button -> {
            if (imageButton.hasImage()) {
                this.updateImagetext(false);
                button.tooltip(this.imagetextLogic.getText());
            }
        });
        imageButton.setButtonCallback(image -> {
            boolean hasImage = image != null;
            executeButton.active = hasImage;
            previewButton.active = hasImage;
            if (hasImage)
                this.updateAspectRatio(image);
        });
    }

    @SuppressWarnings("unchecked")
    private void setTabs(FlowLayout rootComponent, ITabsEnum selectedTab, ITabsEnum[] enumValues, String tabId, Consumer<Enum<? extends ITabsEnum>> setter, HashMap<String, IScreenTab> tabsHashMap) {
        Enum<? extends ITabsEnum> selectedTabEnum = (Enum<? extends ITabsEnum>) selectedTab;
        this.setTabs(tabsHashMap, selectedTabEnum);
        ScreenTabRow.setup(rootComponent, tabId, selectedTabEnum);
        for (var imagetextTab : enumValues) {
            IScreenTab tab = this.getTab(imagetextTab, IImagetextTab.class, tabsHashMap);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button ->
                    setter.accept(this.selectScreenTab(rootComponent, tab, selectedTabEnum, tabsHashMap)));
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTabEnum, tabsHashMap);
    }

    private void onResolutionChanged(ImageButtonComponent imageWidget, BooleanButton preserveImageAspectRatioButton,
                                     SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (!imageWidget.hasImage() || !preserveImageAspectRatioButton.enabled())
            return;

        Optional<BufferedImage> imageOptional = imageWidget.getImage();
        if (imageOptional.isEmpty())
            return;
        BufferedImage image = imageOptional.get();

        int configValue = (int) config.parsedValue();
        Vec2f rescaledSize = ImagetextLogic.changeResolutionKeepingAspectRatio(image.getWidth(), image.getHeight(), configValue, isWidth);

        int newValue = (int) (isWidth ? rescaledSize.y : rescaledSize.x);

        if (newValue > configToChange.max())
            newValue = (int) configToChange.max();
        else if (newValue < configToChange.min())
            newValue = (int) configToChange.min();

        configToChange.setDiscreteValueWithoutCallback(newValue);
    }

    public void execute() {
        if (!this.imageElements.imageButton().hasImage())
            return;

        this.updateImagetext(true);
        this.getTab(selectedTab, IImagetextTab.class).execute(this.imagetextLogic);
    }

    public void updateImagetext(boolean isExecute) {
        Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
        if (image.isEmpty())
            return;

        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = this.smoothImageToggle.enabled();
        boolean showResolution = this.showResolutionToggle.enabled();
        double percentageOfSimilarityToCompress = (double) this.percentageOfSimilarityToCompress.parsedValue();

        IImagetextAlgorithm algorithm = (IImagetextAlgorithm) this.algorithmsTabs.get(selectedAlgorithm.getId());
        this.getTab(selectedTab, IImagetextTab.class).generate(algorithm, this.imagetextLogic, new ImagetextData(image.get(), width, height, smoothScaling, percentageOfSimilarityToCompress), isExecute);

        if (showResolution)
            this.imagetextLogic.addResolution();
    }

    private void updateAspectRatio(BufferedImage image) {
        if (!(this.preserveImageAspectRatioToggle.enabled()))
            return;

        int width = image.getWidth();
        int height = image.getHeight();

        if (height > width)
            this.onResolutionChanged(this.imageElements.imageButton(), this.preserveImageAspectRatioToggle, this.heightSlider, this.widthSlider, false);
        else
            this.onResolutionChanged(this.imageElements.imageButton(), this.preserveImageAspectRatioToggle, this.widthSlider, this.heightSlider, true);
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
                this.smoothImageToggle.enabled(),
                this.showResolutionToggle.enabled(),
                this.preserveImageAspectRatioToggle.enabled(),
                (double) this.percentageOfSimilarityToCompress.parsedValue(),
                this.createMementoTabs(this.tabs),
                this.createMementoTabs(this.algorithmsTabs)
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        ImagetextMemento memento = (ImagetextMemento) mementoObject;
        this.imageElements.valueField().setText(memento.imageRowValue);
        this.imageElements.valueField().setCursorToStart(false);
        this.imageElements.imageModeButtons().get(memento.imageMode).onPress();
        this.widthSlider.setFromDiscreteValue(memento.width);
        this.heightSlider.setFromDiscreteValue(memento.height);
        this.smoothImageToggle.enabled(memento.smoothScaling);
        this.showResolutionToggle.enabled(memento.showResolution);
        this.preserveImageAspectRatioToggle.enabled(memento.preserveImageAspectRatio);
        this.percentageOfSimilarityToCompress.setFromDiscreteValue(memento.percentageOfSimilarityToCompress);
        this.restoreMementoTabs(memento.mementoTabHashMap, this.tabs);
        this.restoreMementoTabs(memento.mementoAlgorithmTabHashMap, this.algorithmsTabs);
    }

    private record ImagetextMemento(String imageRowValue, ImageMode imageMode, int width, int height,
                                    boolean smoothScaling, boolean showResolution, boolean preserveImageAspectRatio,
                                    double percentageOfSimilarityToCompress, HashMap<String, IMementoObject> mementoTabHashMap,
                                    HashMap<String, IMementoObject> mementoAlgorithmTabHashMap) implements IMementoObject {
    }
}
