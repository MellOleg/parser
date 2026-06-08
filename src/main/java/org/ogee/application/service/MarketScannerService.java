package org.ogee.application.service;

import org.ogee.infrastructure.http.StalcraftApiClient;
import org.ogee.infrastructure.http.dto.AuctionHistoryResponse;
import org.ogee.infrastructure.http.dto.AuctionLotDto;
import org.ogee.infrastructure.http.dto.AuctionLotsResponse;
import org.ogee.infrastructure.http.dto.AuctionPriceDto;
import org.ogee.ui.model.MarketOpportunityRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MarketScannerService {

    private final WatchlistService watchlistService;
    private final BlacklistService blacklistService;
    private final ItemService itemService;
    private final StalcraftApiClient stalcraftApiClient;

    public MarketScannerService(
            WatchlistService watchlistService,
            BlacklistService blacklistService,
            ItemService itemService,
            StalcraftApiClient stalcraftApiClient
    ) {
        this.watchlistService = watchlistService;
        this.blacklistService = blacklistService;
        this.itemService = itemService;
        this.stalcraftApiClient = stalcraftApiClient;
    }

    public MarketScanResult scanMarket(
            String region,
            int lotsLimit,
            int historyDepth,
            double minProfitPercent,
            long minTotalProfit
    ) {
        List<String> itemIds = watchlistService.getItemIds();
        Set<String> blacklistedIds = blacklistService.getBlacklistedItemIds();

        List<MarketOpportunityRow> rows = new ArrayList<>();

        int scannedItems = 0;
        int foundLots = 0;

        for (String itemId : itemIds) {
            if (blacklistedIds.contains(itemId)) {
                continue;
            }

            try {
                String itemName = itemService.getItemName(itemId);

                AuctionHistoryResponse history = stalcraftApiClient.getAuctionHistory(
                        region,
                        itemId,
                        historyDepth,
                        0
                );

                if (history.getPrices() == null || history.getPrices().isEmpty()) {
                    continue;
                }

                long marketPricePerUnit = calculateMedianPricePerUnit(history.getPrices());

                if (marketPricePerUnit <= 0) {
                    continue;
                }

                AuctionLotsResponse lotsResponse = stalcraftApiClient.getAuctionLots(
                        region,
                        itemId,
                        lotsLimit,
                        0
                );

                if (lotsResponse.getLots() == null || lotsResponse.getLots().isEmpty()) {
                    continue;
                }

                scannedItems++;
                foundLots += lotsResponse.getLots().size();

                System.out.println(
                        "Scanned itemId=" + itemId
                                + ", name=" + itemName
                                + ", lots=" + lotsResponse.getLots().size()
                                + ", market=" + marketPricePerUnit
                );

                for (AuctionLotDto lot : lotsResponse.getLots()) {
                    if (!lot.hasBuyoutPrice()) {
                        continue;
                    }

                    if (lot.getAmount() <= 0) {
                        continue;
                    }

                    long lotPrice = lot.getBuyoutPrice();
                    long pricePerUnit = lot.getBuyoutPricePerUnit();

                    if (pricePerUnit <= 0) {
                        continue;
                    }

                    double profitPercent = calculateProfitPercent(
                            pricePerUnit,
                            marketPricePerUnit
                    );

                    long estimatedProfit = calculateEstimatedProfit(
                            lot.getAmount(),
                            pricePerUnit,
                            marketPricePerUnit
                    );

                    if (profitPercent < minProfitPercent) {
                        continue;
                    }

                    if (estimatedProfit < minTotalProfit) {
                        continue;
                    }

                    rows.add(new MarketOpportunityRow(
                            itemName,
                            itemId,
                            lot.getAmount(),
                            lotPrice,
                            pricePerUnit,
                            marketPricePerUnit,
                            profitPercent,
                            estimatedProfit,
                            lot.getEndTime()
                    ));
                }

            } catch (Exception e) {
                System.out.println("Failed to scan itemId=" + itemId + " | " + e.getMessage());
            }
        }

        rows.sort((a, b) -> Long.compare(b.getEstimatedProfit(), a.getEstimatedProfit()));

        return new MarketScanResult(
                rows,
                itemIds.size(),
                blacklistedIds.size(),
                scannedItems,
                foundLots
        );
    }

    private long calculateMedianPricePerUnit(List<AuctionPriceDto> prices) {
        if (prices == null || prices.isEmpty()) {
            return 0;
        }

        List<Long> unitPrices = prices.stream()
                .map(AuctionPriceDto::getPricePerUnit)
                .filter(price -> price > 0)
                .sorted()
                .toList();

        if (unitPrices.isEmpty()) {
            return 0;
        }

        int size = unitPrices.size();
        int middle = size / 2;

        if (size % 2 == 1) {
            return unitPrices.get(middle);
        }

        return (unitPrices.get(middle - 1) + unitPrices.get(middle)) / 2;
    }

    private double calculateProfitPercent(long pricePerUnit, long marketPricePerUnit) {
        if (pricePerUnit <= 0) {
            return 0;
        }

        double profit = marketPricePerUnit - pricePerUnit;

        return Math.round((profit / pricePerUnit * 100.0) * 100.0) / 100.0;
    }

    private long calculateEstimatedProfit(
            int amount,
            long pricePerUnit,
            long marketPricePerUnit
    ) {
        return (marketPricePerUnit - pricePerUnit) * amount;
    }

    public int getWatchlistSize() {
        return watchlistService.getWatchlistSize();
    }
}