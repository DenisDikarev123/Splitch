package com.example.splitch;

import android.content.Context;

import androidx.savedstate.SavedStateRegistryOwner;

import com.example.splitch.db.MainDatabase;

public class Injection {

    public static ViewModelsFactory provideViewModelFactory(SavedStateRegistryOwner owner, Context context) {
        MainDatabase database = MainDatabase.getInstance(context);
        return new ViewModelsFactory(owner, null, database.mainDao());
    }

}
