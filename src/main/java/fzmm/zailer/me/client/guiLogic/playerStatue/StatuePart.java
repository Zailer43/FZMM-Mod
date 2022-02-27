package fzmm.zailer.me.client.guiLogic.playerStatue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.gui.enums.options.DirectionOption;
import fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin.AbstractStatueSkinManager;
import fzmm.zailer.me.client.guiLogic.playerStatue.statueHeadSkin.HeadModelSkin;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.ArmorStandUtils;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.position.PosF;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

public class StatuePart {
    private static final String DEFAULT_SKIN_VALUE = "Error!";
    private static final float Z_FIGHT_FIX_DISTANCE = 0.00001f;
    private final HeadModelSkin headModelSkin;
    private final StatuePartEnum part;
    private final String name;
    private final DirectionOption direction;
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
        this.direction = DirectionOption.NORTH;
        this.basePos = new PosF(0f, 0f);
        this.headHeight = headHeight;
        this.rotation = 0;
        this.zFightX = (short) zFightX;
        this.zFightY = (short) zFightY;
        this.zFightZ = (short) zFightZ;
        this.skinGenerated = false;
        this.skinValue = DEFAULT_SKIN_VALUE;
        this.headModelSkin = HeadModelSkin.of(this.part.getDefaultHeadModel(), headModelSkin);
        this.setDirection(DirectionOption.NORTH);
        this.headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.skinManager = skinManager;
    }

    public StatuePart(StatuePartEnum part, String name, int headHeight, int zFightX, int zFightY, int zFightZ, DirectionOption direction, String skinValue) {
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
        NbtCompound fzmmTag = stack.getOrCreateSubNbt(TagsConstant.FZMM);
        NbtCompound playerStatueTag = fzmmTag.getCompound(TagsConstant.FZMM_PLAYER_STATUE);
        NbtCompound zFight = playerStatueTag.getCompound(PlayerStatueTags.Z_FIGHT);

        StatuePartEnum part = StatuePartEnum.get(playerStatueTag.getString(PlayerStatueTags.PART));
        String name = playerStatueTag.getString(PlayerStatueTags.NAME);
        int headHeight = playerStatueTag.getInt(PlayerStatueTags.HEAD_HEIGHT);
        DirectionOption direction = DirectionOption.values()[playerStatueTag.getInt(PlayerStatueTags.DIRECTION)];
        String skinValue = playerStatueTag.getString(PlayerStatueTags.SKIN_VALUE);
        int x = zFight.getInt("x");
        int y = zFight.getInt("y");
        int z = zFight.getInt("z");

        return new StatuePart(part, name, headHeight, x, y, z, direction, skinValue);
    }

    public static boolean isStatue(ItemStack stack) {
        if (!stack.hasNbt())
            return false;
        NbtCompound fzmmTag = stack.getOrCreateSubNbt(TagsConstant.FZMM);

        if (!fzmmTag.contains(TagsConstant.FZMM_PLAYER_STATUE, NbtElement.COMPOUND_TYPE))
            return false;
        NbtCompound playerStatueTag = fzmmTag.getCompound(TagsConstant.FZMM_PLAYER_STATUE);

        if (!playerStatueTag.contains(PlayerStatueTags.PART, NbtElement.STRING_TYPE))
            return false;

        if (!playerStatueTag.contains(PlayerStatueTags.NAME, NbtElement.STRING_TYPE))
            return false;

        if (!playerStatueTag.contains(PlayerStatueTags.HEAD_HEIGHT, NbtElement.INT_TYPE))
            return false;

        if (!playerStatueTag.contains(PlayerStatueTags.DIRECTION, NbtElement.INT_TYPE))
            return false;
        int directionOrdinal = playerStatueTag.getInt(PlayerStatueTags.DIRECTION);

        if (Direction.values().length < directionOrdinal)
            return false;

        if (!playerStatueTag.contains(PlayerStatueTags.SKIN_VALUE, NbtElement.STRING_TYPE))
            return false;

        if (!playerStatueTag.contains(PlayerStatueTags.Z_FIGHT, NbtElement.COMPOUND_TYPE))
            return false;

        NbtCompound zFight = playerStatueTag.getCompound(PlayerStatueTags.Z_FIGHT);

        return zFight.contains("x", NbtElement.INT_TYPE) && zFight.contains("y", NbtElement.INT_TYPE) && zFight.contains("z", NbtElement.INT_TYPE);
    }

    public ItemStack get(Vec3f pos, DirectionOption direction) {
        if (!this.isSkinGenerated())
            return new ItemStack(Items.BARRIER);

        this.setDirection(direction);
        this.fixZFight(pos);
        float x = pos.getX() + this.basePos.getX();
        float y = pos.getY() + this.headHeight * 0.25f - 0.9f;
        float z = pos.getZ() + this.basePos.getY();

        ItemStack statuePart = new ArmorStandUtils().setPos(x, y, z).setImmutableAndInvisible().setRightArmPose(new Vec3f(-45f, this.rotation, 0f))
                .setRightHandItem(FzmmUtils.playerHeadFromSkin(this.skinValue)).getItem(this.name);

        statuePart.setSubNbt(TagsConstant.FZMM, this.writeFzmmTag());
        return statuePart;
    }

    public void setStatueSkin(BufferedImage playerSkin) {
        try {
            this.draw(playerSkin, this.headSkin);

            this.apiRequest();
        } catch (Exception e) {
            PlayerStatue.LOGGER.error("The statue " + this.name + " had an error generating its skin");
            e.printStackTrace();
            this.errorGeneratingSkin();
        }
    }

    public boolean isSkinGenerated() {
        return this.skinGenerated;
    }

    private void draw(BufferedImage playerSkin, BufferedImage destinationSkin) {
        Graphics2D graphics = destinationSkin.createGraphics();
        this.headModelSkin.draw(this.skinManager, graphics, playerSkin);

    }

    private void fixZFight(Vec3f pos) {
        pos.add(zFightX * Z_FIGHT_FIX_DISTANCE, zFightY * Z_FIGHT_FIX_DISTANCE, zFightZ * Z_FIGHT_FIX_DISTANCE);
    }

    private void setDirection(DirectionOption direction) {
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

    private void apiRequest() throws IOException {
        String apiKey = Configs.Generic.MINESKIN_API_KEY.getStringValue();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(this.headSkin, "png", baos);
        byte[] skin = baos.toByteArray();

        try (CloseableHttpClient httpclient = HttpClients.custom().setUserAgent("FZMMPlayerStatue/1.0").build()) {
            HttpPost httppost = new HttpPost(PlayerStatue.MINESKIN_API + "generate/upload");

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("key", new StringBody(apiKey, ContentType.TEXT_PLAIN))
                    .addPart("visibility", new StringBody("1", ContentType.TEXT_PLAIN))
                    .addBinaryBody("file", skin, ContentType.APPLICATION_FORM_URLENCODED, "player_statue")
                    .build();

            httppost.setEntity(reqEntity);

            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                HttpEntity resEntity = response.getEntity();
                int httpCode = response.getStatusLine().getStatusCode();
                if (httpCode == HttpURLConnection.HTTP_OK) {
                    this.useResponse(EntityUtils.toString(resEntity));
                } else {
                    PlayerStatue.LOGGER.warn("HTTP error " + httpCode + " generating skin in '" + this.name + "'");
                    PlayerStatue.nextDelayMillis = 5000;
                    this.errorGeneratingSkin();
                }
                EntityUtils.consume(resEntity);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void useResponse(String reply) throws IOException, InterruptedException {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        JsonObject json = (JsonObject) JsonParser.parseString(reply);
        this.skinValue = json.getAsJsonObject("data").getAsJsonObject("texture").get("value").getAsString();
        this.skinGenerated = true;

        PlayerStatue.progress++;
        PlayerStatue.nextDelayMillis = (short) this.getDelay(json.getAsJsonObject("delayInfo").get("millis").getAsInt());
        PlayerStatue.updateStatus();

        Thread.sleep(PlayerStatue.nextDelayMillis);
    }

    private int getDelay(int delay) {
        return MathHelper.clamp(delay, 1000, 5000);
    }

    private void errorGeneratingSkin() {
        this.skinGenerated = false;
        PlayerStatue.errors++;
        PlayerStatue.updateStatus();
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
     *
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
