package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ImagetextLine {
    public static final String DEFAULT_TEXT = "█";
    private final ArrayList<ImagetextLineComponent> line;
    private final List<String> charactersToUse;
    private final boolean isDefaultText;
    private final double percentageOfSimilarityToCompress;

    public ImagetextLine(String charactersToUse, double percentageOfSimilarityToCompress) {
        this.line = new ArrayList<>();
        this.charactersToUse = FzmmUtils.splitMessage(charactersToUse);
        this.isDefaultText = charactersToUse.equals(DEFAULT_TEXT);
        this.percentageOfSimilarityToCompress = percentageOfSimilarityToCompress;
    }

    public ImagetextLine add(int color) {
        int size = this.line.size();
        if (size > 0) {
            if (this.line.get(size - 1).tryAdd(color, this.percentageOfSimilarityToCompress))
                return this;
        }

        this.line.add(new ImagetextLineComponent(color));
        return this;
    }

    public MutableText getLine() {
        MutableText lineList = Text.empty().setStyle(Style.EMPTY.withItalic(false));
        short lineIndex = 0;
        for (var lineComponent : this.line) {
            int color = lineComponent.getColor();
            int alpha = (color >> 24) & 0xFF;
            int repetitions = lineComponent.getRepetitions();
            Text line = this.isDefaultText && alpha == 0 ? lineComponent.getEmptyText() : lineComponent.getText(this.charactersToUse, lineIndex);
            lineIndex += repetitions;
            lineList.append(line);
        }
        return lineList;
    }
}
