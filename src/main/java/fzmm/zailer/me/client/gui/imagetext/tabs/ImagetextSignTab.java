package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.SignBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TextUtils;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImagetextSignTab implements IImagetextTab, IImagetextTooltip, IMemento {
    private static final String BASE_ITEMS_TRANSLATION_KEY = "fzmm.item.imagetext.sign.";
    private ContextMenuButton signTypeButton;
    private SmallCheckboxComponent isHangingSignButton;
    private WoodType woodType;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();

        List<ItemStack> signContainers = ContainerBuilder.builder()
                .containerItem(Items.DYED_SHULKER_BOX.gray())//TODO: replace hardcoded
                .maxItemByContainer(27)
                .addAll(this.signItemsOf(logic))
                .getAsList();

        ItemStack signMainContainer = ContainerBuilder.builder()
                .containerItem(Items.DYED_SHULKER_BOX.lightGray())
                .maxItemByContainer(27)
                .add(
                        DisplayBuilder.builder()
                                .item(Items.PAPER)
                                .setName(
                                        Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "details.name",
                                                this.horizontalSignsOf(logic.text()),
                                                this.verticalSignsOf(logic.height())
                                        ), color)
                                .get()
                ).addAll(signContainers)
                .getAsList().get(0);

        signMainContainer = DisplayBuilder.of(signMainContainer)
                .setName(Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.name"), color)
                .addLore(Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.lore.1", logic.width(), logic.height()), color)
                .get();

        ItemUtils.give(signMainContainer);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.signTypeButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "signType");
        this.signTypeButton.setContextMenuOptions(dropdownComponent -> {
            List<WoodType> optionList = WoodType.values()
                    .sorted(Comparator.comparing(woodType1 -> this.getSignText(woodType1).getString()))
                    .toList();
            for (var option : optionList) {
                dropdownComponent.button(this.getSignText(option), dropdownButton -> {
                    this.updateSignType(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateSignType(WoodType.OAK);
        this.isHangingSignButton = rootComponent.childByIdOrThrow(SmallCheckboxComponent.class, "isHangingSign-checkbox");
        this.isHangingSignButton.checked(false);
    }

    private void updateSignType(WoodType option) {
        this.woodType = option;
        this.signTypeButton.setMessage(this.getSignText(this.woodType));
    }

    @Override
    public String getId() {
        return "sign";
    }

    private Component getSignText(WoodType type) {
        return Component.translatable("block.minecraft." + type.name() + "_sign");
    }

    public List<ItemStack> signItemsOf(ImagetextLogic logic) {
        Font textRenderer = Minecraft.getInstance().font;
        List<SignBuilder> signBuilders = new ArrayList<>();
        List<Component> imagetext = logic.text();
        int height = logic.height();

        int horizontalSigns = this.horizontalSignsOf(imagetext);
        int verticalSigns = this.verticalSignsOf(height);
        int maxTextWidth = this.getMaxTextWidth();
        Item item = this.getItem();

        FormattedCharSequence[][] imagetextWrapped = new FormattedCharSequence[imagetext.size()][horizontalSigns];
        for (int i = 0; i != imagetext.size(); i++) {
            imagetextWrapped[i] = textRenderer.split(imagetext.get(i), maxTextWidth).toArray(FormattedCharSequence[]::new);
        }

        for (int y = 0; y != verticalSigns; y++) {
            for (int x = 0; x != horizontalSigns; x++) {
                int index = y * horizontalSigns + x;

                if (signBuilders.size() <= index) {
                    signBuilders.add(SignBuilder.builder().item(item));
                }

                this.addSignLines(x, y, signBuilders.get(index), imagetextWrapped, maxTextWidth);
            }
        }

        return this.formatSignItems(signBuilders, horizontalSigns);
    }

    public int horizontalSignsOf(List<Component> imagetext) {
        if (imagetext.isEmpty()) return 0;
        int maxWidth = this.getMaxTextWidth() - 1;
        return Minecraft.getInstance().font.split(imagetext.get(0), maxWidth).size();
    }

    public int verticalSignsOf(int height) {
        if (height <= SignBuilder.MAX_ROWS) return 1;

        return (int) Math.floor(height / (double) SignBuilder.MAX_ROWS);
    }

    public void addSignLines(int x, int y, SignBuilder builder, FormattedCharSequence[][] imagetextWrapped, int maxTextWidth) {
        for (int i = 0; i != SignBuilder.MAX_ROWS; i++) {
            int index = y * SignBuilder.MAX_ROWS + i;
            if (index < imagetextWrapped.length && x < imagetextWrapped[index].length) {
                builder.addFrontLine(this.orderedTextToText(imagetextWrapped[index][x]), maxTextWidth);
            }
        }
    }

    // why exist OrderedText and no a method to convert it to Text ???
    private Component orderedTextToText(FormattedCharSequence text) {
        ImagetextLine line = new ImagetextLine(0d);
        StringBuilder characters = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            characters.appendCodePoint(codePoint);
            return true;
        });
        // line needs all characters, and use of ImagetextData can give different results because text was wrapped
        line.characters(TextUtils.splitMessage(characters.toString()).toArray(String[]::new));

        text.accept((index, characterStyle, c) -> {
            if (characterStyle.getColor() != null) {
                line.add(characterStyle.getColor().getValue());
            }
            return true;
        });
        return line.build();
    }

    public List<ItemStack> formatSignItems(List<SignBuilder> signBuilders, int signsPerLine) {
        List<ItemStack> signStackList = new ArrayList<>();
        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();

        for (int i = 0; i != signBuilders.size(); i++) {
            ItemStack sign = signBuilders.get(i).wax().get();

            sign = DisplayBuilder.of(sign)
                    .setName(String.format("X: %d - Y: %d", i % signsPerLine + 1, i / signsPerLine + 1), color)
                    .get();
            signStackList.add(sign);
        }

        return signStackList;
    }

    public int getMaxTextWidth() {
        SignBlockEntity signBlockEntity = this.isHangingSignButton.checked() ?
                new HangingSignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_HANGING_SIGN.defaultBlockState()) :
                new SignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_SIGN.defaultBlockState());

        return signBlockEntity.getMaxTextLineWidth();
    }

    public Item getItem() {
        boolean isHangingSign = this.isHangingSignButton.checked();

        for (var block : BuiltInRegistries.BLOCK.stream().toList()) {
            if (isHangingSign && block instanceof CeilingHangingSignBlock hangingSignBlock && hangingSignBlock.type() == this.woodType) {
                return hangingSignBlock.asItem();
            } else if (!isHangingSign && block instanceof StandingSignBlock signBlock && signBlock.type() == this.woodType) {
                return signBlock.asItem();
            }
        }

        return Items.OAK_SIGN;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.woodType.name());
        output.writeBoolean(this.isHangingSignButton.checked());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        String woodName = (String) input.readObject();
        WoodType woodType = WoodType.values()
                .filter(woodType1 -> woodType1.name().equals(woodName))
                .findFirst()
                .orElse(WoodType.OAK);
        this.updateSignType(woodType);
        this.isHangingSignButton.checked(input.readBoolean());
    }

    @Override
    public Component getTooltip(ImagetextLogic logic) {
        int horizontalSigns = this.horizontalSignsOf(logic.text());
        int verticalSigns = this.verticalSignsOf(logic.height());
        return Component.translatable("fzmm.gui.imagetext.tab.sign.tooltip", horizontalSigns, verticalSigns);
    }

}
