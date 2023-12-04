package com.releasetech.multidevice.Tool;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MediaReplacer {
    private static final String TAG = "[MEDIA REPLACER]";

    private static final int DIRECTORY = 0;
    private static final int IMAGE = 1;
    private static final int IMAGE_PNG = 2;
    private static final int VIDEO = 3;
    private static final int MEDIA = 4;

    private static final ArrayList<String> DESIGN_DIRECTORY_NAMES = new ArrayList<>();
    private static final String[] FILE_EXCLUDES = new String[]{"버튼_원두"};
    private static final String[] FILE_EXTRAS = new String[]{
            "팝업_옵션/오버레이_핫아이스.png",
            "팝업_옵션/오버레이_사이즈.png",
            "팝업_옵션/오버레이_샷추가.png",
            "팝업_옵션/오버레이_블렌드.png"
    };
    public static String replaceDesign(Context context) {
        String removableStoragePath = Utils.getRemovableStorage();
        if (removableStoragePath == null) return "USB 또는 마이크로 SD 카드가 없습니다.";

        ArrayList<String> directoryList = new ArrayList<>();
        ArrayList<String> fileList = new ArrayList<>();
        String dataPath = context.getExternalFilesDir(null).toString();
        for (String directoryName : DESIGN_DIRECTORY_NAMES) {
            File f = new File(dataPath, directoryName);
            extractFilesDirectories(directoryList, fileList, f, dataPath);
        }
        ArrayList<File> newDirectories = new ArrayList<>();
        ArrayList<File> newFiles = new ArrayList<>();
        directoryList.forEach(s -> newDirectories.add(new File(removableStoragePath, s)));
        fileList.forEach(s -> newFiles.add(new File(removableStoragePath, s)));

        if (newDirectories.stream().allMatch(File::exists) && newFiles.stream().allMatch(File::exists)) {
            directoryList.forEach(s -> clearDirectory(dataPath + "/" + s));
            fileList.forEach(s ->
                    copy(
                            new File(removableStoragePath, s),
                            new File(dataPath, s)
                    )
            );
            Arrays.stream(FILE_EXTRAS).forEach(s ->
                    {
                        if (new File(removableStoragePath, s).exists()) {
                            copy(
                                    new File(removableStoragePath, s),
                                    new File(dataPath, s)
                            );
                        }
                    }
            );

            return "교체 완료";
        }

        StringBuilder tempStr = new StringBuilder();
        newDirectories.stream().filter(file -> !file.exists())
                .forEach(file ->
                        tempStr.append(getRelativePath(removableStoragePath, file))
                                .append(" 폴더가 없습니다.\n")
                );
        newFiles.stream().filter(file -> !file.exists())
                .forEach(file ->
                        tempStr.append(getRelativePath(removableStoragePath, file))
                                .append(" 파일이 없습니다.\n")
                );

        return tempStr.toString();
    }

    private static String getRelativePath(String base, File path) {
        return new File(base).toURI().relativize(path.toURI()).getPath();
    }

    private static void extractFilesDirectories
            (ArrayList<String> directoryList, ArrayList<String> fileList, File file, String base) {
        if (file.isDirectory()) {
            directoryList.add(getRelativePath(base, file));
            File[] tfs = file.listFiles();
            if (tfs == null || tfs.length == 0) return;
            for (File f : tfs) {
                extractFilesDirectories(directoryList, fileList, f, base);
            }
        } else {
            if (!Utils.startsWithAny(file.getName(), FILE_EXCLUDES)) {   //Deprecated된 파일 포함된 채로 출고됨
                fileList.add(getRelativePath(base, file));
            }
        }
    }

    public static String replaceIdleAd(Context context) {
        String removableStoragePath = Utils.getRemovableStorage();
        if (removableStoragePath == null) return "USB 또는 마이크로 SD 카드가 없습니다.";

        File directory = new File(removableStoragePath, "광고_대기");
        if (!directory.exists()) return "\"광고_대기\" 폴더가 없습니다.";
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return "파일이 없습니다.";
        if (Arrays.stream(files).allMatch(file -> checkType(file, MEDIA))) {
            String dataPath = context.getExternalFilesDir(null) + "/광고_대기";
            clearDirectory(dataPath);
            for (File file : files)
                copy(file, new File(dataPath, file.getName()));
            return "교체 완료";
        } else {
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("이미지 또는 동영상으로 불러올 수 없는 파일입니다.\n\n");
            Arrays.stream(files).filter(file -> !checkType(file, MEDIA))
                    .forEach(file -> tempStr.append(file.getName()).append("\n"));
            return tempStr.toString();
        }
    }

    public static String replaceMenuAd(Context context) {
        String removableStoragePath = Utils.getRemovableStorage();
        if (removableStoragePath == null) return "USB 또는 마이크로 SD 카드가 없습니다.";

        File directory = new File(removableStoragePath, "광고_메뉴");
        if (!directory.exists()) return "\"광고_메뉴\" 폴더가 없습니다.";
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return "파일이 없습니다.";
        if (Arrays.stream(files).allMatch(file -> checkType(file, IMAGE))) {
            String dataPath = context.getExternalFilesDir(null) + "/광고_메뉴";
            clearDirectory(dataPath);
            for (File file : files)
                copy(file, new File(dataPath, file.getName()));
            return "교체 완료";
        } else {
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("이미지로 불러올 수 없는 파일입니다.\n\n");
            Arrays.stream(files).filter(file -> !checkType(file, IMAGE))
                    .forEach(file -> tempStr.append(file.getName()).append("\n"));
            return tempStr.toString();
        }
    }

    public static String replaceMakingAd(Context context) {
        String removableStoragePath = Utils.getRemovableStorage();
        if (removableStoragePath == null) return "USB 또는 마이크로 SD 카드가 없습니다.";

        File directory = new File(removableStoragePath, "광고_제조");
        if (!directory.exists()) return "\"광고_제조\" 폴더가 없습니다.";
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return "파일이 없습니다.";

        List<File> fileList = Arrays.asList(files);

        if (!fileList.stream().allMatch(MediaReplacer::matchMakingAdNameRules)) {
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("이름 규칙이 잘못되었습니다.\n" +
                    "이름엔 숫자와 대시(-)만 사용할 수 있으며\n" +
                    "\"1-1\", \"1-2\", \"1-3\" 형태로 지어야합니다.\n\n");
            fileList.stream().filter(file -> !matchMakingAdNameRules(file))
                    .forEach(file -> tempStr.append(file.getName()).append("\n"));
            return tempStr.toString();
        }

        HashMap<String, File[]> adMap = new HashMap<>();
        fileList.forEach(f -> {
            String[] tempNameSplit = f.getName().split("-");
            String prefix = tempNameSplit[0];
            String suffix = tempNameSplit[1].split("\\.")[0];
            if (!adMap.containsKey(prefix)) {
                adMap.put(prefix, new File[3]);
            }
            int index = Integer.parseInt(suffix) - 1;
            adMap.get(prefix)[index] = f;
        });

        if (adMap.values().stream().anyMatch(v -> Arrays.asList(v).contains(null))) {
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("파일이 누락되었습니다.\n\n");
            adMap.forEach((key, value) -> {
                if (Arrays.asList(value).contains(null)) {
                    for (int i = 0; i < 3; i++) {
                        if (value[i] == null)
                            tempStr.append(key).append("-").append(i).append("\n");
                    }
                }
            });
            return tempStr.toString();
        }

        if (!adMap.values().stream().allMatch(v -> checkType(v[0], VIDEO) && checkType(v[1], MEDIA) && checkType(v[2], MEDIA))) {
            StringBuilder tempStr = new StringBuilder();
            tempStr.append("이미지 또는 동영상으로 불러올 수 없는 파일입니다.\n\n");
            adMap.values().stream().forEachOrdered(fs -> {
                if (!checkType(fs[0], VIDEO)) tempStr.append(fs[0].getName()).append("\n");
                if (!checkType(fs[1], MEDIA)) tempStr.append(fs[1].getName()).append("\n");
                if (!checkType(fs[2], MEDIA)) tempStr.append(fs[2].getName()).append("\n");
            });
            return tempStr.toString();
        }

        ArrayList<File[]> adPacks = (ArrayList<File[]>) adMap.values().stream().collect(Collectors.toList());

        String dataPath = context.getExternalFilesDir(null) + "/광고_제조";
        clearDirectory(dataPath);
        adPacks.forEach(pack -> {
            for (File file : pack)
                copy(file, new File(dataPath, file.getName()));
        });

        return "교체 완료";
    }

    private static boolean matchMakingAdNameRules(File f) {
        try {
            String[] tempNameSplit = f.getName().split("-");
            String prefix = tempNameSplit[0];
            String suffix = tempNameSplit[1].split("\\.")[0];

            return TextUtils.isDigitsOnly(prefix) && (suffix.equals("1") || suffix.equals("2") || suffix.equals("3"));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean checkType(File file, int type) {
        switch (type) {
            case DIRECTORY:
                return file.isDirectory();
            case IMAGE:
                return Utils.isImage(file);
            case IMAGE_PNG:
                return checkType(file, IMAGE) && file.getName().toLowerCase().endsWith("png");
            case VIDEO:
                return Utils.isVideo(file);
            case MEDIA:
                return checkType(file, IMAGE) || checkType(file, VIDEO);
            default:
                return false;
        }
    }

    private static void clearDirectory(String path) {
        Utils.logD(TAG, path);
        File[] oldFiles = new File(path).listFiles();
        if (oldFiles != null) {
            for (File file : oldFiles) {
                if (file.exists()) {
                    if (!file.isDirectory()) file.delete();
                    file.delete();
                }
            }
        }
    }

    private static void copy(File src, File dst) {
        InputStream in;
        OutputStream out;
        String parent = dst.getParent();
        //if parent directory doesn't exist, create it
        if (!new File(parent).exists()) {
            new File(parent).mkdirs();
        }
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
