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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.CartItem;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.DataLoader;
import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.Manager.CheckoutManager;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.MultiDevice.MultiDevice;
import com.releasetech.multidevice.Stock.Stock;
import com.releasetech.multidevice.Tool.Cache;
import com.releasetech.multidevice.Tool.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class OrderActivity extends AppCompatActivity {

    private static final String TAG = "[ORDER]";
    private ArrayList<CartItem> arrayList = new ArrayList<>();

    int SEND_REQUEST_CODE = 1;
    int SEND_REQUEST_CHKVALID = 2;
    int SEND_REQUEST_CHKCARDBIN = 3;
    int SEND_REQUEST_CHKCASHIC = 4;
    int SEND_REQUEST_CHKMEMBERSHIP = 5;
    int SEND_REQUEST_NORMAL = 6;
    char fs = 0x1C;

    /* Order */
    CartManager cartManager;
    boolean loadCartCache = false;

    /* Stock */
    private Stock stock;
    DessertItem dessertItem;

    CartViewAdapter adapter = new CartViewAdapter();

    /* Password Related */
    private int settingsCount = 0;
    private PasswordManager passwordManager;
    DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        passwordManager = new PasswordManager(this);
        dbManager = new DBManager(this);
        dbManager.open();
        dbManager.create();

        Utils.hideNavBar(getWindow());

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
        numberClick();

        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> {
            ArrayList throwOutProduct = new ArrayList();
            for(int i=0; i< cartManager.getCount(); i++){
                throwOutProduct.add(cartManager.getItem(i).productName);
            }
            Log.i("테스트", throwOutProduct.toString());
            Intent intent = new Intent(OrderActivity.this, ThrowOutActivity.class);
            intent.putExtra("ThrowOutProduct", throwOutProduct);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.cart_recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        Button checkoutButton = findViewById(R.id.button_checkout);
        checkoutButton.setOnClickListener(view ->{
            Log.i("테스트", "-------------------");
            checkout();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbManager.open();
        dbManager.create();

        if (loadCartCache) {
            try {
                cartManager = ((CartManager) Cache.Read(this, "cart_cache")).clone();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            //cartManager.setOnUpdateListner(dataManager -> stock.applyCart(cartManager));
            loadCartCache = false;
        } else {
            cartManager = new CartManager(Integer.parseInt(PreferenceManager.getString(this, "cart_quantity")));
            //cartManager.setOnUpdateListner(dataManager -> stock.applyCart(cartManager));
            clearCart();
            adapter.notifyDataSetChanged();
            TextView textPrice = findViewById(R.id.total_price);
            textPrice.setText("합계 : 0원");
        }
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
            try {
                if (adapter.getItemCount() < 5) {
                    if(cartManager.getCount()<5) {
                        cartView(number);
                    }
                } else {
                    try {
                        Utils.showToast(this, "한 번에 5개까지 주문 가능합니다");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            number.setLength(0);
            numberText.setText(number);
        });
    }

    private void checkout(){
        Utils.logD(TAG, "카드 결제 시도: " + cartManager.getTotalPrice() + "원");
        for(int i=0; i<cartManager.getCount(); i++) {
            Log.i("카트 매니저", cartManager.getItem(i).number+"");
        }
        CheckoutManager.checkout(this, cartManager.getTotalPrice());
        loadCartCache = true;
    }

    private void clearCart() {
        if (cartManager != null) {
            cartManager.clear();
        }
    }

    public void cartView(StringBuilder sb) {

        String productNumber = sb.toString();
        Long itemCategory = Long.parseLong(PreferenceManager.getString(this, "product_"+productNumber+"_category"));
        String itemName = PreferenceManager.getString(this, "product_"+productNumber+"_name");
        int itemPrice = Integer.parseInt(PreferenceManager.getString(this, "product_"+productNumber+"_price"));
        int number = Integer.parseInt(PreferenceManager.getString(this, "product_"+productNumber+"_number"));

        CartItem cartItem = new CartItem(itemCategory.toString(), itemName, itemPrice, 1, number);

        TextView textPrice = findViewById(R.id.total_price);

        for (CartItem item : arrayList){
            if (item.number == cartItem.number) {
                cartManager.add(cartItem);
                item.count++;
                adapter.notifyDataSetChanged();
                textPrice.setText("합계 : " + cartManager.getTotalPrice() + "원");
                return;
            }
        }
        cartManager.add(cartItem);
        Log.i("임시 테스트", cartManager.getCount()+"");

        arrayList.add(cartItem);
        textPrice.setText("합계 : " + cartManager.getTotalPrice() + "원");

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
                    if(cartManager.getCount()<5) {
                        int position = getAdapterPosition();
                        int count = Integer.parseInt(itemCount.getText().toString());
                        CartItem.set(position, new CartItem(CartItem.get(position).categoryName, CartItem.get(position).productName, CartItem.get(position).price, (count + 1), CartItem.get(position).number));
                        cartManager.add(CartItem.get(position));
                        adapter.notifyDataSetChanged();
                    }
                    textPrice.setText("합계 : " + cartManager.getTotalPrice() + "원");
                });

                itemDecr.setOnClickListener(view -> {
                    if(cartManager.getCount() >0) {
                        int position = getAdapterPosition();
                        int count = Integer.parseInt(itemCount.getText().toString());
                        if (Integer.parseInt(itemCount.getText().toString()) > 1) {
                            CartItem.set(position, new CartItem(CartItem.get(position).categoryName, CartItem.get(position).productName, CartItem.get(position).price, (count - 1), CartItem.get(position).number));
                            cartManager.remove(CartItem.get(position));
                            adapter.notifyDataSetChanged();
                        }
                    }
                    textPrice.setText("합계 : " + cartManager.getTotalPrice() + "원");
                });

                itemRemove.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    cartManager.removeSameIndex(CartItem.get(position));
                    CartItem.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, CartItem.size());
                    textPrice.setText("합계 : " + cartManager.getTotalPrice() + "원");
                });
            }

            public void setItem(CartItem item) {
                itemName.setText(item.getProductName());
                itemPrice.setText(String.format("%,d₩",item.getPrice() * item.count));
                itemCount.setText(item.count + "");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_REQUEST_CODE) { //SEND_REQUEST_CODE에 대한 응답
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "결제 완료", Toast.LENGTH_SHORT).show();

                Stack stack = new Stack();
                for(int i=0; i< cartManager.getCount(); i++){
                    Log.i("테스트", "상품이름"+cartManager.getItem(i).productName);
                    Log.i("테스트", "전체금액 "+cartManager.getTotalPrice()+"");
                    stack.push(DataLoader.loadProductByNumber(dbManager, cartManager.getItem(i).number));
                }
                MultiDevice.throwOutNext(this, stack, new MultiDevice.OnThrowOutListener() {
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
                        if (OrderActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                            runOnUiThread(OrderActivity.this::onResume);
                        }
                    }
                });

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "결제 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }


}