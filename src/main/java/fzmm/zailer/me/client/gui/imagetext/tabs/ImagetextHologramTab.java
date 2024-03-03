package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextHologramTab implements IImagetextTab {
    private static final String POS_X_ID = "hologramPosX";
    private static final String POS_Y_ID = "hologramPosY";
    private static final String POS_Z_ID = "hologramPosZ";
    private static final String HOLOGRAM_TAG = "ImagetextHologram";
    private static final String BASE_ITEMS_TRANSLATION_KEY = "fzmm.item.imagetext.hologram.";
    private static final float Y_DISTANCE = 0.23f;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        int x = (int) this.posX.parsedValue();
        int y = (int) this.posY.parsedValue();
        int z = (int) this.posZ.parsedValue();
        int color = FzmmClient.CONFIG.colors.imagetextHologram().rgb();

        List<ItemStack> hologramContainers = ContainerBuilder.builder()
                .of(Items.WHITE_SHULKER_BOX.getDefaultStack(), false)//todo
                .addAll(this.getHologramItems(logic, x, y, z))
                .getAsContainerList();

        ItemStack hologramMainContainer = ContainerBuilder.builder()
                .of(Items.WHITE_SHULKER_BOX.getDefaultStack(), false)//TODO
                .addAll(hologramContainers)
                .getAsContainerList().get(0);

        hologramMainContainer = DisplayBuilder.of(hologramMainContainer)
                .setName(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "name"), color)
                .addLore(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "lore.1", x, y, z), color)
                .addLore(Text.translatable(BASE_ITEMS_TRANSLATION_KEY + "lore.2", logic.getWidth(), logic.getHeight()), color)
                .get();

        FzmmUtils.giveItem(hologramMainContainer);
    }

    public List<ItemStack> getHologramItems(ImagetextLogic logic, int x, double y, int z) {
        List<ItemStack> hologramItems = new ArrayList<>();
        NbtList imagetext = logic.get();
        int size = imagetext.size();

        for (int i = 0; i != size; i++) {
            y += Y_DISTANCE;
            ItemStack armorStandHologram = ArmorStandBuilder.builder()
                    .setPos(x, y, z)
                    .setTags(HOLOGRAM_TAG)
                    .setAsHologram(imagetext.get(size - i - 1).asString())
                    .getItem(String.valueOf(i));

            hologramItems.add(armorStandHologram);
        }

        return hologramItems;
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        this.posX = NumberRow.setup(rootComponent, POS_X_ID, player.getBlockX(), Integer.class);
        this.posY = NumberRow.setup(rootComponent, POS_Y_ID, player.getBlockY(), Integer.class);
        this.posZ = NumberRow.setup(rootComponent, POS_Z_ID, player.getBlockZ(), Integer.class);
    }

    @Override
    public String getId() {
        return "hologram";
    }

    public static boolean isHologramPart(ItemStack stack) {
        if (!stack.hasNbt())
            return false;

        NbtCompound entityNbt = stack.getOrCreateSubNbt(EntityType.ENTITY_TAG_KEY);

        NbtList tags = entityNbt.getList(TagsConstant.ENTITY_TAG_TAGS_ID, NbtElement.STRING_TYPE);

        for (int i = 0; i < tags.size(); i++) {
            if (tags.getString(i).equals(HOLOGRAM_TAG))
                return true;
        }

        return false;
    }

    @Override
    public IMementoObject createMemento() {
        return null;
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
    }
}
