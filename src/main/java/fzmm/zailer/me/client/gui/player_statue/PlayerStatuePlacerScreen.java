package fzmm.zailer.me.client.gui.player_statue;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.utils.auto_placer.AbstractAutoPlacer;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.TagsConstant;
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
        this.containerItems = InventoryUtils.getItemsFromContainer(this.playerStatueStack);
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

        labelList.add(EComponents.label(this.playerStatueStack.getName()));

        for (var text : DisplayBuilder.of(this.playerStatueStack).getLoreText()) {
            labelList.add(EComponents.label(text));
        }

        return labelList;
    }

    @Override
    protected ItemStack processStack(ItemStack stack) {
        // armor stand does not need a custom name
        stack.removeSubNbt(ItemStack.DISPLAY_KEY);

        // since 1.21.5 custom data are transferred to the entity (this tag is in custom data in 1.20.5+)
        stack.removeSubNbt(TagsConstant.FZMM);

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
}
