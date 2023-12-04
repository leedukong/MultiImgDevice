package com.releasetech.multidevice.Update;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateDesign extends AsyncTask<String, Void, Void>{

    public final static String TAG = "[UPDATE_DESIGN]";
    public final static String SERVER = "server";
    public final static String DEFAULT = "default";
    public static String serverUrl = "";
    public static List<String> failedItems = new ArrayList<>();
    private Context context;
    public UpdateDesign(Context context){
        this.context = context;
    }
    public static void updateDefaultDesign(Context context) {
        Log.d(TAG, "기본 디자인");
        String[] items = {"/팝업_전체취소/배경.png", "/팝업_전체취소/버튼_확인.png", "/팝업_전체취소/버튼_취소.png", "/화면_메뉴/장바구니/버튼_결제.png", "/화면_메뉴/장바구니/버튼_전체취소.png", "/화면_메뉴/장바구니/이미지_합계.png",
                "/팝업_결제/버튼_결제.png", "/팝업_결제/버튼_취소.png", "/팝업_결제/버튼_쿠폰적용.png", "/팝업_결제/배경.png", "/팝업_결제/버튼_수량취소.png", "/팝업_결제/버튼_카드.png", "/팝업_결제/버튼_카드_선택.png", "/팝업_결제/버튼_페이코.png", "/팝업_결제/버튼_페이코_선택.png", "/팝업_결제/버튼_삼성페이.png",
                "/팝업_옵션/버튼_수량증가.png", "/팝업_옵션/버튼_수량감소.png", "/팝업_페이코/배경.png", "/팝업_페이코/배경_키패드.png", "/팝업_페이코/키패드_1.png", "/팝업_페이코/키패드_2.png", "/팝업_페이코/키패드_3.png",
                "/팝업_페이코/키패드_4.png", "/팝업_페이코/키패드_5.png", "/팝업_페이코/키패드_6.png", "/팝업_페이코/키패드_7.png", "/팝업_페이코/키패드_8.png", "/팝업_페이코/키패드_9.png", "/팝업_페이코/키패드_0.png", "/팝업_페이코/키패드_확인.png", "/팝업_페이코/키패드_지움.png"};
        File file = null;
        String FILE_PATH = context.getExternalFilesDir(null).toString();
        for (int i = 0; i < items.length; i++) {
//            2023.10.10 이미 있는 파일은 다운로드 하지 않도록 수정, 모두 확인하면 메뉴 화면 로딩이 너무 오래 걸림.
            File outputFile = new File(FILE_PATH, items[i]);
            if (outputFile.exists()) {
                continue;
            }
            try {
                URL url = new URL(context.getString(R.string.update_design_url) + items[i]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                if (i < 3) {
                    file = new File(FILE_PATH, "팝업_전체취소");
                } else if (i < 6) {
                    file = new File(FILE_PATH, "화면_메뉴/장바구니");
                } else if (i < 16){
                    file = new File(FILE_PATH, "팝업_결제");
                } else if (i < 18){
                    file = new File(FILE_PATH, "팝업_옵션");
                }else{
                    file = new File(FILE_PATH, "팝업_페이코");
                }
                if (!file.exists()) {
                    file.mkdirs();
                }

                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                Utils.logD(TAG, "기본 디자인 항목 :" + items[i] + "가져오기 성공");
            } catch (Exception e) {
                Utils.logD(TAG, "기본 디자인 : " + items[i] + "가져오기 실패");
            }
        }
    }

    public static void updateDesignFromServer(Context context) {
        failedItems.clear();
        Log.d("업데이트 테스트 : ", "서버 디자인 가져오기");
        String[] items = {"/팝업_옵션/버튼_HOT.png", "/팝업_옵션/버튼_HOT_선택.png", "/팝업_옵션/버튼_HOT_비활성화.png", "/팝업_옵션/버튼_ICE.png", "/팝업_옵션/버튼_ICE_선택.png", "/팝업_옵션/버튼_ICE_비활성화.png",
                "/팝업_옵션/버튼_뒤로가기.png", "/팝업_옵션/배경.png", "/팝업_옵션/버튼_담기.png",
                "/팝업_옵션/버튼_블렌드_1.png", "/팝업_옵션/버튼_블렌드_1_선택.png", "/팝업_옵션/버튼_블렌드_1_비활성화.png",
                "/팝업_옵션/버튼_블렌드_2.png", "/팝업_옵션/버튼_블렌드_2_선택.png", "/팝업_옵션/버튼_블렌드_2_비활성화.png",
                "/팝업_옵션/버튼_블렌드_3.png", "/팝업_옵션/버튼_블렌드_3_선택.png", "/팝업_옵션/버튼_블렌드_3_비활성화.png",
                "/팝업_옵션/버튼_블렌드_4.png", "/팝업_옵션/버튼_블렌드_4_선택.png", "/팝업_옵션/버튼_블렌드_4_비활성화.png",
                "/팝업_옵션/버튼_샷추가_0.png", "/팝업_옵션/버튼_샷추가_0_선택.png", "/팝업_옵션/버튼_샷추가_0_비활성화.png",
                "/팝업_옵션/버튼_샷추가_1.png", "/팝업_옵션/버튼_샷추가_1_선택.png", "/팝업_옵션/버튼_샷추가_1_비활성화.png",
                "/팝업_옵션/버튼_샷추가_2.png", "/팝업_옵션/버튼_샷추가_2_선택.png", "/팝업_옵션/버튼_샷추가_2_비활성화.png",
                "/팝업_옵션/버튼_샷추가_3.png", "/팝업_옵션/버튼_샷추가_3_선택.png", "/팝업_옵션/버튼_샷추가_3_비활성화.png"
        };
    }

    public static void updateBackground(Context context) {
        File file = null;
        String FILE_PATH = context.getExternalFilesDir(null).toString();
        File outputFile = new File(FILE_PATH, "/팝업_옵션/배경2.png");
        if (outputFile.exists()) {
            return;
        }
        try {
            URL url = new URL("https://in.sanitystudio.com/EightPresso/UpdateDesign" + "/팝업_옵션/배경2.png");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            file = new File(FILE_PATH, "팝업_옵션");
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();

            Utils.logD(TAG, "기본 디자인 항목 :" + "/팝업_옵션/배경2.png" + "가져오기 성공");
        } catch (Exception e) {
            Utils.logD(TAG, "기본 디자인 : " + "/팝업_옵션/배경2.png" + "가져오기 실패");
        }
    }


    @Override
    protected Void doInBackground(String... args) {
        String arg = args[0];
        if (arg.equals(SERVER)) updateDesignFromServer(context);
        else if (arg.equals(DEFAULT)) {
            updateBackground(context);
            updateDefaultDesign(context);
        }
        return null;
    }
}
