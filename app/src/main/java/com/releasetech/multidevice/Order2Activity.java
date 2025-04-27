package com.releasetech.multidevice;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

import com.releasetech.multidevice.Database.Data.CartItem;
import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.Manager.CheckoutManager;
import com.releasetech.multidevice.Manager.PasswordManager;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.Manager.SalesManager;
import com.releasetech.multidevice.Sound.SoundService;
import com.releasetech.multidevice.Tool.Utils;

import org.checkerframework.checker.units.qual.C;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Order2Activity extends AppCompatActivity {

    LinearLayout row1, row2, row3;
    private int settingsCount = 0;
    int SEND_REQUEST_CODE = 1;
    int SEND_REQUEST_CHKVALID = 2;
    int SEND_REQUEST_CHKCARDBIN = 3;
    int SEND_REQUEST_CHKCASHIC = 4;
    int SEND_REQUEST_CHKMEMBERSHIP = 5;
    int SEND_REQUEST_NORMAL = 6;
    char fs = 0x1C;
    CartManager cartManager;
    private static final String TAG = "[ORDER]";
    private PasswordManager passwordManager;
    private LinearLayout layoutGuide;
    private LinearLayout layoutCart;
    private TextView ment;
    private TextView priceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);

        cartManager = new CartManager(5);
        ment = findViewById(R.id.ment);
        layoutCart = findViewById(R.id.layout_cart);
        layoutGuide = findViewById(R.id.layout_guide);
        priceText = findViewById(R.id.text_price);
        layoutCart.setVisibility(View.GONE);
        priceText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        row1 = findViewById(R.id.row1);
        row2 = findViewById(R.id.row2);
        row3 = findViewById(R.id.row3);

        TextView home = findViewById(R.id.home);
        home.setOnClickListener(v -> {
            Intent intent = new Intent(Order2Activity.this, MainActivity.class);
            startActivity(intent);
        });
        TextView cancle = findViewById(R.id.btn_cancel);
        cancle.setOnClickListener(v -> {
            if (cartManager.getCount()>0) {
                cartManager.clear();
                updateCart(cartManager);
                ment.setText("- 키오스크로 주문하는 방법 -");
                layoutGuide.setVisibility(View.VISIBLE);
                layoutCart.setVisibility(View.GONE);
            }
        });

        ViewGroup[] rows = { row1, row2, row3 };

        int category1count = Integer.parseInt(PreferenceManager.getString(getApplicationContext(), "category_1_count"));
        int category2count = Integer.parseInt(PreferenceManager.getString(getApplicationContext(), "category_2_count"));
        int category3count = Integer.parseInt(PreferenceManager.getString(getApplicationContext(), "category_3_count"));
        int categorySize = Integer.parseInt(PreferenceManager.getString(getApplicationContext(), "category_size"));

        for (int r = 0; r < categorySize; r++) {
            int count = 0;
            if (r == 0) {
                count = category1count;
            } else if (r == 1) {
                count = category2count;
            } else if (r == 2) {
                count = category3count;
            }

            for (int i = 0; i < count; i++) {
                View productView = inflater.inflate(R.layout.item_product, rows[r], false);

                ImageButton productImageButton = productView.findViewById(R.id.product_image_button);

                TextView productName = productView.findViewById(R.id.product_name);
                TextView productPrice = productView.findViewById(R.id.product_price);

                String imagePath = "/data/user/0/com.releasetech.multidevice/files/image"+(i+12*r+1)+".jpg";
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Uri imageUri = Uri.fromFile(imageFile);
                    productImageButton.setImageURI(imageUri);
                } else {
                }

                productName.setText(PreferenceManager.getString(getApplicationContext(),"product_"+((i+12*r+1)+"_name")));
                productPrice.setText(PreferenceManager.getString(getApplicationContext(),"product_"+ (i+12*r+1) +"_price"));

                int finalI = i;
                int finalR = r;
                try {
                    productImageButton.setOnClickListener(v -> {
                        if (cartManager.getCount() == 0) {
                            ment.setText("장바구니");
                            layoutGuide.setVisibility(View.GONE);
                            layoutCart.setVisibility(View.VISIBLE);
                            priceText.setVisibility(View.VISIBLE);
                        }
                        if (cartManager.getCount() > 5) {
                            Toast.makeText(getApplicationContext(), "상품은 다섯 개까지 추가 가능합니다.", Toast.LENGTH_SHORT);
                        } else {
                            String productNumber = String.valueOf((12 * finalR + finalI + 1));
                            String itemCategory = PreferenceManager.getString(this, "product_" + productNumber + "_category");
                            String itemName = PreferenceManager.getString(this, "product_" + productNumber + "_name");
                            int itemPrice = Integer.parseInt(PreferenceManager.getString(this, "product_" + productNumber + "_price"));
                            int number = Integer.parseInt(PreferenceManager.getString(this, "product_" + productNumber + "_number"));

                            CartItem cartItem = new CartItem(itemCategory.toString(), itemName, itemPrice, 1, number);
                            cartManager.add(cartItem);
                            updateCart(cartManager);
                            priceText.setText("합계 : "+cartManager.getTotalPrice()+"");
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                rows[r].addView(productView);
            }
        }
        Button[] plusButtons = {findViewById(R.id.plus1), findViewById(R.id.plus2), findViewById(R.id.plus3), findViewById(R.id.plus4), findViewById(R.id.plus5)
        };

        Button[] minusButtons = {findViewById(R.id.minus1), findViewById(R.id.minus2), findViewById(R.id.minus3), findViewById(R.id.minus4), findViewById(R.id.minus5)
        };

        for (int i = 0; i < plusButtons.length; i++) {
            final int index = i; // 람다 안에서 사용하려면 final 또는 effectively final이어야 해
            plusButtons[i].setOnClickListener(v -> {
                if (cartManager.getItem(index) != null) {
                    if (cartManager.getCount() > 5) {
                        Toast.makeText(getApplicationContext(), "상품은 다섯 개까지 추가 가능합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        cartManager.add(cartManager.getItem(index));
                        priceText.setText("합계 : "+cartManager.getTotalPrice()+"");
                        updateCart(cartManager);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "상품을 클릭하여 추가해주세요.", Toast.LENGTH_SHORT).show();
                }
            });

            minusButtons[i].setOnClickListener(v -> {
                if (cartManager.getItem(index) != null) {
                    if (cartManager.getCount() > 0) {
                        if (cartManager.getCount() == 1){
                            ment.setText("- 키오스크로 주문하는 방법 -");
                            layoutGuide.setVisibility(View.VISIBLE);
                            layoutCart.setVisibility(View.GONE);
                            priceText.setVisibility(View.GONE);
                        }
                        cartManager.remove(cartManager.getItem(index));
                        updateCart(cartManager);
                        priceText.setText("합계 : "+cartManager.getTotalPrice()+"");
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "취소할 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button button = findViewById(R.id.btn_pay);
        button.setOnClickListener(view -> {
            if(!PreferenceManager.getBoolean(this, "free_of_charge")) {
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
                checkout();
            }else{
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

                Intent intent = new Intent(Order2Activity.this, ThrowOutActivity.class);
                intent.putExtra("stack", stack);
                intent.putExtra("throwOutProduct", throwOutProduct);
                startActivity(intent);
            }
        });

        passwordManager = new PasswordManager(this);
        TextView settingText = findViewById(R.id.setting_button);
        settingText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                settingsCount++;
                Utils.logD(TAG, "설정 진입 버튼 : " + settingsCount);
                if (settingsCount == 5) {
                    //if (passwordManager.wrongPasswordCount >= 10) return;
                    passwordManager.passwordDialog(Order2Activity.this);
                    settingsCount = 0;
                } else if (settingsCount == 100) {
                    passwordManager.resetPasswords();
                    settingsCount = 0;
                } else if (settingsCount > 90) {
                    Utils.showToast(getApplicationContext(), "패스워드 초기화까지 남은 횟수 : " + (100 - settingsCount));
                }
            }
        });

        TextView homeText = findViewById(R.id.home);
        homeText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Order2Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateCart(CartManager cartManager){
        TextView[] cartItemNames = {findViewById(R.id.cart1_name),findViewById(R.id.cart2_name),findViewById(R.id.cart3_name),findViewById(R.id.cart4_name), findViewById(R.id.cart5_name)};
        TextView[] cartItemPrices = {findViewById(R.id.cart1_price),findViewById(R.id.cart2_price),findViewById(R.id.cart3_price),findViewById(R.id.cart4_price),findViewById(R.id.cart5_price)};

        for(int i=0; i<5; i++) {
            cartItemNames[i].setText("");
            cartItemPrices[i].setText("");
            if(cartManager.getItem(i) != null) {
                Log.i("카트매니저 테스트", i + cartManager.getItem(i).productName + "\n");
            }
        }

        for(int i=0; i<cartManager.getCount(); i++) {
            cartItemNames[i].setText(cartManager.getItem(i).productName + "");
            cartItemPrices[i].setText(cartManager.getItem(i).price + "");
            if(cartManager.getItem(i) != null) {
            }
        }
    }

    private void checkout(){
        Utils.logD(TAG, "카드 결제 시도: " + cartManager.getTotalPrice() + "원");
        for(int i=0; i<cartManager.getCount(); i++) {
            Log.i("카트 매니저", cartManager.getItem(i).number+"");
        }
        //CheckoutManager.checkout(this, cartManager.getTotalPrice());
        CheckoutManager.checkout(this, 1004);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_REQUEST_CODE) { //SEND_REQUEST_CODE에 대한 응답
            if (resultCode == RESULT_OK) {
                SalesManager salesManager = new SalesManager(this);
                salesManager.addSale(cartManager.getTotalPrice());
                recvFS(data.getStringExtra("NVCATRECVDATA"));
                Toast.makeText(getApplicationContext(), "결제 완료", Toast.LENGTH_SHORT).show();
                SoundService.play(Order2Activity.this, SoundService.CHECKOUT_OK);

                Stack stack = new Stack();
                ArrayList throwOutProduct = new ArrayList();
                for(int i=0; i< cartManager.getCount(); i++){
                    stack.push(cartManager.getItem(i).number);
                    throwOutProduct.add(cartManager.getItem(i).productName);
                }
                Log.i("테스트", throwOutProduct.toString());

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    Intent intent = new Intent(Order2Activity.this, ThrowOutActivity.class);
                    intent.putExtra("stack", stack);
                    intent.putExtra("throwOutProduct", throwOutProduct);
                    startActivity(intent);
                }, 2000);
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
                        PreferenceManager.setString(this, "prev_nice_checkout_approval_CATID", strRecv15);
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