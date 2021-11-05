package fzmm.zailer.me.mixin;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    @Final
    private EntityType<?> type;

    private static BufferedImage skinPartBuffered;
    private static Graphics2D g;

    /**
     * @reason Get player head or armor with average color of skin
     * @author Zailer43
     */
    @Overwrite
    public @Nullable ItemStack getPickBlockStack() {
        if (!type.equals(EntityType.PLAYER)) {
            return null;
        }
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) ((Object) this);
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound tag = new NbtCompound();

        if (Screen.hasControlDown()) {
            tag = FzmmUtils.getPlayerHead(player.getGameProfile()).getNbt();
        } else if (Screen.hasAltDown()) {
            new Thread(() -> {
                BufferedImage skinBuffered;
                skinPartBuffered = new BufferedImage(40, 48, BufferedImage.TYPE_INT_ARGB);
                g = skinPartBuffered.createGraphics();

                if (!player.hasSkinTexture()) {
                    return;
                }
                try {
                    String skinPath = player.getSkinTexture().getPath();
                    String absolutePath = MinecraftClient.getInstance().runDirectory.toPath() + "\\assets\\skins\\" + skinPath.substring(6, 8) + "\\" + skinPath.substring(6);
                    File skinFile = new File(absolutePath);
                    skinBuffered = ImageIO.read(skinFile);

                    g.drawImage(skinBuffered, 0, 0, 40, 32, 16, 16, 56, 48, null);
                    g.drawImage(skinBuffered, 0, 32, 32, 48, 32,  48, 64, 64, null);
                    this.giveDyedArmor(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST, (byte) 6);

                    g.drawImage(skinBuffered, 0, 0, 16, 8, 0, 20, 16, 28, null);
                    g.drawImage(skinBuffered, 0, 8, 16, 16, 0, 36, 16, 44, null);
                    g.drawImage(skinBuffered, 0, 16, 32, 24, 0, 52, 32, 60, null);
                    this.giveDyedArmor(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS, (byte) 7);

                    g.drawImage(skinBuffered, 0, 0, 16, 4, 0, 28, 16, 32, null);
                    g.drawImage(skinBuffered, 0, 4, 16, 8, 0, 44, 16, 48, null);
                    g.drawImage(skinBuffered, 0, 8, 32, 12, 0, 60, 32, 64, null);
                    this.giveDyedArmor(Items.LEATHER_BOOTS, EquipmentSlot.FEET, (byte) 8);
                } catch (IOException ignore) {
                }
            }).start();
            return null;
        } else {
            tag.putString(SkullItem.SKULL_OWNER_KEY, player.getName().getString());
        }

        head.setNbt(tag);
        return head;
    }

    private void giveDyedArmor(Item item, EquipmentSlot slot, byte slotId) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        ItemStack stack = item.getDefaultStack();
        NbtCompound display = new NbtCompound();
        int averageColor = getAverageColor(skinPartBuffered);

        if (mc.interactionManager == null) {
            return;
        }

        display.putInt(DyeableItem.COLOR_KEY, averageColor);
        stack.setSubNbt(DyeableItem.DISPLAY_KEY, display);

        skinPartBuffered = new BufferedImage(40, 48, BufferedImage.TYPE_INT_ARGB);
        g = skinPartBuffered.createGraphics();

        mc.player.equipStack(slot, stack);
        mc.interactionManager.clickCreativeStack(stack, slotId);
    }

    private int getAverageColor(BufferedImage img) {
        byte width = (byte) img.getWidth();
        byte height = (byte) img.getHeight();
        int red = 0,
            green = 0,
            blue = 0;
        int totalPixels = 0;

        for (byte x = 0; x != width; x++) {
            for (byte y = 0; y != height; y++) {
                Color pixel = new Color(img.getRGB(x, y), true);
                if (pixel.getAlpha() == 255) {
                    red += pixel.getRed();
                    green += pixel.getGreen();
                    blue += pixel.getBlue();
                    totalPixels++;
                }
            }
        }

        red /= totalPixels;
        green /= totalPixels;
        blue /= totalPixels;

        return (red << 16) + (green << 8) + blue;
    }

}
