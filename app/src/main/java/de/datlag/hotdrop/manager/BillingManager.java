package de.datlag.hotdrop.manager;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {

    //ToDO: monthly subscriptions
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {}
}
