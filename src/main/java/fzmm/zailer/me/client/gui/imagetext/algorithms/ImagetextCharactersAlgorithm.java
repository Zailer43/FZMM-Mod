package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImagetextCharactersAlgorithm implements IImagetextAlgorithm {
    private static final String CHARACTERS_ID = "characters";
    private SuggestionTextBox charactersTextField;
    private BufferedImage image = null;

    @Override
    public String getId() {
        return "algorithm.characters";
    }

    @Override
    public List<MutableText> get(ImagetextLogic logic, ImagetextData data, int lineSplitInterval) {
        this.cacheResizedImage(data);
        List<MutableText> linesList = new ArrayList<>();

        String characters = this.charactersTextField.getText();
        if (characters == null || characters.isBlank())
            characters = ImagetextLine.DEFAULT_TEXT;

        for (int y = 0; y != data.height(); y++) {
            ImagetextLine line = new ImagetextLine(characters, data.percentageOfSimilarityToCompress(), lineSplitInterval);
            for (int x = 0; x != data.width(); x++) {
                line.add(this.image.getRGB(x, y));
            }

            linesList.addAll(line.getLineComponents());
        }

        return linesList;
    }

    @Override
    public String getCharacters() {
        return this.charactersTextField.getText();
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
    public void cacheResizedImage(ImagetextData data) {
        if (this.image == null || this.image.getWidth() != data.width() || this.image.getHeight() != data.height()) {
            this.clearCache();
            this.image = ImageUtils.fastResizeImage(data.image(), data.width(), data.height(), data.smoothRescaling());
        }
    }

    @Override
    public void clearCache() {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.charactersTextField = (SuggestionTextBox) TextBoxRow.setup(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT, FzmmClient.CONFIG.imagetext.maxResolution());
        this.charactersTextField.setCursorToStart(false);
        this.charactersTextField.setSuggestionProvider((nul, builder) -> {
            if (builder.getInput().isBlank()) {
                builder.suggest(ImagetextLine.DEFAULT_TEXT);

                List<String> suggestions = List.of("▎", "▋", "☐", "🌑");
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

    private record CharactersAlgorithmMementoTab(String characters) implements IMementoObject{
    }
}
