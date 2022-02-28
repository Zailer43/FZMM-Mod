package fzmm.zailer.me.client.guiLogic.playerStatue;

import fzmm.zailer.me.client.gui.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.enums.options.DirectionOption;
import fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin.*;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatue {
    public static final Logger LOGGER = LogManager.getLogger("FZMM PlayerStatue");
    protected static final String MINESKIN_API = "https://api.mineskin.org/";
    private final List<StatuePart> statueList;
    private final String name;
    private final BufferedImage playerSkin;
    private final Vec3f pos;
    private final DirectionOption direction;
    public static short progress = 0;
    public static short errors = 0;
    public static short nextDelayMillis = 0;

    public PlayerStatue(BufferedImage playerSkin, String name, Vec3f pos, DirectionOption direction) {
        this.playerSkin = playerSkin;
        this.name = name;
        progress = 0;
        errors = 0;
        nextDelayMillis = 0;
        this.statueList = new ArrayList<>();
        this.pos = pos;
        this.direction = direction;
    }

    public PlayerStatue generateStatues() {
        this.statueList.clear();

        PlayerStatueScreen.status.setHide(false);
        if (MinecraftClient.getInstance().currentScreen instanceof PlayerStatueScreen playerStatueScreen)
            playerStatueScreen.reload();
        updateStatus();

        HeadModelSkin empty = new HeadModelSkin();
        HeadModelSkin bottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE);
        HeadModelSkin top = new HeadModelSkin(HeadFace.HEAD_FACE.UP_FACE);
        HeadModelSkin left = new HeadModelSkin(HeadFace.HEAD_FACE.LEFT_FACE);
        HeadModelSkin right = new HeadModelSkin(HeadFace.HEAD_FACE.RIGHT_FACE);
        HeadModelSkin leftBottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE, HeadFace.HEAD_FACE.LEFT_FACE);
        HeadModelSkin rightBottom = new HeadModelSkin(HeadFace.HEAD_FACE.BOTTOM_FACE, HeadFace.HEAD_FACE.RIGHT_FACE);

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Left bottom leg", 0, leftBottom, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Left middle leg", 1, left, -2, 0, 1, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_LEG, "Left top leg", 2, left, 2, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_LEG, AbstractStatueSkinManager.Height.UPPER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Left bottom body", 3, empty, -1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.LOWER, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Left middle body", 4, empty, 1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.MIDDLE, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_BODY, "Left top body", 5, top, -1, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.UPPER, true)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Right bottom leg", 0, rightBottom, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Right middle leg", 1, right, 1, 0, -1, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_LEG, "Right top leg", 2, right, -2, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_LEG, AbstractStatueSkinManager.Height.UPPER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Right bottom body", 3, empty, 0, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.LOWER, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Right middle body", 4, empty, -2, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.MIDDLE, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_BODY, "Right top body", 5, top, 0, 0, 0, new BodySkinManager(AbstractStatueSkinManager.Height.UPPER, false)));

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Left bottom arm", 3, bottom, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Left middle arm", 4, empty, -2, 0, 1, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_ARM, "Left top arm", 5, top, 0, 0, 0, new ExtremitySkinManager(SkinPart.LEFT_ARM, AbstractStatueSkinManager.Height.UPPER)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Right bottom arm", 3, bottom, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.LOWER)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Right middle arm", 4, empty, 1, 0, -1, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.MIDDLE)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_ARM, "Right top arm", 5, top, -1, 0, 0, new ExtremitySkinManager(SkinPart.RIGHT_ARM, AbstractStatueSkinManager.Height.UPPER)));

        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_FRONT, "Left bottom front head", 6, bottom, 1, 0, 0, new HeadSkinManager(false, true, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_FRONT, "Left top front head", 7, top, 2, 0, 2, new HeadSkinManager(false, false, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_BACK, "Left bottom back head", 6, bottom, 0, 0, 1, new HeadSkinManager(false, true, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.LEFT_HEAD_BACK, "Left top back head", 7, top, -3, 0, -3, new HeadSkinManager(false, false, true)));

        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_FRONT, "Right bottom front head", 6, bottom, -1, 0, 0, new HeadSkinManager(true, true, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_FRONT, "Right top front head", 7, top, 1, 0, -1, new HeadSkinManager(true, false, false)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_BACK, "Right bottom back head", 6, bottom, 1, 0, -2, new HeadSkinManager(true, true, true)));
        this.statueList.add(new StatuePart(StatuePartEnum.RIGHT_HEAD_BACK, "Right top back head", 7, top, -2, 0, 1, new HeadSkinManager(true, false, true)));

        for (StatuePart statuePart : this.statueList)
            statuePart.setStatueSkin(this.playerSkin);

        this.fixGeneratingError();
        this.fixGeneratingError();

        PlayerStatueScreen.status.setHide(true);
        return this;
    }

    public void fixGeneratingError() {
        for (StatuePart statuePart : this.statueList) {
            if (!statuePart.isSkinGenerated()) {
                statuePart.setStatueSkin(this.playerSkin);
                if (statuePart.isSkinGenerated()) {
                    errors--;
                    updateStatus();
                }
            }
        }
    }

    public static ItemStack getStatueName(Vec3f pos, String name) {
        float x = pos.getX() + 0.5f;
        float y = pos.getY() - 0.1f;
        float z = pos.getZ() + 0.5f;

        if (name != null && !name.isEmpty()) {
            try {
                Text.Serializer.fromJson(name);
            } catch (Exception e) {
                if (name.length() > 100)
                    name = name.substring(0, 99);
                name = Text.Serializer.toJson(new LiteralText(name));
            }
        }

        ItemStack nameTagStack = new ArmorStandUtils().setPos(x, y, z).setAsHologram(name).getItem("Name tag");
        NbtCompound fzmmTag = new NbtCompound();
        NbtCompound playerStatueTag = new NbtCompound();

        playerStatueTag.putByte(StatuePart.PlayerStatueTags.NAME_TAG, (byte) 1);
        fzmmTag.put(TagsConstant.FZMM_PLAYER_STATUE, playerStatueTag);
        nameTagStack.setSubNbt(TagsConstant.FZMM, fzmmTag);

        return nameTagStack;
    }

    public static boolean isNameTag(ItemStack stack) {
        if (!stack.hasNbt())
            return false;
        NbtCompound fzmmTag = stack.getOrCreateSubNbt(TagsConstant.FZMM);

        if (!fzmmTag.contains(TagsConstant.FZMM_PLAYER_STATUE, NbtElement.COMPOUND_TYPE))
            return false;
        NbtCompound playerStatueTag = fzmmTag.getCompound(TagsConstant.FZMM_PLAYER_STATUE);

        return playerStatueTag.contains(StatuePart.PlayerStatueTags.NAME_TAG, NbtElement.BYTE_TYPE);
    }

    public List<ItemStack> getStatueItems() {
        List<ItemStack> stackList = new ArrayList<>();

        for (StatuePart statue : this.statueList)
            stackList.add(statue.get(this.pos, this.direction));

        if (!this.name.isEmpty())
            stackList.add(getStatueName(this.pos, this.name));

        return stackList;
    }

    public ItemStack getStatueInContainer() {
        return getStatueInContainer(this.getStatueItems(), this.pos);
    }

    public static ItemStack getStatueInContainer(List<ItemStack> statueList, Vec3f pos) {
        int x = (int) pos.getX();
        int y = (int) pos.getY();
        int z = (int) pos.getZ();
        int color = FzmmUtils.RGBAtoRGB(Configs.Colors.PLAYER_STATUE.getColor()).intValue;

        DisplayUtils displayUtils = new DisplayUtils(Configs.getConfigItem(Configs.Generic.PLAYER_STATUE_DEFAULT_CONTAINER))
                .setName(new LiteralText("Player Statue").setStyle(Style.EMPTY.withColor(color).withBold(true)))
                .addLore("Pos: " + x + " " + y + " " + z, color);

        InventoryUtils invUtils = new InventoryUtils(displayUtils.get())
                .addItem(statueList)
                .setNameStyleToItems(Style.EMPTY.withColor(color))
                .addLoreToItems(Items.ARMOR_STAND,"Player statue part, put in a dispenser that " , color)
                .addLoreToItems(Items.ARMOR_STAND,"is " + Formatting.UNDERLINE + "FACING UP" + Formatting.RESET + " and activate the dispenser", color);

        return invUtils.get();
    }

    protected static void updateStatus() {
        PlayerStatueScreen.status.setTranslationValues(String.valueOf(progress), String.valueOf(errors), String.valueOf(nextDelayMillis / 1000f));
    }

    public static ItemStack updateStatue(ItemStack container, Vec3f pos, DirectionOption direction, String name) {
        List<ItemStack> containerItems = InventoryUtils.getItemsFromContainer(container);
        List<ItemStack> statueList = new ArrayList<>();

        for (ItemStack stack : containerItems) {
            if (StatuePart.isStatue(stack))
                statueList.add(StatuePart.ofItem(stack).get(pos, direction));
            else if (isNameTag(stack))
                statueList.add(getStatueName(pos, name));
        }

        return getStatueInContainer(statueList, pos);
    }
}
