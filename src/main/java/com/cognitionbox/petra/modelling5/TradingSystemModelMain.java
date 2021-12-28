package com.cognitionbox.petra.modelling5;

import static com.cognitionbox.petra.modelling5.MODELLING.*;

public class TradingSystemModelMain {
    public static void main(String[] args){
        SYSTEM system = system("TRADING");

        STATE strategiesEmpty = state("strategiesEmpty");
        STATE strategiesPrepared = state("strategiesPrepared");
        STATE strategiesView = xor(strategiesEmpty,strategiesPrepared);

        STATE goodQuote = state("goodQuote");
        STATE badQuote = state("badQuote");
        STATE quoteView = xor(goodQuote,badQuote);

        STATE isBuy = state("isBuy");
        STATE isSell = state("isSell");
        STATE directionView = xor(isBuy,isSell);

        STATE isWaiting = state("isWaiting");
        STATE isDoSubmit = state("isDoSubmit");
        STATE isFailedToOpen = state("isFailedToOpen");
        STATE isOpen = state("isOpen");
        STATE isStopped = state("isStopped");
        STATE isExited = state("isExited");
        STATE statusView = xor(isWaiting,isDoSubmit,isFailedToOpen,isOpen,isStopped,isExited);

        STATE askAboveLimit = state("askAboveLimit");
        STATE askBelowLimit = state("askBelowLimit");
        STATE askVsLimitView = xor(askAboveLimit,askBelowLimit);

        STATE isWaitingAndBuy =  isWaiting.with(isBuy).with(askVsLimitView);

        STATE bidAboveLimit = state("bidAboveLimit");
        STATE bidBelowLimit = state("bidBelowLimit");
        STATE bidVsLimitView = xor(bidAboveLimit,bidBelowLimit);

        STATE isWaitingAndSell =  isWaiting.with(isSell).with(bidVsLimitView);

        STATE bidAboveExit = state("bidAboveExit");
        STATE bidBelowExit = state("bidBelowExit");
        STATE bidVsExitView = xor(bidAboveExit,bidBelowExit);

        STATE askAboveExit = state("askAboveExit");
        STATE askBelowExit = state("askBelowExit");
        STATE askVsExitView = xor(askAboveExit,askBelowExit);

        STATE bidAboveStop = state("bidAboveStop");
        STATE bidBelowStop = state("bidBelowStop");
        STATE bidVsStopView = xor(bidAboveStop,bidBelowStop);

        STATE askAboveStop = state("askAboveStop");
        STATE askBelowStop = state("askBelowStop");
        STATE askVsStopView = xor(askAboveStop,askBelowStop);

        STATE isOpenAndBuy =  isOpen.with(isBuy).with(bidVsExitView).with(bidVsStopView);

        STATE isOpenAndSell =  isOpen.with(isSell).with(askVsExitView).with(askVsStopView);

        STATE strategyDecisionStates =
                quoteView
                        .with(directionView)
                        .with(statusView)
                        .with(bidVsLimitView)
                        .with(askVsLimitView)
                        .with(bidVsExitView)
                        .with(askVsExitView)
                        .with(bidVsStopView)
                        .with(askVsStopView);

        STATE isWaitingAndShouldBuy = state("isWaitingAndShouldBuy");
        STATE isWaitingAndShouldNotBuy = state("isWaitingAndShouldNotBuy");
        STATE isWaitingAndShouldSell = state("isWaitingAndShouldSell");
        STATE isWaitingAndShouldNotSell = state("isWaitingAndShouldNotSell");
        STATE isOpenBuyAndShouldStop = state("isOpenBuyAndShouldStop");
        STATE isOpenBuyAndShouldExit = state("isOpenBuyAndShouldExit");
        STATE isOpenBuyAndShouldWait = state("isOpenBuyAndShouldWait");
        STATE isOpenSellAndShouldStop = state("isOpenSellAndShouldStop");
        STATE isOpenSellAndShouldExit = state("isOpenSellAndShouldExit");
        STATE isOpenSellAndShouldWait = state("isOpenSellAndShouldWait");
        STATE isBadQuote = state("isBadQuote");

        STATE strategyDecisionStatesView =
                xor(isBadQuote,
                        isWaitingAndShouldBuy,
                        isWaitingAndShouldNotBuy,
                        isWaitingAndShouldSell,
                        isWaitingAndShouldNotSell,
                        isOpenBuyAndShouldStop,
                        isOpenBuyAndShouldExit,
                        isOpenBuyAndShouldWait,
                        isOpenSellAndShouldStop,
                        isOpenSellAndShouldExit,
                        isOpenSellAndShouldWait);

        ABS abs = strategyDecisionStates.abs(strategyDecisionStatesView,
                a->a.symbol.contains("isBadQuote"), b->!b.symbol.contains("isBadQuote"));
        //system.add(abs);
//        system.add(strategyDecisionStates);
//        system.add(strategyDecisionStatesView);

        system.add(isWaitingAndBuy);
        system.add(isOpenAndBuy);
        system.add(isWaitingAndSell);
        system.add(isOpenAndSell);


        system.renderStates();
        System.out.println(system.verifyStates());
    }
}
