package org.ogee.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuctionPriceDto {

    private int amount;
    private long price;
    private Instant time;
    private Map<String, Object> additional;

    public int getAmount() {
        return amount;
    }

    public long getPrice() {
        return price;
    }

    public Instant getTime() {
        return time;
    }

    public Map<String, Object> getAdditional() {
        return additional;
    }

    public long getPricePerUnit() {
        if (amount <= 0) {
            return price;
        }

        return price / amount;
    }
}