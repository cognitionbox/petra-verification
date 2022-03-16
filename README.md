# petra-verification
The verifier for the Petra programming language.
This is a Java JUnit plugin which reads Petra code and uses formal semantics to verify the code automatically. This plugin will work with your existing Java 8+ build pipelines and replaces non-exhaustive tests with an exhaustive proof of functional correctness (down to but not including edge level code, edge code will be verified in later versions).
In order to help understand this README file it would help to read the README of ```petra```.

## Formal Semantics ##
Petra semantics semantics are given by two separate term rewriting systems, one for translation and the other for execution.
The translation system aims to translate a the graph kases within a Petra program into sequential steps which can
be reasoned about easily. The execution system describes how a Petra program executes and affects its input state, step by step.

### 1. Petra Program (definition) ###
A petra program p, is a tuple:

<img src="https://render.githubusercontent.com/render/math?math=<r, GD,  ED,  RV,  CV,  PV>">

where
r is the root graph definition of entry point,
GD is the set of graph definitions
ED is the set of edge definitions
RV is the set of reference view definitions
CV is the set of collection view definitions
PV is the set of primitive view definitions

### 2. Translation (definition) ###
These rules are syntax pattern matched rules which match/replace graph kase sub-terms, given by a relation
<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{trans}"> where gk is a graph kase,
<img src="https://render.githubusercontent.com/render/math?math=gk \in GK">. 
Please note that the translation rules are mirrored in the Java implementation of Petra's embedded style language. 
This means the concrete execution semantics of a petra program remains consistent through translations, 
which is important as properties proved during translations using conditions on the rewrites will remain during the concrete execution.

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{trans} \ \subset GK \times GK">

### 3. Symbolic Execution (definition) ###
These symbolic execution is given by a relation:

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{sym} \ \subset \Sigma \times \Sigma">

where <img src="https://render.githubusercontent.com/render/math?math=\sigma"> is a tuple,

<img src="https://render.githubusercontent.com/render/math?math=\sigma \in \Sigma">
<img src="https://render.githubusercontent.com/render/math?math=<gk, \alpha, pf>">

gk = a graph kase statement,

<img src="https://render.githubusercontent.com/render/math?math=\alpha"> = symbolic state,
pf = proved flag.

### 4. Concrete Execution (definition) ###
Concrete execution is given by a relation:

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{conc} \ \subset X \times X">

where x is a tuple, <img src="https://render.githubusercontent.com/render/math?math=x \in X">

<img src="https://render.githubusercontent.com/render/math?math=<gk, \gamma, pf>">

gk = a graph kase statement,

<img src="https://render.githubusercontent.com/render/math?math=\gamma"> = concrete state,

pf = proved flag.

### 5. Validity (definition) ###
A Petra program p is valid iff



where,

<img src="https://render.githubusercontent.com/render/math?math=ProgramKases = \{x \mid \forall gd \in GD \ \forall x \in kases(gd)\}">

<img src="https://render.githubusercontent.com/render/math?math=kases: GD \xrightarrow{} K">

<img src="https://render.githubusercontent.com/render/math?math=\xrightarrow{trans}* = \text{is the reflexive transitive closure of} \xrightarrow{trans}">

<img src="https://render.githubusercontent.com/render/math?math=k' \in kase(pre,post,\{jseqs\})">

### 6. Symbolic Reachability (definition) ###

<img src="https://render.githubusercontent.com/render/math?math=\forall k \in ProgramKases \ \forall \alpha \in symbolicProduct(classDefLookup(view(graph(k))) , \\ <k',\alpha,F>\xrightarrow{symb}*<k'',\alpha', T> \ given \ program \ is \ valid.">

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