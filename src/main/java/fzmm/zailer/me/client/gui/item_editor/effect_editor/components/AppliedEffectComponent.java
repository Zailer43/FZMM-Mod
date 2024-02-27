package fzmm.zailer.me.client.gui.item_editor.effect_editor.components;

import fzmm.zailer.me.builders.EffectBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.AppliedLevelableComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class AppliedEffectComponent extends AppliedLevelableComponent<StatusEffect, EffectBuilder.EffectData, EffectBuilder> {

    private ConfigTextBox durationTextBox;

    public AppliedEffectComponent(EffectBuilder.EffectData levelable, @Nullable Runnable callback, LevelableEditor<StatusEffect, EffectBuilder.EffectData, EffectBuilder> editor, EffectBuilder builder) {
        super(levelable, callback, editor, builder);
    }

    @Override
    protected List<? extends Component> getOptions() {
        LabelComponent levelLabel = Components.label(Text.translatable("fzmm.gui.itemEditor.effect.label.level"));

        Component levelTextBox = super.getOptions().get(0);
        levelTextBox.horizontalSizing(Sizing.fixed(30));

        LabelComponent durationLabel = Components.label(Text.translatable("fzmm.gui.itemEditor.effect.label.duration"));
        durationLabel.tooltip(Text.translatable("fzmm.gui.itemEditor.effect.label.duration.tooltip"));

        this.durationTextBox = new ConfigTextBox();
        configureForTime(this.durationTextBox);
        this.durationTextBox.horizontalSizing(Sizing.fixed(60));
        this.durationTextBox.setMaxLength(24);
        this.setDuration(this.getLevelable().duration());

        this.durationTextBox.onChanged().subscribe(value -> {
            if (this.durationTextBox.isValid()) {
                this.getLevelable().duration(this.getDuration());
                if (this.updateItemCallback)
                    this.editor.updateItemPreview();
            }
        });

        return List.of(levelLabel, levelTextBox, durationLabel, this.durationTextBox);
    }

    public static void configureForTime(ConfigTextBox textBox) {
        textBox.valueParser(s -> {
            if (s.equalsIgnoreCase("inf") || s.equalsIgnoreCase("infinity") || s.equals("-1")) {
                // Handle infinite time
                return -1;
            } else if (s.matches("\\d+")) {
                // Handle ticks
                try {
                    long ticks = Long.parseLong(s);
                    return (int) MathHelper.clamp(ticks, 0, Integer.MAX_VALUE);
                } catch (NumberFormatException e) {
                    return 0;
                }
            } else {
                // Handle time duration
                return getTimeInTicks(s);
            }
        });

        textBox.applyPredicate(s -> {
            if (s.equalsIgnoreCase("inf") || s.equalsIgnoreCase("infinity") || s.equals("-1")) {
                return true; // Infinite time or -1 is always valid
            } else if (s.matches("\\d+")) {
                return true; // Ticks are always valid
            } else {
                // Check for valid time duration format
                return Pattern.compile("^((\\d+[dDhHmMsS] ?)+)$").matcher(s).matches();
            }
        });
    }

    public static int getTimeInTicks(String value) {
        Pattern combinationPattern = Pattern.compile("(\\d+\\w)");
        Pattern numberPattern = Pattern.compile("(\\d+)");
        Pattern unitPattern = Pattern.compile("([a-z,A-Z])");

        long ticks = 0;

        Matcher combinationMatcher = combinationPattern.matcher(value);
        while (combinationMatcher.find()) {

            String arg = combinationMatcher.group(1);
            Matcher numberMatcher = numberPattern.matcher(arg);
            Matcher letterMatcher = unitPattern.matcher(arg);

            if (!numberMatcher.find() || !letterMatcher.find())
                continue;

            int number;

            try {
                number = Integer.parseInt(numberMatcher.group(1));
            } catch (NumberFormatException e) {
                continue;
            }

            switch (letterMatcher.group(1)) {
                case "d":
                    ticks += TimeUnit.DAYS.toSeconds(number);
                    break;
                case "h":
                    ticks += TimeUnit.HOURS.toSeconds(number);
                    break;
                case "m":
                    ticks += TimeUnit.MINUTES.toSeconds(number);
                    break;
                case "s":
                    ticks += number;
                    break;
                default:
                    break;
            }
        }

        return (int) Math.min(ticks * 20, Integer.MAX_VALUE);
    }

    public void setDuration(String duration) {
        this.durationTextBox.setText(duration);
        this.durationTextBox.setCursorToStart(false);
    }

    public void setDuration(int duration) {
        String value;

        if (duration == -1)
            value = "inf";
        else if (duration % 20 == 0)
            value = getTimeFormat(duration);
        else
            value = String.valueOf(duration);

        this.setDuration(value);
    }

    public static String getTimeFormat(int duration) {
        duration /= 20;
        long days = TimeUnit.SECONDS.toDays(duration);
        long daysToSeconds = TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(duration - daysToSeconds);
        long hoursToSeconds = TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(duration - hoursToSeconds - daysToSeconds);

        long seconds = duration - TimeUnit.MINUTES.toSeconds(minutes) - hoursToSeconds - daysToSeconds;

        StringBuilder sb = new StringBuilder();

        if (days > 0)
            sb.append(days).append("d ");

        if (hours > 0)
            sb.append(hours).append("h ");

        if (minutes > 0)
            sb.append(minutes).append("m ");

        if (seconds > 0 || duration == 0)
            sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public int getDuration() {
        return (int) this.durationTextBox.parsedValue();
    }

}
