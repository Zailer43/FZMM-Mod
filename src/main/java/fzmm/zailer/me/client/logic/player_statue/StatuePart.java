package fzmm.zailer.me.client.logic.player_statue;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.logic.player_statue.statue_head_skin.AbstractStatueSkinManager;
import fzmm.zailer.me.client.logic.player_statue.statue_head_skin.HeadModelSkin;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.SkinPart;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.position.PosF;
import org.joml.Vector3f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class StatuePart {
    public static final String PLAYER_STATUE_TAG = "PlayerStatue";
    private static final String DEFAULT_SKIN_VALUE = "Error!";
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
    private boolean skinGenerated;
    private String skinValue;
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
        this.skinGenerated = false;
        this.skinValue = DEFAULT_SKIN_VALUE;
        this.headModelSkin = HeadModelSkin.of(this.part.getDefaultHeadModel(), headModelSkin);
        this.setDirection(HorizontalDirectionOption.NORTH);
        this.headSkin = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.skinManager = skinManager;
    }

    public StatuePart(StatuePartEnum part, String name, int headHeight, int zFightX, int zFightY, int zFightZ, HorizontalDirectionOption direction, String skinValue) {
        this.part = part;
        this.name = name;
        this.direction = direction;
        this.basePos = new PosF(0f, 0f);
        this.headHeight = headHeight;
        this.rotation = 0;
        this.zFightX = (short) zFightX;
        this.zFightY = (short) zFightY;
        this.zFightZ = (short) zFightZ;
        this.skinGenerated = true;
        this.skinValue = skinValue;
        this.headModelSkin = null;
        this.headSkin = null;

        this.setDirection(this.direction);
    }

    private CompoundTag writePlayerStatueTag() {
        CompoundTag playerStatueTag = new CompoundTag();
        CompoundTag zFight = new CompoundTag();
        zFight.putInt("x", this.zFightX);
        zFight.putInt("y", this.zFightY);
        zFight.putInt("z", this.zFightZ);

        playerStatueTag.putInt("headHeight", this.headHeight);
        playerStatueTag.putInt("direction", this.direction.ordinal());
        playerStatueTag.putString("part", this.part.toString());
        playerStatueTag.putString("name", this.name);
        playerStatueTag.putString("skinValue", this.skinValue);
        playerStatueTag.put("zFight", zFight);

        return playerStatueTag;
    }

    private CompoundTag writeFzmmTag() {
        CompoundTag playerStatueTag = this.writePlayerStatueTag();
        CompoundTag fzmmTag = new CompoundTag();
        fzmmTag.put(TagsConstant.FZMM_PLAYER_STATUE, playerStatueTag);
        return fzmmTag;
    }

    public static StatuePart ofItem(ItemStack stack) {
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        CompoundTag fzmmTag = customDataTag.getCompoundOrEmpty(TagsConstant.FZMM);

        CompoundTag playerStatueTag = fzmmTag.getCompoundOrEmpty(TagsConstant.FZMM_PLAYER_STATUE);
        CompoundTag zFight = playerStatueTag.getCompoundOrEmpty(PlayerStatueTags.Z_FIGHT);

        StatuePartEnum part = StatuePartEnum.get(playerStatueTag.getStringOr(PlayerStatueTags.PART, ""));
        String name = playerStatueTag.getStringOr(PlayerStatueTags.NAME, "");
        int headHeight = playerStatueTag.getIntOr(PlayerStatueTags.HEAD_HEIGHT, 1);
        HorizontalDirectionOption direction = HorizontalDirectionOption.values()[playerStatueTag.getIntOr(PlayerStatueTags.DIRECTION, 0)];
        String skinValue = playerStatueTag.getStringOr(PlayerStatueTags.SKIN_VALUE, "");
        int x = zFight.getIntOr("x", 0);
        int y = zFight.getIntOr("y", 0);
        int z = zFight.getIntOr("z", 0);

        return new StatuePart(part, name, headHeight, x, y, z, direction, skinValue);
    }

    public static boolean isStatue(ItemStack stack) {
        CompoundTag customDataTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag())).copyTag();
        CompoundTag fzmmTag = customDataTag.getCompoundOrEmpty(TagsConstant.FZMM);

        Optional<CompoundTag> playerStatueTagOptional = fzmmTag.getCompound(TagsConstant.FZMM_PLAYER_STATUE);
        if (playerStatueTagOptional.isEmpty()) return false;
        CompoundTag playerStatueTag = playerStatueTagOptional.get();

        if (playerStatueTag.getString(PlayerStatueTags.PART).isEmpty()) return false;
        if (playerStatueTag.getString(PlayerStatueTags.NAME).isEmpty()) return false;
        if (playerStatueTag.getInt(PlayerStatueTags.HEAD_HEIGHT).isEmpty()) return false;
        if (playerStatueTag.getInt(PlayerStatueTags.DIRECTION).isEmpty()) return false;

        int directionOrdinal = playerStatueTag.getIntOr(PlayerStatueTags.DIRECTION, -1);
        if (Direction.values().length < directionOrdinal || directionOrdinal < 0) return false;
        if (playerStatueTag.getString(PlayerStatueTags.SKIN_VALUE).isEmpty()) return false;

        Optional<CompoundTag> zFightOptional = playerStatueTag.getCompound(PlayerStatueTags.Z_FIGHT);
        if (zFightOptional.isEmpty()) return false;
        CompoundTag zFight = zFightOptional.get();

        return zFight.getInt("x").isPresent() && zFight.getInt("y").isPresent() && zFight.getInt("z").isPresent();
    }

    public String getName() {
        return this.name;
    }

    public ItemStack get(Vector3f pos, HorizontalDirectionOption direction) {
        if (!this.isSkinGenerated())
            return new ItemStack(Items.BARRIER);

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

        statuePart.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, nbtComponent -> {
            CompoundTag result = nbtComponent.copyTag();

            result.put(TagsConstant.FZMM, this.writeFzmmTag());

            return CustomData.of(result);
        });
        return statuePart;
    }

    /**
     * @return milliseconds left to generate another skin
     */
    public CompletableFuture<Integer> setStatueSkin(BufferedImage playerSkin, int scale) {
        this.draw(playerSkin, this.headSkin, scale);
        return new HeadUtils().uploadHead(this.headSkin, this.name)
                .thenApply(headUtils -> {
                    this.skinValue = headUtils.getSkinValue();
                    this.skinGenerated = headUtils.isSkinGenerated();

                    if (!this.skinGenerated) {
                        FzmmClient.LOGGER.error("[StatuePart] The statue {} had an error generating its skin", this.name);
                    }

                    return headUtils.getDelayForNext(TimeUnit.MILLISECONDS);
                });
    }
    public boolean isSkinGenerated() {
        return this.skinGenerated;
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
