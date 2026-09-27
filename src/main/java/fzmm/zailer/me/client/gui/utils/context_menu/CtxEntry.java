package fzmm.zailer.me.client.gui.utils.context_menu;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CtxEntry<T> extends CtxElement<T> {
    @Nullable
    protected final Consumer<T> action;
    @Nullable
    protected final SelectionMode mode;

    private CtxEntry(String id, @Nullable Consumer<T> action, SelectionMode mode) {
        super(id);
        this.action = action;
        this.mode = mode;
    }

    public static <T> CtxEntry<T> any(String id, Consumer<T> action) {
        return new CtxEntry<>(id, action, SelectionMode.ANY);
    }

    public static <T> CtxEntry<T> single(String id, Consumer<T> action) {
        return new CtxEntry<>(id, action, SelectionMode.SINGLE);
    }

    public static <T> CtxEntry<T> multiple(String id, Consumer<T> action) {
        return new CtxEntry<>(id, action, SelectionMode.MULTIPLE);
    }

    public static <T> CtxEntry<T> none(String id, Consumer<T> action) {
        return new CtxEntry<>(id, action, null);
    }

    public static <T> CtxEntry<T> decorator() {
        return new CtxEntry<>("decorator", null, SelectionMode.ANY);
    }

    public static <T> CtxElement<T> divider() {
        return CtxEntry.<T>decorator().component(ICtxComponent.divider());
    }

    @Override
    public boolean isDecorator() {
        return this.action == null;
    }

    @Override
    public boolean hasNested() {
        return false;
    }

    @Override
    public boolean supports(T selection, CtxType type) {
        return this.isDecorator() || super.supports(selection, type);
    }

    @Override
    public boolean modeSupports(T value) {
        return this.modeSupports(value, this.mode);
    }

    @Override
    public CtxElement<T> execute(T selection, CtxType type) {
        if (!this.modeSupports(selection)) return this;

        if (this.action != null && this.supports(selection, type)) {
            this.action.accept(selection);
        }
        return this;
    }
}