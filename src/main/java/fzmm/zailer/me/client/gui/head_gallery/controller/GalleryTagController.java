package fzmm.zailer.me.client.gui.head_gallery.controller;

import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTag;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fzmm.zailer.me.client.FzmmClient.MCH_RESOURCES;

public class GalleryTagController {
    private final ITagCallback callback;
    private final Set<IMchMatcher> includeTags = new HashSet<>();
    private final Set<IMchMatcher> excludeTags = new HashSet<>();
    private List<MchTag> entries;
    private EButtonComponent openTagsButton;
    private EButtonComponent clearButton;

    public GalleryTagController(ITagCallback callback) {
        this.callback = callback;
    }

    public void setupComponents(EFlowLayout rootComponent, UIModel uiModel, Consumer<EFlowLayout> addOverlay) {
        this.openTagsButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "tags-button");
        this.openTagsButton.onPress(button -> this.openTagsButton(uiModel, addOverlay));

        this.clearButton = rootComponent.childByIdOrThrow(EButtonComponent.class, "clear-tags");
        this.clearButton.onPress(button -> this.clearTags());

        this.updateButton();
    }

    private void onChange() {
        this.updateButton();
        this.callback.update(this.includeTags, this.excludeTags);
    }

    public void updateButton() {
        this.openTagsButton.setMessage(Text.translatable("fzmm.gui.headGallery.button.tags", this.includeTags.size() + this.excludeTags.size()));
        boolean active = MCH_RESOURCES.hasTags();
        this.openTagsButton.active(active);
        this.clearButton.active(active);

        if (!MCH_RESOURCES.hasTags()) {
            Text text = Text.translatable("fzmm.gui.headGallery.tier.missingPermission.details", MchTier.minTierRequired(MchTier.TAG_GENERAL_REQUEST).message());
            List<OrderedText> textList = MinecraftClient.getInstance().textRenderer.wrapLines(text, 250);
            this.openTagsButton.tooltip(textList.stream().map(TooltipComponent::of).collect(Collectors.toList()));
        }
    }

    public Set<IMchMatcher> includeTags() {
        return this.includeTags;
    }

    public Set<IMchMatcher> excludeTags() {
        return this.excludeTags;
    }

    public void update(Set<IMchMatcher> includeTags, Set<IMchMatcher> excludeTags) {
        this.includeTags.clear();
        this.includeTags.addAll(includeTags);
        this.excludeTags.clear();
        this.excludeTags.addAll(excludeTags);
        this.onChange();
    }

    public void clearTags() {
        this.includeTags.clear();
        this.excludeTags.clear();
        this.onChange();
    }


    public void openTagsButton(UIModel uiModel, Consumer<EFlowLayout> addOverlay) {
        EFlowLayout result = EContainers.verticalFlow(Sizing.content(), Sizing.content());

        EFlowLayout tagSelectPanel = uiModel.expandTemplate(EFlowLayout.class, "select-tag", Map.of());
        tagSelectPanel.<EFlowLayout>configure(layout -> {
            FlowLayout tagListLayout = layout.childByIdOrThrow(FlowLayout.class, "tags-content");

            ELabelComponent tagsOverlayLabel = layout.childByIdOrThrow(ELabelComponent.class, "tags-overlay-label");
            ButtonComponent clearSelectedTags = layout.childByIdOrThrow(ButtonComponent.class, "clear-tags-overlay");

            this.updateButton();

            HashMap<MchTag, ButtonComponent> tagButtonsMap = new HashMap<>();
            for (MchTag tag : MCH_RESOURCES.tags()) {
                tagButtonsMap.put(tag, this.overlayTagButton(tag, tagsOverlayLabel));
            }

            clearSelectedTags.onPress(buttonComponent -> {
                this.clearTags();
                for (var button : tagButtonsMap.values()) {
                    button.setMessage(button.getMessage().copy().setStyle(Style.EMPTY));
                }

                this.overlayUpdate(tagsOverlayLabel);
            });

            TextBoxComponent tagSearchBox = layout.childByIdOrThrow(TextBoxComponent.class, "tag-search");

            tagSearchBox.onChanged().subscribe(search -> this.searchCallback(search, tagButtonsMap, tagListLayout));
            this.searchCallback("", tagButtonsMap, tagListLayout);
            this.overlayUpdate(tagsOverlayLabel);
        });

        tagSelectPanel.mouseDown().subscribe((input, doubled) -> true);

        result.child(tagSelectPanel);
        addOverlay.accept(result);
    }

    private void searchCallback(String search, HashMap<MchTag, ButtonComponent> tagButtonsMap, FlowLayout tagListLayout) {
        if (!MCH_RESOURCES.hasTags()) return;
        HashMap<MchTag, ButtonComponent> filteredTags = new HashMap<>(tagButtonsMap);

        if (!search.isEmpty()) {
            for (var tag : this.filter(search.toLowerCase(Locale.ROOT))) {
                if (this.includeTags.contains(tag) || this.excludeTags.contains(tag)) continue;

                filteredTags.remove(tag);
            }
        }

        tagListLayout.<FlowLayout>configure(layout -> {
            layout.clearChildren();
            List<ButtonComponent> sortedEntries = filteredTags.values().stream()
                    .sorted(Comparator.comparing(button -> button.getMessage().getString()))
                    .toList();
            layout.children(sortedEntries);
        });
    }

    private List<MchTag> filter(String search) {
        return this.entries.parallelStream()
                .filter(tag -> tag.name().toLowerCase(Locale.ROOT).contains(search))
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    private ButtonComponent overlayTagButton(MchTag tag, ELabelComponent tagsOverlayLabel) {
        ButtonComponent result = EComponents.button(this.overlayButtonText(tag));
        result.margins(Insets.bottom(4));

        result.mouseDown().subscribe((input, doubled) -> {
            this.overlayButtonExecute(tag, tagsOverlayLabel, result, input.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT);
            return false; // buttonComponent#onPress don't have mouseButton, return false to click sound
        });

        result.keyPress().subscribe((input) -> {
            if (input.isEnter()) {
                this.overlayButtonExecute(tag, tagsOverlayLabel, result, true);
                return true;
            }
            return false;
        });
        return result;
    }

    private void overlayButtonExecute(MchTag tag, ELabelComponent tagsOverlayLabel, ButtonComponent buttonComponent, boolean isInclude) {
        if (!this.includeTags.remove(tag) && !this.excludeTags.remove(tag)) { // if was selected, remove, else add
            if (isInclude) {
                this.includeTags.add(tag);
            } else {
                this.excludeTags.add(tag);
            }
        }
        buttonComponent.setMessage(this.overlayButtonText(tag));

        this.overlayUpdate(tagsOverlayLabel);
    }

    private Text overlayButtonText(MchTag tag) {
        boolean included = this.includeTags.contains(tag);
        if (included || this.excludeTags.contains(tag)) {
            return tag.text(included);
        } else {
            return Text.literal(tag.formattedName());
        }
    }

    private void overlayUpdate(ELabelComponent tagsOverlayLabel) {
        this.onChange();
        tagsOverlayLabel.text(Text.translatable("fzmm.gui.headGallery.label.tagsOverlay",
                this.includeTags.size() + this.excludeTags.size(), MCH_RESOURCES.tags().size()
        ));
    }

    protected void updateEntries() {
        this.entries = MCH_RESOURCES.tags().parallelStream().collect(Collectors.toCollection(ObjectArrayList::new));
    }

    protected List<MchHead> filterHeads(List<MchHead> heads) {
        if (!MCH_RESOURCES.hasTags()) return heads;

        heads = this.filter(heads, this.includeTags, heads::retainAll);
        heads = this.filter(heads, this.excludeTags, heads::removeAll);

        return heads;
    }

    protected List<MchHead> filter(List<MchHead> heads, Set<IMchMatcher> tags, Function<Set<MchHead>, Boolean> filter) {
        if (tags.isEmpty()) return heads;
        Set<MchHead> matches = new LinkedHashSet<>();

        for (var tag : tags) {
            matches.addAll(tag.filter(heads));
        }

        filter.apply(matches);

        return heads;
    }

    @FunctionalInterface
    public interface ITagCallback {
        void update(Set<IMchMatcher> includeTags, Set<IMchMatcher> excludeTags);
    }
}
