package com.cognitionbox.petra.examples.coffeemachine;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.collection.PCollection;

@View public interface CoffeeMugs {
    PCollection<CoffeeMug> coffeeMugs();
}
