package com.cognitionbox.petra.examples.todolist;


import com.cognitionbox.petra.examples.coffeemachine.CoffeeMachine;
import com.cognitionbox.petra.examples.coffeemachine.ProcessCoffee;
import com.cognitionbox.petra.lang.Petra;
import com.cognitionbox.petra.lang.collection.PQueue;

import static com.cognitionbox.petra.lang.Petra.make;

public class TodoMain {
    public static void main(String... args){
        TodoSystem system = make(TodoSystem.class);
        PQueue<TodoEvent> events = new PQueue<>();
        events.stream().forEach(e->{
            system.event().set(e);
            Petra.start(TodoProcessor::go, system);
        });
    }
}
