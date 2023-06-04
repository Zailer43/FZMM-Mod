package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.client.gui.utils.selectItem.SelectItemScreen;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.client.toast.UpdatedPlayerStatueToast;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PlayerStatueUpdateTab implements IPlayerStatueTab {
    @Override
    public String getId() {
        return "update";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
    }

    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        RequestedItem requestedItem = new RequestedItem(
                PlayerStatue::containsStatuePart,
                stack -> {
                    if (PlayerStatue.containsStatuePart(stack)) {
                        ItemStack statue = PlayerStatue.updateStatue(stack, new Vector3f(x, y, z), direction, name);
                        FzmmUtils.giveItem(statue);

                        UpdatedPlayerStatueToast toast = new UpdatedPlayerStatueToast();
                        client.getToastManager().add(toast);
                    }
                },
                new ArrayList<>(),
                Text.translatable("fzmm.gui.playerStatue.option.select.title"),
                true
        );

        client.setScreen(new SelectItemScreen(client.currentScreen, requestedItem));
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public IMementoObject createMemento() {
        return null;
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {

    }
}
