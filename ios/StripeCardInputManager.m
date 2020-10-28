//
//  StripeCardInput.m
//  StripePayments
//
//  Created by Travlr on 27/10/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(StripeCardInputManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(onCardValid, RCTDirectEventBlock)
RCT_EXTERN_METHOD(
  confirmPayment:(nonnull NSNumber *)node
  clientSecret:(NSString *)clientSecret
)

@end
