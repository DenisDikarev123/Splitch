package com.example.splitch.features.adding_persons;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitch.R;
import com.example.splitch.databinding.ItemPersonBinding;
import com.example.splitch.db.entities.Person;

public class AddingPersonsAdapter extends PagedListAdapter<Person, AddingPersonsAdapter.PersonViewHolder> {
    private static final String TAG = AddingPersonsAdapter.class.getSimpleName();

    final private ListItemClickListener itemClickListener;

    AddingPersonsAdapter(ListItemClickListener itemClickListener) {
        super(DIFF_CALLBACK);

        this.itemClickListener = itemClickListener;
    }

    public interface ListItemClickListener{
        void onListItemDeleteClick(Person person);
        void onListItemEditClick(Person person);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPersonBinding binding = ItemPersonBinding.inflate(inflater, parent, false);
        return new PersonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddingPersonsAdapter.PersonViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

     class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ItemPersonBinding binding;

        PersonViewHolder(ItemPersonBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            this.binding.buttonIconPersonDelete.setOnClickListener(this);
            this.binding.buttonIconPersonEdit.setOnClickListener(this);
        }

        void bind(Person person){
            binding.textPersonName.setText(person.name);
            int drawableId = getDrawable(person.colorId);
            Context context = binding.getRoot().getContext();
            Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
            binding.imagePersonAvatar.setBackgroundDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_icon_person_delete){
                Person person = getCurrentPerson();
                itemClickListener.onListItemDeleteClick(person);
            } else if(v.getId() == R.id.button_icon_person_edit){
                Person person = getCurrentPerson();
                itemClickListener.onListItemEditClick(person);
            }
        }

         private Person getCurrentPerson(){
             int position = getBindingAdapterPosition();
             return getItem(position);
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
