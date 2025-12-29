package fzmm.zailer.me.client.logic.player_statue;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.logic.player_statue.statue_head_skin.AbstractStatueSkinManager;
import fzmm.zailer.me.client.logic.player_statue.statue_head_skin.HeadModelSkin;
import fzmm.zailer.me.utils.SkinPart;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.position.PosF;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class StatuePart {
    public static final String PLAYER_STATUE_TAG = "PlayerStatue";
    private static final float Z_FIGHT_FIX_DISTANCE = 0.00001f;
    private final HeadModelSkin headModelSkin;
    private final StatuePartEnum part;
    private final String name;
    private final HorizontalDirectionOption direction;
    private PosF basePos;
    private final int headHeight;
    private int rotation;
    private final short zFightX;
    private final short zFightY;
    private final short zFightZ;
    @Nullable
    private String skinValue = null;
    private final BufferedImage headSkin;
    private AbstractStatueSkinManager skinManager;

    public StatuePart(StatuePartEnum part, String name, int headHeight, HeadModelSkin headModelSkin, int zFightX, int zFightY, int zFightZ, AbstractStatueSkinManager skinManager) {
        this.part = part;
        this.name = name;
        this.direction = HorizontalDirectionOption.NORTH;
        this.basePos = new PosF(0f, 0f);
        this.headHeight = headHeight;
        this.rotation = 0;
        this.zFightX = (short) zFightX;
        this.zFightY = (short) zFightY;
        this.zFightZ = (short) zFightZ;
        this.headModelSkin = HeadModelSkin.of(this.part.getDefaultHeadModel(), headModelSkin);
        this.setDirection(HorizontalDirectionOption.NORTH);
        this.headSkin = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.skinManager = skinManager;
    }

    public StatuePart(StatuePartEnum part, String name, int headHeight, int zFightX, int zFightY, int zFightZ, HorizontalDirectionOption direction, @Nullable String skinValue) {
        this.part = part;
        this.name = name;
        this.direction = direction;
        this.basePos = new PosF(0f, 0f);
        this.headHeight = headHeight;
        this.rotation = 0;
        this.zFightX = (short) zFightX;
        this.zFightY = (short) zFightY;
        this.zFightZ = (short) zFightZ;
        this.skinValue = skinValue;
        this.headModelSkin = null;
        this.headSkin = null;

        this.setDirection(this.direction);
    }

    private NbtCompound writePlayerStatueTag() {
        NbtCompound playerStatueTag = new NbtCompound();
        NbtCompound zFight = new NbtCompound();
        zFight.putInt("x", this.zFightX);
        zFight.putInt("y", this.zFightY);
        zFight.putInt("z", this.zFightZ);

        playerStatueTag.putInt("headHeight", this.headHeight);
        playerStatueTag.putInt("direction", this.direction.ordinal());
        playerStatueTag.putString("part", this.part.toString());
        playerStatueTag.putString("name", this.name);
        assert this.skinValue != null;
        playerStatueTag.putString("skinValue", this.skinValue);
        playerStatueTag.put("zFight", zFight);

        return playerStatueTag;
    }

    private NbtCompound writeFzmmTag() {
        NbtCompound playerStatueTag = this.writePlayerStatueTag();
        NbtCompound fzmmTag = new NbtCompound();
        fzmmTag.put(TagsConstant.FZMM_PLAYER_STATUE, playerStatueTag);
        return fzmmTag;
    }

    public static StatuePart ofItem(ItemStack stack) {
        NbtCompound customDataTag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        NbtCompound fzmmTag = customDataTag.getCompoundOrEmpty(TagsConstant.FZMM);

        NbtCompound playerStatueTag = fzmmTag.getCompoundOrEmpty(TagsConstant.FZMM_PLAYER_STATUE);
        NbtCompound zFight = playerStatueTag.getCompoundOrEmpty(PlayerStatueTags.Z_FIGHT);

        StatuePartEnum part = StatuePartEnum.get(playerStatueTag.getString(PlayerStatueTags.PART, ""));
        String name = playerStatueTag.getString(PlayerStatueTags.NAME, "");
        int headHeight = playerStatueTag.getInt(PlayerStatueTags.HEAD_HEIGHT, 1);
        HorizontalDirectionOption direction = HorizontalDirectionOption.values()[playerStatueTag.getInt(PlayerStatueTags.DIRECTION, 0)];
        String skinValue = playerStatueTag.getString(PlayerStatueTags.SKIN_VALUE, "");
        int x = zFight.getInt("x", 0);
        int y = zFight.getInt("y", 0);
        int z = zFight.getInt("z", 0);

        return new StatuePart(part, name, headHeight, x, y, z, direction, skinValue);
    }

    public static boolean isStatue(ItemStack stack) {
        NbtCompound customDataTag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound())).copyNbt();
        NbtCompound fzmmTag = customDataTag.getCompoundOrEmpty(TagsConstant.FZMM);

        Optional<NbtCompound> playerStatueTagOptional = fzmmTag.getCompound(TagsConstant.FZMM_PLAYER_STATUE);
        if (playerStatueTagOptional.isEmpty()) return false;
        NbtCompound playerStatueTag = playerStatueTagOptional.get();

        if (playerStatueTag.getString(PlayerStatueTags.PART).isEmpty()) return false;
        if (playerStatueTag.getString(PlayerStatueTags.NAME).isEmpty()) return false;
        if (playerStatueTag.getInt(PlayerStatueTags.HEAD_HEIGHT).isEmpty()) return false;
        if (playerStatueTag.getInt(PlayerStatueTags.DIRECTION).isEmpty()) return false;

        int directionOrdinal = playerStatueTag.getInt(PlayerStatueTags.DIRECTION, -1);
        if (Direction.values().length < directionOrdinal || directionOrdinal < 0) return false;
        if (playerStatueTag.getString(PlayerStatueTags.SKIN_VALUE).isEmpty()) return false;

        Optional<NbtCompound> zFightOptional = playerStatueTag.getCompound(PlayerStatueTags.Z_FIGHT);
        if (zFightOptional.isEmpty()) return false;
        NbtCompound zFight = zFightOptional.get();

        return zFight.getInt("x").isPresent() && zFight.getInt("y").isPresent() && zFight.getInt("z").isPresent();
    }

    public String getName() {
        return this.name;
    }

    public ItemStack get(Vector3f pos, HorizontalDirectionOption direction) {
        if (this.skinValue == null) return new ItemStack(Items.BARRIER);

        this.setDirection(direction);
        this.fixZFight(pos);
        float x = pos.x() + this.basePos.getX();
        float y = pos.y() + this.headHeight * 0.25f - 0.9f;
        float z = pos.z() + this.basePos.getY();

        ItemStack statuePart = ArmorStandBuilder.builder()
                .setPos(x, y, z)
                .setImmutableAndInvisible()
                .setRightArmPose(new Vector3f(-45f, this.rotation, 0f))
                .setRightHandItem(HeadBuilder.builder().skinValue(this.skinValue).get())
                .setTags(PLAYER_STATUE_TAG)
                .getItem(this.name);

        statuePart.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbtComponent -> {
            NbtCompound result = nbtComponent.copyNbt();

            result.put(TagsConstant.FZMM, this.writeFzmmTag());

            return NbtComponent.of(result);
        });
        return statuePart;
    }

    public void value(String skinValue) {
        this.skinValue = skinValue;
    }

    public BufferedImage drawAndGet(BufferedImage playerSkin, int scale) {
        this.draw(playerSkin, this.headSkin, scale);
        return this.headSkin;
    }

    public boolean isEquals(BufferedImage skin) {
        return this.headSkin.equals(skin);
    }

    private void draw(BufferedImage playerSkin, BufferedImage destinationSkin, int scale) {
        Graphics2D graphics = destinationSkin.createGraphics();
        this.headModelSkin.draw(this.skinManager, graphics, playerSkin, scale);
    }

    private void fixZFight(Vector3f pos) {
        pos.add(zFightX * Z_FIGHT_FIX_DISTANCE, zFightY * Z_FIGHT_FIX_DISTANCE, zFightZ * Z_FIGHT_FIX_DISTANCE);
    }

    private void setDirection(HorizontalDirectionOption direction) {
        PosF newPos = switch (direction) {
            case EAST -> {
                this.basePos = new PosF(0.93f, 0.7f);
                this.rotation = -135;
                yield this.part.getEast();
            }
            case SOUTH -> {
                this.basePos = new PosF(-0.01f, 0.6f);
                this.rotation = -45;
                yield this.part.getSouth();
            }
            case WEST -> {
                this.basePos = new PosF(0.08f, -0.33f);
                this.rotation = 45;
                yield this.part.getWest();
            }
            default -> {
                this.basePos = new PosF(1.01f, -0.25f);
                this.rotation = 135;
                yield this.part.getNorth();
            }
        };
        this.basePos.add(newPos);
    }

    /**
     * Statue part:
     * {
     *  name: string,
     *  part: string,
     *  headHeight: int,
     *  zFight: {
     *   x: int,
     *   y: int,
     *   z: int
     *  },
     *  direction: int,
     *  skinValue: string
     * }
     * Statue name tag:
     * {
     *  nameTag: 1b
     * }
     */
    public static class PlayerStatueTags {

        public static final String NAME = "name";
        public static final String PART = "part";
        public static final String HEAD_HEIGHT = "headHeight";
        public static final String Z_FIGHT = "zFight";
        public static final String DIRECTION = "direction";
        public static final String SKIN_VALUE = "skinValue";
        public static final String NAME_TAG = "nameTag";
    }

}
