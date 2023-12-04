package com.releasetech.multidevice.ManagerSettings.Fragment;

import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.APPROVAL_DATE;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.APPROVAL_NUM;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.CARD_NAME;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.CARD_NUM;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.RESULT_CODE;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.TOTAL_AMOUNT;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutDB.TRANS_TYPE;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.APPROVAL_AMOUNT;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.APPROVAL_CARD_NO;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.APPROVAL_COMPANY_NAME;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.APPROVAL_DATE_TIME;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.APPROVAL_NO;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.PIN_CODE;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.SIGNATURE;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.TRADE_NO;
import static com.releasetech.multidevice.Database.DataBase.CreateCheckoutPaycoDB.TRADE_REQUEST_NO;
import static com.releasetech.multidevice.Manager.CheckoutManager.CHECKOUT_APPROVE;
import static com.releasetech.multidevice.Manager.CheckoutManager.CHECKOUT_REFUND;
import static com.releasetech.multidevice.Manager.CheckoutManager.CHECKOUT_ROLLBACK;
import static com.releasetech.multidevice.Manager.CheckoutManager.cancelPayment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.Client.Payco;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.Order;
import com.releasetech.multidevice.Database.DessertDataLoader;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.DatePickerFragment;
import com.releasetech.multidevice.Tool.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    private static final String TAG = "[HISTORY PAGE]";
    static int lineColorCount = 0;
    static String prevApprovalId = "";
    ArrayList<Order> orders = new ArrayList<>();
    int[] lineColors = {0xfffbfbfb, 0xffdbdbdb};
    private HistoryAdapter historyAdapter = null;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.logD(TAG, "화면 표시됨");

        historyAdapter = new HistoryAdapter(orders);
        RecyclerView historyListRecyclerView = view.findViewById(R.id.history_list);
        historyListRecyclerView.setAdapter(historyAdapter);
        historyListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true));
        RadioButton todayButton = view.findViewById(R.id.history_today);
        RadioButton weekButton = view.findViewById(R.id.history_week);
        RadioButton monthButton = view.findViewById(R.id.history_month);
        RadioButton customDateButton = view.findViewById(R.id.history_custom);
        todayButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), true, true));
                dbManager.close();
                historyAdapter.notifyDataSetChanged();
                historyListRecyclerView.scrollToPosition(orders.size() - 1);
            }
        });
        weekButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime past = now.minusDays(7);
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, past.getYear(), past.getMonthValue(), past.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), true, true));
                dbManager.close();
                historyAdapter.notifyDataSetChanged();
                historyListRecyclerView.scrollToPosition(orders.size() - 1);
            }
        });
        monthButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime past = now.minusDays(30);
                orders.clear();
                orders.addAll(DessertDataLoader.loadOrders(dbManager, past.getYear(), past.getMonthValue(), past.getDayOfMonth(), now.getYear(), now.getMonthValue(), now.getDayOfMonth(), true, true));
                dbManager.close();
                historyAdapter.notifyDataSetChanged();
                historyListRecyclerView.scrollToPosition(orders.size() - 1);
            }
        });
        customDateButton.setOnCheckedChangeListener((compoundButton, b) -> {
            view.findViewById(R.id.history_start_date).setEnabled(b);
            view.findViewById(R.id.history_end_date).setEnabled(b);
            view.findViewById(R.id.history_custom_apply).setEnabled(b);
        });

        EditText startDateEditText = view.findViewById(R.id.history_start_date);
        EditText endDateEditText = view.findViewById(R.id.history_end_date);
        LocalDateTime tempNow = LocalDateTime.now();
        startDateEditText.setText(tempNow.getYear() + "-" + tempNow.getMonthValue() + "-" + tempNow.getDayOfMonth());
        endDateEditText.setText(tempNow.getYear() + "-" + tempNow.getMonthValue() + "-" + tempNow.getDayOfMonth());
        startDateEditText.setOnClickListener(view1 -> {
            String[] startSplit = startDateEditText.getText().toString().split("-");
            int startYear = Integer.parseInt(startSplit[0]);
            int startMonth = Integer.parseInt(startSplit[1]);
            int startDay = Integer.parseInt(startSplit[2]);
            DatePickerFragment datePickerFragment = new DatePickerFragment(startYear, startMonth, startDay);
            datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            datePickerFragment.setOnDateSetListener((datePicker, year, month, day) -> {
                String[] endSplit = endDateEditText.getText().toString().split("-");
                int endYear = Integer.parseInt(endSplit[0]);
                int endMonth = Integer.parseInt(endSplit[1]);
                int endDay = Integer.parseInt(endSplit[2]);
                if ((endYear < year) || ((endYear == year) && endMonth < month) || ((endYear == year) && (endMonth == month) && endDay < day)) {
                    Utils.timedAlert(getContext(), "시작일이 종료일보다\n클 수 없습니다.", 2);
                } else {
                    startDateEditText.setText(year + "-" + month + "-" + day);
                }
            });
        });
        endDateEditText.setOnClickListener(view1 -> {
            String[] endSplit = endDateEditText.getText().toString().split("-");
            int endYear = Integer.parseInt(endSplit[0]);
            int endMonth = Integer.parseInt(endSplit[1]);
            int endDay = Integer.parseInt(endSplit[2]);
            DatePickerFragment datePickerFragment = new DatePickerFragment(endYear, endMonth, endDay);
            datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            datePickerFragment.setOnDateSetListener((datePicker, year, month, day) -> {
                String[] startSplit = startDateEditText.getText().toString().split("-");
                int startYear = Integer.parseInt(startSplit[0]);
                int startMonth = Integer.parseInt(startSplit[1]);
                int startDay = Integer.parseInt(startSplit[2]);
                if ((year < startYear) || ((year == startYear) && month < startMonth) || ((year == startYear) && (month == startMonth) && day < startDay)) {
                    Utils.timedAlert(getContext(), "종료일이 시작일보다\n작을 수 없습니다.", 2);
                } else {
                    endDateEditText.setText(year + "-" + month + "-" + day);
                }
            });
        });

        Button applyCustomDateButton = view.findViewById(R.id.history_custom_apply);
        applyCustomDateButton.setOnClickListener(view1 -> {
            DBManager dbManager = new DBManager(getContext());
            dbManager.open();
            dbManager.create();
            String[] startSplit = startDateEditText.getText().toString().split("-");
            int startYear = Integer.parseInt(startSplit[0]);
            int startMonth = Integer.parseInt(startSplit[1]);
            int startDay = Integer.parseInt(startSplit[2]);
            String[] endSplit = endDateEditText.getText().toString().split("-");
            int endYear = Integer.parseInt(endSplit[0]);
            int endMonth = Integer.parseInt(endSplit[1]);
            int endDay = Integer.parseInt(endSplit[2]);
            Utils.logD(TAG, startYear + "/" + startMonth + "/" + startDay);
            Utils.logD(TAG, endYear + "/" + endMonth + "/" + endDay);
            orders.clear();
            orders.addAll(DessertDataLoader.loadOrders(dbManager, startYear, startMonth, startDay, endYear, endMonth, endDay, true, true));
            dbManager.close();
            historyAdapter.notifyDataSetChanged();
            historyListRecyclerView.scrollToPosition(orders.size() - 1);
        });

        todayButton.setChecked(true);


    }

    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private final ArrayList<Order> mList;

        public HistoryAdapter(ArrayList<Order> mList) {
            this.mList = mList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.history_item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            Order order = mList.get(position);
            if (!order.approvalId.equals(prevApprovalId)) {
                lineColorCount++;
                prevApprovalId = order.approvalId;
            }
            holder.layout.setBackgroundColor(lineColors[lineColorCount % lineColors.length]);
            holder.datetime.setText(order.year + "년 " + order.month + "월 " + order.day + "일 " + order.time.split("\\.")[0]);
            holder.approvalId.setText(order.approvalId);
            holder.category.setText(order.category);
            holder.product.setText(order.product);
            holder.hotIceOption.setText(order.hotIceOption);
            holder.sizeOption.setText(order.sizeOption);
            holder.blendOption.setText(order.blendOption);
            holder.shotOption.setText("+" + order.shotOption + "샷");
            holder.price.setText(order.price + "원");
            if (order.cancelId.equals("")) {
                holder.canceled.setText("정상승인");
            } else {
                holder.canceled.setText("결제취소");
            }
            if (order.approvalId.equals("-")) {
                holder.checkoutInfo.setVisibility(View.GONE);
            } else {
                holder.checkoutInfo.setOnClickListener(view -> {
                    if(order.approvalId.startsWith("페이코")){
                        // split by underscore
                        holder.checkoutPaycoInformationDialog(order.approvalId.split("_")[1]);
                    }else {
                        holder.checkoutInformationDialog(order.approvalId);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            TextView datetime;
            TextView approvalId;
            TextView category;
            TextView product;
            TextView hotIceOption;
            TextView sizeOption;
            TextView blendOption;
            TextView shotOption;
            TextView price;
            TextView canceled;
            Button checkoutInfo;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layout = itemView.findViewById(R.id.history_item_layout);
                datetime = itemView.findViewById(R.id.history_item_datetime);
                approvalId = itemView.findViewById(R.id.history_item_approval_id);
                category = itemView.findViewById(R.id.history_item_category);
                product = itemView.findViewById(R.id.history_item_product);
                hotIceOption = itemView.findViewById(R.id.history_item_hot_ice_option);
                sizeOption = itemView.findViewById(R.id.history_item_size_option);
                blendOption = itemView.findViewById(R.id.history_item_blend_option);
                shotOption = itemView.findViewById(R.id.history_item_shot_option);
                price = itemView.findViewById(R.id.history_item_price);
                canceled = itemView.findViewById(R.id.history_item_canceled);
                checkoutInfo = itemView.findViewById(R.id.history_item_checkout_info);
            }

            void checkoutInformationDialog(String approvalId) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ConstraintLayout checkoutInfoDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_checkout_info, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                TextView checkoutInformationDialogTitle = new TextView(getContext());
                checkoutInformationDialogTitle.setText("결제 정보");
                checkoutInformationDialogTitle.setPadding(0, 32, 0, 32);
                checkoutInformationDialogTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                checkoutInformationDialogTitle.setTextSize(32);
                AlertDialog checkoutInformationDialog = builder.setCustomTitle(checkoutInformationDialogTitle).setView(checkoutInfoDialogLayout)
                        .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss()).show();

                boolean canceled = false;
                String approvalDate = "";
                String price = "0";
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();

                Cursor iCursor = dbManager.selectColumns(DBManager.CHECKOUT,
                        new String[]{TRANS_TYPE, APPROVAL_DATE, TOTAL_AMOUNT, APPROVAL_NUM, CARD_NUM, CARD_NAME},
                        APPROVAL_NUM + "=?", new String[]{approvalId},
                        null, null, TRANS_TYPE);
                if (iCursor != null) {
                    while (iCursor.moveToNext()) {
                        if (iCursor.getString(0).equals(CHECKOUT_APPROVE)) {
                            TextView approvalDateTimeTextView = checkoutInfoDialogLayout.findViewById(R.id.approval_datetime);
                            String tempDateTime = iCursor.getString(1);
                            approvalDate = tempDateTime.substring(0, 6);
                            approvalDateTimeTextView.setText(
                                    String.format("%s년 %s월 %s일 %s시 %s분",
                                            tempDateTime.substring(0, 2),
                                            tempDateTime.substring(2, 4),
                                            tempDateTime.substring(4, 6),
                                            tempDateTime.substring(6, 8),
                                            tempDateTime.substring(8, 10)));
                            TextView approvalPriceTextView = checkoutInfoDialogLayout.findViewById(R.id.approval_price);
                            price = iCursor.getString(2);
                            approvalPriceTextView.setText(price + "원");
                            TextView approvalNumberTextView = checkoutInfoDialogLayout.findViewById(R.id.approval_number);
                            approvalNumberTextView.setText(iCursor.getString(3));
                            TextView approvalCardNumberTextView = checkoutInfoDialogLayout.findViewById(R.id.approval_card_number);
                            String tempCardNumber = iCursor.getString(4);
                            approvalCardNumberTextView.setText(
                                    String.format("%s-%s**-****-****",
                                            tempCardNumber.substring(0, 4),
                                            tempCardNumber.substring(4, 6)));
                            TextView approvalCardNameTextView = checkoutInfoDialogLayout.findViewById(R.id.approval_card_name);
                            approvalCardNameTextView.setText(iCursor.getString(5));
                        } else if (iCursor.getString(0).equals(CHECKOUT_REFUND) || iCursor.getString(0).equals(CHECKOUT_ROLLBACK)) {
                            canceled = true;
                            LinearLayout cancelLinearLayout = checkoutInfoDialogLayout.findViewById(R.id.cancel_linearLayout);
                            cancelLinearLayout.setVisibility(View.VISIBLE);
                            TextView cancelDateTimeTextView = checkoutInfoDialogLayout.findViewById(R.id.cancel_datetime);
                            String tempDateTime = iCursor.getString(1);
                            cancelDateTimeTextView.setText(
                                    String.format("%s년 %s월 %s일 %s시 %s분",
                                            tempDateTime.substring(0, 2),
                                            tempDateTime.substring(2, 4),
                                            tempDateTime.substring(4, 6),
                                            tempDateTime.substring(6, 8),
                                            tempDateTime.substring(8, 10)));
                        }
                    }
                    Button cancelPaymentButton = checkoutInfoDialogLayout.findViewById(R.id.button_cancel_payment);
                    if (!canceled) {
                        String finalApprovalDate = approvalDate;
                        String finalPrice = price;
                        cancelPaymentButton.setOnClickListener(view -> {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                            builder1.setMessage("결제를 취소 하시겠습니까?").setPositiveButton("Yes", (dialogInterface, i) -> {
                                        cancelPayment(getActivity(), approvalId, finalApprovalDate, finalPrice);
                                        checkoutInformationDialog.dismiss();
                                    })
                                    .setNegativeButton("No", null).show();
                        });
                    } else {
                        cancelPaymentButton.setVisibility(View.GONE);
                    }
                }
                dbManager.close();

                Button btnPositive = checkoutInformationDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.width = 150;
                btnPositive.setLayoutParams(layoutParams);
                btnPositive.setTextSize(24);

                Window window = checkoutInformationDialog.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.setLayout(1500, 500);
            }


            void checkoutPaycoInformationDialog(String approvalId) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ConstraintLayout checkoutPaycoInfoDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_checkout_info, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                TextView checkoutPaycoInformationDialogTitle = new TextView(getContext());
                checkoutPaycoInformationDialogTitle.setText("결제 정보");
                checkoutPaycoInformationDialogTitle.setPadding(0, 32, 0, 32);
                checkoutPaycoInformationDialogTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                checkoutPaycoInformationDialogTitle.setTextSize(32);
                AlertDialog checkoutPaycoInfoDialog = builder.setCustomTitle(checkoutPaycoInformationDialogTitle).setView(checkoutPaycoInfoDialogLayout)
                        .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss()).show();

                boolean canceled = false;
                String approvalDate = "";
                String price = "0";
                DBManager dbManager = new DBManager(getContext());
                dbManager.open();
                dbManager.create();
                Cursor iCursor = dbManager.selectColumns(DBManager.CHECKOUT_PAYCO,
                        new String[]{RESULT_CODE, SIGNATURE, PIN_CODE, TRADE_REQUEST_NO, TRADE_NO, APPROVAL_NO, APPROVAL_AMOUNT, APPROVAL_DATE_TIME, APPROVAL_CARD_NO, APPROVAL_COMPANY_NAME, TOTAL_AMOUNT},
                        APPROVAL_NO + "=?", new String[]{approvalId},
                        null, null, RESULT_CODE);
                if (iCursor != null) {
                    String signature="";
                    String pinCode="";
                    String tradeRequestNo="";
                    String tradeNo="";
                    String approvalNo="";
                    String approvalDateTime="";
                    String approvalAmount="";
                    String totalAmount = "";
                    while (iCursor.moveToNext()) {
                        if(iCursor.getCount()>0) {
                            if (iCursor.getString(0).equals("0")) {
                                signature = iCursor.getString(1);
                                pinCode = iCursor.getString(2);
                                tradeRequestNo = iCursor.getString(3);
                                tradeNo = iCursor.getString(4);
                                approvalNo = iCursor.getString(5);
                                approvalDateTime = iCursor.getString(7);
                                approvalAmount = iCursor.getString(6);
                                totalAmount = iCursor.getString(10);
                                TextView approvalDateTimeTextView = checkoutPaycoInfoDialog.findViewById(R.id.approval_datetime);
                                String tempDateTime = iCursor.getString(7);
                                approvalDate = tempDateTime.substring(2, 8);
                                approvalDateTimeTextView.setText(
                                        String.format("%s년 %s월 %s일 %s시 %s분",
                                                tempDateTime.substring(2, 4),
                                                tempDateTime.substring(4, 6),
                                                tempDateTime.substring(6, 8),
                                                tempDateTime.substring(8, 10),
                                                tempDateTime.substring(10, 12)));
                                TextView approvalPriceTextView = checkoutPaycoInfoDialog.findViewById(R.id.approval_price);
                                price = iCursor.getString(10);
                                approvalPriceTextView.setText(price + "원");
                                TextView approvalNumberTextView = checkoutPaycoInfoDialog.findViewById(R.id.approval_number);
                                approvalNumberTextView.setText(iCursor.getString(5));
                                TextView approvalCardNumberTextView = checkoutPaycoInfoDialog.findViewById(R.id.approval_card_number);
                                String tempCardNumber = iCursor.getString(8);
                                if(tempCardNumber != null && tempCardNumber !="" && tempCardNumber.length() > 7){
                                    approvalCardNumberTextView.setText(
                                            String.format("%s-%s**-****-****",
                                                    tempCardNumber.substring(0, 4),
                                                    tempCardNumber.substring(4, 6)));
                                }else{
                                    approvalCardNumberTextView.setText(
                                            "페이코 쿠폰"
                                    );
                                }
                                TextView approvalCardNameTextView = checkoutPaycoInfoDialogLayout.findViewById(R.id.approval_card_name);
                                approvalCardNameTextView.setText(iCursor.getString(9));
                                //} else if (iCursor.getString(0).equals(CHECKOUT_REFUND) || iCursor.getString(0).equals(CHECKOUT_ROLLBACK)) {
                            } else {
                                canceled = true;
                                LinearLayout cancelLinearLayout = checkoutPaycoInfoDialogLayout.findViewById(R.id.cancel_linearLayout);
                                cancelLinearLayout.setVisibility(View.VISIBLE);
                                TextView cancelDateTimeTextView = checkoutPaycoInfoDialogLayout.findViewById(R.id.cancel_datetime);
                                String tempDateTime = iCursor.getString(7);
                                cancelDateTimeTextView.setText(
                                        String.format("%s년 %s월 %s일 %s시 %s분",
                                                tempDateTime.substring(2, 4),
                                                tempDateTime.substring(4, 6),
                                                tempDateTime.substring(6, 8),
                                                tempDateTime.substring(8, 10),
                                                tempDateTime.substring(10, 12)));
                            }
                        }
                    }
                    Button cancelPaymentButton = checkoutPaycoInfoDialogLayout.findViewById(R.id.button_cancel_payment);
                    if (!canceled) {
                        String finalSignature = signature;
                        String finalPincode = pinCode;
                        String finalTradeRequestNo = tradeRequestNo;
                        String finalTradeNo = tradeNo;
                        String finalApprovalNo = approvalNo;
                        String finalApprovalDateTime = approvalDateTime;
                        String finalApprovalAmount = approvalAmount;
                        String finalTotalAmount = totalAmount;
                        cancelPaymentButton.setOnClickListener(view -> {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                            builder1.setMessage("결제를 취소 하시겠습니까?").setPositiveButton("Yes", (dialogInterface, i) -> {
                                Payco payco = new Payco(getContext());
                                    payco.setOnCancelCallback(new Payco.OnCancelCallback() {
                                        Handler handler = new Handler();
                                        @Override
                                        public void onCancelResult(String result) {
                                            if (result.equals("0")) {
                                                handler.post(() -> Toast.makeText(getContext(), "결제 취소가 완료되었습니다.", Toast.LENGTH_SHORT).show());
                                            }else if (result.equals("1002")) {
                                                handler.post(() -> Toast.makeText(getContext(), "이미 취소된 결제 건입니다.", Toast.LENGTH_SHORT).show());
                                            }
                                        }
                                        @Override
                                        public void onCancelException(Exception e) {
                                            handler.post(() -> Toast.makeText(getContext(), "결제 취소 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show());
                                        }
                                    });

                                payco.setPaycoApprovalInfo(finalSignature, finalTradeRequestNo, finalTradeNo, finalApprovalNo, finalApprovalAmount, finalApprovalDateTime);
                                payco.setPinCode(finalPincode);
                                double temp = Double.parseDouble(finalTotalAmount);
                                payco.setPaycoTotalAmount(temp);
                                payco.execute(Payco.CANCEL);
                                checkoutPaycoInfoDialog.dismiss();
                            })
                            .setNegativeButton("No", null).show();
                        });
                    } else {
                        cancelPaymentButton.setVisibility(View.GONE);
                    }
                }
                dbManager.close();

                Button btnPositive = checkoutPaycoInfoDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.width = 150;
                btnPositive.setLayoutParams(layoutParams);
                btnPositive.setTextSize(24);

                Window window = checkoutPaycoInfoDialog.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                window.setLayout(1500, 500);
            }
        }


    }
}