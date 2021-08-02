package fzmm.zailer.me.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class StatueLogic {

    public static final float Y_DIFFERENCE = 0.25f;
    private static final HeadFace RIGHT_FACE = new HeadFace((byte) 0, (byte) 8, (byte) 8, (byte) 16),
            FRONT_FACE = new HeadFace((byte) 8, (byte) 8, (byte) 16, (byte) 16),
            LEFT_FACE = new HeadFace((byte) 16, (byte) 8, (byte) 24, (byte) 16),
            BACK_FACE = new HeadFace((byte) 24, (byte) 8, (byte) 32, (byte) 16),
            BOTTOM_FACE = new HeadFace((byte) 16, (byte) 0, (byte) 24, (byte) 8),
            TOP_FACE = new HeadFace((byte) 8, (byte) 0, (byte) 16, (byte) 8);
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final String FZMM_PATH = mc.runDirectory.toPath() + "\\config\\fzmmConfig";
    private static final String ERROR_SKIN = "ewogICJ0aW1lc3RhbXAiIDogMTYyNzg1NzA3NTc0OCwKICAicHJvZmlsZUlkIiA6ICJmZTdlM2MzNGRkMTA0ODc1ODFjNTUwZjQzZjEwNWI4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RRdWlja1NpbHZlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84MjA0YzFkZGE5NmFlMjNiNTQwZjhiYWIzNDdhNmE0MzBhNzRhNWY4MGM2Y2Y2ZmI1MjJjOGEwYTg0MjY5ODMwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0";
    private static final Logger LOGGER = LogManager.getLogger("Player Statue");
    private static int requestDelay;
    public static String apiKey;
    private static statuePart[] statue;
    private static byte uploadIndex;
    private static BufferedImage headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    private static BufferedImage skinBuffered;
    private static Graphics2D g = headSkin.createGraphics();
    private static final int[] baseSkinId = new int[3];
    private static Direction direction;
    private static byte skinSize, requestTry;

    protected static void generateStatue(String skin, int x, int y, int z, @Nullable String name, Direction direction2, StatueScreen.SkinMode skinMode, boolean isShulker) {
        ItemStack container = isShulker ? Items.WHITE_SHULKER_BOX.getDefaultStack() : Items.BARREL.getDefaultStack();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList containerItems = new NbtList();
        Random random = new Random(new Date().getTime());
        assert mc.player != null;

        for (int i = 0; i != 3; i++) {
            baseSkinId[i] = random.nextInt(Integer.MAX_VALUE);
        }
        statue = new statuePart[26];
        direction = direction2;
        requestTry = 0;
        apiKey = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().general.mineSkinApiKey;
        if (apiKey.length() != 0) {
            apiKey = "key=" + apiKey;
        }

        new Thread(() -> {
            StatueScreen.progress = new LiteralText("Getting skin...");
            if (skinMode == StatueScreen.SkinMode.FROM_PC) {
                File skinFile = new File(skin);
                try {
                    skinBuffered = ImageIO.read(skinFile);
                } catch (IOException e) {
                    StatueScreen.progress = new LiteralText("Error loading skin (IOException)");
                    StatueScreen.active = false;
                    return;
                }
            } else {
                try {
                    skinBuffered = getPlayerSkin(FzmmUtils.getPlayerUuid(skin));
                } catch (IOException e) {
                    e.printStackTrace();
                    StatueScreen.progress = new LiteralText("Error loading skin (IOException)");
                    StatueScreen.active = false;
                    return;
                } catch (ClassCastException e) {
                    StatueScreen.progress = new LiteralText("The player is not premium");
                    StatueScreen.active = false;
                    return;
                }
            }
            StatueScreen.progress = new LiteralText("Getting delay...");
            try {
                requestDelay = getDelay() * 1000;
            } catch (Exception e) {
                requestDelay = 5000;
                apiKey = "";
            }
            StatueScreen.progress = new LiteralText("0/26 skins generated");
            uploadSkins();

            for (byte i = 0; i != 26; i++) {
                NbtCompound tagItems = new NbtCompound();

                if (statue[i] == null) {
                    statue[i] = new statuePart(ERROR_SKIN);
                }

                tagItems.putInt("Slot", i);
                tagItems.putString("id", "armor_stand");
                tagItems.putInt("Count", 1);

                tagItems.put("tag", statue[i].getArmorStand(String.valueOf(i)));

                containerItems.add(tagItems);
            }

            blockEntityTag.put("Items", containerItems);
            container.putSubTag("BlockEntityTag", blockEntityTag);

            FzmmUtils.giveItem(updateStatue(container, x, y, z, direction, name));
            StatueScreen.progress = new LiteralText("Finished statue");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            StatueScreen.active = false;
            StatueScreen.progress = new LiteralText("");
        }).start();
    }

    public static class statuePart {
        NbtCompound head;

        public statuePart(String skinValue) {
            NbtList textures = new NbtList();
            NbtCompound value = new NbtCompound(),
                    properties = new NbtCompound(),
                    skullOwner = new NbtCompound(),
                    tag = new NbtCompound();
            Random random = new Random(new Date().getTime());
            NbtIntArray id = new NbtIntArray(new int[]{baseSkinId[0], baseSkinId[1], baseSkinId[2], random.nextInt(Integer.MAX_VALUE)});

            value.putString("Value", skinValue);
            textures.add(value);
            properties.put("textures", textures);
            skullOwner.put("Properties", properties);
            skullOwner.put("Id", id);

            tag.put("SkullOwner", skullOwner);

            this.head = tag;
        }

        public NbtCompound getArmorStand(String name) {
            NbtCompound tag = new NbtCompound(),
                    entityTag = new NbtCompound(),
                    headTag = new NbtCompound(),
                    display = new NbtCompound();
            NbtList tags = new NbtList(),
                    handItems = new NbtList();

            headTag.putString("id", "player_head");
            headTag.putByte("Count", (byte) 1);
            headTag.put("tag", this.head);
            handItems.add(headTag);

            tags.add(NbtString.of("PlayerStatue"));

            display.putString("Name", name);

            entityTag.put("Tags", tags);
            entityTag.put("HandItems", handItems);
            entityTag.put("Pos", null);
            entityTag.putInt("DisabledSlots", 4144959); // 4144959
            entityTag.putBoolean("NoGravity", true);
            entityTag.putBoolean("ShowArms", true);
            entityTag.putBoolean("Invisible", true);

            tag.put("EntityTag", entityTag);
            tag.put("display", display);
            return tag;
        }
    }

    public static void uploadSkins() {
        skinSize = 1;
        if (skinBuffered.getWidth() == 128) {
            skinSize = 2;
        }
        headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = headSkin.createGraphics();

        //Leg bottom-middle left (1, 2)
        bottomAndMiddleArmOrLeg(false, 16, 56, 0, 56);

        //Leg top left (3)
        sideFrontBackFace(false, 16, 52, 0, 52);
        upload();

        //Body bottom-middle-top left (4, 5, 6)
        for (byte i = 28; i >= 20; i -= 4) {
            frontBack(24, i, 32, i, 24, i + 16, 32, i + 16);
            upload();
        }

        //Leg bottom-middle right (7, 8)
        bottomAndMiddleArmOrLeg(true, 0, 24, 0, 40);

        //Leg top right (9)
        sideFrontBackFace(true, 0, 20, 0, 36);
        upload();

        //Body bottom-middle-top right (10, 11, 12)
        for (byte i = 28; i >= 20; i -= 4) {
            frontBack(20, i, 36, i, 20, i + 16, 36, i + 16);
            upload();
        }

        //Arm bottom-middle left (13, 14)
        bottomAndMiddleArmOrLeg(false, 32, 56, 48, 56);

        //Arm top left (15)
        sideFrontBackFace(false, 32, 52, 48, 52);
        draw(TOP_FACE.x, TOP_FACE.y, TOP_FACE.endX, TOP_FACE.endY, 36, 48, 40, 52);
        draw(TOP_FACE.x + 32, TOP_FACE.y, TOP_FACE.endX + 32, TOP_FACE.endY, 52, 48, 56, 52);
        upload();

        //Arm bottom-middle right (16, 17)
        bottomAndMiddleArmOrLeg(true, 40, 24, 40, 40);

        //Arm top right (18)
        sideFrontBackFace(true, 40, 20, 40, 36);
        draw(TOP_FACE.x, TOP_FACE.y, TOP_FACE.endX, TOP_FACE.endY, 44, 16, 48, 20);
        draw(TOP_FACE.x + 32, TOP_FACE.y, TOP_FACE.endX + 32, TOP_FACE.endY, 44, 32, 48, 36);
        upload();

        //Head front bottom left (19)
        headFace(FRONT_FACE, 12, 12);
        headFace(LEFT_FACE, 16, 12);
        headFace(BOTTOM_FACE, 20, 4);
        upload();

        //Head front top left (20)
        headFace(FRONT_FACE, 12, 8);
        headFace(LEFT_FACE, 16, 8);
        headFace(TOP_FACE, 12, 4);
        upload();

        //Head front bottom right (21)
        headFace(FRONT_FACE, 8, 12);
        headFace(RIGHT_FACE, 4, 12);
        headFace(BOTTOM_FACE, 16, 4);
        upload();

        //Head front top right (22)
        headFace(FRONT_FACE, 8, 8);
        headFace(RIGHT_FACE, 4, 8);
        headFace(TOP_FACE, 8, 4);
        upload();

        //Head back bottom left (23)
        headFace(BACK_FACE, 24, 12);
        headFace(LEFT_FACE, 20, 12);
        headFace(BOTTOM_FACE, 20, 0);
        upload();

        //Head back top left (24)
        headFace(BACK_FACE, 24, 8);
        headFace(LEFT_FACE, 20, 8);
        headFace(TOP_FACE, 12, 0);
        upload();

        //Head back bottom right (25)
        headFace(BACK_FACE, 28, 12);
        headFace(RIGHT_FACE, 0, 12);
        headFace(BOTTOM_FACE, 16, 0);
        upload();

        //Head back top right (26)
        headFace(BACK_FACE, 28, 8);
        headFace(RIGHT_FACE, 0, 8);
        headFace(TOP_FACE, 8, 0);
        upload();
    }

    public static void upload() {
        try {
            File fzmmPath = new File(FZMM_PATH);
            if (!fzmmPath.exists() && !fzmmPath.mkdirs()) {
                throw new Exception("No se pudo crear la carpeta de configuraciones");
            }
            // TODO: pasar el BufferedImage a File sin escribirlo
            ImageIO.write(headSkin, "png", new File(FZMM_PATH + "\\playerStatue.png"));
            apiRequest(new File(FZMM_PATH + "\\playerStatue.png"), uploadIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadIndex++;
        headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        g = headSkin.createGraphics();
    }

    public static void apiRequest(File skinFile, byte statueIndex) throws IOException, InterruptedException {
        Thread.sleep(requestDelay);
        URLConnection connection = new URL("https://api.mineskin.org/generate/upload?model=steve" + (apiKey.isEmpty() ? "" : "&") + apiKey).openConnection();

        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
        httpsConnection.setUseCaches(false);
        httpsConnection.setDoOutput(true);
        httpsConnection.setDoInput(true);
        httpsConnection.setRequestMethod("POST");
        String boundary = UUID.randomUUID().toString();
        httpsConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpsConnection.setRequestProperty("User-Agent", "User-Agent");

        OutputStream outputStream = httpsConnection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        final String LINE = "\r\n";
        writer.append("--").append(boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"file\"").append(LINE);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE);
        writer.append(LINE);
        writer.append(skinFile.getName()).append(LINE);
        writer.flush();

        writer.append("--").append(boundary).append(LINE);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(skinFile.getName()).append("\"").append(LINE);
        writer.append("Content-Type: image/png").append(LINE);
        writer.append("Content-Transfer-Encoding: binary").append(LINE);
        writer.append(LINE);
        writer.flush();

        byte[] fileBytes = Files.readAllBytes(skinFile.toPath());
        outputStream.write(fileBytes, 0, fileBytes.length);

        outputStream.flush();
        writer.append(LINE);
        writer.flush();

        writer.append("--").append(boundary).append("--").append(LINE);
        writer.close();
        httpsConnection.disconnect();

        if (httpsConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String reply = br.readLine();
            LiteralText progressText = new LiteralText((statueIndex + 1) + "/26 skins generated");
            assert mc.player != null;

            statue[statueIndex] = new statuePart(reply.split("\"value\":\"")[1].split("\"")[0]);
            if (mc.currentScreen instanceof StatueScreen) {
                StatueScreen.progress = progressText;
            } else {
                mc.inGameHud.setOverlayMessage(progressText, false);
            }
        } else {
            LOGGER.log(Level.WARN, "httpsConnection error (" + httpsConnection.getResponseCode() + ") generating statue part " + uploadIndex);
            if (requestTry < 8) {
                requestTry++;
                apiRequest(skinFile, statueIndex);
            } else {
                statue[statueIndex] = new statuePart(ERROR_SKIN);
                LOGGER.log(Level.WARN, "statue part " + statueIndex + " could not be generated after several attempts");
            }
        }
    }

    public record HeadFace(byte x, byte y, byte endX, byte endY) {
    }

    public static void sideFrontBackFace(boolean isRight, int x, int y, int cape2X, int cape2Y) {
        HeadFace side;
        if (isRight) {
            side = RIGHT_FACE;
        } else {
            side = LEFT_FACE;
        }

        draw(side.x, side.y, side.endX, side.endY, x + (isRight ? 0 : 8), y, x + 4 + (isRight ? 0 : 8), y + 4);
        draw(side.x + 32, side.y, side.endX + 32, side.endY, cape2X + (isRight ? 0 : 8), cape2Y, cape2X + 4 + (isRight ? 0 : 8), cape2Y + 4);
        frontBack(x + 4, y, x + 12, y, cape2X + 4, cape2Y, cape2X + 12, cape2Y);
    }

    public static void frontBack(int xFront, int yFront, int xBack, int yBack, int cape2XFront, int cape2YFront, int cape2XBack, int cape2YBack) {
        draw(FRONT_FACE.x, FRONT_FACE.y, FRONT_FACE.endX, FRONT_FACE.endY, xFront, yFront, xFront + 4, yFront + 4);
        draw(BACK_FACE.x, BACK_FACE.y, BACK_FACE.endX, BACK_FACE.endY, xBack, yBack, xBack + 4, yBack + 4);
        draw(FRONT_FACE.x + 32, FRONT_FACE.y, FRONT_FACE.endX + 32, FRONT_FACE.endY, cape2XFront, cape2YFront, cape2XFront + 4, cape2YFront + 4);
        draw(BACK_FACE.x + 32, BACK_FACE.y, BACK_FACE.endX + 32, BACK_FACE.endY, cape2XBack, cape2YBack, cape2XBack + 4, cape2YBack + 4);
    }

    public static void bottomAndMiddleArmOrLeg(boolean isRight, int x, int y, int cape2X, int cape2Y) {
        sideFrontBackFace(isRight, x, y + 4, cape2X, cape2Y + 4);
        draw(BOTTOM_FACE.x, BOTTOM_FACE.y, BOTTOM_FACE.endX, BOTTOM_FACE.endY, x + 8, y - 8, x + 12, y - 4);
        draw(BOTTOM_FACE.x + 32, BOTTOM_FACE.y, BOTTOM_FACE.endX + 32, BOTTOM_FACE.endY, cape2X + 8, cape2Y - 8, cape2X + 12, cape2Y - 4);
        upload();

        sideFrontBackFace(isRight, x, y, cape2X, cape2Y);
        upload();
    }

    public static void headFace(HeadFace face, int x, int y) {
        draw(face.x, face.y, face.endX, face.endY, x, y, x + 4, y + 4);
        draw(face.x + 32, face.y, face.endX + 32, face.endY, x + 32, y, x + 36, y + 4);
    }

    public static ItemStack updateStatue(ItemStack statue, float x, int y, float z, Direction statueDirection, @Nullable String name) {
        NbtCompound tag,
                finalTag,
                pose = new NbtCompound(),
                display = new NbtCompound();
        NbtList items,
                armPose = new NbtList(),
                lore = new NbtList();
        String loreCoords;
        int directionSelect;
        NbtList[] coordinates;
        float xRight,
                xLeft,
                zRight,
                zLeft,
                xRightArm,
                zRightArm,
                xLeftArm,
                zLeftArm,
                xRightFrontHead,
                zRightFrontHead,
                xLeftFrontHead,
                zLeftFrontHead,
                xRightBackHead,
                zRightBackHead,
                xLeftBackHead,
                zLeftBackHead,
                xName = x + 0.5f,
                zName = z + 0.5f;
        final ArrayList<float[]> statueCoords = new ArrayList<>();

        loreCoords = (int) x + " " + y + " " + (int) z + " - ";

        switch (statueDirection) {
            case NORTH -> {
                x += 1.01f;
                z -= 0.25f;
                directionSelect = 135;
                xRight = x - 0.125f;
                zRight = z;
                xLeft = x + 0.125f;
                zLeft = z;
                xRightArm = x - 0.375f;
                zRightArm = z;
                xLeftArm = x + 0.375f;
                zLeftArm = z;
                xRightFrontHead = x - 0.125f;
                zRightFrontHead = z - 0.125f;
                xLeftFrontHead = x + 0.125f;
                zLeftFrontHead = z - 0.125f;
                xRightBackHead = x - 0.125f;
                zRightBackHead = z + 0.125f;
                xLeftBackHead = x + 0.125f;
                zLeftBackHead = z + 0.125f;
                loreCoords += "North";
            }
            case EAST -> {
                x += 0.93f;
                z += 0.7f;
                directionSelect = -135;
                xRight = x;
                zRight = z - 0.125f;
                xLeft = x;
                zLeft = z + 0.125f;
                xRightArm = x;
                zRightArm = z - 0.375f;
                xLeftArm = x;
                zLeftArm = z + 0.375f;
                xRightFrontHead = x + 0.125f;
                zRightFrontHead = z - 0.125f;
                xLeftFrontHead = x + 0.125f;
                zLeftFrontHead = z + 0.125f;
                xRightBackHead = x - 0.125f;
                zRightBackHead = z - 0.125f;
                xLeftBackHead = x - 0.125f;
                zLeftBackHead = z + 0.125f;
                loreCoords += "East";
            }
            case SOUTH -> {
                x -= 0.01f;
                z += 0.6f;
                directionSelect = -45;
                xRight = x + 0.125f;
                zRight = z;
                xLeft = x - 0.125f;
                zLeft = z;
                xRightArm = x + 0.375f;
                zRightArm = z;
                xLeftArm = x - 0.375f;
                zLeftArm = z;
                xRightFrontHead = x + 0.125f;
                zRightFrontHead = z + 0.125f;
                xLeftFrontHead = x - 0.125f;
                zLeftFrontHead = z + 0.125f;
                xRightBackHead = x + 0.125f;
                zRightBackHead = z - 0.125f;
                xLeftBackHead = x - 0.125f;
                zLeftBackHead = z - 0.125f;
                loreCoords += "South";
            }
            case WEST -> {
                x += 0.08f;
                z -= 0.33f;
                directionSelect = 45;
                xRight = x;
                zRight = z + 0.125f;
                xLeft = x;
                zLeft = z - 0.125f;
                xRightArm = x;
                zRightArm = z + 0.375f;
                xLeftArm = x;
                zLeftArm = z - 0.375f;
                xRightFrontHead = x - 0.125f;
                zRightFrontHead = z + 0.125f;
                xLeftFrontHead = x - 0.125f;
                zLeftFrontHead = z - 0.125f;
                xRightBackHead = x + 0.125f;
                zRightBackHead = z + 0.125f;
                xLeftBackHead = x + 0.125f;
                zLeftBackHead = z - 0.125f;
                loreCoords += "West";
            }
            default -> {
                return Items.BARRIER.getDefaultStack();
            }
        }

        lore.add(NbtString.of(Text.Serializer.toJson(new LiteralText("Player Statue")
                .setStyle(Style.EMPTY.withColor(1666703)))));
        lore.add(NbtString.of(Text.Serializer.toJson(new LiteralText(loreCoords)
                .setStyle(Style.EMPTY.withColor(4288392)))));
        display.put("Lore", lore);

        y--;

        armPose.add(NbtFloat.of(-45));
        armPose.add(NbtFloat.of(directionSelect));
        armPose.add(NbtFloat.of(0));
        pose.put("RightArm", armPose);

        generateCoordinates(statueCoords, xRight, y + 0.1f, zRight, (byte) 6); // right leg and right body
        generateCoordinates(statueCoords, xLeft, y + 0.1f, zLeft, (byte) 6); // left leg and left body
        generateCoordinates(statueCoords, xRightArm, y + 0.85f, zRightArm, (byte) 3); // right arm
        generateCoordinates(statueCoords, xLeftArm, y + 0.85f, zLeftArm, (byte) 3); // left arm

        generateCoordinates(statueCoords, xRightFrontHead, y + 1.6f, zRightFrontHead, (byte) 2); // right head front
        generateCoordinates(statueCoords, xLeftFrontHead, y + 1.6f, zLeftFrontHead, (byte) 2); // left head front
        generateCoordinates(statueCoords, xRightBackHead, y + 1.6f, zRightBackHead, (byte) 2); // right head back
        generateCoordinates(statueCoords, xLeftBackHead, y + 1.6f, zLeftBackHead, (byte) 2); // left head back

        generateCoordinates(statueCoords, xName, y + 0.9f, zName, (byte) 1); // name

        coordinates = fixZFight(statueDirection, statueCoords);

        items = statue.getOrCreateSubTag("BlockEntityTag").getList("Items", 10);
        assert statue.getTag() != null;

        if (items.size() < 26) {
            return Items.BARRIER.getDefaultStack();
        }

        for (byte i = 0; i != 26; i++) {
            NbtCompound item = (NbtCompound) items.get(i);
            tag = (NbtCompound) item.get("tag");
            if (tag == null) {
                LOGGER.log(Level.ERROR, "statue[" + i + "] is null");
                tag = new statuePart(ERROR_SKIN)
                        .getArmorStand(i + " - Error skin");
            }
            tag.getCompound("EntityTag").put("Pos", coordinates[i]);
            tag.getCompound("EntityTag").put("Pose", pose);
            item.put("tag", tag);
            items.set(i, item);
        }
        if (name != null && !name.isEmpty()) {
            try {
                Text.Serializer.fromJson(name);
            } catch (Exception e) {
                name = Text.Serializer.toJson(new LiteralText(name));
            }

            if (items.size() < 27) {
                items.add(26, generateStatueName(coordinates[26], name));
            } else {
                items.set(26, generateStatueName(coordinates[26], name));
            }

            display.put("Name", NbtString.of(name));
        } else if (items.size() == 27) {
            items.remove(26);
        }
        finalTag = statue.getTag();
        finalTag.getCompound("BlockEntityTag").put("Items", items);
        finalTag.put("display", display);
        statue.setTag(finalTag);
        return statue;
    }

    public static void generateCoordinates(ArrayList<float[]> statueCoordinates, final float x, final float y, final float z, final byte amount) {
        for (byte i = 0; i != amount; i++) {
            float[] coordinate = new float[3];
            coordinate[0] = x;
            coordinate[1] = y + i * Y_DIFFERENCE;
            coordinate[2] = z;
            statueCoordinates.add(coordinate);
        }
    }

    public static NbtCompound generateStatueName(NbtList coordinates, String name) {
        NbtCompound tagItems = new NbtCompound();

        NbtCompound tag = new NbtCompound(),
                entityTag = new NbtCompound(),
                display = new NbtCompound();
        NbtList tags = new NbtList();

        tags.add(NbtString.of("PlayerStatue"));
        tags.add(NbtString.of("StatueName"));

        display.putString("Name", String.valueOf(26));

        entityTag.put("Tags", tags);
        entityTag.put("Pos", coordinates);
        entityTag.putInt("DisabledSlots", 4144959); // 4144959
        entityTag.putString("CustomName", name);
        entityTag.putBoolean("NoGravity", true);
        entityTag.putBoolean("Invisible", true);
        entityTag.putBoolean("CustomNameVisible", true);

        tag.put("EntityTag", entityTag);
        tag.put("display", display);

        tagItems.putInt("Slot", 26);
        tagItems.putString("id", "armor_stand");
        tagItems.putInt("Count", 1);
        tagItems.put("tag", tag);

        return tagItems;
    }

    public static String getStatueName() {
        assert mc.player != null;
        ItemStack stack = mc.player.getMainHandStack();
        NbtCompound tag = stack.getOrCreateSubTag("BlockEntityTag");

        if (tag.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList itemsTag = tag.getList("Items", NbtElement.COMPOUND_TYPE);
            if (itemsTag.size() >= 27) {
                NbtCompound statue = (NbtCompound) itemsTag.get(26);
                if (statue.contains("tag", NbtElement.COMPOUND_TYPE)) {
                    NbtCompound statueTag = statue.getCompound("tag");
                    if (statueTag.contains("EntityTag", NbtElement.COMPOUND_TYPE)) {
                        NbtCompound entityTag = statueTag.getCompound("EntityTag");
                        if (entityTag.contains("CustomName", NbtElement.STRING_TYPE)) {
                            return entityTag.getString("CustomName");
                        }
                    }
                }
            }
        }
        return "";
    }

    public static BufferedImage getPlayerSkin(String uuid) throws IOException {
        URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JsonParser parser = new JsonParser();
        JsonObject obj = (JsonObject) parser.parse(new InputStreamReader(conn.getInputStream()));
        JsonArray properties = (JsonArray) obj.get("properties");
        JsonObject prop = (JsonObject) properties.get(0);

        String dataJsonStr = new String(Base64.getDecoder().decode(prop.get("value").getAsString()));
        obj = (JsonObject) parser.parse(dataJsonStr);
        String skinUrl = ((JsonObject) ((JsonObject) obj.get("textures")).get("SKIN")).get("url").getAsString();

        conn.disconnect();

        return FzmmUtils.getImageFromUrl(skinUrl);
    }

    public static int getDelay() throws IOException {
        URL url = new URL("https://api.mineskin.org/get/delay?" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        JsonParser parser = new JsonParser();
        JsonObject obj = (JsonObject) parser.parse(new InputStreamReader(conn.getInputStream()));
        int delay = obj.get("delay").getAsInt();

        conn.disconnect();

        return delay;
    }

    public static void draw(int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(skinBuffered, dx1, dy1, dx2, dy2, sx1 * skinSize, sy1 * skinSize, sx2 * skinSize, sy2 * skinSize, null);
    }

    private static NbtList[] fixZFight(Direction direction, ArrayList<float[]> coords) {
        float distance = 0.00001f;
        NbtList[] coordinates = new NbtList[27];
        byte x = 0,
            y = 1,
            z = 2;

        coords.get(18)[y] += distance; // head front bottom left
        coords.get(19)[y] += distance; // head front top left
        coords.get(20)[y] += distance; // head front bottom right
        coords.get(21)[y] += distance * 4; // head front top right
        coords.get(22)[y] -= distance; // head back bottom left
        coords.get(23)[y] += distance * 3; // head back top left
        coords.get(24)[y] += distance; // head back bottom right

        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            byte valueX = x;
            x = z;
            z = valueX;
            distance *= -1;
        }

        coords.get(1)[x] -= distance * 2; // left middle leg
        coords.get(1)[z] += distance; // left middle leg
        coords.get(2)[x] += distance * 2; // left top leg
        coords.get(3)[x] -= distance; // left bottom body
        coords.get(4)[x] += distance; // left middle body
        coords.get(5)[x] -= distance; // left top body
        coords.get(6)[x] -= distance; // right bottom leg
        coords.get(7)[x] += distance; // right middle leg
        coords.get(7)[z] -= distance; // right middle leg
        coords.get(8)[x] -= distance * 2; // right top leg
        coords.get(10)[x] -= distance * 2; // right middle body
        coords.get(13)[x] -= distance * 2; // left middle arm
        coords.get(13)[z] += distance; // left middle arm
        coords.get(15)[x] -= distance; // right bottom arm
        coords.get(16)[x] += distance; // right middle arm
        coords.get(16)[z] -= distance; // right middle arm
        coords.get(17)[x] -= distance; // right top arm
        coords.get(18)[x] += distance; // head front bottom left
        coords.get(19)[x] += distance; // head front top left
        coords.get(19)[x] += distance * 2; // head front top left
        coords.get(19)[z] += distance * 2; // head front top left
        coords.get(20)[x] -= distance; // head front bottom right
        coords.get(21)[x] += distance; // head front top right
        coords.get(21)[z] -= distance; // head front top right
        coords.get(22)[z] += distance; // head back bottom left
        coords.get(23)[x] -= distance * 3; // head back top left
        coords.get(23)[z] -= distance * 3; // head back top left
        coords.get(24)[x] += distance; // head back bottom right
        coords.get(24)[z] -= distance * 2; // head back bottom right
        coords.get(25)[x] -= distance * 2; // head back top right
        coords.get(25)[z] += distance; // head back top right

        for (byte i = 0; i != 27; i++) {
            NbtList xyz = new NbtList();
            xyz.add(NbtDouble.of(coords.get(i)[0]));
            xyz.add(NbtDouble.of(coords.get(i)[1]));
            xyz.add(NbtDouble.of(coords.get(i)[2]));
            coordinates[i] = xyz;
        }
        return coordinates;
    }
}
