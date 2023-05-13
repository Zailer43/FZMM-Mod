package fzmm.zailer.me.client.gui.components;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SuggestionTextBox extends FlowLayout {
    private static final int SUGGESTION_HEIGHT = 16;
    private static final int TEXT_BOX_HEIGHT = 22;
    private static final int SUGGESTION_MATCH_COLOR = Formatting.YELLOW.getColorValue() == null ? 0xFFFF55 : Formatting.YELLOW.getColorValue();
    private static final int SUGGESTION_NEW_COLOR = Formatting.GRAY.getColorValue() == null ? 0xAAAAAA : Formatting.GRAY.getColorValue();
    private SuggestionPosition suggestionPosition;
    private SuggestionProvider<?> suggestionProvider;
    @SuppressWarnings("UnstableApiUsage")
    private final ConfigTextBox textBox;
    private final FlowLayout suggestionsLayout;
    private final ScrollContainer<FlowLayout> suggestionsContainer;
    private boolean mouseHoverSuggestion;
    private int maxSuggestionLines;

    @SuppressWarnings("UnstableApiUsage")
    public SuggestionTextBox(Sizing horizontalSizing, SuggestionPosition position, int maxSuggestionLines) {
        super(horizontalSizing, Sizing.content(), Algorithm.VERTICAL);
        this.suggestionProvider = (nul, builder) -> CompletableFuture.completedFuture(builder.build());
        this.textBox = new ConfigTextBox();
        this.mouseHoverSuggestion = false;
        this.textBox.horizontalSizing(Sizing.fill(100));
        this.suggestionsLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        this.suggestionsContainer = Containers.verticalScroll(horizontalSizing, Sizing.fixed(0), this.suggestionsLayout);
        this.suggestionsContainer.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));
        this.child(this.textBox);

        this.setMaxSuggestionLines(maxSuggestionLines);
        this.setSuggestionPosition(position);

        this.textBox.onChanged().subscribe(this::onTextChanged);
        this.removeSuggestionOnLostFocusEvents();

        this.focusLost().subscribe(this.suggestionsLayout::clearChildren);
    }

    private void removeSuggestionOnLostFocusEvents() {
        this.textBox.focusGained().subscribe(source -> {
            // if you put the suggestionContainer in the root when there is a container everything seems
            // to work correctly, but it does not let you write in the text field of SuggestionTextBox,
            // so you have to do black magic, and if I do not put the suggestionContainer in the root
            // it overlaps other components making it not visible or selectable
            this.getSuggestionsContainerParent().ifPresent(flowLayout -> flowLayout.child(this.suggestionsContainer));

            // is necessary because otherwise the suggestion stays in the
            // wrong position because a memento was executed too early
            this.updateSuggestionsPos();
        });
        this.textBox.focusLost().subscribe(() -> {
            // we live in a society where if you click on a component first the focusLost is
            // sent and then the mouseDown so if that focusLost removes your component with
            // mouseDown it will never be called
            if (!this.mouseHoverSuggestion)
                ((FlowLayout) this.root()).removeChild(this.suggestionsContainer);
            this.mouseHoverSuggestion = false;
        });
        this.suggestionsContainer.mouseEnter().subscribe(() -> this.mouseHoverSuggestion = true);
        this.suggestionsContainer.mouseLeave().subscribe(() -> this.mouseHoverSuggestion = false);
        this.suggestionsContainer.focusGained().subscribe(source -> this.onClickSuggestion());
    }

    private Optional<FlowLayout> getSuggestionsContainerParent() {
        FlowLayout root = (FlowLayout) this.root();
        if (root == null)
            return Optional.empty();

        for (var child : root.children()) {
            if (child instanceof OverlayContainer<?> overlayContainer && overlayContainer.child() instanceof FlowLayout flowLayout)
                return Optional.of(flowLayout);
        }
        return Optional.of(root);
    }

    private void onTextChanged(String newMessage) {
        try {
            int messageLength = newMessage.length();
            String newMessageToLowerCase = newMessage.toLowerCase();
            List<Suggestion> suggestions = this.suggestionProvider.getSuggestions(null, new SuggestionsBuilder(newMessage, messageLength)).get().getList();
            this.suggestionsLayout.clearChildren();
            int maxHorizontalSizing = this.suggestionsLayout.width() - 10;

            for (int i = 0; i != suggestions.size(); i++) {
                this.tryAddSuggestion(suggestions.get(i).getText(), newMessageToLowerCase, maxHorizontalSizing);
            }

            this.updateSuggestionsPos();

        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SuggestionTextBox] Failed to get suggestions", e);
            this.suggestionsLayout.clearChildren();
            this.suggestionsLayout.child(Components.label(Text.literal("Failed to get suggestions")));
        }
    }

    private void tryAddSuggestion(String suggestion, String textBoxMessageToLowerCase, int maxHorizontalSizing) {
        int matchIndex = suggestion.toLowerCase().indexOf(textBoxMessageToLowerCase);

        if (matchIndex >= 0) {
            this.suggestionsLayout.child(this.getSuggestionComponent(suggestion, this.getSuggestionMessage(suggestion, textBoxMessageToLowerCase, matchIndex, maxHorizontalSizing)));
        }
    }

    private Text getSuggestionMessage(String suggestion, String textBoxMessageToLowerCase, int matchIndex, int maxHorizontalSizing) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int suggestionWidth = textRenderer.getWidth(suggestion);
        int startNewColorIndex = matchIndex + textBoxMessageToLowerCase.length();

        if (suggestionWidth > maxHorizontalSizing) {
            int suggestionLength = suggestion.length();
            String ellipsis = "...";
            maxHorizontalSizing -= textRenderer.getWidth(ellipsis);
            suggestion = ellipsis + textRenderer.trimToWidth(suggestion, maxHorizontalSizing, true);
            int difference = Math.abs(suggestionLength - suggestion.length());
            matchIndex -= difference;
            matchIndex = Math.max(0, matchIndex);
            startNewColorIndex -= difference;
            startNewColorIndex = Math.max(0, startNewColorIndex);
        }

        return Text.literal(suggestion.substring(0, matchIndex))
                .setStyle(Style.EMPTY.withColor(SUGGESTION_NEW_COLOR))
                .append(
                        Text.literal(suggestion.substring(matchIndex, startNewColorIndex))
                                .setStyle(Style.EMPTY.withColor(SUGGESTION_MATCH_COLOR))
                )
                .append(
                        Text.literal(suggestion.substring(startNewColorIndex))
                                .setStyle(Style.EMPTY.withColor(SUGGESTION_NEW_COLOR))
                );
    }

    // we live in a society where if you click on a component first the focusLost is
    // sent and then the mouseDown so if that focusLost removes your component with
    // mouseDown it will never be called
    private void onClickSuggestion() {
        this.update(0, 0, 0);
        this.mouseHoverSuggestion = false;
        this.suggestionsLayout.clearChildren();
        this.textBox.onFocusLost();
    }

    private Component getSuggestionComponent(String suggestion, Text suggestionText) {
        // ButtonComponent does not allow the text to be left aligned
        LabelComponent labelComponent = Components.label(suggestionText);
        FlowLayout layout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(SUGGESTION_HEIGHT));
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.textBox.text(suggestion);
            this.onClickSuggestion();
            return true;
        });

        int backgroundColor = 0xA0000000;
        layout.mouseEnter().subscribe(() -> layout.surface(Surface.flat(0xE0000000)));
        layout.mouseLeave().subscribe(() -> layout.surface(Surface.flat(backgroundColor)));

        layout.surface(Surface.flat(backgroundColor))
                .padding(Insets.horizontal(4))
                .verticalAlignment(VerticalAlignment.CENTER)
                .zIndex(300)
                .cursorStyle(CursorStyle.HAND);
        labelComponent.cursorStyle(CursorStyle.HAND);

        layout.child(labelComponent);


        return layout;
    }

    private int getSuggestionsHeight(int lines) {
        return (int) (SUGGESTION_HEIGHT * (lines + 0.5f));
    }

    private void updateSuggestionsPos() {
        int offset = switch (this.suggestionPosition) {
            case TOP ->
                    -this.getSuggestionsHeight(Math.min(this.suggestionsLayout.children().size(), this.maxSuggestionLines));
            case BOTTOM -> TEXT_BOX_HEIGHT;
        };

        this.getSuggestionsContainerParent().ifPresent(flowLayout -> {
            Insets padding =  flowLayout.padding().get();
            int x = this.x - flowLayout.x() - padding.left();
            int y = this.y - flowLayout.y() - padding.top();
            this.suggestionsContainer.positioning(Positioning.absolute(x, y + offset));
        });
    }


    @Override
    public void layout(Size space) {
        super.layout(space);
        this.updateSuggestionsPos();
    }

    public void setSuggestionPosition(SuggestionPosition position) {
        this.suggestionPosition = position;
        this.updateSuggestionsPos();
    }

    public void setMaxSuggestionLines(int maxSuggestionLines) {
        this.maxSuggestionLines = maxSuggestionLines;
        this.suggestionsContainer.verticalSizing(Sizing.fixed(this.getSuggestionsHeight(maxSuggestionLines)));
    }

    /**
     * Note: <b>CommandContext is always null</b>
     */
    public void setSuggestionProvider(SuggestionProvider<?> provider) {
        this.suggestionProvider = provider;
        this.suggestionsLayout.clearChildren();
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        return super.isInBoundingBox(x, y) || this.suggestionsContainer.isInBoundingBox(x, y);
    }

    @SuppressWarnings("UnstableApiUsage")
    public ConfigTextBox getTextBox() {
        return this.textBox;
    }

    @Override
    public Component horizontalSizing(Sizing horizontalSizing) {
        this.suggestionsContainer.horizontalSizing(horizontalSizing);
        return super.horizontalSizing(horizontalSizing);
    }

    public void visible(boolean visible) {
        this.textBox.visible = visible;
    }

    public enum SuggestionPosition {
        TOP,
        BOTTOM
    }
}
