package com.fitpassu.stripepayments;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;

import com.facebook.react.bridge.WritableMap;
import com.google.gson.FieldNamingPolicy;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StripePaymentsModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;
    public static StripePaymentsModule self;

    private Stripe stripe;
    private Callback paymentCallback;

    private final ActivityEventListener activityListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (paymentCallback == null || stripe == null) {
                super.onActivityResult(activity, requestCode, resultCode, data);
                return;
            }
            boolean handled = false;
                handled = stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(paymentCallback, new Runnable() {
                    @Override
                    public void run() {
                        paymentCallback = null;
                    }
                }));
            if (!handled) {
                super.onActivityResult(activity, requestCode, resultCode, data);
            }
        }
    };

    StripePaymentsModule(ReactApplicationContext context) {
        super(context);

        context.addActivityEventListener(activityListener);

        reactContext = context;
        self = this;
    }

    @Override
    public String getName() {
        return "StripePayments";
    }

    @ReactMethod
    public void init(String publishableKey) {
        PaymentConfiguration.init(
                reactContext,
                publishableKey
        );
    }

    @ReactMethod
    public void onPaymentSuccess(Callback callback) {
        this.paymentCallback = callback;
    }

    @ReactMethod
    public void confirmPayment(String secret, ReadableMap cardParams) {
        PaymentMethodCreateParams.Card card = new PaymentMethodCreateParams.Card(
                cardParams.getString("number"),
                cardParams.getInt("expMonth"),
                cardParams.getInt("expYear"),
                cardParams.getString("cvc"),
                null,
                null
        );
        PaymentMethodCreateParams params = PaymentMethodCreateParams.create(card);
        this.confirmPayment(secret, params);
    }

    public void confirmPayment(String secret, PaymentMethodCreateParams params) {
        Map<String, String> extraParams = new HashMap<>();
        extraParams.put("setup_future_usage", "off_session");

        ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                .createWithPaymentMethodCreateParams(params, secret, null, false, extraParams);
        if (params == null) {
            if (paymentCallback != null) {
                paymentCallback.invoke("StripeModule.invalidPaymentIntentParams", "Bad Request");
            }
            return;
        }
        stripe = new Stripe(
                reactContext,
                PaymentConfiguration.getInstance(reactContext).getPublishableKey()
        );
        stripe.confirmPayment(getCurrentActivity(), confirmParams);
    }

    private static final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        private Callback callback = null;
        private Runnable runAfterDone;

        PaymentResultCallback(Callback callback, Runnable runAfterDone) {
            this.callback = callback;
            this.runAfterDone = runAfterDone;
        }

        @Override
        public void onSuccess(PaymentIntentResult result) {
            final PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();

            if (
                    status == PaymentIntent.Status.Succeeded ||
                    status == PaymentIntent.Status.Processing ||
                    status == PaymentIntent.Status.RequiresCapture
            ) {
                Gson gson = new GsonBuilder()
                        .serializeNulls()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();

                String paymentIntentJson = gson.toJson(paymentIntent);

                WritableMap map = Arguments.createMap();
                map.putString("id", paymentIntent.getId());
                map.putString("paymentMethodId", paymentIntent.getPaymentMethod().id);
                map.putString("paymentIntent", paymentIntentJson);

                if (callback != null) {
                    callback.invoke(map);
                }
            } else if (status == PaymentIntent.Status.Canceled) {
                if (callback != null){
                    callback.invoke("StripeModule.cancelled", "");
                }
            } else {
                if (callback != null){
                    callback.invoke("StripeModule.failed", status.toString());
                }
            }
            
            if (runAfterDone != null) {
                runAfterDone.run();
            }
        }

        @Override
        public void onError(Exception e) {
            if (callback != null){
                callback.invoke("StripeModule.failed", e.getMessage());
            }
        }
    }
}
