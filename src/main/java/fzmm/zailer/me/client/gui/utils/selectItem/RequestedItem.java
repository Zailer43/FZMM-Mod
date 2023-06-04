package fzmm.zailer.me.client.gui.utils.selectItem;

import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class RequestedItem {
    private final Predicate<ItemStack> predicate;
    private final Consumer<ItemStack> consumer;
    private final Text title;
    private final boolean required;
    private final List<ItemStack> defaultItems;
    @Nullable
    private ItemStack stack;

    public RequestedItem(Predicate<ItemStack> predicate, Consumer<ItemStack> consumer, List<ItemStack> defaultItems, @Nullable ItemStack stack, Text title, boolean required) {
        this.predicate = predicate;
        this.consumer = consumer;
        this.stack = stack;
        this.title = title;
        this.required = required;
        this.defaultItems = defaultItems;
    }

    public RequestedItem(Predicate<ItemStack> predicate, Consumer<ItemStack> consumer, List<ItemStack> defaultItems, Text title, boolean required) {
        this(predicate, consumer, defaultItems, null, title, required);
    }

    public Predicate<ItemStack> predicate() {
        return this.predicate;
    }

    public void execute() {
        if (this.stack != null)
            this.consumer.accept(this.stack);
    }

    public Optional<ItemStack> stack() {
        return Optional.ofNullable(this.stack);
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
