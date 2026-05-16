package com.learningassistant.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.material.snackbar.Snackbar;
import com.learningassistant.app.R;
import com.learningassistant.app.adapters.UpgradeTierAdapter;
import com.learningassistant.app.models.UpgradeTier;
import com.learningassistant.app.network.ApiClient;
import com.learningassistant.app.network.ApiResponse;
import com.learningassistant.app.network.SaveUpgradeTierRequest;
import com.learningassistant.app.utils.AnimationUtils;
import com.learningassistant.app.utils.PaymentsUtil;
import com.learningassistant.app.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpgradeActivity extends AppCompatActivity {

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private SessionManager sessionManager;
    private UpgradeTierAdapter adapter;
    private PaymentsClient paymentsClient;
    private int pendingPurchasePosition = -1;
    private List<UpgradeTier> tiers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        AnimationUtils.slideInRight(this);

        sessionManager = new SessionManager(this);
        paymentsClient = PaymentsUtil.createPaymentsClient(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        buildTiers();
        setupRecyclerView();
    }

    private void buildTiers() {
        tiers = new ArrayList<>();
        tiers.add(new UpgradeTier("Starter", "Improved Quiz generation", "$4.99/month", false));
        tiers.add(new UpgradeTier("Intermediate", "Advanced AI quiz personalisation", "$9.99/month", true));
        tiers.add(new UpgradeTier("Advanced", "Full LLM study coach + analytics", "$19.99/month", false));

        String savedTier = sessionManager.getUpgradeTier();
        if (!savedTier.isEmpty()) {
            for (int i = 0; i < tiers.size(); i++) {
                if (tiers.get(i).getName().equals(savedTier)) {
                    tiers.get(i).setPurchased(true);
                    break;
                }
            }
        }
    }

    private void setupRecyclerView() {
        RecyclerView rvTiers = findViewById(R.id.rvTiers);
        rvTiers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UpgradeTierAdapter(tiers);
        adapter.setOnPurchaseClickListener((tier, position) -> {
            pendingPurchasePosition = position;
            launchGooglePay(tier);
        });
        rvTiers.setAdapter(adapter);

        rvTiers.post(() -> {
            List<View> views = new ArrayList<>();
            LinearLayoutManager lm = (LinearLayoutManager) rvTiers.getLayoutManager();
            if (lm != null) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    View child = lm.findViewByPosition(i);
                    if (child != null) views.add(child);
                }
            }
            if (!views.isEmpty()) {
                AnimationUtils.staggerFadeIn(views, 120);
            }
        });
    }

    private void launchGooglePay(UpgradeTier tier) {
        try {
            PaymentDataRequest request = PaymentDataRequest.fromJson(
                    PaymentsUtil.getPaymentDataRequest(tier.getPrice()).toString());
            Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(request);
            AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
        } catch (Exception e) {
            showSimulatedDialog(tier, pendingPurchasePosition);
        }
    }

    private void showSimulatedDialog(UpgradeTier tier, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Complete Purchase")
                .setMessage("Purchase " + tier.getName() + " plan for " + tier.getPrice()
                        + "?\n\nPayment method: Credit Card (Simulated)")
                .setPositiveButton("Confirm Purchase", (dialog, which) -> onPurchaseSuccess(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onPurchaseSuccess(int position) {
        if (position < 0 || position >= tiers.size()) return;
        UpgradeTier tier = tiers.get(position);
        sessionManager.saveUpgradeTier(tier.getName());
        adapter.markPurchased(position);
        Snackbar.make(findViewById(android.R.id.content),
                "Successfully upgraded to " + tier.getName() + "!",
                Snackbar.LENGTH_LONG).show();

        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            ApiClient.getService()
                    .saveUpgradeTier(new SaveUpgradeTierRequest(username, tier.getName()))
                    .enqueue(new Callback<ApiResponse>() {
                        @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
                        @Override public void onFailure(Call<ApiResponse> call, Throwable t) {}
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    onPurchaseSuccess(pendingPurchasePosition);
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    if (pendingPurchasePosition >= 0) {
                        showSimulatedDialog(tiers.get(pendingPurchasePosition), pendingPurchasePosition);
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.slideInLeft(this);
    }
}
