package com.releasetech.multidevice.Client;

import static com.google.firebase.crashlytics.internal.common.SessionReportingCoordinator.convertInputStreamToString;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Tool.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Payco extends AsyncTask<String , Void, Void> {


    public final static String TAG = "[PAYCO]";
    public final static String REGISTER = "register";
    public final static String APPROVAL = "approval";
    public final static String CANCEL = "cancel";
    

    private OnRegisterCallback onRegisterCallback;
    private OnApprovalCallback onApprovalCallback;
    private OnCancelCallback onCancelCallback;

    // 여기 static으로 필요한애들
    private String signature;
    private String returnMessage = "";
    private String resultCode = "";
    private String cancelResultCode ="";

    private JSONArray productInfoList;
    private JSONArray paycoApprovalInfo;
    private double paycoTotalAmount;

    //고정값
    private String posType = "KIOSK";
    private String deviceAuthType = "BAR";
    private String serviceType = "PAYCO";
    private String nocvmYN = "Y";

    //취소시
    private String tradeRequestNo;
    private String tradeNo;
    private String tradeDateTime;
    private ArrayList<String> approvalNo;
    private ArrayList<String> approvalDateTime;
    private ArrayList<String> approvalAmount;
    private ArrayList<String> approvalCardNo;
    private ArrayList<String> approvalCompanyName;

    //주문
    private CartManager cartManager;
    private byte[] hashedData;
    //pinCode+posTid
    private String data = "";
    //바코드
    private String pinCode = "";
    private String productNames = "";
    private Context context;
    public Payco(Context context){
        this.context = context;
    }

    public void setOnRegisterCallback(OnRegisterCallback onRegisterCallback){
        this.onRegisterCallback = onRegisterCallback;
    }

    public void setOnApprovalCallback(OnApprovalCallback onApprovalCallback){
        this.onApprovalCallback = onApprovalCallback;
    }

    public void setOnCancelCallback(OnCancelCallback onCancelCallback){
        this.onCancelCallback = onCancelCallback;
    }

    private void register(){
        //프리퍼런스 값
        String registrationNumber = PreferenceManager.getString(context, "registration_number");
        String vanCorpCode = PreferenceManager.getString(context, "van_corp_code");
        String vanPosTid = PreferenceManager.getString(context, "van_pos_tid");
        String posDevCorpName = PreferenceManager.getString(context, "pos_dev_corp_name");
        String posSolutionName = PreferenceManager.getString(context, "pos_solution_name");
        String posSolutionVersion = PreferenceManager.getString(context, "pos_solution_version");

        //request 파라미터
        String[] params = {"posType", "registrationNumber", "vanCorpCode", "vanPosTid", "posDevCorpName", "posSolutionName", "totalTaxableAmt"};
        String[] values = {posType, registrationNumber, vanCorpCode, vanPosTid, posDevCorpName, posSolutionName, posSolutionVersion};

        try {
            JSONObject jsonObject = new JSONObject();
            for(int i=0; i< params.length; i++){
                jsonObject.accumulate(params[i], values[i]);
            }

            InputStream is = post("https://dongle.payco.com/pos/v1/registration", jsonObject.toString());
            if(is != null){
                JSONObject inputJsonObject = new JSONObject(convertInputStreamToString(is));
                returnMessage = inputJsonObject.getString("message");
                resultCode = inputJsonObject.getString("resultCode");
                JSONObject inputResultJsonObject = new JSONObject(inputJsonObject.getString("result"));

                String posTid = inputResultJsonObject.getString("posTid");
                PreferenceManager.setString(context, "pos_tid", posTid);
                String apiKey = inputResultJsonObject.getString("apiKey");
                PreferenceManager.setString(context, "api_key", apiKey);
                onRegisterCallback.onRegisterResult(returnMessage);

            }else{
                onRegisterCallback.onRegisterResult("서버 응답 없음");
            }
        }catch (Exception e) {
            onRegisterCallback.onRegisterException(e);
        }
    }

    private void approval(){
        PreferenceManager.setString(context, "payco_return_message", "");

        //프리퍼런스 값
        String posTid = PreferenceManager.getString(context, "pos_tid");
        String apiKey = PreferenceManager.getString(context, "api_key");
        String registrationNumber = PreferenceManager.getString(context, "registration_number");
        String vanCorpCode = PreferenceManager.getString(context, "van_corp_code");
        String vanPosTid = PreferenceManager.getString(context, "van_pos_tid");
        //request 파라미터
        String[] params = {"nocvmYN", "deviceType", "deviceAuthType", "serviceType", "registrationNumber", "posTid", "vanCorpCode", "vanPosTid", "pinCode", "currency", "productName"};
        String[] values = {nocvmYN, posType, deviceAuthType, serviceType, registrationNumber, posTid, vanCorpCode, vanPosTid, pinCode, "KRW", productNames};

        try {
            String data = pinCode+posTid;
            hashedData = Utils.getHmacSHA256(apiKey, data);
            //공백이 들어와서 지워버림
            signature = Base64.encodeToString(hashedData,Base64.DEFAULT).replaceAll("\n","").replaceAll("\\\\", "").trim();
            PreferenceManager.setString(context, "payco_prev_signature", signature);

            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("signature",signature);
            for(int i=0; i<params.length; i++) {
                jsonObject.accumulate(params[i], values[i]);
            }
            //변수 타입이 String이 아니라 따로 accumulate
            jsonObject.accumulate("totalAmount", paycoTotalAmount);
            JSONObject extraObject = new JSONObject();
            extraObject.accumulate("posDevCorpName", PreferenceManager.getString(context, "pos_dev_corp_name"));
            extraObject.accumulate("posSolutionName", PreferenceManager.getString(context, "pos_solution_name"));
            extraObject.accumulate("posSolutionVersion", PreferenceManager.getString(context, "pos_solution_version"));
            jsonObject.accumulate("extras", extraObject);
            jsonObject.accumulate("productInfoList", productInfoList);

            Log.i("payco approval out", jsonObject.toString());
            InputStream is = post("https://dongle.payco.com/payment/v1/approval/simple", jsonObject.toString());


            if(is != null){
                JSONObject inputJsonObject = new JSONObject(convertInputStreamToString(is));
                JSONObject resultObject = new JSONObject(inputJsonObject.getString("result"));
                JSONObject approvalObject = new JSONObject();
                JSONArray tempArray = resultObject.getJSONArray("approvalResultList");
                returnMessage = inputJsonObject.getString("message");
                resultCode = inputJsonObject.getString("resultCode");
                PreferenceManager.setString(context, "payco_return_message", returnMessage);
                PreferenceManager.setString(context, "payco_result_code", resultCode);

                approvalNo = new ArrayList<>();
                approvalAmount = new ArrayList<>();
                approvalDateTime = new ArrayList<>();
                approvalCardNo = new ArrayList<>();
                approvalCompanyName = new ArrayList<>();

                for(int i=0; i<tempArray.length(); i++){
                    approvalObject = tempArray.getJSONObject(i);
                    approvalNo.add(approvalObject.getString("approvalNo"));
                    PreferenceManager.setString(context, "payco_prev_approval_no", approvalObject.getString("approvalNo"));
                    approvalAmount.add(approvalObject.getString("approvalAmount"));
                    PreferenceManager.setString(context, "payco_prev_approval_amount", approvalObject.getString("approvalAmount"));
                    approvalDateTime.add(approvalObject.getString("approvalDatetime"));
                    PreferenceManager.setString(context, "payco_prev_approval_datetime", approvalObject.getString("approvalDatetime"));
                    approvalCardNo.add(approvalObject.getString("approvalCardNo"));
                    approvalCompanyName.add(approvalObject.getString("approvalCompanyName"));
                }

                tradeRequestNo = resultObject.getString("tradeRequestNo");
                PreferenceManager.setString(context, "payco_prev_trade_request_no", tradeRequestNo);
                tradeNo = resultObject.getString("tradeNo");
                PreferenceManager.setString(context, "payco_prev_trade_no", tradeNo);

                onApprovalCallback.onApprovalResult(this, returnMessage, approvalObject.getString("approvalNo"));
                Log.i("payco approval in", inputJsonObject + "");
            }else{
                onApprovalCallback.onApprovalResult(this, "서버 응답 없음", "");
            }
        }catch (Exception e) {
            e.printStackTrace();
            onApprovalCallback.onApprovalException(e);
        }
    }

    private void cancel(){

        //프리퍼런스 값
        String posTid = PreferenceManager.getString(context, "pos_tid");
        String registrationNumber = PreferenceManager.getString(context, "registration_number");
        String vanCorpCode = PreferenceManager.getString(context, "van_corp_code");
        String vanPosTid = PreferenceManager.getString(context, "van_pos_tid");

        int card = approvalNo.size() -1;
        //request 파라미터
        String[] params = {"nocvmYN", "deviceType","signature", "tradeRequestNo", "deviceAuthType", "posTid", "tradeNo", "serviceType", "registrationNumber", "vanCorpCode", "vanPosTid", "approvalNo", "approvalDatetime", "pinCode"};
        String[] values = {nocvmYN, posType, signature, tradeRequestNo, deviceAuthType, posTid, tradeNo, serviceType, registrationNumber, vanCorpCode, vanPosTid, approvalNo.get(card), approvalDateTime.get(card), pinCode};

        try {
            JSONObject jsonObject = new JSONObject();
            for(int i=0; i< params.length; i++){
                jsonObject.accumulate(params[i], values[i]);
            }
            JSONObject extraObject = new JSONObject();
            extraObject.accumulate("posDevCorpName", PreferenceManager.getString(context, "pos_dev_corp_name"));
            extraObject.accumulate("posSolutionName", PreferenceManager.getString(context, "pos_solution_name"));
            extraObject.accumulate("posSolutionVersion", PreferenceManager.getString(context, "pos_solution_version"));
            jsonObject.accumulate("totalAmount", paycoTotalAmount); //double 형임
            double temp =0;
            for(int i=0; i<approvalAmount.size(); i++){
                temp += Double.parseDouble(approvalAmount.get(i));
            }
            jsonObject.accumulate("approvalAmount", temp); //double 형임
            jsonObject.accumulate("extras", extraObject);

            InputStream is = post("https://dongle.payco.com/payment/v1/cancel/simple", jsonObject.toString());
            Log.i("payco cancel out", jsonObject.toString());
            if(is != null){
                JSONObject inputJsonObject = new JSONObject(convertInputStreamToString(is));
                Log.i("payco cancel in", inputJsonObject.toString());
                cancelResultCode = inputJsonObject.getString("sourceResultCode");
                onCancelCallback.onCancelResult(cancelResultCode);
            }else{
                onCancelCallback.onCancelResult("서버 응답 없음");
            }
        }catch (Exception e) {
            onCancelCallback.onCancelException(e);
        }
    }

    private InputStream post(String strUrl, String json) throws IOException {
        //초기값 지정
        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        try {
            OutputStream os = connection.getOutputStream();
            if (os != null) {
                os.write(json.getBytes("UTF-8"));
                os.flush();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return connection.getInputStream();
    }

    @Override
    protected Void doInBackground(String... args) {
        String arg = args[0];
        if (arg.equals(REGISTER)) {
            register();
        }else if (arg.equals(APPROVAL)){
            approval();
        }else if (arg.equals(CANCEL)){
            cancel();
        }
        else{
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
    }


    public void setProductInfoList(JSONArray productInfoList) {
        this.productInfoList = productInfoList;
    }
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
    public String getPinCode() {
        this.pinCode = pinCode;
        return pinCode;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public void setPaycoTotalAmount(double paycoTotalAmount) {
        this.paycoTotalAmount = paycoTotalAmount;
    }

    public double getPaycoTotalAmount(){
        return paycoTotalAmount;
    }

    public void setPaycoApprovalInfo(String signature, String tradeRequestNo, String tradeNo, String approvalNo, String approvalAmount, String approvalDateTime){
        this.approvalNo = new ArrayList<>();
        this.approvalAmount = new ArrayList<>();
        this.approvalDateTime = new ArrayList<>();
        this.signature = signature;
        this.tradeRequestNo = tradeRequestNo;
        this.tradeNo = tradeNo;
        this.approvalNo.add(approvalNo);
        this.approvalAmount.add(approvalAmount);
        this.approvalDateTime.add(approvalDateTime);
    }

    public String getPaycoApprovalInfoString(String info){
        if(info.equals("resultCode")){
            this.resultCode = resultCode;
            return resultCode;
        }
        else if(info.equals("signature")){
            this.signature = signature;
            return signature;
        }else if(info.equals("tradeRequestNo")){
            this.tradeRequestNo = tradeRequestNo;
            return tradeRequestNo;
        }
        else if(info.equals("tradeNo")){
            this.tradeNo = tradeNo;
            return tradeNo;
        }else{
            return null;
        }
    }

    public ArrayList<String> getPaycoApprovalInfoArray(String info) {
        if(info.equals("approvalNo")){
            this.approvalNo = approvalNo;
            return approvalNo;
        }
        else if(info.equals("approvalAmount")){
            this.approvalAmount = approvalAmount;
            return approvalAmount;
        }
        else if(info.equals("approvalDateTime")){
            this.approvalDateTime = approvalDateTime;
            return approvalDateTime;
        }else if(info.equals("approvalCardNo")){
            this.approvalCardNo = approvalCardNo;
            return approvalCardNo;
        }else if(info.equals("approvalCompanyName")) {
            this.approvalCompanyName = approvalCompanyName;
            return approvalCompanyName;
        } else{
            return null;
        }
    }


    public interface OnRegisterCallback {
        void onRegisterResult(String result);
        void onRegisterException(Exception e);
    }

    public interface OnApprovalCallback {
        void onApprovalResult(Payco payco, String result, String approvalNo);

        void onApprovalException(Exception e);
    }

    public interface OnCancelCallback {
        void onCancelResult(String result);
        void onCancelException(Exception e);
    }
}