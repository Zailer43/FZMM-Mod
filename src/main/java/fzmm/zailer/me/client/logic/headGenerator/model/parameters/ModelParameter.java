package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModelParameter<T> implements IModelParameter<T> {
    private final String id;
    @Nullable
    private T value;
    private final boolean isRequested;

    public ModelParameter(String id, @Nullable T value, boolean isRequested) {
        this.id = id;
        this.value = value;
        this.isRequested = isRequested;
    }

    public String id() {
        return this.id;
    }

    public Optional<T> value() {
        return Optional.ofNullable(this.value);
    }

    public void setValue(@Nullable T value) {
        this.value = value;
    }

    public boolean isRequested() {
        return this.isRequested;
    }

}
