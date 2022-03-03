package com.cognitionbox.petra.examples.algorithmictrading.marketdata;

import com.cognitionbox.petra.annotations.Graph;
import com.cognitionbox.petra.examples.algorithmictrading.system.ModeWithDecisionsWithQuotes;

import java.util.function.Consumer;

import static com.cognitionbox.petra.lang.Petra.*;

@Graph
public class UpdateDecisionQuote implements Consumer<ModeWithDecisionsWithQuotes> {
    @Override
    public void accept(ModeWithDecisionsWithQuotes m) {
        kases(m,
                kase(modeWithDecisionsWithQuotes->
                        modeWithDecisionsWithQuotes.mode().isHistoricalMode(),
                     modeWithDecisionsWithQuotes->
                        modeWithDecisionsWithQuotes.mode().isHistoricalMode() &&
                        modeWithDecisionsWithQuotes.decision().bid().isUpdated() &&
                        modeWithDecisionsWithQuotes.decision().ask().isUpdated(),
                        modeWithDecisionsWithQuotes->{
                            seq((DecisionWithQuotes)modeWithDecisionsWithQuotes, new UpdateDecisionQuoteUsingHistoricalCsvData());
                        }),
                kase(modeWithDecisionsWithQuotes->
                                modeWithDecisionsWithQuotes.mode().isLiveMode(),
                        modeWithDecisionsWithQuotes->
                                modeWithDecisionsWithQuotes.mode().isLiveMode() &&
                                        modeWithDecisionsWithQuotes.decision().bid().isUpdated() &&
                                        modeWithDecisionsWithQuotes.decision().ask().isUpdated(),
                        modeWithDecisionsWithQuotes->{
                            seq((DecisionWithQuotes)modeWithDecisionsWithQuotes, new UpdateDecisionQuoteUsingLiveData());
                        })
        );
    }
}
