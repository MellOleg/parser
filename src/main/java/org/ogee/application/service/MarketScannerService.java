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
            long minTotalProfit,
            double auctionFeePercent
    ) {
        List<String> itemIds = watchlistService.getItemIds();
        Set<String> blacklistedIds = blacklistService.getBlacklistedItemIds();

        List<MarketOpportunityRow> rows = new ArrayList<>();

        int scannedItems = 0;
        int foundLots = 0;

        System.out.println("Watchlist items: " + itemIds.size());
        System.out.println("Blacklisted items: " + blacklistedIds.size());

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
                    System.out.println("No history for itemId=" + itemId);
                    continue;
                }

                long marketPricePerUnit = calculateMedianPricePerUnit(history.getPrices());

                if (marketPricePerUnit <= 0) {
                    System.out.println("Invalid market price for itemId=" + itemId);
                    continue;
                }

                AuctionLotsResponse lotsResponse = stalcraftApiClient.getAuctionLots(
                        region,
                        itemId,
                        lotsLimit,
                        0
                );

                if (lotsResponse.getLots() == null || lotsResponse.getLots().isEmpty()) {
                    System.out.println("No lots for itemId=" + itemId);
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

                    long sellAfterFeePerUnit = calculateSellAfterFee(
                            marketPricePerUnit,
                            auctionFeePercent
                    );

                    double profitPercent = calculateProfitPercent(
                            pricePerUnit,
                            sellAfterFeePerUnit
                    );

                    long estimatedProfit = calculateEstimatedProfit(
                            lot.getAmount(),
                            pricePerUnit,
                            sellAfterFeePerUnit
                    );
                    boolean hasProfit = estimatedProfit > 0;
                    boolean passesPercentFilter = profitPercent >= minProfitPercent;
                    boolean passesTotalProfitFilter = estimatedProfit >= minTotalProfit;

                    if (!hasProfit) {
                        continue;
                    }

                    if (!passesPercentFilter && !passesTotalProfitFilter) {
                        continue;
                    }
                    String priority = calculatePriority(profitPercent, estimatedProfit);

                    rows.add(new MarketOpportunityRow(
                            priority,
                            itemName,
                            itemId,
                            lot.getAmount(),
                            lotPrice,
                            pricePerUnit,
                            marketPricePerUnit,
                            profitPercent,
                            estimatedProfit,
                            sellAfterFeePerUnit,
                            lot.getEndTime()
                    ));
                }

            } catch (Exception e) {
                System.out.println("Failed to scan itemId=" + itemId + " | " + e.getMessage());
            }
        }

        rows.sort((a, b) -> {
            int priorityCompare = Integer.compare(
                    getPriorityRank(b.getPriority()),
                    getPriorityRank(a.getPriority())
            );

            if (priorityCompare != 0) {
                return priorityCompare;
            }

            int percentCompare = Double.compare(
                    b.getProfitPercent(),
                    a.getProfitPercent()
            );

            if (percentCompare != 0) {
                return percentCompare;
            }

            return Long.compare(
                    b.getEstimatedProfit(),
                    a.getEstimatedProfit()
            );
        });


        return new MarketScanResult(
                rows,
                itemIds.size(),
                blacklistedIds.size(),
                scannedItems,
                foundLots
        );
    }
    private String calculatePriority(double profitPercent, long estimatedProfit) {
        if (estimatedProfit >= 500_000 || profitPercent >= 100.0) {
            return "S";
        }

        if (estimatedProfit >= 100_000 || profitPercent >= 50.0) {
            return "A";
        }

        if (estimatedProfit >= 30_000 || profitPercent >= 20.0) {
            return "B";
        }

        return "C";
    }
    private int getPriorityRank(String priority) {
        return switch (priority) {
            case "S" -> 4;
            case "A" -> 3;
            case "B" -> 2;
            case "C" -> 1;
            default -> 0;
        };
    }
    private long calculateSellAfterFee(long marketPricePerUnit, double auctionFeePercent) {
        double feeMultiplier = 1.0 - (auctionFeePercent / 100.0);

        if (feeMultiplier <= 0) {
            return 0;
        }

        return Math.round(marketPricePerUnit * feeMultiplier);
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

    private double calculateProfitPercent(long pricePerUnit, long sellAfterFeePerUnit) {
        if (pricePerUnit <= 0) {
            return 0;
        }

        double profit = sellAfterFeePerUnit - pricePerUnit;

        return Math.round((profit / pricePerUnit * 100.0) * 100.0) / 100.0;
    }

    private long calculateEstimatedProfit(
            int amount,
            long pricePerUnit,
            long sellAfterFeePerUnit
    ) {
        return (sellAfterFeePerUnit - pricePerUnit) * amount;
    }

    public int getWatchlistSize() {
        return watchlistService.getWatchlistSize();
    }
}