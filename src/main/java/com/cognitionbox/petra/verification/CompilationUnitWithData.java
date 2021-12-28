package com.cognitionbox.petra.verification;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CompilationUnitWithData implements Serializable {
    public Path path;
    List<Expression> kases = new ArrayList<>();
    public Map<Expression,Boolean> status = new HashMap<>();
    public Map<Integer, SymbolicState> symbolicStates = new HashMap<>();
    public CompilationUnit compilationUnit;
    public final Class clazz;
    boolean isEdge;
    boolean isProved;
    public CompilationUnitWithData(Path path, Class clazz, CompilationUnit compilationUnit) {
        this.path = path;
        this.clazz = clazz;
        this.compilationUnit = compilationUnit;
    }
}
