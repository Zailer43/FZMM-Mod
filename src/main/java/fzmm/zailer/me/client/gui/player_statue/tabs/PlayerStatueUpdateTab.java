package fzmm.zailer.me.client.gui.player_statue.tabs;

import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import fzmm.zailer.me.client.gui.utils.select_item.SelectItemScreen;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.client.logic.player_statue.StatuePart;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PlayerStatueUpdateTab implements IPlayerStatueTab {
    @Override
    public String getId() {
        return "update";
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
    }

    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        RequestedItem requestedItem = new RequestedItem(
                PlayerStatue::isPlayerStatue,
                stack -> {
                    if (PlayerStatue.isPlayerStatue(stack)) {
                        ItemStack statue = PlayerStatue.updateStatue(stack, new Vector3f(x, y, z), direction, name);
                        ItemUtils.give(statue);

                        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.PLAYER_STATUE_ID)
                                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                                .title(Component.translatable("fzmm.snack_bar.playerStatue.updated.title"))
                                .lowTimer()
                                .startTimer()
                                .build()
                        );
                        InvisibleEntityWarning.add(true, true, Component.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.playerStatue"), StatuePart.PLAYER_STATUE_TAG);
                    }
                },
                new ArrayList<>(),
                Component.translatable("fzmm.gui.playerStatue.option.select.title"),
                true
        );

        FzmmUtils.setScreen(new SelectItemScreen(client.gui.screen(), requestedItem));
    }

    @Override
    public boolean canExecute() {
        return true;
    }
}
