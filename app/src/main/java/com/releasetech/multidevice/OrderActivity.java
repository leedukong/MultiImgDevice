package com.releasetech.multidevice;

import static com.releasetech.multidevice.Database.DataLoader.loadProductByNumber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.CartItem;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Tool.Utils;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {

    private static final String TAG = "[ORDER]";

    private ArrayList<CartItem> arrayList = new ArrayList<>();
    CartViewAdapter adapter = new CartViewAdapter();

    /* Password Related */
    private int settingsCount = 0;
    private PasswordManager passwordManager;
    DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        dbManager = new DBManager(this);
        dbManager.open();
        dbManager.create();

        Button hiddenButton = findViewById(R.id.setting_button);
        hiddenButton.setOnClickListener(view -> {
            settingsCount++;
            Utils.logD(TAG, "설정 진입 버튼 : " + settingsCount);
            if (settingsCount == 10) {
                //if (passwordManager.wrongPasswordCount >= 10) return;
                passwordManager.passwordDialog(this);
                settingsCount = 0;
            } else if (settingsCount == 100) {
                passwordManager.resetPasswords();
                settingsCount = 0;
            } else if (settingsCount > 90) {
                Utils.showToast(this, "패스워드 초기화까지 남은 횟수 : " + (100 - settingsCount));
            }
        });

        passwordManager = new PasswordManager(this);

        numberClick();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(OrderActivity.this, DischargeActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.cart_recyclerView);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    public void numberClick() {

        Button buttonNumber[] = new Button[10];
        buttonNumber[0] = findViewById(R.id.button0);
        buttonNumber[1] = findViewById(R.id.button1);
        buttonNumber[2] = findViewById(R.id.button2);
        buttonNumber[3] = findViewById(R.id.button3);
        buttonNumber[4] = findViewById(R.id.button4);
        buttonNumber[5] = findViewById(R.id.button5);
        buttonNumber[6] = findViewById(R.id.button6);
        buttonNumber[7] = findViewById(R.id.button7);
        buttonNumber[8] = findViewById(R.id.button8);
        buttonNumber[9] = findViewById(R.id.button9);
        Button buttonBack = findViewById(R.id.buttonBack);
        Button buttonAdd = findViewById(R.id.buttonOk);

        StringBuilder number = new StringBuilder();
        EditText numberText = findViewById(R.id.numberText);

        for (int i = 0; i < 10; i++) {
            buttonNumber[i].setOnClickListener(view -> {
                Button btn1 = (Button) view;
                if (number.length() == 0 && btn1.getText().toString().equals("0")) {
                    return;
                } else if (number.length() >= 2) {
                    return;
                }
                number.append(btn1.getText().toString());
                numberText.setText(number);
            });
        }
        buttonBack.setOnClickListener(view -> {
            number.delete(number.length()-1, number.length());
            numberText.setText(number);
        });

        buttonAdd.setOnClickListener(view -> {

            if (adapter.getItemCount() < 5) {
                CartView(number);
            } else {
                try {
                    Utils.showToast(this, "한 번에 5개까지 주문 가능합니다");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            number.setLength(0);
            numberText.setText(number);
        });
    }

    public void CartView(StringBuilder number) {

        Long CartItemCategory = loadProductByNumber(dbManager, Integer.parseInt(number.toString())).category;
        String CartItemName = loadProductByNumber(dbManager, Integer.parseInt(number.toString())).name;
        Integer CartItemPrice = loadProductByNumber(dbManager, Integer.parseInt(number.toString())).price;

        CartItem cartItem = new CartItem(CartItemCategory.toString(), CartItemName, CartItemPrice, 1, Integer.parseInt(number.toString()));

        for (CartItem item : arrayList){
            if (item.number == cartItem.number) {
                item.count++;
                item.price = item.price / (item.count - 1) * item.count;
                adapter.notifyDataSetChanged();

                TextView textPrice = findViewById(R.id.total_price);
                textPrice.setText("합계 : " + arrayList.stream().mapToInt(CartItem::getPrice).sum() + "원");
                return;
            }
        }
        arrayList.add(cartItem);

        TextView textPrice = findViewById(R.id.total_price);
        textPrice.setText("합계 : " + arrayList.stream().mapToInt(CartItem::getPrice).sum() + "원");

        adapter.setItems(arrayList);
    }

    private class CartViewAdapter extends RecyclerView.Adapter<CartViewAdapter.ViewHolder> {

        ArrayList<CartItem> CartItem = new ArrayList<>();

        @NonNull
        @Override
        public CartViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            CartItem item = CartItem.get(position);
            viewHolder.setItem(item);
        }

        public void setItems(ArrayList<CartItem> items) {
            this.CartItem = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return CartItem.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView itemName;
            TextView itemPrice;
            Button itemIncr;
            Button itemDecr;
            TextView itemCount;
            Button itemRemove;
            TextView textPrice;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.product_cart_name);
                itemPrice = itemView.findViewById(R.id.product_cart_price);
                itemIncr = itemView.findViewById(R.id.cart_incr);
                itemDecr = itemView.findViewById(R.id.cart_decr);
                itemCount = itemView.findViewById(R.id.product_cart_count);
                itemRemove = itemView.findViewById(R.id.cart_remove);

                textPrice = findViewById(R.id.total_price);

                itemIncr.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    int count = Integer.parseInt(itemCount.getText().toString());
                    CartItem.set(position, new CartItem(CartItem.get(position).categoryName, CartItem.get(position).productName, CartItem.get(position).price / count * (count+1), (count + 1), CartItem.get(position).number));
                    int totalPrice= Integer.parseInt(textPrice.getText().toString().replaceAll("[^0-9]", ""));
                    textPrice.setText("합계 : " + (totalPrice + (CartItem.get(position).price / (count+1))) + "원");
                    adapter.notifyDataSetChanged();
                });

                itemDecr.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    int count = Integer.parseInt(itemCount.getText().toString());
                    if (Integer.parseInt(itemCount.getText().toString()) > 1) {
                        CartItem.set(position, new CartItem(CartItem.get(position).categoryName, CartItem.get(position).productName, CartItem.get(position).price / count * (count-1), (count - 1), CartItem.get(position).number));
                        int totalPrice= Integer.parseInt(textPrice.getText().toString().replaceAll("[^0-9]", ""));
                        textPrice.setText("합계 : " + (totalPrice - (CartItem.get(position).price / (count-1))) + "원");
                        adapter.notifyDataSetChanged();
                    }
                });

                itemRemove.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    int totalPrice= Integer.parseInt(textPrice.getText().toString().replaceAll("[^0-9]", ""));
                    textPrice.setText("합계 : " + Integer.parseInt(String.valueOf(totalPrice - CartItem.get(position).price)) + "원");
                    CartItem.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, CartItem.size());
                });
            }

            public void setItem(CartItem item) {
                itemName.setText(item.getProductName());
                itemPrice.setText(item.getPriceText());
                itemCount.setText(item.count + "");
            }
        }
    }
}