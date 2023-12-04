//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.port;

import com.epton.sdk.a.HexByteConverter;

import java.util.Timer;

public class ADH812Port {
    private static final String TAG = "[ADH812Port]";
    private static Timer H;
    static int a;
    static int b;
    static int c;
    static int d;
    static int e;
    static int f;
    static int g;
    public static int doorCloseLimiter;
    public static int bottomLimiter;
    public static int topLimiter;
    public static int magneticSwitch;
    public static int checkPickSensor1;
    public static int checkPickSensor2;
    static boolean h;
    static boolean i;
    static boolean j;
    static boolean k;
    static boolean l;
    static boolean m;
    static boolean n;
    static boolean o;
    static boolean p = true;
    static boolean q;
    static boolean r;
    static boolean s;
    static boolean t;
    static boolean u;
    static boolean v;
    static String w;
    static String x;
    static String y;
    static String z;
    static String A;
    public static String commonError;
    public static String haltError;
    public static String debugBuffer;
    public static int curLayerCount;
    static String B;
    static boolean C;
    static boolean D;
    public static byte boardState;
    public static byte currentLayer;
    static short E;
    public static short cur812Temp;
    public static short platformCoordinate;
    static int[] F = new int[10];
    static int[] G;

    public ADH812Port() {
    }

    static void a() {
        byte[] var0 = new byte[]{10, 1};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "查询ID");
        PortController.a(var2, "쿼리ID");
    }

    static synchronized void b() {
        byte[] var0 = new byte[]{10, 3};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "轮询");
        PortController.a(var2, "폴링");
    }

    static void a(String var0, int var1) {
        String var2 = Integer.toHexString(var1);
        if (var2.length() % 2 != 0) {
            var2 = "0" + var2;
        }

        if (var2.length() < 4) {
            var2 = "00" + var2;
        }

        byte[] var3 = new byte[]{10, 4, HexByteConverter.hexToByteArray(var0)[0], HexByteConverter.hexToByteArray(var2)[0], HexByteConverter.hexToByteArray(var2)[1]};
        String var4 = PortController.a(var3);
        String var5 = HexByteConverter.byteArrayToHex(var3) + var4 + "0D0A";
//        PortController.a(var5, "温控");
        PortController.a(var5, "온도제어");
    }

    static void a(int var0) {
        String var1 = Integer.toHexString(var0);
        var1 = var1.length() < 2 ? "0" + var1 : var1;
        byte[] var2 = new byte[]{10, 5, HexByteConverter.hexToByteArray(var1)[0]};
        String var3 = PortController.a(var2);
        String var4 = HexByteConverter.byteArrayToInt(var2) + var3 + "0D0A";
//        PortController.a(var4, "升降梯定位");
        PortController.a(var4, "엘리베이터 위치");
    }

    static void c() {
        byte[] var0 = new byte[]{10, 6};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "升降梯归位");
        PortController.a(var2, "엘리베이터 초기화");
    }

    static void d() {
        byte[] var0 = new byte[]{10, 7};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "扫描------------------------------------");
        PortController.a(var2, "스캔------------------------------------");
    }

    static void a(String var0) {
        byte[] var1 = new byte[]{10, 8, HexByteConverter.hexToByteArray(var0)[0]};
        String var2 = PortController.a(var1);
        String var3 = HexByteConverter.byteArrayToHex(var1) + var2 + "0D0A";
//        PortController.a(var3, "出货完成");
        PortController.a(var3, "출하 완료");
    }

    static void e() {
        byte[] var0 = new byte[]{10, 9};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "故障清除");
        PortController.a(var2, "에러 클리어");
    }

    static void b(String var0) {
        byte[] var1 = new byte[]{10, 16, HexByteConverter.hexToByteArray(var0)[0]};
        String var2 = PortController.a(var1);
        String var3 = HexByteConverter.byteArrayToHex(var1) + var2 + "0D0A";
//        PortController.a(var3, "调试开关");
        PortController.a(var3, "디버그 스위치");
    }

    static void f() {
        byte[] var0 = new byte[]{10, 11};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "读取扫描数据");
        PortController.a(var2, "스캔 데이터 읽기");
    }

    static void a(int[] var0) {
        String var1 = "0A0C";

        String var3;
        for(int var2 = 0; var2 < 10; ++var2) {
            var3 = HexByteConverter.ensureProperLength(Integer.toHexString(var0[var2]));
            var1 = var1 + var3;
        }

        String var4 = PortController.a(HexByteConverter.hexToByteArray(var1));
        var3 = var1 + var4 + "0D0A";
//        PortController.a(var3, "设置层坐标");
        PortController.a(var3, "층 좌표 설정");
    }

    static void a(int var0, String var1) {
        String var2 = Integer.toHexString(var0);
        String var3 = var2.length() < 2 ? "0" + var2 : var2;
        byte[] var4 = new byte[]{10, 17, HexByteConverter.hexToByteArray(var3)[0], HexByteConverter.hexToByteArray(var1)[0]};
        String var5 = PortController.a(var4);
        String var6 = HexByteConverter.byteArrayToHex(var4) + var5 + "0D0A";
//        PortController.a(var6, "手动调试");
        PortController.a(var6, "수동 디버그");
    }

    static void g() {
        byte[] var0 = new byte[]{10, 19};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "读输入点");
        PortController.a(var2, "입력 포인트 읽기");
    }

    static void c(String var0) {
        byte[] var1 = new byte[]{10, 20, HexByteConverter.hexToByteArray(var0)[0]};
        String var2 = PortController.a(var1);
        String var3 = HexByteConverter.byteArrayToHex(var1) + var2 + "0D0A";
//        PortController.a(var3, "强制Do");
        PortController.a(var3, "강제 Do");
    }

    static void h() {
        byte[] var0 = new byte[]{10, 48};
        String var1 = PortController.a(var0);
        String var2 = HexByteConverter.byteArrayToHex(var0) + var1 + "0D0A";
//        PortController.a(var2, "开始运行");
        PortController.a(var2, "운행 시작");
    }
}
