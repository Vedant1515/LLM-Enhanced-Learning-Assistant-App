package com.learningassistant.app.utils;

import android.app.Activity;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentsUtil {

    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    public static JSONObject isReadyToPayRequest() throws JSONException {
        JSONObject request = new JSONObject();
        request.put("apiVersion", 2);
        request.put("apiVersionMinor", 0);
        // false = show Google Pay even if no card is saved yet (lets user add one in the sheet)
        request.put("existingPaymentMethodRequired", false);

        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedCardNetworks", new JSONArray()
                .put("VISA").put("MASTERCARD").put("AMEX"));
        parameters.put("allowedAuthMethods", new JSONArray()
                .put("PAN_ONLY").put("CRYPTOGRAM_3DS"));
        cardPaymentMethod.put("parameters", parameters);

        request.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));
        return request;
    }

    public static JSONObject getPaymentDataRequest(String price) throws JSONException {
        JSONObject request = new JSONObject();
        request.put("apiVersion", 2);
        request.put("apiVersionMinor", 0);

        JSONObject cardPaymentMethod = new JSONObject();
        cardPaymentMethod.put("type", "CARD");

        JSONObject parameters = new JSONObject();
        parameters.put("allowedCardNetworks", new JSONArray()
                .put("VISA").put("MASTERCARD").put("AMEX"));
        parameters.put("allowedAuthMethods", new JSONArray()
                .put("PAN_ONLY").put("CRYPTOGRAM_3DS"));
        cardPaymentMethod.put("parameters", parameters);

        JSONObject tokenizationSpec = new JSONObject();
        tokenizationSpec.put("type", "PAYMENT_GATEWAY");
        JSONObject tokenizationParams = new JSONObject();
        tokenizationParams.put("gateway", "example");
        tokenizationParams.put("gatewayMerchantId", "exampleMerchantId");
        tokenizationSpec.put("parameters", tokenizationParams);
        cardPaymentMethod.put("tokenizationSpecification", tokenizationSpec);

        request.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));

        String numericPrice = price.replaceAll("[^0-9.]", "");
        JSONObject transactionInfo = new JSONObject();
        transactionInfo.put("totalPrice", numericPrice);
        transactionInfo.put("totalPriceStatus", "FINAL");
        transactionInfo.put("currencyCode", "USD");
        request.put("transactionInfo", transactionInfo);

        JSONObject merchantInfo = new JSONObject();
        merchantInfo.put("merchantName", "LearnAI");
        merchantInfo.put("merchantId", "BCR2DN4TRG46QSYM");  // test merchant ID
        request.put("merchantInfo", merchantInfo);

        return request;
    }
}
