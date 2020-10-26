package com.fitpassu.stripepayments;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.android.view.CardNumberTextInputLayout;
import com.stripe.android.view.CardValidCallback;
import com.stripe.android.view.CardWidgetProgressView;
import com.stripe.android.view.IconTextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class StripeCardInputManager extends SimpleViewManager<CardMultilineWidget> implements CardValidCallback {

    public static final String REACT_CLASS = "StripeCardInput";
    private static final String TAG = StripeCardInputManager.class.getSimpleName();
    private static final int COMMAND_CONFIRM_PAYMENT = 9;

    private ThemedReactContext reactContext;

    private CardMultilineWidget view;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CardMultilineWidget createViewInstance(final ThemedReactContext reactContext) {
        this.reactContext = reactContext;

        view = new CardMultilineWidget(reactContext);
        /**
         * Temporary Bug Fix Stripe(v16.0.1) CardMultilineWidget
         * Bug: `the specified child already has a parent` when re-attached
         * Cause: CardNumberTextInputLayout re-attaching CardWidgetProgressView without removing it first from the parent
         */
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                for (int index = 0; index < ((ViewGroup) v).getChildCount(); index ++) {
                    View child = ((ViewGroup) v).getChildAt(index);
                    if (child.getClass().getName().contains("CardNumberTextInputLayout")) {
                        IconTextInputLayout problematicView = (IconTextInputLayout) child;
                        if (problematicView.getChildCount() > 0) {
                            ViewGroup internalChild = (ViewGroup) problematicView.getChildAt(0);
                            for (int iindex = 0; iindex < internalChild.getChildCount(); iindex++) {
                                View internalChild2 = internalChild.getChildAt(iindex);
                                if (internalChild2.getClass().getName().contains("CardWidgetProgressView")){
                                    internalChild.removeView(internalChild2);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });
        view.setPostalCodeRequired(false);
        view.setUsZipCodeRequired(false);
        view.setShouldShowPostalCode(false);
        view.setCardValidCallback(this);
        return view;
    }

    public void confirmPayment(String clientSecret) {
        StripePaymentsModule.self.confirmPayment(clientSecret, view.getPaymentMethodCreateParams(), null);
    }

    public boolean isValid() {
        return view.validateAllFields();
    }

    @Override
    public Map<String,Integer> getCommandsMap() {
        Log.d("React"," View manager getCommandsMap:");
        return MapBuilder.of("confirmPayment", COMMAND_CONFIRM_PAYMENT);
    }

    @Override
    public void receiveCommand(CardMultilineWidget view, int commandType, ReadableArray args) {
        Assertions.assertNotNull(view);
        Assertions.assertNotNull(args);
        switch (commandType) {
            case COMMAND_CONFIRM_PAYMENT: {
                this.confirmPayment(args.getString(0));
                return;
            }
            default:
                throw new IllegalArgumentException(String.format(
                        "Unsupported command %d received by %s.",
                        commandType,
                        getClass().getSimpleName()));
        }
    }

    @Override
    public void onInputChanged(boolean b, @NotNull Set<? extends Fields> set) {
        if (b) {
            WritableMap event = Arguments.createMap();
            event.putBoolean("isValid", isValid());
            reactContext
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(
                        view.getId(),
                        "cardvalid",
                        event
                );
        }
    }

    @Override
    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put(
                        "cardvalid",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onCardValid")
                        )
                )
                .build();
    }
}