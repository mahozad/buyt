package com.pleon.buyt.billing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an in-app product's listing details.
 */
public class SkuDetails {

    private String mSku;
    private String mType;
    private String mPrice;
    private String mTitle;
    private String mDescription;
    private String mJson;

    SkuDetails(String jsonSkuDetails) throws JSONException {
        mJson = jsonSkuDetails;
        JSONObject o = new JSONObject(mJson);
        mSku = o.optString("productId");
        mType = o.optString("type");
        mPrice = o.optString("price");
        mTitle = o.optString("title");
        mDescription = o.optString("description");
    }

    String getSku() {
        return mSku;
    }

    public String getType() {
        return mType;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }
}
