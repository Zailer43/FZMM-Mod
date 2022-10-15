package fzmm.zailer.me.client.toast;

import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.text.Text;

public interface IStatus {

    Text getStatusTranslation();

    Text getDetailsTranslation();

    Icon getIcon();

    int getOutlineColor();

    int getBackgroundColor();
}
