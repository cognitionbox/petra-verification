package com.cognitionbox.petra.examples.tradingsystem4;

import com.cognitionbox.petra.annotations.Infinite;
import com.cognitionbox.petra.annotations.Root;
import com.cognitionbox.petra.examples.tradingsystem4.decision.RunStrategyAgainstQuote;
import com.cognitionbox.petra.examples.tradingsystem4.decision.TrySubmiting;
import com.cognitionbox.petra.examples.tradingsystem4.strat.*;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

// aim is to create a basic market open momentum strategy
// which flips buying/selling and increases
// stop losses/risk return ratio, on every loss
// idea is that the next trade would cover the losses of the last trades and
// we ride the wins, i.e. the current winning direction.

// kase_1: 1st iteration does setup
// kase_2: subsequent iterations does trading
// forall(t.strategies(),s->s.badQuoteCountIncrementedBy1() ^ ...) is the main effect which evaporates after each iteration

// decisions need to have valid limits
//widen(t, new IsolateNotConnected()); // weakens... removes info
// changes view, only if pre/post conditions are valid wrt to the change, can be used to widen/narrow
// executes all strategies in parallel
// on every iteration need to first check connection, if connected do nothing, else connect
// tradingSystem.brokerConnection().isNotConnected() && tradingSystem.strategies().isEmpty()
// tradingSystem.strategiesNotEmptyAndNotBrokerConnected()
//  cast(tradingSystem,
//      t -> t.strategiesPrepared().prepared() && t.brokerConnection().isConnected(),
//      t -> t.strategiesPrepared().strategies().forall(strategy->strategy.isPrepared()) && t.brokerConnection().isConnected());
/*
 * join(parr(tradingSystem.strategies(), new UpdateStrategyQuote())) ->
 * seqr(tradingSystem.strategies(), new UpdateStrategyQuote())) ->
 * seqr(tradingSystem.strategies(), kases(kase(x->x.a,x->x.b))) / ie does not have the universal quantifier
 * // instead pull out the inside of the universal quantifier from the symbolic state, and rewrite this part from within the symbolic state
 * ie forall x->x.a -> forall x->x.b
 * this will allow us to do conjunction matching comjuncts inside the universal quantifiers. ie we are looking for either conjunctions which can contain disjuncts
 * or only disjunctions.
 */
@Root
@Infinite
public class RunTradingSystem implements Consumer<TradingSystem> {
    public void accept(TradingSystem ts) {
        kases(ts,
                kase(
                        tradingSystem -> tradingSystem.strategiesEmpty(),
                        tradingSystem -> tradingSystem.strategiesPrepared(),
                        tradingSystem -> {
                            seq(tradingSystem, new UpdateMarketData());
                            seq(tradingSystem, new PopulateStrategies());
                        })
                ,
                kase(
                        tradingSystem -> tradingSystem.strategiesPrepared(),
                        tradingSystem -> tradingSystem.strategiesPrepared() && tradingSystem.strategiesEffected(),
                        tradingSystem -> {
                            seq(tradingSystem, new UpdateMarketData());
                            cast(tradingSystem,
                                    t -> t.strategiesPrepared(),
                                    t -> t.strategies().forall(strategy->strategy.parameters().isPrepared()));
                            join(parr(tradingSystem.strategies(), new UpdateStrategyQuote()));
                            // cast below should rewrite into the forall condition
                            castr(tradingSystem.strategies(),
                                    strategy -> strategy.strategyState().goodQuote() ^ strategy.strategyState().badQuote(),
                                    strategy ->
                                            strategy.strategyState().isBadQuote() ^
                                                    strategy.strategyState().isWaitingAndShouldBuy() ^
                                                    strategy.strategyState().isWaitingAndShouldSell() ^
                                                    strategy.strategyState().isWaitingAndShouldNotBuy() ^
                                                    strategy.strategyState().isWaitingAndShouldNotSell() ^
                                                    strategy.strategyState().isOpenBuyAndShouldStop() ^
                                                    strategy.strategyState().isOpenSellAndShouldStop() ^
                                                    strategy.strategyState().isOpenBuyAndShouldExit() ^
                                                    strategy.strategyState().isOpenSellAndShouldExit());
                            // need to allow rewriting into the proposition of the forall on the collection specified,
                            // then we don't need to include  the invariant everywhere
                            join(parr(tradingSystem.strategies().extract(s->s.strategyState()), new RunStrategyAgainstQuote()));
                            join(parr(tradingSystem.strategies(), new TrySubmiting())); // think about how this one rewrites... this one also needs to maintain the invariant as it operates on a higher level so it can affect it
                            cast(tradingSystem,
                                    t->t.strategies().forall(s ->
                                                    s.parameters().isPrepared() && (
                                                        s.strategyState().isDoSubmit() ^
                                                        s.strategyState().isFailedToOpen() ^
                                                        s.strategyState().isOpen() ^
                                                        s.strategyState().isStopped() ^
                                                        s.strategyState().isExited())),
                                    t->t.strategies().forall(s->s.parameters().isPrepared()) &&
                                            t.strategies().forall(s->(
                                                            s.strategyState().isDoSubmit() ^
                                                            s.strategyState().isFailedToOpen() ^
                                                            s.strategyState().isOpen() ^
                                                            s.strategyState().isStopped() ^
                                                            s.strategyState().isExited())));
                            cast(tradingSystem,
                                    t->t.strategies().forall(strategy->strategy.parameters().isPrepared()),
                                    t->t.strategiesPrepared());
                            cast(tradingSystem,
                                    t->t.strategies().forall(s ->
                                                        s.strategyState().isDoSubmit() ^
                                                        s.strategyState().isFailedToOpen() ^
                                                        s.strategyState().isOpen() ^
                                                        s.strategyState().isStopped() ^
                                                        s.strategyState().isExited()),
                                    t->t.strategiesEffected());
                        })
        );
    }
}
