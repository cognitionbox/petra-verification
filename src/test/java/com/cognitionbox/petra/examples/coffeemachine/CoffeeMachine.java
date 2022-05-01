package com.cognitionbox.petra.examples.coffeemachine;

import com.cognitionbox.petra.annotations.View;

@View
public interface CoffeeMachine {
    CoffeeBag coffeeBag();
    CoffeePile coffeePile();
    CoffeeMugs coffeeMugs();
}
