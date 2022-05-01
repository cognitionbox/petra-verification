package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.collection.PQueue;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;

import java.util.Map;

@View public interface TodosMap {
    Map<Integer, Todo> events();

    default Todo get(int id){
        return events().get(id);
    }

    default boolean hasId(int id){
        return events().containsKey(id);
    }
}
