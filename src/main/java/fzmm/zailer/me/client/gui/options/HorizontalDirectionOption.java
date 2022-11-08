package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

public enum HorizontalDirectionOption implements IMode {
    EAST("east"),
    SOUTH("south"),
    WEST("west"),
    NORTH("north");

    private final String name;

    HorizontalDirectionOption(String name) {
        this.name = name;
    }

    @Override
    public Text getTranslation() {
        return Text.translatable("fzmm.gui.option.direction." + this.name);
    }

    public static HorizontalDirectionOption getPlayerHorizontalDirection() {
        assert MinecraftClient.getInstance().player != null;
        Direction direction = MinecraftClient.getInstance().player.getHorizontalFacing();

        return switch (direction) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }
}