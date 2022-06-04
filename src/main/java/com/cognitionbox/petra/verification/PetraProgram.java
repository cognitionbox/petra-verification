package com.cognitionbox.petra.verification;

import com.cognitionbox.petra.annotations.*;
import com.cognitionbox.petra.lang.step.PGraph;
import com.cognitionbox.petra.verification.tasks.ProveKaseTask;
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
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

import static com.cognitionbox.petra.verification.Strings.*;
import static org.junit.Assert.fail;

public class PetraProgram {

    final static Logger LOG = new Logger();

    static final String rootDir = new File(ROOT_DIR_PATH).getPath();
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
                    .filter(i->i.getFileName().toString().contains(JAVA_FILE_EXT))
                    .forEach(path->{
                        JavaParser jp = new JavaParser();
                        try {
                            ParseResult<CompilationUnit> pr = jp.parse(path);
                            if (pr.isSuccessful()){
                                Class clazz = null;
                                try {
                                    clazz = Class.forName(pr.getResult().get().getPackageDeclaration().get().getName()+DOT+pr.getResult().get().getPrimaryTypeName().get().toString());
                                    if (!clazz.getPackage().toString().contains(entryPointPackageName)){
                                        return;
                                    }
                                } catch (NoSuchElementException e){
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                if (clazz.isAnnotationPresent(View.class)){
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

    static SymbolicState getViewTruth(Class clazz) throws ClassNotFoundException {
        int i = 0;
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().filter(m->!Modifier.isStatic(m.getModifiers()) && !m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        Set<String>[] methodNames = null;
        if (fields.size()==1 && !fields.get(0).isDefault() && Collection.class.isAssignableFrom(fields.get(0).getReturnType())) {
            // check defaults of view
            Class<?> elementType = (Class<?>) ((ParameterizedType) fields.get(0).getGenericReturnType()).getActualTypeArguments()[0];
            CompilationUnitWithData cu = all.get(elementType);
            ClassOrInterfaceDeclaration declaration = cu.getCompilationUnit().getInterfaceByName(elementType.getSimpleName()).get();
            methodNames = new Set[1];
            methodNames[0] = declaration.getMethods().stream().filter(m->m.getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).map(m->m.asMethodDeclaration().getName().toString()).collect(Collectors.toSet());
            methodNames[0].add(IS_EMPTY);
            Set<List<String>> truth = Sets.cartesianProduct(methodNames);
            return new SymbolicState(truth,true);
        } else {
            methodNames = new Set[fields.size()];
            for (Class<?> t : fields.stream().filter(m->!Modifier.isStatic(m.getModifiers()) && !m.isDefault() && !m.getReturnType().equals(boolean.class)).map(f->f.getReturnType()).collect(Collectors.toList())){
                if (t.getSimpleName().contains(COLLECTION)){
                    continue;
                } else {
                    Class cls = getClassIfSimpleNameIsUniqueInPackage(t.getSimpleName());
                    CompilationUnitWithData cu2 = all.get(cls);
                    ClassOrInterfaceDeclaration declaration = cu2.getCompilationUnit().getInterfaceByName(t.getSimpleName()).get();
                    methodNames[i] = declaration.getMethods().stream().filter(m->m.getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).map(m->m.asMethodDeclaration().getName().toString()).collect(Collectors.toSet());
                    i++;
                }
            }
            Set<List<String>> truth = Sets.cartesianProduct(methodNames);
            return new SymbolicState(truth,false);
        }
    }

    static Set<List<String>> filterStatesUsingBooleanPrecondition(Set<List<String>> states, boolean forall, String expression, Class clazz){
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().filter(m->!Modifier.isStatic(m.getModifiers()) && !m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        String r = expression;
        r = r.replaceAll(DOT_ESCAPED,DOT+EQUALS+OPEN_BRACKET+DOUBLE_QUOTE_ESCAPED);
        if (forall){
            String newP = r.replaceAll(fields.get(0).getName()+OPEN_CLOSED_BRACKETS_ESCAPED,"list.get"+OPEN_BRACKET+0+CLOSED_BRACKET);
            if (r.equals(newP)){
                Class<?> elementType = (Class<?>) ((ParameterizedType) fields.get(0).getGenericReturnType()).getActualTypeArguments()[0];
                String mn = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,elementType.getSimpleName());
                r = r.replaceAll(mn+DOT_ESCAPED,"list.get"+OPEN_BRACKET+0+CLOSED_BRACKET+DOT_ESCAPED);
                r = r.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,DOUBLE_QUOTE_ESCAPED+CLOSED_BRACKET);
            } else {
                r = newP.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,DOUBLE_QUOTE_ESCAPED+CLOSED_BRACKET);
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
                r = r.replaceAll(fields.get(x).getName()+OPEN_CLOSED_BRACKETS_ESCAPED,"list.get"+OPEN_BRACKET+x+CLOSED_BRACKET);
                r = r.replaceAll(fields.get(x).getName(),"list.get"+OPEN_BRACKET+x+CLOSED_BRACKET); // hack!!! only on of these two lines should be enough
            }
            r = r.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,DOUBLE_QUOTE_ESCAPED+CLOSED_BRACKET);
        }
        r = RETURN+SPACE+r+SEMI_COLON;
        String uuid = "Precondition"+UNDERSCORE+UUID.randomUUID().toString().replaceAll(DASH,UNDERSCORE);
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

    static boolean isViewSoundAndComplete(Class clazz, ClassOrInterfaceDeclaration c) {
                int i = 0;
                // field methods need to be declared in alphabetical order
        List<Method> fields = Arrays.asList(clazz.getMethods()).stream().sorted(Comparator.comparing(Method::getName)).filter(m->!Modifier.isStatic(m.getModifiers()) && !m.isDefault() && !m.getReturnType().equals(boolean.class)).collect(Collectors.toList());
        // use this one instead.
        List<MethodDeclaration> fieldMethodDeclarations = c.getMethods().stream().filter(m->!m.isStatic() && !m.isDefault() && !m.getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).collect(Collectors.toList());

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
            viewPropsNamesSet.remove(IS_EMPTY);
            // Now we don't have to declare all these methods like with the non-collection view types
            boolean collectionViewOk = true;
            String invalidCollectionViewPropositionName = null;
            String invalidCollectionViewPropositionImpl = null;
            String matchedVar = null;
            String matchedCollectionVar = null;
            for (String m : viewPropsNamesSet){
                String impl = getImplementation(m,all.get(clazz)).replaceAll(SPACE,BLANK);
                String[] split = impl.split(ARROW);
                String collectionVar = split[0].split(DOT_ESCAPED)[0].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK);
                String var = split[1].split(DOT_ESCAPED)[0];
                impl = split[1].replaceAll(OPEN_BRACKET_ESCAPED,BLANK).replaceAll(CLOSED_BRACKET_ESCAPED,BLANK);

                LOG.info(m+COLON+NEW_LINE+FORALL+" "+var+" in "+collectionVar+", "+impl
                        .replaceAll(XOR_ESCAPED,SPACE+"XOR"+SPACE)
                        .replaceAll(AND_ESCAPED+AND_ESCAPED,SPACE+"AND"+SPACE)
                        .replaceAll(AND_ESCAPED,SPACE+"AND"+SPACE)
                        .replaceAll(OR_ESCAPED+OR_ESCAPED,SPACE+"OR"+SPACE)
                        .replaceAll(OR_ESCAPED,SPACE+"OR"+SPACE));

                invalidCollectionViewPropositionName = m;
                invalidCollectionViewPropositionImpl = impl;
                matchedVar = var;
                matchedCollectionVar = collectionVar;

                impl = impl.replaceAll(var+DOT_ESCAPED,BLANK);
                List<String> list = Arrays.asList(impl.split(XOR_ESCAPED));
                if (!orderedPs.contains(list)){
                    collectionViewOk = false;
                    break;
                }
            }
            if (!collectionViewOk){
                throw new IllegalStateException("Invalid collection view."+NEW_LINE+"The following universal quantification does not use a valid proposition."+NEW_LINE+"Valid propositions for collection views can only be exclusive disjunctions (without negations)"+NEW_LINE+"of the disjuncts from the underlying type:"+NEW_LINE+NEW_LINE+
                        invalidCollectionViewPropositionName+OPEN_BRACKET+CLOSED_BRACKET+OPEN_CURLY_BRACKET+NEW_LINE+TAB+RETURN+SPACE+matchedCollectionVar+OPEN_BRACKET+CLOSED_BRACKET+DOT+FORALL+OPEN_BRACKET+matchedVar+ARROW+invalidCollectionViewPropositionImpl+CLOSED_BRACKET+SEMI_COLON+NEW_LINE+CLOSED_CURLY_BRACKET);
            } else {
                LOG.info("Collection view is sound as all universal quantifications use a valid proposition.");
                return true;
            }
        } else if (fields.stream().filter(f->!f.isDefault() && Collection.class.isAssignableFrom(f.getReturnType())).count() > 1){
            // not allowed
            throw new IllegalStateException("Cannot have two collections directly in a view.");
        }

        Set<String>[] methodNames = new Set[fields.size()];
        for (Class<?> t : fields.stream().map(f->f.getReturnType()).collect(Collectors.toList())){
            if (t.getSimpleName().contains(COLLECTION)){
                continue;
            } else {
                Class cls = getClassIfSimpleNameIsUniqueInPackage(t.getSimpleName());
                methodNames[i] = Arrays.asList(cls.getMethods()).stream().filter(m->m.isDefault() && m.getReturnType().equals(boolean.class)).map(m->m.getName()).collect(Collectors.toSet());
                i++;
            }
        }

        Set<MethodDeclaration> methodDeclarations = c.getMethods().stream().filter(m->m.isDefault() && m.getType().isPrimitiveType() && m.getType().asPrimitiveType().getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).collect(Collectors.toSet());

        if (clazz.getInterfaces().length>0){
            Class superClass = clazz.getInterfaces()[0];
            methodDeclarations.addAll(all.get(superClass).getCompilationUnit().getInterfaceByName(superClass.getSimpleName()).get().getMethods()
                    .stream().filter(m->m.isDefault() && m.getType().isPrimitiveType() && m.getType().asPrimitiveType().getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).collect(Collectors.toSet()));
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
            p = p.replaceAll(DOT_ESCAPED,DOT+EQUALS+OPEN_BRACKET+DOUBLE_QUOTE_ESCAPED);
            for (Integer x : indicies){
                p = p.replaceAll(OPEN_BRACKET_ESCAPED+fields.get(x).getName()+OPEN_CLOSED_BRACKETS_ESCAPED+DOT_ESCAPED,"(list.get"+OPEN_BRACKET+x+CLOSED_BRACKET+DOT);
                p = p.replaceAll(NOT_ESCAPED+fields.get(x).getName()+OPEN_CLOSED_BRACKETS_ESCAPED+DOT_ESCAPED,NOT+"list.get"+OPEN_BRACKET+x+CLOSED_BRACKET+DOT);
                p = p.replaceAll(SPACE+fields.get(x).getName()+OPEN_CLOSED_BRACKETS_ESCAPED+DOT_ESCAPED,SPACE+"list.get"+OPEN_BRACKET+x+CLOSED_BRACKET+DOT);
            }
            p = p.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,DOUBLE_QUOTE_ESCAPED+CLOSED_BRACKET);
            String viewDisjunctPredicateClassName =
                    "ViewDisjunct"+UNDERSCORE+
                    UUID.randomUUID().toString().replaceAll(DASH,UNDERSCORE)+UNDERSCORE+
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
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
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
                                .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                                .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                                .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
                        );
                    }
                }
            }
        }
        for (MethodDeclarationAndSet m : methodDeclarationAndSets){
            if (m.set.isEmpty()){
                LOG.info(m.methodDeclaration.getNameAsString()+SPACE+"is empty.");
                isSound = false;
            }
        }
        if (overridesMethodFromBaseInterface(clazz)){
            LOG.info(clazz.getSimpleName()+SPACE+"overrides methods from"+SPACE+clazz.getInterfaces()[0].getSimpleName());
            isSound = false;
        }
        if (isComplete){
            List t = truth.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            List a = all.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            LOG.info("all cases: "+t.toString()
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
            );
            LOG.info("covered cases: "+a.toString()
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
            );
        } else {
            List t = truth.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            List a = all.stream().map(e->e.toString()).sorted().collect(Collectors.toList());
            LOG.info("all cases: "+t.toString()
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
            );
            LOG.info("covered cases: "+a.toString()
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
            );
            Set<List<String>> m = new HashSet<>(truth);
            m.removeAll(all);
            LOG.info("missing cases: "+m.toString()
                    .replaceAll(OPEN_SQUARE_BRACKET_ESCAPED+OPEN_SQUARE_BRACKET_ESCAPED,OPEN_SQUARE_BRACKET+NEW_LINE+SPACE+OPEN_SQUARE_BRACKET)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+COMMA,CLOSED_SQUARE_BRACKET+COMMA+NEW_LINE)
                    .replaceAll(CLOSED_SQUARE_BRACKET_ESCAPED+CLOSED_SQUARE_BRACKET_ESCAPED,CLOSED_SQUARE_BRACKET+NEW_LINE+CLOSED_SQUARE_BRACKET)
            );
            LOG.info("not all cases covered, therefore is not complete.");
        }
        if (isSound){
            LOG.info("View is sound as there are no overlaps.");
        } else {
            LOG.info("View is not sound as there are overlaps or there are empty sets or view default boolean methods override base interface methods.");
        }
        dataTypeInfoMap.put(clazz,new DataTypeInfo(Sets.cartesianProduct(methodNames),methodDeclarations));

        return true;
        //return isSound && isComplete;
    }

    static void rewriteJoinForallParSteps(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
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
                            LOG.debug("before PAR_FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
                        String x = ref.split(DOT_ESCAPED)[0];
                        step.getArguments().replace(step.getArgument(0),new JavaParser().parseExpression(x).getResult().get());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+ARROW+ref+"."+preWithForall.toString());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+ARROW+ref+"."+postWithForall.toString());
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
            LOG.info("PAR_FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after PAR_FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
    }

    static void rewriteSingleParStepToSeqStep(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
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
                    LOG.debug("before SINGLE_PAR_TO_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
            LOG.info("SINGLE_PAR_TO_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after SINGLE_PAR_TO_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
    }

    static void rewriteStepsWithForall(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
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
                            LOG.debug("before FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
                        String x = ref.split(DOT_ESCAPED)[0];
                        step.getArguments().replace(step.getArgument(0),new JavaParser().parseExpression(x).getResult().get());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+ARROW+ref+"."+preWithForall.toString());
                        step.getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().addArgument(x+ARROW+ref+"."+postWithForall.toString());
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
                    LOG.info("FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug("after FORALL_INTRO applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug(BLANK);
                }
    }

    static void rewriteSingleJoinPar(ProveKaseTask task) {
                Expression kase = task.getKase();
                CompilationUnitWithData cu = task.getCompilationUnitWithData();
                int count = task.getCount();
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
                            LOG.debug("before SIMPLIFY_SINGLE_JOIN applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
                    LOG.info("SIMPLIFY_SINGLE_JOIN applied "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug("after SIMPLIFY_SINGLE_JOIN applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug(BLANK);
                }
    }

    static void rewriteGraphKaseJoinParsOrSeperatedSeqsToSeq(Expression kase, CompilationUnitWithData cu, int count) {
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before JOIN_PARS_OR_SEPARATED_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            beforeLogged = true;
        }

        Class dataType = Arrays.asList(cu.getClazz().getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals(ACCEPT)).findFirst().get().getParameterTypes()[0];
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
                            preConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                            postConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                            fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split(ARROW)[1].split(DOT_ESCAPED)[1].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).trim());
                        } else {
                            preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                            postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                            fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split(ARROW)[1].split(DOT_ESCAPED)[1].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).trim());
                        }
                        a++;
                    }
                } else if (stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("seq") &&
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")){
                    preConjunction.append("("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                    postConjunction.append("("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                    fields.remove(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(0).toString().split(DOT_ESCAPED)[1].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).trim());
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
                        preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        fields.remove(par.asMethodCallExpr().getArguments().get(0).toString().split(ARROW)[1].split(DOT_ESCAPED)[1].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).trim());
                    }
                    a++;
                }
            } else if (started &&
                    stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("seq") &&
                    stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().contains(".")
            ){
                preConjunction.append("&("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                postConjunction.append("&("+stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                fields.remove(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(0).toString().split(DOT_ESCAPED)[1].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).trim());
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
            LOG.info("JOIN_PARS_OR_SEPARATED_SEQ applied "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after JOIN_PARS_OR_SEPARATED_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
    }

    static void rewriteGraphKaseJoinParsToSeq(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before JOIN_PARS_TO_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
                Class dataType = cu.getClazz();
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
                        preConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        postConjunction.append("("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                    } else {
                        preConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        postConjunction.append("&("+par.asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
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
                kse.addArgument("x->"+preConjunction.toString());
                kse.addArgument("x->"+postConjunction.toString());
                kse.addArgument(new LambdaExpr(new Parameter(), new BlockStmt()));
                kse.addArgument("ASSUMED");
                kases.addArgument(kse);
                break; // hack!!! need to find out why we are getting ConcurrentModificationException without this.
            }
            i++;
        }

        if (beforeLogged){
            LOG.info("JOIN_PARS_TO_SEQ applied "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after JOIN_PARS_TO_SEQ applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
    }

    static void rewriteGraphKaseSeperatedStepsToNonSeperatedSteps(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
        boolean beforeLogged = false;
        // go through step instructions
        if (!beforeLogged){
            LOG.debug("before SEPERATED_TO_NON_SEPERATED applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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
                Class dataType = cu.getClazz();
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
                        preConjunction.append("("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        postConjunction.append("("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                    } else {
                        preConjunction.append("&("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                        postConjunction.append("&("+s.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().get(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0).asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].replaceAll(SPACE,BLANK)+")");
                    }
                    c++;
                    s.remove();
                }

                MethodCallExpr kse = new MethodCallExpr("kase");
                kse.addArgument("x->"+preConjunction.toString());
                kse.addArgument("x->"+postConjunction.toString());
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
            LOG.info("SEPERATED_TO_NON_SEPERATED applied "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after SEPERATED_TO_NON_SEPERATED applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
    }

    static void rewriteGraphKaseJoinParStepsToEdges(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
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
                LOG.debug("before RESOLVE_JOIN_PAR and KASE_COMB applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
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

                    if (parOrParr.asMethodCallExpr().getArgument(arg).toString().contains("::")) {
                        String[] split = parOrParr.asMethodCallExpr().getArgument(arg).toString().split("::");
                        String methodName = split[1];
                        Class c = getClassIfSimpleNameIsUniqueInPackage(split[0]);
                        if (c==null){
                            LOG.debug(split[0]+": cannot find class in local package "+cu.getCompilationUnit().getPackageDeclaration().get().getNameAsString());
                            cantFindClass = true;
                            continue;
                        }
                        parOrParr.asMethodCallExpr().getArgument(arg).remove();
                        BlockStmt blockStmt = all.get(c).getCompilationUnit()
                                .getInterfaceByName(c.getSimpleName()).get()
                                .getMethodsByName(methodName).get(0).getBody().get();
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
                        List<Expression> ks = lambdaExpr.getBody().asBlockStmt().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr().getArguments();
                        int noOfkases = ks.size();
                        for (Expression k : ks) {
                            if (count2==0){
                                count2++;
                                continue;
                            } else if (count2==1){
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String preProp = pre.split(ARROW)[1];
                                    String preRefRoot = preProp.split(DOT_ESCAPED)[0];
                                    pre = root+ARROW+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(SPACE,BLANK);
                                }
                                pres.add(pre);
                                String z = pre.split(ARROW)[0].trim();
                                if (pre.contains("forall")){
                                    pre = pre.split(ARROW,2)[1].trim();
                                } else {
                                    pre = pre.split(ARROW)[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String postProp = post.split(ARROW)[1];
                                    String postRefRoot = postProp.split(DOT_ESCAPED)[0];
                                    post = root+ARROW+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split(ARROW,2)[1].trim();
                                } else {
                                    post = post.split(ARROW)[1].trim();
                                }
                                if (noOfkases-1>1){
                                    kasesPreconditionDisjunction.append(z+" -> ("+pre+")");
                                    kasesPostconditionDisjunction.append(z+" -> ("+post+")");
                                } else if (noOfkases-1==1){
                                    kasesPreconditionDisjunction.append(z+" -> "+pre);
                                    kasesPostconditionDisjunction.append(z+" -> "+post);
                                }

//                                try {
//                                    cu.addInOutSymbolicStatesMappingForKase(task.getCount(),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(task.getViewClass()).getSymbolicStates(),
//                                                    false,
//                                                    resolve(pre,task.getViewClass()),
//                                                    task.getViewClass()),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(task.getViewClass()).getSymbolicStates(),
//                                                    false,
//                                                    resolve(post,task.getViewClass()),
//                                                    task.getViewClass()));
//                                } catch (ClassNotFoundException e){
//                                    e.printStackTrace();
//                                }

                            } else {
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String preProp = pre.split(ARROW)[1];
                                    String preRefRoot = preProp.split(DOT_ESCAPED)[0];
                                    pre = root+ARROW+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(SPACE,BLANK);
                                }
                                pres.add(pre);
                                if (pre.contains("forall")){
                                    pre = pre.split(ARROW,2)[1].trim();
                                } else {
                                    pre = pre.split(ARROW)[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String postProp = post.split(ARROW)[1];
                                    String postRefRoot = postProp.split(DOT_ESCAPED)[0];
                                    post = root+ARROW+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split(ARROW,2)[1].trim();
                                } else {
                                    post = post.split(ARROW)[1].trim();
                                }
                                kasesPreconditionDisjunction.append(" ^ ("+pre+")");
                                kasesPostconditionDisjunction.append(" ^ ("+post+")");

//                                try {
//                                    cu.addInOutSymbolicStatesMappingForKase(task.getCount(),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(task.getViewClass()).getSymbolicStates(),
//                                                    false,
//                                                    resolve(pre,task.getViewClass()),
//                                                    task.getViewClass()),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(task.getViewClass()).getSymbolicStates(),
//                                                    false,
//                                                    resolve(post,task.getViewClass()),
//                                                    task.getViewClass()));
//                                } catch (ClassNotFoundException e){
//                                    e.printStackTrace();
//                                }
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
                        Queue<String> calls = Arrays.asList(parOrParr.asMethodCallExpr().getArgument(0).toString().split(DOT_ESCAPED)).stream().collect(Collectors.toCollection(()->new LinkedList<>()));
                        Class dataType = Arrays.asList(cu.getClazz().getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals(ACCEPT)).findFirst().get().getParameterTypes()[0];
                        Class current = dataType;
                        calls.poll();
                        while(calls.size()>0){
                            try {
                                Class returnType = current.getMethod(calls.peek().replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).getReturnType();
                                if (Collection.class.isAssignableFrom(returnType)){
                                    current = (Class) ((ParameterizedType) current.getMethod(calls.peek().replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).getGenericReturnType()).getActualTypeArguments()[0];
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
                        String x = parOrParr.asMethodCallExpr().getArgument(0).toString().split(BLANK)[0].toLowerCase();
                        String p = parOrParr.asMethodCallExpr().getArgument(1).toString();
                        String q = parOrParr.asMethodCallExpr().getArgument(2).toString();
                        String refFullname = parOrParr.asMethodCallExpr().getArgument(0).toString();
                        kasesPreconditionDisjunction.append(p.replaceAll(SPACE,BLANK).replaceAll(x+ARROW,refFullname.split(DOT_ESCAPED)[0]+ARROW).replaceAll(x+DOT_ESCAPED,refFullname+"."));
                        kasesPostconditionDisjunction.append(q.replaceAll(SPACE,BLANK).replaceAll(x+ARROW,refFullname.split(DOT_ESCAPED)[0]+ARROW).replaceAll(x+DOT_ESCAPED,refFullname+"."));
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
            LOG.info("RESOLVE_JOIN_PAR and KASE_COMB applied "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug("after RESOLVE_JOIN_PAR and KASE_COMB applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
            LOG.debug(kase.toString());
            LOG.debug(BLANK);
        }
        if (cantFindClass){
            throw new IllegalStateException("cant find classes.");
        }
        // intro graph
        kase.asMethodCallExpr().addArgument("UNPROVED");
        kase.asMethodCallExpr().addArgument(kase.asMethodCallExpr().getArgument(0).clone());
    }

    static void rewriteGraphKaseStepsToEdges(ProveKaseTask task) {
        Expression kase = task.getKase();
        CompilationUnitWithData cu = task.getCompilationUnitWithData();
        int count = task.getCount();
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
                        LOG.debug("before RESOLVE and KASE_COMB applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
                        LOG.debug(kase.toString());
                        beforeLogged = true;
                    }
                    int arg = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArguments().size() - 1;
                    if (!stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getName().asString().equals("join") &&
                            stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).toString().contains("::")) {
                        String[] split = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).toString().split("::");
                        String methodName = split[1];
                        Class c = getClassIfSimpleNameIsUniqueInPackage(split[0]);
                        if (c==null){
                            LOG.debug(split[0]+": cannot find class in local package "+cu.getCompilationUnit().getPackageDeclaration().get().getNameAsString());
                            cantFindClass = true;
                            continue;
                        }
                        stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).remove();
                        BlockStmt blockStmt = all.get(c).getCompilationUnit()
                                .getInterfaceByName(c.getSimpleName()).get()
                                .getMethodsByName(methodName).get(0).getBody().get();
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
                        List<Expression> ks = lambdaExpr.getBody().asBlockStmt().getStatements().get(0).asExpressionStmt().getExpression().asMethodCallExpr().getArguments();
                        int noOfkases = ks.size();
                        for (Expression k : ks) {
                            if (count2==0){
                                count2++;
                                continue;
                            } else if (count2==1){
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String preProp = pre.split(ARROW)[1];
                                    String preRefRoot = preProp.split(DOT_ESCAPED)[0];
                                    pre = root+ARROW+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(SPACE,BLANK);
                                }
                                pres.add(pre);
                                String z = pre.split(ARROW)[0].trim();
                                if (pre.contains("forall")){
                                    pre = pre.split(ARROW,2)[1].trim();
                                } else {
                                    pre = pre.split(ARROW)[1].trim();
                                }
                                String post;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    post = k.asMethodCallExpr().getArgument(3).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(2).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String postProp = post.split(ARROW)[1];
                                    String postRefRoot = postProp.split(DOT_ESCAPED)[0];
                                    post = root+ARROW+postProp.replaceAll(postRefRoot,ref);
                                } else {
                                    post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                }
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split(ARROW,2)[1].trim();
                                } else {
                                    post = post.split(ARROW)[1].trim();
                                }
                                if (noOfkases-1>1){
                                    kasesPreconditionDisjunction.append(z+" -> ("+pre+")");
                                    kasesPostconditionDisjunction.append(z+" -> ("+post+")");
                                } else if (noOfkases-1==1){
                                    kasesPreconditionDisjunction.append(z+" -> "+pre);
                                    kasesPostconditionDisjunction.append(z+" -> "+post);
                                }

//                                try {
//                                    cu.addInOutSymbolicStatesMappingForKase(task.getCount(),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(c).getSymbolicStates(),
//                                                    false,
//                                                    resolve(pre,c),
//                                                    c),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(c).getSymbolicStates(),
//                                                    false,
//                                                    resolve(post,c),
//                                                    c));
//                                } catch (ClassNotFoundException e){
//                                    e.printStackTrace();
//                                }

                            } else {
                                String pre;
                                if (k.asMethodCallExpr().getName().asString().equals("invkase")){
                                    pre = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                    String ref = k.asMethodCallExpr().getArgument(0).toString().split(ARROW)[1].replaceAll(SPACE,BLANK);
                                    String root = ref.split(DOT_ESCAPED)[0];
                                    String preProp = pre.split(ARROW)[1];
                                    String preRefRoot = preProp.split(DOT_ESCAPED)[0];
                                    pre = root+ARROW+preProp.replaceAll(preRefRoot,ref);
                                } else {
                                    pre = k.asMethodCallExpr().getArgument(0).toString().replaceAll(SPACE,BLANK);
                                }
                                pres.add(pre);
                                if (pre.contains("forall")){
                                    pre = pre.split(ARROW,2)[1].trim();
                                } else {
                                    pre = pre.split(ARROW)[1].trim();
                                }
                                String post = k.asMethodCallExpr().getArgument(1).toString().replaceAll(SPACE,BLANK);
                                posts.add(post);
                                if (post.contains("forall")){
                                    post = post.split(ARROW,2)[1].trim();
                                } else {
                                    post = post.split(ARROW)[1].trim();
                                }
                                kasesPreconditionDisjunction.append(" ^ ("+pre+")");
                                kasesPostconditionDisjunction.append(" ^ ("+post+")");

//                                try {
//                                    cu.addInOutSymbolicStatesMappingForKase(task.getCount(),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(c).getSymbolicStates(),
//                                                    false,
//                                                    resolve(pre,c),
//                                                    c),
//                                            filterStatesUsingBooleanPrecondition(
//                                                    getViewTruth(c).getSymbolicStates(),
//                                                    false,
//                                                    resolve(post,c),
//                                                    c));
//                                } catch (ClassNotFoundException e){
//                                    e.printStackTrace();
//                                }
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
                        Queue<String> calls = Arrays.asList(stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().split(DOT_ESCAPED)).stream().collect(Collectors.toCollection(()->new LinkedList<>()));
                        Class dataType = Arrays.asList(cu.getClazz().getMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) && m.getName().equals(ACCEPT)).findFirst().get().getParameterTypes()[0];
                        Class current = dataType;
                        calls.poll();
                        while(calls.size()>0){
                            try {
                                Class returnType = current.getMethod(calls.peek().replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).getReturnType();
                                if (Collection.class.isAssignableFrom(returnType)){
                                    current = (Class) ((ParameterizedType) current.getMethod(calls.peek().replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).getGenericReturnType()).getActualTypeArguments()[0];
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
                        String x = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString().split(BLANK)[0].toLowerCase();
                        String p = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).toString();
                        String q = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(2).toString();
                        String refFullname = stepInstruction.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).toString();
                        kasesPreconditionDisjunction.append(p.replaceAll(SPACE,BLANK).replaceAll(x+ARROW,refFullname.split(DOT_ESCAPED)[0]+ARROW).replaceAll(x+DOT_ESCAPED,refFullname+"."));
                        kasesPostconditionDisjunction.append(q.replaceAll(SPACE,BLANK).replaceAll(x+ARROW,refFullname.split(DOT_ESCAPED)[0]+ARROW).replaceAll(x+DOT_ESCAPED,refFullname+"."));
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
                    LOG.info("RESOLVE and KASE_COMB applied "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug("after RESOLVE and KASE_COMB applied to "+cu.getClazz().getSimpleName()+" kase:"+count);
                    LOG.debug(kase.toString());
                    LOG.debug(BLANK);
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
                    String res = pn.replaceAll(pt,BLANK);
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
        String pre = preCondition.replaceAll(OPEN_BRACKET_ESCAPED,"").replaceAll(CLOSED_BRACKET_ESCAPED,"");
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split(XOR)).stream().map(s->s.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).collect(Collectors.toList()));
        // abstraction / concretion check
        AtomicReference<Class> preRes2 = new AtomicReference();
        AtomicReference<Class> postRes2 = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.getClazz(),false,preRes2);
        if (preRes2.get()!=null ^ postRes2.get()!=null){
            // check implementation is valid
            if (preRes2.get()!=null && postRes2.get()==null){
                List<MethodDeclaration> method = all.get(preRes2.get()).getCompilationUnit().getInterfaceByName(preRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0));
                if (method.size()>0){
                    String impl = method.get(0).getBody().get().getStatement(0).toString().replaceAll(RETURN,BLANK);
                    return impl.trim().replaceAll(SEMI_COLON,BLANK);
                } else {
                    Class clazz = preRes2.get().getInterfaces()[0];
                    method = all.get(clazz).getCompilationUnit().getInterfaceByName(clazz.getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0));
                    String impl = method.get(0).getBody().get().getStatement(0).toString().replaceAll(RETURN,BLANK);
                    return impl.trim().replaceAll(SEMI_COLON,BLANK);
                }
            }
        }
        return null;
    }

    static void checkCastPreAndPostConditionsEitherGoBetweenWholeViewsOrBetweenAbstractionsAndImplementations(Expression cast, CompilationUnitWithData graphTerm) {
        String x = cast.asMethodCallExpr().getArgument(1).toString().split(ARROW)[0].trim();
        String pre = cast.asMethodCallExpr().getArgument(1).toString().split(ARROW,2)[1].trim().replaceAll(x+DOT_ESCAPED,BLANK);
        String post = cast.asMethodCallExpr().getArgument(2).toString().split(ARROW,2)[1].trim().replaceAll(x+DOT_ESCAPED,BLANK);
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split(XOR)).stream().map(s->s.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).collect(Collectors.toList()));
        Set<String> postSet = new HashSet<>(Arrays.asList(post.split(XOR)).stream().map(s->s.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK)).collect(Collectors.toList()));
        AtomicReference<Class> preRes = new AtomicReference();
        AtomicReference<Class> postRes = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.getClazz(),true,preRes);
        checkForPreOrPostsAgainstViews(postSet,graphTerm.getClazz(),true,postRes);

        // abstraction / concretion check
        AtomicReference<Class> preRes2 = new AtomicReference();
        AtomicReference<Class> postRes2 = new AtomicReference();
        checkForPreOrPostsAgainstViews(preSet,graphTerm.getClazz(),false,preRes2);
        checkForPreOrPostsAgainstViews(postSet,graphTerm.getClazz(),false,postRes2);

        if (preRes2.get()!=null ^ postRes2.get()!=null){
            // check implementation is valid
            if (preRes2.get()!=null && postRes2.get()==null){
                String impl = all.get(preRes2.get()).getCompilationUnit().getInterfaceByName(preRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(preSet).get(0)).get(0).getBody().get().getStatement(0).toString().replaceAll(RETURN,BLANK);
                impl = impl.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).replaceAll(SEMI_COLON,BLANK).trim();
                if (!impl.equals(new ArrayList<>(postSet).get(0))){
                    LOG.error(cast.toString());
                    throw new IllegalStateException("incorrect concretion: implementation does not match to abstraction.");
                }
            } else if (postRes2.get()!=null && preRes2.get()==null){
                String impl = all.get(postRes2.get()).getCompilationUnit().getInterfaceByName(postRes2.get().getSimpleName()).get().getMethodsByName(new ArrayList<>(postSet).get(0)).get(0).getBody().get().getStatement(0).toString().replaceAll(RETURN,BLANK);
                impl = impl.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).replaceAll(SEMI_COLON,BLANK).trim();
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
        String pre = kase.asMethodCallExpr().getArgument(1).toString().split(ARROW)[1].trim();
        // just check this for pre-conditions, so that kases always complete the input instance wtr to a view. Post-conditions need to be flexible
        Set<String> preSet = new HashSet<>(Arrays.asList(pre.split(XOR)).stream().map(s->s.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).split(DOT_ESCAPED)[s.replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK).split(DOT_ESCAPED).length-1]).collect(Collectors.toList()));
        AtomicReference<Class> res = new AtomicReference();
        List<Method> methods = Arrays.asList(graphTerm.getClazz().getDeclaredMethods()).stream().filter(m->!m.getParameterTypes()[0].equals(Object.class) &&
                m.getName().equals(ACCEPT)).collect(Collectors.toList());
        checkForPreOrPostsAgainstViews(preSet,methods.get(0).getParameterTypes()[0],false,res);
        if (res.get()==null){
            throw new IllegalStateException("condition does not fall inside distinct view");
        }
    }

    static String resolveImplementation(String precondition, Class theViewClass) {
        String[] split = precondition.split(ARROW,2);
        String cond = split[split.length-1].replaceAll(SPACE,BLANK).replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK);
        String impl = BLANK;
        int o = 0;
        if (precondition.contains("forall")){
            String s = precondition.split(ARROW,2)[1].split(ARROW)[1];
            String newPre = s.replaceAll(SPACE,BLANK).substring(0,s.replaceAll(SPACE,BLANK).length()-1);
            return newPre;
        }
        if (precondition.contains("&")){
            impl = split[split.length-1].replaceAll(SPACE,BLANK).replaceAll(DOT_ESCAPED,OPEN_CLOSED_BRACKETS_ESCAPED+DOT_ESCAPED);
        } else {
            for (String disjunct : Arrays.asList(cond.split(XOR_ESCAPED))){
                if (o==0){
                    impl = "("+getImplementation(disjunct.split(DOT_ESCAPED)[1],all.get(theViewClass))+")";
                } else {
                    impl = impl+"^("+getImplementation(disjunct.split(DOT_ESCAPED)[1],all.get(theViewClass))+")";
                }
                o++;
            }
            impl = impl.replaceAll(SEMI_COLON,BLANK);
            if (impl.contains("forall")){
                impl = impl.split(ARROW)[1].substring(0, impl.split(ARROW)[1].length()-2).replaceAll(SPACE,BLANK);
            }
        }
        return impl;
    }

    static boolean rewriteKase(ProveKaseTask task) {
        Expression k = task.getKase();
        CompilationUnitWithData graph = task.getCompilationUnitWithData();
        int kaseNo = task.getCount();
        String P = null;
        String Q = null;
        SymbolicState viewTruth = null;
        Class theViewClass = null;
        try {
            String viewName = graph.getClazz().getSimpleName();
            theViewClass = getClassIfSimpleNameIsUniqueInPackage(viewName);
            viewTruth =  PetraProgram.getViewTruth(theViewClass);

            String pre = k.asMethodCallExpr().getArgument(0).toString();
            String post = k.asMethodCallExpr().getArgument(1).toString();
            P = resolve(pre,theViewClass);
            Q = resolve(post,theViewClass);

            // init symbolic state by filtering truth with pre-condition of kase
            graph.getSymbolicStates().put(kaseNo,
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
                if (graph.getClazz().isAnnotationPresent(Invariants.class)){
                    for (String inv : ((Invariants)graph.getClazz().getDeclaredAnnotation(Invariants.class)).value()) {
                        boolean allMatch = graph.getSymbolicStates().get(kaseNo).getSymbolicStates().stream().allMatch(l->l.contains(inv));
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
//                    Class impl = getImplementationOfView(theViewClass);
//                    if (!isViewOfImplementation(impl,theCastClass)){
//                        throw new IllegalStateException("the cast view must also be a view of the graph view's implementation.");
//                    }
                }

                String pre = si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0)
                        .asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0) // the kase
                        .asMethodCallExpr().getArgument(0).toString();
                String post = si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatement(0)
                        .asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0) // the kase
                        .asMethodCallExpr().getArgument(1).toString();
                String siP = resolve(pre,theViewClass);
                String siQ = resolve(post,theViewClass);

                Set<List<String>> preSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siP, theViewClass);
                if (si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatements().size()==1 &&
                        (!graph.getSymbolicStates().get(kaseNo).isForall() && preSet.containsAll(graph.getSymbolicStates().get(kaseNo).getSymbolicStates()))) {

                    LOG.debug("before SEQ_EXEC applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());

                    // update symbolic state
                    Set<List<String>> postSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siQ, theViewClass);
                    graph.getSymbolicStates().put(kaseNo, new SymbolicState(postSet, graph.getSymbolicStates().get(kaseNo).isForall()));

                    // remove sequential step
                    si.remove();
                    LOG.info("SEQ_EXEC applied "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug("after SEQ_EXEC applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());
                    LOG.debug(BLANK);
                } else if (si.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asLambdaExpr().getBody().asBlockStmt().getStatements().size()==1 &&
                        ((P.contains(".isEmpty()") || Q.contains(".isEmpty()") || P.contains(".forall(") || Q.contains(".forall(")) && graph.getSymbolicStates().get(kaseNo).isForall() && preSet.containsAll(graph.getSymbolicStates().get(kaseNo).getSymbolicStates()))){
                    LOG.debug("before SEQ_EXEC applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());

                    Set<List<String>> postSet =
                            filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), siQ, theViewClass);
                    graph.getSymbolicStates().put(kaseNo, new SymbolicState(postSet, graph.getSymbolicStates().get(kaseNo).isForall()));

                    // remove sequential step
                    si.remove();
                    LOG.info("SEQ_EXEC applied "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug("after SEQ_EXEC applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                    LOG.debug(k.toString());
                    LOG.debug(BLANK);
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
        // disabled this check which assumes kases within non edge steps to be proved if they have pre/post condition expression which are syntactically equal
        if (false && k.asMethodCallExpr().getArgument(0).equals(k.asMethodCallExpr().getArgument(1))){
            LOG.debug("before PROVE_KASE applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());

            k.asMethodCallExpr().getArgument(3).remove();
            k.asMethodCallExpr().addArgument("PROVED");
            graph.getStatus().put(k,true);
            LOG.info("PROVE_KASE applied "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
            LOG.debug("after PROVE_KASE applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());
        } else if (noOfStatements==0){
            String[] postSplit = k.asMethodCallExpr().getArgument(1).toString().split(ARROW,2);
            String post = postSplit[postSplit.length-1].replaceAll(SPACE,BLANK).replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED,BLANK);
            String postImpl = BLANK;
            int o = 0;
            for (String disjunct : Arrays.asList(post.split(XOR_ESCAPED))){
                if (o==0){
                    postImpl = "("+getImplementation(disjunct.split(DOT_ESCAPED)[1],all.get(theViewClass))+")";
                } else {
                    postImpl = postImpl+"^("+getImplementation(disjunct.split(DOT_ESCAPED)[1],all.get(theViewClass))+")";
                }
                o++;
            }
            Set<List<String>> postSet = filterStatesUsingBooleanPrecondition(viewTruth.getSymbolicStates(), viewTruth.isForall(), Q, theViewClass);
            // It only makes sense if the post-condition represents states which contain the states produced by the composition,
            // otherwise some states might not pass through
            if (postSet.containsAll(graph.getSymbolicStates().get(kaseNo).getSymbolicStates())) {

                LOG.debug("before PROVE_KASE applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                LOG.debug(k.toString());

                k.asMethodCallExpr().getArgument(3).remove();
                k.asMethodCallExpr().addArgument("PROVED");
                graph.getStatus().put(k,true);

                LOG.info("PROVE_KASE applied "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                LOG.debug("after PROVE_KASE applied to "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
                LOG.debug(k.toString());
            } else {
                Set<List<String>> a = new HashSet<>(postSet);
                Set<List<String>> b = new HashSet<>(graph.getSymbolicStates().get(kaseNo).getSymbolicStates());
                b.removeAll(a);
                graph.getStatus().put(k,false);
                fail("Postcondition does not cover: "+b.toString()
                        .replaceAll("\\[\\[","[\n [")
                        .replaceAll("\\],","],\n")
                        .replaceAll("\\]\\]","]\n]"));
            }
        }
        if (noOfStatements>0){
            LOG.debug("steps remaining: "+graph.getClazz().getSimpleName()+" kase:"+kaseNo);
            LOG.debug(k.toString());
            LOG.debug("symbolic states: "+graph.getSymbolicStates().get(kaseNo).getSymbolicStates().toString());
            LOG.debug("isForall: "+graph.getSymbolicStates().get(kaseNo).isForall());
        }
        graph.getKases().add(k);
        return graph.getStatus().getOrDefault(k,false);
    }

     static void convertToControlledEnglish(){
        StringBuilder sb = new StringBuilder();
        try {
            Class root = Class.forName(entryPointPackageName+"."+rootGraphName);
            startingLetter = 64;
            appendToStringBuilderAndGetNextClassToProcess(root,all.get(root).getPath(),rootGraphName,sb);
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("target/"+entryPointPackageName.replaceAll(DOT_ESCAPED,UNDERSCORE)+"_FLOWS.pdf"));
            document.open();
            Paragraph p = new Paragraph(sb.toString());
            document.add(p);
            document.close();

            startingLetter = 64;
            Document document2 = new Document();
            PdfWriter.getInstance(document2, new FileOutputStream("target/"+entryPointPackageName.replaceAll(DOT_ESCAPED,UNDERSCORE)+"_DATA.pdf"));
            document2.open();
            for (CompilationUnitWithData cu : all.values().stream().filter(c->
                    c.getClazz().isAnnotationPresent(View.class) &&
                    c.getClazz().isInterface() &&
                    !c.getClazz().getSimpleName().startsWith("P") &&
                    !Consumer.class.isAssignableFrom(c.getClazz()) &&
                    !c.getClazz().isAnnotationPresent(Primative.class)).collect(Collectors.toList())) {
                ClassOrInterfaceDeclaration c = cu.getCompilationUnit().getInterfaceByName(cu.getClazz().getSimpleName()).get();
                try {
                    String view = getControlledEnglishOfView(cu.getClazz(),c);
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
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, condition).replaceAll(UNDERSCORE,SPACE)
                .split(ARROW,2)[1]
                .replaceAll(AND,"and")
                .replaceAll(XOR_ESCAPED,"or \n")
                .replaceAll(COMMA,BLANK)
                .replaceAll(ARROW,", where each")
                .replaceAll(OPEN_BRACKET_ESCAPED,BLANK)
                .replaceAll(CLOSED_BRACKET_ESCAPED,BLANK)
                .replaceAll(DOT_ESCAPED,SPACE)
                .replaceAll("forall","each have a ");
    }
    private static Set<Class<?>> willRenderControlledEnglish = new HashSet<>();
    private static void appendToStringBuilderAndGetNextClassToProcess(Class clazz, Path path, String name, StringBuilder sb){
        if (willRenderControlledEnglish.contains(clazz)){
            return;
        } else {
            willRenderControlledEnglish.add(clazz);
        }
        ParseResult<CompilationUnit> pr = null;
        try {
            pr =  new JavaParser().parse(path);
            if (pr.isSuccessful()){
                CompilationUnit cu = pr.getResult().get();
                ClassOrInterfaceDeclaration graph = cu.getInterfaceByName(name).get();

                Collection<Class<?>> superinterfaces = new ArrayList<>();
                collectAllSuperInterfacesRecursively(clazz,superinterfaces);
                superinterfaces.add(clazz);

                for (Class<?> superInterface : superinterfaces){
                    for (MethodDeclaration action : graph.getMethodsByParameterTypes(superInterface)){

                        Statement kases = action.getBody().get().getStatement(0);
                        sb.append(NEW_LINE);
                        startingLetter++;
                        sb.append("Flow "+startingLetter+" - "+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,name).replaceAll(UNDERSCORE,SPACE)+" "+action.getName()+":\n\n");
                        int i = 0;
                        for (Expression a : kases.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                            if (i==0){

                            } else {
                                sb.append("Case "+i+".\nGiven"+formatCondition(a.asMethodCallExpr().getArguments().get(0).toString())+",\n");
                                if (!action.isAnnotationPresent(Edge.class)){
                                    for (Statement step : a.asMethodCallExpr().getArgument(2).asLambdaExpr().getBody().asBlockStmt().getStatements()){
                                        String stepName = null;
                                        if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("join")){
                                            String firstParOrParrStep = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asMethodCallExpr().getArgument(1).toString().split("::")[1];
                                            StringBuilder steps = new StringBuilder();
                                            steps.append(BLANK+firstParOrParrStep);
                                            for (int arg=2;arg<=step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asMethodCallExpr().getArguments().size();arg++){
                                                String parOrParrStep = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).asMethodCallExpr().getArgument(1).toString().split("::")[1];
                                                steps.append(" in parallel with "+parOrParrStep+",\n");
                                            }
                                            stepName = steps.toString();
                                        } else if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seq") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("par") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seqr") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("parr")){
                                            stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).toString().split("::")[1];
                                        }
                                        if (stepName!=null){
                                            stepName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, stepName);
                                            stepName = stepName.replaceAll(UNDERSCORE,SPACE);
                                            sb.append(BLANK+stepName+",\n");
                                        }
                                    }
                                    sb.append("Then"+formatCondition(a.asMethodCallExpr().getArguments().get(1).toString())+".\n\n");
                                } else {
                                    sb.append("Then"+formatCondition(a.asMethodCallExpr().getArguments().get(1).toString())+".\n\n");
                                }
                            }
                            i++;
                        }

                        i = 0;
                        for (Expression a : kases.asExpressionStmt().getExpression().asMethodCallExpr().getArguments()){
                            if (i==0){
                                // skip
                            } else {
                                if (!action.isAnnotationPresent(Edge.class)){
                                    for (Statement step : a.asMethodCallExpr().getArgument(2).asLambdaExpr().getBody().asBlockStmt().getStatements()){
                                        String stepName = null;
                                        if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("join")){
                                            //stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(0).asMethodCallExpr().getArgument(1).toString().replaceAll("::"," ");
                                            for (int arg=2;arg<=step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).asMethodCallExpr().getArguments().size();arg++){
                                                String parOrParrStep = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(arg).asMethodCallExpr().getArgument(1).toString().split("::")[0];
                                                Class clz = getClassIfSimpleNameIsUniqueInPackage(parOrParrStep);
                                                if (clz==null){
                                                    // skip
                                                } else {
                                                    CompilationUnitWithData x = all.get(clz);
                                                    appendToStringBuilderAndGetNextClassToProcess(clz,x.getPath(),parOrParrStep,sb);
                                                }
                                            }
                                        } else if (step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seq") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("par") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("seqr") ||
                                                step.asExpressionStmt().getExpression().asMethodCallExpr().getName().toString().equals("parr")){
                                            stepName = step.asExpressionStmt().getExpression().asMethodCallExpr().getArgument(1).toString().split("::")[0];
                                            Class clz = getClassIfSimpleNameIsUniqueInPackage(stepName);
                                            if (clz==null){
                                                // skip
                                            } else {
                                                CompilationUnitWithData x = all.get(clz);
                                                appendToStringBuilderAndGetNextClassToProcess(clz,x.getPath(),stepName,sb);
                                            }
                                        }
                                    }
                                }
                            }
                            i++;
                        }



                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int getMatchesCount(String someString, char toCount){
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
        for (Expression k : cu.getCompilationUnit()
                .getClassByName(cu.getClazz().getSimpleName()).get()
                .getMethodsByName(ACCEPT).get(0).getBody().get()
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
                pres.add(pre.split(ARROW)[1].trim());
                String z = pre.split(ARROW)[0].trim();
                if (pre.contains("forall")){
                    pre = pre.split(ARROW,2)[1].trim();
                } else {
                    pre = pre.split(ARROW)[1].trim();
                }
                String post = k.asMethodCallExpr().getArgument(1).toString();
                posts.add(post.split(ARROW)[1].trim());
                if (post.contains("forall")){
                    post = post.split(ARROW,2)[1].trim();
                } else {
                    post = post.split(ARROW)[1].trim();
                }
                kasesPreconditionDisjunction.append(z+" -> "+pre);
                kasesPostconditionDisjunction.append(z+" -> "+post);
            } else {
                String pre = k.asMethodCallExpr().getArgument(0).toString();
                pres.add(pre.split(ARROW)[1].trim());
                if (pre.contains("forall")){
                    pre = pre.split(ARROW,2)[1].trim();
                } else {
                    pre = pre.split(ARROW)[1].trim();
                }
                String post = k.asMethodCallExpr().getArgument(1).toString();
                posts.add(post.split(ARROW)[1].trim());
                if (post.contains("forall")){
                    post = post.split(ARROW,2)[1].trim();
                } else {
                    post = post.split(ARROW)[1].trim();
                }
                kasesPreconditionDisjunction.append(" ^ "+pre);
                kasesPostconditionDisjunction.append(" ^ "+post);
            }
            count2++;
        }

        if (cu.getClazz().isAnnotationPresent(Root.class)){
            if (!cu.getClazz().isAnnotationPresent(Infinite.class)){
                List<Method> methods = Arrays.asList(cu.getClazz().getDeclaredMethods()).stream().filter(i->!i.getParameterTypes()[0].equals(Object.class) && i.getName().equals(ACCEPT)).collect(Collectors.toList());
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
        if (cu.getCompilationUnit()
                .getInterfaceByName(cu.getClazz().getSimpleName()).isPresent()){
            if (cu.getCompilationUnit().getInterfaceByName(cu.getClazz().getSimpleName()).get()
                    .getMethodsByName(methodName).size()==1){
                // skip
            } else {
                return null;
            }
        }
        return cu.getCompilationUnit()
                .getInterfaceByName(cu.getClazz().getSimpleName()).get()
                .getMethodsByName(methodName).get(0).getBody().get().getStatement(0).toString()
                .replaceAll(RETURN,BLANK).replaceAll(SEMI_COLON,BLANK).trim();
    }

    static String getDefaultBooleanMethodImplementation(String methodName){
        for (CompilationUnitWithData cu : all.values().stream().filter(c->c.getClazz().isInterface()).collect(Collectors.toList())){
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

//        List<MethodDeclaration> fieldMethodDeclarations = c.getMethods().stream().filter(m -> !m.isDefault() && !m.getType().asString().equals(BOOLEAN_PRIMITIVE_TYPE)).collect(Collectors.toList());
//        if (!fields.stream().map(f -> f.getName()).collect(Collectors.toList()).equals(
//                fieldMethodDeclarations.stream().map(f -> f.getName().asString()).collect(Collectors.toList()))
//        ) {
//            throw new IllegalStateException("fields of view must be declared in alphabetical order.");
//        }

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
                String impl = getImplementation(m, all.get(clazz)).replaceAll(SPACE, BLANK);
                String[] split = impl.split(ARROW);
                String collectionVar = split[0].split(DOT_ESCAPED)[0].replaceAll(OPEN_CLOSED_BRACKETS_ESCAPED, BLANK);
                String var = split[1].split(DOT_ESCAPED)[0];
                impl = split[1].replaceAll(OPEN_BRACKET_ESCAPED, BLANK).replaceAll(CLOSED_BRACKET_ESCAPED, BLANK);
                impl = impl.replaceAll(var+DOT_ESCAPED,BLANK);

                String methodInEnglish = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,m).replaceAll(UNDERSCORE,SPACE);
                sb.append(""+no+". "+methodInEnglish+BLANK + ", means, for every " + var + " in " + collectionVar + ", " + lowerCamelToEnglishForEachInSplit(impl)
                        .replaceAll("\\!", "not ")
                        .replaceAll(XOR_ESCAPED, " xor ")
                        .replaceAll("\\&\\&", " and ")
                        .replaceAll("\\&", " and ")
                        .replaceAll("\\|\\|", " or ")
                        .replaceAll("\\|", " or ")+".\n\n");
                no++;
            }
        } else {
            int no = 1;
            for (String m : viewPropsNamesSet) {
                String impl = getImplementation(m, all.get(clazz)).replaceAll(SPACE,BLANK);
                impl = impl.replaceAll(OPEN_BRACKET_ESCAPED, BLANK).replaceAll(CLOSED_BRACKET_ESCAPED, BLANK);
                impl = impl.replaceAll(DOT_ESCAPED,SPACE);
                String methodInEnglish = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,m).replaceAll(UNDERSCORE,SPACE);
                sb.append(""+no+". "+methodInEnglish + ", means, "+lowerCamelToEnglishForEachInSplit(impl)
                        .replaceAll(NOT_ESCAPED, "not ")
                        .replaceAll(XOR_ESCAPED, " or ")
                        .replaceAll(AND_ESCAPED+AND_ESCAPED, " and ")
                        .replaceAll(AND_ESCAPED, " and ")
                        .replaceAll(OR_ESCAPED+OR_ESCAPED, " or ")
                        .replaceAll(OR_ESCAPED, " or ")+".\n\n");
                no++;
            }
        }
        return sb.toString();
    }

    private static String lowerCamelToEnglishForEachInSplit(String toSplit){
        String[] split = toSplit.split(SPACE);
        StringBuilder sb = new StringBuilder();
        sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,split[0]).replaceAll(UNDERSCORE,SPACE));
        for (int i=1;i<split.length; i++){
            sb.append(SPACE+CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,split[i]).replaceAll(UNDERSCORE,SPACE));
        }
        return sb.toString();
    }

    public static void collectAllSuperInterfacesRecursively(Class<?> start, Collection<Class<?>> superinterfaces){
        for (Class<?> c : start.getInterfaces()){
            superinterfaces.add(c);
            collectAllSuperInterfacesRecursively(c, superinterfaces);
        }
    }

//    static String resolveImplementation(String precondition, Class theViewClass) {
//
////        String pre = precondition.split(ARROW)[1].trim().split(DOT_ESCAPED,2)[1];
////        int maxDotCount = 0;
////        for (String c : new String[]{"\\&\\&","\\|\\|","\\^","\\&","\\|"}){
////            for (String split : pre.split(c)){
////                int count = split.split("\\.").length;
////                if (count>maxDotCount){
////                    maxDotCount = count;
////                }
////            }
////        }
////        if (maxDotCount==1 || precondition.contains("forall")){// ||  preconditionDotCount==preconditionXorCount+1){
////            return resolveImplementation(precondition, theViewClass);
////        } else if (maxDotCount<=3){
//            if (true){
//                //return precondition.split(ARROW)[1].trim().replaceAll(theViewClass.getSimpleName().toLowerCase().substring(0,1)+"\\.","");
//                return precondition.split(ARROW)[1].trim();
//            }
////        } else if (maxDotCount>3){
////            throw new IllegalArgumentException("precondition cannot go this deep!");
////        }
//
    public static String resolve(String condition, Class theViewClass){
        int dotCount = getMatchesCount(condition,'.');
        int xorCount = getMatchesCount(condition,'^');
        if (dotCount==1 || condition.contains("forall") ||  dotCount==xorCount+1){
            return resolveImplementation(condition, theViewClass);
        } else { // covers the case where predicate name at local level is used or if true is used
            String var = condition.split(ARROW)[0].trim();
            return condition.split(ARROW)[1].trim().replaceAll(var+"\\.","");//.split(DOT_ESCAPED,2)[1];
        }
    }
}
