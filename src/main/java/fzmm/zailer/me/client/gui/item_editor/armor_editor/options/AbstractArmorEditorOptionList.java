package fzmm.zailer.me.client.gui.item_editor.armor_editor.options;

import blue.endless.jankson.annotation.Nullable;
import fzmm.zailer.me.builders.ArmorBuilder;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractArmorEditorOptionList<TYPE> {
    protected final HashMap<TYPE, Component> options = new HashMap<>();
    protected final ArmorEditorScreen parent;
    protected ArmorBuilder selectedArmorBuilder = null;
    protected RequestedItem selectedArmorRequest = null;
    private Component selectedComponent = null;
    private Runnable executeCallback = null;

    protected AbstractArmorEditorOptionList(ArmorEditorScreen parent) {
        this.parent = parent;
    }

    public void setSelectedArmor(ArmorBuilder armorBuilder, RequestedItem requestedItem) {
        this.selectedArmorBuilder = armorBuilder;
        this.selectedArmorRequest = requestedItem;
    }

    public void generateLayout(FlowLayout optionParent) {
        optionParent.clearChildren();
        List<Component> components = new ArrayList<>();
        this.options.clear();

        if (this.addFirstValueNull())
            components.add(this.getLayout(null, this.getValueId(null)));

        for (var value : this.getValueList())
            components.add(this.getLayout(value, this.getValueId(value)));

        this.updateSelectedOption(this.getSelectedValue());

        optionParent.children(components);
    }

    public abstract List<TYPE> getValueList();

    public abstract String getValueId(@Nullable TYPE value);

    public abstract TYPE getSelectedValue();

    public abstract Component getLayout(@Nullable TYPE value, String id);

    public void updateSelectedOption(TYPE value) {
        if (this.selectedComponent != null)
            this.unselectComponent(this.selectedComponent);

        Component valueComponent = this.options.get(value);
        if (valueComponent != null) {
            this.selectComponent(valueComponent);
            this.selectedComponent = valueComponent;
        }
    }

    protected abstract void selectComponent(Component component);

    protected abstract void unselectComponent(Component component);

    public void execute(TYPE value) {
        this.selectOption(value);

        if (this.executeCallback != null)
            this.executeCallback.run();
    }

    protected abstract void selectOption(@Nullable TYPE value);

    public void setExecuteCallback(Runnable runnable) {
        this.executeCallback = runnable;
    }

    protected abstract boolean addFirstValueNull();
}
