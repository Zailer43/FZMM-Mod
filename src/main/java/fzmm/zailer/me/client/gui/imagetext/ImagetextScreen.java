package fzmm.zailer.me.client.gui.imagetext;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.imagetext.tabs.ImagetextBookPageTab;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.components.image.mode.ImageMode;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.Vec2f;

import java.awt.image.BufferedImage;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    private static final String IMAGE_ID = "image";
    private static final String IMAGE_SOURCE_TYPE_ID = "imageSourceType";
    private static final String WIDTH_ID = "width";
    private static final String HEIGHT_ID = "height";
    private static final String CHARACTERS_ID = "characters";
    private static final String PRESERVE_IMAGE_ASPECT_RATIO_ID = "preserveImageAspectRatio";
    private static final String SHOW_RESOLUTION_ID = "showResolution";
    private static final String SMOOTH_IMAGE_ID = "smoothImage";
    private static ImagetextTabs selectedTab = ImagetextTabs.LORE;
    private final ImagetextLogic imagetextLogic;
    private ImageButtonWidget imageButton;
    private ConfigToggleButton preserveImageAspectRatioToggle;
    private ConfigToggleButton showResolutionToggle;
    private ConfigToggleButton smoothImageToggle;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private TextFieldWidget charactersTextField;


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
    }

    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "imagetext-options-list",
                this.newImageRow(IMAGE_ID),
                this.newEnumRow(IMAGE_SOURCE_TYPE_ID),
                this.newSliderRow(WIDTH_ID, "resolution", 0),
                this.newSliderRow(HEIGHT_ID, "resolution", 0),
                this.newTextFieldRow(CHARACTERS_ID),
                this.newBooleanRow(PRESERVE_IMAGE_ASPECT_RATIO_ID),
                this.newBooleanRow(SHOW_RESOLUTION_ID),
                this.newBooleanRow(SMOOTH_IMAGE_ID),
                this.newScreenTabRow(selectedTab)
        );

        FlowLayout container = rootComponent.childById(FlowLayout.class, "imagetext-options-list");
        if (container == null)
            return;

        for (var tab : ImagetextTabs.values())
            container.child(this.newScreenTab(tab.getId(), tab.getComponents(this)));
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FzmmConfig.Imagetext config = FzmmClient.CONFIG.imagetext;
        //general
        this.imageButton = this.setupImage(rootComponent, IMAGE_ID, IMAGE_SOURCE_TYPE_ID, ImageMode.URL);
        this.preserveImageAspectRatioToggle = this.setupBooleanButton(rootComponent, PRESERVE_IMAGE_ASPECT_RATIO_ID, config.defaultPreserveImageAspectRatio());
        SliderWidget widthSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(WIDTH_ID));
        SliderWidget heightSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(HEIGHT_ID));
        ButtonWidget.PressAction onWidthChange = button -> this.onResolutionChanged(this.imageButton, this.preserveImageAspectRatioToggle, widthSlider, heightSlider, true);
        this.widthSlider = this.setupSlider(rootComponent, WIDTH_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class,
                aDouble -> onWidthChange.onPress(null)
        );
        this.heightSlider = this.setupSlider(rootComponent, HEIGHT_ID, DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class,
                aDouble -> this.onResolutionChanged(this.imageButton, this.preserveImageAspectRatioToggle, heightSlider, widthSlider, false)
        );
        this.charactersTextField = this.setupTextField(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT);
        this.showResolutionToggle = this.setupBooleanButton(rootComponent, SHOW_RESOLUTION_ID, false);
        this.smoothImageToggle = this.setupBooleanButton(rootComponent, SMOOTH_IMAGE_ID, true);
        //tabs
        for (var tab : ImagetextTabs.values()) {
            tab.setupComponents(this, rootComponent);
            this.setupButton(rootComponent, this.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                selectedTab = tab;
            });
        }
        this.selectScreenTab(rootComponent, selectedTab);
        //bottom buttons
        ButtonWidget executeButton = this.setupButton(rootComponent, this.getButtonId("execute"), false, button -> this.execute());
        ButtonWidget previewButton = this.setupButton(rootComponent, this.getButtonId("preview"), false, button -> {
            if (this.imageButton.hasImage()) {
                this.updateImagetext();
                button.tooltip(this.imagetextLogic.getText());
            }
        });
        this.imageButton.setImageLoadedEvent(image -> {
            onWidthChange.onPress(null);
            executeButton.active = true;
            previewButton.active = true;
            return ImageStatus.IMAGE_LOADED;
        });
    }

    private void onResolutionChanged(ImageButtonWidget imageWidget, ConfigToggleButton preserveImageAspectRatioButton,
                                     SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (!imageWidget.hasImage() || !((boolean) preserveImageAspectRatioButton.parsedValue()))
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
        if (!this.imageButton.hasImage())
            return;

        this.updateImagetext();
        selectedTab.execute(this.imagetextLogic);
    }

    public void updateImagetext() {
        Optional<BufferedImage> image = this.imageButton.getImage();
        if (image.isEmpty())
            return;

        String characters = this.charactersTextField.getText();
        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = (boolean) this.smoothImageToggle.parsedValue();
        boolean showResolution = (boolean) this.showResolutionToggle.parsedValue();

        if (selectedTab == ImagetextTabs.BOOK_PAGE) {
            width = ImagetextBookPageTab.getMaxImageWidthForBookPage(characters);
            height = 15;
        }

        this.imagetextLogic.generateImagetext(image.get(), characters, width, height, smoothScaling);

        if (showResolution)
            this.imagetextLogic.addResolution();
    }

}
