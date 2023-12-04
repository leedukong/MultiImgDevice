//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.a;

import android.util.Log;

import com.epton.sdk.SerialPortFinder;

import java.io.File;

import android_serialport_api.SerialPort;

//d
public class SerialPortManager {
    public SerialPortFinder a = new SerialPortFinder();
    private SerialPort serialPort = null;
    private String TAG = "SerialSdk";

    public SerialPortManager() {
    }

    //a
    public SerialPort getSerialPortByNumber(int portNumber, int baudRate) {
        if (this.serialPort == null) {
            try {
                String portPath = "/dev/ttyO" + portNumber;
                this.serialPort = new SerialPort(new File(portPath), baudRate, 0);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return this.serialPort;
    }

    //a
    public SerialPort getSerialPortByName(String portName, int suffix, int baudRate) {
        if (this.serialPort == null) {
            try {
                String portPath = portName + suffix;
                Log.e(TAG, portPath + "," + baudRate);
                this.serialPort = new SerialPort(new File(portPath), baudRate, 0);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        return this.serialPort;
    }

    public void closeSerialPort() {
        if (this.serialPort != null) {
            this.serialPort.close();
            this.serialPort = null;
        }

    }
}
