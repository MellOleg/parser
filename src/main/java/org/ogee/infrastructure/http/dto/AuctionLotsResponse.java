package org.ogee.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuctionLotsResponse {

    private long total;
    private List<AuctionLotDto> lots;

    public long getTotal() {
        return total;
    }

    public List<AuctionLotDto> getLots() {
        return lots;
    }
}