const stripe = Stripe('pk_test_51QyqoiQFkuFRMNacnJpe5I29kR6tLyzTs0WqIyC4k9sbHDi28K7n2n7iX2Ql46kSWvczAWwCSHC4JDRvTJhPM62C00GVIVOGuE');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
	stripe.redirectToCheckout({
		sessionId: sessionId
	})
});