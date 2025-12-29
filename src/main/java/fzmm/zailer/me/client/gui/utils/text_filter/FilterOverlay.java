package fzmm.zailer.me.client.gui.utils.text_filter;

import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FilterOverlay extends OverlayContainer<EFlowLayout> {
    protected final Set<AbstractTextFilter<?, ?>> filters = new HashSet<>();
    protected final Consumer<String> callback;
    protected final String translationKey;
    protected EFlowLayout filtersLayout;
    protected String searchText;

    public FilterOverlay(String baseTranslationKey, Consumer<String> callback) {
        super(EContainers.verticalFlow(Sizing.fixed(340), Sizing.fill(80)));

        this.callback = callback;
        this.translationKey = baseTranslationKey;
        this.buildComponents();
    }

    public void filters(Collection<? extends AbstractTextFilter<?, ?>> filters, String searchText) {
        this.filters.clear();
        this.filters.addAll(filters);
        this.searchText = searchText;

        this.updateFilterLayout();
    }

    protected void buildComponents() {
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(12);
        this.child.padding(Insets.of(6));
        this.child.surface(this.child.styledPanel());

        this.child.child(EComponents.label(Text.translatable("fzmm.gui.search.advanced.label")));
        this.child.child(this.buildFilterWrapper());
        this.child.child(this.buildBottomButtons());
    }

    protected Component buildFilterWrapper() {
        EFlowLayout result = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());

        this.buildFilterLayout(result);

        return result;
    }

    protected void buildFilterLayout(EFlowLayout parent) {
        this.filtersLayout = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());

        parent.child(EContainers.verticalScroll(Sizing.expand(100), Sizing.expand(100), this.filtersLayout, false));
    }

    protected Component buildBottomButtons() {
        // Fixed size to fix owo-lib #348 (340 - 12 = parent width - padding)
        EFlowLayout result = EContainers.horizontalFlow(Sizing.fixed(340 - 12), Sizing.fixed(16));
        result.child(
                EComponents.button(Text.translatable("gui.done"))
                        .onPress(button -> this.execute())
                        .verticalSizing(Sizing.fixed(16))
                        .positioning(Positioning.relative(0, 100))
        );

        result.child(
                EComponents.button(Text.translatable("fzmm.gui.button.cancel"))
                        .onPress(button -> this.remove())
                        .verticalSizing(Sizing.fixed(16))
                        .positioning(Positioning.relative(100, 100))
        );

        return result;
    }

    protected Component toFilterRow(AbstractTextFilter<?, ?> filter) {
        boolean isActive = this.isActive(filter);
        EFlowLayout result = EContainers.horizontalFlow(Sizing.expand(100), Sizing.content());

        result.child(this.toFilterLabel(filter, isActive));
        result.child(this.toFilterIncludeButton(filter, isActive));
        result.child(this.toFilterInput(filter, isActive));

        result.verticalAlignment(VerticalAlignment.CENTER);
        result.gap(2);
        result.hoveredSurface(EStyles.DEFAULT_HOVERED);

        return result;
    }

    protected boolean isActive(AbstractTextFilter<?, ?> filter) {
        return true;
    }

    protected Component toFilterLabel(AbstractTextFilter<?, ?> filter, boolean active) {
        ELabelComponent result = EComponents.label(Text.empty());
        result.tooltip(Text.translatable("fzmm.gui.search.advanced.key", filter.key()));
        this.filterText(filter, result, active);

        return result.horizontalSizing(Sizing.fixed(115));
    }

    protected void filterText(AbstractTextFilter<?, ?> filter, ELabelComponent label, boolean active) {
        label.text(Text.translatable(this.translationKey + filter.key()));
    }

    protected Component toFilterIncludeButton(AbstractTextFilter<?, ?> filter, boolean active) {
        EBooleanButton result = new EBooleanButton(
                Text.translatable("fzmm.gui.search.include.enabled").formatted(Formatting.GREEN),
                Text.translatable("fzmm.gui.search.include.disabled").formatted(Formatting.RED)
        );
        result.enabled(filter.isIncluded());
        result.active(active);

        result.onPress(button -> filter.include(!filter.isIncluded()))
                .horizontalSizing(Sizing.fixed(60));

        return result;
    }

    protected void execute() {
        List<String> filters = this.filters.stream()
                .map(AbstractTextFilter::serialize)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        filters.add(this.searchText);

        this.callback.accept(String.join(" ", filters));
        this.remove();
    }

    protected <T> Component toFilterInput(AbstractTextFilter<T, ?> filter, boolean active) {
        TextBoxComponent result = filter.toInputComponent();
        result.text(filter.serializeValue());
        result.onChanged().subscribe(s -> filter.value(filter.parseValue(s).orElse(null)));
        result.active = active;

        result.horizontalSizing(Sizing.expand(100));
        result.keyPress().subscribe(input -> {
            if (input.isEnter()) {
                this.execute();
                return true;
            }
            return false;
        });

        return result;
    }

    private void updateFilterLayout() {
        this.filtersLayout.<EFlowLayout>configure(layout -> {
            layout.clearChildren();
            layout.children(this.filters.stream()
                    .sorted(
                            // sort by class to have same type next to each other because
                            // and then sort by id because Map is not sorted
                            Comparator.comparing(o -> o.getClass().getSimpleName())
                                    .thenComparing(o -> ((AbstractTextFilter<?, ?>) o).key())
                    ).map(this::toFilterRow)
                    .toList()
            );
        });
    }
}
