package com.learningassistant.app.adapters;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.learningassistant.app.R;
import com.learningassistant.app.models.UpgradeTier;
import java.util.List;

public class UpgradeTierAdapter extends RecyclerView.Adapter<UpgradeTierAdapter.VH> {

    public interface OnPurchaseClickListener {
        void onPurchaseClick(UpgradeTier tier, int position);
    }

    private final List<UpgradeTier> tiers;
    private OnPurchaseClickListener listener;

    public UpgradeTierAdapter(List<UpgradeTier> tiers) {
        this.tiers = tiers;
    }

    public void setOnPurchaseClickListener(OnPurchaseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upgrade_tier, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UpgradeTier tier = tiers.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvTierName.setText(tier.getName());
        holder.tvTierDescription.setText(tier.getDescription());
        holder.tvTierPrice.setText(tier.getPrice());

        if (tier.isBestSeller()) {
            holder.tvBestSeller.setVisibility(View.VISIBLE);
            startPulseAnimation(holder.tvBestSeller);
        } else {
            holder.tvBestSeller.setVisibility(View.GONE);
        }

        if (tier.isPurchased()) {
            holder.btnPurchase.setText("Current Plan ✓");
            holder.btnPurchase.setEnabled(false);
            holder.materialCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.colorSuccess));
            holder.materialCard.setStrokeWidth(4);
        } else {
            holder.btnPurchase.setText("Purchase");
            holder.btnPurchase.setEnabled(true);
            holder.materialCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.colorDivider));
            holder.materialCard.setStrokeWidth(2);
        }

        holder.btnPurchase.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && listener != null) {
                listener.onPurchaseClick(tiers.get(pos), pos);
            }
        });
    }

    public void markPurchased(int position) {
        for (int i = 0; i < tiers.size(); i++) {
            tiers.get(i).setPurchased(i == position);
        }
        notifyDataSetChanged();
    }

    private void startPulseAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.05f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.05f);
        scaleX.setDuration(700);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setDuration(700);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    @Override
    public int getItemCount() {
        return tiers != null ? tiers.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView materialCard;
        TextView tvTierName, tvTierDescription, tvTierPrice, tvBestSeller;
        Button btnPurchase;

        VH(@NonNull View itemView) {
            super(itemView);
            materialCard = itemView.findViewById(R.id.materialCard);
            tvTierName = itemView.findViewById(R.id.tvTierName);
            tvTierDescription = itemView.findViewById(R.id.tvTierDescription);
            tvTierPrice = itemView.findViewById(R.id.tvTierPrice);
            tvBestSeller = itemView.findViewById(R.id.tvBestSeller);
            btnPurchase = itemView.findViewById(R.id.btnPurchase);
        }
    }
}
