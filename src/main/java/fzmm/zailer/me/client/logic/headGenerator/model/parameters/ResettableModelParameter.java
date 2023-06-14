package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

import org.jetbrains.annotations.Nullable;

public class ResettableModelParameter<VALUE, DEFAULT_VALUE> extends ModelParameter<VALUE> {
    @Nullable
    private final DEFAULT_VALUE defaultValue;

    public ResettableModelParameter(String id, @Nullable VALUE value, @Nullable DEFAULT_VALUE defaultValue, boolean isRequested) {
        super(id, value, isRequested);
        this.defaultValue = defaultValue;
    }

    @Nullable
    public DEFAULT_VALUE getDefaultValue() {
        return this.defaultValue;
    }

}
