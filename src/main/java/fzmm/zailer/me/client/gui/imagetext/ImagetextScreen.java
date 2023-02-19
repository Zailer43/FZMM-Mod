package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.mode.ImageMode;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.gui.utils.IMementoScreen;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen implements IMementoScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    public static final double MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS = 10d;
    private static final String IMAGE_ID = "image";
    private static final String IMAGE_SOURCE_TYPE_ID = "imageSourceType";
    private static final String WIDTH_ID = "width";
    private static final String HEIGHT_ID = "height";
    private static final String CHARACTERS_ID = "characters";
    private static final String PRESERVE_IMAGE_ASPECT_RATIO_ID = "preserveImageAspectRatio";
    private static final String SHOW_RESOLUTION_ID = "showResolution";
    private static final String SMOOTH_IMAGE_ID = "smoothImage";
    private static final String PERCENTAGE_OF_SIMILARITY_TO_COMPRESS_ID = "percentageOfSimilarityToCompress";
    private static ImagetextTabs selectedTab = ImagetextTabs.LORE;
    private static ImagetextMemento memento = null;
    private final ImagetextLogic imagetextLogic;
    private ImageRowsElements imageElements;
    private BooleanButton preserveImageAspectRatioToggle;
    private BooleanButton showResolutionToggle;
    private BooleanButton smoothImageToggle;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private SliderWidget percentageOfSimilarityToCompress;
    private TextFieldWidget charactersTextField;


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
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
        this.widthSlider = SliderRow.setup(rootComponent, WIDTH_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0,
                aDouble -> onWidthChange.onPress(null)
        );
        this.heightSlider = SliderRow.setup(rootComponent, HEIGHT_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0,
                aDouble -> this.onResolutionChanged(imageButton, this.preserveImageAspectRatioToggle, heightSlider, widthSlider, false)
        );
        this.charactersTextField = TextBoxRow.setup(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT, config.maxResolution());
        this.showResolutionToggle = BooleanRow.setup(rootComponent, SHOW_RESOLUTION_ID, false);
        this.smoothImageToggle = BooleanRow.setup(rootComponent, SMOOTH_IMAGE_ID, true);
        this.percentageOfSimilarityToCompress = SliderRow.setup(rootComponent, PERCENTAGE_OF_SIMILARITY_TO_COMPRESS_ID, config.defaultPercentageOfSimilarityToCompress(), 0d, MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS, Double.class, 1, null);
        //tabs
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var imagetextTab : ImagetextTabs.values()) {
            IScreenTab tab = this.getTab(imagetextTab, IImagetextTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button ->
                    selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab));
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);
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

        String characters = this.charactersTextField.getText();
        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = this.smoothImageToggle.enabled();
        boolean showResolution = this.showResolutionToggle.enabled();
        double percentageOfSimilarityToCompress = (double) this.percentageOfSimilarityToCompress.parsedValue();

        this.getTab(selectedTab, IImagetextTab.class).generate(this.imagetextLogic, new ImagetextData(image.get(), characters, width, height, smoothScaling, percentageOfSimilarityToCompress), isExecute);

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
                (ImageMode) this.imageElements.mode().getValue(),
                this.charactersTextField.getText(),
                (int) this.widthSlider.parsedValue(),
                (int) this.heightSlider.parsedValue(),
                this.smoothImageToggle.enabled(),
                this.showResolutionToggle.enabled(),
                this.preserveImageAspectRatioToggle.enabled(),
                (double) this.percentageOfSimilarityToCompress.parsedValue(),
                this.createMementoTabs()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        ImagetextMemento memento = (ImagetextMemento) mementoObject;
        this.imageElements.valueField().setText(memento.imageRowValue);
        this.imageElements.valueField().setCursor(0);
        this.imageElements.mode().setValue(memento.imageGetter);
        this.charactersTextField.setText(memento.characters);
        this.charactersTextField.setCursor(0);
        this.widthSlider.setFromDiscreteValue(memento.width);
        this.heightSlider.setFromDiscreteValue(memento.height);
        this.smoothImageToggle.enabled(memento.smoothScaling);
        this.showResolutionToggle.enabled(memento.showResolution);
        this.preserveImageAspectRatioToggle.enabled(memento.preserveImageAspectRatio);
        this.percentageOfSimilarityToCompress.setFromDiscreteValue(memento.percentageOfSimilarityToCompress);
        this.restoreMementoTabs(memento.mementoTabHashMap);

    }

    private record ImagetextMemento(String imageRowValue, ImageMode imageGetter, String characters, int width, int height,
                                    boolean smoothScaling, boolean showResolution, boolean preserveImageAspectRatio,
                                    double percentageOfSimilarityToCompress, HashMap<String, IMementoObject> mementoTabHashMap) implements IMementoObject {
    }
}
