//
//  StripePayments.m
//  StripePayments
//
//  Created by Travlr on 27/10/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <Stripe/Stripe.h>

@interface RCT_EXTERN_MODULE(StripePayments, NSObject)

RCT_EXTERN_METHOD(onPaymentSuccess:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(init:(NSString *)publishableKey)
RCT_EXTERN_METHOD(
    confirmPayment:(NSString *)secret
    cardParams:(STPPaymentMethodCardParams *)cardParams
)

@end
