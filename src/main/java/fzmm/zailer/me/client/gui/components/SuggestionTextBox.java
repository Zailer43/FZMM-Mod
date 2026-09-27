package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.context_menu.*;
import fzmm.zailer.me.compat.symbol_chat.components.FontTextBoxComponent;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SuggestionTextBox extends FontTextBoxComponent {
    public static final IProvider<?> EMPTY_SUGGESTIONS = (IProvider<Object>) input -> List.of();
    private static final int SUGGESTION_HEIGHT = 18;
    private final int maxSuggestionLines = 5;
    @Nullable
    private SuggestionTextBox.IProvider<?> suggestionProvider = null;
    @Nullable
    private Runnable selectedCallback = null;
    protected CtxMenuManager<String> contextMenu = new CtxMenuManager<>(new ArrayList<>(), o -> null);
    private CompletableFuture<Boolean> suggestionFuture = CompletableFuture.completedFuture(false);
    private String previousValue = "";

    public SuggestionTextBox(Sizing horizontalSizing) {
        super(horizontalSizing);
        this.onChanged().subscribe(this::updateSuggestions);
    }

    private Sizing menuVerticalSizing(int lines) {
        return Sizing.fixed(Math.min(this.menuHeightFrom(lines), this.menuHeightFrom(this.maxSuggestionLines)));
    }

    private int menuHeightFrom(int lines) {
        if (this.maxSuggestionLines < lines) {
            return SUGGESTION_HEIGHT * lines + ICtxComponent.LAYOUT_VERTICAL_PADDING;
        } else {
            return (int) (SUGGESTION_HEIGHT * (lines + 0.3f));
        }
    }

    /**
     * Note: <b>CommandContext is always null</b>
     */
    public void suggestionProvider(IProvider<?> provider) {
        this.suggestionProvider = provider;
    }

    public void setSelectedCallback(@Nullable Runnable selectedCallback) {
        this.selectedCallback = selectedCallback;
    }

    public void removeContextMenu() {
        this.contextMenu.clearChildren();
    }

    public CompletableFuture<Boolean> updateContextMenu() {
        if (this.suggestionProvider == null) return CompletableFuture.completedFuture(false);

        var client = Minecraft.getInstance();
        var screen = client.gui.screen();
        this.suggestionFuture.cancel(true); // cancel previous updates
        String value = this.getValue();

        this.suggestionFuture = CompletableFuture.<Deque<Pair<String, ICtxComponent<String>>>>supplyAsync(() -> {
            if (this.suggestionProvider == null) return new ArrayDeque<>();

            return this.suggestionProvider.mappedFrom(value);
        }).thenApply(suggestions -> {
            if (!this.getValue().equals(value)) return false; // cancel previous updates
            if (client.gui.screen() != screen) return false;

            client.execute(this::removeContextMenu);

            if (suggestions.isEmpty()) return false;
            if (!(screen instanceof BaseFzmmScreen baseScreen)) return false;

            client.execute(() -> this.openContextMenu(suggestions, baseScreen));
            return true;
        }).whenComplete((aBoolean, throwable) -> {
            if (throwable == null) return;
            FzmmClient.LOGGER.info("[SuggestionTextBox] Exception opening/updating context menu", throwable);
        });

        return this.suggestionFuture;
    }

    private void openContextMenu(Deque<Pair<String, ICtxComponent<String>>> suggestions, BaseFzmmScreen baseScreen) {
        this.contextMenu = new CtxMenuManager<>(this.toEntries(suggestions), o -> null);
        this.contextMenu.rootNode().nodeComponent(ICtxComponent.scrollNode(
                Sizing.fixed(this.width()), this.menuVerticalSizing(suggestions.size()),
                ICtxComponent.BACKGROUND_COLOR, 0xFFA3A3A3
        ));
        this.contextMenu.createLayout(baseScreen.root(), false).configureNode(layout -> {
            if (!(layout instanceof ScrollContainer<?> scroll)) return;

            this.contextMenu.rootNode().findLayout(layout).ifPresent(nodeLayout -> {
                for (var element : nodeLayout.children()) {
                    element.focusGained().subscribe(source -> {
                        if (source != FocusSource.KEYBOARD_CYCLE) return;
                        scroll.scrollTo(element);
                    });
                }
            });
        }).initAt(List.of(), CtxType.UI, this.x(), this.y() + this.height());
    }

    private void updateSuggestions(String s) {
        if (!this.hasParent() || this.previousValue.equals(s)) return; // Hack: ContextMenu should not open when selected via focus

        this.updateContextMenu();
    }

    @Override
    public void setValue(String value) {
        this.previousValue = value;
        super.setValue(value);
    }

    private List<CtxElement<String>> toEntries(Deque<Pair<String, ICtxComponent<String>>> suggestions) {
        var result = new ArrayList<CtxElement<String>>();

        for (var entry : suggestions) {
            String value = entry.getA();
            result.add(CtxEntry.<String>none("suggestion_" + value, o -> this.onSelect(value))
                    .component(entry.getB())
            );
        }

        return result;
    }

    private void onSelect(String value) {
        this.setValue(value);
        if (this.selectedCallback != null) {
            this.selectedCallback.run();
        }
        this.removeContextMenu();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isCycleFocus() || input.isDown()) {
            if (this.contextMenu.isUnmounted()) {
                return this.updateContextMenu().join();
            } else {
                this.contextMenu.focus();
                return true;
            }
        }

        if (input.isEscape()) {
            boolean contextMenuIsOpen = !this.contextMenu.isUnmounted();
            this.removeContextMenu();
            return contextMenuIsOpen;
        }

        return super.keyPressed(input);
    }

    @FunctionalInterface
    public interface IProvider<T> {

        List<T> from(String input);

        default ICtxComponent<String> toComponent(T value, String input) {
            return ICtxComponent.simple(Component.literal(this.toSuggestion(value)));
        }

        default String toSuggestion(T value) {
            return value.toString();
        }

        default Deque<Pair<String, ICtxComponent<String>>> mappedFrom(String input) {
            var suggestions = this.from(input);
            var result = new ArrayDeque<Pair<String, ICtxComponent<String>>>(suggestions.size());

            for (var entry : this.from(input)) {
                result.add(new Pair<>(this.toSuggestion(entry), this.toComponent(entry, input)));
            }

            return result;
        }
    }
}
