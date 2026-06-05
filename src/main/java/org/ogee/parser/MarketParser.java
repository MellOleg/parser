package org.ogee.parser;


import org.ogee.domain.model.MarketOffer;

import java.util.List;

public interface MarketParser {
    String sourceName();

    List<MarketOffer> fetchOffers();
}