package com.example.splitch.features.result;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.R;
import com.example.splitch.databinding.ItemCardPersonBinding;
import com.example.splitch.db.entities.Person;

public class ResultAdapter extends ListAdapter<Person, ResultAdapter.PersonViewHolder> {

    final private ResultAdapter.ListItemClickListener itemClickListener;

    ResultAdapter(ResultAdapter.ListItemClickListener itemClickListener) {
        super(DIFF_CALLBACK);

        this.itemClickListener = itemClickListener;
    }

    public interface ListItemClickListener{
        void onListItemDeleteClick(Person person);
    }

    @NonNull
    @Override
    public ResultAdapter.PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCardPersonBinding binding = ItemCardPersonBinding.inflate(inflater, parent, false);
        return new ResultAdapter.PersonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.PersonViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemCardPersonBinding binding;

        PersonViewHolder(ItemCardPersonBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            this.binding.getRoot().setOnClickListener(this);
        }

        void bind(Person person){
            binding.textPersonName.setText(person.name);
            binding.textPersonTotalPrice.setText(String.valueOf(person.total));
            int drawableId = getDrawable(person.colorId);
            Context context = binding.getRoot().getContext();
            Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
            binding.imageViewPersonAvatar.setBackgroundDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Person person = getItem(position);
            itemClickListener.onListItemDeleteClick(person);
        }
    }

    private int getDrawable(int colorId){
        switch (colorId){
            case R.color.material_indigo:
                return R.drawable.ic_account_circle_indigo;
            case R.color.material_light_blue:
                return R.drawable.ic_account_circle_light_blue;
            case R.color.material_pink:
                return R.drawable.ic_account_circle_pink;
            case R.color.material_red:
                return R.drawable.ic_account_circle_red;
            case R.color.material_purple:
                return R.drawable.ic_account_circle_purple;
            default:
                throw new IllegalArgumentException("incorrect color Id was passed to this function");
        }
    }

    private static final DiffUtil.ItemCallback<Person> DIFF_CALLBACK = new DiffUtil.ItemCallback<Person>() {
        @Override
        public boolean areItemsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
            return oldItem.personId.equals(newItem.personId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Person oldItem, @NonNull Person newItem) {
            return oldItem.equals(newItem);
        }
    };
}
