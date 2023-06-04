package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.toast.status.IStatus;
import fzmm.zailer.me.client.toast.status.PlayerStatueStatus;
import net.minecraft.text.Text;

public class UpdatedPlayerStatueToast extends AbstractStatusToast {


    @Override
    public IStatus getStatus() {
        return PlayerStatueStatus.UPDATED;
    }

    @Override
    public Text getDetails() {
        return PlayerStatueStatus.UPDATED.getDetailsTranslation();
    }
}
