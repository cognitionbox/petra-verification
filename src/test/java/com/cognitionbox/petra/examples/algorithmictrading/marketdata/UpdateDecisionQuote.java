package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.examples.algorithmictrading.system.ModeWithDecisionsWithQuotes;
import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public interface UpdateDecisionQuote extends PEdge<ModeWithDecisionsWithQuotes> {
    static  void accept(ModeWithDecisionsWithQuotes m) {
        kases(m,
                kase(modeWithDecisionsWithQuotes->
                        modeWithDecisionsWithQuotes.mode().isHistoricalMode(),
                     modeWithDecisionsWithQuotes->
                        modeWithDecisionsWithQuotes.mode().isHistoricalMode() &&
                        modeWithDecisionsWithQuotes.decision().bid().isUpdated() &&
                        modeWithDecisionsWithQuotes.decision().ask().isUpdated(),
                        modeWithDecisionsWithQuotes->{
                            seq((DecisionWithQuotes)modeWithDecisionsWithQuotes, UpdateDecisionQuoteUsingHistoricalCsvData::accept);
                        }),
                kase(modeWithDecisionsWithQuotes->
                                modeWithDecisionsWithQuotes.mode().isLiveMode(),
                        modeWithDecisionsWithQuotes->
                                modeWithDecisionsWithQuotes.mode().isLiveMode() &&
                                        modeWithDecisionsWithQuotes.decision().bid().isUpdated() &&
                                        modeWithDecisionsWithQuotes.decision().ask().isUpdated(),
                        modeWithDecisionsWithQuotes->{
                            seq((DecisionWithQuotes)modeWithDecisionsWithQuotes, UpdateDecisionQuoteUsingLiveData::accept);
                        })
        );
    }
}
