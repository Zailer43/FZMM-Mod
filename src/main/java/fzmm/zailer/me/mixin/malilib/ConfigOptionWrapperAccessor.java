package fzmm.zailer.me.mixin.malilib;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfigOptionWrapper.class)
public interface ConfigOptionWrapperAccessor {

    @Accessor(remap = false)
    void setType(ConfigOptionWrapper.Type type);

    @Accessor(remap = false)
    void setConfig(IConfigBase config);

    @Accessor(remap = false)
    void setLabel(String label);
}