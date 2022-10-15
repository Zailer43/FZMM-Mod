package fzmm.zailer.me.client.gui;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.enums.options.ImagetextBookOption;
import fzmm.zailer.me.client.gui.enums.options.LoreOption;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.widgets.EnumWidget;
import fzmm.zailer.me.client.gui.widgets.SliderWidget;
import fzmm.zailer.me.client.gui.widgets.image.ImageButtonWidget;
import fzmm.zailer.me.client.gui.widgets.image.mode.ImageMode;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.client.toast.BookNbtOverflowToast;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;

import java.awt.image.BufferedImage;

public class ImagetextScreen extends BaseFzmmScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    private static final double MAX_SIZE_VALUE = 127;
    private static final String IMAGE_ID = "image";
    private static final String IMAGE_SOURCE_TYPE_ID = "imageSourceType";
    private static final String WIDTH_ID = "width";
    private static final String HEIGHT_ID = "height";
    private static final String CHARACTERS_ID = "characters";
    private static final String PRESERVE_IMAGE_ASPECT_RATIO_ID = "preserveImageAspectRatio";
    private static final String SHOW_RESOLUTION_ID = "showResolution";
    private static final String SMOOTH_IMAGE_ID = "smoothImage";
    private static final String LORE_MODE_ID = "loreMode";
    private static final String BOOK_PAGE_MODE_ID = "bookPageMode";
    private static final String BOOK_TOOLTIP_MODE_ID = "bookTooltipMode";
    private static final String BOOK_TOOLTIP_AUTHOR_ID = "bookTooltipAuthor";
    private static final String BOOK_TOOLTIP_MESSAGE_ID = "bookTooltipMessage";
    private static final String HOLOGRAM_POS_X_ID = "hologramPosX";
    private static final String HOLOGRAM_POS_Y_ID = "hologramPosY";
    private static final String HOLOGRAM_POS_Z_ID = "hologramPosZ";
    private static ImagetextGuiTab selectedTab = ImagetextGuiTab.LORE;
    private final ImagetextLogic imagetextLogic;

    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
    }


    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "imagetext-options-list",
                this.getImageRow(IMAGE_ID),
                this.getEnumRow(IMAGE_SOURCE_TYPE_ID),
                this.getSliderRow(WIDTH_ID, "resolution"),
                this.getSliderRow(HEIGHT_ID, "resolution"),
                this.getTextFieldRow(CHARACTERS_ID),
                this.getBooleanRow(PRESERVE_IMAGE_ASPECT_RATIO_ID),
                this.getBooleanRow(SHOW_RESOLUTION_ID),
                this.getBooleanRow(SMOOTH_IMAGE_ID),
                this.getScreenTabRow(selectedTab),
                this.getScreenTab(ImagetextGuiTab.LORE.getId(),
                        this.getEnumRow(LORE_MODE_ID)
                ),
                this.getScreenTab(ImagetextGuiTab.BOOK_PAGE.getId(),
                        this.getEnumRow(BOOK_PAGE_MODE_ID)
                ),
                this.getScreenTab(ImagetextGuiTab.BOOK_TOOLTIP.getId(),
                        this.getEnumRow(BOOK_TOOLTIP_MODE_ID),
                        this.getTextFieldRow(BOOK_TOOLTIP_AUTHOR_ID),
                        this.getTextFieldRow(BOOK_TOOLTIP_MESSAGE_ID)
                ),
                this.getScreenTab(ImagetextGuiTab.HOLOGRAM.getId(),
                        this.getNumberRow(HOLOGRAM_POS_X_ID, Integer.class),
                        this.getNumberRow(HOLOGRAM_POS_Y_ID, Integer.class),
                        this.getNumberRow(HOLOGRAM_POS_Z_ID, Integer.class)
                )
        );
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        this.setupImage(rootComponent, IMAGE_ID, IMAGE_SOURCE_TYPE_ID, ImageMode.URL);
        ImageButtonWidget imageWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(IMAGE_ID));
        ConfigToggleButton preserveImageAspectRatioWidget = rootComponent.childById(ConfigToggleButton.class, this.getToggleButtonId(PRESERVE_IMAGE_ASPECT_RATIO_ID));
        SliderWidget widthSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(WIDTH_ID));
        SliderWidget heightSlider = rootComponent.childById(SliderWidget.class, this.getSliderId(HEIGHT_ID));
        ButtonWidget.PressAction onWidthChange = button -> this.onResolutionChanged(imageWidget, preserveImageAspectRatioWidget, widthSlider, heightSlider, true);
        this.setupSlider(rootComponent, WIDTH_ID, DEFAULT_SIZE_VALUE, 2, MAX_SIZE_VALUE, Integer.class,
                aDouble -> onWidthChange.onPress(null)
        );
        this.setupSlider(rootComponent, HEIGHT_ID, DEFAULT_SIZE_VALUE, 2, MAX_SIZE_VALUE, Integer.class,
                aDouble -> this.onResolutionChanged(imageWidget, preserveImageAspectRatioWidget, heightSlider, widthSlider, false)
        );

        this.setupTextField(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT);
        this.setupBooleanButton(rootComponent, SHOW_RESOLUTION_ID, false);
        this.setupBooleanButton(rootComponent, SMOOTH_IMAGE_ID, true);
        //lore
        this.setupEnum(rootComponent, LORE_MODE_ID, LoreOption.ADD, null);
        //book page
        this.setupEnum(rootComponent, BOOK_PAGE_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        //book tooltip
        this.setupEnum(rootComponent, BOOK_TOOLTIP_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        this.setupTextField(rootComponent, BOOK_TOOLTIP_AUTHOR_ID, this.client.player.getName().getString());
        this.setupTextField(rootComponent, BOOK_TOOLTIP_MESSAGE_ID, FzmmClient.CONFIG.imagetext.defaultBookMessage());
        //hologram
        this.setupNumberField(rootComponent, HOLOGRAM_POS_X_ID, String.valueOf(this.client.player.getBlockX()));
        this.setupNumberField(rootComponent, HOLOGRAM_POS_Y_ID, String.valueOf(this.client.player.getBlockY()));
        this.setupNumberField(rootComponent, HOLOGRAM_POS_Z_ID, String.valueOf(this.client.player.getBlockZ()));
        //tabs
        this.selectScreenTab(rootComponent, selectedTab);
        for (var tab : ImagetextGuiTab.values()) {
            this.setupButton(rootComponent, this.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                button.active = false;
                selectedTab = tab;
            });
        }

        this.setupButton(rootComponent, this.getButtonId("execute"), false, button -> this.execute(rootComponent));
        this.setupButton(rootComponent, this.getButtonId("preview"), false, button -> {
            if (!imageWidget.hasNoImage()) {
                this.updateImagetext(rootComponent);
                button.tooltip(this.imagetextLogic.getText());
            }
        });
        imageWidget.setOnValueChanged(button -> {
            onWidthChange.onPress(null);
            if (!imageWidget.hasNoImage()) {
                rootComponent.childById(ButtonWidget.class, this.getButtonId("execute")).active = true;
                rootComponent.childById(ButtonWidget.class, this.getButtonId("preview")).active = true;
            }
        });
    }

    private enum ImagetextGuiTab implements IScreenTab {
        LORE("lore"),
        BOOK_PAGE("bookPage"),
        BOOK_TOOLTIP("bookTooltip"),
        HOLOGRAM("hologram"),
        //SIGN("sign"),
        JSON("json");

        private final String id;

        ImagetextGuiTab(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void onResolutionChanged(ImageButtonWidget imageWidget, ConfigToggleButton preserveImageAspectRatioButton,
                                     SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (imageWidget.hasNoImage() || !((boolean) preserveImageAspectRatioButton.parsedValue()))
            return;

        BufferedImage image = imageWidget.getImage();
        assert image != null;
        int configValue = (int) config.parsedValue();
        Vec2f rescaledSize = ImagetextLogic.changeResolutionKeepingAspectRatio(image.getWidth(), image.getHeight(), configValue, isWidth);

        int newValue = (int) (isWidth ? rescaledSize.y : rescaledSize.x);

        if (newValue > configToChange.max())
            newValue = (int) configToChange.max();
        else if (newValue < configToChange.min())
            newValue = (int) configToChange.min();

        configToChange.setDiscreteValueWithoutCallback(newValue);
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    public void execute(FlowLayout rootComponent) {
        ImageButtonWidget imageWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(IMAGE_ID));
        assert imageWidget != null;
        if (imageWidget.hasNoImage())
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        assert client != null;
        assert client.player != null;
        ItemStack stack = client.player.getMainHandStack();

        this.updateImagetext(rootComponent);


        switch (selectedTab) {
            case LORE -> {
                LoreOption loreOption = (LoreOption) rootComponent.childById(EnumWidget.class, this.getEnumId(LORE_MODE_ID)).getValue();
                ImagetextScreen.this.imagetextLogic.giveInLore(stack, loreOption == LoreOption.ADD);
            }
            case BOOK_PAGE -> {
                ImagetextBookOption bookOption = (ImagetextBookOption) rootComponent.childById(EnumWidget.class, this.getEnumId(BOOK_PAGE_MODE_ID)).getValue();
                ImagetextScreen.this.imagetextLogic.giveBookPage(bookOption);
            }
            case BOOK_TOOLTIP -> {
                ImagetextBookOption bookOption = (ImagetextBookOption) rootComponent.childById(EnumWidget.class, this.getEnumId(BOOK_TOOLTIP_MODE_ID)).getValue();
                String author = rootComponent.childById(TextFieldWidget.class, this.getTextFieldId(BOOK_TOOLTIP_AUTHOR_ID)).getText();
                String bookMessage = rootComponent.childById(TextFieldWidget.class, this.getTextFieldId(BOOK_TOOLTIP_MESSAGE_ID)).getText();
                try {
                    this.imagetextLogic.giveBookTooltip(author, bookMessage, bookOption);
                } catch (BookNbtOverflow e) {
                    this.client.getToastManager().add(new BookNbtOverflowToast(e));
                }
            }
            case HOLOGRAM -> {
                int x = Integer.parseInt(rootComponent.childById(ConfigTextBox.class, this.getNumberFieldId(HOLOGRAM_POS_X_ID)).getText());
                int y = Integer.parseInt(rootComponent.childById(ConfigTextBox.class, this.getNumberFieldId(HOLOGRAM_POS_Y_ID)).getText());
                int z = Integer.parseInt(rootComponent.childById(ConfigTextBox.class, this.getNumberFieldId(HOLOGRAM_POS_Z_ID)).getText());

                ImagetextScreen.this.imagetextLogic.giveAsHologram(x, y, z);
            }
            case JSON -> client.keyboard.setClipboard(ImagetextScreen.this.imagetextLogic.getImagetextString());
        }
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    public void updateImagetext(FlowLayout rootComponent) {
        ImageButtonWidget imageWidget = rootComponent.childById(ImageButtonWidget.class, this.getImageButtonId(IMAGE_ID));
        if (imageWidget.hasNoImage())
            return;

        BufferedImage image = imageWidget.getImage();
        String characters = rootComponent.childById(TextFieldWidget.class, this.getTextFieldId(CHARACTERS_ID)).getText();
        int width = (int) rootComponent.childById(SliderWidget.class, this.getSliderId(WIDTH_ID)).parsedValue();
        int height = (int) rootComponent.childById(SliderWidget.class, this.getSliderId(HEIGHT_ID)).parsedValue();
        boolean smoothScaling = (boolean) rootComponent.childById(ConfigToggleButton.class, this.getToggleButtonId(SMOOTH_IMAGE_ID)).parsedValue();
        boolean showResolution = (boolean) rootComponent.childById(ConfigToggleButton.class, this.getToggleButtonId(SHOW_RESOLUTION_ID)).parsedValue();

        if (selectedTab == ImagetextGuiTab.BOOK_PAGE) {
            width = this.imagetextLogic.getMaxImageWidthForBookPage(characters);
            height = 15;
        }

        this.imagetextLogic.generateImagetext(image, characters, width, height, smoothScaling);

        if (showResolution)
            this.imagetextLogic.addResolution();
    }

}
