package fzmm.zailer.me.client.gui.headgenerator;

import com.google.gson.JsonIOException;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.logic.HeadGenerator;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.compress.utils.Lists;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends GuiBase {
    private static final Path HEAD_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), "fzmm", "heads");
    private HeadListWidget headListWidget;
    private HeadLayersListWidget headLayersWidget;
    private boolean initialized;
    private TextFieldWidget searchBox;
    private TextFieldWidget playerNameField;
    private ButtonWidget loadSkinButton;
    private ButtonWidget giveMergedHeadButton;
    private ButtonWidget saveSkinButton;
    private ButtonWidget openFolderButton;
    private String currentSearch;
    private String playerName;
    private Set<String> headNames;

    public HeadGeneratorScreen(Screen parent) {
        super();
        this.initialized = false;
        this.setTitle(Text.translatable("fzmm.gui.title.headGenerator").getString());
        this.setParent(parent);
    }

    @Override
    public void initGui() {
        super.initGui();
        int halfWidth = this.width / 2;
        if (this.initialized) {
            this.headListWidget.updateSize(halfWidth, this.height, 88, this.height - 80);
            this.headLayersWidget.updateSize(halfWidth, this.height, 88, this.height - 80);
        } else {
            this.headListWidget = new HeadListWidget(this, this.client, halfWidth, this.height, 88, this.height - 80, 36);
            this.headLayersWidget = new HeadLayersListWidget(this.client, halfWidth, this.height, 88, this.height - 80, 36);
        }
        this.headListWidget.setLeftPos(halfWidth);
        this.headLayersWidget.setLeftPos(0);
        int halfRowWidth = this.headListWidget.getRowWidth() / 2;
        int headListLeft = this.headListWidget.getLeft() + 1;
        int headListRowWidth = this.headListWidget.getRowWidth();

        String previousUsername = this.playerNameField != null ? this.playerNameField.getText() : "";
        this.playerNameField = new TextFieldWidget(this.textRenderer, headListLeft, 45, halfRowWidth, 20, Text.translatable("fzmm.gui.headGenerator.playerName"));
        this.playerNameField.setText(previousUsername);

        this.loadSkinButton = new ButtonWidget(headListLeft + halfRowWidth + 4, 45, Buttons.LOAD_SKIN.getWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.LOAD_SKIN.getTranslation(), this::loadPlayerSkinExecute);

        String previousSearch = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, headListLeft, 68, headListRowWidth, 20, Text.translatable("gui.socialInteractions.search_hint"));
        this.searchBox.setText(previousSearch);
        this.searchBox.setChangedListener(this::onSearchChange);

        int buttonsY = this.headLayersWidget.getBottom() + 4;
        this.giveMergedHeadButton = new ButtonWidget(this.headLayersWidget.getLeft(), buttonsY, Buttons.GIVE.getWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.GIVE.getTranslation(), this::giveMergedHeadExecute);
        this.saveSkinButton = new ButtonWidget(this.giveMergedHeadButton.x + this.giveMergedHeadButton.getWidth() + 4, buttonsY, Buttons.HEAD_GENERATOR_SAVE_SKIN.getWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.HEAD_GENERATOR_SAVE_SKIN.getTranslation(), this::saveSkinExecute);
        this.openFolderButton = new ButtonWidget(this.saveSkinButton.x + this.saveSkinButton.getWidth() + 4, buttonsY, Buttons.HEAD_GENERATOR_OPEN_HEADS_FOLDER.getWidth(), ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.HEAD_GENERATOR_OPEN_HEADS_FOLDER.getTranslation(), this::openFolderExecute);

        this.addSelectableChild(this.playerNameField);
        this.addSelectableChild(this.loadSkinButton);
        this.addSelectableChild(this.searchBox);
        this.addSelectableChild(this.headLayersWidget);
        this.addSelectableChild(this.headListWidget);
        this.addSelectableChild(this.giveMergedHeadButton);
        this.addSelectableChild(this.saveSkinButton);
        this.addSelectableChild(this.openFolderButton);

        if (this.playerName == null)
            this.playerName = "";

        this.headNames = HeadGenerator.getHeadsNames();
        this.currentSearch = this.searchBox.getText();

        this.addButton(Buttons.BACK.getToLeft(this.width - 30, this.height - 40),
                (button, mouseButton) -> GuiBase.openGui(this.getParent()));

        this.initialized = true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (!this.headListWidget.isEmpty()) {
            this.headListWidget.render(matrices, mouseX, mouseY, delta);
            this.headLayersWidget.render(matrices, mouseX, mouseY, delta);
            this.giveMergedHeadButton.render(matrices, mouseX, mouseY, delta);
            this.saveSkinButton.render(matrices, mouseX, mouseY, delta);
            this.openFolderButton.render(matrices, mouseX, mouseY, delta);
        }
        int halfWidth = this.width / 2;
        int halfRowWidth = this.headLayersWidget.getRowWidth() / 2;

        this.loadSkinButton.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.playerNameField.render(matrices, mouseX, mouseY, delta);

        if (this.playerNameField.getText().isEmpty() && !this.playerNameField.isFocused())
            this.textRenderer.drawWithShadow(matrices, this.playerNameField.getMessage(), this.playerNameField.x + 2, this.playerNameField.y + 4, 0xDDDDDD);

        if (this.searchBox.getText().isEmpty() && !this.searchBox.isFocused())
            this.textRenderer.drawWithShadow(matrices, this.searchBox.getMessage(), this.searchBox.x + 2, this.searchBox.y + 4, 0xDDDDDD);

        Text numberOfResults = Text.translatable("fzmm.gui.headGenerator.numberOfResults", this.headListWidget.size());
        int numberOfResultsPosX = halfWidth - this.textRenderer.getWidth(numberOfResults) / 2 + halfRowWidth;
        this.textRenderer.drawWithShadow(matrices, numberOfResults, numberOfResultsPosX, 32, ScreenConstants.TEXT_COLOR);

        Identifier mergedHead = this.headLayersWidget.getMergedHeadIdentifier();
        if (mergedHead != null) {
            RenderSystem.setShaderTexture(0, mergedHead);
            DrawableHelper.drawTexture(matrices, halfWidth - this.headLayersWidget.getRowWidth(), this.headLayersWidget.getTop() - 36, 128, 32, 0, 0, 32, 16, 64, 64);
            RenderSystem.enableBlend();
            DrawableHelper.drawTexture(matrices, halfWidth - this.headLayersWidget.getRowWidth(), this.headLayersWidget.getTop() - 36, 128, 32, 32, 0,  32, 16, 64, 64);
            RenderSystem.disableBlend();
        }

        this.drawGuiMessages(matrices);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button)
                || this.headListWidget.mouseClicked(mouseX, mouseY, button)
                || this.headLayersWidget.mouseClicked(mouseX, mouseY, button);
    }

    private void onSearchChange(String currentSearch) {
        currentSearch = currentSearch.toLowerCase();

        if (!currentSearch.equals(this.currentSearch)) {
            this.headListWidget.filter(currentSearch);
            this.currentSearch = currentSearch;
        }
    }

    public void loadPlayerSkinExecute(ButtonWidget button) {
        String playerUsername = this.playerNameField.getText();
        try {
            BufferedImage playerSkin = FzmmUtils.getPlayerSkin(playerUsername);
            if (playerSkin == null)
                return;
            this.playerName = playerUsername;
            this.headLayersWidget.setBaseSkin(playerSkin);

            this.headListWidget.updatePreview(playerSkin);
            this.headListWidget.filter(this.searchBox.getText());
        } catch (IOException | JsonIOException e) {
            e.printStackTrace();
        }
    }

    public void giveMergedHeadExecute(ButtonWidget button) {
        this.execute(this.headLayersWidget.getMergedHeadImage());
    }

    public void execute(BufferedImage image) {
        new Thread(() -> {
            try {
                this.setUndefinedDelay();
                String playerName = this.getPlayerName();
                if (playerName == null)
                    playerName = "NULL";

                HeadUtils headUtils = new HeadUtils().uploadHead(image, playerName);
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                ItemStack head = headUtils.getHead(playerName);
                FzmmUtils.giveItem(head);
                this.setDelay(delay);
            } catch (IOException ignored) {
            }
        }, "Head generator").start();
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButtons(this.getGiveButtons(), waitMessage, false);
    }

    private List<ButtonWidget> getGiveButtons() {
        List<ButtonWidget> buttonList = Lists.newArrayList();
        buttonList.add(this.giveMergedHeadButton);
        buttonList.addAll(this.headListWidget.getGiveButtons());

        return buttonList;
    }

    public void setDelay(int seconds) {
        List<ButtonWidget> buttonList = this.getGiveButtons();

        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButtons(buttonList, message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> this.updateButtons(buttonList, Buttons.GIVE.getTranslation(), true));
    }

    public void updateButtons(List<ButtonWidget> buttonList, Text message, boolean active) {
        for (var button : buttonList) {
            button.active = active;
            button.setMessage(message);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.playerNameField.tick();
        this.searchBox.tick();
    }

    public String getPlayerName() {
        return this.playerNameField.getText();
    }

    public Set<String> getHeadNames() {
        return this.headNames;
    }

    public void addLayer(HeadEntry headEntry) {
        HeadLayerEntry entry = new HeadLayerEntry(this.headLayersWidget, this.client, headEntry.getName(), headEntry.getPreviewImage(), headEntry.getHeadTexture());
        this.headLayersWidget.add(entry);
    }

    public void saveSkinExecute(ButtonWidget button) {
        BufferedImage skin = this.headLayersWidget.getMergedHeadImage();
        if (skin == null) {
            InfoUtils.showGuiMessage(Message.MessageType.ERROR, "fzmm.gui.headGenerator.saveSkin.thereIsNoSkin");
            return;
        }
        this.addBody(skin);
        File file = HEAD_SAVE_FOLDER_PATH.toFile();
        if (file.mkdirs())
            FzmmClient.LOGGER.info("Head save folder created");

        file = ScreenshotRecorder.getScreenshotFilename(HEAD_SAVE_FOLDER_PATH.toFile());
        try {
            ImageIO.write(skin, "png", file);
            InfoUtils.showGuiMessage(Message.MessageType.SUCCESS, "fzmm.gui.headGenerator.saveSkin.saved");
        } catch (IOException e) {
            e.printStackTrace();
            InfoUtils.showGuiMessage(Message.MessageType.ERROR, "fzmm.gui.headGenerator.saveSkin.saveError");
        }
    }

    private void addBody(BufferedImage head) {
        Graphics2D g2d = head.createGraphics();
        g2d.drawImage(this.headLayersWidget.getBaseSkin(), 0, 16, 64, 64, 0, 16, 64, 64, null);
        g2d.dispose();
    }

    public void openFolderExecute(ButtonWidget button) {
        Util.getOperatingSystem().open(HEAD_SAVE_FOLDER_PATH.toFile());
    }
}
