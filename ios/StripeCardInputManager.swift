//
//  StripeCardInput.swift
//  StripePayments
//
//  Created by Travlr on 27/10/20.
//  Copyright © 2020 Facebook. All rights reserved.
//
import Stripe

@objc(StripeCardInputManager)
class StripeCardInputManager: RCTViewManager {
    override func view() -> UIView! {
        return StripeCardInput()
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc func confirmPayment(
        _ node: NSNumber,
        clientSecret: String
    ) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(
                forReactTag: node
            ) as! StripeCardInput
            component.confirmPayment(clientSecret: clientSecret, bridge: self.bridge)
        }
    }
}
