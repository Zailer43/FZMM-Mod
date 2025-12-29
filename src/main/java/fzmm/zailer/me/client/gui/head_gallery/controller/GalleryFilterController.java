package fzmm.zailer.me.client.gui.head_gallery.controller;

import com.google.common.collect.ImmutableMap;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_gallery.components.GalleryFilterOverlay;
import fzmm.zailer.me.client.gui.head_gallery.filter.AbstractFilter;
import fzmm.zailer.me.client.gui.head_gallery.filter.CategoryFilter;
import fzmm.zailer.me.client.gui.head_gallery.filter.OtherCollectionFilter;
import fzmm.zailer.me.client.gui.head_gallery.filter.SelfCollectionFilter;
import fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterDate;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterInteger;
import fzmm.zailer.me.client.gui.utils.text_filter.TextFilterString;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchCategory;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.FocusHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fzmm.zailer.me.client.FzmmClient.MCH_RESOURCES;

public class GalleryFilterController implements IMemento {
    public static final TextFilterInteger<MchHead> ID_GREATER_FILTER;
    private final IFilterCallback filterCallback;
    private final List<AbstractFilter> filters;
    private final Map<AbstractTextFilter<?, MchHead>, Integer> textFilters;
    private List<MchHead> entries;
    private AbstractFilter selectedFilter;
    @Nullable
    private IMchMatcher filterValue;
    private EFlowLayout filterLayout;
    private TextBoxComponent searchField;
    private ContextMenuButton filterButton;
    @Nullable
    private GalleryTagController tagManager = null;

    public GalleryFilterController(IFilterCallback callback, @Nullable IMchMatcher filterValue) {
        this.filterCallback = callback;
        this.filterValue = filterValue;
        this.filters = List.of(
                new CategoryFilter(this::filter),
                new SelfCollectionFilter(this::filter),
                new OtherCollectionFilter(this::filter)
        );
        this.selectedFilter = this.filters.get(0);

        ImmutableMap.Builder<AbstractTextFilter<?, MchHead>, Integer> filterBuilder = ImmutableMap.builder();
        // id
        filterBuilder.put(ID_GREATER_FILTER, MchTier.HEADS_ADD_DATA_FREE);
        filterBuilder.put(new TextFilterInteger<>("id_lower", (id, head) -> head.id() != null && head.id() <= id), MchTier.HEADS_ADD_DATA_FREE);
        filterBuilder.put(new TextFilterInteger<>("id", (id, head) -> head.id() != null && head.id().equals(id)), MchTier.HEADS_ADD_DATA_FREE);

        // name
        filterBuilder.put(new TextFilterString<>("word", (value, head) ->
                Arrays.asList(head.name().toLowerCase(Locale.ROOT).split(" ")).contains(value.toLowerCase(Locale.ROOT))
        ), MchTier.HEADS_BASIC_DATA);

        // value
        filterBuilder.put(new TextFilterString<>("value", (value, head) ->
                HeadBuilder.urlValueToSkinValue(head.convertRawValue()).equals(value)), MchTier.HEADS_BASIC_DATA
        );
        filterBuilder.put(new TextFilterString<>("url", (value, head) -> {
            String[] urlRoutes = value.split("/");
            if (urlRoutes.length < 1) return false;

            return head.convertRawValue().equals(urlRoutes[urlRoutes.length - 1]); // only check url value
        }), MchTier.HEADS_BASIC_DATA);

        // published at
        filterBuilder.put(new TextFilterDate<>(
                "date_after", (date, head) ->
                head.publishedAt() != null && head.publishedAt().isAfter(date),
                "yyyy-M-d"
        ), MchTier.HEADS_ADD_DATA_FREE);
        filterBuilder.put(new TextFilterDate<>(
                "date_before", (date, head) ->
                head.publishedAt() != null && head.publishedAt().isBefore(date),
                "yyyy-M-d"
        ), MchTier.HEADS_ADD_DATA_FREE);

        //TODO: add saved search filters, maybe an user has commonly used filters,
        // like "date_after=2023-01-01 tag=christmas gift" or "category=miscellaneous,decoration icon"

        //TODO: filter by list:
        //   tags=can,"fast food"
        //   -tags=drink
        //   category=blocks,humans
        this.textFilters = filterBuilder.build();
        this.updateEntries(new ObjectArrayList<>());
    }

    public void setupComponents(EFlowLayout rootComponent) {
        // search
        this.searchField = rootComponent.childByIdOrThrow(TextBoxComponent.class, "content-search");
        this.searchField.onChanged().subscribe(s -> this.onChange(false));
        this.searchField.setMaxLength(2048);

        EButtonComponent advancedSearchButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "advanced-search");
        advancedSearchButton.onPress(button -> {
            GalleryFilterOverlay overlay = new GalleryFilterOverlay("fzmm.gui.headGallery.option.search.filter.", this.searchField::text);
            overlay.filters(this.textFilters, AbstractTextFilter.removeFilters(this.searchField.getValue()));
            rootComponent.child(overlay);
        });

        // filters: category, collection (self), collection (other)
        this.filterLayout = rootComponent.childByIdOrThrow(EFlowLayout.class, "filter-layout");
        this.filterButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "filter-button");
        this.filterButton.active(false);
    }

    /**
     * Initializing filters requires being called after essential data is obtained from the API
     */
    public void init() {
        if (MCH_RESOURCES.categories().isEmpty()) return;

        this.tags(new HashSet<>(), new HashSet<>());

        for (var filter : this.filters) {
            filter.hasPermission(MCH_RESOURCES.licenseDetected().hasPermission(filter.permissionRequired()));
        }

        this.filterButton.active(true);
        this.filterButton.setContextMenuOptions(dropdown -> {
            for (var filter : this.filters) {
                Component message = filter.buttonText();
                dropdown.button(message, dropdownComponent -> {
                    this.selectFilter(filter);
                    dropdownComponent.remove();
                });
            }
        });
        this.filters.get(0).initLayout(this.filterLayout, this.filterValue == null ? MchCategory.ALL : this.filterValue);
    }

    private void selectFilter(AbstractFilter filter) {
        IMchMatcher filterOption = this.filterValue == null ? MchCategory.ALL : this.filterValue;

        this.selectedFilter = filter;
        filter.initLayout(this.filterLayout, filterOption);
        this.filter(filterOption);
        this.onChange(true);
        this.filterButton.setMessage(filter.buttonText());
    }

    public void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.searchField, UIComponent.FocusSource.MOUSE_CLICK);
    }

    public void onChange(boolean resetPage) {
        this.filterCallback.filter(this.filterHeads(), resetPage);
    }

    protected List<MchHead> filterHeads() {
        String search = AbstractTextFilter.updateFilters(this.searchField.getValue(), this.textFilters.keySet()).toLowerCase(Locale.ROOT);

        Stream<MchHead> filteredStream = this.entries.parallelStream();
        if (!search.isEmpty()) {
            filteredStream = filteredStream.filter(head -> Strings.CI.contains(head.name(), search));
        }
        List<MchHead> filtered = filteredStream.filter(this::textFilters)
                .collect(Collectors.toCollection(ObjectArrayList::new));

        return this.tagManager == null ? filtered : this.tagManager.filterHeads(filtered);
    }

    private boolean textFilters(MchHead head) {
        return AbstractTextFilter.matchesAll(this.textFilters.keySet(), head);
    }

    public void updateEntries(List<MchHead> heads) {
        this.entries = heads;

        if (this.tagManager != null) {
            this.tagManager.updateEntries();
        }
    }

    public TextBoxComponent searchTextBox() {
        return this.searchField;
    }

    private void filter(IMchMatcher value) {
        this.filterValue = value;
        List<MchHead> heads;

        if (value.equals(MchCategory.ALL)) {
            heads = new ObjectArrayList<>(MCH_RESOURCES.heads());
        } else {
            heads = value.filter(MCH_RESOURCES.heads());
        }

        for (var filter : this.filters) {
            filter.optionsEnabled(false);
        }

        this.updateEntries(heads);
        this.onChange(true);

        for (var filter : this.filters) {
            filter.optionsEnabled(true);
        }
    }

    public void tagManager(GalleryTagController value) {
        this.tagManager = value;
    }

    public void tags(Set<IMchMatcher> includeTags, Set<IMchMatcher> excludeTags) {
        if (this.tagManager == null) return;
        this.tagManager.update(includeTags, excludeTags);

        this.onChange(false);
    }

    static {
        ID_GREATER_FILTER = new TextFilterInteger<>("id_greater", (id, head) -> head.id() != null && head.id() >= id);
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.tagManager == null ? new HashSet<>() : this.tagManager.includeTags());
        output.writeObject(this.tagManager == null ? new HashSet<>() : this.tagManager.excludeTags());
        output.writeObject(this.searchField.getValue());
        output.writeObject(this.filterValue);
        output.writeInt(this.filters.indexOf(this.selectedFilter));
        if (this.selectedFilter instanceof IMemento memento) {
            memento.backup(output);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.tags((Set<IMchMatcher>) input.readObject(), (Set<IMchMatcher>) input.readObject());
        this.searchField.text((String) input.readObject());
        this.filterValue = (IMchMatcher) input.readObject();
        AbstractFilter filter = this.filters.get(input.readInt());
        this.selectFilter(filter);
        if (filter instanceof IMemento memento) {
            memento.restore(input);
        }
    }

    @FunctionalInterface
    public interface IFilterCallback {
        void filter(List<MchHead> heads, boolean resetPage);
    }
}
