package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;
import com.cognitionbox.petra.lang.primitives.impls.PString;

@View public interface Todo {
    PString desc();
    PBoolean done();
}
