package fzmm.zailer.me.client.gui.item_editor.hide_flags_editor;

import fzmm.zailer.me.builders.HideFlagsBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HideFlagsEditor implements IItemEditorScreen {
    private RequestedItem stackRequested = null;
    private List<RequestedItem> requestedItems = null;
    private final HideFlagsBuilder hideFlagsBuilder = HideFlagsBuilder.builder();
    private final HashMap<ItemStack.TooltipSection, CheckboxComponent> flagComponents = new HashMap<>();
    private boolean ignoreCallback;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.stackRequested = new RequestedItem(
                itemStack -> {
                    if (HideFlagsBuilder.builder().of(itemStack).hideFlags() != 0)
                        return true;

                    return !this.getActiveFlags(itemStack).isEmpty();
                },
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.label.anyItem"),
                true
        );

        this.requestedItems = List.of(this.stackRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.PAPER.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.ignoreCallback = false;

        ButtonComponent selectAllButton = editorLayout.childById(ButtonComponent.class, "select-all");
        BaseFzmmScreen.checkNull(selectAllButton, "button", "select-all");
        selectAllButton.onPress(buttonComponent -> this.selectAll(true));

        ButtonComponent deselectAllButton = editorLayout.childById(ButtonComponent.class, "unselect-all");
        BaseFzmmScreen.checkNull(deselectAllButton, "button", "unselect-all");
        deselectAllButton.onPress(buttonComponent -> this.selectAll(false));

        // flags
        String translationKey = "fzmm.gui.itemEditor.hide_flags.option.";
        for (var flag : ItemStack.TooltipSection.values()) {
            CheckboxComponent flagComponent = Components.checkbox(Text.translatable(translationKey + flag.name().toLowerCase()));
            flagComponent.onChanged(aBoolean -> {
                if (this.ignoreCallback)
                    return;

                if (flagComponent.isChecked())
                    this.hideFlagsBuilder.set(flag);
                else
                    this.hideFlagsBuilder.remove(flag);

                this.updateItemPreview();
            });
            this.flagComponents.put(flag, flagComponent);
        }

        int maxWidth = 0;
        for (var flagComponent : this.flagComponents.values())
            maxWidth = Math.max(maxWidth, flagComponent.getWidth());

        for (var flagComponent : this.flagComponents.values())
            flagComponent.horizontalSizing(Sizing.fixed(maxWidth));

        FlowLayout flagLayout = editorLayout.childById(FlowLayout.class, "flags");
        BaseFzmmScreen.checkNull(flagLayout, "layout", "flags");
        flagLayout.children(this.flagComponents.values());

        return editorLayout;
    }

    @Override
    public String getId() {
        return "hide_flags";
    }

    @Override
    public void updateItemPreview() {
        this.stackRequested.setStack(this.hideFlagsBuilder.get());
        this.stackRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.hideFlagsBuilder.of(stack);

        this.ignoreCallback = true;

        for (var flag : this.flagComponents.keySet())
            this.flagComponents.get(flag).checked(this.hideFlagsBuilder.has(flag));

        this.ignoreCallback = false;

        List<ItemStack.TooltipSection> activeFlags = this.getActiveFlags(stack);
        for (var flag : ItemStack.TooltipSection.values()) {
            CheckboxComponent flagComponent = this.flagComponents.get(flag);
            MutableText checkboxText = flagComponent.getMessage().copy();
            int color = Color.ofFormatting(activeFlags.contains(flag) ? Formatting.GRAY  : Formatting.DARK_GRAY).argb();
            checkboxText.setStyle(Style.EMPTY.withColor(color));
            flagComponent.setMessage(checkboxText);
        }
    }

    private void selectAll(boolean value) {
        this.ignoreCallback = true;
        this.hideFlagsBuilder.setAll(value);
        for (var flagComponent : this.flagComponents.values())
            flagComponent.checked(value);

        this.ignoreCallback = false;
        this.updateItemPreview();
    }

    public List<ItemStack.TooltipSection> getActiveFlags(ItemStack stack) {
        List<ItemStack.TooltipSection> flags = new ArrayList<>();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        HideFlagsBuilder builder = HideFlagsBuilder.builder();
        int tooltipSize = builder.of(stack)
                .setAll(false)
                .get()
                .getTooltip(player, TooltipContext.BASIC)
                .size();

        for (var flag : ItemStack.TooltipSection.values()) {
            ItemStack copyWithHideFlag = builder.of(stack).setAll(false).set(flag).get();

            if (tooltipSize != copyWithHideFlag.getTooltip(player, TooltipContext.BASIC).size())
                flags.add(flag);
        }

        return flags;
    }
}
