package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextCharactersAlgorithm implements IImagetextAlgorithm {
    private static final String CHARACTERS_ID = "characters";
    private TextFieldWidget charactersTextField;

    @Override
    public String getId() {
        return "algorithm.characters";
    }

    @Override
    public List<MutableText> get(ImagetextLogic logic, ImagetextData data, int lineSplitInterval) {
        BufferedImage image = logic.resizeImage(data.image(), data.width(), data.height(), data.smoothRescaling());
        List<MutableText> linesList = new ArrayList<>();

        String characters = this.charactersTextField.getText();
        if (characters == null || characters.isBlank())
            characters = ImagetextLine.DEFAULT_TEXT;

        for (int y = 0; y != data.height(); y++) {
            ImagetextLine line = new ImagetextLine(characters, data.percentageOfSimilarityToCompress(), lineSplitInterval);
            for (int x = 0; x != data.width(); x++) {
                line.add(image.getRGB(x, y));
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
    public void setupComponents(FlowLayout rootComponent) {
        this.charactersTextField = TextBoxRow.setup(rootComponent, CHARACTERS_ID, ImagetextLine.DEFAULT_TEXT, FzmmClient.CONFIG.imagetext.maxResolution());
        this.charactersTextField.setCursor(0);
    }

    @Override
    public IMementoObject createMemento() {
        return new CharactersAlgorithmMementoTab(this.charactersTextField.getText());
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        CharactersAlgorithmMementoTab memento = (CharactersAlgorithmMementoTab) mementoObject;
        this.charactersTextField.setText(memento.characters);
        this.charactersTextField.setCursor(0);
    }

    private record CharactersAlgorithmMementoTab(String characters) implements IMementoObject{
    }
}
