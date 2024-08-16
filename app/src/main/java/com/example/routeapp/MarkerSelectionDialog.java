package com.example.routeapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class MarkerSelectionDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.marker_selection_dialog, null);

        ImageView mushroomsMarker = view.findViewById(R.id.mushrooms_marker);
        ImageView berriesMarker = view.findViewById(R.id.berries_marker);
        ImageView campingMarker = view.findViewById(R.id.camping_marker);

        mushroomsMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        berriesMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        campingMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        builder.setView(view)
                .setTitle("Select Marker")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
