package fzmm.zailer.me.client.gui.wrapper;

import fi.dy.masa.malilib.config.IConfigBase;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OptionWrapper {
    private final Type type;
    @Nullable
    private final IConfigBase config;
    @Nullable
    private final String key;
    @Nullable
    private String[] translationValues;
    private boolean hide;

    public OptionWrapper(@NotNull IConfigBase config) {
        this.type = Type.OPTION;
        this.config = config;
        this.key = null;
        this.translationValues = null;
        this.hide = false;
    }

    public OptionWrapper(@NotNull String key) {
        this(key, new String[0], false);
    }

    public OptionWrapper(@NotNull String key, boolean hide) {
        this(key, new String[0], hide);
    }

    public OptionWrapper(@NotNull String key, @NotNull String[] translationValues, boolean hide) {
        this.type = Type.LABEL;
        this.config = null;
        this.key = key;
        this.translationValues = translationValues;
        this.hide = hide;
    }

    public Type getType() {
        return this.type;
    }

    @Nullable
    public IConfigBase getConfig() {
        return this.config;
    }

    public void setTranslationValues(String... translationsValues) {
        this.translationValues = translationsValues;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public boolean isHide() {
        return this.hide;
    }

    @Nullable
    public String getLabel() {
        if (this.key == null)
            return "";
        TranslatableText translate = this.translationValues == null ?
                new TranslatableText(this.key) :
                new TranslatableText(this.key, (Object[]) this.translationValues);

        return translate.getString();
    }

    public static List<OptionWrapper> createFor(Collection<? extends IConfigBase> configs) {
        List<OptionWrapper> list = new ArrayList<>();

        for (IConfigBase config : configs) {
            list.add(new OptionWrapper(config));
        }

        return list;
    }

    public enum Type {
        OPTION,
        LABEL
    }
}