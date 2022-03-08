 /*
  * CONFIDENTIAL - COGNITION BOX LTD.
  *
  * Copyright 2016-2022 Cognition Box Ltd. - All Rights Reserved
  *
  * This file is part of "petra-verification-0.1.0-alpha.jar".
  * "petra-verification-0.1.0-alpha.jar" is owned by:
  *
  * Cognition Box Ltd. (10194162)
  * 9 Grovelands Road,
  * Palmers Green,
  * London, N13 4RJ
  * England.
  *
  * All information contained herein is, and remains the property of
  * Cognition Box Ltd. The intellectual and technical concepts contained
  * herein are proprietary to Cognition Box Ltd. and may be covered
  * by patents in process, and are protected by trade secret or
  * copyright law. Dissemination of this information or reproduction
  * of this material is strictly forbidden unless prior written
  * permission is obtained from Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" includes trade secrets of Cognition Box Ltd.
  * In order to protect "petra-verification-0.1.0-alpha.jar", You shall not decompile, reverse engineer, decrypt,
  * extract or disassemble "petra-verification-0.1.0-alpha.jar" or otherwise reduce or attempt to reduce any software
  * in "petra-verification-0.1.0-alpha.jar" to source code form. You shall ensure, both during and
  * (if you still have possession of "petra-verification-0.1.0-alpha.jar") after the performance of this Agreement,
  * that (i) persons who are not bound by a confidentiality agreement consistent with this Agreement
  * shall not have access to "petra-verification-0.1.0-alpha.jar" and (ii) persons who are so bound are put on written notice that
  * "petra-verification-0.1.0-alpha.jar" contains trade secrets, owned by and proprietary to Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" is Proprietary and Confidential.
  * Unauthorized copying of "petra-verification-0.1.0-alpha.jar" files, via any medium is strictly prohibited.
  *
  * "petra-verification-0.1.0-alpha.jar" can not be copied and/or distributed without the express
  * permission of Cognition Box Ltd.
  *
  * "petra-verification-0.1.0-alpha.jar" is written by Aran Hakki.
  */

package com.cognitionbox.petra.verification;

import com.cognitionbox.petra.annotations.*;
import com.cognitionbox.petra.lang.step.PEdge;
import com.cognitionbox.petra.lang.step.PGraph;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.Pair;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import net.openhft.compiler.CompilerUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PetraProgram {

    final static Logger LOG = new Logger();

    static final String rootDir = new File("./").getPath();
    static String entryPointPackageName;
    static String rootGraphName;
    static final Map<Class,Class> viewImplementation = new HashMap<>();
    static final Map<Class,Integer> viewImplementationCount = new HashMap<>();
    static final Map<Class,CompilationUnitWithData> all = new HashMap<>();
    static final Map<Class,DataTypeInfo> dataTypeInfoMap = new HashMap<>();

    static void parseSrcFiles(){
        Path start = Paths.get(rootDir);
        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            stream
                    .filter(i->i.getFileName().toString().contains(".java"))
                    .forEach(path->{
                        JavaParser jp = new JavaParser();
                        try {
                            ParseResult<CompilationUnit> pr = jp.parse(path);
                            if (pr.isSuccessful()){
                                Class clazz = null;
                                try {
                                    clazz = Class.forName(pr.getResult().get().getPackageDeclaration().get().getName()+"."+pr.getResult().get().getPrimaryTypeName().get().toString());
                                    if (!clazz.getPackage().toString().contains(entryPointPackageName)){
                                        return;
                                    }
                                } catch (NoSuchElementException e){
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                if (Consumer.class.isAssignableFrom(clazz)){
                                    CompilationUnitWithData cu = new CompilationUnitWithData(path,clazz,pr.getResult().get());
                                    if (PEdge.class.isAssignableFrom(clazz) && Consumer.class.isAssignableFrom(clazz)){
                                        cu.isEdge = true;
                                        cu.isProved = true;
                                    } else if (PGraph.class.isAssignableFrom(clazz) && Consumer.class.isAssignableFrom(clazz)){
                                        cu.isEdge = false;
                                    }
                                    all.put(clazz,cu);
                                } else {
                                    // accumulates the number of implementations for each @View
                                    for (Class c : clazz.getInterfaces()){
                                        if (c.isAnnotationPresent(View.class)){
                                            if (viewImplementationCount.containsKey(c)){
                                                viewImplementationCount.put(c,viewImplementationCount.get(c));
                                            } else {
                                                viewImplementation.put(c,clazz);
                                                viewImplementationCount.put(c,1);
                                            }
                                        }
                                    }
                                    CompilationUnitWithData cu = new CompilationUnitWithData(path,clazz,pr.getResult().get());
                                    all.put(clazz,cu);
                                }
                            }
                        } catch (IOException e) {
                            LOG.debug("Cannot process: "+e.getMessage());
                        }
                    });
            if (viewImplementationCount.values().stream().anyMatch(i->i>1)){
                throw new IllegalStateException("Cannot have more than one implementation per view.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isViewOfImplementation(Class impl, Class view){
        return Arrays.asList(impl.getInterfaces()).stream().anyMatch(i->i.isAnnotationPresent(View.class) && i.equals(view));
    }

    private static Class getImplementationOfView(Class view){
        return viewImplementation.get(view);
    }

    static SymbolicState getViewTruth(CompilationUnit cu, Class clazz) throws ClassNotFoundException {
        int i = 0;
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().filter(m->!m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        Set<String>[] methodNames = null;
        if (fields.size()==1 && !fields.get(0).isDefault() && Collection.class.isAssignableFrom(fields.get(0).getReturnType())) {
            // check defaults of view
            Class<?> elementType = (Class<?>) ((ParameterizedType) fields.get(0).getGenericReturnType()).getActualTypeArguments()[0];
            CompilationUnitWithData cu2 = all.get(elementType);
            ClassOrInterfaceDeclaration declaration = cu2.compilationUnit.getInterfaceByName(elementType.getSimpleName()).get();
            methodNames = new Set[1];
            methodNames[0] = declaration.getMethods().stream().filter(m->m.getType().asString().equals("boolean")).map(m->m.asMethodDeclaration().getName().toString()).collect(Collectors.toSet());
            methodNames[0].add("isEmpty");
            Set<List<String>> truth = Sets.cartesianProduct(methodNames);
            return new SymbolicState(truth,true);
        } else {
            methodNames = new Set[fields.size()];
            for (Class<?> t : fields.stream().map(f->f.getReturnType()).collect(Collectors.toList())){
                if (t.getSimpleName().contains("Collection")){
                    continue;
                } else {
                    Class cls = getClassIfSimpleNameIsUniqueInPackage(t.getSimpleName());
                    CompilationUnitWithData cu2 = all.get(cls);
                    ClassOrInterfaceDeclaration declaration = cu2.compilationUnit.getInterfaceByName(t.getSimpleName()).get();
                    methodNames[i] = declaration.getMethods().stream().filter(m->m.getType().asString().equals("boolean")).map(m->m.asMethodDeclaration().getName().toString()).collect(Collectors.toSet());
                    i++;
                }
            }
            Set<List<String>> truth = Sets.cartesianProduct(methodNames);
            return new SymbolicState(truth,false);
        }
    }

    static Set<List<String>> filterStatesUsingBooleanPrecondition(Set<List<String>> states, boolean forall, String expression, Class clazz){
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().filter(m->!m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        String r = expression;
        r = r.replaceAll("\\.",".equals(\"");
        if (forall){
            String newP = r.replaceAll(fields.get(0).getName()+"\\(\\)","list.get("+0+")");
            if (r.equals(newP)){
                Class<?> elementType = (Class<?>) ((ParameterizedType) fields.get(0).getGenericReturnType()).getActualTypeArguments()[0];
                String mn = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,elementType.getSimpleName());
                r = r.replaceAll(mn+"\\.","list.get("+0+")\\.");
                r = r.replaceAll("\\(\\)","\")");
            } else {
                r = newP.replaceAll("\\(\\)","\")");
            }
        } else {
            List<Integer> indicies = new ArrayList<>();
            int count = 0;
            for (Method f : fields){
                if (r.contains(f.getName())){
                    indicies.add(count);
                }
                count++;
            }
            for (Integer x : indicies){
                r = r.replaceAll(fields.get(x).getName()+"\\(\\)","list.get("+x+")");
            }
            r = r.replaceAll("\\(\\)","\")");
        }
        r = "return "+r+";";
        String uuid = "Precondition_"+UUID.randomUUID().toString().replaceAll("-","_");
        String predicateSrc = "package com.cognitiobox.petra.codegen; import java.util.List; import java.util.function.Predicate; public class "+uuid+" implements Predicate<List<String>> { public boolean test(List<String> list){"+r+"}}";

        try {
            Class generatedClazz = CompilerUtils.CACHED_COMPILER.loadFromJava("com.cognitiobox.petra.codegen."+uuid, predicateSrc);
            Predicate<List<String>> pred = (Predicate<List<String>>) generatedClazz.newInstance();
            return states.stream().filter(pred).collect(Collectors.toSet());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    static private boolean overridesMethodFromBaseInterface(Class clazz){
        if (clazz.getInterfaces().length>0){
            Class base = clazz.getInterfaces()[0];
            for (Method c : clazz.getDeclaredMethods()){
                for (Method b : base.getDeclaredMethods()){
                    if (c.equals(b)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isViewSoundAndComplete(CompilationUnit cu, Class clazz, ClassOrInterfaceDeclaration c) throws ClassNotFoundException {
                int i = 0;
                // field methods need to be declared in alphabetical order
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().sorted(Comparator.comparing(Method::getName)).filter(m->!m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        // use this one instead.
        List<MethodDeclaration> fieldMethodDeclarations = c.getMethods().stream().filter(m->!m.isDefault() && !m.getType().asString().equals("boolean")).collect(Collectors.toList());

        if (!fields.stream().map(f->f.getName()).collect(Collectors.toList()).equals(
                fieldMethodDeclarations.stream().map(f->f.getName().asString()).collect(Collectors.toList()))
        ){
            throw new IllegalStateException("fields of view must be declared in alphabetical order.");
        }

        if (fields.size()==1 && !fields.get(0).isDefault() && Collection.class.isAssignableFrom(fields.get(0).getReturnType())){
            // check defaults of view
            Class<?> elementType = (Class<?>)((ParameterizedType)fields.get(0).getGenericReturnType()).getActualTypeArguments()[0];
            List<Method> elementTypeProps = Arrays.asList(elementType.getMethods()).stream().filter(m->m.isDefault() && m.getReturnType().equals(boolean.class)).collect(Collectors.toList());

            List<Method> viewProps = Arrays.asList(clazz.getMethods()).stream().filter(m->m.isDefault() && m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
            Set<String> elementTypePropsNames = elementTypeProps.stream().map(e->e.getName()).collect(Collectors.toSet());
            Set<Set<String>> ps = Sets.powerSet(elementTypePropsNames);
            List<List<String>> orderedPs = ps
                            .stream()
                            .sorted((a,b)->Integer.compare(a.size(),b.size())==0?a.toString().compareTo(b.toString()):Integer.compare(a.size(),b.size()))
                            .map(ss->ss.stream()
                                    .sorted((a,b)->a.toString().compareTo(b.toString()))
                                    .collect(Collectors.toList()))
                            .collect(Collectors.toList());
            Set<String> viewPropsNamesSet = viewProps.stream().map(m->m.getName()).collect(Collectors.toSet());
            viewPropsNamesSet.remove("isEmpty");
            // Now we don't have to declare all these methods like with the non-collection view types
            boolean collectionViewOk = true;
            String invalidCollectionViewPropositionName = null;
            String invalidCollectionViewPropositionImpl = null;
            String matchedVar = null;
            String matchedCollectionVar = null;
            for (String m : viewPropsNamesSet){
                String impl = getImplementation(m,all.get(clazz)).replaceAll(" ","");
                String[] split = impl.split("->");
                String collectionVar = split[0].split("\\.")[0].replaceAll("\\(\\)","");
                String var = split[1].split("\\.")[0];
                impl = split[1].replaceAll("\\(","").replaceAll("\\)","");

                LOG.info(m+":\nforall "+var+" in "+collectionVar+", "+impl
                        .replaceAll("\\^"," XOR ")
                        .replaceAll("\\&\\&"," AND ")
                        .replaceAll("\\&"," AND ")
                        .replaceAll("\\|\\|"," OR ")
                        .replaceAll("\\|"," OR "));

                invalidCollectionViewPropositionName = m;
                invalidCollectionViewPropositionImpl = impl;
                matchedVar = var;
                matchedCollectionVar = collectionVar;

                impl = impl.replaceAll(var+"\\.","");
                List<String> list = Arrays.asList(impl.split("\\^"));
                if (!orderedPs.contains(list)){
                    collectionViewOk = false;
                    break;
                }
            }
            if (!collectionViewOk){
                throw new IllegalStateException("Invalid collection view.\nThe following universal quantification does not use a valid proposition.\nValid propositions for collection views can only be exclusive disjunctions (without negations)\nof the disjuncts from the underlying type:\n\n"+
                        invalidCollectionViewPropositionName+"(){\n\treturn "+matchedCollectionVar+"().forall("+matchedVar+"->"+invalidCollectionViewPropositionImpl+");\n}");
            } else {
                LOG.info("Collection view is sound as all universal quantifications use a valid proposition.");
                return true;
            }
        } else if (fields.stream().filter(f->!f.isDefault() && Collection.class.isAssignableFrom(f.getReturnType())).count() > 1){
            // not allowed
            throw new IllegalStateException("Cannot have two collections directly in a view");
        }

        Set<String>[] methodNames = new Set[fields.size()];
        for (Class<?> t : fields.stream().map(f->f.getReturnType()).collect(Collectors.toList())){
            if (t.getSimpleName().contains("Collection")){
                continue;
            } else {
                Class cls = getClassIfSimpleNameIsUniqueInPackage(t.getSimpleName());
                methodNames[i] = Arrays.asList(cls.getMethods()).stream().filter(m->m.isDefault() && m.getReturnType().equals(boolean.class)).map(m->m.getName()).collect(Collectors.toSet());
                i++;
            }
        }

        Set<MethodDeclaration> methodDeclarations = c.getMethods().stream().filter(m->m.isDefault() && m.getType().isPrimitiveType() && m.getType().asPrimitiveType().getType().asString().equals("boolean")).collect(Collectors.toSet());

        if (clazz.getInterfaces().length>0){
            Class superClass = clazz.getInterfaces()[0];
            methodDeclarations.addAll(all.get(superClass).compilationUnit.getInterfaceByName(superClass.getSimpleName()).get().getMethods()
                    .stream().filter(m->m.isDefault() && m.getType().isPrimitiveType() && m.getType().asPrimitiveType().getType().asString().equals("boolean")).collect(Collectors.toSet()));
        }

        List<MethodDeclarationAndPredicate> methodDeclarationAndPredicates = new ArrayList<>();
        for (MethodDeclaration m : methodDeclarations){
            String p = m.getBody().get().asBlockStmt().getStatement(0).toString();
            List<Integer> indicies = new ArrayList<>();
            int count = 0;
            for (Method f : fields){
                if (p.contains(f.getName())){
                    indicies.add(count);
                }
                count++;
            }
            p = p.replaceAll("\\.",".equals(\"");
            for (Integer x : indicies){
                p = p.replaceAll("\\("+fields.get(x).getName()+"\\(\\)\\.","(list.get("+x+").");
                p = p.replaceAll("\\!"+fields.get(x).getName()+"\\(\\)\\.","!list.get("+x+").");
                p = p.replaceAll(" "+fields.get(x).getName()+"\\(\\)\\."," list.get("+x+").");
            }
            p = p.replaceAll("\\(\\)","\")");
            String viewDisjunctPredicateClassName =
                    "ViewDisjunct_"+
                    UUID.randomUUID().toString().replaceAll("-","_")+"_"+
                    m.getName().toString();
            String predicateSrc = "package com.cognitiobox.petra.codegen; import java.util.List; import java.util.function.Predicate; public class "+viewDisjunctPredicateClassName+" implements Predicate<List<String>> { public boolean test(List<String> list){"+p+"}}";

            try {
                Class generatedClazz = CompilerUtils.CACHED_COMPILER.loadFromJava("com.cognitiobox.petra.codegen."+viewDisjunctPredicateClassName, predicateSrc);
                Predicate<List<String>> pred = (Predicate<List<String>>) generatedClazz.newInstance();
                methodDeclarationAndPredicates.add(new MethodDeclarationAndPredicate(m,pred));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        Set<List<String>> truth = Sets.cartesianProduct(methodNames);

        LOG.info("View: "+clazz.getSimpleName()+" = "+fieldMethodDeclarations.stream().map(f->f.getType().asString()).reduce((a,b)->a+" X "+b).get()+" = "+truth.size()+" states.");

        Set<List<String>> all = new HashSet<>();
        List<MethodDeclarationAndSet> methodDeclarationAndSets = new ArrayList<>();
        for (MethodDeclarationAndPredicate pair : methodDeclarationAndPredicates){
            Set<List<String>> s = truth.stream().filter(e->pair.predicate.test(e)).collect(Collectors.toSet());
            LOG.info(pair.methodDeclaration.getNameAsString()+" covers: "+s.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
            methodDeclarationAndSets.add(new MethodDeclarationAndSet(pair.methodDeclaration,s));
            all.addAll(s);
        }
        boolean isComplete = false;

        // reduces programmers work when compared to all.equals(truth) as not all states are relevant
        if (truth.containsAll(all)){
            isComplete = true;
        }
        boolean isSound = true;
        for (MethodDeclarationAndSet m1 : methodDeclarationAndSets){
            for (MethodDeclarationAndSet m2 : methodDeclarationAndSets){
                if (m1.set!=m2.set){
                    Set a = new HashSet<>(m1.set);
                    a.retainAll(m2.set);
                    if (!a.isEmpty()){
                        if (isSound){
                            isSound = false;
                        }
                        LOG.info(m1.methodDeclaration.getNameAsString()+" overlaps with "+m2.methodDeclaration.getNameAsString()+" on states: "+a.toString()
                                .replaceAll("\\[\\[","[\n [")
                                .replaceAll("\\],","],\n")
                                .replaceAll("\\]\\]","]\n]")
                        );
                    }
                }
            }
        }
        for (MethodDeclarationAndSet m : methodDeclarationAndSets){
            if (m.set.isEmpty()){
                LOG.info(m.methodDeclaration.getNameAsString()+" is empty.");
                isSound = false;
            }
        }
        if (overridesMethodFromBaseInterface(clazz)){
            LOG.info(clazz.getSimpleName()+" overrides methods from "+clazz.getInterfaces()[0].getSimpleName());
            isSound = false;
        }
        if (isComplete){
            List t = truth.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            List a = all.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            LOG.info("all cases: "+t.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
            LOG.info("covered cases: "+a.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
        } else {
            List t = truth.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            List a = all.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            LOG.info("all cases: "+t.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
            LOG.info("covered cases: "+a.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
            Set<List<String>> m = new HashSet<>(truth);
            m.removeAll(all);
            LOG.info("missing cases: "+m.toString()
                    .replaceAll("\\[\\[","[\n [")
                    .replaceAll("\\],","],\n")
                    .replaceAll("\\]\\]","]\n]")
            );
            LOG.info("not all cases covered, therefore is not complete.");
        }
        if (isSound){
            LOG.info("View is sound as there are no overlaps.");
        } else {
            LOG.info("View is not sound as there are overlaps or there are empty sets or view default boolean methods override base interface methods.");
        }
        dataTypeInfoMap.put(clazz,new DataTypeInfo(Sets.cartesianProduct(methodNames),methodDeclarations));

        return isSound && isComplete;
    }

    static void rewriteJoinForallParSteps(Expression kase, CompilationUnitWithData cu, int count) {
        List<Pair<Integer,Expression>> replacements = new ArrayList<>();
        int i = 0;
        boolean beforeLogged = false;
        for (Statement si : kase
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements()) {
            if (si.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("join")){
                int a = 0;
                for (Expression arg : si.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (a==0){
                        a++;
                        continue;
                    }
                    if (arg.asMethodCallExpr().getName().toString().equals("parr")){
                        if (!beforeLogged){
                            LOG.debug("before PAR_FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                            LOG.debug(kase.toString());
                            beforeLogged = true;
                        }
                        String methodName = arg.asMethodCallExpr().getName().asString();
                        MethodCallExpr step = new MethodCallExpr(methodName.substring(0,methodName.length()-1));
                        for (Expression e : arg.asMethodCallExpr().getArguments()) {
                            step.addArgument(e);
                        }
                        MethodCallExpr preWithForall = new MethodCallExpr("forall");
                        preWithForall.addArgument(arg.asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0));
                        MethodCallExpr postWithForall = new MethodCallExpr("forall");
                        postWithForall.addArgument(arg.asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1));

                        List<Expression> args = new ArrayList<>();
                        int j = 0;
                        for (Expression e : step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArguments()){
                            if (j>1){
                                args.add(e);
                            }
                            j++;
                        }

                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().setArguments(new NodeList<>());
                        String ref = arg.asMethodCallExpr().getArgument(0).toString();
                        String x = ref.split("\\.")[0];
                        step.getArguments().replace(step.getArgument(0),new JavaParser().parseExpression(x).getResult().get());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+"->"+ref+"."+preWithForall.toString());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+"->"+ref+"."+postWithForall.toString());
                        args.forEach(e->step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(e));
                        replacements.add(new Pair<Integer, Expression>(i,step));
                    }
                    a++;
                }
                for (Pair<Integer,Expression> r : replacements){
                    si.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().remove(r.a);
                    si.asExpressionStmt().getExpression().asMethodCallExpr().addArgument(r.b);
                }
            }
        }
        if (beforeLogged){
            LOG.info("PAR_FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after PAR_FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
    }

    static void rewriteSingleParStepToSeqStep(Expression kase, CompilationUnitWithData cu, int count) {
        List<Pair<Integer,Expression>> replacements = new ArrayList<>();
        int i = 0;
        boolean beforeLogged = false;
        for (Statement stepInstruction : kase
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements()) {
            if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("par")){
                if (!beforeLogged){
                    LOG.debug("before SINGLE_PAR_TO_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    beforeLogged = true;
                }
                MethodCallExpr step = new MethodCallExpr("seq");
                for (Expression a : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()) {
                    step.addArgument(a);
                }
                replacements.add(new Pair<Integer, Expression>(i,step));
            }
            i++;
        }
        for (Pair<Integer,Expression> r : replacements){
            kase.asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody().asBlockStmt().getStatement(r.a).remove();
            kase.asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody().asBlockStmt().addStatement(r.a,r.b);
        }
        if (beforeLogged){
            LOG.info("SINGLE_PAR_TO_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after SINGLE_PAR_TO_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
    }

    static void rewriteStepsWithForall(Expression kase, CompilationUnitWithData cu, int count) {
                List<Pair<Integer,Expression>> replacements = new ArrayList<>();
                int i = 0;
                boolean beforeLogged = false;
                for (Statement stepInstruction : kase
                        .asMethodCallExpr()
                        .getArgument(2)
                        .asLambdaExpr()
                        .getBody()
                        .asBlockStmt()
                        .getStatements()) {
                    if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seqr") ||
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("castr") ||
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("parr")){
                        if (!beforeLogged){
                            LOG.debug("before FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                            LOG.debug(kase.toString());
                            beforeLogged = true;
                        }
                        String methodName = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString();
                        MethodCallExpr step = new MethodCallExpr(methodName.substring(0,methodName.length()-1));
                        for (Expression a : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()) {
                            step.addArgument(a);
                        }
                        MethodCallExpr preWithForall = new MethodCallExpr("forall");
                        preWithForall.addArgument(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0));
                        MethodCallExpr postWithForall = new MethodCallExpr("forall");
                        postWithForall.addArgument(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1));

                        List<Expression> args = new ArrayList<>();
                        int j = 0;
                        for (Expression e : step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArguments()){
                            if (j>1){
                                args.add(e);
                            }
                            j++;
                        }

                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().setArguments(new NodeList<>());
                        String ref = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString();
                        String x = ref.split("\\.")[0];
                        step.getArguments().replace(step.getArgument(0),new JavaParser().parseExpression(x).getResult().get());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+"->"+ref+"."+preWithForall.toString());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+"->"+ref+"."+postWithForall.toString());
                        args.forEach(e->step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(e));
                        replacements.add(new Pair<Integer, Expression>(i,step));
                    }
                    i++;
                }
                for (Pair<Integer,Expression> r : replacements){
                    kase.asMethodCallExpr()
                            .getArgument(2)
                            .asLambdaExpr()
                            .getBody().asBlockStmt().getStatement(r.a).remove();
                    kase.asMethodCallExpr()
                            .getArgument(2)
                            .asLambdaExpr()
                            .getBody().asBlockStmt().addStatement(r.a,r.b);
                }
                if (beforeLogged){
                    LOG.info("FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug("after FORALL_INTRO applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug("");
                }
    }

    static void rewriteSingleJoinPar(Expression kase, CompilationUnitWithData cu, int count) throws ClassNotFoundException {
                List<Pair<Integer,Expression>> replacements = new ArrayList<>();
                int i = 0;
                boolean beforeLogged = false;
                for (Statement stepInstruction : kase
                        .asMethodCallExpr()
                        .getArgument(2)
                        .asLambdaExpr()
                        .getBody()
                        .asBlockStmt()
                        .getStatements()) {
                    if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join") &&
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().size()==1) {
                        if (!beforeLogged){
                            LOG.debug("before SIMPLIFY_SINGLE_JOIN applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                            LOG.debug(kase.toString());
                            beforeLogged = true;
                        }
                        MethodCallExpr seqr = new MethodCallExpr("seqr");
                        for (Expression a : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArguments()) {
                            seqr.addArgument(a);
                        }
                        replacements.add(new Pair<Integer, Expression>(i,seqr));
                    }
                    i++;
                }
                for (Pair<Integer,Expression> r : replacements){
                    kase.asMethodCallExpr()
                            .getArgument(2)
                            .asLambdaExpr()
                            .getBody().asBlockStmt().getStatement(r.a).remove();
                    kase.asMethodCallExpr()
                            .getArgument(2)
                            .asLambdaExpr()
                            .getBody().asBlockStmt().addStatement(r.a,r.b);
                }
                if (beforeLogged){
                    LOG.info("SIMPLIFY_SINGLE_JOIN applied "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug("after SIMPLIFY_SINGLE_JOIN applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug("");
                }
    }

    static void rewriteGraphKaseJoinParsOrSeperatedSeqsToSeq(Expression kase, CompilationUnitWithData cu, int count) {
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before JOIN_PARS_OR_SEPARATED_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            beforeLogged = true;
        }

        Class dataType = Arrays.asList(cu.clazz.getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals("accept")).findFirst().get().getParameterTypes()[0];
        Set<String> fields = Arrays.asList(dataType.getDeclaredMethods())
                .stream()
                .filter(m->!m.isDefault() && m.getParameterTypes().length==0 && !m.getReturnType().equals(Void.class))
                .map(m->m.getName())
                .collect(Collectors.toSet());

        MethodCallExpr seq = null;
        MethodCallExpr kases = null;
        boolean started = false;
        int startedIndex = 0;
        StringBuilder preConjunction = null;
        StringBuilder postConjunction = null;
        int i = 0;
        List<Statement> toRemove = new ArrayList<>();
        for (Statement stepInstruction : kase
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements()) {
            if (!started && (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join") ||
                    (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("seq") &&
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")))
            ){
                preConjunction = new StringBuilder();
                postConjunction = new StringBuilder();

                seq = new MethodCallExpr("seq");
                seq.addArgument(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,dataType.getSimpleName()));
                BlockStmt newBlockStmt = new BlockStmt();
                LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                kases = new MethodCallExpr("kases");
                newBlockStmt.addStatement(kases);

                seq.addArgument(newLambdaExpr);

                if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join")){
                    int a = 0;
                    for (Expression par : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                        if (a==0){
                            a++;
                            continue;
                        } else if (a==1){
                            preConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                            postConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                            fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split("->")[1].split("\\.")[1].replaceAll("\\(\\)","").trim());
                        } else {
                            preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                            postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                            fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split("->")[1].split("\\.")[1].replaceAll("\\(\\)","").trim());
                        }
                        a++;
                    }
                } else if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("seq") &&
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    preConjunction.append("("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                    postConjunction.append("("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                    fields.remove(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(0).toString().split("\\.")[1].replaceAll("\\(\\)","").trim());
                }
                started = true;
                startedIndex = i;
            } else if (started && stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join")){
                int a = 0;
                for (Expression par : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (a==0){
                        a++;
                        continue;
                    } else {
                        preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                        postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                        fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split("->")[1].split("\\.")[1].replaceAll("\\(\\)","").trim());
                    }
                    a++;
                }
            } else if (started &&
                    stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("seq") &&
                    stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")
            ){
                preConjunction.append("&("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                postConjunction.append("&("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                fields.remove(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(0).toString().split("\\.")[1].replaceAll("\\(\\)","").trim());
            }
            toRemove.add(stepInstruction);
            i++;
        }

        if (fields.size()==0){
            for (Statement s : toRemove){
                s.remove();
            }
            kase.asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody()
                    .asBlockStmt().addStatement(startedIndex,seq);
            MethodCallExpr kse = new MethodCallExpr("kase");
            kse.addArgument(preConjunction.toString());
            kse.addArgument(postConjunction.toString());
            kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
            kse.addArgument("ASSUMED");
            kases.addArgument(kse);
        }

        if (beforeLogged){
            LOG.info("JOIN_PARS_OR_SEPARATED_SEQ applied "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after JOIN_PARS_OR_SEPARATED_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
    }

    static void rewriteGraphKaseJoinParsToSeq(Expression kase, CompilationUnitWithData cu, int count) {
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before JOIN_PARS_TO_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            beforeLogged = true;
        }

        int i = 0;
        for (Statement stepInstruction : kase
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements()) {
            if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join")){
                MethodCallExpr seq = new MethodCallExpr("seq");
                Class dataType = Arrays.asList(cu.clazz.getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals("accept")).findFirst().get().getParameterTypes()[0];
                seq.addArgument(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,dataType.getSimpleName()));

                BlockStmt newBlockStmt = new BlockStmt();
                LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                MethodCallExpr kases = new MethodCallExpr("kases");
                newBlockStmt.addStatement(kases);

                seq.addArgument(newLambdaExpr);

                StringBuilder preConjunction = new StringBuilder();
                StringBuilder postConjunction = new StringBuilder();
                int a = 0;
                for (Expression par : stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (a==0){
                        a++;
                        continue;
                    } else if (a==1){
                        preConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                        postConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                    } else {
                        preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                        postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                    }
                    a++;
                }
                stepInstruction.remove();
                kase.asMethodCallExpr()
                        .getArgument(2)
                        .asLambdaExpr()
                        .getBody()
                        .asBlockStmt().addStatement(i,seq);
                MethodCallExpr kse = new MethodCallExpr("kase");
                kse.addArgument(preConjunction.toString());
                kse.addArgument(postConjunction.toString());
                kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                kse.addArgument("ASSUMED");
                kases.addArgument(kse);
                break; // hack!!! need to find out why we are getting ConcurrentModificationException without this.
            }
            i++;
        }

        if (beforeLogged){
            LOG.info("JOIN_PARS_TO_SEQ applied "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after JOIN_PARS_TO_SEQ applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
    }

    static void rewriteGraphKaseSeperatedStepsToNonSeperatedSteps(Expression kase, CompilationUnitWithData cu, int count) {
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before SEPERATED_TO_NON_SEPERATED applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            beforeLogged = true;
        }

        boolean carryon = true;
        while(carryon){
            List<Statement> seperationGroup = new ArrayList<>();
            int i = 0;
            for (Statement stepInstruction : kase
                    .asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody()
                    .asBlockStmt()
                    .getStatements()) {
                if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join")){
                    i++;
                    continue;
                }
                if (seperationGroup.isEmpty() && stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    seperationGroup.add(stepInstruction);
                } else if (seperationGroup.isEmpty() && !stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    // do nothing
                } else if (!seperationGroup.isEmpty() && stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    if (seperationGroup.get(0).asExpressionStmt().getExpression().asMethodCallExpr().getName()
                            .equals(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName())){
                        // groups must be of the same step type ie seq, par, ie this must happen after forall intros
                        seperationGroup.add(stepInstruction);
                    }
                } else if (!seperationGroup.isEmpty() && !stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    // do nothing
                    break;
                }
                i++;
            }
            if (seperationGroup.size()<2){
                seperationGroup.clear();
            }
            if (!seperationGroup.isEmpty()){
                MethodCallExpr seq = new MethodCallExpr("seq");
                Class dataType = Arrays.asList(cu.clazz.getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals("accept")).findFirst().get().getParameterTypes()[0];
                seq.addArgument(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,dataType.getSimpleName()));

                BlockStmt newBlockStmt = new BlockStmt();
                LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                MethodCallExpr kases = new MethodCallExpr("kases");
                newBlockStmt.addStatement(kases);

                seq.addArgument(newLambdaExpr);

                StringBuilder preConjunction = new StringBuilder();
                StringBuilder postConjunction = new StringBuilder();

                kase.asMethodCallExpr()
                        .getArgument(2)
                        .asLambdaExpr()
                        .getBody()
                        .asBlockStmt().addStatement(i,seq);

                int c = 0;
                for (Statement s : seperationGroup){
                    if (c==0){
                        preConjunction.append("("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                        postConjunction.append("("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                    } else {
                        preConjunction.append("&("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","")+")");
                        postConjunction.append("&("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split("->")[1].replaceAll(" ","")+")");
                    }
                    c++;
                    s.remove();
                }

                MethodCallExpr kse = new MethodCallExpr("kase");
                kse.addArgument(preConjunction.toString());
                kse.addArgument(postConjunction.toString());
                kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                kse.addArgument("ASSUMED");
                kases.addArgument(kse);
            }

            if (seperationGroup.isEmpty() &&
                    i==kase
                    .asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody()
                    .asBlockStmt()
                    .getStatements().size()){
                break;
            }
        }
        if (beforeLogged){
            LOG.info("SEPERATED_TO_NON_SEPERATED applied "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after SEPERATED_TO_NON_SEPERATED applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
    }

    static void rewriteGraphKaseJoinParStepsToEdges(Expression kase, CompilationUnitWithData cu, int count) {
        boolean cantFindClass = false;
        boolean beforeLogged = false;
        // go through step instructions
        for (Statement si : kase
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements()) {
            if (!beforeLogged){
                LOG.debug("before RESOLVE_JOIN_PAR and KASE_COMB applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                LOG.debug(kase.toString());
                beforeLogged = true;
            }
            if (si.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join")){
                int a = 0;
                for (Expression parOrParr : si.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (a==0){
                        a++;
                        continue;
                    }
                    int arg = parOrParr.asMethodCallExpr().getArguments().size() - 1;
                    if (parOrParr.asMethodCallExpr().getArgument(arg).isObjectCreationExpr()) {
                        Class c = getClassIfSimpleNameIsUniqueInPackage(parOrParr.asMethodCallExpr().getArgument(arg).asObjectCreationExpr().getType().getNameAsString());
                        if (c==null){
                            LOG.debug(parOrParr.asMethodCallExpr().getArgument(arg).asObjectCreationExpr().getType().getNameAsString()+": cannot find class in local package "+cu.compilationUnit.getPackageDeclaration().get().getNameAsString());
                            cantFindClass = true;
                            continue;
                        }
                        parOrParr.asMethodCallExpr().getArgument(arg).remove();
                        BlockStmt blockStmt = all.get(c).compilationUnit.getClassByName(c.getSimpleName()).get().getMethodsByName("accept").get(0).getBody().get();
                        LambdaExpr lambdaExpr = new LambdaExpr(new Parameter(), blockStmt);
                        BlockStmt newBlockStmt = new BlockStmt();
                        LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                        int count2 = 0;
                        MethodCallExpr kases = new MethodCallExpr("kases");
                        newBlockStmt.addStatement(kases);

                        StringBuilder kasesPreconditionDisjunction = new StringBuilder();
                        StringBuilder kasesPostconditionDisjunction = new StringBuilder();
                        Set<String> pres = new HashSet<>();
                        Set<String> posts = new HashSet<>();
                        for (Expression k : lambdaExpr.getBody().asBlockStmt().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr().getArguments()) {
                            if (count2==0){
                                count2++;
                                continue;
                            } else if (count2==1){
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String preProp = pre.split("->")[1];
                                    String preRefRoot = preProp.split("\\.")[0];
                                    pre = root+"->"+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(" ","");
                                }
                                pres.add(pre);
                                String z = pre.split("->")[0].trim();
                                if (pre.contains("forall")){
                                    pre = pre.split("->",2)[1].trim();
                                } else {
                                    pre = pre.split("->")[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String postProp = post.split("->")[1];
                                    String postRefRoot = postProp.split("\\.")[0];
                                    post = root+"->"+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split("->",2)[1].trim();
                                } else {
                                    post = post.split("->")[1].trim();
                                }
                                kasesPreconditionDisjunction.append(z+" -> "+pre);
                                kasesPostconditionDisjunction.append(z+" -> "+post);
                            } else {
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String preProp = pre.split("->")[1];
                                    String preRefRoot = preProp.split("\\.")[0];
                                    pre = root+"->"+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(" ","");
                                }
                                pres.add(pre);
                                if (pre.contains("forall")){
                                    pre = pre.split("->",2)[1].trim();
                                } else {
                                    pre = pre.split("->")[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String postProp = post.split("->")[1];
                                    String postRefRoot = postProp.split("\\.")[0];
                                    post = root+"->"+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split("->",2)[1].trim();
                                } else {
                                    post = post.split("->")[1].trim();
                                }
                                kasesPreconditionDisjunction.append(" ^ "+pre);
                                kasesPostconditionDisjunction.append(" ^ "+post);
                            }
                            count2++;
                        }
                        MethodCallExpr kse = new MethodCallExpr("kase");
                        kse.addArgument(kasesPreconditionDisjunction.toString());
                        kse.addArgument(kasesPostconditionDisjunction.toString());
                        kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                        kse.addArgument("ASSUMED");
                        kases.addArgument(kse);
                        parOrParr.asMethodCallExpr().addArgument(newLambdaExpr);
                    } else if (
                            parOrParr.asMethodCallExpr().getArguments().size()==3 &&
                                    (parOrParr.asMethodCallExpr().getName().asString().equals("cast") ||
                                            parOrParr.asMethodCallExpr().getName().asString().equals("castr"))
                    ) {
                        // need to get the data type of the instance being cast, this can not be itself a view when casting between views,
                        // it must be a concrete data type with multiple views implemented
                        Queue<String> calls = Arrays.asList(parOrParr.asMethodCallExpr().getArgument(0).toString().split("\\.")).stream().collect(Collectors.toCollection(()->new LinkedList<>()));
                        Class dataType = Arrays.asList(cu.clazz.getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals("accept")).findFirst().get().getParameterTypes()[0];
                        Class current = dataType;
                        calls.poll();
                        while(calls.size()>0){
                            try {
                                Class returnType = current.getMethod(calls.peek().replaceAll("\\(\\)","")).getReturnType();
                                if (Collection.class.isAssignableFrom(returnType)){
                                    current = (Class) ((ParameterizedType) current.getMethod(calls.peek().replaceAll("\\(\\)","")).getGenericReturnType()).getActualTypeArguments()[0];
                                } else {
                                    current = returnType;
                                }
                                calls.poll();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                        // going between whole views needs to be views on the same memory region
                        // also need to allow going between whole foralls of conjunctions to separate foralls with each conjunct
                        checkCastPreAndPostConditionsEitherGoBetweenWholeViewsOrBetweenAbstractionsAndImplementations(
                                parOrParr,
                                all.get(current));

                        BlockStmt newBlockStmt = new BlockStmt();
                        LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                        MethodCallExpr kases = new MethodCallExpr("kases");
                        newBlockStmt.addStatement(kases);

                        StringBuilder kasesPreconditionDisjunction = new StringBuilder();
                        StringBuilder kasesPostconditionDisjunction = new StringBuilder();
                        String x = parOrParr.asMethodCallExpr().getArgument(0).toString().split("")[0].toLowerCase();
                        String p = parOrParr.asMethodCallExpr().getArgument(1).toString();
                        String q = parOrParr.asMethodCallExpr().getArgument(2).toString();
                        String refFullname = parOrParr.asMethodCallExpr().getArgument(0).toString();
                        kasesPreconditionDisjunction.append(p.replaceAll(" ","").replaceAll(x+"->",refFullname.split("\\.")[0]+"->").replaceAll(x+"\\.",refFullname+"."));
                        kasesPostconditionDisjunction.append(q.replaceAll(" ","").replaceAll(x+"->",refFullname.split("\\.")[0]+"->").replaceAll(x+"\\.",refFullname+"."));
                        parOrParr.asMethodCallExpr().getArgument(1).remove();
                        parOrParr.asMethodCallExpr().getArgument(1).remove();
                        MethodCallExpr kse = new MethodCallExpr("kase");
                        kse.addArgument(kasesPreconditionDisjunction.toString());
                        kse.addArgument(kasesPostconditionDisjunction.toString());
                        kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                        kse.addArgument("ASSUMED");
                        kases.addArgument(kse);
                        parOrParr.asMethodCallExpr().addArgument(newLambdaExpr);
                    }
                    a++;
                }
            }
        }
        if (beforeLogged){
            LOG.info("RESOLVE_JOIN_PAR and KASE_COMB applied "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug("after RESOLVE_JOIN_PAR and KASE_COMB applied to "+cu.clazz.getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug("");
        }
        if (cantFindClass){
            throw new IllegalStateException("cant find classes.");
        }
        // intro graph
        kase.asMethodCallExpr().addArgument("UNPROVED");
        kase.asMethodCallExpr().addArgument(kase.asMethodCallExpr().getArgument(0).clone());
    }

    static void rewriteGraphKaseStepsToEdges(Expression kase, CompilationUnitWithData cu, int count) {
        boolean cantFindClass = false;
                boolean beforeLogged = false;
                // go through step instructions
                for (Statement stepInstruction : kase
                        .asMethodCallExpr()
                        .getArgument(2)
                        .asLambdaExpr()
                        .getBody()
                        .asBlockStmt()
                        .getStatements()) {
                    if (!beforeLogged){
                        LOG.debug("before RESOLVE and KASE_COMB applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                        LOG.debug(kase.toString());
                        beforeLogged = true;
                    }
                    int arg = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().size() - 1;
                    if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).isObjectCreationExpr()) {
                        Class c = getClassIfSimpleNameIsUniqueInPackage(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).asObjectCreationExpr().getType().getNameAsString());
                        if (c==null){
                            LOG.debug(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).asObjectCreationExpr().getType().getNameAsString()+": cannot find class in local package "+cu.compilationUnit.getPackageDeclaration().get().getNameAsString());
                            cantFindClass = true;
                            continue;
                        }
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).remove();
                        BlockStmt blockStmt = all.get(c).compilationUnit.getClassByName(c.getSimpleName()).get().getMethodsByName("accept").get(0).getBody().get();
                        LambdaExpr lambdaExpr = new LambdaExpr(new Parameter(), blockStmt);
                        BlockStmt newBlockStmt = new BlockStmt();
                        LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                        int count2 = 0;
                        MethodCallExpr kases = new MethodCallExpr("kases");
                        newBlockStmt.addStatement(kases);

                        StringBuilder kasesPreconditionDisjunction = new StringBuilder();
                        StringBuilder kasesPostconditionDisjunction = new StringBuilder();
                        Set<String> pres = new HashSet<>();
                        Set<String> posts = new HashSet<>();
                        String x = lambdaExpr.getBody().asBlockStmt().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString();
                        for (Expression k : lambdaExpr.getBody().asBlockStmt().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr().getArguments()) {
                            if (count2==0){
                                count2++;
                                continue;
                            } else if (count2==1){
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String preProp = pre.split("->")[1];
                                    String preRefRoot = preProp.split("\\.")[0];
                                    pre = root+"->"+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(" ","");
                                }
                                pres.add(pre);
                                String z = pre.split("->")[0].trim();
                                if (pre.contains("forall")){
                                    pre = pre.split("->",2)[1].trim();
                                } else {
                                    pre = pre.split("->")[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String postProp = post.split("->")[1];
                                    String postRefRoot = postProp.split("\\.")[0];
                                    post = root+"->"+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split("->",2)[1].trim();
                                } else {
                                    post = post.split("->")[1].trim();
                                }
                                kasesPreconditionDisjunction.append(z+" -> "+pre);
                                kasesPostconditionDisjunction.append(z+" -> "+post);
                            } else {
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split("->")[1].replaceAll(" ","");
                                    String root = ref.split("\\.")[0];
                                    String preProp = pre.split("->")[1];
                                    String preRefRoot = preProp.split("\\.")[0];
                                    pre = root+"->"+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(" ","");
                                }
                                pres.add(pre);
                                if (pre.contains("forall")){
                                    pre = pre.split("->",2)[1].trim();
                                } else {
                                    pre = pre.split("->")[1].trim();
                                }
                                String post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(" ","");
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split("->",2)[1].trim();
                                } else {
                                    post = post.split("->")[1].trim();
                                }
                                kasesPreconditionDisjunction.append(" ^ "+pre);
                                kasesPostconditionDisjunction.append(" ^ "+post);
                            }
                            count2++;
                        }
                        MethodCallExpr kse = new MethodCallExpr("kase");
                        kse.addArgument(kasesPreconditionDisjunction.toString());
                        kse.addArgument(kasesPostconditionDisjunction.toString());
                        kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                        kse.addArgument("ASSUMED");
                        kases.addArgument(kse);
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().addArgument(newLambdaExpr);
                    } else if (
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().size()==3 &&
                                    (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("cast") ||
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("castr"))
                    ) {
                        // need to get the data type of the instance being cast, this can not be itself a view when casting between views,
                        // it must be a concrete data type with multiple views implemented
                        Queue<String> calls = Arrays.asList(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().split("\\.")).stream().collect(Collectors.toCollection(()->new LinkedList<>()));
                        Class dataType = Arrays.asList(cu.clazz.getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals("accept")).findFirst().get().getParameterTypes()[0];
                        Class current = dataType;
                        calls.poll();
                        while(calls.size()>0){
                            try {
                                Class returnType = current.getMethod(calls.peek().replaceAll("\\(\\)","")).getReturnType();
                                if (Collection.class.isAssignableFrom(returnType)){
                                    current = (Class) ((ParameterizedType) current.getMethod(calls.peek().replaceAll("\\(\\)","")).getGenericReturnType()).getActualTypeArguments()[0];
                                } else {
                                    current = returnType;
                                }
                                calls.poll();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                        // going between whole views needs to be views on the same memory region
                        // also need to allow going between whole foralls of conjunctions to separate foralls with each conjunct
                        checkCastPreAndPostConditionsEitherGoBetweenWholeViewsOrBetweenAbstractionsAndImplementations(
                                stepInstruction.asExpressionStmt().getExpression(),
                                all.get(current));

                        BlockStmt newBlockStmt = new BlockStmt();
                        LambdaExpr newLambdaExpr = new LambdaExpr(new Parameter(), newBlockStmt);
                        MethodCallExpr kases = new MethodCallExpr("kases");
                        newBlockStmt.addStatement(kases);

                        StringBuilder kasesPreconditionDisjunction = new StringBuilder();
                        StringBuilder kasesPostconditionDisjunction = new StringBuilder();
                        String x = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().split("")[0].toLowerCase();
                        String p = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).toString();
                        String q = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(2).toString();
                        String refFullname = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString();
                        kasesPreconditionDisjunction.append(p.replaceAll(" ","").replaceAll(x+"->",refFullname.split("\\.")[0]+"->").replaceAll(x+"\\.",refFullname+"."));
                        kasesPostconditionDisjunction.append(q.replaceAll(" ","").replaceAll(x+"->",refFullname.split("\\.")[0]+"->").replaceAll(x+"\\.",refFullname+"."));
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).remove();
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).remove();
                        MethodCallExpr kse = new MethodCallExpr("kase");
                        kse.addArgument(kasesPreconditionDisjunction.toString());
                        kse.addArgument(kasesPostconditionDisjunction.toString());
                        kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                        kse.addArgument("ASSUMED");
                        kases.addArgument(kse);
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().addArgument(newLambdaExpr);
                    }
                }
                if (beforeLogged){
                    LOG.info("RESOLVE and KASE_COMB applied "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug("after RESOLVE and KASE_COMB applied to "+cu.clazz.getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug("");
                }
        if (cantFindClass){
            throw new IllegalStateException("cant find classes.");
        }
        // intro graph
        kase.asMethodCallExpr().addArgument("UNPROVED");
        kase.asMethodCallExpr().addArgument(kase.asMethodCallExpr().getArgument(0).clone());
    }

    static Class getClassIfSimpleNameIsUniqueInPackage(String simpleName){
        Class c = null;
        try {
            int count = 0;
            for (ClassPath.ClassInfo info : ClassPath.from(Thread.currentThread().getContextClassLoader())
                    .getAllClasses()){
                if (info.getPackageName().contains(entryPointPackageName)){
                    String pn = info.getPackageName();
                    String pt = Pattern.quote(entryPointPackageName);
                    String res = pn.replaceAll(pt,"");
                    if (res.startsWith(".") || res.isEmpty()){
                        if (info.getSimpleName().equals(simpleName)){
                            count++;
                            if (count>1){
                                throw new IllegalStateException("class names must be unique.");
                            }
                            c = info.load();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    static String getImplementation(String preCondition, CompilationUnitWithData graphTerm) {
        String pre = preCondition;
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split("^")).stream().map(s->s.replaceAll("\\(\\)","")).collect(Collectors.toList()));
        // abstraction / concretion check
        AtomicReference<Class> preRes2 = new AtomicReference();
        AtomicReference<Class> postRes2 = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.clazz,false,preRes2);
        if (preRes2.get()!=null ^ postRes2.get()!=null){
            // check implementation is valid
            if (preRes2.get()!=null && postRes2.get()==null){
                List<MethodDeclaration> method = all.get(preRes2.get()).compilationUnit.getInterfaceByName(preRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0));
                if (method.size()>0){
                    String impl = method.get(0).getBody().get().getStatement(0).toString().replaceAll("return","");
                    return impl.trim().replaceAll(";","");
                } else {
                    Class clazz = preRes2.get().getInterfaces()[0];
                    method = all.get(clazz).compilationUnit.getInterfaceByName(clazz.getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0));
                    String impl = method.get(0).getBody().get().getStatement(0).toString().replaceAll("return","");
                    return impl.trim().replaceAll(";","");
                }
            }
        }
        return null;
    }

    static void checkCastPreAndPostConditionsEitherGoBetweenWholeViewsOrBetweenAbstractionsAndImplementations(Expression cast, CompilationUnitWithData graphTerm) {
        String x = cast.asMethodCallExpr().getArgument(1).toString().split("->")[0].trim();
        String pre = cast.asMethodCallExpr().getArgument(1).toString().split("->",2)[1].trim().replaceAll(x+"\\.","");
        String post = cast.asMethodCallExpr().getArgument(2).toString().split("->",2)[1].trim().replaceAll(x+"\\.","");
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split("^")).stream().map(s->s.replaceAll("\\(\\)","")).collect(Collectors.toList()));
        Set<String> postSet = new HashSet<>(Arrays.asList(post.split("^")).stream().map(s->s.replaceAll("\\(\\)","")).collect(Collectors.toList()));
        AtomicReference<Class> preRes = new AtomicReference();
        AtomicReference<Class> postRes = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.clazz,true,preRes);
        checkForPreOrPostsAgainstViews(postSet,graphTerm.clazz,true,postRes);

        // abstraction / concretion check
        AtomicReference<Class> preRes2 = new AtomicReference();
        AtomicReference<Class> postRes2 = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.clazz,false,preRes2);
        checkForPreOrPostsAgainstViews(postSet,graphTerm.clazz,false,postRes2);

        if (preRes2.get()!=null ^ postRes2.get()!=null){
            // check implementation is valid
            if (preRes2.get()!=null && postRes2.get()==null){
                String impl = all.get(preRes2.get()).compilationUnit.getInterfaceByName(preRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0)).get(0).getBody().get().getStatement(0).toString().replaceAll("return","");
                impl = impl.replaceAll("\\(\\)","").replaceAll(";","").trim();
                if (!impl.equals(new ArrayList<>(postSet).get(0))){
                    LOG.error(cast.toString());
                    throw new IllegalStateException("incorrect concretion: implementation does not match to abstraction.");
                }
            } else if (postRes2.get()!=null && preRes2.get()==null){
                String impl = all.get(postRes2.get()).compilationUnit.getInterfaceByName(postRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(postSet).get(0)).get(0).getBody().get().getStatement(0).toString().replaceAll("return","");
                impl = impl.replaceAll("\\(\\)","").replaceAll(";","").trim();
                if (!impl.equals(new ArrayList<>(preSet).get(0))){
                    LOG.error(cast.toString());
                    throw new IllegalStateException("incorrect abstraction: implementation does not match to abstraction.");
                }
            }
            if (preRes.get()!=null){
                if (postRes.get()!=null){
                    // good view switching cast
                } else {
                    LOG.error(cast.toString());
                    throw new IllegalStateException("not casting between whole views.");
                }
            }
        } else {
            LOG.error(cast.toString());
            throw new IllegalStateException("condition does not fall inside distinct view or cast is not an abstraction/concretion when it looks like it should be.");
        }
    }

    static void checkKasePreConditionsFallInsideADistinctView(Expression kase, CompilationUnitWithData graphTerm, int count) {
        String pre = kase.asMethodCallExpr().getArgument(1).toString().split("->")[1].trim();
        // just check this for pre-conditions, so that kases always complete the input instance wtr to a view. Post-conditions need to be flexible
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split("^")).stream().map(s->s.replaceAll("\\(\\)","").split("\\.")[s.replaceAll("\\(\\)","").split("\\.").length-1]).collect(Collectors.toList()));
        AtomicReference<Class> res = new AtomicReference();
        List<Method> methods = Arrays.asList(graphTerm.clazz.getDeclaredMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) &&
                m.getName().equals("accept")).collect(Collectors.toList());
        checkForPreOrPostsAgainstViews(preSet,methods.get(0).getParameterTypes()[0],false,res);
        if (res.get()==null){
            throw new IllegalStateException("condition does not fall inside distinct view");
        }
    }

    static String resolveImplementation(String precondition, Class theViewClass) {
        String[] split = precondition.split("->",2);
        String cond = split[split.length-1].replaceAll(" ","").replaceAll("\\(\\)","");
        String impl = "";
        int o = 0;
        if (precondition.contains("forall")){
            String s = precondition.split("->",2)[1].split("->")[1];
            String newPre = s.replaceAll(" ","").substring(0,s.replaceAll(" ","").length()-1);
            return newPre;
        }
        if (precondition.contains("&")){
            impl = split[split.length-1].replaceAll(" ","").replaceAll("\\.","\\(\\)\\.");
        } else {
            for (String disjunct : Arrays.asList(cond.split("\\^"))){
                if (o==0){
                    impl = "("+getImplementation(disjunct.split("\\.")[1],all.get(theViewClass))+")";
                } else {
                    impl = impl+"^("+getImplementation(disjunct.split("\\.")[1],all.get(theViewClass))+")";
                }
                o++;
            }
            impl = impl.replaceAll(";","");
            if (impl.contains("forall")){
                impl = impl.split("->")[1].substring(0, impl.split("->")[1].length()-2).replaceAll(" ","");
            }
        }
        return impl;
    }

    static boolean rewriteKase(boolean isLastKaseInRootIeTheInfKase, Expression k, CompilationUnitWithData graph, int kaseNo){
        String P = null;
        String Q = null;
        SymbolicState viewTruth = null;
        Class theViewClass = null;
        try {
            String viewName = graph.compilationUnit.getClassByName(graph.clazz.getSimpleName()).get().getMethodsByName("accept").get(0).getParameter(0).getType().asString();
            theViewClass = getClassIfSimpleNameIsUniqueInPackage(viewName);
            viewTruth =  PetraProgram.getViewTruth(all.get(theViewClass).compilationUnit,theViewClass);
            int preconditionDotCount = getMatchesCount(k.asMethodCallExpr().getArgument(0).toString(),'.');
            int preconditionXorCount = getMatchesCount(k.asMethodCallExpr().getArgument(0).toString(),'^');
            if (preconditionDotCount==1 || k.asMethodCallExpr().getArgument(0).toString().contains("forall") ||  preconditionDotCount==preconditionXorCount+1){
                P = resolveImplementation(k.asMethodCallExpr().getArgument(0).toString(), theViewClass);
            } else if (preconditionDotCount==2){
                P = k.asMethodCallExpr().getArgument(0).toString().split("->")[1].trim().split("\\.",2)[1];
                throw new IllegalArgumentException("precondition cannot go this deep!");
            } else if (preconditionDotCount>2){
                throw new IllegalArgumentException("precondition cannot go this deep!");
            }

            int postconditionDotCount = getMatchesCount(k.asMethodCallExpr().getArgument(1).toString(),'.');
            int postconditionXorCount = getMatchesCount(k.asMethodCallExpr().getArgument(1).toString(),'^');
            if (postconditionDotCount==1 || k.asMethodCallExpr().getArgument(1).toString().contains("forall") || postconditionDotCount==postconditionXorCount+1){
                Q = resolveImplementation(k.asMethodCallExpr().getArgument(1).toString(), theViewClass);
            } else if (postconditionDotCount==2){
                Q = k.asMethodCallExpr().getArgument(1).toString().split("->")[1].trim().split("\\.",2)[1];
                throw new IllegalArgumentException("precondition cannot go this deep!");
            } else if (postconditionDotCount>2){
                throw new IllegalArgumentException("postcondition cannot go this deep!");
            }

            graph.symbolicStates.put(kaseNo,
                    new SymbolicState(filterStatesUsingBooleanPrecondition(
                            viewTruth.getSymbolicStates(),
                            viewTruth.isForall(),
                            P,
                            theViewClass)
                            ,viewTruth.isForall()));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        int noOfStatements = k
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements().size();
        boolean invariantLatched = false;
        for (int i=0;i<noOfStatements;i++) {
            Statement si = k
                    .asMethodCallExpr()
                    .getArgument(2)
                    .asLambdaExpr()
                    .getBody()
                    .asBlockStmt()
                    .getStatement(0);
            if (si.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seq") ||
                    si.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("cast")) {
                if (graph.clazz.isAnnotationPresent(Invariants.class)){
                    for (String inv : ((Invariants)graph.clazz.getDeclaredAnnotation(Invariants.class)).value()) {
                        boolean allMatch = graph.symbolicStates.get(kaseNo).getSymbolicStates().stream().allMatch(l->l.contains(inv));
                        if (allMatch){
                            if (!invariantLatched){
                                invariantLatched = true;
                            }
                        } else {
                            if (invariantLatched){
                                invariantLatched = false;
                                break;
                            }
                        }
                    }
                    if (!invariantLatched){
                        throw new IllegalStateException("invariant violation or invariant condition was never established.");
                    }
                }

                Class theCastClass = theViewClass;
                if (si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).isCastExpr()){
                    String castClass = si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asCastExpr().getType().asString();
                    theCastClass = getClassIfSimpleNameIsUniqueInPackage(castClass);
                    if (!theCastClass.isAnnotationPresent(View.class)){
                        throw new IllegalStateException("the cast class must be a view.");
                    }
                    Class impl = getImplementationOfView(theViewClass);
                    if (!isViewOfImplementation(impl,theCastClass)){
                        throw new IllegalStateException("the cast view must also be a view of the graph view's implementation.");
                    }
                }

                String siP = resolveImplementation(si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0)
                        .asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0) // the kase
                        .asMethodCallExpr().getArgument(0).toString(),theCastClass);
                String siQ = resolveImplementation(si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0)
                        .asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0) // the kase
                        .asMethodCallExpr().getArgument(1).toString(),theCastClass);
                Set<List<String>> preSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siP, theViewClass);
                if (si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatements().size()==1 &&
                        (!graph.symbolicStates.get(kaseNo).isForall() && preSet.containsAll(graph.symbolicStates.get(kaseNo).getSymbolicStates()))) {

                    LOG.debug("before SEQ_EXEC applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());

                    // update symbolic state
                    Set<List<String>> postSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siQ, theViewClass);
                    graph.symbolicStates.put(kaseNo, new SymbolicState(postSet, graph.symbolicStates.get(kaseNo).isForall()));

                    // remove sequential step
                    si.remove();
                    LOG.info("SEQ_EXEC applied "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug("after SEQ_EXEC applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());
                    LOG.debug("");
                } else if (si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatements().size()==1 &&
                        ((P.contains(".isEmpty()") || Q.contains(".isEmpty()") || P.contains(".forall(") || Q.contains(".forall(")) && graph.symbolicStates.get(kaseNo).isForall() && preSet.containsAll(graph.symbolicStates.get(kaseNo).getSymbolicStates()))){
                    LOG.debug("before SEQ_EXEC applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());

                    Set<List<String>> postSet =
                            filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siQ, theViewClass);
                    graph.symbolicStates.put(kaseNo, new SymbolicState(postSet, graph.symbolicStates.get(kaseNo).isForall()));

                    // remove sequential step
                    si.remove();
                    LOG.info("SEQ_EXEC applied "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug("after SEQ_EXEC applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());
                    LOG.debug("");
                }
            }
        }
        noOfStatements = k
                .asMethodCallExpr()
                .getArgument(2)
                .asLambdaExpr()
                .getBody()
                .asBlockStmt()
                .getStatements().size();
        // add false && in order to disable this check so that developers must code a state change
        if (k.asMethodCallExpr().getArgument(0).equals(k.asMethodCallExpr().getArgument(1))){
            LOG.debug("before PROVE_KASE applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());

            k.asMethodCallExpr().getArgument(3).remove();
            k.asMethodCallExpr().addArgument("PROVED");
            graph.status.put(k,true);
            LOG.info("PROVE_KASE applied "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
            LOG.debug("after PROVE_KASE applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());
        } else if (noOfStatements==0){
            String[] postSplit = k.asMethodCallExpr().getArgument(1).toString().split("->",2);
            String post = postSplit[postSplit.length-1].replaceAll(" ","").replaceAll("\\(\\)","");
            String postImpl = "";
            int o = 0;
            for (String disjunct : Arrays.asList(post.split("\\^"))){
                if (o==0){
                    postImpl = "("+getImplementation(disjunct.split("\\.")[1],all.get(theViewClass))+")";
                } else {
                    postImpl = postImpl+"^("+getImplementation(disjunct.split("\\.")[1],all.get(theViewClass))+")";
                }
                o++;
            }
            Set<List<String>> postSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), Q, theViewClass);
            if (postSet.containsAll(graph.symbolicStates.get(kaseNo).getSymbolicStates())) {

                LOG.debug("before PROVE_KASE applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                LOG.debug(k.toString());

                k.asMethodCallExpr().getArgument(3).remove();
                k.asMethodCallExpr().addArgument("PROVED");
                graph.status.put(k,true);

                LOG.info("PROVE_KASE applied "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                LOG.debug("after PROVE_KASE applied to "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
                LOG.debug(k.toString());
            } else {
                Set<List<String>> a = new HashSet<>(postSet);
                Set<List<String>> b = new HashSet<>(graph.symbolicStates.get(kaseNo).getSymbolicStates());
                b.removeAll(a);
                LOG.info("Postcondition does not cover: "+b.toString()
                        .replaceAll("\\[\\[","[\n [")
                        .replaceAll("\\],","],\n")
                        .replaceAll("\\]\\]","]\n]")
                );
                graph.status.put(k,false);
            }
        }
        if (noOfStatements>0){
            LOG.debug("steps remaining: "+graph.clazz.getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());
            LOG.debug("symbolic states: "+graph.symbolicStates.get(kaseNo).getSymbolicStates().toString());
            LOG.debug("isForall: "+graph.symbolicStates.get(kaseNo).isForall());
        }
        graph.kases.add(k);
        return graph.status.getOrDefault(k,false);
    }

     static void convertToControlledEnglish(){
        StringBuilder sb = new StringBuilder();
        try {
            Class root = Class.forName(entryPointPackageName+"."+rootGraphName);
            startingLetter = 64;
            appendToStringBuilderAndGetNextClassToProcess(root,all.get(root).path,rootGraphName,sb);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("target/"+entryPointPackageName.replaceAll("\\.","_")+"_FLOWS.pdf"));
            document.open();
            Paragraph p = new Paragraph(sb.toString());
            document.add(p);
            document.close();

            startingLetter = 64;
            Document document2 = new Document();
            PdfWriter.getInstance(document2, new FileOutputStream("target/"+entryPointPackageName.replaceAll("\\.","_")+"_DATA.pdf"));
            document2.open();
            for (CompilationUnitWithData cu : all.values().stream().filter(c->
                    c.clazz.isInterface() &&
                            !c.clazz.getSimpleName().startsWith("P") &&
                            !Consumer.class.isAssignableFrom(c.clazz) &&
                            !c.clazz.isAnnotationPresent(Primative.class)).collect(Collectors.toList())) {
                ClassOrInterfaceDeclaration c = cu.compilationUnit.getInterfaceByName(cu.clazz.getSimpleName()).get();
                try {
                    String view = getControlledEnglishOfView(cu.clazz,c);
                    Paragraph p2 = new Paragraph(view);
                    document2.add(p2);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            document2.close();

        } catch (ClassNotFoundException | FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }
        LOG.debug(sb.toString());
    }

    private static String formatCondition(String condition){
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, condition).replaceAll("_"," ")
                .split("->",2)[1]
                .replaceAll("&&","and")
                .replaceAll("\\^","or \n")
                .replaceAll(",","")
                .replaceAll("->",", where each")
                .replaceAll("\\(","")
                .replaceAll("\\)","")
                .replaceAll("\\."," ")
                .replaceAll("forall","each have a ");
    }
    private static void appendToStringBuilderAndGetNextClassToProcess(Class clazz, Path path, String name, StringBuilder sb){
        ParseResult<CompilationUnit> pr = null;
        try {
            pr =  new JavaParser().parse(path);
            if (pr.isSuccessful()){
                CompilationUnit cu = pr.getResult().get();
                ClassOrInterfaceDeclaration graph = cu.getClassByName(name).get();
                Statement kases = graph.getMethodsByName("accept").get(0).getBody().get().getStatement(0);
                sb.append("\n");
                startingLetter++;
                sb.append("Flow "+startingLetter+" - "+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,name).replaceAll("_"," ")+":\n\n");
                int i = 0;
                for (Expression a : kases.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (i==0){

                    } else {
                        sb.append("Case "+i+".\nGiven"+formatCondition(a.asMethodCallExpr().getArguments().get(0).toString())+",\n");
                        if (!PEdge.class.isAssignableFrom(clazz)){
                            int j = 0;
                            for (Statement step : a.asMethodCallExpr().getArgument(2).asLambdaExpr().getBody().asBlockStmt().getStatements()){
                                String stepName = null;
                                if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("join")){
                                    String firstParOrParrStep = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asMethodCallExpr().getArgument(1).asObjectCreationExpr().getType().getName().toString();
                                    StringBuilder steps = new StringBuilder();
                                    steps.append(""+firstParOrParrStep);
                                    for (int arg=2;arg<=step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asMethodCallExpr().getArguments().size();arg++){
                                        String parOrParrStep = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).asMethodCallExpr().getArgument(1).asObjectCreationExpr().getType().getName().toString();
                                        steps.append(" in parallel with "+parOrParrStep);
                                    }
                                    stepName = steps.toString();
                                } else if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seq") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("par") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seqr") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("parr")){
                                    stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asObjectCreationExpr().getType().getName().toString();
                                }
                                if (stepName!=null){
                                    stepName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, stepName);
                                    stepName = stepName.replaceAll("_"," ");
                                    if (j==0){
                                        sb.append(""+stepName);
                                    } else {
                                        sb.append(",\nthen "+stepName);
                                    }
                                }
                                j++;
                            }
                            sb.append(", ");
                            sb.append("\nthen"+formatCondition(a.asMethodCallExpr().getArguments().get(1).toString())+".\n\n");
                        } else {
                            sb.append("then"+formatCondition(a.asMethodCallExpr().getArguments().get(1).toString())+".\n\n");
                        }
                    }
                    i++;
                }

                i = 0;
                for (Expression a : kases.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                    if (i==0){
                        // skip
                    } else {
                        if (!PEdge.class.isAssignableFrom(clazz)){
                            for (Statement step : a.asMethodCallExpr().getArgument(2).asLambdaExpr().getBody().asBlockStmt().getStatements()){
                                String stepName = null;
                                if (step.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().size()==3){
                                    // skip
                                } else if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("join")){
                                    stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).asObjectCreationExpr().getType().getName().toString();
                                } else if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seq") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("par") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seqr") ||
                                        step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("parr")){
                                    stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asObjectCreationExpr().getType().getName().toString();
                                }
                                Class clz = getClassIfSimpleNameIsUniqueInPackage(stepName);
                                if (clz==null){
                                    // skip
                                } else {
                                    CompilationUnitWithData x = all.get(clz);
                                    appendToStringBuilderAndGetNextClassToProcess(clz,x.path,stepName,sb);
                                }
                            }
                        }
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getMatchesCount(String someString, char toCount){
        int count = 0;
        for (int i = 0; i < someString.length(); i++) {
            if (someString.charAt(i) == toCount) {
                count++;
            }
        }
        return count;
    }

    public static boolean checkForPreOrPostsAgainstView(Set<String> presOrposts, Class inter, boolean trueForEqualsFalseforContains){
        Set<String> booleanMethodNames = Arrays.asList(inter.getMethods()).stream().filter(m->m.getReturnType().equals(boolean.class)).map(m->m.getName()).collect(Collectors.toSet());
        if (trueForEqualsFalseforContains){
            if (booleanMethodNames.equals(presOrposts)){
                return true;
            } else {
                LOG.info(presOrposts+" does equal "+booleanMethodNames);
            }
        } else {
            if (booleanMethodNames.containsAll(presOrposts)){
                return true;
            } else {
                LOG.info(presOrposts+" not contained within "+booleanMethodNames);
            }
        }
        return false;
    }

    public static void checkForPreOrPostsAgainstViews(Set<String> presOrposts, Class type, boolean trueForEqualsFalseforContains, AtomicReference<Class> res){
        if (type.isInterface()){
            if (checkForPreOrPostsAgainstView(presOrposts,type,trueForEqualsFalseforContains)){
                res.set(type);
                return;
            }
        } else {
            for (Class i : type.getInterfaces()){
                if (checkForPreOrPostsAgainstView(presOrposts,i,trueForEqualsFalseforContains)){
                    res.set(i);
                    return;
                } else {
                    checkForPreOrPostsAgainstViews(presOrposts,i,trueForEqualsFalseforContains,res);
                }
            }
        }
    }

    public static boolean checkRootKasesForCompletenessAndSoundnessAgainstAView(CompilationUnitWithData cu){
        StringBuilder kasesPreconditionDisjunction = new StringBuilder();
        StringBuilder kasesPostconditionDisjunction = new StringBuilder();
        Set<String> pres = new HashSet<>();
        Set<String> posts = new HashSet<>();
        int count2 = 0;
        for (Expression k : cu.compilationUnit
                .getClassByName(cu.clazz.getSimpleName()).get()
                .getMethodsByName("accept").get(0).getBody().get()
                .asBlockStmt()
                .getStatement(0)
                .asExpressionStmt()
                .getExpression()
                .asMethodCallExpr()
                .getArguments()) {
            if (count2==0){
                count2++;
                continue;
            } else if (count2==1){
                String pre = k.asMethodCallExpr().getArgument(0).toString();
                pres.add(pre.split("->")[1].trim());
                String z = pre.split("->")[0].trim();
                if (pre.contains("forall")){
                    pre = pre.split("->",2)[1].trim();
                } else {
                    pre = pre.split("->")[1].trim();
                }
                String post = k.asMethodCallExpr().getArgument(1).toString();
                posts.add(post.split("->")[1].trim());
                if (post.contains("forall")){
                    post = post.split("->",2)[1].trim();
                } else {
                    post = post.split("->")[1].trim();
                }
                kasesPreconditionDisjunction.append(z+" -> "+pre);
                kasesPostconditionDisjunction.append(z+" -> "+post);
            } else {
                String pre = k.asMethodCallExpr().getArgument(0).toString();
                pres.add(pre.split("->")[1].trim());
                if (pre.contains("forall")){
                    pre = pre.split("->",2)[1].trim();
                } else {
                    pre = pre.split("->")[1].trim();
                }
                String post = k.asMethodCallExpr().getArgument(1).toString();
                posts.add(post.split("->")[1].trim());
                if (post.contains("forall")){
                    post = post.split("->",2)[1].trim();
                } else {
                    post = post.split("->")[1].trim();
                }
                kasesPreconditionDisjunction.append(" ^ "+pre);
                kasesPostconditionDisjunction.append(" ^ "+post);
            }
            count2++;
        }

        if (cu.clazz.isAnnotationPresent(Root.class)){
            if (!cu.clazz.isAnnotationPresent(Infinite.class)){
                List<Method> methods = Arrays.asList(cu.clazz.getDeclaredMethods()).stream().filter(i->!i.getParameterTypes()[0].equals(Object.class) && i.getName().equals("accept")).collect(Collectors.toList());
                if (methods.get(0).getParameterTypes().length==1){
                    Class clazz = methods.get(0).getParameterTypes()[0];
                    AtomicReference<Class> presOk = new AtomicReference<Class>();
                    checkForPreOrPostsAgainstViews(pres,clazz,true,presOk);

                    AtomicReference<Class> postsOk = new AtomicReference<Class>();
                    checkForPreOrPostsAgainstViews(posts,clazz,true,postsOk);

                    if (presOk.get()!=null && postsOk.get()!=null){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static String getDefaultBooleanMethodImplementation(String methodName, CompilationUnitWithData cu){
        if (cu.compilationUnit
                .getInterfaceByName(cu.clazz.getSimpleName()).isPresent()){
            if (cu.compilationUnit.getInterfaceByName(cu.clazz.getSimpleName()).get()
                    .getMethodsByName(methodName).size()==1){
                // skip
            } else {
                return null;
            }
        }
        return cu.compilationUnit
                .getInterfaceByName(cu.clazz.getSimpleName()).get()
                .getMethodsByName(methodName).get(0).getBody().get().getStatement(0).toString()
                .replaceAll("return","").replaceAll(";","").trim();
    }

    static String getDefaultBooleanMethodImplementation(String methodName){
        for (CompilationUnitWithData cu : all.values().stream().filter(c->c.clazz.isInterface()).collect(Collectors.toList())){
            String res = getDefaultBooleanMethodImplementation(methodName, cu);
            if (res!=null){
                return res;
            }
        }
        return null;
    }

    private static char startingLetter = 65;
    static String getControlledEnglishOfView(Class clazz, ClassOrInterfaceDeclaration c) throws ClassNotFoundException {
        // field methods need to be declared in alphabetical order
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().sorted(Comparator.comparing(Method::getName)).filter(m -> !m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        // use this one instead.
        List<MethodDeclaration> fieldMethodDeclarations = c.getMethods().stream().filter(m -> !m.isDefault() && !m.getType().asString().equals("boolean")).collect(Collectors.toList());

        if (!fields.stream().map(f -> f.getName()).collect(Collectors.toList()).equals(
                fieldMethodDeclarations.stream().map(f -> f.getName().asString()).collect(Collectors.toList()))
        ) {
            throw new IllegalStateException("fields of view must be declared in alphabetical order.");
        }

        StringBuilder sb = new StringBuilder();
        startingLetter++;
        sb.append("Data "+startingLetter+" - "+clazz.getSimpleName().toLowerCase()+":\n\n");
        List<Method> viewProps = Arrays.asList(clazz.getMethods()).stream().filter(m -> m.isDefault() && m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        Set<String> viewPropsNamesSet = viewProps.stream().map(m -> m.getName()).collect(Collectors.toSet());
        if (fields.size() == 1 && !fields.get(0).isDefault() && Collection.class.isAssignableFrom(fields.get(0).getReturnType())) {
            // check defaults of view

            viewPropsNamesSet.remove("isEmpty");

            int no = 1;
            for (String m : viewPropsNamesSet) {
                String impl = getImplementation(m, all.get(clazz)).replaceAll(" ", "");
                String[] split = impl.split("->");
                String collectionVar = split[0].split("\\.")[0].replaceAll("\\(\\)", "");
                String var = split[1].split("\\.")[0];
                impl = split[1].replaceAll("\\(", "").replaceAll("\\)", "");
                impl = impl.replaceAll(var+"\\.","");

                String methodInEnglish = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,m).replaceAll("_"," ");
                sb.append("Partition "+no+". "+methodInEnglish+"" + ", means, for every " + var + " in " + collectionVar + ", " + lowerCamelToEnglishForEachInSplit(impl)
                        .replaceAll("\\!", "not ")
                        .replaceAll("\\^", " xor ")
                        .replaceAll("\\&\\&", " and ")
                        .replaceAll("\\&", " and ")
                        .replaceAll("\\|\\|", " or ")
                        .replaceAll("\\|", " or ")+".\n\n");
                no++;
            }
        } else {
            int no = 1;
            for (String m : viewPropsNamesSet) {
                String impl = getImplementation(m, all.get(clazz)).replaceAll(" ", "");
                impl = impl.replaceAll("\\(", "").replaceAll("\\)", "");
                impl = impl.replaceAll("\\."," ");
                String methodInEnglish = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,m).replaceAll("_"," ");
                sb.append("Partition "+no+". "+methodInEnglish + ", means, "+lowerCamelToEnglishForEachInSplit(impl)
                        .replaceAll("\\!", "not ")
                        .replaceAll("\\^", " or ")
                        .replaceAll("\\&\\&", " and ")
                        .replaceAll("\\&", " and ")
                        .replaceAll("\\|\\|", " or ")
                        .replaceAll("\\|", " or ")+".\n\n");
                no++;
            }
        }
        return sb.toString();
    }

    private static String lowerCamelToEnglishForEachInSplit(String toSplit){
        String[] split = toSplit.split(" ");
        StringBuilder sb = new StringBuilder();
        sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,split[0]).replaceAll("_"," "));
        for (int i=1;i<split.length; i++){
            sb.append(" "+CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,split[i]).replaceAll("_"," "));
        }
        return sb.toString();
    }
}
