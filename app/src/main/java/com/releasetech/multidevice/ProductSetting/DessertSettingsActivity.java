package com.releasetech.multidevice.ProductSetting;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.MultiDevice.MultiDevice;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DessertSettingsActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private int numRows = 6;
    private int numCols = 6;

    private DBManager dbManager = new DBManager(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dessert_setting);
        gridLayout = findViewById(R.id.gridLayout);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {

                View childLayout = LayoutInflater.from(this).inflate(R.layout.dessert_test_layout, gridLayout, false);

                TextView dessertNumber = childLayout.findViewById(R.id.dessert_number);
                TextView dessertProduct = childLayout.findViewById(R.id.dessert_product);
                TextView dessertCurrent = childLayout.findViewById(R.id.dessert_product_current);
                Button rechargeButton = childLayout.findViewById(R.id.button_recharge);
                Button testButton = childLayout.findViewById(R.id.button_test);

                int finalRow = row;
                int finalCol = col;

                int coordinate = finalRow * 10 + finalCol;
                int productNumber = finalRow * numCols + finalCol + 1;

                try {
                    String name = PreferenceManager.getString(this, "product_" + productNumber + "_name");
                    String price = PreferenceManager.getString(this, "product_" + productNumber + "_price");
                    String totalCount = PreferenceManager.getString(this, "product_" + productNumber + "_total_count");
                    AtomicReference<String> currentCount = new AtomicReference<>(PreferenceManager.getString(this, "product_" + productNumber + "_current_count"));

                    Log.i("재고 테스트", "이름"+name);
                    Log.i("재고 테스트", "가격"+price);
                    Log.i("재고 테스트", "현재개수"+totalCount);
                    Log.i("재고 테스트", "총개수"+currentCount);

                    dessertProduct.setText(name);
                    dessertCurrent.setText(currentCount + " / " + totalCount);
                    if (Objects.equals(currentCount.get(), totalCount))
                        childLayout.setBackgroundColor(0xFFA2C1A6);
                    else if (Integer.parseInt(currentCount.get()) == 0)
                        childLayout.setBackgroundColor(0xFFC1A2A2);
                    else
                        childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));

                    rechargeButton.setOnClickListener(v -> {
                        PreferenceManager.setString(this, "product_" + productNumber + "_current_count", totalCount);
                        currentCount.set(PreferenceManager.getString(this, "product_" + productNumber + "_current_count"));
                        dessertCurrent.setText(currentCount + " / " + totalCount);
                        if (Objects.equals(currentCount.get(), totalCount))
                            childLayout.setBackgroundColor(0xFFA2C1A6);
                        else if (Integer.parseInt(currentCount.get()) == 0)
                            childLayout.setBackgroundColor(0xFFC1A2A2);
                        else
                            childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));
                    });

                    testButton.setOnClickListener(v -> {
                        try{
                            MultiDevice.throwOut(this, productNumber, new MultiDevice.OnThrowOutListener() {
                                @Override
                                public void onThrowOut(String productName) {
//                                    String is = "를";
//                                    char lastName = productName.charAt(productName.length() - 1);
//                                    if (lastName >= 0xAC00 && lastName <= 0xD7A3) {
//                                        if ((lastName - 0xAC00) % 28 > 0) {
//                                            is = "을";
//                                        }
//                                    }
//                                    Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");
                                }

                                @Override
                                public void onThrowOutDone() {
//                                    dessertCurrent.setText(currentCount + " / " + totalCount);
//                                    if (currentCount.equals(totalCount))
//                                        childLayout.setBackgroundColor(0xFFA2C1A6);
//                                    else if (currentCount.get().equals("0"))
//                                        childLayout.setBackgroundColor(0xFFC1A2A2);
//                                    else
//                                        childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));
                                }
                            });
                        }catch(Exception e){
                        }
//                        try {
//                            if (MultiDevice.locked) return;
//                            if (Integer.parseInt(currentCount.get()) > 0) {
//                                //todo 멀티디바이스 투출
//                            MultiDevice.throwOut(this, product, new MultiDevice.OnThrowOutListener() {
//                                @Override
//                                public void onThrowOut(String productName) {
//
//                                    //Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");
//                                }
//
//                                @Override
//                                public void onThrowOutDone() {
//                                    dessertCurrent.setText(currentCount + " / " + totalCount);
//                                    if (currentCount.equals(totalCount))
//                                        childLayout.setBackgroundColor(0xFFA2C1A6);
//                                    else if (currentCount.get().equals("0"))
//                                        childLayout.setBackgroundColor(0xFFC1A2A2);
//                                    else
//                                        childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));
//
//                                }
//                            });
//                            } else {
//                            }
//                        } catch (Exception e) {
//                        }
                    });
                }catch (Exception e){

                }
                dessertNumber.setText("" + (row * numCols + col + 1));

                GridLayout.Spec rowSpec = GridLayout.spec(row);
                GridLayout.Spec colSpec = GridLayout.spec(col);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, colSpec);
                childLayout.setLayoutParams(layoutParams);
                childLayout.setTop(10);
                childLayout.setBottom(10);
                childLayout.setLeft(10);
                childLayout.setRight(10);
                gridLayout.addView(childLayout);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager.open();
        dbManager.create();
    }
}