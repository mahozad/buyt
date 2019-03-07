package com.pleon.buyt.ui;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.card.MaterialCardView;
import com.pleon.buyt.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class BaseViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.cardContainer) public FrameLayout cardCtn;
    @BindView(R.id.cardBackground) public MaterialCardView cardBg;
    @BindView(R.id.cardForeground) public MaterialCardView cardFg;
    @BindView(R.id.delete_icon) public ImageView delIcon;
    @BindView(R.id.circular_reveal) public View delRevealView;
    public boolean delAnimating = false;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
