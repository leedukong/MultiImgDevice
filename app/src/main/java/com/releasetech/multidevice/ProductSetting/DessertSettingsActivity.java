package com.releasetech.multidevice.ProductSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DataLoader;
import com.releasetech.multidevice.MultiDevice.MultiDevice;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

public class DessertSettingsActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private int numRows = 9;
    private int numCols = 4;

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

                DBManager dbManager = new DBManager(this);
                try {
                    Product product = DataLoader.loadProductByNumber(dbManager, productNumber);
                    dessertProduct.setText(product.name);
                    int tempCurrentCount = DataLoader.loadCurrentStockById(dbManager, product.id);
                    dessertCurrent.setText(tempCurrentCount + " / " + product.total_count);
                    if (tempCurrentCount == product.total_count)
                        childLayout.setBackgroundColor(0xFFA2C1A6);
                    else if (tempCurrentCount == 0) childLayout.setBackgroundColor(0xFFC1A2A2);
                    else
                        childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));

                    rechargeButton.setOnClickListener(v -> {
                        dbManager.updateColumnToRechargeCount(product.id);
                        int currentCount = DataLoader.loadCurrentStockById(dbManager, product.id);
                        dessertCurrent.setText(currentCount + " / " + product.total_count);
                        if (currentCount == product.total_count)
                            childLayout.setBackgroundColor(0xFFA2C1A6);
                        else if (currentCount == 0) childLayout.setBackgroundColor(0xFFC1A2A2);
                        else
                            childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));
                    });

                    testButton.setOnClickListener(v -> {
                        if (MultiDevice.locked) return;
                        //int currentCount = DataLoader.loadCurrentStockById(dbManager, product.id);
                        //if (currentCount > 0) {
                        MultiDevice.throwOut(this, product, new MultiDevice.OnThrowOutListener() {
                            @Override
                            public void onThrowOut(String productName) {
                                String is = "를";
                                char lastName = productName.charAt(productName.length() - 1);
                                if (lastName >= 0xAC00 && lastName <= 0xD7A3) {
                                    if ((lastName - 0xAC00) % 28 > 0) {
                                        is = "을";
                                    }
                                }
                                Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");
                            }

                            @Override
                            public void onThrowOutDone() {
                                int currentCount = DataLoader.loadCurrentStockById(dbManager, product.id);
                                dessertCurrent.setText(currentCount + " / " + product.total_count);
                                if (currentCount == product.total_count)
                                    childLayout.setBackgroundColor(0xFFA2C1A6);
                                else if (currentCount == 0)
                                    childLayout.setBackgroundColor(0xFFC1A2A2);
                                else
                                    childLayout.setBackgroundColor(getResources().getColor(R.color.silver_sand));
                            }
                        });
                         //else {
                            //Utils.showToast(this, product.name + " 상품의 재고가 부족합니다.");
                        //}
                    });
                } catch (Exception e) {
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
}