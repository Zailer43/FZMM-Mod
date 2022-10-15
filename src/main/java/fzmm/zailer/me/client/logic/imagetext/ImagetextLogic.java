package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.builders.ArmorStandBuilder;
import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.enums.options.ImagetextBookOption;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextLogic {
    private NbtList imagetext;
    private int width;
    private int height;

    public ImagetextLogic() {
        this.imagetext = new NbtList();
        this.width = 0;
        this.height = 0;
    }

    public ImagetextLogic generateImagetext(BufferedImage image, @Nullable String characters, int width, int height, boolean smoothRescaling) {
        image = this.resizeImage(image, width, height, smoothRescaling);
        this.width = width;
        this.height = height;
        if (characters == null || characters.isEmpty())
            characters = ImagetextLine.DEFAULT_TEXT;
        NbtList tooltipList = new NbtList();

        for (int y = 0; y != height; y++) {
            ImagetextLine line = new ImagetextLine(characters);
            for (int x = 0; x != width; x++) {
                line.add(image.getRGB(x, y));
            }
            tooltipList.add(FzmmUtils.toNbtString(line.getLine(), false));
        }
        this.imagetext = tooltipList;
        return this;
    }

    private BufferedImage resizeImage(BufferedImage image, int width, int height, boolean smoothRescaling) {
        Image tmp = image.getScaledInstance(width, height, smoothRescaling ? Image.SCALE_SMOOTH : Image.SCALE_REPLICATE);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * @param width                Width of which you want to preserve the aspect ratio.
     * @param height               Height of which you want to preserve the aspect ratio.
     * @param referenceSide        The side that is used as a reference for the new resolution.
     * @param referenceSideIsWidth If the variable referenceSide is width (true) otherwise it is height (false).
     * @return Vec2f.x = width, Vec2f.y = height
     * <p>
     */
    public static Vec2f changeResolutionKeepingAspectRatio(int width, int height, int referenceSide, boolean referenceSideIsWidth) {
        int modifiedSide = (int) ((double) referenceSide / (referenceSideIsWidth ? width : height) * (referenceSideIsWidth ? height : width));

        return referenceSideIsWidth ? new Vec2f(referenceSide, modifiedSide) : new Vec2f(modifiedSide, referenceSide);
    }

    public void giveInLore(ItemStack stack, boolean add) {
        if (stack.isEmpty())
            stack = FzmmUtils.getItem(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultStack();
        DisplayBuilder display = DisplayBuilder.of(stack);
        if (add)
            display.addLore(this.imagetext).get();//TODO
        else
            display.setLore(this.imagetext).get();

        FzmmUtils.giveItem(display.get());
    }

    public void giveBookTooltip(String author, String bookText, ImagetextBookOption bookOption) throws BookNbtOverflow {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;
        BookBuilder bookBuilder = bookOption.getBookBuilder()
                .author(author)
                .addPage(Text.literal(Formatting.BLUE + bookText)
                        .setStyle(Style.EMPTY
                                .withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(this.getText())))
                );

        ItemStack book = bookBuilder.get();
        assert book.getNbt() != null;

        long bookLength = FzmmUtils.getLength(book);
        if (bookLength > BookNbtOverflow.MAX_BOOK_NBT_SIZE)
            throw new BookNbtOverflow(bookLength);
        else
            FzmmUtils.giveItem(book);
    }

    public void giveBookPage(ImagetextBookOption bookOption) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        BookBuilder bookBuilder = bookOption.getBookBuilder();
        bookBuilder.addPage(this.getText());

        FzmmUtils.giveItem(bookBuilder.get());
    }

    public void addResolution() {
        String message = Text.translatable("imagetext.resolution", this.width, this.height).getString();
        int color = Integer.parseInt(FzmmClient.CONFIG.colors.imagetextMessages(), 16);
        Text text = Text.translatable(message)
                .setStyle(Style.EMPTY.withColor(color));
        this.imagetext.add(FzmmUtils.toNbtString(text, true));
    }

    public void giveAsHologram(int x, float y, int z) {
        final float Y_DISTANCE = 0.23f;
        ItemStack hopper = Items.HOPPER.getDefaultStack();
        NbtCompound hopperBlockEntityTag = new NbtCompound();
        NbtList hopperItems = new NbtList();
        NbtList shulkerItems = new NbtList();
        int color = Integer.parseInt(FzmmClient.CONFIG.colors.imagetextHologram(), 16);

        byte size = (byte) this.imagetext.size(),
                hopperIndex = 0;

        for (byte i = 0; i != size; i++) {
            y += Y_DISTANCE;
            ItemStack armorStandHologram = ArmorStandBuilder.builder()
                    .setPos(x, y, z)
                    .setTags("ImagetextHologram")
                    .setAsHologram(this.imagetext.get(size - i - 1).asString())
                    .getItem(String.valueOf(i));

            InventoryUtils.addSlot(shulkerItems, armorStandHologram, i % 27);
            if (i % 27 == 0 && i != 0) {
                addShulker(shulkerItems, hopperItems, hopperIndex);
                shulkerItems = new NbtList();
                hopperIndex++;
            }
        }

        if (shulkerItems.size() != 0)
            addShulker(shulkerItems, hopperItems, hopperIndex);

        hopperBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, hopperItems);
        hopper.setSubNbt(TagsConstant.BLOCK_ENTITY, hopperBlockEntityTag);
        hopper = DisplayBuilder.of(hopper)
                .addLore(x + " " + y + " " + z, color)
                .addLore("Imagetext: Hologram", color)
                .get();

        FzmmUtils.giveItem(hopper);
    }

    private void addShulker(NbtList shulkerItems, NbtList hopperItems, byte hopperIndex) {
        NbtCompound shulkerBlockEntityTag = new NbtCompound();
        ItemStack shulker = new ItemStack(Items.LIGHT_BLUE_SHULKER_BOX);
        shulkerBlockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, shulkerItems);
        shulker.setSubNbt(TagsConstant.BLOCK_ENTITY, shulkerBlockEntityTag);

        InventoryUtils.addSlot(hopperItems, shulker, hopperIndex);
    }

    public String getImagetextString() {
        return Text.Serializer.toJson(this.getText());
    }

    public NbtList get() {
        return this.imagetext;
    }

    public Text getText() {
        MutableText text = Text.empty();

        List<Text> textList = this.getTextList();
        int size = textList.size();
        for (int i = 0; i != size; i++) {
            text.append(textList.get(i));
            if (i != size - 1)
                text.append("\n");
        }

        return text;
    }

    public List<Text> getTextList() {
        List<Text> textList = new ArrayList<>();

        for (var line : this.imagetext) {
            textList.add(Text.Serializer.fromJson(line.asString()));
        }

        return textList;
    }

    public boolean isEmpty() {
        return this.imagetext.isEmpty();
    }

    public int getMaxImageWidthForBookPage(String characters) {
        int maxTextWidth = BookScreen.MAX_TEXT_WIDTH - 1;
        int width = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (characters.length() == 1)
            width = maxTextWidth / textRenderer.getWidth(characters);
        else {
            String message = "";
            int length = characters.length();
            do {
                message += characters.charAt(width % length);
                width++;
            } while (textRenderer.getWidth(message) < maxTextWidth);
        }

        return width;
    }

}