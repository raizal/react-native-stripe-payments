//
//  StripeCardInput.swift
//  StripePayments
//
//  Created by Travlr on 27/10/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

import UIKit
import Stripe

class StripeCardInput: UIView {
    @objc var onCardValid: RCTDirectEventBlock?
    
    lazy var stripeInput: STPPaymentCardTextField = {
        let control = STPPaymentCardTextField()
        control.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        control.postalCodeEntryEnabled = false
        control.delegate = self
        return control
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(stripeInput)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func confirmPayment(clientSecret: String, bridge: RCTBridge) {
        let stripeModule = bridge.module(forName: "StripePayments") as! StripePayments
        stripeModule.confirmPayment(clientSecret, cardParams: stripeInput.cardParams)
    }
}

extension StripeCardInput: STPPaymentCardTextFieldDelegate {
    func paymentCardTextFieldDidChange(_ textField: STPPaymentCardTextField) {
        self.onCardValid!(["isValid": textField.isValid])
    }
}
