package com.cognitionbox.petra.examples.nesting2;

public class Foo {
    final Fum fum1 = new Fum();
    final Fum fum2 = new Fum();

    // {a(),b(),c(),d()} X
    // {a(),b(),c(),d()}

    // special proposition, which does not have to be XOR from the others, its the Truth of the type
    // this will be contructed in the type system
//    public boolean top(){
//        return a() ^ b() ^ c() ^ d() ^ e() ^ f() ^ g() ^ h() ^ i() ^ j() ^ k() ^ l() ^ m() ^ n() ^ o() ^ p();
//    }

    public boolean a(){ return fum1.a() ^ fum2.a(); }
    public boolean b(){ return fum1.a() ^ fum2.b(); }
    public boolean c(){ return fum1.a() ^ fum2.c(); }
    public boolean d(){ return fum1.a() ^ fum2.d(); }
    public boolean e(){ return fum1.b() ^ fum2.a(); }
    public boolean f(){ return fum1.b() ^ fum2.b(); }
    public boolean g(){ return fum1.b() ^ fum2.c(); }
    public boolean h(){ return fum1.b() ^ fum2.d(); }
    public boolean i(){ return fum1.c() ^ fum2.a(); }
    public boolean j(){ return fum1.c() ^ fum2.b(); }
    public boolean k(){ return fum1.c() ^ fum2.c(); }
    public boolean l(){ return fum1.c() ^ fum2.d(); }
    public boolean m(){ return fum1.d() ^ fum2.a(); }
    public boolean n(){ return fum1.d() ^ fum2.b(); }
    public boolean o(){ return fum1.d() ^ fum2.c(); }
    public boolean p(){ return fum1.d() ^ fum2.d(); }

//    public boolean r(){
//        return fum1.a() || fum1.b();
//    }
//
//    public boolean s(){
//        return fum1.c() || fum1.d();
//    }
}
