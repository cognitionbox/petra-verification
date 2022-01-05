package com.cognitionbox.petra.examples.tradingsystem4.decision.views;

// having separate default interfaces means each can be analysed in isolation,
// ie allows for separation to reduce state space and therefore reduces complexity of abstract soundness/completeness
// checking, relative to the information provided in the interface only.
public interface StrategyDecisionStateView {
    AskVsExitView askVsExitView();
    AskVsLimitView askVsLimitView();
    AskVsStopView askVsStopView();
    BidVsExitView bidVsExitView();
    BidVsLimitView bidVsLimitView();
    BidVsStopView bidVsStopView();
    DirectionView directionView();
    QuoteView quoteView();
    StatusView statusView();

    // buy if so
    default boolean isWaitingAndShouldBuy(){return quoteView().goodQuote() && statusView().isWaiting() && directionView().isBuy() && askVsLimitView().askBelowLimit();}

    // hold if so
    default boolean isWaitingAndShouldNotBuy(){return quoteView().goodQuote() && statusView().isWaiting() && directionView().isBuy() && askVsLimitView().askAboveLimit();}

    // sell if so
    default boolean isWaitingAndShouldSell(){return quoteView().goodQuote() && statusView().isWaiting() && directionView().isSell() && bidVsLimitView().bidAboveLimit();}

    // hold if so
    default boolean isWaitingAndShouldNotSell(){return quoteView().goodQuote() && statusView().isWaiting() && directionView().isSell() && bidVsLimitView().bidBelowLimit();}

    // stop if so
    default boolean isOpenBuyAndShouldStop(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isBuy() && bidVsStopView().bidBelowStop();}

    // exit if so
    default boolean isOpenBuyAndShouldExit(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isBuy() && bidVsExitView().bidAboveExit();}

    // hold if so
    default boolean isOpenBuyAndShouldWait(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isBuy() && bidVsStopView().bidAboveStop() && bidVsExitView().bidBelowExit();}

    // stop if so
    default boolean isOpenSellAndShouldStop(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isSell() && askVsStopView().askAboveStop();}

    // exit if so
    default boolean isOpenSellAndShouldExit(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isSell() && askVsExitView().askBelowExit();}

    // hold if so
    default boolean isOpenSellAndShouldWait(){return quoteView().goodQuote() && statusView().isOpen() && directionView().isSell() && askVsStopView().askBelowStop() && askVsExitView().askAboveExit();}

    default boolean isBadQuote(){return quoteView().badQuote();}

}

