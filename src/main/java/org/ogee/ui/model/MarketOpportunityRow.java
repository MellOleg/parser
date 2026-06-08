package org.ogee.ui.model;

public class MarketOpportunityRow {

    private final String itemName;
    private final String itemId;
    private final int amount;
    private final long lotPrice;
    private final long pricePerUnit;
    private final long marketPricePerUnit;
    private final double profitPercent;
    private final long estimatedProfit;
    private final String time;

    public MarketOpportunityRow(
            String itemName,
            String itemId,
            int amount,
            long lotPrice,
            long pricePerUnit,
            long marketPricePerUnit,
            double profitPercent,
            long estimatedProfit,
            String time
    ) {
        this.itemName = itemName;
        this.itemId = itemId;
        this.amount = amount;
        this.lotPrice = lotPrice;
        this.pricePerUnit = pricePerUnit;
        this.marketPricePerUnit = marketPricePerUnit;
        this.profitPercent = profitPercent;
        this.estimatedProfit = estimatedProfit;
        this.time = time;
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

    public String getTime() {
        return time;
    }
}