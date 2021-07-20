package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
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
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class StatueLogic {

	public static final float Y_DIFFERENCE = 0.25f;
	public static final String API_KEY = "&key=" + "92a4b903114bed4cad604522b98349b4cf1d0526767e030576600aa45782f997"; //TODO: poner en config
	private static final HeadFace RIGHT_FACE = new HeadFace((byte) 0, (byte) 8, (byte) 8, (byte) 16),
		FRONT_FACE = new HeadFace((byte) 8, (byte) 8, (byte) 16, (byte) 16),
		LEFT_FACE = new HeadFace((byte) 16, (byte) 8, (byte) 24, (byte) 16),
		BACK_FACE = new HeadFace((byte) 24, (byte) 8, (byte) 32, (byte) 16),
		BOTTOM_FACE = new HeadFace((byte) 16, (byte) 0, (byte) 24, (byte) 8),
		TOP_FACE = new HeadFace((byte) 8, (byte) 0, (byte) 16, (byte) 8);
	private static final String FZMM_PATH = MinecraftClient.getInstance().runDirectory.toPath() + "\\config\\fzmmConfig";
	private static final short REQUEST_DELAY = 3000;
	private static statuePart[] statue;
	private static byte count;
	private static BufferedImage headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
	private static BufferedImage skinFile;
	private static Graphics2D g = headSkin.createGraphics();
	private static final int[] baseSkinId = new int[3];
	private static final NbtList[] coordinates = new NbtList[27];
	private static Direction direction;


	protected static void generateStatue(String skinPath, int x, short y, int z, @Nullable String name, Direction direction2) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ItemStack shulker = Items.WHITE_SHULKER_BOX.getDefaultStack();
		NbtCompound blockEntityTag = new NbtCompound();
		NbtList shulkerItems = new NbtList();
		Random random = new Random(new Date().getTime());
		assert mc.player != null;

		for (int i = 0; i != 3; i++) {
			baseSkinId[i] = random.nextInt(Integer.MAX_VALUE);
		}
		count = 0;
		statue = new statuePart[26];
		direction = direction2;
		StatueScreen.progress = new LiteralText("0/26 skins generated");

		new Thread(() -> {
			File skinFile = new File(skinPath);
			try (FileInputStream input = new FileInputStream(skinFile)) {
				if (input.read() == 137) {
					uploadSkins(ImageIO.read(skinFile));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (count = 0; count != 26; count++) {
				NbtCompound tagItems = new NbtCompound();

				tagItems.putInt("Slot", count);
				tagItems.putString("id", "armor_stand");
				tagItems.putInt("Count", 1);
				tagItems.put("tag", statue[count].getArmorStand());

				shulkerItems.add(tagItems);
			}
			if (name != null) {
				NbtCompound tagItems = new NbtCompound();

				NbtCompound tag = new NbtCompound(),
					entityTag = new NbtCompound(),
					display = new NbtCompound();
				NbtList tags = new NbtList();

				tags.add(NbtString.of("PlayerStatue"));
				tags.add(NbtString.of("StatueName"));

				display.putString("Name", String.valueOf(26));

				entityTag.put("Tags", tags);
				entityTag.putString("CustomName", Text.Serializer.toJson(new LiteralText(name)));
				entityTag.put("Pos", null);
				entityTag.putBoolean("NoGravity", true);
				entityTag.putBoolean("Invisible", true);
				entityTag.putBoolean("CustomNameVisible", true);

				tag.put("EntityTag", entityTag);
				tag.put("display", display);

				tagItems.putInt("Slot", 26);
				tagItems.putString("id", "armor_stand");
				tagItems.putInt("Count", 1);
				tagItems.put("tag", tag);

				shulkerItems.add(tagItems);
			}

			blockEntityTag.put("Items", shulkerItems);
			shulker.putSubTag("BlockEntityTag", blockEntityTag);

			FzmmUtils.giveItem(updateStatue(shulker, x, y, z, direction));
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

		public NbtCompound getArmorStand() {
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

			display.putString("Name", String.valueOf(count));

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

	public static void uploadSkins(BufferedImage skin) {
		skinFile = skin;
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
		g.drawImage(skinFile, TOP_FACE.x, TOP_FACE.y, TOP_FACE.endX, TOP_FACE.endY, 36, 48, 40, 52, null);
		g.drawImage(skinFile, TOP_FACE.x + 32, TOP_FACE.y, TOP_FACE.endX + 32, TOP_FACE.endY, 52, 48, 56, 52, null);
		upload();

		//Arm bottom-middle right (16, 17)
		bottomAndMiddleArmOrLeg(true, 40, 24, 40, 40);

		//Arm top right (18)
		sideFrontBackFace(true, 40, 20, 40, 36);
		g.drawImage(skinFile, TOP_FACE.x, TOP_FACE.y, TOP_FACE.endX, TOP_FACE.endY, 44, 16, 48, 20, null);
		g.drawImage(skinFile, TOP_FACE.x + 32, TOP_FACE.y, TOP_FACE.endX + 32, TOP_FACE.endY, 44, 32, 48, 36, null);
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
			apiRequest(new File(FZMM_PATH + "\\playerStatue.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		headSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		g = headSkin.createGraphics();
	}

	public static void apiRequest(File skinFile) throws IOException, InterruptedException {
		URLConnection connection = new URL("https://api.mineskin.org/generate/upload?model=steve" + API_KEY).openConnection();

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
			MinecraftClient mc = MinecraftClient.getInstance();
			LiteralText text = new LiteralText((count + 1) + "/26 skins generated");
			assert mc.player != null;

			statue[count] = new StatueLogic.statuePart(reply.split("\"value\":\"")[1].split("\"")[0]);
			count++;
			if (mc.currentScreen instanceof StatueScreen)
				StatueScreen.progress = text;
			else {
				mc.inGameHud.addChatMessage(MessageType.SYSTEM, text, mc.player.getUuid());
			}
		} else {
			System.out.println("Fzmm Mod: La conexión dio un error (" + httpsConnection.getResponseCode() + ") en count " + count);
			Thread.sleep(REQUEST_DELAY);
			apiRequest(skinFile);
			return;
		}
		if (count != 25) {
			Thread.sleep(REQUEST_DELAY);
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

		g.drawImage(skinFile, side.x, side.y, side.endX, side.endY, x + (isRight ? 0 : 8), y, x + 4 + (isRight ? 0 : 8), y + 4, null);
		g.drawImage(skinFile, side.x + 32, side.y, side.endX + 32, side.endY, cape2X + (isRight ? 0 : 8), cape2Y, cape2X + 4 + (isRight ? 0 : 8), cape2Y + 4, null);
		frontBack(x + 4, y, x + 12, y, cape2X + 4, cape2Y, cape2X + 12, cape2Y);
	}

	public static void frontBack(int xFront, int yFront, int xBack, int yBack, int cape2XFront, int cape2YFront, int cape2XBack, int cape2YBack) {
		g.drawImage(skinFile, FRONT_FACE.x, FRONT_FACE.y, FRONT_FACE.endX, FRONT_FACE.endY, xFront, yFront, xFront + 4, yFront + 4, null);
		g.drawImage(skinFile, BACK_FACE.x, BACK_FACE.y, BACK_FACE.endX, BACK_FACE.endY, xBack, yBack, xBack + 4, yBack + 4, null);
		g.drawImage(skinFile, FRONT_FACE.x + 32, FRONT_FACE.y, FRONT_FACE.endX + 32, FRONT_FACE.endY, cape2XFront, cape2YFront, cape2XFront + 4, cape2YFront + 4, null);
		g.drawImage(skinFile, BACK_FACE.x + 32, BACK_FACE.y, BACK_FACE.endX + 32, BACK_FACE.endY, cape2XBack, cape2YBack, cape2XBack + 4, cape2YBack + 4, null);
	}

	public static void bottomAndMiddleArmOrLeg(boolean isRight, int x, int y, int cape2X, int cape2Y) {
		sideFrontBackFace(isRight, x, y + 4, cape2X, cape2Y + 4);
		g.drawImage(skinFile, BOTTOM_FACE.x, BOTTOM_FACE.y, BOTTOM_FACE.endX, BOTTOM_FACE.endY, x + 8, y - 8, x + 12, y - 4, null);
		g.drawImage(skinFile, BOTTOM_FACE.x + 32, BOTTOM_FACE.y, BOTTOM_FACE.endX + 32, BOTTOM_FACE.endY, cape2X + 8, cape2Y - 8, cape2X + 12, cape2Y - 4, null);
		upload();

		sideFrontBackFace(isRight, x, y, cape2X, cape2Y);
		upload();
	}

	public static void headFace(HeadFace face, int x, int y) {
		g.drawImage(skinFile, face.x, face.y, face.endX, face.endY, x, y, x + 4, y + 4, null);
		g.drawImage(skinFile, face.x + 32, face.y, face.endX + 32, face.endY, x + 32, y, x + 36, y + 4, null);
	}

	public static ItemStack updateStatue(ItemStack statue, float x, short y, float z, Direction direction2) {
		NbtCompound tag,
			finalTag,
			item,
			pose = new NbtCompound();
		NbtList items,
		armPose = new NbtList();
		int directionSelect;

		byte itemsSize;
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
			xName = x,
			zName = z;

		switch (direction2) {
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
				//Testear
				xRightFrontHead = x - 0.125f;
				zRightFrontHead = z - 0.125f;
				xLeftFrontHead = x + 0.125f;
				zLeftFrontHead = z - 0.125f;
				xRightBackHead = x - 0.125f;
				zRightBackHead = z + 0.125f;
				xLeftBackHead = x + 0.125f;
				zLeftBackHead = z + 0.125f;
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
			}
			case WEST -> {
				x += 0.7f;
				z -= 0.955f;
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
			}
			default -> {
				return ItemStack.EMPTY;
			}
		}

		armPose.add(NbtFloat.of(-45));
		armPose.add(NbtFloat.of(directionSelect));
		armPose.add(NbtFloat.of(0));
		pose.put("RightArm", armPose);

		for (byte i = 0; i != 27; i++) {
			coordinates[i] = new NbtList();
		}
		count = 0;
		y--;

		generateCoordinates(xRight, y + 0.1f, zRight, (byte) 6); // right leg and right body
		generateCoordinates(xLeft, y + 0.1f, zLeft, (byte) 6); // left leg and left body
		generateCoordinates(xRightArm, y + 0.85f, zRightArm, (byte) 3); // right arm
		generateCoordinates(xLeftArm, y + 0.85f, zLeftArm, (byte) 3); // left arm

		generateCoordinates(xRightFrontHead, y + 1.6f, zRightFrontHead, (byte) 2); // right head front
		generateCoordinates(xLeftFrontHead, y + 1.6f, zLeftFrontHead, (byte) 2); // left head front
		generateCoordinates(xRightBackHead, y + 1.6f, zRightBackHead, (byte) 2); // right head back
		generateCoordinates(xLeftBackHead, y + 1.6f, zLeftBackHead, (byte) 2); // left head back

//		generateCoordinates(x - 0.425f, y + 0.9f, z - 0.825f, (byte) 1); // name
		generateCoordinates(xName, y + 0.9f, zName, (byte) 1); // name

		items = statue.getOrCreateSubTag("BlockEntityTag").getList("Items", 10);
		assert statue.getTag() != null;

		if (items.size() < 26) {
			System.out.println("El item de tu mano contiene menos de 26 items");
			return Items.BARRIER.getDefaultStack();
		}
		itemsSize = items.size() >= 27 ? (byte) 27 : (byte) 26;
		for (byte i = 0; i != itemsSize; i++) {
			item = (NbtCompound) items.get(i);
			tag = (NbtCompound) item.get("tag");
			if (tag == null) {
				System.out.println("Error: statue[" + i + "] es null");
				tag = new statuePart("ewogICJ0aW1lc3RhbXAiIDogMTYxNDY1MDMyNTM3NiwKICAicHJvZmlsZUlkIiA6ICI1NDU2NTUxMmJhMzk0NzM1YjQ2YmQyMmE2MDMzYWFiNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZWRvdmthIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M1OTFkODE4MTVlNWVjMGZkMjUwMWQ5MzA1YjVmZDc3MDIyYmU3YjNiOWE4MDk2ZDQ1YmY3ZjEzMmNjMGMwOTkiCiAgICB9CiAgfQp9")
					.getArmorStand();
			}
			tag.getCompound("EntityTag").put("Pos", coordinates[i]);
			tag.getCompound("EntityTag").put("Pose", pose);
			item.put("tag", tag);
			items.set(i, item);
		}
		finalTag = statue.getTag();
		finalTag.getCompound("BlockEntityTag").put("Items", items);
		statue.setTag(finalTag);
		return statue;
	}

	public static void generateCoordinates(final float x, final float y, final float z, final byte amount) {
		for (byte i = 0; i != amount; i++) {
			coordinates[count].add(NbtDouble.of(x));
			coordinates[count].add(NbtDouble.of(y + i * Y_DIFFERENCE));
			coordinates[count].add(NbtDouble.of(z));
			count++;
		}
	}

	public enum Direction {
		NORTH,
		EAST,
		SOUTH,
		WEST;

		Direction() {
		}
	}
}
