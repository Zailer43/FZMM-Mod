package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.builders.SignBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.gui.options.SignTypeOption;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
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
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ImagetextSignTab implements IImagetextTab {
    private static final String BASE_ITEMS_TRANSLATION_KEY = "fzmm.item.imagetext.sign.";

    private static final String SIGN_TYPE_ID = "signType";
    private static final String IS_HANGING_ID = "isHangingSign";
    private EnumWidget signTypeEnum;
    private BooleanButton isHangingSignButton;


    @Override
    public void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        if (isExecute)
            logic.generateImagetext(data, this.getLineSplitInterval(data.characters()));
        else
            logic.generateImagetext(data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        assert MinecraftClient.getInstance().player != null;

        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();

        List<ItemStack> signContainers = ContainerBuilder.builder()
                .containerItem(Items.LIGHT_GRAY_SHULKER_BOX)//todo
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
                                                this.getHorizontalSigns(logic.getWidth(), this.getLineSplitInterval(logic.getCharacters())),
                                                this.getVerticalSigns(logic.getHeight())
                                        ), color)
                                .get()
                ).addAll(signContainers)
                .getAsList().get(0);

        signMainContainer = DisplayBuilder.of(signMainContainer)
                .setName(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.name"), color)
                .addLore(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "container.lore.1", logic.getWidth(), logic.getHeight()), color)
                .get();

        FzmmUtils.giveItem(signMainContainer);
    }

    @Override
    public String getId() {
        return "sign";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.signTypeEnum = EnumRow.setup(rootComponent, SIGN_TYPE_ID, SignTypeOption.OAK, null);
        this.isHangingSignButton = BooleanRow.setup(rootComponent, IS_HANGING_ID, false);
    }

    public List<ItemStack> getSignItems(ImagetextLogic logic) {
        List<SignBuilder> signBuilders = new ArrayList<>();
        NbtList imagetext = logic.get();
        int width = logic.getWidth();
        int height = logic.getHeight();

        int lineSplitInterval = this.getLineSplitInterval(logic.getCharacters());
        int horizontalSigns = this.getHorizontalSigns(width, lineSplitInterval);
        int verticalSigns = this.getVerticalSigns(height);
        int maxTextWidth = this.getMaxTextWidth();
        Item item = this.getItem();


        for (int y = 0; y != verticalSigns; y++) {
            for (int x = 0; x != horizontalSigns; x++) {
                int index = y * horizontalSigns + x;

                if (signBuilders.size() <= index)
                    signBuilders.add(SignBuilder.builder().item(item));

                SignBuilder signBuilder = signBuilders.get(index);

                for (int i = 0; i != SignBuilder.MAX_ROWS; i++) {
                    int imagetextIndex = (y * SignBuilder.MAX_ROWS + i) * horizontalSigns + x;
                    if (imagetext.size() > imagetextIndex)
                        signBuilder.addLine((NbtString) imagetext.get(imagetextIndex), maxTextWidth);
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
            ItemStack sign = signBuilders.get(i).get();

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

        return (int) Math.ceil(numRepeats * characters.length());
    }

    public int getMaxTextWidth() {
        SignBlockEntity signBlockEntity = this.isHangingSignButton.enabled() ?
                new HangingSignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_HANGING_SIGN.getDefaultState()) :
                new SignBlockEntity(new BlockPos(0, 0, 0), Blocks.OAK_SIGN.getDefaultState());

        return signBlockEntity.getMaxTextWidth();
    }

    public Item getItem() {
        WoodType type = ((SignTypeOption) this.signTypeEnum.getValue()).getType();
        boolean isHangingSign = this.isHangingSignButton.enabled();

        for (var block : Registries.BLOCK.stream().toList()) {
            if (isHangingSign && block instanceof HangingSignBlock hangingSignBlock && hangingSignBlock.getWoodType() == type)
                return hangingSignBlock.asItem();
            else if (!isHangingSign && block instanceof SignBlock signBlock && signBlock.getWoodType() == type)
                return signBlock.asItem();

        }

        return Items.OAK_SIGN;
    }

    @Override
    public IMementoObject createMemento() {
        return new SignMementoTab((SignTypeOption) this.signTypeEnum.getValue(), this.isHangingSignButton.enabled());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        SignMementoTab memento = (SignMementoTab) mementoTab;
        this.signTypeEnum.setValue(memento.signType);
        this.isHangingSignButton.enabled(memento.isHangingSign());
    }

    private record SignMementoTab(SignTypeOption signType, boolean isHangingSign) implements IMementoObject {
    }
}
