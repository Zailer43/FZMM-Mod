package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModelParameter<VALUE> implements IModelParameter<VALUE> {
    private final String id;
    @Nullable
    private VALUE value;
    private final boolean isRequested;

    public ModelParameter(String id, @Nullable VALUE value, boolean isRequested) {
        this.id = id;
        this.value = value;
        this.isRequested = isRequested;
    }

    public String id() {
        return this.id;
    }

    public Optional<VALUE> value() {
        return Optional.ofNullable(this.value);
    }

    public void setValue(@Nullable VALUE value) {
        this.value = value;
    }

    public boolean isRequested() {
        return this.isRequested;
    }

}
