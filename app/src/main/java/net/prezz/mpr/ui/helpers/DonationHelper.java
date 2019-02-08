package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import net.prezz.mpr.R;

import java.util.Arrays;
import java.util.List;

public class DonationHelper {

    private static final String SKU_SMALL_DONATION = "sku_donate_small";
    private static final String SKU_MEDIUM_DONATION = "sku_donate_medium";
    private static final String SKU_LARGE_DONATION = "sku_donate_large";
    private static final String SKU_HUGE_DONATION = "sku_donate_huge";

    private final Activity activity;

    private BillingClient billingClient;


    public DonationHelper(Activity owner) {
        this.activity = owner;

        // initialize the billing client
        executeBillingRequest(new Runnable() {
            @Override
            public void run() {
                //do nothing
            }
        }, false);
    }

    public void startDonation() {
        Runnable request = new Runnable() {
            @Override
            public void run() {
                List<String> skuList = Arrays.asList(SKU_SMALL_DONATION, SKU_MEDIUM_DONATION, SKU_LARGE_DONATION, SKU_HUGE_DONATION);
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder()
                        .setSkusList(skuList)
                        .setType(BillingClient.SkuType.INAPP);

                billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@BillingClient.BillingResponse int responseCode, List<SkuDetails> skuDetails) {
                        if (responseCode != BillingClient.BillingResponse.OK || skuDetails == null) {
                            Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
                            Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase. Response code: " + responseCode);
                            return;
                        }

                        showDonation(skuDetails);
                    }
                });
            }
        };

        executeBillingRequest(request, true);
    }

    public void close() {
        if (billingClient != null && billingClient.isReady()) {
            try {
                billingClient.endConnection();
            } catch (Exception ex) {
                Log.e(DonationHelper.class.getName(), "Problem closing billing client", ex);
            }
        }
        billingClient = null;
    }

    private void showDonation(final List<SkuDetails> skuDetails) {
        String[] items = new String[skuDetails.size()];
        for (int i = 0; i < items.length; i++) {
            SkuDetails details = skuDetails.get(i);
            items[i] = "Donate " + details.getPrice();
        }

        final int[] selection = { 0 };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Donate");
        builder.setSingleChoiceItems(items, selection[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selection[0] = which;
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SkuDetails details = skuDetails.get(selection[0]);
                ensureDonationConsumed(details);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ensureDonationConsumed(final SkuDetails skuDetails) {
        Runnable request = new Runnable() {
            @Override
            public void run() {
                Purchase incompletePurchase = null;
                Purchase.PurchasesResult purchaseResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                List<Purchase> purchaseList = purchaseResult.getPurchasesList();
                for (Purchase purchase : purchaseList) {
                    if (skuDetails != null && skuDetails.getSku().equals(purchase.getSku())) {
                        incompletePurchase = purchase;
                    }
                }

                if (incompletePurchase != null) {
                    billingClient.consumeAsync(incompletePurchase.getPurchaseToken(), new ConsumeResponseListener() {
                        @Override
                        public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String purchaseToken) {
                            if (responseCode != BillingClient.BillingResponse.OK) {
                                Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
                                Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase. Response code: " + responseCode);
                                return;
                            }

                            performDonation(skuDetails);
                        }
                    });
                } else {
                    performDonation(skuDetails);
                }
            }
        };

        executeBillingRequest(request, true);
    }

    private void performDonation(final SkuDetails details) {
        Runnable request = new Runnable() {
            @Override
            public void run() {
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(details.getSku())
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                billingClient.launchBillingFlow(activity, flowParams);
            }
        };

        executeBillingRequest(request, true);
    }

    private void executeBillingRequest(final Runnable billingRequest, boolean showError) {
        try {
            if (billingClient != null && billingClient.isReady()) {
                billingRequest.run();
            } else {
                initializeBillingClient(billingRequest, showError);
            }
        } catch (Exception ex) {
            if (showError) {
                Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
            }
            Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase.", ex);
        }
    }

    private void initializeBillingClient(final Runnable onSuccess, final boolean showError) {
        billingClient = BillingClient.newBuilder(activity)
                .setListener(new OnPurchaseUpdatedListener())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int responseCode) {
                if (responseCode != BillingClient.BillingResponse.OK) {
                    if (showError) {
                        Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
                    }
                    Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase. Response code: " + responseCode);
                    return;
                }

                try {
                    onSuccess.run();
                } catch (Exception ex) {
                    if (showError) {
                        Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
                    }
                    Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase.", ex);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });
    }

    private final class OnPurchaseUpdatedListener implements PurchasesUpdatedListener {

        @Override
        public void onPurchasesUpdated(@BillingClient.BillingResponse int responseCode, final List<Purchase> purchases) {
            if (responseCode != BillingClient.BillingResponse.OK) {
                Boast.makeText(activity, R.string.player_donate_complete_error_toast).show();
                Log.e(DonationHelper.class.getName(), "Unable to completing up in-app purchase. Response code: " + responseCode);
                return;
            }

            Runnable request = new Runnable() {
                @Override
                public void run() {
                    for (Purchase purchase : purchases) {
                        billingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String purchaseToken) {
                                if (responseCode != BillingClient.BillingResponse.OK) {
                                    Log.e(DonationHelper.class.getName(), "Unable to consume in-app purchase");
                                }
                            }
                        });
                    }
                };
            };

            executeBillingRequest(request, false);
        }
    }
}
