//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.port;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.epton.sdk.a.ChecksumCalculator;
import com.epton.sdk.a.FileLogger;
import com.epton.sdk.a.HexByteConverter;
import com.epton.sdk.a.SerialPortManager;
import com.epton.sdk.callback.ADH812ResultListener;
import com.epton.sdk.callback.ADH812StateListener;
import com.epton.sdk.callback.DevicesStateListener;
import com.epton.sdk.callback.ResultCallBack;
import com.releasetech.multidevice.Tool.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android_serialport_api.SerialPort;

public class PortController {
    private static final String c = "PortController";
    private static final String TAG = "[PORT CONTROLLER]";
    private static byte[] d;
    private static int e;
    private static int f;
    private static int g;
    private static int h;
    private static int i;
    private static int j;
    private static int k;

    private static SerialPortManager serialPortManager = null;
    private static SerialPort l;
    static OutputStream a;
    private static InputStream m;
    static boolean b;
    private static boolean n;
    private static boolean o = true;
    private static DevicesStateListener p;
    private static ADH812StateListener q;
    private static ADH812ResultListener r;
    private static boolean s;

    public PortController() {
    }

    public static void setAdh812StateListener(ADH812StateListener var0) {
        q = var0;
    }

    public static void setAdh812ResultListener(ADH812ResultListener var0) {
        r = var0;
    }

    public static boolean init(Context var0, String var1, int var2, int var3) {
        if (serialPortManager != null) return true;
//        enableLog(true);
        try {
            o = true;
            s = false;
            serialPortManager = new SerialPortManager();
            l = serialPortManager.getSerialPortByName(var1, var2, var3);
            m = l.getInputStream();
            a = l.getOutputStream();
            a var5 = new a();
            var5.start();
            b var6 = new b();
            var6.start();
            return true;
        } catch (NullPointerException var8) {
            var8.printStackTrace();
            return false;
        }
    }

    @NonNull
    static String a(byte[] var0) {
        int var1 = ChecksumCalculator.calculateChecksum(var0);
        String var2 = Integer.toHexString(var1);
        if (var2.length() % 2 != 0) {
            var2 = "0" + var2;
        }

        if (var2.length() < 4) {
            var2 = "00" + var2;
        }

        String var3 = var2.substring(2);
        String var4 = var2.substring(0, 2);
        var2 = var3 + var4;
        return var2;
    }

    static synchronized void a(String var0, String var1) {
        try {
            byte[] var2 = HexByteConverter.hexToByteArray(var0);
            if (n) {
//                Log.d("PortController", "发送" + var1 + "数据:" + var0);
                if(!var1.equals("조회"))
                    Utils.logD(TAG, "발송" + var1 + "데이터:" + var0);
            }

            a.write(var2);
            a.flush();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    private static synchronized void b(byte[] var0, int var1) {
        d = HexByteConverter.mergeByteArrays(d, var0, var1);
        if (d != null) {
            if (d.length > 1) {
                if (d[0] == 0) {
                    if (g == 1) {
                        h = 0;
                    } else if (g == 2) {
                        i = 0;
                    } else if (g == 3) {
                        j = 0;
                    }
                } else if (d[0] == 10) {
                    k = 0;
                }
            }

            String var2 = HexByteConverter.byteArrayToHex(d);
            e += var1;
            if (d.length == 4 && d[1] == 6) {
                if (b) {
//                    FileLogger.logMessage("重置返回数据" + var2);
                    Utils.logD(TAG, "리셋 응답 데이터" + var2);
                }

                a("重置返回数据");
            } else if (d.length == 5 && d[1] == 5) {
                if (b) {
//                    FileLogger.logMessage("启动电机返回数据" + var2);
                    Utils.logD(TAG, "모터 시작 응답 데이터" + var2);
                }

                a("启动电机返回数据");
            } else if (d.length == 6 && d[1] == 33) {
                if (b) {
//                    FileLogger.logMessage("815设置模式返回数据" + var2);
                    Utils.logD(TAG, "815모드 설정 응답 데이터" + var2);
                }

                if (n) {
//                    Log.d("PortController", "815设置模式" + var2);
                    Utils.logD(TAG, "815모드 설정" + var2);
                }

                if (d[0] == 1) {
                    if (d[2] != 0 && d[2] != 16 && (d[2] & 255) != 32 && (d[2] & 255) != 8 && (d[2] & 255) != 24 && (d[2] & 255) != 40) {
                        if ((d[2] & 255) == 128 || (d[2] & 255) == 144 || (d[2] & 255) == 160 || (d[2] & 255) == 176 || (d[2] & 255) == 136 || (d[2] & 255) == 152 || (d[2] & 255) == 168 || (d[2] & 255) == 184) {
                            ADH815Port.S = true;
                        }
                    } else {
                        ADH815Port.S = false;
                    }
                } else if (d[0] == 2) {
                    if (d[2] != 0 && d[2] != 16 && (d[2] & 255) != 32 && (d[2] & 255) != 8 && (d[2] & 255) != 24 && (d[2] & 255) != 40) {
                        if ((d[2] & 255) == 128 || (d[2] & 255) == 144 || (d[2] & 255) == 160 || (d[2] & 255) == 176 || (d[2] & 255) == 136 || (d[2] & 255) == 152 || (d[2] & 255) == 168 || (d[2] & 255) == 184) {
                            ADH815Port.T = true;
                        }
                    } else {
                        ADH815Port.T = false;
                    }
                } else if (d[0] == 3) {
                    if (d[2] != 0 && d[2] != 16 && (d[2] & 255) != 32 && (d[2] & 255) != 8 && (d[2] & 255) != 24 && (d[2] & 255) != 40) {
                        if ((d[2] & 255) == 128 || (d[2] & 255) == 144 || (d[2] & 255) == 160 || (d[2] & 255) == 176 || (d[2] & 255) == 136 || (d[2] & 255) == 152 || (d[2] & 255) == 168 || (d[2] & 255) == 184) {
                            ADH815Port.U = true;
                        }
                    } else {
                        ADH815Port.U = false;
                    }
                }

                d = null;
                e = 0;
            } else if (d.length == 7 && d[1] == 4) {
                if (n) {
//                    Log.d("PortController", "收到设置温度返回指令" + var2);
                    Utils.logD(TAG, "수신 설정 온도 응답 전문" + var2);
                }

                if (b) {
//                    FileLogger.logMessage("收到设置温度返回指令" + var2);
                    Utils.logD(TAG, "수신 설정 온도 응답 전문" + var2);
                }

                d = null;
                e = 0;
            } else if (d.length == 6 && d[1] == 32) {
                if (n) {
//                    Log.d("PortController", "收到读取模式返回指令" + var2);
                    Utils.logD(TAG, "수신 모드 읽기 응답 전문" + var2);
                }

                if (b) {
//                    FileLogger.logMessage("收到读取模式返回指令" + var2);
                    Utils.logD(TAG, "수신 모드 읽기 응답 전문" + var2);
                }

                String var4 = var2.substring(4, 6);
                if (g == 1) {
                    ADH815Port.curRunMode = var4;
                } else if (g == 2) {
                    ADH815Port.curRunMode2 = var4;
                } else {
                    ADH815Port.curRunMode3 = var4;
                }

                p.onModeChanged();
                d = null;
                e = 0;
            } else if (d.length == 6 && d[1] == 33) {
                if (n) {
//                    Log.d("PortController", "收到设置模式返回指令" + var2);
                    Utils.logD(TAG, "수신 모드 설정 응답 전문" + var2);
                }

                if (b) {
//                    FileLogger.logMessage("收到设置模式返回指令" + var2);
                    Utils.logD(TAG, "수신 모드 설정 응답 전문" + var2);
                }

                d = null;
                e = 0;
            } else if (d.length == 13 && d[0] == 0 && d[1] == 3) {
                if (g == 1) {
                    ADH815Port.ADH815State = d[2];
                    ADH815Port.sensorState = d[4] >> 2 & 1;
                    ADH815Port.curTemp = d[10];
                } else if (g == 2) {
                    ADH815Port.ADH815State2 = d[2];
                    ADH815Port.sensorState2 = d[4] >> 2 & 1;
                    ADH815Port.curTemp2 = d[10];
                } else {
                    ADH815Port.ADH815State3 = d[2];
                    ADH815Port.sensorState3 = d[4] >> 2 & 1;
                    ADH815Port.curTemp3 = d[10];
                }

                if (n) {
//                    Log.d("PortController", "轮询返回数据" + var2);
                    Utils.logD(TAG, "폴링 응답 데이터" + var2);
                }

                if (d[2] == 2) {
                    if (n) {
//                        Log.d("PortController", "货道号" + ADH815Port.j + "，出货结束,msg:" + var2);
                        Utils.logD(TAG, "채널 번호" + ADH815Port.j + "，출하 종료, msg:" + var2);
                    }

                    if (b) {
//                        FileLogger.logMessage("出货结束,msg:" + var2);
                        Utils.logD(TAG, "출하 종료, msg:" + var2);
                    }

                    if (g == 1) {
                        ADH815Port.H = false;
                        ADH815Port.s = true;
                        ADH815Port.m = false;
                        a(ADH815Port.a, g, var2);
                    } else if (g == 2) {
                        ADH815Port.I = false;
                        ADH815Port.t = true;
                        ADH815Port.n = false;
                        a(ADH815Port.b, g, var2);
                    } else {
                        ADH815Port.J = false;
                        ADH815Port.u = true;
                        ADH815Port.o = false;
                        a(ADH815Port.c, g, var2);
                    }
                } else if (d[2] == 1) {
                    if (n) {
//                        Log.d("PortController", "出货中" + var2);
                        Utils.logD(TAG, "출하 중" + var2);
                    }
                } else if (d[2] == 0 && n) {
//                    Log.d("PortController", "815空闲" + var2);
                    Utils.logD(TAG, "815비어있음" + var2);
                }

                if (p != null) {
                    p.onStateChanged(new int[]{ADH815Port.ADH815State, ADH815Port.ADH815State2, ADH815Port.ADH815State3}, new byte[]{ADH815Port.curTemp, ADH815Port.curTemp2, ADH815Port.curTemp3});
                }

                d = null;
                e = 0;
            } else if (e == 27 && d[0] == 10 && d[1] == 1) {
                ADH812Port.v = (d[2] & 1) == 1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    ADH812Port.B = HexByteConverter.hexToCharString(HexByteConverter.byteArrayToHex(Arrays.copyOfRange(d, 7, 22)));
                }
                if (q != null) {
                    q.onVersionReturn(ADH812Port.v, ADH812Port.B);
                }

//                a("获取812版本");
                a("812 버전 가져 오기");
            } else if (e == 33 && d[1] == 3) {
                ADH812Port.boardState = d[2];
                ADH812Port.curLayerCount = d[4];
                ADH812Port.doorCloseLimiter = d[5] >> 0 & 1;
                ADH812Port.g = d[5] >> 1 & 1;
                ADH812Port.bottomLimiter = d[5] >> 2 & 1;
                ADH812Port.topLimiter = d[5] >> 3 & 1;
                ADH812Port.magneticSwitch = d[5] >> 4 & 1;
                ADH812Port.checkPickSensor1 = d[5] >> 5 & 1;
                ADH812Port.checkPickSensor2 = d[5] >> 6 & 1;
                ADH812Port.commonError = var2.substring(12, 16);
                ADH812Port.haltError = var2.substring(16, 20);
                ADH812Port.cur812Temp = (short) HexByteConverter.byteArrayToInt(new byte[]{0, 0, d[10], d[11]});
                ADH812Port.currentLayer = d[12];
                ADH812Port.platformCoordinate = (short) HexByteConverter.byteArrayToInt(new byte[]{0, 0, d[15], d[16]});
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    ADH812Port.debugBuffer = HexByteConverter.byteArrayToHex(Arrays.copyOfRange(d, 17, 27));
                }
                if (ADH812Port.boardState == 0) {
//                    a("812空闲");
                    a("812 비어있음");
                } else if (ADH812Port.boardState == 1) {
                    if ((d[3] >> 1 & 1) == 1) {
                        if (n) {
//                            Log.d("PortControllerTest", "升降梯到位");
                            Utils.logD(TAG, "승강기 도착");
                        }

                        if (r != null) {
                            r.on812Located();
                        }
                    } else if (n) {
//                        Log.d("PortControllerTest", "未到位");
                        Utils.logD(TAG, "미도착");
                    }

//                    a("运行中");
                    a("운행 중");
                } else if (ADH812Port.boardState == 2) {
                    ADH812Port.C = false;
                    ADH812Port.h = true;
                    if (r != null) {
                        r.on812Success(ADH812Port.a);
                    }

//                    a("执行成功");
                    a("구동 성공");
                } else if (ADH812Port.boardState == 3) {
                    ADH812Port.C = false;
                    ADH812Port.h = true;
                    if (r != null) {
                        r.on812Failure(ADH815Port.j);
                    }

//                    a("执行失败");
                    a("구동 실패");
                } else if (ADH812Port.boardState == 4) {
//                    a("初始化状态");
                    a("초기화 중");
                } else if (ADH812Port.boardState == 5) {
//                    a("运行即将结束");
                    a("운행 대기");
                } else if (ADH812Port.boardState == 6) {
//                    a("扫描中");
                    a("스캔 중");
                } else if (ADH812Port.boardState == 7) {
//                    a("扫描完成");
                    a("스캔 완료");
                } else if (ADH812Port.boardState == 8) {
//                    a("故障状态");
                    a("에러 발생");
                } else if (ADH812Port.boardState == 9) {
//                    a("调试状态");
                    a("디버깅");
                } else if (ADH812Port.boardState == 16) {
//                    a("门打开状态");
                    a("문 열림");
                } else {
//                    a("未知状态");
                    a("알 수 없는 오류");
                }

                if (q != null) {
                    q.on812StateReturned(ADH812Port.boardState);
                }
            } else if (e == 6 && d[1] == 4) {
//                a("温控返回");
                a("온도 제어 응답");
            } else if (e == 7 && d[1] == 5) {
//                a("定位指令返回");
                a("위치 명령 응답");
            } else if (e == 6 && d[1] == 6) {
//                a("升降梯归位");
                a("승강기 복귀");
            } else if (e == 6 && d[1] == 7) {
//                a("扫描返回");
                a("스캔 응답");
            } else if (e == 6 && d[1] == 8) {
//                a("出货完成返回");
                a("출하 완료 응답");
            } else if (e == 6 && d[1] == 9) {
//                a("故障清除返回");
                a("오류 해결 응답");
            } else if (e == 6 && d[1] == 16) {
//                a("调试开关返回");
                a("디버깅 스위치 응답");
            } else if (e == 6 && d[1] == 17) {
//                a("手动调试返回");
                a("수동 디버깅 응답");
            } else if (e == 6 && d[1] == 19) {
//                a("读输入点返回");
                a("입력 포인트 응답");
            } else if (e == 7 && d[1] == 20) {
//                a("强制do返回");
                a("강제 do 응답");
            } else if (e == 28 && d[1] == 11) {
                ADH812Port.E = (short) HexByteConverter.byteArrayToInt(new byte[]{0, 0, d[2], d[3]});
                if (ADH812Port.G == null) {
                    ADH812Port.G = new int[10];
                }

                for (int var3 = 0; var3 < ADH812Port.curLayerCount; ++var3) {
                    ADH812Port.G[ADH812Port.curLayerCount - 1 - var3] = HexByteConverter.byteArrayToInt(new byte[]{0, 0, d[4 + 2 * var3], d[4 + 2 * var3 + 1]});
                }

                if (q != null) {
                    q.on812CoordinateReturned(ADH812Port.G);
                }

//                a("读扫描返回");
                a("스캔 읽기 응답");
            } else if (e == 6 && d[1] == 12) {
//                a("设置层坐标返回");
                a("층 좌표 설정 응답");
            } else if (e == 6 && d[1] == 48) {
//                a("开始运行返回");
                a("운행 시작 응답");
            } else if (d.length > 33) {
//                a("未知数据");
                a("알 수 없는 데이터");
            }

        }
    }

    private static void a(String var0) {
        String var1 = HexByteConverter.byteArrayToHex(d);
        if (n) {
//            Log.d("PortController", "收到" + var0 + "指令:" + var1);
            Utils.logD(TAG, "수신" + var0 + "명령:" + var1);
        }

        d = null;
        e = 0;
    }

    private static void a(ResultCallBack var0, int var1, String var2) {
        if (var1 == 1) {
            if (ADH815Port.S) {
                d(var0, var1, var2);
            } else {
                b(var0, var1, var2);
            }
        } else if (var1 == 2) {
            if (ADH815Port.T) {
                d(var0, var1, var2);
            } else {
                b(var0, var1, var2);
            }
        } else if (var1 == 3) {
            if (ADH815Port.U) {
                d(var0, var1, var2);
            } else {
                b(var0, var1, var2);
            }
        }

    }

    private static void b(ResultCallBack var0, int var1, String var2) {
        if (d[4] == 0) {
            if (var0 != null) {
                if (var1 == 1) {
                    var0.onSuccess(ADH815Port.j, 0);
                } else if (var1 == 2) {
                    var0.onSuccess(ADH815Port.k, 0);
                } else {
                    var0.onSuccess(ADH815Port.l, 0);
                }
            }

            if (n) {
//                Log.d("PortController", "出货成功");
                Utils.logD(TAG, "출하 성공");
            }

            if (b) {
//                FileLogger.logMessage("出货成功" + var2);
                Utils.logD(TAG, "출하 성공" + var2);
            }
        } else {
            c(var0, var1, var2);
        }

    }

    private static void c(ResultCallBack var0, int var1, String var2) {
        String var3 = "99";
//        String var4 = "未知错误2";
        String var4 = "알 수 없는 오류2";
        if (n) {
            Log.d("PortController", "bufArray[4]" + d[4]);
        }

        if (d[4] == 1) {
            var3 = "04";
//            var4 = "电机过流";
            var4 = "모터 과전류";
        } else if (d[4] == 2) {
            var3 = "05";
//            var4 = "电机断线";
            var4 = "모터 단선";
        } else if (d[4] == 3) {
            var3 = "06";
//            var4 = "未检测到停止信号";
            var4 = "정지 신호를 감지하지 못함";
        }

        if (var0 != null) {
            if (var1 == 1) {
                var0.onFailure(ADH815Port.j, var3, var4);
            } else if (var1 == 2) {
                var0.onFailure(ADH815Port.k, var3, var4);
            } else {
                var0.onFailure(ADH815Port.l, var3, var4);
            }
        }

        if (n) {
//            Log.d("PortController", "出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
            Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
        }

        if (b) {
//            FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
            Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
        }

    }

    private static void d(ResultCallBack var0, int var1, String var2) {
        if (var0 != null) {
            String var3 = "99";
//            String var4 = "未知错误";
            String var4 = "알 수 없는 오류";
            int var5;
            if (var1 == 1) {
                var5 = ADH815Port.j;
            } else if (var1 == 2) {
                var5 = ADH815Port.k;
            } else {
                var5 = ADH815Port.l;
            }

            if (d[4] == 0) {
                var0.onSuccess(var5, 0);
                if (n) {
//                    Log.d("PortController", "出货成功");
                    Utils.logD(TAG, "출하 성공");
                }

                if (b) {
//                    FileLogger.logMessage("出货成功" + var2);
                    Utils.logD(TAG, "출하 성공" + var2);
                }
            } else if (d[4] == 1) {
                var0.onSuccess(var5, 1);
                if (b) {
//                    FileLogger.logMessage("出货成功" + var2);
                    Utils.logD(TAG, "출하 성공" + var2);
                }
            } else if (d[4] == 2) {
                var0.onSuccess(var5, 2);
                if (b) {
//                    FileLogger.logMessage("出货成功" + var2);
                    Utils.logD(TAG, "출하 성공" + var2);
                }
            } else if (d[4] == 3) {
                var0.onSuccess(var5, 3);
                if (b) {
//                    FileLogger.logMessage("出货成功" + var2);

                }
            } else if (d[4] == 4) {
                var3 = "11";
//                var4 = "无故障,但出货失败";
                var4 = "오류 없음, 출하 실패";
                var0.onFailure(var5, var3, var4);
                if (b) {
//                    FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
                    Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
                }
            } else if (d[4] == 5) {
                var3 = "04";
//                var4 = "电机过流";
                var4 = "모터 과전류";
                var0.onFailure(var5, var3, var4);
                if (b) {
//                    FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
                    Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
                }
            } else if (d[4] == 6) {
                var3 = "05";
//                var4 = "电机断线";
                var4 = "모터 단선";
                var0.onFailure(var5, var3, var4);
                if (b) {
//                    FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
                    Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
                }
            } else if (d[4] == 7) {
                var3 = "06";
//                var4 = "未检测到停止信号";
                var4 = "정지 신호를 감지하지 못함";
                var0.onFailure(var5, var3, var4);
                if (b) {
//                    FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + "," + var2);
                    Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + "," + var2);
                }
            } else {
                var0.onFailure(var5, var3, var4);
                if (b) {
//                    FileLogger.logMessage("出货失败,错误码:" + var3 + ",失败原因:" + var4 + ",2," + var2);
                    Utils.logD(TAG, "출하 실패, 오류 코드:" + var3 + ", 실패 원인:" + var4 + ",2," + var2);
                }
            }

        }
    }

    public static void setTemperature(int var0, int var1) {
        ADH815Port.g = var0;
        ADH815Port.d = var1;
        ADH815Port.v = true;
    }

    public static void setTemperature2(int var0, int var1) {
        ADH815Port.h = var0;
        ADH815Port.e = var1;
        ADH815Port.w = true;
    }

    public static void setTemperature3(int var0, int var1) {
        ADH815Port.i = var0;
        ADH815Port.f = var1;
        ADH815Port.x = true;
    }

    public static void set815Mode(int var0, String var1) {
        if (var0 == 1) {
            ADH815Port.V = var1;
            ADH815Port.K = true;
            ADH815Port.N = true;
        } else if (var0 == 2) {
            ADH815Port.W = var1;
            ADH815Port.L = true;
            ADH815Port.O = true;
        } else if (var0 == 3) {
            ADH815Port.X = var1;
            ADH815Port.M = true;
            ADH815Port.P = true;
        } else {
//            Log.e("PortController", "未知主板编号!");
            Utils.logD(TAG, "알 수 없는 메인 보드 번호!");
        }

    }

    public static void enableSalve(boolean var0, boolean var1) {
        ADH815Port.Q = var0;
        ADH815Port.R = var1;
    }

    public static int get815State(int var0) {
        return var0 == 0 ? ADH815Port.ADH815State : (var0 == 1 ? ADH815Port.ADH815State2 : ADH815Port.ADH815State3);
    }

    public static void outGoods(int var0, ResultCallBack var1) {
        ADH815Port.a(var0, var1);
    }

    public static void setStateListener(DevicesStateListener var0) {
        p = var0;
    }

    public static void enableLog(boolean var0) {
        n = var0;
    }

    public static void enableLoggers(Context var0, boolean var1) {
        FileLogger.initializeLogger(var0);
        b = true;
    }

    public static void enable812(boolean var0, int var1) {
        ADH812Port.b = var1;
        ADH812Port.D = var0;
    }

    public static void getADH812Version() {
        ADH812Port.p = true;
    }

    public static void liftLocation(int var0) {
        ADH812Port.e = ADH812Port.b - var0 / 10;
        ADH812Port.a = var0;
        ADH812Port.i = true;
    }

    public static void run() {
        ADH812Port.k = true;
    }

    public static void set812Temp(String var0, int var1) {
        ADH812Port.A = var0;
        ADH812Port.c = var1;
        ADH812Port.u = true;
    }

    public static void clearError() {
        ADH812Port.m = true;
    }

    public static void debugMode(boolean var0) {
        ADH812Port.x = var0 ? "01" : "00";
        ADH812Port.n = true;
    }

    public static void debugOpt(int var0, String var1) {
        ADH812Port.f = var0;
        ADH812Port.y = var1;
        ADH812Port.o = true;
    }

    public static void setCoordinate(int var0, int var1) {
        if (ADH812Port.F == null) {
            ADH812Port.F = new int[10];
        }

        ADH812Port.F[ADH812Port.b - var0] = var1;
        ADH812Port.r = true;
    }

    public static void readCoordinate() {
        ADH812Port.q = true;
    }

    public static void outOver(String var0) {
        ADH812Port.a(var0);
    }

    public static void setTotalLayerCount(int var0) {
        ADH812Port.b = var0;
    }

    public static void closeSerialPort() {
        s = true;
    }

    private static class a extends Thread {
        private a() {
        }

        public void run() {
            while (true) {
                try {
                    if (PortController.o) {
                        byte[] var1 = new byte[64];
                        if (PortController.m == null) {
                            return;
                        }

                        SystemClock.sleep(30L);
                        int var2 = PortController.m.read(var1);
                        if (var2 > 0) {
                            PortController.b(var1, var2);
                        }
                        continue;
                    }
                } catch (IndexOutOfBoundsException | IOException var3) {
                    var3.printStackTrace();
                }

                return;
            }
        }
    }

    private static class b extends Thread {
        private b() {
        }

        public void run() {
            SystemClock.sleep(1000L);

            for (; PortController.o; SystemClock.sleep(150L)) {
                if (PortController.f == 0) {
                    ADH815Port.ae = "01";
                    if (ADH815Port.s) {
                        ADH815Port.s = false;
                        ADH815Port.b();
                    } else if (ADH815Port.p) {
                        ADH815Port.p = false;
                        ADH815Port.a(ADH815Port.ab);
                    } else if (ADH815Port.v) {
                        ADH815Port.v = false;
                        ADH815Port.a(ADH815Port.d, ADH815Port.g);
                    } else if (ADH815Port.y) {
                        ADH815Port.y = false;
//                        ADH815Port.a("80", "主柜开启掉货检测");
                        ADH815Port.a("80", "메인 캐비닛이 열렸습니다.");
                    } else if (ADH815Port.B) {
                        ADH815Port.B = false;
//                        ADH815Port.a("00", "主柜关闭掉货检测");
                        ADH815Port.a("00", "메인 캐비닛이 닫혔습니다.");
                    } else if (ADH815Port.K) {
                        ADH815Port.K = false;
                        ADH815Port.a(ADH815Port.V, ADH815Port.Y);
                    } else if (ADH815Port.N) {
                        ADH815Port.N = false;
                        ADH815Port.c();
                    } else if (ADH815Port.E) {
                        ADH815Port.a();
                    }

                    PortController.g = 1;
                    if (ADH815Port.Q) {
                        PortController.f = 1;
                    } else if (ADH815Port.R) {
                        PortController.f = 2;
                    } else if (ADH812Port.D) {
                        PortController.f = 3;
                    }
                } else if (PortController.f == 1) {
                    ADH815Port.ae = "02";
                    if (ADH815Port.t) {
                        ADH815Port.t = false;
                        ADH815Port.b();
                    } else if (ADH815Port.q) {
                        ADH815Port.q = false;
                        ADH815Port.a(ADH815Port.ac);
                    } else if (ADH815Port.w) {
                        ADH815Port.w = false;
                        ADH815Port.a(ADH815Port.e, ADH815Port.h);
                    } else if (ADH815Port.z) {
                        ADH815Port.z = false;
//                        ADH815Port.a("80", "副柜1开启掉货检测");
                        ADH815Port.a("80", "서브 캐비닛 1이 열렸습니다.");
                    } else if (ADH815Port.C) {
                        ADH815Port.C = false;
//                        ADH815Port.a("00", "副柜1关闭掉货检测");
                        ADH815Port.a("00", "서브 캐비닛 1이 닫혔습니다.");
                    } else if (ADH815Port.L) {
                        ADH815Port.L = false;
                        ADH815Port.a(ADH815Port.W, ADH815Port.Z);
                    } else if (ADH815Port.O) {
                        ADH815Port.O = false;
                        ADH815Port.c();
                    } else if (ADH815Port.F) {
                        ADH815Port.a();
                    }

                    PortController.g = 2;
                    if (ADH815Port.R) {
                        PortController.f = 2;
                    } else if (ADH812Port.D) {
                        PortController.f = 3;
                    } else {
                        PortController.f = 0;
                    }
                } else if (PortController.f == 2) {
                    ADH815Port.ae = "03";
                    if (ADH815Port.u) {
                        ADH815Port.u = false;
                        ADH815Port.b();
                    } else if (ADH815Port.r) {
                        ADH815Port.r = false;
                        ADH815Port.a(ADH815Port.ad);
                    } else if (ADH815Port.x) {
                        ADH815Port.x = false;
                        ADH815Port.a(ADH815Port.f, ADH815Port.i);
                    } else if (ADH815Port.A) {
                        ADH815Port.A = false;
//                        ADH815Port.a("80", "副柜2开启掉货检测");
                        ADH815Port.a("80", "서브 캐비닛 2가 열렸습니다.");
                    } else if (ADH815Port.D) {
                        ADH815Port.D = false;
//                        ADH815Port.a("00", "副柜2关闭掉货检测");
                        ADH815Port.a("00", "서브 캐비닛 2가 닫혔습니다.");
                    } else if (ADH815Port.M) {
                        ADH815Port.M = false;
                        ADH815Port.a(ADH815Port.X, ADH815Port.aa);
                    } else if (ADH815Port.P) {
                        ADH815Port.P = false;
                        ADH815Port.c();
                    } else if (ADH815Port.G) {
                        ADH815Port.a();
                    }

                    PortController.g = 3;
                    if (ADH812Port.D) {
                        PortController.f = 3;
                    } else {
                        PortController.f = 0;
                    }
                } else if (PortController.f == 3) {
                    if (ADH812Port.p) {
                        ADH812Port.p = false;
                        ADH812Port.a();
                    } else if (ADH812Port.h) {
                        ADH812Port.h = false;
                        ADH812Port.c();
                    } else if (ADH812Port.i) {
                        ADH812Port.i = false;
                        ADH812Port.a(ADH812Port.e);
                    } else if (ADH812Port.j) {
                        ADH812Port.j = false;
                        ADH812Port.a(ADH812Port.w);
                    } else if (ADH812Port.k) {
                        ADH812Port.k = false;
                        ADH812Port.h();
                    } else if (ADH812Port.u) {
                        ADH812Port.u = false;
                        ADH812Port.a(ADH812Port.A, ADH812Port.c);
                    } else if (ADH812Port.l) {
                        ADH812Port.l = false;
                        ADH812Port.d();
                    } else if (ADH812Port.m) {
                        ADH812Port.m = false;
                        ADH812Port.e();
                    } else if (ADH812Port.n) {
                        ADH812Port.n = false;
                        ADH812Port.b(ADH812Port.x);
                    } else if (ADH812Port.o) {
                        ADH812Port.o = false;
                        ADH812Port.a(ADH812Port.f, ADH812Port.y);
                    } else if (ADH812Port.s) {
                        ADH812Port.s = false;
                        ADH812Port.g();
                    } else if (ADH812Port.t) {
                        ADH812Port.t = false;
                        ADH812Port.c(ADH812Port.z);
                    } else if (ADH812Port.r) {
                        SystemClock.sleep(30L);
                        ADH812Port.r = false;
                        ADH812Port.a(ADH812Port.F);
                    } else if (ADH812Port.q) {
                        ADH812Port.q = false;
                        ADH812Port.f();
                    } else {
                        ADH812Port.b();
                    }

                    PortController.f = 0;
                }

                PortController.h++;
                if (ADH815Port.Q) {
                    PortController.i++;
                }

                if (ADH815Port.R) {
                    PortController.j++;
                }

                if (ADH812Port.D) {
                    PortController.k++;
                }

                if (PortController.h >= 20) {
                    PortController.h = 0;
                    if (PortController.b) {
//                        FileLogger.logMessage("815连接中断!");
                        Utils.logD(TAG, "815 연결 중단!");
                    }

                    if (PortController.p != null) {
                        PortController.p.onDisconnected(1);
                    }
                }

                if (PortController.i >= 20) {
                    PortController.i = 0;
                    if (PortController.b) {
//                        FileLogger.logMessage("815_2连接中断!");
                        Utils.logD(TAG, "815_2 연결 중단!");
                    }

                    if (PortController.p != null) {
                        PortController.p.onDisconnected(2);
                    }
                }

                if (PortController.j >= 20) {
                    PortController.j = 0;
                    if (PortController.b) {
//                        FileLogger.logMessage("815_3连接中断!");
                        Utils.logD(TAG, "815_3 연결 중단!");
                    }

                    if (PortController.p != null) {
                        PortController.p.onDisconnected(3);
                    }
                }

                if (PortController.k == 20) {
                    PortController.k = 0;
                    if (PortController.b) {
//                        FileLogger.logMessage("812连接中断!");
                        Utils.logD(TAG, "812 연결 중단!");
                    }

                    if (PortController.q != null) {
                        PortController.q.onADH812Disconnected();
                    }
                }

                if (PortController.s) {
                    SystemClock.sleep(200L);
                    PortController.s = false;
                    if (PortController.l == null || PortController.m == null || PortController.a == null) {
                        if (PortController.n) {
//                            Log.e("PortController", "serialPort或is、os为空,关闭失败");
                            Utils.logD(TAG, "serialPort 또는 is, os가 비어 있습니다. 닫을 수 없습니다.");
                        }

                        return;
                    }

                    try {
                        PortController.o = false;
                        PortController.m.close();
                        PortController.a.close();
                        PortController.l.close();
                    } catch (IOException var2) {
                        var2.printStackTrace();
                    }
                }
            }

        }
    }
}
