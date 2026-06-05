package org.ogee.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuctionHistoryResponse {

    private int total;
    private List<AuctionPriceDto> prices;

    public int getTotal() {
        return total;
    }

    public List<AuctionPriceDto> getPrices() {
        return prices;
    }
}