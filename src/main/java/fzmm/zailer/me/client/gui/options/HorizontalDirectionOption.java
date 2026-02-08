package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.client.gui.components.IMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;

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
    public String getTranslationKey() {
        return "fzmm.gui.option.direction." + this.name;
    }

    public static HorizontalDirectionOption getPlayerHorizontalDirection() {
        assert Minecraft.getInstance().player != null;
        Direction direction = Minecraft.getInstance().player.getDirection();

        return switch (direction) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }
}