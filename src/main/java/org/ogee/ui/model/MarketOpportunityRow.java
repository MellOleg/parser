package org.ogee.ui.model;

public class MarketOpportunityRow {

    private final String priority;
    private final String itemName;
    private final String itemId;
    private final int amount;
    private final long lotPrice;
    private final long pricePerUnit;
    private final long marketPricePerUnit;
    private final double profitPercent;
    private final long estimatedProfit;
    private final long sellAfterFeePerUnit;
    private final String time;

    public MarketOpportunityRow(
            String priority,
            String itemName,
            String itemId,
            int amount,
            long lotPrice,
            long pricePerUnit,
            long marketPricePerUnit,
            double profitPercent,
            long estimatedProfit,
            long sellAfterFeePerUnit,
            String time
    ) {
        this.priority = priority;
        this.itemName = itemName;
        this.itemId = itemId;
        this.amount = amount;
        this.lotPrice = lotPrice;
        this.pricePerUnit = pricePerUnit;
        this.marketPricePerUnit = marketPricePerUnit;
        this.profitPercent = profitPercent;
        this.estimatedProfit = estimatedProfit;
        this.sellAfterFeePerUnit = sellAfterFeePerUnit;
        this.time = time;
    }
    public String getPriority() {
        return priority;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public long getLotPrice() {
        return lotPrice;
    }

    public long getPricePerUnit() {
        return pricePerUnit;
    }

    public long getMarketPricePerUnit() {
        return marketPricePerUnit;
    }

    public double getProfitPercent() {
        return profitPercent;
    }

    public long getEstimatedProfit() {
        return estimatedProfit;
    }

    public long getSellAfterFeePerUnit() {
        return sellAfterFeePerUnit;
    }

    public String getTime() {
        return time;
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
}