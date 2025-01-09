package com.codeenhancer.java_code_enhancer.service.impl;

import com.codeenhancer.java_code_enhancer.util.CodeMetrics;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static com.codeenhancer.java_code_enhancer.util.CodeTransformationUtils.*;

@Service
public class DashboardServiceImpl {
    public CodeMetrics analyzeCode(String code) {
        CompilationUnit cu = StaticJavaParser.parse(code);

        CodeMetrics metrics = new CodeMetrics();

        // Total Lines of Code (LOC)
        metrics.setTotalLinesOfCode(code.split("\n").length);

        // Cyclomatic Complexity
        metrics.setCyclomaticComplexity(calculateCyclomaticComplexity(cu));

        // Deprecated APIs Count
        metrics.setDeprecatedApiCount(countDeprecatedAPIs(cu));

        // Modernization Opportunities
        metrics.setModernizationOpportunities(countModernizationOpportunities(cu));

        return metrics;
    }

    private int calculateCyclomaticComplexity(CompilationUnit cu) {
        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
        visitor.visit(cu, null);
        return visitor.getComplexity();
    }


    private static int countDeprecatedAPIs(CompilationUnit cu) {
        Set<String> deprecatedMethods = Set.of(
                "list",
                "toURL",
                "getBytes",
                "newInstance"
        );

        Set<String> deprecatedTypes = Set.of(
                "Date",
                "SecureRandom"
        );

        int methodCount = (int) cu.findAll(MethodCallExpr.class)
                .stream()
                .filter(method -> deprecatedMethods.contains(method.getNameAsString()))
                .count();

        int typeCount = (int) cu.findAll(ObjectCreationExpr.class)
                .stream()
                .filter(expr -> deprecatedTypes.contains(expr.getTypeAsString()))
                .count();

        return methodCount + typeCount;
    }

    private int countModernizationOpportunities(CompilationUnit cu) {
        int opportunities = 0;

        // Count traditional for-loops that can be modernized
        opportunities += cu.findAll(ForStmt.class).stream()
                .filter(forStmt -> isModernizableForLoop(forStmt))
                .count();

        // Count while-loops that can be modernized
        opportunities += cu.findAll(WhileStmt.class).stream()
                .filter(whileStmt -> isModernizableWhileLoop(whileStmt))
                .count();

        // Count switch statements that can be transformed into switch expressions
        opportunities += cu.findAll(SwitchStmt.class).stream()
                .filter(switchStmt -> isModernizableSwitch(switchStmt))
                .count();

        return opportunities;
    }

    private boolean isModernizableForLoop(ForStmt forStmt) {
        Optional<Expression> init = forStmt.getInitialization().stream().findFirst();
        Optional<Expression> compare = forStmt.getCompare();
        Optional<Expression> update = forStmt.getUpdate().stream().findFirst();

        // Check if it's a traditional indexed loop or an array/collection loop
        return init.isPresent() && compare.isPresent() && update.isPresent()
                && (isIndexBasedLoop(forStmt) || isArrayOrCollectionLoop(init, compare, update));
    }

    private boolean isIndexBasedLoop(ForStmt forStmt) {
        Optional<Expression> init = forStmt.getInitialization().stream().findFirst();
        Optional<Expression> compare = forStmt.getCompare();
        Optional<Expression> update = forStmt.getUpdate().stream().findFirst();

        return init.isPresent() &&
                compare.isPresent() &&
                update.isPresent() &&
                init.get() instanceof VariableDeclarationExpr &&
                compare.get() instanceof BinaryExpr &&
                update.get() instanceof UnaryExpr;
    }

    private boolean isArrayOrCollectionLoop(Optional<Expression> init,
                                            Optional<Expression> compare,
                                            Optional<Expression> update) {
        if (init.isEmpty() || compare.isEmpty() || update.isEmpty()) return false;

        Expression initExpr = init.get();
        if (!(initExpr instanceof VariableDeclarationExpr)) return false;

        Expression compareExpr = compare.get();
        return compareExpr instanceof BinaryExpr &&
                ((BinaryExpr) compareExpr).getOperator() == BinaryExpr.Operator.LESS;
    }


    private boolean isModernizableWhileLoop(WhileStmt whileStmt) {
        // Check for infinite while-loops or simple conditions
        return isInfiniteLoop(whileStmt);
    }

    private boolean isModernizableSwitch(SwitchStmt switchStmt) {
        // A switch statement is modernizable if it uses traditional labels or lacks arrow cases
        return switchStmt.getEntries().stream().anyMatch(entry -> !entry.getStatements().isEmpty());
    }

    private boolean isInfiniteLoop(WhileStmt whileStmt) {
        Expression condition = whileStmt.getCondition();
        return condition instanceof BooleanLiteralExpr &&
                ((BooleanLiteralExpr) condition).getValue();
    }


    // Inner class for calculating cyclomatic complexity
    private static class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {
        private int complexity = 1; // Start with 1 for the method itself

        @Override
        public void visit(SwitchStmt n, Void arg) {
            super.visit(n, arg);
            complexity += n.getEntries().size(); // Each switch case adds to complexity
        }

        public int getComplexity() {
            return complexity;
        }
    }
}
