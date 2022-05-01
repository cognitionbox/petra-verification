package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.kase;
import static com.cognitionbox.petra.lang.Petra.kases;

public interface Edit extends PGraph<Todo> {
    static void process(Todo t){
        kases(t,
                kase(todo->true, todo->true,
                     todo -> {

                    }
                ));
    }
}
