package com.cognitionbox.petra.verification;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;
import java.util.function.Predicate;

public class MethodDeclarationAndPredicate {
    final MethodDeclaration methodDeclaration;
    final Predicate<List<String>> predicate;
    public MethodDeclarationAndPredicate(MethodDeclaration methodDeclaration, Predicate<List<String>> predicate) {
        this.methodDeclaration = methodDeclaration;
        this.predicate = predicate;
    }
}
