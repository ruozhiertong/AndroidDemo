package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.activities.LoginActivity;
//import com.example.myapplication.activities.SettingsActivity;
import com.example.myapplication.activities.SettingsActivity;
import com.example.myapplication.dao.UserDAO;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.models.User;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {
    private static final String PREF_NAME = "CarRentalPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";


    private TextView tvUsername;
    private TextView tvRole;
    private TextView tvRealName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvLastLogin;
    private MaterialCardView cardSettings;
    private MaterialCardView cardHelp;
    private MaterialCardView cardAbout;
    private MaterialCardView cardLogout;

    private UserDAO userDAO;
    private User currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getActivity());
        userDAO = new UserDAO(dbHelper);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(R.string.profile);

        initializeViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvUsername = view.findViewById(R.id.tv_username);
        tvRole = view.findViewById(R.id.tv_role);
        tvRealName = view.findViewById(R.id.tv_real_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvLastLogin = view.findViewById(R.id.tv_last_login);
        cardSettings = view.findViewById(R.id.card_settings);
        cardHelp = view.findViewById(R.id.card_help);
        cardAbout = view.findViewById(R.id.card_about);
        cardLogout = view.findViewById(R.id.card_logout);
    }

    private void loadUserData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        System.out.println("loadUserData" + userId);
        if (userId != -1) {
            currentUser = userDAO.getUser(userId);
            System.out.println("loadUserData:"  + currentUser.getPassword());
            if (currentUser != null) {
                updateUI();
            }
        }
    }

    private void updateUI() {
        tvUsername.setText(currentUser.getUsername());
        tvRole.setText(currentUser.getRole());
        tvRealName.setText(currentUser.getRealName());
        tvEmail.setText(currentUser.getEmail());
        tvPhone.setText(currentUser.getPhone());
        tvLastLogin.setText(currentUser.getLastLogin());
    }

    private void setupClickListeners() {
        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        cardHelp.setOnClickListener(v -> showHelpDialog());
        cardAbout.setOnClickListener(v -> showAboutDialog());
        cardLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.help_center)
                .setMessage(R.string.help_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.about)
                .setMessage(R.string.about_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Return to login screen
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        System.out.println("Profile on Resume");
        super.onResume();
        loadUserData();
    }
}