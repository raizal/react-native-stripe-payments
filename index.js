import { NativeModules } from 'react-native';

const { StripePayments } = NativeModules;

const InitParams = {
  publishingKey: ''
}

const CardDetails = {
  number: '',
  expMonth: '',
  expYear: '',
  cvc: ''
}

const PaymentResult = {
  id: '',
  paymentMethodId: '',
  paymentIntent: ''
}

class Stripe {
  _stripeInitialized = false

  setOptions = (options = InitParams) => {
    if (this._stripeInitialized) { return; }
    StripePayments.init(options.publishingKey);
    this._stripeInitialized = true;
  }

  confirmPayment(clientSecret, cardDetails = CardDetails) {
    return StripePayments.confirmPayment(clientSecret, cardDetails)
  }

  isCardValid(cardDetails = CardDetails) {
    return StripePayments.isCardValid(cardDetails) == true;
  }
}

export default new Stripe();
