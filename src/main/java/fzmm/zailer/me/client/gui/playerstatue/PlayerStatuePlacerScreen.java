package fzmm.zailer.me.client.gui.playerstatue;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.utils.autoplacer.AbstractAutoPlacer;
import fzmm.zailer.me.client.gui.utils.autoplacer.AutoPlacerHud;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.utils.InventoryUtils;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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
        this.containerItems = InventoryUtils.getItemsFromContainer(this.playerStatueStack, false);
    }

    public static AutoPlacerHud.Activation getActivation() {
        Predicate<ItemStack> predicate = itemStack -> !PlayerStatuePlacerScreen.isActive &&
                itemStack.getItem() instanceof BlockItem blockItem &&
                blockItem.getBlock() instanceof BlockWithEntity &&
                PlayerStatue.isPlayerStatue(itemStack);

        List<AutoPlacerHud.Requirement> requirements = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        requirements.add(new AutoPlacerHud.Requirement(() -> {
            float yaw = MathHelper.wrapDegrees(client.player.getYaw());
            return yaw > 80 && yaw < 110;
        }, Text.translatable("fzmm.gui.playerStatuePlacer.label.requirement.invalidYaw")));

        return new AutoPlacerHud.Activation(predicate, PlayerStatuePlacerScreen::new, requirements);
    }

    @Override
    protected List<Component> getInfoLabels() {
        List<Component> labelList = new ArrayList<>();

        labelList.add(Components.label(this.playerStatueStack.getName()));

        for (var text : DisplayBuilder.of(this.playerStatueStack).getLoreText()) {
            labelList.add(Components.label(text));
        }

        return labelList;
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
}
