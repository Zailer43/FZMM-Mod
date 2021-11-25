package fzmm.zailer.me.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class IntSliderWidget extends SliderWidget {
    private final int min;
    private int max;
    private final String translateKey;
    private final boolean joinWithArgs;
    private Consumer<Integer> changedListener;

    public IntSliderWidget(int x, int y, int width, int height, String translateKey, boolean joinWithArgs, double value, int min, int max) {
        super(x, y, width, height, new TranslatableText(translateKey), (value - min) / max);
        this.min = min;
        this.max = max;
        this.translateKey = translateKey;
        this.joinWithArgs = joinWithArgs;
        this.updateMessage();
    }

    public int getValue() {
        return (int) Math.round(MathHelper.lerp(MathHelper.clamp(this.value, 0.0D, 1.0D), this.min, this.max)) - 1;
    }

    public double getRatio() {
        return this.value;
    }

    public void setRatio(double value) {
        this.value = value;
        this.updateMessage();
    }

    public void setMax(int newMax) {
        this.max = newMax;
        this.setValue(Math.min(this.getValue(), newMax));
    }

    public int getMax() {
        return this.max;
    }

    public void setValue(int value) {
        double ratio = (double) value / this.max;
        this.value = MathHelper.clamp(ratio, 0.0D, 1.0D);
        this.updateMessage();
    }

    public void setChangedListener(Consumer<Integer> changedListener) {
        this.changedListener = changedListener;
    }

    private void useListener() {
        if (this.changedListener != null) {
            this.changedListener.accept(getValue());
        }
    }

    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.useListener();
    }

    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        this.useListener();
    }

    public void onRelease(double mouseX, double mouseY) {
        super.playDownSound(MinecraftClient.getInstance().getSoundManager());
        this.useListener();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(
                new TranslatableText(this.translateKey + (this.joinWithArgs ? "" : this.getValue()),
                        this.joinWithArgs ? this.getValue() : null)
        );
    }

    @Override
    protected void applyValue() {
    }
}
