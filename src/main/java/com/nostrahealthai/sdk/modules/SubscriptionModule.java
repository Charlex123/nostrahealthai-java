package com.nostrahealthai.sdk.modules;

import com.nostrahealthai.sdk.NostraHealthAI;
import com.nostrahealthai.sdk.exceptions.NostraHealthAIException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * AI subscription management module.
 *
 * <p>Supports subscription plans, purchasing, upgrading/downgrading,
 * cancellation, and AI usage monitoring.</p>
 */
public class SubscriptionModule {
    private final NostraHealthAI client;

    public SubscriptionModule(NostraHealthAI client) {
        this.client = client;
    }

    /**
     * Get all available subscription plans.
     *
     * @return Map with "plans" and "categoryLimitsPerTier"
     */
    public Map<String, Object> getPlans() throws NostraHealthAIException {
        return client.get("/api/v1/ai/subscriptions/plans");
    }

    /**
     * Get details for a specific subscription plan.
     *
     * @param tier Subscription tier (free, basic, standard, premium, premium_plus)
     * @return Plan details
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPlanDetails(String tier) throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/subscriptions/plans/" + tier);
        Object plan = result.get("plan");
        if (plan instanceof Map) {
            return (Map<String, Object>) plan;
        }
        return result;
    }

    /**
     * Get current user's subscription.
     *
     * @return Subscription data
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMySubscription() throws NostraHealthAIException {
        Map<String, Object> result = client.get("/api/v1/ai/subscriptions/my-subscription");
        Object subscription = result.get("subscription");
        if (subscription instanceof Map) {
            return (Map<String, Object>) subscription;
        }
        return result;
    }

    /**
     * Purchase a subscription.
     *
     * @param tier          Subscription tier
     * @param billingCycle  "monthly" or "yearly" (required for paid plans)
     * @param paymentMethod Payment method (required for paid plans)
     * @param currency      Optional currency code
     * @param promoCode     Optional promotional code
     * @return Map with subscription, transaction, and message
     */
    public Map<String, Object> purchase(String tier, String billingCycle, String paymentMethod,
                                         String currency, String promoCode) throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("tier", tier);
        if (billingCycle != null) body.put("billingCycle", billingCycle);
        if (paymentMethod != null) body.put("paymentMethod", paymentMethod);
        if (currency != null) body.put("currency", currency);
        if (promoCode != null) body.put("promoCode", promoCode);

        return client.postJson("/api/v1/ai/subscriptions/purchase", body);
    }

    /**
     * Purchase a free subscription.
     */
    public Map<String, Object> purchaseFree() throws NostraHealthAIException {
        return purchase("free", null, null, null, null);
    }

    /**
     * Change (upgrade or downgrade) subscription.
     *
     * @param newTier         New subscription tier
     * @param reason          Optional reason for change
     * @param immediateChange Whether to change immediately (default: true)
     * @return Map with subscription and message
     */
    public Map<String, Object> change(String newTier, String reason, Boolean immediateChange)
            throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        body.put("newTier", newTier);
        if (reason != null) body.put("reason", reason);
        if (immediateChange != null) body.put("immediateChange", immediateChange);

        return client.putJson("/api/v1/ai/subscriptions/change", body);
    }

    /**
     * Change subscription tier.
     */
    public Map<String, Object> change(String newTier) throws NostraHealthAIException {
        return change(newTier, null, null);
    }

    /**
     * Cancel subscription.
     *
     * @param reason Optional cancellation reason
     */
    public void cancel(String reason) throws NostraHealthAIException {
        Map<String, Object> body = new HashMap<>();
        if (reason != null) body.put("reason", reason);

        client.deleteWithBody("/api/v1/ai/subscriptions/cancel", body);
    }

    /**
     * Cancel subscription without reason.
     */
    public void cancel() throws NostraHealthAIException {
        cancel(null);
    }

    /**
     * Get AI usage statistics.
     *
     * <p>Returns comprehensive usage data including limits, usage counts,
     * remaining requests, reset times, and per-tool/category breakdown.</p>
     *
     * @return Usage response data
     */
    public Map<String, Object> getAiUsage() throws NostraHealthAIException {
        return client.get("/api/v1/ai/subscriptions/ai-usage");
    }

    // =========================================================================
    // ASYNC METHODS
    // =========================================================================

    /**
     * Get all available subscription plans asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getPlansAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getPlans();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get details for a specific subscription plan asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getPlanDetailsAsync(String tier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getPlanDetails(tier);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Get current user's subscription asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getMySubscriptionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getMySubscription();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Purchase a subscription asynchronously.
     */
    public CompletableFuture<Map<String, Object>> purchaseAsync(String tier, String billingCycle,
                                                                 String paymentMethod, String currency, String promoCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return purchase(tier, billingCycle, paymentMethod, currency, promoCode);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Change subscription tier asynchronously.
     */
    public CompletableFuture<Map<String, Object>> changeAsync(String newTier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return change(newTier);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Cancel subscription asynchronously.
     */
    public CompletableFuture<Void> cancelAsync(String reason) {
        return CompletableFuture.runAsync(() -> {
            try {
                cancel(reason);
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }

    /**
     * Cancel subscription without reason asynchronously.
     */
    public CompletableFuture<Void> cancelAsync() {
        return cancelAsync(null);
    }

    /**
     * Get AI usage statistics asynchronously.
     */
    public CompletableFuture<Map<String, Object>> getAiUsageAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getAiUsage();
            } catch (NostraHealthAIException e) {
                throw new CompletionException(e);
            }
        }, client.getExecutorService());
    }
}
