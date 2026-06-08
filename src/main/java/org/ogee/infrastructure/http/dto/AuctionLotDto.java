package org.ogee.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuctionLotDto {

    private String itemId;
    private int amount;
    private long startPrice;
    private long currentPrice;
    private long buyoutPrice;
    private String startTime;
    private String endTime;
    private Map<String, Object> additional;

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public long getStartPrice() {
        return startPrice;
    }

    public long getCurrentPrice() {
        return currentPrice;
    }

    public long getBuyoutPrice() {
        return buyoutPrice;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Map<String, Object> getAdditional() {
        return additional;
    }

    public long getBuyoutPricePerUnit() {
        if (amount <= 0) {
            return 0;
        }

        return buyoutPrice / amount;
    }

    public boolean hasBuyoutPrice() {
        return buyoutPrice > 0;
    }
}