package com.cognitionbox.petra.examples.nesting;

public interface SomeView2 {
    FeeView fee1();
    FeeView fee2();

    default boolean w(){return fee1().a() && fee2().a();}
    default boolean x(){return fee1().a() && fee2().b();}
    default boolean y(){return fee1().b() && fee2().a();}
    default boolean z(){return fee1().b() && fee2().b();}
}
