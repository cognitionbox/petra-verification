# petra-verification
The verifier for the Petra programming language.
This is a Java JUnit plugin which reads Petra Core code and uses formal semantics to verify the code automatically. This plugin will work with your existing Java 8+ build pipelines and replaces non-exhaustive tests with an exhaustive proof of functional correctness (down to but not including edge level code, edge code will be verified in later versions).
In order to help understand this README file it would help to read the README of ```petra```.

## Formal Semantics ##
Petra semantics semantics are given by two separate term rewriting systems, one for translation and the other for execution.
The translation system aims to translate a the graph kases within a Petra program into Petra Runtime Language. 
The execution system describes how a Petra program executes and affects its input state, step by step.

### Petra Program ###
A petra program p, is a tuple:

<img src="https://render.githubusercontent.com/render/math?math=<r, GD,  ED,  RV,  CV,  PV>">

where
r is the root graph definition of entry point,
GD is the set of graph definitions
ED is the set of edge definitions
RV is the set of reference view definitions
CV is the set of collection view definitions
PV is the set of primitive view definitions

### Translation ###
These rules are syntax pattern matched rules which match/replace graph kase sub-terms, given by a relation
<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{trans}"> where gk is a graph kase,
<img src="https://render.githubusercontent.com/render/math?math=gk \in GK">. 
Please note that the translation rules are mirrored in the Java implementation of Petra's embedded style language. 
This means the concrete execution semantics of a petra program remains consistent through translations, 
which is important as properties proved during translations using conditions on the rewrites will remain during the concrete execution.

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{trans} \ \subset GK \times GK">

### Symbolic Execution ###
These symbolic execution is given by a relation:

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{sym} \ \subset \Sigma \times \Sigma">

where <img src="https://render.githubusercontent.com/render/math?math=\sigma"> is a tuple,

<img src="https://render.githubusercontent.com/render/math?math=\sigma \in \Sigma">
<img src="https://render.githubusercontent.com/render/math?math=<gk, \alpha, pf>">

gk = a graph kase statement,

<img src="https://render.githubusercontent.com/render/math?math=\alpha"> = symbolic state,
pf = proved flag.

### Concrete Execution ###
Concrete execution is given by a relation:

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{conc} \ \subset X \times X">

where x is a tuple, <img src="https://render.githubusercontent.com/render/math?math=x \in X">

<img src="https://render.githubusercontent.com/render/math?math=<gk, \gamma, pf>">

gk = a graph kase statement,

<img src="https://render.githubusercontent.com/render/math?math=\gamma"> = concrete state,

pf = proved flag.

### Validity ###
A Petra program p is valid iff

<img src="https://render.githubusercontent.com/render/math?math=\forall gd \in GD \ \forall k \in kases(gd) , \ k \xrightarrow{trans}* \ k'">

### Symbolic Reachability ###

<img src="https://render.githubusercontent.com/render/math?math=\forall k \in kasesRecursively(r) \ \forall \alpha \in \mathrm{A}(view(graph(k))) , \ <k',\alpha,F>\xrightarrow{symb}*<k'',\alpha', T> \ given, \ k \xrightarrow{trans}* \ k'">

### Functional Correctness ###
In general functional correctness is the correct input/output behavior of an algorithm, i.e. for each input there is an output which satisfies the specification.
Below we define Petra's version of functional correctness which is a stronger statement.
For all runs of the program p, for all kases in the root graph r, for all concrete states <img src="https://render.githubusercontent.com/render/math?math=\gamma"> in the data type of r,
<img src="https://render.githubusercontent.com/render/math?math=\gamma"> must be transformed into some <img src="https://render.githubusercontent.com/render/math?math=\gamma'">which satisfies the post condition of the kase, without at any point, pausing due to a programmed condition and
the <img src="https://render.githubusercontent.com/render/math?math=\gamma"> must be transformed into the same <img src="https://render.githubusercontent.com/render/math?math=\gamma'"> for every run.

<img src="https://render.githubusercontent.com/render/math?math=\forall \rho \in \Rho \ \forall k \in kases(r) \ \forall \gamma \in \Gamma(data(graph(k))) , \ <k',\gamma,F>\xrightarrow{conc}*<k'',\gamma', T> \ given, \ k \xrightarrow{trans}* \ k'">

This implies requirements of Dead-lock/Live-lock/Starvation freedom.

## Build & Install ##
Clone the following repos:

```git clone https://github.com/cognitionbox/petra.git```

```git clone https://github.com/cognitionbox/petra-verification.git```

Then run ```mvn clean install``` for both the projects, starting with ```petra```.

## Test ##
The tests are some examples which are present in the ```petra-examples``` project.
These are to be removed as the ```petra-examples``` will be used to test the ```petra-verification``` in addition
to unit tests which have yet to be added to ```petra-verification```.
The tests within this project can only be run one at a time as there are static variables in use within the verification system. 
You can either run the tests from within your IDE or using maven like so ```mvn clean test -Dclass=<VERIFICATION_CLASS_TO_TEST>``` e.g.
```mvn clean test -Dclass=LicenseDeletionVerification```.

## Verify ##
To try using the verification system from within another project clone the ```petra-examples``` repo:

```git clone https://github.com/cognitionbox/petra-examples.git```

Then run ```mvn clean test``` within this project to trigger the verification process on the sub-projects within the repo.
Testing multiple projects in one go works as testing each project happens within a new process and therefore there is no issue with the use of static variables as mentioned previously.
Please note the ```cyclic-system``` does not currently support verification and therefore this project is skipped within the verification process.