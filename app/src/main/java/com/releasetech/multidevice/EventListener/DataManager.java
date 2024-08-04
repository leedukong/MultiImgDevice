package com.releasetech.multidevice.EventListener;

public class DataManager {
    OnUpdateListener onUpdateListener;

    protected void update() {
        if (onUpdateListener != null)
            onUpdateListener.onUpdate(this);
    }

    public void setOnUpdateListner(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public interface OnUpdateListener {
        void onUpdate(DataManager dataManager);
    }
}
