package com.cognitionbox.petra.examples.coffeemachine;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.collection.PCollection;
import com.cognitionbox.petra.lang.primitives.impls.PDouble;

@View public interface CoffeeBean {
    PDouble size();
    PCollection<CoffeeBean> granuals();
}
