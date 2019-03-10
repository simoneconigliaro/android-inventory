package com.project.simoneconigliaro.inventorymanager;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    List<Product> mProducts;

    OnClickHandler onClickHandler;
    ProductViewModel mProductViewModel;

    public ProductAdapter(ProductViewModel productViewModel) {
        this.mProductViewModel = productViewModel;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {
        holder.nameTextView.setText(mProducts.get(position).getProductName());
        holder.quantityTextView.setText(String.valueOf(mProducts.get(position).getQuantity()));
        holder.priceTextView.setText("£ " + mProducts.get(position).getPrice());
        Bitmap bitmap = ImageUtil.byteArrayToBitmap(mProducts.get(position).getImage());
        holder.productImageView.setImageBitmap(bitmap);

        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = mProducts.get(position).getQuantity();
                if (quantity > 0) {
                    quantity--;
                    Product product = mProducts.get(position);
                    product.setQuantity(quantity);
                    mProductViewModel.update(product);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mProducts != null) {
            return mProducts.size();
        } else return 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView quantityTextView;
        private TextView priceTextView;
        private ImageView productImageView;
        private ImageButton saleButton;


        public ProductViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            quantityTextView = itemView.findViewById(R.id.text_view_quantity);
            priceTextView = itemView.findViewById(R.id.text_view_price);
            productImageView = itemView.findViewById(R.id.image_view_product_small);
            saleButton = itemView.findViewById(R.id.button_sale);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onClickHandler != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        int position = getAdapterPosition();
                        onClickHandler.onItemClick(mProducts.get(position));
                    }
                }
            });
        }
    }

    public void setProducts(List<Product> products) {
        mProducts = products;
        notifyDataSetChanged();
    }

    interface OnClickHandler {
        void onItemClick(Product product);
    }

    public void setOnClickHandler(OnClickHandler onClickHandler) {
        this.onClickHandler = onClickHandler;
    }

}
