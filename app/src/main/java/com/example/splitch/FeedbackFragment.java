package com.example.splitch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class FeedbackFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.feedback_screen, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);

        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        toolbar.setTitle(getString(R.string.title_send_feedback));
        toolbar.setNavigationIcon(R.drawable.ic_clear_24dp);

        Preference feedbackPreference = findPreference(getString(R.string.pref_feedback_key));
        feedbackPreference.setOnPreferenceClickListener(preference -> {
            sendFeedback();
            return true;
        });
    }

    private void sendFeedback() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_address) });
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        startActivity(Intent.createChooser(intent, getString(R.string.title_send_feedback)));
    }
}
