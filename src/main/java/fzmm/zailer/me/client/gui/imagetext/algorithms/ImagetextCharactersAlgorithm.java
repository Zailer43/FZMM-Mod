package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.utils.TextUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImagetextCharactersAlgorithm implements IImagetextAlgorithm {
    private static final String CHARACTERS_ID = "characters";
    private SuggestionTextBox charactersTextField;
    private BufferedImage image = null;
    private String[] palette = null;

    @Override
    public BufferedImage image() {
        return this.image;
    }

    @Override
    public void image(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void build() {
        this.updatePalette();
    }

    private void updatePalette() {
        String palette = this.sanitize(this.charactersTextField.getValue());
        this.palette = TextUtils.splitMessage(palette).toArray(new String[0]);
    }

    private String sanitize(String value) {
        return value.isBlank() ? ImagetextLine.DEFAULT_TEXT : value;
    }

    @Override
    public String getId() {
        return "characters";
    }

    @Override
    public String[] linePixels(int line) {
        return this.palette;
    }

    @Override
    public String pixelExample() {
        return this.sanitize(this.charactersTextField.getValue());
    }

    @Override
    public float widthRatio() {
        return 1f;
    }

    @Override
    public float heightRatio() {
        return 1f;
    }

    @Override
    public void setUpdatePreviewCallback(Runnable callback) {
        this.charactersTextField.onChanged().subscribe(value -> callback.run());
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.charactersTextField = (SuggestionTextBox) TextBoxRow.setup(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT, FzmmClient.CONFIG.imagetext.maxResolution());
        this.charactersTextField.moveCursorToStart(false);
        this.charactersTextField.setSuggestionProvider((nul, builder) -> {
            if (builder.getInput().isBlank()) {
                List<String> suggestions = List.of(ImagetextLine.DEFAULT_TEXT, "▎", "▋", "☐", "🌑");
                for (var suggestion : suggestions) {
                    builder.suggest(suggestion);
                }
            }

            return CompletableFuture.completedFuture(builder.build());
        });
        this.charactersTextField.enableFontProcess(true);

        FlowLayout parentLayout = rootComponent.childByIdOrThrow(FlowLayout.class, TextBoxRow.getTextBoxId(CHARACTERS_ID) + "-parent");
        if (Minecraft.getInstance().gui.screen() instanceof BaseFzmmScreen baseScreen) {
            parentLayout.removeChild(this.charactersTextField);

            List<UIComponent> buttons = baseScreen.getSymbolChatCompat().getButtons(baseScreen, this.charactersTextField);
            for (var button : buttons) {
                button.sizing(Sizing.fixed(16));
            }
            parentLayout.children(buttons);

            parentLayout.child(this.charactersTextField);
        }
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.charactersTextField.getValue());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.charactersTextField.text((String) input.readObject());
    }
}
