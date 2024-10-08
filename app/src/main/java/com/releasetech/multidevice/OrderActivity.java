package com.releasetech.multidevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.releasetech.multidevice.Sound.SoundService;
import com.releasetech.multidevice.Stock.Stock;
import com.releasetech.multidevice.Tool.Cache;
import com.releasetech.multidevice.Tool.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

        Button button = findViewById(R.id.button_checkout);
        button.setOnClickListener(view -> {

            ArrayList arrayList1 = new ArrayList();
            for(int i=0; i< cartManager.getCount(); i++){
                arrayList1.add(cartManager.getItem(i).number);
            }
            Collections.sort(arrayList1);

            int count = Integer.parseInt(PreferenceManager.getString(this, "product_" + arrayList1.get(0) + "_current_count")) - 1;
            if (count < 0) {
                Toast.makeText(getApplicationContext(), arrayList1.get(0) + "번 재고 부족", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int j = 0; j < arrayList1.size() - 1; j++) {
                if (arrayList1.get(j) == arrayList1.get(j + 1)) {
                    count--;
                } else {
                    count = Integer.parseInt(PreferenceManager.getString(this, "product_" + arrayList1.get(j+1) + "_current_count")) - 1;
                }
                if (count < 0) {
                    Toast.makeText(getApplicationContext(), arrayList1.get(j+1) + "번 재고 부족", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Log.i("테스트", "-------------------");
            checkout();
        });

        RecyclerView recyclerView = findViewById(R.id.cart_recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
            adapter.CartItem.clear();
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
                if (adapter.getItemCount() < Integer.parseInt(PreferenceManager.getString(this, "cart_quantity"))) {
                    if(cartManager.getCount() < Integer.parseInt(PreferenceManager.getString(this, "cart_quantity"))) {
                        cartView(number);
                    }
                } else {
                    try {
                        Utils.showToast(this, "한 번에 " + Integer.parseInt(PreferenceManager.getString(this, "cart_quantity"))+ "개까지 주문 가능합니다");
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
        //CheckoutManager.checkout(this, cartManager.getTotalPrice());
        CheckoutManager.checkout(this, 1004);
        loadCartCache = true;
    }

    private void clearCart() {
        if (cartManager != null) {
            cartManager.clear();
        }
    }

    public void cartView(StringBuilder sb) {

        String productNumber = sb.toString();
        String itemCategory = PreferenceManager.getString(this, "product_"+productNumber+"_category");
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
                    if(cartManager.getCount() < Integer.parseInt(PreferenceManager.getString(OrderActivity.this, "cart_quantity"))) {
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
        Log.i("테스트", "결제 완료1");
        if (requestCode == SEND_REQUEST_CODE) { //SEND_REQUEST_CODE에 대한 응답
            Log.i("테스트", "결제 완료2");
            if (resultCode == RESULT_OK) {
                Log.i("테스트", "결제 완료3");
                recvFS(data.getStringExtra("NVCATRECVDATA"));
                Toast.makeText(getApplicationContext(), "결제 완료", Toast.LENGTH_SHORT).show();
                SoundService.play(OrderActivity.this, SoundService.CHECKOUT_OK);

                Stack stack = new Stack();
                ArrayList throwOutProduct = new ArrayList();
                for(int i=0; i< cartManager.getCount(); i++){
                    Log.i("테스트", "상품이름"+cartManager.getItem(i).productName);
                    Log.i("테스트", "전체금액 "+cartManager.getTotalPrice()+"");
//                    stack.push(DataLoader.loadProductByNumber(dbManager, cartManager.getItem(i).number));
                    stack.push(cartManager.getItem(i).number);
                    throwOutProduct.add(cartManager.getItem(i).productName);
                }
                Log.i("테스트", throwOutProduct.toString());

                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        Intent intent = new Intent(OrderActivity.this, ThrowOutActivity.class);
                        intent.putExtra("stack", stack);
                        intent.putExtra("throwOutProduct", throwOutProduct);
                        startActivity(intent);
                    }
                }, 2000);



                //MultiDevice.testThrow(this, 1);
//                Stack stack = new Stack();
//                for(int i=0; i< cartManager.getCount(); i++){
//                    Log.i("테스트", "상품이름"+cartManager.getItem(i).productName);
//                    Log.i("테스트", "전체금액 "+cartManager.getTotalPrice()+"");
//                    stack.push(DataLoader.loadProductByNumber(dbManager, cartManager.getItem(i).number));
//                }
//                MultiDevice.throwOutNext(this, stack, new MultiDevice.OnThrowOutListener() {
//                    @Override
//                    public void onThrowOut(String productName) {
//                        String is = "를";
//                        char lastName = productName.charAt(productName.length() - 1);
//                        if (lastName >= 0xAC00 && lastName <= 0xD7A3) {
//                            if ((lastName - 0xAC00) % 28 > 0) {
//                                is = "을";
//                            }
//                        }
//                        Utils.showToast(getApplicationContext(), productName + is + " 출하합니다.");
//                    }
//
//                    @Override
//                    public void onThrowOutDone() {
//                        if (OrderActivity.this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
//                            runOnUiThread(OrderActivity.this::onResume);
//                        }
//                    }
//                });
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "결제 실패", Toast.LENGTH_SHORT).show();
                SoundService.play(this, SoundService.CHECKOUT_FAIL);
            }
        }
    }

    String strRecv01, strRecv02, strRecv03, strRecv04, strRecv05, strRecv06, strRecv07, strRecv08, strRecv09, strRecv10, strRecv11, strRecv12, strRecv13, strRecv14, strRecv15, strRecv16, strRecv17, strRecv18, strRecv19, strRecv20, strRecv21, strRecv22, strRecv23, strRecv24, strRecv25, strRecv26, strRecv27, strRecv28, strRecv29, strRecv30;
    String cardNum;
    int strPrice;
    private void recvFS(String recvdata) {
        int i, j = 0, k = 0;
        for (i = 0; i < recvdata.length(); i++) {
            if (recvdata.substring(i, i + 1).equals(String.valueOf(fs))) {
                k++;
                switch (k) {
                    case 1: //거래구분
                        strRecv01 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 구분", strRecv01);
                        j = i + 1;
                        break;
                    case 2: //거래유형
                        strRecv02 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 유형", strRecv02);
                        j = i + 1;
                        break;
                    case 3: //응답코드
                        strRecv03 = recvdata.substring(j, i);
                        Log.i("환불 테스트 응답 코드", strRecv03);
                        j = i + 1;
                        break;
                    case 4: //거래금액
                        strRecv04 = recvdata.substring(j, i);
                        strPrice = Integer.valueOf(strRecv04);
                        PreferenceManager.setInt(this, "prev_nice_checkout_approval_price", strPrice);
                        Log.i("환불 테스트 거래 금액", strRecv04);
                        j = i + 1;
                        break;
                    case 5: //부가세
                        strRecv05 = recvdata.substring(j, i);
                        Log.i("환불 테스트 부가세", strRecv05);
                        j = i + 1;
                        break;
                    case 6: //봉사료
                        strRecv06 = recvdata.substring(j, i);
                        Log.i("환불 테스트 봉사료", strRecv06);
                        j = i + 1;
                        break;
                    case 7: //할부
                        strRecv07 = recvdata.substring(j, i);
                        Log.i("환불 테스트 할부", strRecv07);
                        j = i + 1;
                        break;
                    case 8: //승인번호
                        strRecv08 = recvdata.substring(j, i);
                        PreferenceManager.setString(this, "prev_nice_checkout_approval_no", strRecv08.replaceAll(" " , ""));
                        Log.i("나이스 테스트 8", PreferenceManager.getString(this, "prev_nice_checkout_approval_no"));
                        Log.i("환불 테스트 승인 번호", strRecv08);
                        j = i + 1;
                        break;
                    case 9: //승인일자
                        strRecv09 = recvdata.substring(j, i);
                        if(strRecv09.replaceAll(" ", "").length() > 6) {
                            PreferenceManager.setString(this, "prev_nice_checkout_approval_date", strRecv09.replaceAll(" ", "").substring(0, 6));
                            Log.i("나이스 테스트 9", PreferenceManager.getString(this, "prev_nice_checkout_approval_date"));
                            Log.i("환불 테스트 승인 일자", strRecv09);
                        }else {
                            PreferenceManager.setString(this, "prev_nice_checkout_approval_date", "");
                            Log.i("나이스 테스트 9-2", PreferenceManager.getString(this, "prev_nice_checkout_approval_date"));
                            Log.i("환불 테스트 승인 일자", strRecv09);
                        }
                        j = i + 1;
                        break;
                    case 10: //발급사코드
                        strRecv10 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발급사 코드", strRecv10);
                        j = i + 1;
                        break;
                    case 11: //발급사명
                        strRecv11 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발급사 이름", strRecv11);
                        j = i + 1;
                        break;
                    case 12: //매입사코드
                        strRecv12 = recvdata.substring(j, i);
                        Log.i("환불 테스트 매입사 코드", strRecv12);
                        j = i + 1;
                        break;
                    case 13: //매입사명
                        strRecv13 = recvdata.substring(j, i);
                        Log.i("환불 테스트 매입사 이름", strRecv13);
                        j = i + 1;
                        break;
                    case 14: //가맹점번호
                        strRecv14 = recvdata.substring(j, i);
                        Log.i("환불 테스트 가맹점 번호", strRecv14);
                        j = i + 1;
                        break;
                    case 15: //승인CATID
                        strRecv15 = recvdata.substring(j, i);
                        Log.i("환불 테스트 승인 CATID", strRecv15);
                        j = i + 1;
                        break;
                    case 16: //잔액
                        strRecv16 = recvdata.substring(j, i);
                        Log.i("환불 테스트 잔액", strRecv16);
                        j = i + 1;
                        break;
                    case 17: //응답메시지
                        strRecv17 = recvdata.substring(j, i);
                        Log.i("환불 테스트 응답 메시지", strRecv17);
                        j = i + 1;
                        break;
                    case 18: //카드BIN
                        strRecv18 = recvdata.substring(j, i);
                        cardNum = strRecv18.substring(0,6);
                        Log.i("환불 테스트 카드 BIN", cardNum);
                        j = i + 1;
                        break;
                    case 19: //카드구분
                        strRecv19 = recvdata.substring(j, i);
                        Log.i("환불 테스트 카드 구분", strRecv19);
                        j = i + 1;
                        break;
                    case 20: //전문관리번호
                        strRecv20 = recvdata.substring(j, i);
                        Log.i("환불 테스트 전문 관리 번호", strRecv20);
                        j = i + 1;
                        break;
                    case 21: //거래일련번호
                        strRecv21 = recvdata.substring(j, i);
                        Log.i("환불 테스트 거래 일련 번호", strRecv21);
                        //etCashnum.setText(strRecv21);
                        j = i + 1;
                        break;
                    case 22: //발생포인트(할인금액)
                        strRecv22 = recvdata.substring(j, i);
                        Log.i("환불 테스트 발생 포인트", strRecv22);
                        j = i + 1;
                        break;
                    case 23: //가용포인트(지불금액)
                        strRecv23 = recvdata.substring(j, i);
                        Log.i("환불 테스트 가용 포인트", strRecv23);
                        j = i + 1;
                        break;
                    case 24: //누적포인트(잔액한도)
                        strRecv24 = recvdata.substring(j, i);
                        Log.i("환불 테스트 누적 포인트", strRecv24);
                        j = i + 1;
                        break;
                    case 25: //캐시백가맹점
                        strRecv25 = recvdata.substring(j, i);
                        Log.i("환불 테스트 캐시백 가맹점", strRecv25);
                        j = i + 1;
                        break;
                    case 26: //캐시백승인번호
                        strRecv26 = recvdata.substring(j, i);
                        Log.i("환불 테스트 캐시백 승인 번호", strRecv26);
                        j = i + 1;
                        break;
                    case 27:
                        strRecv27 = recvdata.substring(j, i);
                        Log.i("환불 테스트 27", strRecv27);
                        j = i + 1;
                        break;
                    case 28:
                        strRecv28 = recvdata.substring(j, i);
                        Log.i("환불 테스트 28", strRecv28);
                        j = i + 1;
                        break;
                    case 29:
                        strRecv29 = recvdata.substring(j, i);
                        Log.i("환불 테스트 29", strRecv29);
                        j = i + 1;
                        break;
                    case 30:
                        strRecv30 = recvdata.substring(j, i);
                        Log.i("환불 테스트 30", strRecv30);
                        j = i + 1;
                        break;
                }
            }
        }
    }
}