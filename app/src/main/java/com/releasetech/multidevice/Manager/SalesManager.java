package com.releasetech.multidevice.Manager;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SalesManager {
    private static final String PREF_NAME = "sales_data";
    private static final String KEY_SALES = "sales_json";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SalesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // 🛒 새 결제 추가 (amount = 결제 금액)
    public void addSale(int amount) {
        try {
            // 기존 데이터 불러오기
            String jsonString = prefs.getString(KEY_SALES, "{}");
            JSONObject salesData = new JSONObject(jsonString);

            // 오늘 날짜 / 주 / 월 가져오기
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String weekOfYear = getWeekOfYear();
            String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());

            // 각 항목 초기화
            JSONObject daily = salesData.optJSONObject("daily");
            if (daily == null) daily = new JSONObject();
            JSONObject weekly = salesData.optJSONObject("weekly");
            if (weekly == null) weekly = new JSONObject();
            JSONObject monthly = salesData.optJSONObject("monthly");
            if (monthly == null) monthly = new JSONObject();

            // 값 업데이트
            daily.put(today, daily.optInt(today, 0) + amount);
            weekly.put(weekOfYear, weekly.optInt(weekOfYear, 0) + amount);
            monthly.put(month, monthly.optInt(month, 0) + amount);

            // JSON 객체 업데이트
            salesData.put("daily", daily);
            salesData.put("weekly", weekly);
            salesData.put("monthly", monthly);

            // SharedPreferences 저장
            editor.putString(KEY_SALES, salesData.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 📅 현재 연도 + 주차 계산 (ex: 2025-W07)
    private String getWeekOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        return year + "-W" + String.format(Locale.getDefault(), "%02d", week);
    }

    public int getTodaySales() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        return getDailySales(today);
    }

    public int getThisWeekSales() {
        String currentWeek = getWeekOfYear();
        return getWeeklySales(currentWeek);
    }

    public int getThisMonthSales() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        return getMonthlySales(currentMonth);
    }

    // 특정 날짜의 매출 조회
    public int getDailySales(String date) {
        try {
            String jsonString = prefs.getString(KEY_SALES, "{}");
            JSONObject salesData = new JSONObject(jsonString);
            JSONObject daily = salesData.optJSONObject("daily");
            return daily != null ? daily.optInt(date, 0) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 특정 주의 매출 조회
    public int getWeeklySales(String week) {
        try {
            String jsonString = prefs.getString(KEY_SALES, "{}");
            JSONObject salesData = new JSONObject(jsonString);
            JSONObject weekly = salesData.optJSONObject("weekly");
            return weekly != null ? weekly.optInt(week, 0) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void resetSalesData() {
        editor.putString(KEY_SALES, "{}");
        editor.apply();
    }

    // 특정 월의 매출 조회
    public int getMonthlySales(String month) {
        try {
            String jsonString = prefs.getString(KEY_SALES, "{}");
            JSONObject salesData = new JSONObject(jsonString);
            JSONObject monthly = salesData.optJSONObject("monthly");
            return monthly != null ? monthly.optInt(month, 0) : 0;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
