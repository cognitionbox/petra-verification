package com.cognitionbox.petra.examples.nesting;

import com.cognitionbox.petra.annotations.Primative;

@Primative
public interface FiiView {
    com.cognitionbox.petra.examples.nesting2.Foo foo1();
    com.cognitionbox.petra.examples.nesting.Foo foo2(); // need to refer to this too
    // {a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p} X {a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p}

    default boolean a(){return (foo1().a() || foo1().b() || foo1().c() || foo1().d());}// && foo2().all();}
    default boolean b(){return foo1().e() || foo1().f() || foo1().g() || foo1().h();}// && foo2().all();}
    default boolean c(){return foo1().i() || foo1().j() || foo1().k() || foo1().l();}// && foo2().all();}
    default boolean d(){return foo1().m() || foo1().m() || foo1().o() || foo1().p();}// && foo2.all();}
}
