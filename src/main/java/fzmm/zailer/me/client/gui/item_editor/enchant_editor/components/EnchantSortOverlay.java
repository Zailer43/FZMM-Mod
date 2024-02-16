package fzmm.zailer.me.client.gui.item_editor.enchant_editor.components;

import fzmm.zailer.me.builders.EnchantmentBuilder;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.components.enchant.SortEnchantComponent;
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

public class EnchantSortOverlay extends OverlayContainer<FlowLayout> {
    private final List<EnchantmentBuilder.EnchantmentData> enchantmentList;
    private final List<EnumWidget> sorterList;
    private final EnchantEditor editor;
    private final EnchantmentBuilder builder;

    public EnchantSortOverlay(EnchantEditor editor, EnchantmentBuilder builder) {
        super(Containers.verticalFlow(Sizing.fill(70), Sizing.fill(90)));

        this.editor = editor;
        this.builder = builder;
        this.enchantmentList = builder.enchantments();

        FlowLayout enchantsLayout = Containers.verticalFlow(Sizing.content(0), Sizing.content(0));
        enchantsLayout.children(this.addEnchants());

        FlowLayout sorters = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        sorters.gap(4);
        this.sorterList = this.getSorters(enchantsLayout);
        sorters.children(this.sorterList);

        for (var sorter : this.sorterList)
            //noinspection UnstableApiUsage
            sorter.select(0);

        ScrollContainer<FlowLayout> scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), enchantsLayout);
        scrollContainer.margins(Insets.vertical(24));
        scrollContainer.positioning(Positioning.relative(0, 0));

        ButtonComponent backButton = Components.button(Text.translatable("fzmm.gui.button.back"), buttonComponent -> {
            if (this.parent() != null)
                this.parent().remove();
        });
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

    private List<? extends Component> addEnchants() {
        List<SortEnchantComponent> components = new ArrayList<>();

        for (var enchantment : this.enchantmentList) {
            SortEnchantComponent enchantComponent = new SortEnchantComponent(enchantment.getEnchantment(), enchantment.getLevel(),
                    this.editor, this.builder);
            components.add(enchantComponent);

            enchantComponent.getUpButton().onPress(buttonComponent -> ListUtils.upEntry(components, enchantComponent, null));
            enchantComponent.getDownButton().onPress(buttonComponent -> ListUtils.downEntry(components, enchantComponent, null));
        }

        return components;
    }

    private List<EnumWidget> getSorters(FlowLayout enchantsLayout) {
        List<EnumWidget> components = new ArrayList<>();

        EnumWidget alphabetic = this.getEnumWidget("fzmm.gui.itemEditor.enchant.option.sort.alphabetic");
        alphabetic.init(SortOption.DISABLE);
        alphabetic.onPress(buttonComponent -> this.sorterExecute(alphabetic, enchantsLayout, this::sortAlphabetic));

        EnumWidget levels = this.getEnumWidget("fzmm.gui.itemEditor.enchant.option.sort.levels");
        levels.init(SortOption.DISABLE);
        levels.onPress(buttonComponent -> this.sorterExecute(levels, enchantsLayout, this::sortLevels));

        components.add(alphabetic);
        components.add(levels);

        return components;
    }

    private void sorterExecute(EnumWidget sorterComponent, FlowLayout enchantsLayout, Consumer<SortOption> sortConsumer) {
        SortOption sortOption = (SortOption) sorterComponent.getValue();
        this.disableSorters(sorterComponent, sortOption);
        sortConsumer.accept(sortOption);
        this.updateEnchantsLayout(enchantsLayout);
        this.editor.updatePreview();
    }

    private void updateEnchantsLayout(FlowLayout enchantsLayout) {
        this.builder.enchantments(this.enchantmentList);

        List<Component> layoutComponents = enchantsLayout.children();

        for (int i = 0; i != this.enchantmentList.size(); i++) {
            if (layoutComponents.get(i) instanceof SortEnchantComponent enchantEntry) {
                EnchantmentBuilder.EnchantmentData sortedValue = builder.getEnchant(i);
                SortEnchantComponent enchantCopy = new SortEnchantComponent(sortedValue.getEnchantment(), sortedValue.getLevel(),
                        this.editor, this.builder);
                enchantEntry.setValue(enchantCopy);
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
        Comparator<EnchantmentBuilder.EnchantmentData> comparator = Comparator.comparing(EnchantmentBuilder.EnchantmentData::getName);

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.enchantmentList.sort(comparator);
    }

    private void sortLevels(SortOption option) {
        Comparator<EnchantmentBuilder.EnchantmentData> comparator = Comparator.comparing(EnchantmentBuilder.EnchantmentData::getLevel);

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.enchantmentList.sort(comparator);
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

        this.editor.updateAppliedEnchants();
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
