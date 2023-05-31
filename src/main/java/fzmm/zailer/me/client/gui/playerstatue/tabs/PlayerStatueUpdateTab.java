package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;

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

        ItemStack statue = PlayerStatue.updateStatue(client.player.getMainHandStack(), new Vector3f(x, y, z), direction, name);
        FzmmUtils.giveItem(statue);
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
