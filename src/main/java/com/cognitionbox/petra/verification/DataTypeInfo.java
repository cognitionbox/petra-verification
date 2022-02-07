package com.cognitionbox.petra.verification;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class DataTypeInfo implements Serializable {
    final Set<List<String>> truth;
    final Set<MethodDeclaration> methodDeclarations;
    public DataTypeInfo(Set<List<String>> truth, Set<MethodDeclaration> methodDeclarations) {
        this.truth = truth;
        this.methodDeclarations = methodDeclarations;
    }

    @Override
    public String toString() {
        return "DataTypeInfo{" +
                "truth=" + truth +
                ", methodDeclarations=" + methodDeclarations +
                '}';
    }
}
