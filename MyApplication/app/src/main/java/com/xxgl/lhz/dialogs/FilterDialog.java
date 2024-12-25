package com.xxgl.lhz.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FilterDialog extends DialogFragment {
    private static final String ARG_CURRENT_FILTER = "current_filter";
    private OnFilterAppliedListener listener;
    private int titleId;
    private int itemsId;
    public interface OnFilterAppliedListener {
        void onFilterApplied(int filter);
    }

    private FilterDialog(int titleId, int itemsIdint){
            this.titleId = titleId;
            this.itemsId = itemsIdint;
    }

    public static FilterDialog newInstance(int titleId , int itemsIdint, int currentFilter) {
        FilterDialog dialog = new FilterDialog(titleId, itemsIdint);
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_FILTER, currentFilter);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int currentFilter = getArguments().getInt(ARG_CURRENT_FILTER, 0);

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleId)
                .setSingleChoiceItems(itemsId, currentFilter, (dialog, which) -> {
                    if (listener != null) {
                        listener.onFilterApplied(which);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}