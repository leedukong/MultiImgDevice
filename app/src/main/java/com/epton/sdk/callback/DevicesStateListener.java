//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.callback;

public interface DevicesStateListener {
    void onStateChanged(int[] var1, byte[] var2);

    void onModeChanged();

    void onDisconnected(int var1);
}
