package fzmm.zailer.me.client.gui.player_statue;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.utils.auto_placer.AbstractAutoPlacer;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.TagsConstant;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.BaseEntityBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PlayerStatuePlacerScreen extends AbstractAutoPlacer {
    public static boolean isActive = false;
    private final ItemStack playerStatueStack;
    private final List<ItemStack> containerItems;

    public PlayerStatuePlacerScreen(ItemStack playerStatueStack) {
        super("utils/base_auto_placer", "playerStatuePlacer", null);
        this.playerStatueStack = playerStatueStack;
        this.containerItems = InventoryUtils.getItemsFromContainer(this.playerStatueStack);
    }

    public static AutoPlacerHud.Activation getActivation() {
        Predicate<ItemStack> predicate = itemStack -> !PlayerStatuePlacerScreen.isActive &&
                itemStack.getItem() instanceof BlockItem blockItem &&
                blockItem.getBlock() instanceof BaseEntityBlock &&
                PlayerStatue.isPlayerStatue(itemStack);

        List<AutoPlacerHud.Requirement> requirements = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        requirements.add(new AutoPlacerHud.Requirement(() -> {
            float yaw = Mth.wrapDegrees(client.player.getYRot());
            return yaw > 80 && yaw < 110;
        }, net.minecraft.network.chat.Component.translatable("fzmm.gui.playerStatuePlacer.label.requirement.invalidYaw")));

        return new AutoPlacerHud.Activation(predicate, PlayerStatuePlacerScreen::new, requirements);
    }

    @Override
    protected List<UIComponent> getInfoLabels() {
        List<UIComponent> labelList = new ArrayList<>();

        labelList.add(EComponents.label(this.playerStatueStack.getHoverName()));

        for (var text : DisplayBuilder.of(this.playerStatueStack).getLoreText()) {
            labelList.add(EComponents.label(text));
        }

        return labelList;
    }

    @Override
    protected ItemStack processStack(ItemStack stack) {
        // armor stand does not need a custom name
        stack.remove(DataComponents.CUSTOM_NAME);

        // since 1.21.5 custom data are transferred to the entity
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, component -> {
            if (component.isEmpty()) return component;

            CompoundTag result = component.copyTag();
            result.remove(TagsConstant.FZMM);

            return CustomData.of(result);
        });

        return stack;
    }

    @Override
    protected List<ItemStack> getItems() {
        return this.containerItems;
    }

    @Override
    protected ItemStack getFinalStack() {
        return this.playerStatueStack;
    }

    @Override
    protected boolean isActive() {
        return isActive;
    }

    @Override
    protected void setActive(boolean active) {
        super.setActive(active);
        PlayerStatuePlacerScreen.isActive = active;
    }
}
