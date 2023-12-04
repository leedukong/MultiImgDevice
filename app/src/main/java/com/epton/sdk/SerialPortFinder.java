//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class SerialPortFinder {
    private static final String TAG = "SerialPort";
    private Vector<Driver> availableDrivers = null;

    public SerialPortFinder() {
    }

    //a
    Vector<Driver> findAvailableDrivers() throws IOException {
        if (this.availableDrivers == null) {
            this.availableDrivers = new Vector();
            LineNumberReader reader = new LineNumberReader(new FileReader("/proc/tty/drivers"));

            String line;
            while((line = reader.readLine()) != null) {
                String driverName = line.substring(0, 21).trim();
                String[] splitLine = line.split(" +");
                if (splitLine.length >= 5 && splitLine[splitLine.length - 1].equals("serial")) {
                    Log.d(TAG, "Found new driver " + driverName + " on " + splitLine[splitLine.length - 4]);
                    this.availableDrivers.add(new Driver(driverName, splitLine[splitLine.length - 4]));
                }
            }

            reader.close();
        }

        return this.availableDrivers;
    }

    //b
    public String[] getDeviceNames() throws IOException {
        Vector<String> names = new Vector<>();

        Iterator iterator = this.findAvailableDrivers().iterator();

        while(iterator.hasNext()) {
            Driver var3 = (Driver)iterator.next();
            Iterator var4 = var3.getDevices().iterator();

            while(var4.hasNext()) {
                String var5 = ((File)var4.next()).getName();
                String var6 = String.format("%s (%s)", var5, var3.getDriverName());
                names.add(var6);
            }
        }

        return (String[])names.toArray(new String[names.size()]);
    }

    //c
    public String[] getDevicePaths() throws IOException {
        Vector<String> paths = new Vector<>();

        Iterator<Driver> iterator = this.findAvailableDrivers().iterator();

        while(iterator.hasNext()) {
            Driver driver = iterator.next();
            Iterator<File> fileIterator = driver.getDevices().iterator();

            while(fileIterator.hasNext()) {
                String filePath = fileIterator.next().getAbsolutePath();
                paths.add(filePath);
            }
        }

        return paths.toArray(new String[paths.size()]);
    }

    public class Driver {
        private String driverName;
        private String directory;
        Vector<File> devices = null;

        public Driver(String driverName, String directory) {
            this.driverName = driverName;
            this.directory = directory;
        }

        public Vector<File> getDevices() {
            if (this.devices == null) {
                this.devices = new Vector<>();
                File dir = new File("/dev");
                File[] files = dir.listFiles();

                for(int i = 0; i < files.length; ++i) {
                    if (files[i].getAbsolutePath().startsWith(this.directory)) {
                        Log.d("SerialPort", "Found new device: " + files[i]);
                        this.devices.add(files[i]);
                    }
                }
            }

            return this.devices;
        }

        public String getDriverName() {
            return this.driverName;
        }
    }
}
