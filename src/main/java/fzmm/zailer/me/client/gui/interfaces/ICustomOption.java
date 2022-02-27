package fzmm.zailer.me.client.gui.interfaces;

import fi.dy.masa.malilib.config.IConfigValue;
import fzmm.zailer.me.client.gui.enums.CustomConfigType;

public interface ICustomOption extends IConfigValue {

    CustomConfigType getConfigType();
}
