package fzmm.zailer.me.client.gui.utils.selectItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequestedItem {
    private final Predicate<ItemStack> predicate;
    private final Consumer<ItemStack> executeConsumer;
    @Nullable
    private Consumer<ItemStack> updatePreviewConsumer;
    private final Text title;
    private final boolean required;
    private final List<ItemStack> defaultItems;
    @Nullable
    private ItemStack stack;

    public RequestedItem(Predicate<ItemStack> predicate, Consumer<ItemStack> executeConsumer, @Nullable List<ItemStack> defaultItems, @Nullable ItemStack stack, Text title, boolean required) {
        this.predicate = predicate;
        this.executeConsumer = executeConsumer;
        this.updatePreviewConsumer = null;
        this.stack = stack;
        this.title = title;
        this.required = required;
        this.defaultItems = defaultItems == null ? this.getApplicableItems() : defaultItems;
    }

    public RequestedItem(Predicate<ItemStack> predicate, Consumer<ItemStack> executeConsumer, @Nullable List<ItemStack> defaultItems, Text title, boolean required) {
        this(predicate, executeConsumer, defaultItems, null, title, required);
    }

    private List<ItemStack> getApplicableItems() {
        List<ItemStack> applicableItems = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            if (this.predicate.test(item.getDefaultStack()))
                applicableItems.add(item.getDefaultStack());
        }

        return applicableItems;
    }

    public Predicate<ItemStack> predicate() {
        return this.predicate;
    }

    public void execute() {
        if (this.stack != null)
            this.executeConsumer.accept(this.stack);
    }

    public void updatePreview() {
        if (this.updatePreviewConsumer != null)
            this.updatePreviewConsumer.accept(this.stack());
    }

    public void setUpdatePreviewConsumer(@Nullable Consumer<ItemStack> updatePreviewConsumer) {
        this.updatePreviewConsumer = updatePreviewConsumer;
    }

    public ItemStack stack() {
        return this.stack == null ? ItemStack.EMPTY : this.stack;
    }

    public void setStack(@Nullable ItemStack stack) {
        this.stack = stack;
    }

    public Text title() {
        Text title = this.title;

        if (this.required) {
            int redColor = 0xE32B1C;
            title = this.title.copy()
                    .setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("fzmm.gui.selectItem.label.required")
                                    .setStyle(Style.EMPTY.withColor(redColor))))
                    ).append(Text.translatable("fzmm.gui.selectItem.label.required.icon")
                            .setStyle(Style.EMPTY.withColor(redColor))
                    );
        }

        return title;
    }

    public boolean required() {
        return this.required;
    }

    public boolean canExecute() {
        return this.stack != null || !this.required;
    }

    public List<ItemStack> defaultItems() {
        return this.defaultItems;
    }

}
