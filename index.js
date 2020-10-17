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

const camelToSnakeCase = (str) => {
  return (str[0].toLowerCase() + str.slice(1, str.length)).replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)
}

/**
 * ios & android stripe payment intent json data is a bit different
 * This function will help transform the differences based on ios json format
 *
 * @param {object} oldPaymentIntent PaymentIntent object from Stripe
 * @return {object} transformed object
 */
const transformPaymentIntent = (oldPaymentIntent) => {
  const paymentIntent = {
    ...oldPaymentIntent
  }
  paymentIntent.capture_method = paymentIntent.capture_method.toLowerCase()
  paymentIntent.confirmation_method = paymentIntent.confirmation_method.toLowerCase()
  if (!('livemode' in paymentIntent)) {
    paymentIntent.livemode = paymentIntent.is_live_mode || false
    delete paymentIntent.is_live_mode
  }
  paymentIntent.object = "payment_intent"

  paymentIntent.payment_method.object = 'payment_method'
  if (!('livemode' in paymentIntent.payment_method)) {
    paymentIntent.payment_method.livemode = paymentIntent.payment_method.live_mode || false
    delete paymentIntent.payment_method.live_mode
  }
  paymentIntent.payment_method.type = paymentIntent.payment_method.type.toLowerCase()

  if ('is_supported' in paymentIntent.payment_method.card.three_d_secure_usage) {
    paymentIntent.payment_method.card.three_d_secure_usage = {
      supported: paymentIntent.payment_method.card.three_d_secure_usage.is_supported
    }
  }

  paymentIntent.setup_future_usage = camelToSnakeCase(paymentIntent.setup_future_usage)
  paymentIntent.status = camelToSnakeCase(paymentIntent.status)

  return paymentIntent
}

class Stripe {
  _stripeInitialized = false

  setOptions = (options = InitParams) => {
    if (this._stripeInitialized) { return; }
    StripePayments.init(options.publishingKey);
    this._stripeInitialized = true;
  }

  async confirmPayment(clientSecret, cardDetails = CardDetails) {
    const result = await StripePayments.confirmPayment(clientSecret, cardDetails)
    const paymentIntent = typeof result.paymentIntent === 'string' || result.paymentIntent instanceof String ? JSON.parse(result.paymentIntent) : result.paymentIntent
    result.paymentIntent = JSON.stringify(transformPaymentIntent(paymentIntent))
    return result
  }

  isCardValid = async (cardDetails = CardDetails) => {
    return StripePayments.isCardValid(cardDetails);
  }
}

export default new Stripe();
