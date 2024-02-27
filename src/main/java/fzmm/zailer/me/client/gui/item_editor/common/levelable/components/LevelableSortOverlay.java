package fzmm.zailer.me.client.gui.item_editor.common.levelable.components;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.SortLevelableComponent;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class LevelableSortOverlay<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> extends OverlayContainer<FlowLayout> {
    private final List<D> levelableList;
    private final List<EnumWidget> sorterList;
    private final LevelableEditor<V, D, B> editor;
    private final B builder;

    public LevelableSortOverlay(LevelableEditor<V, D, B> editor, B builder) {
        super(Containers.verticalFlow(Sizing.fill(70), Sizing.fill(90)));

        this.editor = editor;
        this.builder = builder;
        this.levelableList = builder.values();

        FlowLayout levelablesLayout = Containers.verticalFlow(Sizing.content(0), Sizing.content(0));
        levelablesLayout.children(this.getLevelables());

        FlowLayout sorters = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        sorters.gap(4);
        this.sorterList = this.getSorters(levelablesLayout);
        sorters.children(this.sorterList);

        for (var sorter : this.sorterList) {
            //noinspection UnstableApiUsage
            sorter.select(0);
        }

        ScrollContainer<FlowLayout> scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), levelablesLayout);
        scrollContainer.margins(Insets.vertical(24));
        scrollContainer.positioning(Positioning.relative(0, 0));

        ButtonComponent backButton = Components.button(Text.translatable("fzmm.gui.button.back"), buttonComponent -> this.remove());
        backButton.margins(Insets.bottom(10).withRight(10));
        backButton.positioning(Positioning.relative(100, 100));

        this.child.surface(Surface.DARK_PANEL);
        this.child.gap(8);
        this.child.padding(Insets.of(10));
        this.child.child(sorters);
        this.child.child(scrollContainer);
        this.child.child(backButton);

        // otherwise owo-lib closes the overlay
        this.child.mouseDown().subscribe((mouseX, mouseY, button) -> true);

    }

    private List<? extends Component> getLevelables() {
        List<SortLevelableComponent<V, D, B>> components = new ArrayList<>();

        for (var levelable : this.levelableList) {
            SortLevelableComponent<V, D, B> levelableComponent = new SortLevelableComponent<>(levelable, this.editor, this.builder);
            components.add(levelableComponent);

            levelableComponent.getUpButton().onPress(buttonComponent -> ListUtils.upEntry(components, levelableComponent, () -> this.updateLevelableList(components)));
            levelableComponent.getDownButton().onPress(buttonComponent -> ListUtils.downEntry(components, levelableComponent, () -> this.updateLevelableList(components)));
        }

        return components;
    }

    private void updateLevelableList(List<SortLevelableComponent<V, D, B>> components) {
        this.levelableList.clear();

        for (var component : components)
            this.levelableList.add(component.getLevelable());

        this.builder.values(this.levelableList);
    }

    private List<EnumWidget> getSorters(FlowLayout levelablesLayout) {
        List<EnumWidget> components = new ArrayList<>();

        EnumWidget alphabetic = this.getEnumWidget("fzmm.gui.itemEditor.levelable.option.sort.alphabetic");
        alphabetic.init(SortOption.DISABLE);
        alphabetic.onPress(buttonComponent -> this.sorterExecute(alphabetic, levelablesLayout, this::sortAlphabetic));

        EnumWidget levels = this.getEnumWidget("fzmm.gui.itemEditor.levelable.option.sort.levels");
        levels.init(SortOption.DISABLE);
        levels.onPress(buttonComponent -> this.sorterExecute(levels, levelablesLayout, this::sortLevels));

        components.add(alphabetic);
        components.add(levels);

        return components;
    }

    private void sorterExecute(EnumWidget sorterComponent, FlowLayout levelablesLayout, Consumer<SortOption> sortConsumer) {
        SortOption sortOption = (SortOption) sorterComponent.getValue();
        this.disableSorters(sorterComponent, sortOption);
        sortConsumer.accept(sortOption);
        this.updateLevelablesLayout(levelablesLayout);
    }

    private void updateLevelablesLayout(FlowLayout levelablesLayout) {
        this.builder.values(this.levelableList);

        List<Component> layoutComponents = levelablesLayout.children();

        for (int i = 0; i != this.levelableList.size(); i++) {
            if (layoutComponents.get(i) instanceof SortLevelableComponent<?, ?, ?> levelableEntry) {
                //noinspection unchecked
                SortLevelableComponent<V, D, B> entry = (SortLevelableComponent<V, D, B>) levelableEntry;
                D sortedValue = this.builder.getValue(i);
                SortLevelableComponent<V, D, B> levelableCopy = new SortLevelableComponent<>(sortedValue, this.editor, this.builder);
                entry.setValue(levelableCopy);
            }
        }
    }

    private void disableSorters(EnumWidget button, SortOption sortOption) {
        if (sortOption == SortOption.DISABLE)
            return;

        for (var option : this.sorterList) {
            if (option != button && option.getValue() != SortOption.DISABLE)
                option.setValue(SortOption.DISABLE);
        }
    }

    private void sortAlphabetic(SortOption option) {
        Comparator<D> comparator = Comparator.comparing(data -> data.getName().getString());

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.levelableList.sort(comparator);
    }

    private void sortLevels(SortOption option) {
        Comparator<D> comparator = Comparator.comparing(D::getLevel);

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.levelableList.sort(comparator);
    }

    private EnumWidget getEnumWidget(String translationKey) {
        return new EnumWidget() {
            @Override
            public void setMessage(Text message) {
                message = Text.translatable(translationKey, message);
                super.setMessage(message);
            }
        };
    }

    @Override
    public void remove() {
        super.remove();

        this.editor.updateItemPreview();
        this.editor.updateAppliedLevelables();
    }

    private enum SortOption implements IMode {
        DISABLE("fzmm.gui.button.remove"),
        ASCENDING("fzmm.gui.button.arrow.up"),
        DESCENDING("fzmm.gui.button.arrow.down");

        private final String symbol;

        SortOption(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String getTranslationKey() {
            return this.symbol;
        }
    }
}
