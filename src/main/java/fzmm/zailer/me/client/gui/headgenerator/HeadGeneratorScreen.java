package fzmm.zailer.me.client.gui.headgenerator;

import com.google.gson.JsonIOException;
import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.gui.ScreenConstants;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.logic.HeadGenerator;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadGeneratorScreen extends Screen {
    private HeadListWidget headListWidget;
    private HeadLayersListWidget headLayersWidget;
    private boolean initialized;
    private TextFieldWidget searchBox;
    private TextFieldWidget playerNameField;
    private ButtonWidget loadSkinButton;
    private ButtonWidget giveMergedHeadButton;
    private String currentSearch;
    private String playerName;
    private Set<String> headNames;

    public HeadGeneratorScreen() {
        super(Text.translatable("headGenerator"));
        this.initialized = false;
    }

    @Override
    protected void init() {
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
        int halfRowWidth = this.headLayersWidget.getRowWidth() / 2;

        String previousUsername = this.playerNameField != null ? this.playerNameField.getText() : "";
        this.playerNameField = new TextFieldWidget(this.textRenderer, halfRowWidth + halfWidth - 100, 50, 115, 20, Text.translatable("fzmm.gui.headGenerator.playerName"));
        this.playerNameField.setText(previousUsername);

        this.loadSkinButton = new ButtonWidget(halfRowWidth + halfWidth + 20, 50, 80, ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.translatable("fzmm.gui.headGenerator.loadSkin"), this::loadPlayerSkinExecute);

        String previousSearch = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, halfRowWidth + halfWidth - 100, 72, 200, 16, Text.translatable("gui.socialInteractions.search_hint"));
        this.searchBox.setText(previousSearch);
        this.searchBox.setChangedListener(this::onSearchChange);

        this.giveMergedHeadButton = new ButtonWidget(halfWidth + 72 - this.headLayersWidget.getRowWidth(), 50, 80, ScreenConstants.NORMAL_BUTTON_HEIGHT, Buttons.GIVE.getTranslation(), this::giveMergedHeadExecute);

        this.addSelectableChild(this.giveMergedHeadButton);
        this.addSelectableChild(this.playerNameField);
        this.addSelectableChild(this.loadSkinButton);
        this.addSelectableChild(this.searchBox);
        this.addSelectableChild(this.headListWidget);
        this.addSelectableChild(this.headLayersWidget);

        if (this.playerName == null)
            this.playerName = "";

        this.headNames = HeadGenerator.getHeadsNames();
        this.currentSearch = this.searchBox.getText();
        this.initialized = true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        if (!this.headListWidget.isEmpty()) {
            this.headListWidget.render(matrices, mouseX, mouseY, delta);
            this.headLayersWidget.render(matrices, mouseX, mouseY, delta);
            this.giveMergedHeadButton.render(matrices, mouseX, mouseY, delta);
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
            PlayerSkinDrawer.draw(matrices, halfWidth + 32 - this.headLayersWidget.getRowWidth(), 42, 32);
        }
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
}
