package org.ogee.application.service;

import org.ogee.ui.model.MarketOpportunityRow;

import java.util.List;

public class MarketScanResult {

    private final List<MarketOpportunityRow> rows;
    private final int watchlistItems;
    private final int blacklistedItems;
    private final int scannedItems;
    private final int foundLots;

    public MarketScanResult(
            List<MarketOpportunityRow> rows,
            int watchlistItems,
            int blacklistedItems,
            int scannedItems,
            int foundLots
    ) {
        this.rows = rows;
        this.watchlistItems = watchlistItems;
        this.blacklistedItems = blacklistedItems;
        this.scannedItems = scannedItems;
        this.foundLots = foundLots;
    }

    public List<MarketOpportunityRow> getRows() {
        return rows;
    }

    public int getWatchlistItems() {
        return watchlistItems;
    }

    public int getBlacklistedItems() {
        return blacklistedItems;
    }

    public int getScannedItems() {
        return scannedItems;
    }

    public int getFoundLots() {
        return foundLots;
    }

    public int getShownRows() {
        return rows.size();
    }

    public int getFilteredLots() {
        return foundLots - rows.size();
    }

    public int getRequestsPerCycle() {
        return scannedItems * 2;
    }

    public int getRequestsPerMinute(int intervalSeconds) {
        if (intervalSeconds <= 0) {
            return 0;
        }

        return (int) Math.round(getRequestsPerCycle() * (60.0 / intervalSeconds));
    }
}