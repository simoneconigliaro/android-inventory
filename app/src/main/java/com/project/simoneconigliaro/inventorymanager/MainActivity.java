package com.project.simoneconigliaro.inventorymanager;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_PRODUCT_REQUEST = 1;
    public static final String EXTRA_ID = "com.project.simoneconigliaro.inventorymanager.EXTRA_ID";

    private ProductViewModel mProductViewModel;
    private RecyclerView mRecyclerView;
    private ProductAdapter mProductAdapter;
    private LinearLayout linearLayoutForEmptyList;

    private FloatingActionButton addProductButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayoutForEmptyList = findViewById(R.id.linear_layout_empty_list);

        addProductButton = findViewById(R.id.button_add_product);
        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditProductActivity.class);
                startActivityForResult(intent, ADD_PRODUCT_REQUEST);
            }
        });

        mProductViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);

        mProductAdapter = new ProductAdapter(mProductViewModel);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mProductAdapter);

        mProductViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(@Nullable List<Product> products) {
                mProductAdapter.setProducts(products);
                if (products.isEmpty()) {
                    linearLayoutForEmptyList.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutForEmptyList.setVisibility(View.INVISIBLE);
                }
            }
        });

        mProductAdapter.setOnClickHandler(new ProductAdapter.OnClickHandler() {
            @Override
            public void onItemClick(Product product) {
                Intent data = new Intent(MainActivity.this, AddEditProductActivity.class);
                data.putExtra(EXTRA_ID, product.getId());
                startActivity(data);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            case R.id.action_delete_all_products:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mProductViewModel.deleteAllProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void insertDummyData() {
        String supplierName = "Hand tools and more";
        String supplierPhone = "+44 123456789";
        String supplierEmail = "info@handtoolsandmore.com";

        Bitmap hammerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hammer);
        byte[] hammerImage = ImageUtil.bitmapToByteArray(hammerBitmap);

        Product hammer = new Product(
                "Hammer",
                "20",
                50,
                supplierName,
                supplierPhone,
                supplierEmail,
                hammerImage);

        mProductViewModel.insert(hammer);

        Bitmap screwdriverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.screwdriver);
        byte[] screwdriverImage = ImageUtil.bitmapToByteArray(screwdriverBitmap);

        Product screwdriver = new Product(
                "Screwdriver",
                "8",
                80,
                supplierName,
                supplierPhone,
                supplierEmail,
                screwdriverImage);

        mProductViewModel.insert(screwdriver);

        Bitmap pliersBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pliers);
        byte[] pliersImage = ImageUtil.bitmapToByteArray(pliersBitmap);

        Product pliers = new Product(
                "Pliers",
                "12",
                70,
                supplierName,
                supplierPhone,
                supplierEmail,
                pliersImage);

        mProductViewModel.insert(pliers);

        Bitmap drillBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drill);
        byte[] drillImage = ImageUtil.bitmapToByteArray(drillBitmap);

        Product drill = new Product(
                "Drill",
                "60",
                40,
                supplierName,
                supplierPhone,
                supplierEmail,
                drillImage);

        mProductViewModel.insert(drill);
    }
}
