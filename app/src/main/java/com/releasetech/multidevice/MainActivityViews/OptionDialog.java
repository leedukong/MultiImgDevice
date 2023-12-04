package com.releasetech.multidevice.MainActivityViews;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Database.Data.DessertItem;
import com.releasetech.multidevice.Database.Data.ImageSet;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DessertDataLoader;
import com.releasetech.multidevice.Manager.CartManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Stock.Stock;
import com.releasetech.multidevice.Tool.UIImageLoader;
import com.releasetech.multidevice.Tool.Utils;

import java.util.Objects;
import java.util.stream.Collectors;

public class OptionDialog {
    private static final String TAG = "[OPTION DIALOG]";
    private final CartManager cartManager;

    private Context context;
    private DBManager dbManager;
    private Stock stock;

    private boolean showing = false;
    private int optionCount;
    private OnUserInteractionListener listener;
    private TextView optionReceiptHotIceTextView;
    private TextView optionReceiptSizeTextView;
    private TextView optionReceiptBlendTextView;
    private TextView optionReceiptBlendPriceTextView;
    private TextView optionReceiptShotTextView;
    private TextView optionReceiptShotPriceTextView;
    private TextView optionReceiptMenuPriceTextView;
    private TextView optionReceiptMenuTextView;
    private ImageView optionProductImageView;
    private ConstraintLayout optionDialogLayout;
    private TextView totalPriceTextView;
    private DessertItem dessertItem;
    private ImageView checkHotIce;
    private ImageView checkSize;
    private ImageView checkShot;
    private ImageView checkBlend;
    private Product tempProduct;
    private OnResultListener resultListener;
    private AlertDialog optionDialog;
    private ImageSet tempImageSet;

    public OptionDialog(Context context, CartManager cartManager, DBManager dbManager, Stock stock) {
        this.context = context;
        this.cartManager = cartManager;
        this.dbManager = dbManager;
        this.stock = stock;
    }

    public AlertDialog show(int categoryIndex, int productIndex) {
        if (showing) {
            optionDialog.dismiss();
        }
        showing = true;
        optionCount = 1;

        Category tempCategory;
        tempCategory = DessertDataLoader.loadCategories(dbManager).stream().filter(category -> category.available > 0).collect(Collectors.toList()).get(categoryIndex);
        tempProduct = DessertDataLoader.loadProductsByCategoryId(dbManager, tempCategory.id, true).get(productIndex);
        tempImageSet = DessertDataLoader.loadImageSet(dbManager, tempProduct.image_set);
        String imagePath = tempImageSet.getImagePath(ImageSet.MENU_IDLE);
        Uri imageUri = Uri.parse(imagePath);

        dessertItem = new DessertItem();
        dessertItem.categoryName = tempCategory.name;
        dessertItem.productName = tempProduct.name;

        inflate();
        totalPriceTextView.setText(tempProduct.price * optionCount + "원");
        optionProductImageView.setImageURI(imageUri);
        optionReceiptMenuTextView.setText(tempProduct.name);
        optionReceiptMenuPriceTextView.setText(tempProduct.price + "원");

        StringBuilder sb = new StringBuilder();
        String temp = tempProduct.name;
        int count = 0;
        for (int i = 0; i < tempProduct.name.length(); i++) {
            if (temp.charAt(i) == ' ') {
                if (count >= 5) {
                    sb.append("\n");
                    count = 0;
                    continue;
                }
            }
            sb.append(temp.charAt(i));
            count = count + 1;
            if (count == 9) {
                sb.append("\n");
                count = 0;
            }
        }

        //optionReceiptMenuTextView.setText(sb);
        AlertDialog.Builder optionDialogBuilder = new AlertDialog.Builder(context).setView(optionDialogLayout);
        Utils.disableDialogCancel(optionDialogBuilder);
        optionDialog = optionDialogBuilder.create();

        optionDialog.getWindow().
                setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        ImageButton optionBackImageButton = optionDialogLayout.findViewById(R.id.option_back);
        optionBackImageButton.setOnClickListener(view -> {
            listener.onUserInteraction();
            showing = false;
            optionDialog.dismiss();
        });

        ImageButton optionOkButton = optionDialogLayout.findViewById(R.id.option_ok);
        optionOkButton.setOnClickListener(view -> {
            listener.onUserInteraction();
            submit2();
            showing = false;
            optionDialog.dismiss();
        });

        TextView optionCountText = optionDialogLayout.findViewById(R.id.option_count);

        ImageButton optionDecrButton = optionDialogLayout.findViewById(R.id.option_decr);
        optionDecrButton.setOnClickListener(view -> {
            listener.onUserInteraction();
            if (optionCount > 1) {
                optionCount--;
                optionCountText.setText(String.valueOf(optionCount));
                calculateDessertPrice();
            }
        });

        ImageButton optionIncrButton = optionDialogLayout.findViewById(R.id.option_incr);
        optionIncrButton.setOnClickListener(view -> {
            int currentCount = DessertDataLoader.loadCurrentStockById(dbManager, tempProduct.id);
            if (currentCount - optionCount == 0) {
                Utils.showToast(context, "남은 재고가 " + currentCount + "개 입니다.");
                return;
            }
            listener.onUserInteraction();
            if (optionCount < 5) {
                if (cartManager.getSize() - cartManager.getCount() - optionCount > 0) {
                    optionCount++;
                    optionCountText.setText(String.valueOf(optionCount));
                    calculateDessertPrice();
                }
            }
        });

        optionDialog.show();
        Window window = optionDialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.setLayout(1920, 1080);
        window.setBackgroundDrawable(Drawable.createFromPath(context.getExternalFilesDir(null) + "/팝업_옵션/배경2.png"));
        optionDialog.setCanceledOnTouchOutside(false);
        return optionDialog;
    }

    private void selectTheOnlyOption(RadioButton[] radioButtons) {
        int count = 0;
        for (RadioButton radioButton : radioButtons) {
            if (radioButton.isEnabled() && radioButton.getVisibility() == View.VISIBLE) {
                count++;
            }
        }
        if (count == 1) {
            for (RadioButton radioButton : radioButtons) {
                if (radioButton.isEnabled() && radioButton.getVisibility() == View.VISIBLE) {
                    radioButton.setChecked(true);
                }
            }
        }
    }

    private void inflate() {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        optionDialogLayout = (ConstraintLayout) vi.inflate(R.layout.dialog_dessert_option, null);
        optionReceiptMenuTextView = optionDialogLayout.findViewById(R.id.option_receipt_menu);
        UIImageLoader.loadDessertDialogImage(context, optionDialogLayout);
        optionProductImageView = optionDialogLayout.findViewById(R.id.option_product_image);
        totalPriceTextView = optionDialogLayout.findViewById(R.id.option_total_price);
        optionReceiptMenuPriceTextView = optionDialogLayout.findViewById(R.id.option_receipt_menu_price);
    }

    private void submit2(){
        dessertItem.price = tempProduct.price;
        dessertItem.number = tempProduct.number;
        String imagePath = tempImageSet.getImagePath(ImageSet.CART_IDLE);
        resultListener.onSubmit(dessertItem, imagePath, optionCount);

        showing = false;
        optionDialog.dismiss();
    }

    private void calculateTotalPrice() {
        int single = calculateSinglePrice();
        if (single == 0) {
            totalPriceTextView.setText("원");
            return;
        }
        int total = single * optionCount;
        totalPriceTextView.setText(total + "원");
    }

    private void calculateDessertPrice(){
        int tempPrice = tempProduct.price;
        int total = tempPrice * optionCount;
        totalPriceTextView.setText(total + "원");
    }

    private int calculateSinglePrice() {
        int menuPrice = Utils.parsePrice(optionReceiptMenuPriceTextView.getText());
        if (menuPrice == 0) {
            return 0;
        }
        int blendPrice = Utils.parsePrice(optionReceiptBlendPriceTextView.getText());
        int shotPrice = Utils.parsePrice(optionReceiptShotPriceTextView.getText());
        int singleTotal = menuPrice + blendPrice + shotPrice;
        return singleTotal;
    }

    public void setOnUserInteractionListener(OnUserInteractionListener listener) {
        this.listener = listener;
    }

    public void setOnResultListener(OnResultListener listener) {
        this.resultListener = listener;
    }

    public interface OnUserInteractionListener {
        void onUserInteraction();
    }

    public interface OnResultListener {
        void onSubmit(DessertItem dessertItem, String imagePath, int count);

        void onCancel();
    }
}
