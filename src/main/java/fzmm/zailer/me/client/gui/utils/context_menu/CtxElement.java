package fzmm.zailer.me.client.gui.utils.context_menu;

import io.wispforest.owo.ui.core.ParentUIComponent;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.BiPredicate;

public abstract class CtxElement<T> {
    protected final String id;
    protected ICtxComponent<T> componentBuilder;
    protected BiPredicate<T, CtxType> condition = (values, type) -> true;

    protected CtxElement(String id) {
        this.id = id;
        this.componentBuilder = ICtxComponent.simple(Component.empty());
    }

    public abstract boolean hasNested();

    public abstract boolean isDecorator();

    public boolean supports(T selection, CtxType type) {
        return this.modeSupports(selection) && this.condition.test(selection, type);
    }

    public abstract boolean modeSupports(T value);

    public boolean modeSupports(T value, SelectionMode mode) {
        if (mode == null) return true;
        int size = value == null ? 0 : 1;
        if (value instanceof Collection<?> selectionCollection) {
            size = selectionCollection.size();
        }

        return switch (mode) {
            case SINGLE -> size == 1;
            case MULTIPLE -> size > 1;
            case ANY -> size != 0;
        };
    }

    public abstract CtxElement<T> execute(T selection, CtxType type);

    public CtxElement<T> component(ICtxComponent<T> builder) {
        this.componentBuilder = builder;
        return this;
    }

    public ParentUIComponent createComponent() {
        return this.componentBuilder.build(this);
    }

    public CtxElement<T> condition(BiPredicate<T, CtxType> condition) {
        this.condition = condition;
        return this;
    }
}
