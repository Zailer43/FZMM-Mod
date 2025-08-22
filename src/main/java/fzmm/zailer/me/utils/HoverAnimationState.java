package fzmm.zailer.me.utils;

import net.minecraft.util.Util;

public class HoverAnimationState {
    private double delta = 0d;
    private long lastUpdateTime = Util.getMeasuringTimeMs();
    private final float transitionSeconds;

    public HoverAnimationState(float transitionSeconds) {
        this.transitionSeconds = transitionSeconds;
    }

    public double update(boolean isHovered) {
        long currentTime = Util.getMeasuringTimeMs();
        double deltaTime = (currentTime - this.lastUpdateTime) / 1000.0;
        this.lastUpdateTime = currentTime;

        double speed = deltaTime / this.transitionSeconds;
        if (isHovered) {
            this.delta = Math.min(1.0, this.delta + speed);
        } else {
            this.delta = Math.max(0.0, this.delta - speed);
        }

        return easeOutQuart(this.delta);
    }

    private double easeOutQuart(double n) {
        return 1 - Math.pow(1 - n, 4);
    }
}
