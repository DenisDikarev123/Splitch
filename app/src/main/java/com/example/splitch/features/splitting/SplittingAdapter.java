package com.example.splitch.features.splitting;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.R;
import com.example.splitch.databinding.ItemCardProductBinding;
import com.example.splitch.db.entities.Person;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.List;

public class SplittingAdapter extends RecyclerView.Adapter<SplittingAdapter.ProductCardViewHolder> {

    private static final String TAG = SplittingAdapter.class.getSimpleName();

    private List<ProductWithPersonMap> productWithPersonMapList;

    private OnChipClickListener clickListener;

    public interface OnChipClickListener{
        void onChipClicked(boolean isChecked, ProductWithPersonMap productWithPersonMap, Person person);
    }

    public SplittingAdapter(List<ProductWithPersonMap> productWithPersonMapList, OnChipClickListener clickListener) {
        this.productWithPersonMapList = productWithPersonMapList;
        this.clickListener = clickListener;
    }

    public void setProductWithPersonMapList(List<ProductWithPersonMap> productWithPersonMapList) {
        this.productWithPersonMapList = productWithPersonMapList;
    }

    @NonNull
    @Override
    public SplittingAdapter.ProductCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCardProductBinding binding = ItemCardProductBinding.inflate(inflater, parent, false);
        return new SplittingAdapter.ProductCardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SplittingAdapter.ProductCardViewHolder holder, int position) {
        holder.bind(productWithPersonMapList.get(position));
    }

    @Override
    public int getItemCount() {
        return productWithPersonMapList.size();
    }


    class ProductCardViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener {

        private ItemCardProductBinding itemBinding;

        private Context context;

        private ColorStateList textState;

        ProductCardViewHolder(ItemCardProductBinding binding) {
            super(binding.getRoot());

            this.itemBinding = binding;

            Log.i(TAG, "viewHolder constructor");

            context = itemBinding.getRoot().getContext();
            //its value equals for all persons. There is no reason to execute it multiple times.
            textState = AppCompatResources.getColorStateList(context, R.color.chip_text_color_state);
        }

        void bind(ProductWithPersonMap productWithPersonMap){
            itemBinding.textTitleCard.setText(productWithPersonMap.getProduct().name);
            itemBinding.textPriceCard.setText(String.valueOf(productWithPersonMap.getProduct().price));
            Log.i(TAG, "viewHolder bind method");
            if(itemBinding.chipGroup.getChildCount() == 0) {
                initHolder(productWithPersonMap);
            }
        }

        private void initHolder(ProductWithPersonMap productWithPersonMap){
            HashMap<Person, Boolean> personStatusMap = productWithPersonMap.getPersonStatusMap();

            for(Person person: personStatusMap.keySet()){
                int resId = getStateList(person.colorId);
                boolean isChecked = productWithPersonMap.getPersonStatusMap().get(person);
                ColorStateList backgroundState = AppCompatResources.getColorStateList(context, resId);
                Chip chip = createChip(person, backgroundState, textState, isChecked);
                itemBinding.chipGroup.addView(chip);
            }
            Log.i(TAG, "there are " + itemBinding.chipGroup.getChildCount() + " chips in a chip group");
        }

        private Chip createChip(Person person, ColorStateList backgroundState, ColorStateList textState, boolean isChecked){
            Chip chip = new Chip(itemBinding.getRoot().getContext());
            chip.setText(person.name);
            chip.setChecked(isChecked);
            chip.setChipIconVisible(true);
            chip.setChipBackgroundColor(backgroundState);
            chip.setTextColor(textState);
            chip.setOnClickListener(v -> {
                if(v instanceof Chip){
                    clickListener.onChipClicked(((Chip) v).isChecked(),
                            getCurrentProduct(), person);
                }
            });
            return chip;
        }

        private ProductWithPersonMap getCurrentProduct(){
            int position = getBindingAdapterPosition();
            return productWithPersonMapList.get(position);
        }

        private int getStateList(int colorId){
            int colorStateList;
            switch (colorId){
                case R.color.material_indigo:
                    colorStateList = R.color.indigo_500_color_state;
                    break;
                case R.color.material_light_blue:
                    colorStateList = R.color.light_blue_500_color_state;
                    break;
                case R.color.material_pink:
                    colorStateList = R.color.pink_500_color_state;
                    break;
                case R.color.material_red:
                    colorStateList = R.color.red_500_color_state;
                    break;
                case R.color.material_purple:
                    colorStateList = R.color.purple_500_color_state;
                    break;
                default:
                    throw new IllegalArgumentException("unknown color id was passed");
            }
            return colorStateList;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        }
    }

}
