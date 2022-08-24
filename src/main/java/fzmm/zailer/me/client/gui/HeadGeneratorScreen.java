package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.list.HeadGeneratorListWidget;
import fzmm.zailer.me.client.logic.HeadGenerator;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

public class HeadGeneratorScreen extends Screen {
    private HeadGeneratorListWidget headListWidget;
    private boolean initialized;
    private TextFieldWidget searchBox;
    private TextFieldWidget playerNameField;
    private ButtonWidget loadSkinButton;
    private String currentSearch;
    private Set<String> headNames;

    protected HeadGeneratorScreen() {
        super(Text.of("headGenerator"));
        this.initialized = false;
    }

    @Override
    protected void init() {
        if (this.initialized) {
            this.headListWidget.updateSize(this.width, this.height, 88, this.height - 100);
        } else {
            this.headListWidget = new HeadGeneratorListWidget(this, this.client, this.width, this.height, 88, this.height - 80, 36);
        }

        String previousUsername = this.playerNameField != null ? this.playerNameField.getText() : "";
        this.playerNameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 50, 115, 20, Text.of("Player name"));
        this.playerNameField.setText(previousUsername);

        this.loadSkinButton = new ButtonWidget(this.width / 2 + 20, 50, 80, ScreenConstants.NORMAL_BUTTON_HEIGHT, Text.of("Load skin"), new LoadPlayerSkinPressAction(this));

        String previousSearch = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 72, 200, 16, Text.of("Search..."));
        this.searchBox.setText(previousSearch);
        this.searchBox.setChangedListener(this::onSearchChange);

        this.addSelectableChild(this.searchBox);
        this.addSelectableChild(this.playerNameField);
        this.addSelectableChild(this.headListWidget);
        this.addSelectableChild(this.loadSkinButton);

        this.headNames = HeadGenerator.getHeadsNames();
        this.initialized = true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.headListWidget.isEmpty()) {
            this.headListWidget.render(matrices, mouseX, mouseY, delta);
        }

        this.loadSkinButton.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.playerNameField.render(matrices, mouseX, mouseY, delta);

        if (this.playerNameField.getText().isEmpty() && !this.playerNameField.isFocused())
            this.textRenderer.drawWithShadow(matrices, this.playerNameField.getMessage(), this.playerNameField.x + 2, this.playerNameField.y + 4, 0xDDDDDD);

        if (this.searchBox.getText().isEmpty() && !this.searchBox.isFocused())
            this.textRenderer.drawWithShadow(matrices, this.searchBox.getMessage(), this.searchBox.x + 2, this.searchBox.y + 4, 0xDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || this.headListWidget.mouseClicked(mouseX, mouseY, button);
    }

    private void onSearchChange(String currentSearch) {
        currentSearch = currentSearch.toLowerCase();

        if (!currentSearch.equals(this.currentSearch)) {
            this.headListWidget.filter(currentSearch);
            this.currentSearch = currentSearch;
        }
    }

    private record LoadPlayerSkinPressAction(HeadGeneratorScreen parent) implements ButtonWidget.PressAction {

        @Override
        public void onPress(ButtonWidget button) {
            String playerUsername = this.parent.playerNameField.getText();
            try {
                BufferedImage playerSkin = FzmmUtils.getPlayerSkin(playerUsername);

                this.parent.headListWidget.updatePreview(playerSkin);
                this.parent.headListWidget.filter(this.parent.searchBox.getText());
            } catch (IOException ignored) {
            }
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
}
