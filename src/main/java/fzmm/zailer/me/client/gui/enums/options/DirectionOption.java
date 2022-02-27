package fzmm.zailer.me.client.gui.enums.options;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public enum DirectionOption implements IConfigOptionListEntry {
    NORTH("north"),
    EAST("east"),
    SOUTH("south"),
    WEST("west");

    private final String name;

    DirectionOption(String name) {
        this.name = name;
    }

    @Override
    public String getStringValue() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return new TranslatableText("fzmm.gui.option.direction." + this.name).getString();
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward) {
        int ordinal = this.ordinal();
        int valuesLength = values().length;

        ordinal = MathHelper.clamp(forward ? ++ordinal : --ordinal, 0, valuesLength);

        return values()[ordinal % valuesLength];
    }

    @Override
    public IConfigOptionListEntry fromString(String value) {
        for (DirectionOption option : DirectionOption.values()) {
            if (option.getStringValue().equalsIgnoreCase(value)) {
                return option;
            }
        }

        return NORTH;
    }

    public static DirectionOption getPlayerDirection() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        Direction direction = client.player.getHorizontalFacing();
        return switch (direction) {
            case EAST -> DirectionOption.EAST;
            case SOUTH -> DirectionOption.SOUTH;
            case WEST -> DirectionOption.WEST;
            default -> DirectionOption.NORTH;
        };
    }
}