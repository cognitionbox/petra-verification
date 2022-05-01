package com.cognitionbox.petra.examples.todolist;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;
import static com.cognitionbox.petra.lang.Petra.seq;

public interface TodoProcessor extends PGraph<TodoSystem> {
    static void go(TodoSystem t){
        kases(t,
                kase(todoSystem -> !todoSystem.event().isPresent() &&
                                   todoSystem.todoMap().hasId(todoSystem.event().get().id()),
                        todoSystem -> todoSystem.event().isPresent() &&
                                        todoSystem.event().get().isProcessed(),
                        todoSystem -> {
                            seq(todoSystem, TodoProcessor::linkTodoEventWithTodo);
                            seq(todoSystem, TodoProcessor::processEvent);
                        }
                ));
    }

    @Edge static void linkTodoEventWithTodo(TodoSystem t){
        kases(t,
                kase(todoSystem -> (todoSystem.event().get().isEdit() ||
                                   todoSystem.event().get().isDelete()) &&
                                   todoSystem.event().get().todo().isNotPresent() &&
                                   todoSystem.todoMap().hasId(todoSystem.event().get().id()),
                        todoSystem -> todoSystem.event().get().todo().isPresent(),
                        todoSystem -> {
                            Todo todo = todoSystem.todoMap().get(todoSystem.event().get().id());
                            todoSystem.event().get().todo().set(todo);
                        }
                ),
                kase(todoSystem -> todoSystem.event().get().isNew() &&
                                   todoSystem.event().get().todo().isNotPresent(),
                        todoSystem -> todoSystem.event().get().todo().isPresent() &&
                                      todoSystem.todoMap().hasId(todoSystem.event().get().id()),
                        todoSystem -> {
                            Todo todo = make(Todo.class);
                            todoSystem.event().get().todo().set(todo);
                            todoSystem.todoMap().events().put(todoSystem.idGenerator().incrementAndGet(),todo);
                            System.out.println("TodoId: "+todoSystem.event().get().id());
                        }
                ));
    }

    @Edge static void processEvent(TodoSystem t){
        kases(t,
                kase(todoSystem -> todoSystem.event().get().isEdit() &&
                                todoSystem.event().get().todo().isPresent() &&
                                todoSystem.event().get().todo().get().desc().eq(todoSystem.event().get().descUpdate()),
                        todoSystem ->
                                todoSystem.event().get().isProcessed() &&
                                todoSystem.event().get().todo().get().desc().isNotChanged(),
                        todoSystem -> {
                            System.out.println("redundant update as desc is the same! "+todoSystem.event().get().todo());
                        }
                ),
                kase(todoSystem -> todoSystem.event().get().isEdit() &&
                                todoSystem.event().get().todo().isPresent() &&
                                todoSystem.event().get().todo().get().desc().neq(todoSystem.event().get().descUpdate()),
                        todoSystem -> todoSystem.event().get().isProcessed() &&
                                todoSystem.event().get().todo().get().desc().isChanged(),
                        todoSystem -> {
                            todoSystem.event().get().todo().get().desc().set(todoSystem.event().get().descUpdate().get());
                            todoSystem.event().get().processed().set(true);
                            System.out.println("Todo edited! "+todoSystem.event().get().todo());
                        }
                ),
                kase(todoSystem -> todoSystem.event().isPresent() &&
                                todoSystem.event().get().isDelete(),
                        todoSystem -> todoSystem.event().get().isProcessed() &&
                                !todoSystem.todoMap().hasId(todoSystem.event().get().id()),
                        todoSystem -> {
                            int id = todoSystem.event().get().id();
                            todoSystem.todoMap().events().remove(id);
                            todoSystem.event().get().processed().set(true);
                            System.out.println("Todo deleted! "+todoSystem.event().get().todo());
                        }
                ),
                kase(todoSystem -> todoSystem.event().get().event().isPresent() &&
                                todoSystem.event().get().isDone(),
                     todoSystem -> todoSystem.event().get().isProcessed(),
                     todoSystem -> {
                         todoSystem.event().get().todo().get().done().set(true);
                         todoSystem.event().get().processed().set(true);
                         System.out.println("Todo completed! "+todoSystem.event().get().todo());
                     }
                ));
    }
}
