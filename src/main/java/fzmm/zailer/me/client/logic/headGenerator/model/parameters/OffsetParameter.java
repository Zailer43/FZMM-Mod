package fzmm.zailer.me.client.logic.headGenerator.model.parameters;

public final class OffsetParameter {
    private byte value;
    private final byte minValue;
    private final byte maxValue;
    private final boolean isXAxis;
    private boolean enabled;
    private final boolean enabledByDefault;

    public OffsetParameter(byte value, byte minValue, byte maxValue, boolean isXAxis, boolean enabled) {
        this.value = value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isXAxis = isXAxis;
        this.enabled = enabled;
        this.enabledByDefault = this.enabled;
    }

    public byte value() {
        return this.value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public byte minValue() {
        return this.minValue;
    }

    public byte maxValue() {
        return this.maxValue;
    }

    public boolean isXAxis() {
        return this.isXAxis;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        this.enabled = this.enabledByDefault;
    }
}
