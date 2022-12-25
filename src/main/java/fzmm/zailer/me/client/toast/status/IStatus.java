package fzmm.zailer.me.client.toast.status;

import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.text.Text;

public interface IStatus {

    Text getStatusTranslation();

    Text getDetailsTranslation(Object... args);

    Icon getIcon();

    int getOutlineColor();

    int getBackgroundColor();
}
