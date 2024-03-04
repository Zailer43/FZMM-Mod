package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerBuilder {
    private final List<ItemStack> itemList;
    private ItemStack stack;
    private int containerSize;

    private ContainerBuilder() {
        this.itemList = new ArrayList<>();
        this.stack = Items.WHITE_SHULKER_BOX.getDefaultStack();
        this.containerSize = ShulkerBoxBlockEntity.INVENTORY_SIZE;
    }

    public static ContainerBuilder builder() {
        return new ContainerBuilder();
    }

    public ContainerBuilder of(ItemStack stack, boolean addEmptySlots) {
        this.stack = stack.copy();
        this.containerSize = InventoryUtils.getContainerSize(stack.getItem());

        this.clear().addAll(InventoryUtils.getItemsFromContainer(stack, addEmptySlots));

        this.containerSize = Math.max(this.containerSize, this.itemList.size());
        return this;
    }

    public ContainerBuilder clear() {
        this.itemList.clear();
        return this;
    }

    public List<ItemStack> getAsContainerList() {
        List<NbtList> itemsTagList = this.getItemsTagList();
        List<ItemStack> containerList = new ArrayList<>();

        ItemStack defaultStack = this.stack;

        for (var itemTag : itemsTagList) {
            NbtCompound blockEntityTag = new NbtCompound();
            blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, itemTag);

            defaultStack = defaultStack.copy();
            defaultStack.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
            containerList.add(defaultStack);
        }

        return containerList;
    }

    public ItemStack get() {
        NbtList itemsTag = this.getItemsTag(this.itemList);

        if (itemsTag.isEmpty()) {
            NbtCompound nbtCompound = this.stack.getOrCreateNbt();
            nbtCompound.getCompound(TagsConstant.BLOCK_ENTITY).remove(ShulkerBoxBlockEntity.ITEMS_KEY);

            return this.stack;
        }

        this.stack.getOrCreateSubNbt(TagsConstant.BLOCK_ENTITY).put(ShulkerBoxBlockEntity.ITEMS_KEY, itemsTag);

        return this.stack;
    }

    private List<NbtList> getItemsTagList() {
        List<NbtList> itemsTagList = new ArrayList<>();
        int containersAmount = (int) Math.ceil((float) this.itemList.size() / this.containerSize);

        for (int i = 0; i != containersAmount; i++) {
            int sublistEnd = Math.min((i + 1) * this.containerSize, this.itemList.size());
            List<ItemStack> stackSublist = this.itemList.subList(i * this.containerSize, sublistEnd);
            itemsTagList.add(this.getItemsTag(stackSublist));
        }

        return itemsTagList;
    }

    public NbtList getItemsTag(List<ItemStack> stackList) {
        NbtList itemsTag = new NbtList();
        for (int i = 0; i != stackList.size(); i++) {
            ItemStack stack = stackList.get(i);
            if (stack.isEmpty())
                continue;
            NbtCompound tag = stack.writeNbt(new NbtCompound());
            tag.putByte(TagsConstant.INVENTORY_SLOT, (byte) i);
            itemsTag.add(tag);
        }
        return itemsTag;
    }

    public int containerMaxSize() {
        return this.containerSize;
    }

    public int incrementContainerSize() {
        this.itemList.add(ItemStack.EMPTY);
        return ++this.containerSize;
    }

    public ContainerBuilder add(ItemStack stack) {
        this.itemList.add(stack);
        return this;
    }

    public ContainerBuilder addAll(List<ItemStack> stacks) {
        this.itemList.addAll(stacks);
        return this;
    }

    public ContainerBuilder set(int index, ItemStack stack) {
        this.itemList.set(index, stack);
        return this;
    }

    public boolean contains(ItemStack stack) {
        for (ItemStack containerStack : this.itemList) {
            // must not be the same reference, since the stack to which it is compared may already be in the container.
            if (stack != containerStack && ItemStack.canCombine(containerStack, stack))
                return true;
        }

        return false;
    }

    public List<ItemStack> items() {
        return new ArrayList<>(this.itemList);
    }

    public ContainerBuilder addLoreToItems(Item itemToApply, String lore, int color) {
        for (ItemStack stack : this.itemList) {
            if (stack.getItem() == itemToApply && !stack.isEmpty()) {
                NbtCompound tag = DisplayBuilder.of(stack).addLore(lore, color).getNbt();
                stack.setNbt(tag);
            }
        }
        return this;
    }

    public ContainerBuilder setNameStyleToItems(Style style) {
        for (ItemStack stack : this.itemList) {
            if (stack.isEmpty())
                continue;

            Optional<String> optionalName = DisplayBuilder.of(stack).getName();
            if (optionalName.isEmpty())
                continue;
            String name = optionalName.get();
            MutableText nameText;
            try {
                nameText = Text.Serialization.fromJson(name);
                if (nameText == null)
                    nameText = Text.literal(name);

                nameText = nameText.copy();

                nameText.setStyle(style);
                stack.setCustomName(nameText);
            } catch (Exception ignored) {
            }
        }
        return this;
    }
}
