package org.ogee.application.service;

import org.ogee.infrastructure.http.StalcraftApiClient;
import org.ogee.infrastructure.http.dto.AuctionHistoryResponse;
import org.ogee.infrastructure.http.dto.AuctionPriceDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParserService {

    private final StalcraftApiClient stalcraftApiClient;
    private final ItemService itemService;

    public ParserService(
            StalcraftApiClient stalcraftApiClient,
            ItemService itemService
    ) {
        this.stalcraftApiClient = stalcraftApiClient;
        this.itemService = itemService;
    }

    public AuctionHistoryResponse getAuctionHistory(
            String region,
            String itemId,
            int limit,
            int offset
    ) {
        return stalcraftApiClient.getAuctionHistory(region, itemId, limit, offset);
    }

    public List<AuctionPriceDto> getAuctionHistoryPrices(
            String region,
            String itemId,
            int limit,
            int offset
    ) {
        return getAuctionHistory(region, itemId, limit, offset).getPrices();
    }

    public String getItemName(String itemId) {
        return itemService.getItemName(itemId);
    }

    public void testParse() {
        String itemId = "1dk6";

        System.out.println("Loaded items: " + itemService.getLoadedItemsCount());
        System.out.println("Item name: " + itemService.getItemName(itemId));
    }
}