//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.epton.sdk.a;


//b
public class HexByteConverter {
    public HexByteConverter() {
    }

    //a
    public static String ensureProperLength(String hexString) {
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        if (hexString.length() < 4) {
            hexString = "00" + hexString;
        }

        return hexString;
    }

    //b
    public static byte[] hexToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];

        for(int index = 0; index < length; index += 2) {
            byteArray[index / 2] = (byte)((Character.digit(hexString.charAt(index), 16) << 4) + Character.digit(hexString.charAt(index + 1), 16));
        }

        return byteArray;
    }

    //a
    public static String byteArrayToHex(byte[] bytes) {
        String hexResult = "";

        for(int i = 0; i < bytes.length; ++i) {
            String hexValue = Integer.toHexString(bytes[i] & 255);
            if (hexValue.length() == 1) {
                hexValue = '0' + hexValue;
            }

            hexResult = hexResult + hexValue;
        }

        return hexResult;
    }

    //a
    public static byte[] mergeByteArrays(byte[] array1, byte[] array2, int length) {
        if (array1 == null && array2 == null) {
            return null;
        } else {
            byte[] mergedArray;
            if (array1 == null) {
                mergedArray = new byte[length];
                System.arraycopy(array2, 0, mergedArray, 0, length);
                return mergedArray;
            } else if (array2 == null) {
                mergedArray = new byte[array1.length];
                System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                return mergedArray;
            } else {
                mergedArray = new byte[length + array1.length];
                System.arraycopy(array1, 0, mergedArray, 0, array1.length);
                System.arraycopy(array2, 0, mergedArray, array1.length, length);
                return mergedArray;
            }
        }
    }

    //c
    public static String hexToCharString(String hexString) {
        StringBuffer charString = new StringBuffer();
        byte[] byteArray = hexToByteArray(hexString);

        for(int i = 0; i < byteArray.length; ++i) {
            char ch = (char)byteArray[i];
            charString.append(ch);
        }

        return charString.toString();
    }


    //b
    public static int byteArrayToInt(byte[] bytes) {
        return bytes[3] & 255 | (bytes[2] & 255) << 8 | (bytes[1] & 255) << 16 | (bytes[0] & 255) << 24;
    }


    //a
    public static byte[] intToByteArray(int value) {
        return new byte[]{(byte)(value >> 8 & 255), (byte)(value & 255)};
    }
}
