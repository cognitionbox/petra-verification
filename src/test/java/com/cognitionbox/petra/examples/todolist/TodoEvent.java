package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.annotations.View;
import com.cognitionbox.petra.lang.primitives.PValue;
import com.cognitionbox.petra.lang.primitives.impls.PBoolean;
import com.cognitionbox.petra.lang.primitives.impls.PInteger;
import com.cognitionbox.petra.lang.primitives.impls.PString;

@View public interface TodoEvent {
    PValue<TodoEventTypes> event();
    PString descUpdate();
    PInteger todoId();
    PValue<Todo> todo();
    PBoolean processed();

    default boolean isNew(){
        return event().get()==TodoEventTypes.NEW;
    }

    default boolean isEdit(){
        return event().get()==TodoEventTypes.EDIT;
    }

    default boolean isDelete(){
        return event().get()==TodoEventTypes.DELETE;
    }

    default boolean isDone(){
        return event().get()==TodoEventTypes.DONE;
    }

    default boolean isProcessed(){
        return processed().get();
    }

    default boolean isNotProcessed(){
        return !isProcessed();
    }

    default int id(){
        return todoId().get();
    }
}
