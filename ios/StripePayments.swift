//
//  StripePayment.swift
//  StripePayments
//
//  Created by Travlr on 27/10/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import Foundation
import Stripe

@objc(StripePayments)
class StripePayments: NSObject {
    
    @objc var callback: RCTResponseSenderBlock?
    
    override init() {
        self.callback = nil
    }
    
    @objc
    func `init`(_ publishableKey: String) {
        Stripe.setDefaultPublishableKey(publishableKey)
    }
    
    @objc
    func onPaymentSuccess(_ callback: RCTResponseSenderBlock?) {
        self.callback = callback
    }

    @objc
    func confirmPayment(
        _ secret: String,
        cardParams: STPPaymentMethodCardParams
    ) {
        // Collect card details
        let paymentMethodParams = STPPaymentMethodParams(card: cardParams, billingDetails: nil, metadata: nil)
        let paymentIntentParams = STPPaymentIntentParams(clientSecret: secret)
        paymentIntentParams.paymentMethodParams = paymentMethodParams
        paymentIntentParams.setupFutureUsage = NSNumber(value: STPPaymentIntentSetupFutureUsage.offSession.rawValue)

        // Submit the payment
        let paymentHandler = STPPaymentHandler.shared()
        paymentHandler.confirmPayment(withParams: paymentIntentParams, authenticationContext: self) { (status, paymentIntent, error) in
            print("STRIPE:: confirm payment completed")

            switch (status) {
            case .failed:
                print("failed")
                self.callback!(["Payment failed", error?.localizedDescription ?? ""])
                break
            case .canceled:
                print("canceled")
                self.callback!(["Payment canceled", error?.localizedDescription ?? ""])
                break
            case .succeeded:
                print("succeeded")
                self.callback!([
                    [
                        "id": paymentIntent!.allResponseFields["id"],
                        "paymentMethodId": paymentIntent!.paymentMethodId,
                        "paymentIntent": paymentIntent!.allResponseFields
                    ],
                    ""
                ])
                break
            @unknown default:
                print("STRIPE:: Stripe payment failed. Unknown Error")
                fatalError()
                break
            }
            self.callback = nil
        }
    }

    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
}

extension StripePayments: STPAuthenticationContext {
    func authenticationPresentingViewController() -> UIViewController {
        return RCTPresentedViewController()!
    }
}
