package com.releasetech.multidevice.AdminSettings.ProductManage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.releasetech.multidevice.AdminSettings.AdminSettingsActivity;
import com.releasetech.multidevice.Database.DBManager;
import com.releasetech.multidevice.Database.Data.Category;
import com.releasetech.multidevice.Database.Data.ImageSet;
import com.releasetech.multidevice.Database.Data.Product;
import com.releasetech.multidevice.Database.DataLoader;
//import com.releasetech.multidevice.ManagerSettings.IOViewMaster;
import com.releasetech.multidevice.Manager.PreferenceManager;
import com.releasetech.multidevice.R;
import com.releasetech.multidevice.Tool.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint({"SetTextI18n", "UseSwitchCompatOrMaterialCode", "NotifyDataSetChanged"})
@SuppressWarnings("deprecation")
public class ProductManageFragment extends Fragment {
    private static final String TAG = "[PRODUCT DESSERT MANAGE]";

    private DBManager dbManager;

    private final ArrayList<String> productCategoryNames = new ArrayList<>();
    private final ArrayList<Category> productCategories = new ArrayList<>();
    //todo 핸들러 수정
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                if (isAdded()) {
                    Handler handler = ((AdminSettingsActivity) requireActivity()).getHandler();
                    Message msg = handler.obtainMessage();
                    handler.sendMessage(msg);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean cancel() {
            return super.cancel();
        }
    };
    private ListView categoryListView;
    private final ArrayList<String> productNames = new ArrayList<>();
    private final ArrayList<Product> products = new ArrayList<>();
    private ArrayAdapter<String> productCategoriesAdapter;
    private ListView productListView;

    private final static int CATEGORY_IMAGE = 1;
    private final static int MENU_IDLE_IMAGE = 2;
    private final static int MENU_SELECTED_IMAGE = 3;
    private final static int CART_IDLE_IMAGE = 4;
    private final static int CART_SELECTED_IMAGE = 5;
    private final static int QUANTITY_IMAGE = 6;

    private static int currentCategory = -1;
    private static int currentProduct = -1;

    private ProductCache productCache;

    private LinearLayout  productLayout;
    private LinearLayout categorySettingLayout;
    private LinearLayout productSettingLayout;

    private Spinner categorySpinner;
    private ArrayAdapter<String> productsAdapter;


    private TableLayout imageSetLayout;
    private int currentSize = 0;
    private int currentShot = 0;
    //private IOViewMaster ioViewMaster;
    private boolean pauseThread = false;

    public ProductManageFragment() {
    }

    private ArrayAdapter<String> categorySpinnerAdapter;

    @Override
    public void onResume() {
        super.onResume();
        if (dbManager == null) {
            dbManager = new DBManager(requireContext());
        }
        if (!DBManager.mDB.isOpen()) {
            dbManager.open();
            dbManager.create();
        }
    }

    @Override
    public void onDestroy() {
        pauseThread = true;
        dbManager.close();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_manage, container, false);
    }

    Timer timer = new Timer(true);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = new DBManager(getContext());
        dbManager.open();
        dbManager.create();

        productCategoriesAdapter = new ArrayAdapter<>(getContext(), R.layout.listview_layout, productCategoryNames);
        productsAdapter = new ArrayAdapter<>(getContext(), R.layout.listview_layout, productNames);
        categorySpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, productCategoryNames);

        loadCategories();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        timer.schedule(timerTask, 0, 33);


        productLayout = requireView().findViewById(R.id.productLayout);

        categorySettingLayout = requireView().findViewById(R.id.category_setting_layout);
        productSettingLayout = requireView().findViewById(R.id.product_setting_layout);

        categorySpinner = requireView().findViewById(R.id.product_category);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        RecyclerView recipeView = requireView().findViewById(R.id.recipe_view);
        recipeView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        productListView = requireView().findViewById(R.id.productList);
        productListView.setAdapter(productsAdapter);

        Button addProductButton = requireView().findViewById(R.id.addProductButton);
        addProductButton.setOnClickListener(v -> addProduct());

        Button removeProductButton = requireView().findViewById(R.id.removeProductButton);
        removeProductButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("제품을 삭제하시겠습니까?").setPositiveButton("Yes", (dialogInterface, i) -> removeProduct())
                    .setNegativeButton("No", null).show();
        });

        Button saveProductButton = requireView().findViewById(R.id.btn_product_save);
        saveProductButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("제품을 저장하시겠습니까?").setPositiveButton("Yes", (dialogInterface, i) -> saveProduct())
                    .setNegativeButton("No", null).show();
        });

        productListView.setOnItemClickListener((a_parent, a_view, a_position, a_id) -> {
            currentProduct = a_position - 1;
            selectItem(productListView, a_position, R.color.phthalo_green);
            final String item = productsAdapter.getItem(a_position);
            if (item.equals("카테고리 설정")) {
                categorySettingLayout.setVisibility(View.VISIBLE);
                productSettingLayout.setVisibility(View.GONE);
            } else {
                loadProductSettings(currentProduct);
                categorySettingLayout.setVisibility(View.GONE);
                productSettingLayout.setVisibility(View.VISIBLE);
            }
        });

        categoryListView = requireView().findViewById(R.id.productCategoryList);
        categoryListView.setAdapter(productCategoriesAdapter);

        Button addCategoryButton = requireView().findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(v -> {
            if(categoryListView.getCount() <5) {
                addCategory();
            }else{
                Toast.makeText(getContext(), "디저트 카테고리는 최대 5개 까지 추가 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        });


        Button removeCategoryButton = requireView().findViewById(R.id.removeCategoryButton);
        removeCategoryButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "해당 기능 수정중입니다, 카테고리 판매 여부 off 해주세요", Toast.LENGTH_SHORT).show();
            /*
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("카테고리를 삭제하시겠습니까?").setPositiveButton("Yes", ((dialogInterface, i) -> removeCategory()))
                    .setNegativeButton("No", null).show();*/
            //show alert
        });

        categoryListView.setOnItemClickListener((a_parent, a_view, a_position, a_id) -> {
            currentCategory = a_position;
            loadCategorySettings(currentCategory);
            loadProducts();
            productLayout.setVisibility(View.VISIBLE);
            productListView.setAdapter(productsAdapter);
        });

        Button saveCategoryButton = requireView().findViewById(R.id.btn_category_save);
        saveCategoryButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("카테고리를 저장하시겠습니까?").setPositiveButton("Yes", ((dialogInterface, i) -> saveCategory()))
                    .setNegativeButton("No", null).show();
        });


        EditText nameEditText = requireView().findViewById(R.id.product_name);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 16){
                    Toast.makeText(getContext(), "제품 이름은 16자 이하로 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (productCache != null) {
                    if(editable.length() >= 16){
                        productCache.product.name = editable.toString().substring(0, 15);
                    }else {
                        productCache.product.name = editable.toString();
                    }
                }
            }
        });

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                productCache.product.category = productCategories.get(i).id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        EditText indexEditText = requireView().findViewById(R.id.product_index);
        indexEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                int value;
                try {
                    value = Integer.parseInt(text);
                    productCache.product.index = value;
                } catch (NumberFormatException ignored) {
                }
            }
        });

        EditText priceEditText = requireView().findViewById(R.id.dessert_price);
        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                int value;
                try {
                    value = Integer.parseInt(text);
                    productCache.product.price = value;
                } catch (NumberFormatException ignored) {
                }
            }
        });

        EditText numberEditText = requireView().findViewById(R.id.dessert_number);
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                int value;
                try {
                    value = Integer.parseInt(text);
                    productCache.product.number = value;
                } catch (NumberFormatException ignored) {
                }
            }
        });

        EditText totalCountEditText = requireView().findViewById(R.id.dessert_total_count);
        totalCountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                int value;
                try {
                    value = Integer.parseInt(text);
                    productCache.product.total_count = value;
                } catch (NumberFormatException ignored) {
                }
            }
        });



        Switch availableSwitch = requireView().findViewById(R.id.product_available);
        availableSwitch.setOnCheckedChangeListener((compoundButton, b) -> productCache.product.available = b ? 1 : 0);


        //Button imageSetButton = requireView().findViewById(R.id.btn_image_set);
        //imageSetButton.setOnClickListener(v -> imageSetDialog());


        ScrollView scrollView = requireActivity().findViewById(R.id.product_setting_scrollview);
    }

    private long getCategoryIdByName(String name) {
        for (Category c : productCategories) {
            if (name.equals(c.name)) return c.id;
        }
        return -1;
    }

    private int getCategoryIndexById(long id) {
        for (int i = 0; i < productCategories.size(); i++) {
            Category c = productCategories.get(i);
            if (id == c.id) return i;
        }
        return -1;
    }

    private void addCategory() {
        int count;
        count = productCategoriesAdapter.getCount();
        Category c = new Category(0, "카테고리 " + (count + 1), 1, count + 1, "");
        dbManager.insertColumn(DBManager.CATEGORY_DESSERT, c);
        loadCategories();
    }

    private void removeCategory() {
        int count;
        count = productCategoriesAdapter.getCount();
        if (count > 0) {
            if (currentCategory > -1 && currentCategory < count) {
                long id = productCategories.get(currentCategory).id;
                Utils.logD(TAG, "카테고리 삭제 : " + productCategories.get(currentCategory).name);
                DataLoader.removeCategory(dbManager, id);
                loadProducts();
                productsAdapter.notifyDataSetChanged();
                currentCategory = -1;
                categoryListView.setAdapter(productCategoriesAdapter);
                loadCategories();
                if (count == 1) {
                    productLayout.setVisibility(View.GONE);
                    categorySettingLayout.setVisibility(View.GONE);
                    productSettingLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void saveCategory() {
        if (currentCategory < 0) return;
        EditText nameEditText = requireView().findViewById(R.id.category_name);
        Switch availableSwitch = requireView().findViewById(R.id.category_available);
        EditText indexEditText = requireView().findViewById(R.id.category_index);
        ImageView image = requireView().findViewById(R.id.category_image);
        String name = nameEditText.getText().toString();
        int available = availableSwitch.isChecked() ? 1 : 0;
        int index = Integer.parseInt(indexEditText.getText().toString());
        String imagePath = image.getTag().toString();
        long id = productCategories.get(currentCategory).id;
        Category c = new Category(id, name, available, index, imagePath);
        dbManager.updateColumn(DBManager.CATEGORY_DESSERT, c);
        loadCategories();
    }


    private void loadCategories() {
        productCategories.clear();
        productCategoryNames.clear();


        for (Category category : Objects.requireNonNull(DataLoader.loadCategories(dbManager))) {
            if(category != null) {
                productCategoryNames.add(category.name);
                productCategories.add(category);
            }
        }
        categorySpinnerAdapter.notifyDataSetChanged();
        productCategoriesAdapter.notifyDataSetChanged();
    }

    private void loadCategorySettings(int i) {
        Category c = productCategories.get(i);
        EditText nameEditText = requireView().findViewById(R.id.category_name);
        Switch availableSwitch = requireView().findViewById(R.id.category_available);
        EditText indexEditText = requireView().findViewById(R.id.category_index);
        ImageView image = requireView().findViewById(R.id.category_image);
        nameEditText.setText(c.name);
        availableSwitch.setChecked(c.available > 0);
        indexEditText.setText("" + c.index);
        image.setTag(c.image);
        Uri imagePath = Uri.parse(c.image);
        image.setImageURI(imagePath);
    }

    private void addProduct() {
        int count;
        count = productsAdapter.getCount();
        long category = productCategories.get(currentCategory).id;
        /*
        for (int j = 0; j < 4; j++) {
            long[] recipeIDs = new long[4];
            for (int i = 0; i < 4; i++) {
                Recipe recipe = new Recipe();
                recipeIDs[i] = dbManager.insertColumn(DBManager.RECIPE, recipe);
            }
        }*/
        ImageSet is = new ImageSet();

        long imageSetID = dbManager.insertColumn(DBManager.PRODUCT_IMAGE, is);

        Product p = new Product.ProductBuilder(0, "제품" + count, category, count)
                .setAvailable(1)
                .setPrice(0)
                .setNumber(0)
                .setTotalCount(0)
                .setImageSet(imageSetID)
                .build();
        dbManager.insertColumn(DBManager.PRODUCT_DESSERT, p);
        loadProducts();
        loadProductSettings(count - 2);
    }

    private void removeProduct() {
        int count;
        count = productsAdapter.getCount() - 1;

        if (count > 0) {
            if (currentProduct > -1 && currentProduct < count) {
                Utils.logD(TAG, "제품 삭제 : " + products.get(currentProduct).name);
                long id = products.get(currentProduct).id;
                DataLoader.removeProduct(dbManager, id);
                productListView.setAdapter(productsAdapter);
                loadProducts();
                count = productsAdapter.getCount() - 1;
                if (currentProduct == count) {
                    currentProduct--;
                }
                productListView.performItemClick(
                        productListView.getChildAt(currentProduct + 1),
                        currentProduct + 1,
                        productListView.getAdapter().getItemId(currentProduct + 1));
            }
        }
    }

    public void selectItem(ListView listview, int pos, int resId) {
        int size = listview.getAdapter().getCount();
        for (int i = 0; i < size; i++) {
            TextView selectedItem = (TextView) getViewByPosition(i, listview);
            selectedItem.setBackgroundResource(R.color.white);
            selectedItem.setTextColor(requireActivity().getResources().getColor(R.color.black));
        }
        TextView selectedItem = (TextView) getViewByPosition(pos, listview);
        selectedItem.setBackgroundResource(resId);
        selectedItem.setTextColor(requireActivity().getResources().getColor(R.color.white));
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void saveProduct() {
        dbManager.updateColumn(DBManager.PRODUCT_DESSERT, productCache.product);
        //productCache.sizeRecipePackCache.shotRecipePackCache[currentSize].recipe[currentShot].setFromArrayList(currentRecipe);
        loadProducts();
        TextView totalCount = requireActivity().findViewById(R.id.dessert_total_count);
        TextView number = requireActivity().findViewById(R.id.dessert_number);

        while (true) {
            try {
                loadProductSettings(currentProduct);
                break;
            } catch (NullPointerException e) {
                currentProduct--;
                if (currentProduct == 0) {
                    loadCategorySettings(currentCategory);
                    productLayout.setVisibility(View.GONE);
                    categorySettingLayout.setVisibility(View.GONE);
                    productSettingLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadProducts() {
        productNames.clear();
        productNames.add("카테고리 설정");
        long currentCategoryId = productCategories.get(currentCategory).id;
        products.clear();
        products.addAll(DataLoader.loadProductsByCategoryId(dbManager, currentCategoryId, false));
        for (Product product : products) {
            productNames.add(product.name);
        }
        productsAdapter.notifyDataSetChanged();
    }

    private void loadProductSettings(int productIndex) {
        if (productIndex < 0 || productIndex >= products.size()) return;
        Product p = products.get(productIndex);
        productCache = new ProductCache(p);

        EditText nameEditText = requireView().findViewById(R.id.product_name);
        nameEditText.setText(p.name);

        categorySpinner.setSelection(getCategoryIndexById(p.category));

        EditText indexEditText = requireView().findViewById(R.id.product_index);
        indexEditText.setText("" + p.index);

        Switch availableSwitch = requireView().findViewById(R.id.product_available);
        availableSwitch.setChecked(p.available > 0);

        EditText priceText = requireView().findViewById(R.id.dessert_price);
        priceText.setText("" + p.price);

        EditText totalCountText = requireView().findViewById(R.id.dessert_total_count);
        totalCountText.setText("" + p.total_count);

        EditText numberText = requireView().findViewById(R.id.dessert_number);
        numberText.setText("" + p.number);

    }

    private void imageSetDialog() {
        LayoutInflater vi = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageSetLayout = (TableLayout) vi.inflate(R.layout.dialog_image_set, null);
        AlertDialog imageSetDialog = new AlertDialog.Builder(getActivity()).setTitle("이미지를 선택하세요").setView(imageSetLayout)
                .setPositiveButton("저장", (dialog, which) -> saveImageSet())
                .setNeutralButton("취소", (dialog, which) -> dialog.dismiss()).show();
        loadImageSet();
        imageSetDialog.setCanceledOnTouchOutside(false);

        int[][] buttons = {{R.id.btn_menu_idle, MENU_IDLE_IMAGE},
                {R.id.btn_menu_selected, MENU_SELECTED_IMAGE},
                {R.id.btn_cart_idle, CART_IDLE_IMAGE},
                {R.id.btn_cart_selected, CART_SELECTED_IMAGE},
                {R.id.btn_quantity, QUANTITY_IMAGE}};
        for (int[] i : buttons) {
            Button button = imageSetLayout.findViewById(i[0]);
            button.setOnClickListener(v -> {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요"), i[1]);

            });
        }
    }

    private void saveImageSet() {

        ImageSet is = new ImageSet();
        ImageView imageView;
        ImageSet prevIs = DataLoader.loadImageSet(dbManager, productCache.product.image_set);

        is.id = productCache.product.image_set;
        imageView = imageSetLayout.findViewById(R.id.image_menu_idle);
        is.menu_idle = imageView.getTag().toString();
        imageView = imageSetLayout.findViewById(R.id.image_menu_selected);
        is.menu_selected = imageView.getTag().toString();
        imageView = imageSetLayout.findViewById(R.id.image_cart_idle);
        is.cart_idle = imageView.getTag().toString();
        imageView = imageSetLayout.findViewById(R.id.image_cart_selected);
        is.cart_selected = imageView.getTag().toString();
        imageView = imageSetLayout.findViewById(R.id.image_quantity);
        is.quantity = imageView.getTag().toString();
        if (prevIs != null) {
            if (!Objects.equals(prevIs.menu_idle, is.menu_idle)) {
                File tempImage = new File(prevIs.menu_idle);
                if (tempImage.exists()) tempImage.delete();
            }
            if (!Objects.equals(prevIs.menu_selected, is.menu_selected)) {
                File tempImage = new File(prevIs.menu_selected);
                if (tempImage.exists()) tempImage.delete();
            }
            if (!Objects.equals(prevIs.cart_idle, is.cart_idle)) {
                File tempImage = new File(prevIs.cart_idle);
                if (tempImage.exists()) tempImage.delete();
            }
            if (!Objects.equals(prevIs.cart_selected, is.cart_selected)) {
                File tempImage = new File(prevIs.cart_selected);
                if (tempImage.exists()) tempImage.delete();
            }
            if (!Objects.equals(prevIs.quantity, is.quantity)) {
                File tempImage = new File(prevIs.quantity);
                if (tempImage.exists()) tempImage.delete();
            }
        }
        dbManager.updateColumn(DBManager.PRODUCT_IMAGE, is);
    }


    private void loadImageSet() {
        int[] ids = {R.id.image_menu_idle, R.id.image_menu_selected, R.id.image_cart_idle, R.id.image_cart_selected, R.id.image_quantity};
        ImageSet tempImageSet = DataLoader.loadImageSet(dbManager, productCache.product.image_set);
        for (int i = 0; i < 5; i++) {
            int id = ids[i];
            ImageView image = imageSetLayout.findViewById(id);
            String stringUri = tempImageSet.getImagePath(i);
            image.setTag(stringUri);
            image.setImageURI(Uri.parse(stringUri));
        }
        productsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage;
            ImageView image;
            selectedImage = data.getData();
            try {
                Bitmap bm = BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(selectedImage), null, null);
                bm = Bitmap.createBitmap(bm);
                String internalImageName = Utils.md5(selectedImage.toString());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.WEBP, 100, bytes);
                FileOutputStream internalImage = requireActivity().openFileOutput(internalImageName, Context.MODE_PRIVATE);
                internalImage.write(bytes.toByteArray());
                internalImage.close();
                Uri test = Uri.fromFile(requireActivity().getFileStreamPath(internalImageName));
                switch (requestCode) {
                    case CATEGORY_IMAGE:
                        image = requireView().findViewById(R.id.category_image);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                    case MENU_IDLE_IMAGE:
                        image = imageSetLayout.findViewById(R.id.image_menu_idle);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                    case MENU_SELECTED_IMAGE:
                        image = imageSetLayout.findViewById(R.id.image_menu_selected);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                    case CART_IDLE_IMAGE:
                        image = imageSetLayout.findViewById(R.id.image_cart_idle);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                    case CART_SELECTED_IMAGE:
                        image = imageSetLayout.findViewById(R.id.image_cart_selected);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                    case QUANTITY_IMAGE:
                        image = imageSetLayout.findViewById(R.id.image_quantity);
                        image.setTag(test);
                        image.setImageBitmap(bm);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (productSettingLayout.getVisibility() == View.VISIBLE) {
                productSettingLayout.setVisibility(View.GONE);
            } else requireActivity().onBackPressed();
            return true;
        }
        return false;
    }
}