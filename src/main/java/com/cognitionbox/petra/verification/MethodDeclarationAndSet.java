package com.cognitionbox.petra.verification;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;
import java.util.Set;

public class MethodDeclarationAndSet {
    final MethodDeclaration methodDeclaration;
    final Set<List<String>> set;
    public MethodDeclarationAndSet(MethodDeclaration methodDeclaration, Set<List<String>> set) {
        this.methodDeclaration = methodDeclaration;
        this.set = set;
    }
}
