package com.cognitionbox.petra.examples.coffeemachine;

import com.cognitionbox.petra.annotations.Edge;
import com.cognitionbox.petra.lang.step.PGraph;

import static com.cognitionbox.petra.lang.Petra.*;

public interface ProcessCoffee extends PGraph<CoffeeMachine> {

    @Edge static void init(CoffeeMachine c){
        kases(c,
                kase(coffeeMachine->coffeeMachine.coffeeBag().coffeeBeans().isEmpty(),
                        coffeeMachine->coffeeMachine.coffeeBag().coffeeBeans().forall(b->b.size().get()==8),
                        coffeeMachine->{
                            //c_.coffeeBag().coffeeBeans().add(new Co)
                        })
        );
    }

    static void process(CoffeeMachine c){
        kases(c,
                kase(coffeeMachine->coffeeMachine.coffeeBag().coffeeBeans().forall(b->b.size().get()==8),
                        coffeeMachine->coffeeMachine.coffeeMugs().coffeeMugs().size()==10 &&
                                coffeeMachine.coffeeBag().coffeeBeans().isEmpty(),
                        coffeeMachine->{
                            seqr(coffeeMachine.coffeeBag().coffeeBeans(), ProcessCoffee::grind);
                        })
                );
    }

    static void grind(CoffeeBean b){
        kases(b,
                kase(bean->bean.size().get()>2 && bean.size().get()<=8, bean->bean.size().get()==bean.size().last()/2, bean -> {
                    seq(bean, ProcessCoffee::divideBean);
                    seqr(bean.granuals(), ProcessCoffee::grind);
                }),
                kase(bean->bean.size().get()==2, bean->bean.size().get()==1, bean -> {
                    seq(bean, ProcessCoffee::divideBean);
                })
        );
    }

   @Edge static void divideBean(CoffeeBean b){
        kases(b,
                kase(bean->bean.size().get()>2 && bean.size().get()<=8, bean->bean.size().get()==bean.size().last()/2, bean -> {
                    bean.size().set(bean.size().get()/2);
                    bean.granuals().add(make(CoffeeBean.class));
                    bean.granuals().add(make(CoffeeBean.class));
                })
        );
    }
}
