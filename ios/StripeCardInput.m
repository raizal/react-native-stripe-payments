#import "StripeCardInput.h"
#import <Stripe/Stripe.h>

@implementation StripeCardInput

RCT_EXPORT_MODULE()

- (UIView *)view
{
  return [[STPPaymentCardTextField alloc] init];
}

@end