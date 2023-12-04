//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.port;

import com.epton.sdk.a.HexByteConverter;
import com.epton.sdk.callback.ResultCallBack;
import com.releasetech.multidevice.Tool.Utils;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ADH815Port {
    private static final String af = "ADH815Port";
    private static final String TAG = "[ADH815Port]";
    private static int ag = 100;
    private static Timer ah;
    private static Timer ai;
    private static Timer aj;
    static ResultCallBack a;
    static ResultCallBack b;
    static ResultCallBack c;
    static int d;
    static int e;
    static int f;
    static int g;
    static int h;
    static int i;
    static int j;
    static int k;
    static int l;
    public static int sensorState;
    public static int sensorState2;
    public static int sensorState3;
    public static int ADH815State = -1;
    public static int ADH815State2 = -1;
    public static int ADH815State3 = -1;
    static boolean m;
    static boolean n;
    static boolean o;
    static boolean p;
    static boolean q;
    static boolean r;
    static boolean s;
    static boolean t;
    static boolean u;
    static boolean v;
    static boolean w;
    static boolean x;
    static boolean y;
    static boolean z;
    static boolean A;
    static boolean B;
    static boolean C;
    static boolean D;
    static boolean E = true;
    static boolean F = true;
    static boolean G = true;
    static boolean H;
    static boolean I;
    static boolean J;
    static boolean K;
    static boolean L;
    static boolean M;
    static boolean N;
    static boolean O;
    static boolean P;
    static boolean Q;
    static boolean R;
    static boolean S;
    static boolean T;
    static boolean U;
    static String V;
    static String W;
    static String X;
    static String Y;
    static String Z;
    static String aa;
    static String ab;
    static String ac;
    static String ad;
    public static String curRunMode;
    public static String curRunMode2;
    public static String curRunMode3;
    public static byte curTemp;
    public static byte curTemp2;
    public static byte curTemp3;
    static String ae;

    public ADH815Port() {
    }

    static void a(int var0, ResultCallBack var1) {
        int var2;
        String var3;
        if (var0 >= 0 && var0 < ag) {
            m = true;
            j = var0;
            var2 = var0 % ag;
            var3 = Integer.toHexString(var2);
            if (var3.length() < 2) {
                ab = "0" + var3;
            } else {
                ab = var3;
            }

            a = var1;
            H = true;
            if (PortController.a == null && a != null) {
//                a.onFailure(j, "01", "os为空");
                a.onFailure(j, "01", "os 가 null 입니다.");
                m = false;
                H = false;
                if (ah != null) {
                    ah.cancel();
                    ah.purge();
                    ah = null;
                }
            }

            if (ah != null) {
                ah.cancel();
                ah.purge();
                ah = null;
            }

            ah = new Timer();
            ah.schedule(new TimerTask() {
                public void run() {
                    if (ADH815Port.H && ADH815Port.a != null) {
//                        ADH815Port.a.onFailure(ADH815Port.j, "02", "超时");
                        ADH815Port.a.onFailure(ADH815Port.j, "02", "시간 초과");
                        ADH815Port.m = false;
                        ADH815Port.ADH815State = 0;
                    }

                }
            }, 15000L);
            p = true;
        } else if (var0 >= ag && var0 < ag * 2) {
            n = true;
            k = var0;
            var2 = var0 % ag;
            var3 = Integer.toHexString(var2);
            if (var3.length() < 2) {
                ac = "0" + var3;
            } else {
                ac = var3;
            }

            b = var1;
            I = true;
            if (PortController.a == null && b != null) {
//                b.onFailure(k, "01", "os为空");
                b.onFailure(k, "01", "os 가 null 입니다.");
                n = false;
                I = false;
                if (ai != null) {
                    ai.cancel();
                    ai.purge();
                    ai = null;
                }
            }

            if (ai != null) {
                ai.cancel();
                ai.purge();
                ai = null;
            }

            ai = new Timer();
            ai.schedule(new TimerTask() {
                public void run() {
                    if (ADH815Port.I && ADH815Port.b != null) {
//                        ADH815Port.b.onFailure(ADH815Port.k, "02", "超时");
                        ADH815Port.b.onFailure(ADH815Port.k, "02", "시간 초과");
                        ADH815Port.n = false;
                        ADH815Port.ADH815State2 = 0;
                    }

                }
            }, 15000L);
            q = true;
        } else if (var0 >= ag * 2) {
            o = true;
            l = var0;
            var2 = var0 % ag;
            var3 = Integer.toHexString(var2);
            if (var3.length() < 2) {
                ad = "0" + var3;
            } else {
                ad = var3;
            }

            c = var1;
            J = true;
            if (PortController.a == null && c != null) {
//                c.onFailure(l, "01", "os为空");
                c.onFailure(l, "01", "os 가 null 입니다.");
                o = false;
                J = false;
                if (aj != null) {
                    aj.cancel();
                    aj.purge();
                    aj = null;
                }
            }

            if (aj != null) {
                aj.cancel();
                aj.purge();
                aj = null;
            }

            aj = new Timer();
            aj.schedule(new TimerTask() {
                public void run() {
                    if (ADH815Port.J && ADH815Port.c != null) {
//                        ADH815Port.c.onFailure(ADH815Port.l, "02", "超时");
                        ADH815Port.c.onFailure(ADH815Port.l, "02", "시간 초과");
                        ADH815Port.o = false;
                        ADH815Port.ADH815State3 = 0;
                    }

                }
            }, 15000L);
            r = true;
        }

    }

    static void a(String var0) {
        String var1 = ae + "05" + var0;
        String var2 = PortController.a(HexByteConverter.hexToByteArray(var1));
        String var3 = var1 + var2;
//        PortController.a(var3, "出货");
        PortController.a(var3, "출하");
//        Log.d("ADH815Port", "出货:" + " ".join(" ", var3.split("(?<=\\G.{2})")));
        Utils.logD(TAG, "출하:" + " ".join(" ", var3.split("(?<=\\G.{2})")));
        if (PortController.b) {
//            Utils.logD(TAG, "execute:" + " ".join(" ", var3.split("(?<=\\G.{2})")));
            Utils.logD(TAG, "실행:" + " ".join(" ", var3.split("(?<=\\G.{2})")));
        }

    }

    static void a() {
        String var0 = ae + "03";
        String var1 = PortController.a(HexByteConverter.hexToByteArray(var0));
        String var2 = var0 + var1;
//        PortController.a(var2, "查询");
        PortController.a(var2, "조회");
    }

    static void b() {
        String var0 = ae + "06";
        String var1 = PortController.a(HexByteConverter.hexToByteArray(var0));
        String var2 = var0 + var1;
//        PortController.a(var2, "重置");
        PortController.a(var2, "초기화");
//        Log.d("ADH815Port", "重置:" + var2);
        Utils.logD("ADH815Port", "초기화:" + var2);
        if (PortController.b) {
            Utils.logD(TAG, "수신 응답:" + var2);
        }

    }

    static void a(int var0, int var1) {
        String var2 = String.format(Locale.CHINA, "%02d", var1);
        String var3 = HexByteConverter.byteArrayToHex(HexByteConverter.intToByteArray(var0));
        String var4 = ae + "04" + var2 + var3;
        String var5 = PortController.a(HexByteConverter.hexToByteArray(var4));
//        PortController.a(var4 + var5, "set temp value");
        PortController.a(var4 + var5, "온도 설정");
        if (PortController.b) {
//            Utils.logD(TAG, "set temp value:" + var4 + var5);
            Utils.logD(TAG, "온도 설정:" + var4 + var5);
        }

    }

    static void a(String var0, String var1) {
        String var2 = ae + "21" + var0 + "00";
        String var3 = PortController.a(HexByteConverter.hexToByteArray(var2));
        String var4 = var2 + var3;
        PortController.a(var4, var1);
        if (PortController.b) {
            Utils.logD(TAG, var1 + var4);
        }

    }

    static void c() {
        String var0 = ae + "20";
        String var1 = PortController.a(HexByteConverter.hexToByteArray(var0));
        String var2 = var0 + var1;
//        PortController.a(var2, "读取驱动板运行模式");
        PortController.a(var2, "드라이브 보드 운영 모드 읽기");
    }
}
