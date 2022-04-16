package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.annotations.Primative;
import com.cognitionbox.petra.annotations.View;

@Primative @View
public interface FiiView {
    Foo foo1();
    Foo foo2(); // need to refer to this too
    // {a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p} X {a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p}

    default boolean a(){return (foo1().a() || foo1().b() || foo1().c() || foo1().d());}// && foo2().all();}
    default boolean b(){return foo1().e() || foo1().f() || foo1().g() || foo1().h();}// && foo2().all();}
    default boolean c(){return true;}// && foo2().all();}
    default boolean d(){return true;}// && foo2.all();}
}
