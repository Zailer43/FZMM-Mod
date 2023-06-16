package fzmm.zailer.me.client.toast;

import fzmm.zailer.me.client.toast.status.IStatus;
import fzmm.zailer.me.client.toast.status.PlayerStatueStatus;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class LoadingPlayerStatueToast extends AbstractStatusToast {
    private IStatus status;
    private boolean finished;
    private boolean isSecondTry;
    private long loadedTime;
    private int partsGenerated;
    private final int totalToGenerate;
    private int currentErrors;
    private int delayToNextStatue;
    private String partName;

    public LoadingPlayerStatueToast(int totalPartsToGenerate) {
        this.status = PlayerStatueStatus.LOADING;
        this.finished = false;
        this.isSecondTry = false;
        this.loadedTime = -1;
        this.partsGenerated = 0;
        this.totalToGenerate = totalPartsToGenerate;
        this.currentErrors = 0;
        this.delayToNextStatue = 0;
        this.partName = "error";
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.finished && this.loadedTime == -1)
            this.loadedTime = startTime;

        super.draw(context, manager, startTime);

        return this.finished && (startTime - this.loadedTime) > 3000 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public IStatus getStatus() {
        return this.status;
    }

    @Override
    public Text getDetails() {
        return this.status.getDetailsTranslation(
                this.partsGenerated,
                this.currentErrors,
                this.totalToGenerate,
                this.delayToNextStatue,
                this.partName
        );
    }

    public void setDelayToNextStatue(int delayInSeconds) {
        this.delayToNextStatue = delayInSeconds;
    }

    public void partName(String name) {
        this.partName = name;
    }

    public void generated() {
        this.partsGenerated++;

        if (this.isSecondTry)
            this.currentErrors--;
    }

    public void error() {
        if (!this.isSecondTry)
            this.currentErrors++;
    }

    public void secondTry() {
        this.status = PlayerStatueStatus.LOADING_SECOND_TRY;
        this.isSecondTry = true;
    }

    public void finish() {
        this.finished = true;
        this.updateStatus();
    }

    public void updateStatus() {
        this.status = this.partsGenerated == this.totalToGenerate ? PlayerStatueStatus.SUCCESSFUL : PlayerStatueStatus.ERROR;
    }
}
