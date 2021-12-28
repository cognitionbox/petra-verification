package com.cognitionbox.petra.examples.nesting;

public interface SomeView {
    FeeView fee1();
    FeeView fee2();
    //{a,b} X {a,b} = {(a,a),(a,b),(b,b),(b,a)}
    default boolean x(){return fee1().a();}
    default boolean y(){return fee1().b();}
    //default boolean z(){return fee1().a() && fee2().b();}
}
