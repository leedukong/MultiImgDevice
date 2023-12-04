//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.callback;

public interface ADH812StateListener {
    void on812StateReturned(int var1);

    void onVersionReturn(boolean var1, String var2);

    void on812CoordinateReturned(int[] var1);

    void onADH812Disconnected();
}
