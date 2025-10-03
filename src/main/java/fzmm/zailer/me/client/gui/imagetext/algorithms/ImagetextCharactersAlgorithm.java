package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;

import java.awt.image.BufferedImage;
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
        String palette = this.sanitize(this.charactersTextField.getText());
        this.palette = FzmmUtils.splitMessage(palette).toArray(new String[0]);
    }

    private String sanitize(String value) {
        return value.isBlank() ? ImagetextLine.DEFAULT_TEXT : value;
    }

    @Override
    public String getId() {
        return "algorithm.characters";
    }

    @Override
    public String[] linePixels(int line) {
        return this.palette;
    }

    @Override
    public String pixelExample() {
        return this.sanitize(this.charactersTextField.getText());
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
        this.charactersTextField.setCursorToStart(false);
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
        if (MinecraftClient.getInstance().currentScreen instanceof BaseFzmmScreen baseScreen) {
            parentLayout.removeChild(this.charactersTextField);

            List<Component> buttons = baseScreen.getSymbolChatCompat().getButtons(baseScreen, this.charactersTextField);
            for (var button : buttons) {
                button.sizing(Sizing.fixed(16));
            }
            parentLayout.children(buttons);

            parentLayout.child(this.charactersTextField);
        }
    }

    @Override
    public IMementoObject createMemento() {
        return new CharactersAlgorithmMementoTab(this.charactersTextField.getText());
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        CharactersAlgorithmMementoTab memento = (CharactersAlgorithmMementoTab) mementoObject;
        this.charactersTextField.text(memento.characters);
    }

    private record CharactersAlgorithmMementoTab(String characters) implements IMementoObject {
    }
}
