package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImagetextLine {
    public static final String DEFAULT_TEXT = "█";
    private final ArrayList<ImagetextLineComponent> line;
    private final List<String> charactersToUse;
    private final boolean isDefaultText;
    private final double percentageOfSimilarityToCompress;
    private final int splitLineEvery;
    private int lineLength;
    @Nullable
    private List<MutableText> generatedLine;

    public ImagetextLine(String charactersToUse, double percentageOfSimilarityToCompress, int splitLineEvery) {
        this.line = new ArrayList<>();
        this.charactersToUse = FzmmUtils.splitMessage(charactersToUse);
        this.isDefaultText = charactersToUse.equals(DEFAULT_TEXT);
        this.percentageOfSimilarityToCompress = percentageOfSimilarityToCompress;
        this.splitLineEvery = splitLineEvery;
        this.lineLength = 0;
        this.generatedLine = null;
    }

    public ImagetextLine add(int color) {
        int size = this.line.size();
        ImagetextLineComponent lastComponent = size > 0 ? this.line.get(size - 1) : null;

        if (this.shouldSplitLine(this.lineLength) || lastComponent == null || !lastComponent.tryAdd(color, this.percentageOfSimilarityToCompress)) {
            this.line.add(new ImagetextLineComponent(color));
        }

        this.lineLength++;
        return this;
    }

    public void generateLine() {
        List<MutableText> lineList = new ArrayList<>();
        MutableText line = Text.empty().setStyle(Style.EMPTY.withItalic(false));
        short lineIndex = 0;

        int lineComponentSize = this.line.size();
        for (int i = 0; i != lineComponentSize; i++) {
            ImagetextLineComponent lineComponent = this.line.get(i);
            int repetitions = lineComponent.getRepetitions();
            Text lineComponentText = lineComponent.getText(this.charactersToUse, lineIndex, this.isDefaultText);
            lineIndex += repetitions;
            line.append(lineComponentText);

            if (this.shouldSplitLine(lineIndex)) {
                lineList.add(line);
                line = Text.empty().setStyle(Style.EMPTY.withItalic(false));
            } else if (lineComponentSize - 1 == i) {
                lineList.add(line);
            }
        }
        this.generatedLine = lineList;
    }

    public List<MutableText> getLine() {
        if (this.generatedLine == null)
            this.generateLine();

        return this.generatedLine;
    }

    public int getLineWidth() {
        if (this.generatedLine == null)
            this.generateLine();

        MutableText lineText = Text.empty();

        for (var lineComponent : this.generatedLine)
            lineText.append(lineComponent);

        return MinecraftClient.getInstance().textRenderer.getWidth(lineText);
    }



    private boolean shouldSplitLine(int index) {
        return index != 0 && (index % this.splitLineEvery == 0);
    }
}
