package fzmm.zailer.me.client.gui.list;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.GRAY_COLOR;
import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public class HeadGeneratorListEntry extends ElementListWidget.Entry<HeadGeneratorListEntry> {
    private final MinecraftClient client;
    private final String name;
    private final BufferedImage headBufferedImage;
    private final Identifier headIdentifier;
    private final ButtonWidget giveButton;
    private final HeadGeneratorListWidget parent;

    public HeadGeneratorListEntry(HeadGeneratorListWidget parent, MinecraftClient client, String name, BufferedImage headBufferedImage) {
        this.parent = parent;
        this.client = client;
        this.name = name;
        this.headBufferedImage = headBufferedImage;
        this.headIdentifier = this.getHeadIdentifier(headBufferedImage);
        this.giveButton = new ButtonWidget(0, 0, 60, ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.of(Buttons.GIVE.getText()), new GiveButtonListener(this));
    }

    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        int xWithPadding = x + 4;
        int lineHeight = y + (entryHeight - 24) / 2;
        int xText = xWithPadding + 24 + 4;
        DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, GRAY_COLOR);
        Objects.requireNonNull(this.client.textRenderer);
        int yText = y + (entryHeight - 9) / 2;

        RenderSystem.setShaderTexture(0, this.headIdentifier);
        PlayerSkinDrawer.draw(matrices, xWithPadding, lineHeight, 24);

        this.client.textRenderer.draw(matrices, this.name, (float) xText, (float) yText, WHITE_COLOR);

        this.giveButton.x = x + (entryWidth - this.giveButton.getWidth() - 4);
        this.giveButton.y = y + (entryHeight - this.giveButton.getHeight()) / 2;
        this.giveButton.render(matrices, mouseX, mouseY, tickDelta);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return ImmutableList.of(this.giveButton);
    }

    @Override
    public List<? extends Element> children() {
        return ImmutableList.of(this.giveButton);
    }

    private Identifier getHeadIdentifier(BufferedImage bufferedImage) {
        Identifier textureIdentifier = new Identifier(FzmmClient.MOD_ID, UUID.randomUUID().toString());
        try {
            FzmmUtils.saveBufferedImageAsIdentifier(bufferedImage, textureIdentifier);
        } catch (IOException ignored) {
        }
        return textureIdentifier;
    }

    private String getPlayerName() {
        return this.parent.getPlayerName();
    }

    private record GiveButtonListener(HeadGeneratorListEntry parent) implements ButtonWidget.PressAction {

        @Override
        public void onPress(ButtonWidget button) {
            new Thread(() -> {
                try {
                    this.parent.parent.updateList(false, "Wait...");

                    HeadUtils headUtils = new HeadUtils().uploadHead(this.parent.headBufferedImage, this.parent.name);
                    int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                    ItemStack head = headUtils.getHead(this.parent.getPlayerName());

                    FzmmUtils.giveItem(head);
                    this.parent.parent.setDelay(delay);
                } catch (IOException ignored) {
                }
            }).start();
        }
    }

    public void setEnabled(boolean value) {
        this.giveButton.active = value;
    }

    public void setButtonName(Text name) {
        this.giveButton.setMessage(name);
    }
}