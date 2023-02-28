package fzmm.zailer.me.client.logic.headGenerator.model;

import io.wispforest.owo.ui.core.Color;

public interface IPaintableEntry {

    boolean isPaintable();

    void putColor(String key, Color color);

    Color getColor(String key);
}
