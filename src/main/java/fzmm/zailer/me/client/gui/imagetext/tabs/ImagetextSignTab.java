package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.SignBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import net.minecraft.block.Blocks;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImagetextSignTab implements IImagetextTab, IImagetextTooltip {
    private static final String BASE_ITEMS_TRANSLATION_KEY = "fzmm.item.imagetext.sign.";
    private ContextMenuButton signTypeButton;
    private SmallCheckboxComponent isHangingSignButton;
    private String characters;
    private WoodType woodType;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        this.characters = algorithm.getCharacters();
        if (isExecute) {
            logic.generateImagetext(algorithm, data, this.getLineSplitInterval(this.characters));
        } else {
            logic.generateImagetext(algorithm, data);
        }
    }

    @Override
    public void execute(ImagetextLogic logic) {
        assert MinecraftClient.getInstance().player != null;

        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();

        List<ItemStack> signContainers = ContainerBuilder.builder()
                .containerItem(Items.GRAY_SHULKER_BOX)//todo
                .maxItemByContainer(27)
                .addAll(this.getSignItems(logic))
                .getAsList();

        ItemStack signMainContainer = ContainerBuilder.builder()
                .containerItem(Items.LIGHT_GRAY_SHULKER_BOX)//TODO
                .maxItemByContainer(27)
                .add(
                        DisplayBuilder.builder()
                                .item(Items.PAPER)
                                .setName(
                                        Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "details.name",
                                                this.getHorizontalSigns(logic.getWidth(), this.getLineSplitInterval(this.characters)),
                                                this.getVerticalSigns(logic.getHeight())
                                        ), color)
                                .get()
                ).addAll(signContainers)
                .getAsList().get(0);

        signMainContainer = DisplayBuilder.of(signMainContainer)
                .setName(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.name"), color)
                .addLore(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.lore.1", logic.getWidth(), logic.getHeight()), color)
                .get();

        ItemUtils.give(signMainContainer);
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.signTypeButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "signType");
        this.signTypeButton.setContextMenuOptions(dropdownComponent -> {
            List<WoodType> optionList = WoodType.stream()
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

    private Text getSignText(WoodType type) {
        return Text.translatable("block.minecraft." + type.name() + "_sign");
    }

    public List<ItemStack> getSignItems(ImagetextLogic logic) {
        List<SignBuilder> signBuilders = new ArrayList<>();
        List<Text> imagetext = logic.getWrappedText();
        int width = logic.getWidth();
        int height = logic.getHeight();

        int lineSplitInterval = this.getLineSplitInterval(this.characters);
        int horizontalSigns = this.getHorizontalSigns(width, lineSplitInterval);
        int verticalSigns = this.getVerticalSigns(height);
        int maxTextWidth = this.getMaxTextWidth();
        Item item = this.getItem();


        for (int y = 0; y != verticalSigns; y++) {
            for (int x = 0; x != horizontalSigns; x++) {
                int index = y * horizontalSigns + x;

                if (signBuilders.size() <= index) {
                    signBuilders.add(SignBuilder.builder().item(item));
                }

                SignBuilder signBuilder = signBuilders.get(index);

                for (int i = 0; i != SignBuilder.MAX_ROWS; i++) {
                    int imagetextIndex = (y * SignBuilder.MAX_ROWS + i) * horizontalSigns + x;
                    if (imagetext.size() > imagetextIndex) {
                        signBuilder.addFrontLine(imagetext.get(imagetextIndex), maxTextWidth);
                    }
                }
            }
        }

        return this.formatSignItems(signBuilders, horizontalSigns);
    }

    public int getHorizontalSigns(int width, int lineSplitInterval) {
        int horizontalSigns = (int) Math.floor(width / (double) lineSplitInterval);
        if (width % lineSplitInterval != 0)
            horizontalSigns++;

        return horizontalSigns;
    }

    public int getVerticalSigns(int height) {
        int verticalSigns = (int) Math.floor(height / (double) SignBuilder.MAX_ROWS);
        if (height % SignBuilder.MAX_ROWS != 0)
            verticalSigns++;
        return verticalSigns;
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

    public int getLineSplitInterval(String characters) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int textWidth = textRenderer.getWidth(characters);
        double numRepeats = this.getMaxTextWidth() / (double) textWidth;

        return (int) Math.floor(numRepeats * characters.length());
    }

    public int getMaxTextWidth() {
        SignBlockEntity signBlockEntity = this.isHangingSignButton.checked() ?
                new HangingSignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_HANGING_SIGN.getDefaultState()) :
                new SignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_SIGN.getDefaultState());

        return signBlockEntity.getMaxTextWidth();
    }

    public Item getItem() {
        boolean isHangingSign = this.isHangingSignButton.checked();

        for (var block : Registries.BLOCK.stream().toList()) {
            if (isHangingSign && block instanceof HangingSignBlock hangingSignBlock && hangingSignBlock.getWoodType() == this.woodType) {
                return hangingSignBlock.asItem();
            } else if (!isHangingSign && block instanceof SignBlock signBlock && signBlock.getWoodType() == this.woodType) {
                return signBlock.asItem();
            }
        }

        return Items.OAK_SIGN;
    }

    @Override
    public IMementoObject createMemento() {
        return new SignMementoTab(this.woodType, this.isHangingSignButton.checked());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        SignMementoTab memento = (SignMementoTab) mementoTab;
        this.updateSignType(memento.signType);
        this.isHangingSignButton.checked(memento.isHangingSign());
    }

    @Override
    public Text getTooltip(ImagetextLogic logic) {
        int lineSplitInterval = this.getLineSplitInterval(this.characters);
        int horizontalSigns = this.getHorizontalSigns(logic.getWidth(), lineSplitInterval);
        int verticalSigns = this.getVerticalSigns(logic.getHeight());
        return Text.translatable("fzmm.gui.imagetext.tab.sign.tooltip", horizontalSigns, verticalSigns);
    }

    private record SignMementoTab(WoodType signType, boolean isHangingSign) implements IMementoObject {
    }
}
