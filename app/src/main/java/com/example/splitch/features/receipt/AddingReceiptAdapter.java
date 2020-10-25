package com.example.splitch.features.receipt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.R;
import com.example.splitch.databinding.ItemReceiptBinding;
import com.example.splitch.db.entities.Product;

import java.text.DecimalFormat;


public class AddingReceiptAdapter extends PagedListAdapter<Product, AddingReceiptAdapter.ProductViewHolder> {

    final private ListItemClickListener itemClickListener;

    AddingReceiptAdapter(ListItemClickListener itemClickListener) {
        super(DIFF_CALLBACK);
        this.itemClickListener = itemClickListener;
    }

    public interface ListItemClickListener{
        void onListItemDeleteClick(Product product);
        void onListItemEditClick(Product product);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemReceiptBinding binding = ItemReceiptBinding.inflate(inflater, parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProductViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

        private ItemReceiptBinding itemBinding;

        ProductViewHolder(ItemReceiptBinding binding) {
            super(binding.getRoot());

            this.itemBinding = binding;

            this.itemBinding.buttonIconDelete.setOnClickListener(this);
            this.itemBinding.buttonIconEdit.setOnClickListener(this);
        }

        void bind(Product product){
            itemBinding.textNameField.setText(product.name);
            DecimalFormat format = new DecimalFormat("0.00");

            String stringPrice = format.format(product.price);
            itemBinding.textPriceField.setText(stringPrice);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_icon_delete:
                    itemClickListener.onListItemDeleteClick(getCurrentProduct());
                    break;
                case R.id.button_icon_edit:
                    itemClickListener.onListItemEditClick(getCurrentProduct());
                    break;
                default:
                    break;
            }
        }

        private Product getCurrentProduct(){
            int position = getBindingAdapterPosition();
            return getItem(position);
        }
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Product>() {
        //this methods are used to check whether new item have changed since last check
        // to decide to redraw or not
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.productId.equals(newItem.productId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldItem.equals(newItem);
        }
    };
}
