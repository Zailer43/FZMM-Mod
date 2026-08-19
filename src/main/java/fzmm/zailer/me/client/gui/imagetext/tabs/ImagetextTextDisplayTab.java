package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.SpawnEggBuilder;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


@SuppressWarnings("UnstableApiUsage")
public class ImagetextTextDisplayTab implements IImagetextTab, IMemento {
    private static final String TEXT_DISPLAY_TAG = "ImagetextTextDisplay";
    private SliderWidget textOpacity;
    private ConfigTextBox backgroundColor;
    private SmallCheckboxComponent textShadow;
    private SmallCheckboxComponent textSeeThrough;
    private ContextMenuButton textAlignmentButton;
    private Display.TextDisplay.Align textAlignment;
    private ContextMenuButton billboardButton;
    private Display.BillboardConstraints billboard;
    private SliderWidget rotation;

    public ImagetextTextDisplayTab() {
    }

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        CompoundTag textDisplayNbt = new CompoundTag();
        int width = logic.isEmpty() ? 1 : Minecraft.getInstance().font.width(logic.text().get(0));

        textDisplayNbt.store(Display.TextDisplay.TAG_TEXT, ComponentSerialization.CODEC, logic.mergeText());
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_LINE_WIDTH, width);

        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_TEXT_OPACITY, (int) this.textOpacity.discreteValue());
        textDisplayNbt.putInt(TagsConstant.TEXT_DISPLAY_BACKGROUND, ((Color) this.backgroundColor.parsedValue()).argb());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SHADOW, this.textShadow.checked());
        textDisplayNbt.putBoolean(TagsConstant.TEXT_DISPLAY_SEE_THROUGH, this.textSeeThrough.checked());
        textDisplayNbt.putString(TagsConstant.TEXT_DISPLAY_ALIGNMENT, this.textAlignment.getSerializedName());
        textDisplayNbt.putString(Display.TAG_BILLBOARD, this.billboard.getSerializedName());

        ListTag rotationList = new ListTag();
        rotationList.add(FloatTag.valueOf((float) this.rotation.discreteValue()));
        rotationList.add(FloatTag.valueOf(0f));
        textDisplayNbt.put(TagsConstant.ENTITY_ROTATION_ID, rotationList);

        ListTag tagList = new ListTag();
        tagList.add(StringTag.valueOf(TEXT_DISPLAY_TAG));
        textDisplayNbt.put(TagsConstant.ENTITY_TAG_TAGS_ID, tagList);

        ItemStack spawnEgg = SpawnEggBuilder.builder()
                .entityType(EntityTypes.TEXT_DISPLAY)
                .entityTag(textDisplayNbt)
                .get();

        ItemUtils.give(spawnEgg);
        InvisibleEntityWarning.add(false, false, Component.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.textDisplay"), TEXT_DISPLAY_TAG);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        assert Minecraft.getInstance().player != null;

        this.textOpacity = SliderRow.setup(rootComponent, "textDisplayTextOpacity", 255, 0, 255, Integer.class, 0, 10, null);
        this.backgroundColor = ColorRow.setup(rootComponent, "textDisplayBackgroundColor", Color.ofArgb(Display.TextDisplay.INITIAL_BACKGROUND), true,  null);
        this.textShadow = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "textDisplayTextShadow-checkbox");
        this.textShadow.checked(false);
        this.textSeeThrough = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "textDisplayTextSeeThrough-checkbox");
        this.textSeeThrough.checked(false);
        this.textAlignmentButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "textDisplayTextAlignment");
        this.textAlignmentButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : Display.TextDisplay.Align.values()) {
                dropdownComponent.button(this.getTextAlignmentMessage(option), dropdownButton -> {
                    this.updateTextAlignment(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateTextAlignment(Display.TextDisplay.Align.LEFT);
        this.billboardButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "textDisplayBillboard");
        this.billboardButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : Display.BillboardConstraints.values()) {
                dropdownComponent.button(this.getBillboardMessage(option), dropdownButton -> {
                    this.updateBillboard(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBillboard(Display.BillboardConstraints.FIXED);
        this.rotation = SliderRow.setup(rootComponent, "textDisplayRotation", Mth.wrapDegrees(Minecraft.getInstance().player.getYRot()), -180, 180, Float.class, 1, 30, null);
    }

    private void updateTextAlignment(Display.TextDisplay.Align value) {
        this.textAlignment = value;
        this.textAlignmentButton.setMessage(this.getTextAlignmentMessage(this.textAlignment));
    }

    public Component getTextAlignmentMessage(Display.TextDisplay.Align value) {
        return Component.translatable("fzmm.gui.option.text_alignment." + value.getSerializedName());
    }

    public void updateBillboard(Display.BillboardConstraints value) {
        this.billboard = value;
        this.billboardButton.setMessage(this.getBillboardMessage(this.billboard));
    }

    public Component getBillboardMessage(Display.BillboardConstraints value) {
        return Component.translatable("fzmm.gui.option.billboard." + value.getSerializedName());
    }

    @Override
    public String getId() {
        return "textDisplay";
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeInt((int) this.textOpacity.discreteValue());
        output.writeObject(this.backgroundColor.getValue());
        output.writeBoolean(this.textShadow.checked());
        output.writeBoolean(this.textSeeThrough.checked());
        output.writeObject(this.textAlignment);
        output.writeObject(this.billboard);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.textOpacity.setFromDiscreteValue(input.readInt());
        this.backgroundColor.text((String) input.readObject());
        this.textShadow.checked(input.readBoolean());
        this.textSeeThrough.checked(input.readBoolean());
        this.updateTextAlignment((Display.TextDisplay.Align) input.readObject());
        this.updateBillboard((Display.BillboardConstraints) input.readObject());
    }
}
