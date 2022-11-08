package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.playerstatue.IPlayerStatueTab;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class PlayerStatueUpdateTab implements IPlayerStatueTab {
    @Override
    public String getId() {
        return "update";
    }

    @Override
    public Component[] getComponents(BaseFzmmScreen parent) {
        return new Component[0];
    }

    @Override
    public void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent) {
    }

    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        ItemStack statue = PlayerStatue.updateStatue(client.player.getMainHandStack(), new Vec3f(x, y, z), direction, name);
        FzmmUtils.giveItem(statue);
    }

    @Override
    public boolean canExecute() {
        return true;
    }
}
