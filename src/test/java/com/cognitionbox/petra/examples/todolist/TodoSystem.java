package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.PValue;

import java.util.concurrent.atomic.AtomicInteger;

@View public interface TodoSystem {
    AtomicInteger idGenerator();
    PValue<TodoEvent> event();
    TodosMap todoMap();
    // TodoEvents X TodosMap = 6,
}
