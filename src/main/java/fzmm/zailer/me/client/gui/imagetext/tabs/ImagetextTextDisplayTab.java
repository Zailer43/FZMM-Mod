package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.SpawnEggBuilder;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.MathHelper;


@SuppressWarnings("UnstableApiUsage")
public class ImagetextTextDisplayTab implements IImagetextTab {
    private static final String TEXT_DISPLAY_TAG = "ImagetextTextDisplay";
    private SliderWidget textOpacity;
    private ConfigTextBox backgroundColor;
    private SmallCheckboxComponent textShadow;
    private SmallCheckboxComponent textSeeThrough;
    private ContextMenuButton textAlignmentButton;
    private DisplayEntity.TextDisplayEntity.TextAlignment textAlignment;
    private ContextMenuButton billboardButton;
    private DisplayEntity.BillboardMode billboard;
    private SliderWidget rotation;

    public ImagetextTextDisplayTab() {
    }

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        NbtCompound textDisplayNbt = new NbtCompound();

        textDisplayNbt.put(DisplayEntity.TextDisplayEntity.TEXT_NBT_KEY, TextCodecs.CODEC, logic.getText());
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_LINE_WIDTH, logic.getLineWidth());

        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_TEXT_OPACITY, (int) this.textOpacity.discreteValue());
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_BACKGROUND, ((Color) this.backgroundColor.parsedValue()).argb());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SHADOW, this.textShadow.checked());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SEE_THROUGH, this.textSeeThrough.checked());
        textDisplayNbt.putString(TagsConstant.TEXT_DISPLAY_ALIGNMENT, this.textAlignment.asString());
        textDisplayNbt.putString(DisplayEntity.BILLBOARD_NBT_KEY, this.billboard.asString());

        NbtList rotationList = new NbtList();
        rotationList.add(NbtFloat.of((float) this.rotation.discreteValue()));
        rotationList.add(NbtFloat.of(0f));
        textDisplayNbt.put(TagsConstant.ENTITY_ROTATION_ID, rotationList);

        NbtList tagList = new NbtList();
        tagList.add(NbtString.of(TEXT_DISPLAY_TAG));
        textDisplayNbt.put(TagsConstant.ENTITY_TAG_TAGS_ID, tagList);

        ItemStack spawnEgg = SpawnEggBuilder.builder()
                .entityType(EntityType.TEXT_DISPLAY)
                .entityTag(textDisplayNbt)
                .get();

        ItemUtils.give(spawnEgg);
        InvisibleEntityWarning.add(false, false, Text.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.textDisplay"), TEXT_DISPLAY_TAG);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        assert MinecraftClient.getInstance().player != null;

        this.textOpacity = SliderRow.setup(rootComponent, "textDisplayTextOpacity", 255, 0, 255, Integer.class, 0, 10, null);
        this.backgroundColor = ColorRow.setup(rootComponent, "textDisplayBackgroundColor", Color.ofArgb(DisplayEntity.TextDisplayEntity.INITIAL_BACKGROUND), true, 0, null);
        this.textShadow = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "textDisplayTextShadow-checkbox");
        this.textShadow.checked(false);
        this.textSeeThrough = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "textDisplayTextSeeThrough-checkbox");
        this.textSeeThrough.checked(false);
        this.textAlignmentButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "textDisplayTextAlignment");
        this.textAlignmentButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : DisplayEntity.TextDisplayEntity.TextAlignment.values()) {
                dropdownComponent.button(this.getTextAlignmentMessage(option), dropdownButton -> {
                    this.updateTextAlignment(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateTextAlignment(DisplayEntity.TextDisplayEntity.TextAlignment.LEFT);
        this.billboardButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "textDisplayBillboard");
        this.billboardButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : DisplayEntity.BillboardMode.values()) {
                dropdownComponent.button(this.getBillboardMessage(option), dropdownButton -> {
                    this.updateBillboard(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBillboard(DisplayEntity.BillboardMode.FIXED);
        this.rotation = SliderRow.setup(rootComponent, "textDisplayRotation", MathHelper.wrapDegrees(MinecraftClient.getInstance().player.getYaw()), -180, 180, Float.class, 1, 30, null);
    }

    private void updateTextAlignment(DisplayEntity.TextDisplayEntity.TextAlignment value) {
        this.textAlignment = value;
        this.textAlignmentButton.setMessage(this.getTextAlignmentMessage(this.textAlignment));
    }

    public Text getTextAlignmentMessage(DisplayEntity.TextDisplayEntity.TextAlignment value) {
        return Text.translatable("fzmm.gui.option.text_alignment." + value.asString());
    }

    public void updateBillboard(DisplayEntity.BillboardMode value) {
        this.billboard = value;
        this.billboardButton.setMessage(this.getBillboardMessage(this.billboard));
    }

    public Text getBillboardMessage(DisplayEntity.BillboardMode value) {
        return Text.translatable("fzmm.gui.option.billboard." + value.asString());
    }

    @Override
    public String getId() {
        return "textDisplay";
    }

    @Override
    public IMementoObject createMemento() {
        return new TextDisplayMementoTab(
                (int) this.textOpacity.discreteValue(),
                this.backgroundColor.getText(),
                this.textShadow.checked(),
                this.textSeeThrough.checked(),
                this.textAlignment,
                this.billboard
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        TextDisplayMementoTab memento = (TextDisplayMementoTab) mementoTab;
        this.textOpacity.setFromDiscreteValue(memento.textOpacity);
        this.backgroundColor.text(memento.backgroundColor);
        this.textShadow.checked(memento.textShadow);
        this.textSeeThrough.checked(memento.textSeeThrough);
        this.updateTextAlignment(memento.textAlignment);
        this.updateBillboard(memento.billboard);
    }

    private record TextDisplayMementoTab(int textOpacity, String backgroundColor, boolean textShadow,
                                         boolean textSeeThrough, DisplayEntity.TextDisplayEntity.TextAlignment textAlignment,
                                         DisplayEntity.BillboardMode billboard) implements IMementoObject {
    }
}
