package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.ContainerBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextHologramTab implements IImagetextTab {
    private static final String HOLOGRAM_TAG = "ImagetextHologram";
    private static final String BASE_ITEMS_TRANSLATION_KEY = "fzmm.item.imagetext.hologram.";
    private static final float Y_DISTANCE = 0.23f;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        int x = (int) this.posX.parsedValue();
        int y = (int) this.posY.parsedValue();
        int z = (int) this.posZ.parsedValue();
        int color = FzmmClient.CONFIG.colors.imagetextHologram().rgb();

        List<ItemStack> hologramContainers = ContainerBuilder.builder()
                .containerItem(Items.DYED_SHULKER_BOX.white())//todo
                .maxItemByContainer(27)
                .addAll(this.getHologramItems(logic, x, y, z))
                .getAsList();

        ItemStack hologramMainContainer = ContainerBuilder.builder()
                .containerItem(Items.DYED_SHULKER_BOX.white())//TODO
                .maxItemByContainer(27)
                .addAll(hologramContainers)
                .getAsList().get(0);

        hologramMainContainer = DisplayBuilder.of(hologramMainContainer)
                .setName(Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "name"), color)
                .addLore(Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "lore.1", x, y, z), color)
                .addLore(Component.translatable(BASE_ITEMS_TRANSLATION_KEY + "lore.2", logic.width(), logic.height()), color)
                .get();

        ItemUtils.give(hologramMainContainer);
        InvisibleEntityWarning.add(true, true, Component.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.hologram"), HOLOGRAM_TAG);
    }

    public List<ItemStack> getHologramItems(ImagetextLogic logic, int x, double y, int z) {
        List<ItemStack> hologramItems = new ArrayList<>();
        List<Component> imagetext = logic.text();
        int size = imagetext.size();

        for (int i = 0; i != size; i++) {
            y += Y_DISTANCE;
            ItemStack armorStandHologram = ArmorStandBuilder.builder()
                    .setPos(x, y, z)
                    .setTags(HOLOGRAM_TAG)
                    .setAsHologram(imagetext.get(size - i - 1))
                    .getItem(String.valueOf(i));

            hologramItems.add(armorStandHologram);
        }

        return hologramItems;
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        this.posX = NumberRow.setup(rootComponent, "hologramPosX", player.getBlockX(), Integer.class);
        this.posY = NumberRow.setup(rootComponent, "hologramPosY", player.getBlockY(), Integer.class);
        this.posZ = NumberRow.setup(rootComponent, "hologramPosZ", player.getBlockZ(), Integer.class);
    }

    @Override
    public String getId() {
        return "hologram";
    }

    public static boolean isHologramPart(ItemStack stack) {
        CompoundTag entityNbt = stack.getOrDefault(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityTypes.ARMOR_STAND, new CompoundTag()))
                .copyTagWithoutId();

        ListTag tags = entityNbt.getListOrEmpty(TagsConstant.ENTITY_TAG_TAGS_ID);

        for (int i = 0; i < tags.size(); i++) {
            if (tags.getStringOr(i, "").equals(HOLOGRAM_TAG))
                return true;
        }

        return false;
    }
}
