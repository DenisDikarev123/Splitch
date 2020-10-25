package com.example.splitch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AbstractSavedStateViewModelFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.savedstate.SavedStateRegistryOwner;

import com.example.splitch.db.MainDao;
import com.example.splitch.features.adding_persons.AddingPersonsViewModel;
import com.example.splitch.features.receipt.AddingReceiptViewModel;
import com.example.splitch.features.result.ResultViewModel;
import com.example.splitch.features.splitting.SplittingViewModel;

public class ViewModelsFactory extends AbstractSavedStateViewModelFactory {

    private MainDao mainDao;

    /**
     * Constructs this factory.
     *
     * @param owner       {@link SavedStateRegistryOwner} that will provide restored state for created
     *                    {@link ViewModel ViewModels}
     * @param defaultArgs values from this {@code Bundle} will be used as defaults by
     *                    {@link SavedStateHandle} passed in {@link ViewModel ViewModels}
     *                    if there is no previously saved state
     */
    public ViewModelsFactory(@NonNull SavedStateRegistryOwner owner, @Nullable Bundle defaultArgs, MainDao mainDao) {
        super(owner, defaultArgs);

        this.mainDao = mainDao;
    }

    @NonNull
    @Override
    protected <T extends ViewModel> T create(@NonNull String key, @NonNull Class<T> modelClass, @NonNull SavedStateHandle handle) {
        if(modelClass.isAssignableFrom(AddingReceiptViewModel.class)){
            return (T) new AddingReceiptViewModel(mainDao, handle);
        } else if(modelClass.isAssignableFrom(AddingPersonsViewModel.class)){
            return (T) new AddingPersonsViewModel(mainDao, handle);
        } else if (modelClass.isAssignableFrom(SplittingViewModel.class)){
            return (T) new SplittingViewModel(mainDao);
        } else if (modelClass.isAssignableFrom(ResultViewModel.class)){
            return (T) new ResultViewModel(mainDao, handle);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
