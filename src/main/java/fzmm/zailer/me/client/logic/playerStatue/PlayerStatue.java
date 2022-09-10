package fzmm.zailer.me.client.logic.playerStatue;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import fzmm.zailer.me.client.gui.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.enums.options.DirectionOption;
import fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin.*;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class PlayerStatue {
    public static final Logger LOGGER = LogManager.getLogger("FZMM PlayerStatue");
    private final List<StatuePart> statueList;
    private final String name;
    private BufferedImage playerSkin;
    private final Vec3f pos;
    private final DirectionOption direction;
    public static short progress = 0;
    public static int partsLeft = 0;

    public PlayerStatue(BufferedImage playerSkin, String name, Vec3f pos, DirectionOption direction) {
        this.playerSkin = playerSkin;
        this.name = name;
        progress = 0;
        this.statueList = new ArrayList<>();
        this.pos = pos;
        this.direction = direction;
    }

    public PlayerStatue generateStatues() {
        this.statueList.clear();

        if (Configs.Generic.CONVERT_SKINS_WITH_ALEX_MODEL_IN_STEVE_MODEL_IN_PLAYER_STATUE.getBooleanValue() && this.isAlexModel())
            this.convertInSteveModel();

        if (MinecraftClient.getInstance().currentScreen instanceof PlayerStatueScreen playerStatueScreen)
            playerStatueScreen.reload();
        partsLeft = 27;
        InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "fzmm.gui.playerStatue.status.start");

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

        int delay = 0;
        for (StatuePart statuePart : this.statueList) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(delay));
            delay = statuePart.setStatueSkin(this.playerSkin, this.getSkinScale());
            this.notifyStatus(statuePart, delay);
        }

        this.fixGeneratingError();

        InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, "fzmm.gui.playerStatue.status.end");
        return this;
    }

    public void fixGeneratingError() {
        for (StatuePart statuePart : this.statueList) {
            if (!statuePart.isSkinGenerated()) {
                int delay = statuePart.setStatueSkin(this.playerSkin, this.getSkinScale());
                this.notifyStatus(statuePart, delay);
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
                name = Text.Serializer.toJson(Text.of(name));
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
                .setName(Text.literal("Player Statue").setStyle(Style.EMPTY.withColor(color).withBold(true)))
                .addLore("Pos: " + x + " " + y + " " + z, color);

        InventoryUtils invUtils = new InventoryUtils(displayUtils.get())
                .addItem(statueList)
                .setNameStyleToItems(Style.EMPTY.withColor(color))
                .addLoreToItems(Items.ARMOR_STAND, Text.translatable("fzmm.playerStatue.item.lore.1").getString(), color)
                .addLoreToItems(Items.ARMOR_STAND, Text.translatable("fzmm.playerStatue.item.lore.2").getString(), color);

        return invUtils.get();
    }

    public void notifyStatus(StatuePart part, int delayInSeconds) {
        Message.MessageType status = part.isSkinGenerated() ? Message.MessageType.SUCCESS : Message.MessageType.ERROR;
        String translation = part.isSkinGenerated() ? "fzmm.gui.playerStatue.status.generated" : "fzmm.gui.playerStatue.status.error";
        InfoUtils.showGuiOrInGameMessage(status, translation, part.getName(), delayInSeconds, partsLeft);
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

    public boolean isAlexModel() {
        int scale = this.getSkinScale();
        int color = this.playerSkin.getRGB((SkinPart.LEFT_ARM.x() + 15) * scale, (SkinPart.LEFT_ARM.y() + 15) * scale);
        int alpha = new Color(color, true).getAlpha();
        return alpha == 0;
    }

    public void convertInSteveModel() {
        int scale = this.getSkinScale();
        this.convertInSteveModel(SkinPart.LEFT_ARM, scale);
        this.convertInSteveModel(SkinPart.RIGHT_ARM, scale);
    }

    private void convertInSteveModel(SkinPart skinPart, int scale) {
        this.convertInSteveModel(skinPart.x(), skinPart.y(), scale);
        this.convertInSteveModel(skinPart.hatX(), skinPart.hatY(), scale);
    }

    private void convertInSteveModel(int x, int y, int scale) {
        x *= scale;
        y *= scale;
        int imageSize = 64 * scale;
        int space = 4 * scale;
        int steveArmWidth = 4 * scale;
        int alexArmWidth = 3 * scale;
        int skinPartSize = 16 * scale;
        BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        // copy skin
        g2d.drawImage(this.playerSkin, 0, 0, imageSize, imageSize, 0, 0, imageSize, imageSize, null);
        // clear skin part
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(x, y, skinPartSize, skinPartSize);
        // copy side 1
        g2d.drawImage(this.playerSkin, x, y + space, x + steveArmWidth, y + skinPartSize, x, y + space, x + steveArmWidth, y + skinPartSize, null);
        // stretching face 2
        g2d.drawImage(this.playerSkin, x + steveArmWidth, y + space, x + steveArmWidth * 2, y + skinPartSize, x + steveArmWidth, y + space, x + steveArmWidth + alexArmWidth, y + skinPartSize, null);
        // moving face 3
        g2d.drawImage(this.playerSkin, x + steveArmWidth * 2, y + space, x + steveArmWidth * 3, y + skinPartSize, x + steveArmWidth + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth, y + skinPartSize, null);
        // stretching and moving face 4
        g2d.drawImage(this.playerSkin, x + steveArmWidth * 3, y + space, x + steveArmWidth * 4, y + skinPartSize, x + steveArmWidth * 2 + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth * 2, y + skinPartSize, null);
        // stretching top/down face 1
        g2d.drawImage(this.playerSkin, x + space, y, x + steveArmWidth, y + space, x + space, y, x + alexArmWidth, y + space, null);
        // stretching and moving top/down face 2
        g2d.drawImage(this.playerSkin, x + space + steveArmWidth, y, x + steveArmWidth * 2, y + space, x + space + alexArmWidth, y, x + alexArmWidth * 2, y + space, null);

        g2d.dispose();
        this.playerSkin = bufferedImage;
    }

    private int getSkinScale() {
        return this.playerSkin.getHeight() == 128 && this.playerSkin.getWidth() == 128 ? 2 : 1;
    }
}
