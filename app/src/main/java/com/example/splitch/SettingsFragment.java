package com.example.splitch;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_screen, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        toolbar.setTitle(getString(R.string.title_settings));
        toolbar.setNavigationIcon(R.drawable.ic_clear_24dp);

        SwitchPreference darkThemeSwitch = findPreference(getString(R.string.key_settings_theme));
        darkThemeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            if((boolean) newValue){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            return true;
        });
    }
}
