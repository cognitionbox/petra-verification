package com.cognitionbox.petra.examples.tradingsystem4.decision;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.examples.tradingsystem4.marketdata.igindex.IGindexHelper;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

@Edge
public class TrySubmiting implements Consumer<Strategy> {
    @Override
    public void accept(Strategy s) {
        // PExternalEdge must have at least one kase with attempt > 1
        // strategy.holdCountIncBy1 is just a atomic prop like others in edges, but special kind that can do deltas
//        kase(decision,
//                strategy->strategy.doSubmit(),
//                strategy->strategy.successCountIncrementedBy1() ^ strategy.failedCountIncrementedBy1(),
//                10,
//                100,
//                strategy-> {
//                    // submit using non petra code, e.g. client call to webserver
//                    strategy.successCount.next.set(strategy.successCount.current.get()+1);
//                    strategy.successCount.current.set(strategy.successCount.next.get());
//                },
//                strategy->{
//                    // trusted deterministic code only, as no retries
//                    strategy.failedCount.next.set(strategy.failedCount.current.get()+1);
//                    strategy.failedCount.current.set(strategy.failedCount.next.get());
//                    System.out.println("holding...");
//                });
        kases(s,
            kase(
                strategy->strategy.isDoSubmit(),
                strategy->strategy.isOpen() ^ strategy.isFailedToOpen(),
                strategy-> {
                    String[] res = IGindexHelper
                            .createPosition(IGindexHelper.getCst(),
                                    IGindexHelper.getX_security_token(),
                                    strategy.parameters().instrument().get().epic,
                                    strategy.strategyState().direction().get().name(),
                                    strategy.strategyState().limit().get(),
                                    1.0,
                                    strategy.strategyState().exit().get(),
                                    strategy.strategyState().stop().get()
                                    );
                    if (res[0].equals("200")){
                        String[] res2 = IGindexHelper.confirmTrade(IGindexHelper.getCst(),
                                IGindexHelper.getX_security_token(),
                                res[3]);
                        if (res2[0].equals("200") && res[3].equals("ACCEPTED")){
                            // submit using non petra code, e.g. client call to webserver
                            strategy.countsView().successCount.set(strategy.countsView().successCount.get()+1);
                            return;
                        }
                    }
                    strategy.countsView().failedCount.set(strategy.countsView().failedCount.get()+1);
                }));
    }
}
