package com.project.simoneconigliaro.inventorymanager;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String INCREASE_QUANTITY = "increase_quantity";
    public static final String DECREASE_QUANTITY = "decrease_quantity";
    public static final int IMAGE_GALLERY_REQUEST_CODE = 1;
    public static final String IMAGE_KEY = "image_key";

    private ProductViewModel mProductViewModel;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private EditText supplierEmailEditText;
    private ImageView productImageView;

    private boolean isEditMode = false;
    private Product currentProduct;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        nameEditText = findViewById(R.id.edit_text_name);
        priceEditText = findViewById(R.id.edit_text_price);
        quantityEditText = findViewById(R.id.edit_text_quantity);
        supplierNameEditText = findViewById(R.id.edit_text_supplier_name);
        supplierPhoneEditText = findViewById(R.id.edit_text_supplier_phone);
        supplierEmailEditText = findViewById(R.id.edit_text_supplier_email);
        productImageView = findViewById(R.id.image_view_product);
        ImageButton increaseQuantityButton = findViewById(R.id.image_button_increase_quantity);
        ImageButton decreaseQuantityButton = findViewById(R.id.image_button_decrease_quantity);
        Button selectImageButton = findViewById(R.id.button_select_image);

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeQuantity(INCREASE_QUANTITY);
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeQuantity(DECREASE_QUANTITY);
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_GALLERY_REQUEST_CODE);
            }
        });

        mProductViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);

        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.EXTRA_ID)) {
            setTitle(getText(R.string.edit_product));
            isEditMode = true;
            int id = intent.getIntExtra(MainActivity.EXTRA_ID, -1);

            mProductViewModel.getProductById(id).observe(this, new Observer<Product>() {
                @Override
                public void onChanged(@Nullable Product product) {
                    if (product != null) {
                        currentProduct = product;
                        nameEditText.setText(currentProduct.getProductName());
                        priceEditText.setText(currentProduct.getPrice());
                        quantityEditText.setText(String.valueOf(currentProduct.getQuantity()));
                        supplierNameEditText.setText(currentProduct.getSupplierName());
                        supplierPhoneEditText.setText(currentProduct.getSupplierPhone());
                        supplierEmailEditText.setText(currentProduct.getSupplierEmail());

                        if (savedInstanceState != null) {
                            byte[] imageByteArray = savedInstanceState.getByteArray(IMAGE_KEY);
                            setProductImageView(imageByteArray);
                        } else {
                            setProductImageView(currentProduct.getImage());
                        }
                    }
                }
            });
        } else {
            setTitle(getText(R.string.add_product));
            if (savedInstanceState != null) {
                byte[] imageByteArray = savedInstanceState.getByteArray(IMAGE_KEY);
                setProductImageView(imageByteArray);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (productImageView.getDrawable() != null) {
            byte[] imageByteArray = ImageUtil.drawableToByteArray(productImageView.getDrawable());
            outState.putByteArray(IMAGE_KEY, imageByteArray);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentProduct == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            MenuItem menuItemOrderMore = menu.findItem(R.id.action_order_more);
            menuItemDelete.setVisible(false);
            menuItemOrderMore.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_order_more:
                orderMoreProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (isEditMode && hasProductChanged()) {
                    showUnsavedChangesDialog();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(AddEditProductActivity.this);
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isEditMode && hasProductChanged()) {
            showUnsavedChangesDialog();

        } else {
            super.onBackPressed();
            return;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                productImageView.setImageBitmap(bitmap);
                productImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveProduct() {
        String name = nameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();
        String supplierEmail = supplierEmailEditText.getText().toString().trim();
        Drawable productImageDrawable = productImageView.getDrawable();

        if (name.trim().isEmpty() || price.trim().isEmpty() || quantity.trim().isEmpty() ||
                supplierName.trim().isEmpty() || supplierPhone.trim().isEmpty() || supplierEmail.trim().isEmpty() || productImageDrawable == null) {
            Toast.makeText(this, getText(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] image = ImageUtil.drawableToByteArray(productImageDrawable);
        Product product = new Product(name, price, Integer.valueOf(quantity), supplierName, supplierPhone, supplierEmail, image);
        if (isEditMode) {
            int id = getIntent().getIntExtra(MainActivity.EXTRA_ID, -1);
            product.setId(id);
            mProductViewModel.update(product);
        } else {
            mProductViewModel.insert(product);
        }
        finish();
    }

    public void orderMoreProduct() {
        String subject = getText(R.string.new_order).toString();
        String message = getText(R.string.begin_order_message) + nameEditText.getText().toString() + getText(R.string.end_order_message);
        String email = supplierEmailEditText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void changeQuantity(String operation) {
        if (TextUtils.isEmpty(quantityEditText.getText().toString())) {
            quantityEditText.setText(getText(R.string.zero));
        }
        int value = Integer.valueOf(quantityEditText.getText().toString());
        switch (operation) {
            case INCREASE_QUANTITY:
                value++;
                break;
            case DECREASE_QUANTITY:
                if (value > 0) {
                    value--;
                }
                break;
        }
        quantityEditText.setText(String.valueOf(value));
    }

    public void setProductImageView(byte[] imageByteArray) {
        Bitmap bitmap = ImageUtil.byteArrayToBitmap(imageByteArray);
        productImageView.setImageBitmap(bitmap);
        productImageView.setVisibility(View.VISIBLE);
    }

    private void showUnsavedChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mProductViewModel.delete(currentProduct);
                finish();
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

    private boolean hasProductChanged() {
        String name = nameEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        int quantity = Integer.valueOf(quantityEditText.getText().toString().trim());
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();
        String supplierEmail = supplierEmailEditText.getText().toString().trim();
        byte[] imageProduct = ImageUtil.drawableToByteArray(productImageView.getDrawable());

        if (TextUtils.equals(name, currentProduct.getProductName()) && TextUtils.equals(price, currentProduct.getPrice()) &&
                quantity == currentProduct.getQuantity() && TextUtils.equals(supplierName, currentProduct.getSupplierName()) &&
                TextUtils.equals(supplierPhone, currentProduct.getSupplierPhone()) && TextUtils.equals(supplierEmail, currentProduct.getSupplierEmail()) && Arrays.equals(imageProduct, currentProduct.getImage())) {
            return false;
        } else {
            return true;
        }
    }
}
