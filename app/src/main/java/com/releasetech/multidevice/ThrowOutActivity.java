package com.releasetech.multidevice;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class ThrowOutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throw_out);

        throwOutProductView();
    }

    @SuppressLint("WrongViewCast")
    public void throwOutProductView() {
        ArrayList throwOutProduct = (ArrayList) getIntent().getSerializableExtra("ThrowOutProduct");

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

        ImageButton throwOutButton[] = new ImageButton[5];
        throwOutButton[0] = findViewById(R.id.throw_button_1);
        throwOutButton[1] = findViewById(R.id.throw_button_2);
        throwOutButton[2] = findViewById(R.id.throw_button_3);
        throwOutButton[3] = findViewById(R.id.throw_button_4);
        throwOutButton[4] = findViewById(R.id.throw_button_5);
        TextView throwOutState[] = new TextView[5];
        throwOutState[0] = findViewById(R.id.throw_state_1);
        throwOutState[1] = findViewById(R.id.throw_state_2);
        throwOutState[2] = findViewById(R.id.throw_state_3);
        throwOutState[3] = findViewById(R.id.throw_state_4);
        throwOutState[4] = findViewById(R.id.throw_state_5);

        for (int i = 0; i < throwOutProduct.size(); i++) {
            int index = i;
            throwOutButton[i].setOnClickListener(v -> {
                if (throwOutState[index].isSelected()) {
                    throwOutState[index].setText("(투출전)");
                    throwOutButton[index].setColorFilter(Color.parseColor("#FE0000"));
                } else {
                    throwOutState[index].setText("(투출완료)");
                    throwOutButton[index].setColorFilter(Color.parseColor("#00AF50"));
                }
                throwOutState[index].setSelected(!throwOutState[index].isSelected());
            });
        }
    }

}