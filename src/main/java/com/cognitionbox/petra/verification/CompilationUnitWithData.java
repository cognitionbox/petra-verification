package com.cognitionbox.petra.verification;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CompilationUnitWithData implements Serializable {
    private final Path path;
    private final List<Expression> kases = new ArrayList<>();
    private final Map<Expression,Boolean> status = new HashMap<>();
    private final Map<Integer, SymbolicState> symbolicStates = new HashMap<>();
    private CompilationUnit compilationUnit;
    private final Class clazz;
    private boolean isEdge;
    private boolean isProved;

    public Path getPath() {
        return path;
    }

    public List<Expression> getKases() {
        return kases;
    }

    public Map<Expression, Boolean> getStatus() {
        return status;
    }

    public Map<Integer, SymbolicState> getSymbolicStates() {
        return symbolicStates;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(CompilationUnit cu) {
        this.compilationUnit = cu;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean isEdge() {
        return isEdge;
    }

    public boolean isProved() {
        return isProved;
    }

    public void setEdge(boolean edge) {
        isEdge = edge;
    }

    public void setProved(boolean proved) {
        isProved = proved;
    }

    public CompilationUnitWithData(Path path, Class clazz, CompilationUnit compilationUnit) {
        this.path = path;
        this.clazz = clazz;
        this.compilationUnit = compilationUnit;
    }
}
