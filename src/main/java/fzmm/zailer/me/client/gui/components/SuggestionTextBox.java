package fzmm.zailer.me.client.gui.components;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.compat.symbol_chat.components.FontTextBoxComponent;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO: Refactor this spaghetti
public class SuggestionTextBox extends FontTextBoxComponent {
    private static final int SUGGESTION_HEIGHT = 16;
    private final SuggestionPosition suggestionPosition;
    private SuggestionProvider<?> suggestionProvider;
    private int maxSuggestionLines;
    private int selectedSuggestionIndex = -1;
    private boolean disableCallback = false;
    @Nullable
    private Runnable suggestionSelectedCallback = null;
    @Nullable
    private DropdownComponent suggestionsContextMenu;
    @Nullable
    private ScrollContainer<FlowLayout> suggestionsContainer = null;
    @Nullable
    private FlowLayout suggestionsLayout = null;

    public SuggestionTextBox() {
        this(Sizing.content(), SuggestionPosition.BOTTOM, 5);
    }

    public SuggestionTextBox(Sizing horizontalSizing, SuggestionPosition position, int maxSuggestionLines) {
        super(horizontalSizing);
        this.suggestionProvider = (nul, builder) -> CompletableFuture.completedFuture(builder.build());
        this.suggestionPosition = position;

        this.setMaxSuggestionLines(maxSuggestionLines);
        this.onChanged().subscribe(this::updateSuggestions);
    }

    private void openContextMenu() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        ParentComponent root = this.root();
        if (this.contextMenuIsOpen() || screen == null || !(root instanceof FlowLayout rootLayout)) {
            return;
        }

        this.suggestionsContextMenu = DropdownComponent.openContextMenu(screen, rootLayout, FlowLayout::child, this.x(), this.y(), suggestionDropdown -> {
            suggestionDropdown.clearChildren();

            this.suggestionsLayout = new EFlowLayout(Sizing.fill(100), Sizing.content(), FlowLayout.Algorithm.VERTICAL) {
                // when some components, like snack bars, are removed, the position of
                // the context menu changes to Y 0 for some reason if suggestion is in an overlay
                @Override
                public void updateX(int x) {
                    super.updateX(x);
                    SuggestionTextBox.this.updateSuggestionsPos();
                }

                @Override
                public void updateY(int y) {
                    super.updateY(y);
                    SuggestionTextBox.this.updateSuggestionsPos();
                }
            };
            this.suggestionsContainer = EContainers.verticalScroll(Sizing.fixed(this.width()),
                    Sizing.expand(100), this.suggestionsLayout);


            suggestionDropdown.child(this.suggestionsContainer);
        });

        this.suggestionsContextMenu.zIndex(this.zIndex() + 100);
        this.updateSuggestions(this.getText());
    }

    private void closeContextMenu() {
        if (this.suggestionsLayout != null) {
            this.suggestionsLayout.clearChildren();
        }

        if (this.suggestionsContextMenu != null) {
            this.suggestionsContextMenu.remove();
        }

        this.suggestionsLayout = null;
        this.suggestionsContainer = null;
        this.suggestionsContextMenu = null;
    }

    private boolean contextMenuIsOpen() {
        return this.suggestionsContextMenu != null &&
                this.suggestionsContextMenu.hasParent() &&
                this.suggestionsContainer != null &&
                this.suggestionsLayout != null;
    }

    private boolean updateSelectedSuggestionIndex(int addIndex) {
        return this.updateSelectedSuggestionIndex(this.selectedSuggestionIndex, this.selectedSuggestionIndex + addIndex);
    }

    private boolean updateSelectedSuggestionIndex(int currentIndex, int newIndex) {
        if (this.suggestionsLayout == null || this.suggestionsContainer == null) {
            return false;
        }

        List<Component> children = this.suggestionsLayout.children();
        int childrenSize = children.size();

        if (currentIndex >= 0 && childrenSize > currentIndex) {
            children.get(currentIndex).onFocusLost();
        }

        // cycles when it moves out of bounds
        if (newIndex < 0) {
            newIndex = childrenSize - 1;
        } else if (newIndex >= childrenSize) {
            newIndex = 0;
        }

        if (childrenSize > newIndex) {
            Component selectedComponent = children.get(newIndex);
            selectedComponent.onFocusGained(FocusSource.MOUSE_CLICK);
            this.suggestionsContainer.scrollTo(selectedComponent);
        }

        this.selectedSuggestionIndex = newIndex;
        return true;
    }

    private void updateSuggestions(String newMessage) {
        if (!this.contextMenuIsOpen()) {
            this.openContextMenu();
            return;
        }
        assert this.suggestionsLayout != null;
        this.suggestionsLayout.clearChildren();

        String newMessageToLowerCase = newMessage.toLowerCase();
        List<Suggestion> suggestions = this.getSuggestions(newMessage);
        int maxHorizontalSizing = this.suggestionsLayout.width() - 10;

        for (int i = 0; i != suggestions.size(); i++) {
            String suggestion = suggestions.get(i).getText();
            int matchIndex = suggestion.toLowerCase().indexOf(newMessageToLowerCase);

            if (matchIndex >= 0 && this.suggestionsLayout != null) {
                Text suggestionMessage = this.getSuggestionMessage(suggestion, newMessageToLowerCase, matchIndex, maxHorizontalSizing);
                this.suggestionsLayout.child(this.getSuggestionComponent(suggestion, suggestionMessage));
            }
        }

        if (this.suggestionsContextMenu != null) {
            this.suggestionsContextMenu.verticalSizing(Sizing.fixed(this.getMaxSuggestionsHeight(suggestions.size())));
        }
    }

    private List<Suggestion> getSuggestions(String message) {
        try {
            return this.suggestionProvider.getSuggestions(null, new SuggestionsBuilder(message, 0)).get().getList();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[SuggestionTextBox] Failed to get suggestions", e);
            assert this.suggestionsLayout != null;

            this.suggestionsLayout.child(EComponents.label(Text.literal("Failed to get suggestions")));
        }

        return new ArrayList<>();
    }

    private Text getSuggestionMessage(String suggestion, String textBoxMessageToLowerCase, int matchIndex, int maxHorizontalSizing) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int startNewColorIndex = matchIndex + textBoxMessageToLowerCase.length();

        if (textRenderer.getWidth(suggestion) > maxHorizontalSizing) {
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
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                .append(
                        Text.literal(suggestion.substring(matchIndex, startNewColorIndex))
                                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                ).append(
                        Text.literal(suggestion.substring(startNewColorIndex))
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                );
    }

    private Component getSuggestionComponent(String suggestion, Text suggestionText) {
        LabelComponent labelComponent = EComponents.label(suggestionText);
        EFlowLayout layout = EContainers.verticalFlow(Sizing.fill(100), Sizing.fixed(SUGGESTION_HEIGHT));

        Surface selectedSurface = Surface.flat(0xE0000000).and(Surface.outline(0xA0FFFFFF));
        Surface unselectedSurface = Surface.flat(0xA0000000);

        layout.focusGained().subscribe(source -> layout.surface(Surface.BLANK));
        layout.hoveredSurface(selectedSurface);
        layout.focusLost().subscribe(() -> layout.surface(unselectedSurface));
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.text(suggestion);
            if (this.suggestionSelectedCallback != null && !this.disableCallback) {
                this.suggestionSelectedCallback.run();
            }

            this.closeContextMenu();
            return true;
        });
        layout.surface(unselectedSurface)
                .verticalAlignment(VerticalAlignment.CENTER)
                .cursorStyle(CursorStyle.HAND);

        labelComponent.cursorStyle(CursorStyle.HAND)
                .margins(Insets.horizontal(4));

        return layout.child(labelComponent);
    }

    private int getMaxSuggestionsHeight(int lines) {
        return Math.min(this.getSuggestionsHeight(lines), this.getSuggestionsHeight(this.maxSuggestionLines));
    }

    private int getSuggestionsHeight(int lines) {
        float totalLines = lines + (this.maxSuggestionLines > lines ? 0f : 0.5f);
        return (int) (SUGGESTION_HEIGHT * totalLines);
    }

    protected void updateSuggestionsPos() {
        if (this.suggestionsLayout == null || this.suggestionsContextMenu == null) {
            return;
        }

        int offset = switch (this.suggestionPosition) {
            case TOP -> -this.getMaxSuggestionsHeight(this.suggestionsLayout.children().size());
            case BOTTOM -> this.height();
        };
        int x = this.x();
        int y = this.y() + offset;

        assert this.suggestionsContextMenu != null;
        if (this.suggestionsContextMenu.x() != x) {
            this.suggestionsContextMenu.updateX(x);
        }
        if (this.suggestionsContextMenu.y() != y) {
            this.suggestionsContextMenu.updateY(y);
        }
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        this.updateSuggestionsPos();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        this.updateSuggestionsPos();
    }

    public void setMaxSuggestionLines(int maxSuggestionLines) {
        this.maxSuggestionLines = maxSuggestionLines;
        if (this.suggestionsContextMenu != null) {
            this.suggestionsContextMenu.verticalSizing(Sizing.fixed(this.getMaxSuggestionsHeight(maxSuggestionLines)));
        }
    }

    /**
     * Note: <b>CommandContext is always null</b>
     */
    public void setSuggestionProvider(SuggestionProvider<?> provider) {
        this.suggestionProvider = provider;
        if (this.suggestionsLayout != null) {
            this.suggestionsLayout.clearChildren();
        }
    }

    public void setSuggestionSelectedCallback(@Nullable Runnable suggestionSelectedCallback) {
        this.suggestionSelectedCallback = suggestionSelectedCallback;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = switch (keyCode) {
            case GLFW.GLFW_KEY_TAB:
                if (!this.contextMenuIsOpen()) {
                    this.openContextMenu();
                    yield true;
                }
                assert this.suggestionsLayout != null;
                if (this.suggestionsLayout.children().isEmpty()) {
                    this.updateSuggestions(this.getText());
                    yield !this.suggestionsLayout.children().isEmpty();
                } else {
                    yield this.updateSelectedSuggestionIndex(1);
                }
            case GLFW.GLFW_KEY_DOWN:
                yield this.updateSelectedSuggestionIndex(1);
            case GLFW.GLFW_KEY_UP:
                yield this.updateSelectedSuggestionIndex(-1);
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER:
                if (this.suggestionsLayout == null) {
                    yield false;
                }

                List<Component> children = this.suggestionsLayout.children();
                if (this.selectedSuggestionIndex >= 0 && this.selectedSuggestionIndex < children.size()) {
                    this.disableCallback = true;
                    Component selectedComponent = children.get(this.selectedSuggestionIndex);
                    selectedComponent.onMouseDown(selectedComponent.x(), selectedComponent.y(), GLFW.GLFW_MOUSE_BUTTON_1);
                    this.disableCallback = false;
                    yield true;
                }
            case GLFW.GLFW_KEY_ESCAPE:
                boolean contextMenuIsOpen = this.contextMenuIsOpen();
                this.closeContextMenu();
                yield contextMenuIsOpen;
            default:
                yield false;
        };

        return result || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public enum SuggestionPosition {
        TOP,
        BOTTOM
    }
}
