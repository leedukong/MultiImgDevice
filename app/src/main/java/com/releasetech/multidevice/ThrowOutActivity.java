package com.releasetech.multidevice;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Sound.SoundService;
import com.releasetech.multidevice.Stock.Stock;
import com.releasetech.multidevice.Tool.Utils;

import com.releasetech.multidevice.MultiDevice.MultiDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;


public class ThrowOutActivity extends AppCompatActivity {

    int i = -2;
    private DBManager dbManager = new DBManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throw_out);

        Utils.hideNavBar(getWindow());

        SoundService.play(getApplicationContext(), SoundService.DESSERT);
        throwOutView();
        throwOutProduct();
    }

    public void throwOutView() {
        ArrayList throwOutProduct = getIntent().getStringArrayListExtra("throwOutProduct");
        Log.i("출하", "출하할 상품: " + throwOutProduct);

        View throwOutLayout[] = new View[5];
        throwOutLayout[0] = findViewById(R.id.throw_layout_1);
        throwOutLayout[1] = findViewById(R.id.throw_layout_2);
        throwOutLayout[2] = findViewById(R.id.throw_layout_3);
        throwOutLayout[3] = findViewById(R.id.throw_layout_4);
        throwOutLayout[4] = findViewById(R.id.throw_layout_5);

        TextView throwProductName[] = new TextView[5];
        throwProductName[0] = findViewById(R.id.throw_product_1);
        throwProductName[1] = findViewById(R.id.throw_product_2);
        throwProductName[2] = findViewById(R.id.throw_product_3);
        throwProductName[3] = findViewById(R.id.throw_product_4);
        throwProductName[4] = findViewById(R.id.throw_product_5);

        for (int i = 0; i < throwOutProduct.size(); i++) {
            throwProductName[i].setText(throwOutProduct.get(i).toString());
        }
        for (int j = 4; j >= throwOutProduct.size(); j--) {
            throwOutLayout[j].setVisibility(View.GONE);
        }
    }

    public void throwOutProduct() {
        ArrayList arrayList = getIntent().getStringArrayListExtra("stack");
        ArrayList arrayList1 = new ArrayList<>(arrayList);
        Collections.reverse(arrayList1);
        Stack<String> stack = new Stack<>();
        stack.addAll(arrayList1);

        ImageView throwOutImage[] = new ImageView[5];
        throwOutImage[0] = findViewById(R.id.throw_button_1);
        throwOutImage[1] = findViewById(R.id.throw_button_2);
        throwOutImage[2] = findViewById(R.id.throw_button_3);
        throwOutImage[3] = findViewById(R.id.throw_button_4);
        throwOutImage[4] = findViewById(R.id.throw_button_5);
        TextView throwOutState[] = new TextView[5];
        throwOutState[0] = findViewById(R.id.throw_state_1);
        throwOutState[1] = findViewById(R.id.throw_state_2);
        throwOutState[2] = findViewById(R.id.throw_state_3);
        throwOutState[3] = findViewById(R.id.throw_state_4);
        throwOutState[4] = findViewById(R.id.throw_state_5);

        MultiDevice.throwOutNext(this, stack, new MultiDevice.OnThrowOutListener() {
            Stock stock = new Stock(getApplicationContext(), dbManager);
            @Override
            public void onThrowOut(String productName) {
                i++;
                if (i > -1) {
                    runOnUiThread(() -> {
                        if (PreferenceManager.getString(getApplicationContext(), "product_" + arrayList.get(i) + "_current_count").equals("0")) {
                            throwOutState[i].setText("(재고부족)");
                            throwOutImage[i].setColorFilter(Color.parseColor("#FEBF00"));
                            return;
                        }
                        throwOutState[i].setText("(투출완료)");
                        throwOutImage[i].setColorFilter(Color.parseColor("#00AF50"));
                        stock.decreaseStockCount((Integer) arrayList.get(i));
                    });
                }
//                String is = "를";
//                char lastName = productName.charAt(productName.length() - 1);
//                if (lastName >= 0xAC00 && lastName <= 0xD7A3) {
//                    if ((lastName - 0xAC00) % 28 > 0) {
//                        is = "을";
//                    }
//                }
//                Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");
            }

            @Override
            public void onThrowOutDone() {
                i++;
//                if (OrderActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                    runOnUiThread(OrderActivity.this::onResume);
//                }
                runOnUiThread(() -> {
                    if (PreferenceManager.getString(getApplicationContext(), "product_" + arrayList.get(i) + "_current_count").equals("0")) {
                        throwOutState[i].setText("(재고부족)");
                        throwOutImage[i].setColorFilter(Color.parseColor("#FEBF00"));
                        return;
                    }
                    throwOutState[arrayList.size()-1].setText("(투출완료)");
                    throwOutImage[arrayList.size()-1].setColorFilter(Color.parseColor("#00AF50"));
                    stock.decreaseStockCount((Integer) arrayList.get(i));
                });
                SoundService.play(getApplicationContext(), SoundService.THANK_YOU);
                finish();
            }
        });
    }
}