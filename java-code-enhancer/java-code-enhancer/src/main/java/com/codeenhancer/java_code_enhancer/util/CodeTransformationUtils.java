package com.codeenhancer.java_code_enhancer.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeTransformationUtils {


    /**
     * Main method to transform Java code to latest version
     *
     * @param cu CompilationUnit to transform
     * @return Transformed code as String
     */
    public static String transformToLatestVersion(CompilationUnit cu) {
        try {
            // Apply multiple transformation strategies
            applyTransformations(cu);
            return cu.toString();
        } catch (Exception e) {
            // Fallback to original code if transformation fails
            return cu.toString();
        }
    }

    /**
     * Apply multiple code transformation strategies
     *
     * @param cu CompilationUnit to transform
     */
    private static void applyTransformations(CompilationUnit cu) {
        // Java 21 Transformation Strategies
        modernizeLoops(cu);
        enhanceSwitchExpressions(cu);
        updateDeprecatedAPIs(cu);
    }

    /**
     * Modernize loop constructs
     *
     * @param cu CompilationUnit to transform
     */

    private static void modernizeLoops(CompilationUnit cu) {
        // Convert for loops to forEach
        cu.findAll(ForStmt.class).forEach(forStmt -> {
            Optional<Expression> init = forStmt.getInitialization().stream().findFirst();
            Optional<Expression> compare = forStmt.getCompare();
            Optional<Expression> update = forStmt.getUpdate().stream().findFirst();

            if (isArrayOrCollectionLoop(init, compare, update)) {
                VariableDeclarationExpr varDecl = (VariableDeclarationExpr) init.get();

                // Create enhanced for loop
                Statement body = forStmt.getBody();
                Expression collection = extractCollection(compare.get());

                String enhancedFor = String.format("for (%s : %s) %s",
                        varDecl.toString(),
                        collection.toString(),
                        body.toString());

                forStmt.setBody(new BlockStmt().addStatement(enhancedFor));
            }
        });

        // Convert while loops to modern patterns where applicable
        cu.findAll(WhileStmt.class).forEach(whileStmt -> {
            if (isInfiniteLoop(whileStmt)) {
                // Convert to modern infinite loop pattern
                WhileStmt modernWhile = new WhileStmt();
                modernWhile.setCondition(new BooleanLiteralExpr(true));
                modernWhile.setBody(whileStmt.getBody());

                whileStmt.replace(modernWhile);
            }
        });

        // Convert indexed for loops to streams where applicable
        cu.findAll(ForStmt.class).forEach(forStmt -> {
            if (isIndexBasedLoop(forStmt)) {
                // Convert to IntStream
                MethodCallExpr streamExpr = new MethodCallExpr(
                        new NameExpr("IntStream"),
                        "range"
                );

                // Add stream operations
                LambdaExpr lambda = new LambdaExpr();
                lambda.setBody(forStmt.getBody());

                MethodCallExpr forEachExpr = new MethodCallExpr(
                        streamExpr,
                        "forEach",
                        NodeList.nodeList(lambda)
                );

                forStmt.replace(new ExpressionStmt(forEachExpr));
            }
        });
    }

    private static boolean isArrayOrCollectionLoop(Optional<Expression> init,
                                                   Optional<Expression> compare,
                                                   Optional<Expression> update) {
        if (init.isEmpty() || compare.isEmpty() || update.isEmpty()) return false;

        Expression initExpr = init.get();
        if (!(initExpr instanceof VariableDeclarationExpr)) return false;

        Expression compareExpr = compare.get();
        return compareExpr instanceof BinaryExpr &&
                ((BinaryExpr) compareExpr).getOperator() == BinaryExpr.Operator.LESS;
    }

    private static Expression extractCollection(Expression compareExpr) {
        BinaryExpr binary = (BinaryExpr) compareExpr;
        return binary.getRight();
    }

    private static boolean isInfiniteLoop(WhileStmt whileStmt) {
        Expression condition = whileStmt.getCondition();
        return condition instanceof BooleanLiteralExpr &&
                ((BooleanLiteralExpr) condition).getValue();
    }

    private static boolean isIndexBasedLoop(ForStmt forStmt) {
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

    /**
     * Enhance Switch Expressions (Java 14+)
     *
     * @param cu CompilationUnit to transform
     */


    private static void enhanceSwitchExpressions(CompilationUnit cu) {
        cu.findAll(SwitchStmt.class).forEach(switchStmt -> {
            Expression selector = switchStmt.getSelector();

            // Process each case
            for (SwitchEntry entry : switchStmt.getEntries()) {
                NodeList<Statement> statements = entry.getStatements();
                if (statements.isEmpty()) continue;

                // Convert case labels
                StringBuilder arrowCase = new StringBuilder();
                if (entry.getLabels().isEmpty()) {
                    arrowCase.append("default");
                } else {
                    arrowCase.append("case ").append(entry.getLabels().get(0));
                }

                // Convert to yield statements for returns
                if (statements.size() == 1) {
                    Statement stmt = statements.get(0);
                    if (stmt.isReturnStmt()) {
                        ReturnStmt returnStmt = (ReturnStmt) stmt;
                        YieldStmt yieldStmt = new YieldStmt();
                        returnStmt.getExpression().ifPresent(yieldStmt::setExpression);
                        statements.set(0, yieldStmt);
                    }
                }

                // Build the arrow syntax
                arrowCase.append(" -> ");
                if (statements.size() == 1) {
                    arrowCase.append(statements.get(0).toString().trim());
                } else {
                    arrowCase.append("{ ")
                            .append(String.join("; ", statements.stream()
                                    .map(Statement::toString)
                                    .toArray(String[]::new)))
                            .append(" }");
                }

                // Update the entry
                entry.setStatements(new NodeList<>(new ExpressionStmt(
                        new StringLiteralExpr(arrowCase.toString())
                )));
            }
        });
    }


    private static void updateDeprecatedAPIs(CompilationUnit cu) {
        // Update File API calls
        cu.findAll(MethodCallExpr.class).forEach(method -> {
            var methodName = method.getNameAsString();
            switch (methodName) {
                case "list":
                    if (isFileType(method)) {
                        method.setName("listFiles");
                    }
                    break;

                case "toURL":
                    if (isFileType(method)) {
                        method.setName("toURI().toURL()");
                    }
                    break;

                case "getBytes":
                    if (!method.getArguments().isEmpty()) {
                        method.setArguments(NodeList.nodeList(
                                new NameExpr("StandardCharsets.UTF_8")
                        ));
                    }
                    break;

                case "newInstance":
                    if (isClassType(method)) {
                        method.setName("getDeclaredConstructor().newInstance");
                    }
                    break;
            }
        });

        // Update legacy type instantiations
        cu.findAll(ObjectCreationExpr.class).forEach(expr -> {
            var typeStr = expr.getTypeAsString();
            switch (typeStr) {
                case "Date":
                    expr.setType("Instant");
                    expr.setArguments(NodeList.nodeList(
                            new MethodCallExpr("now")
                    ));
                    break;

                case "SecureRandom":
                    expr.replace(new MethodCallExpr(
                            new NameExpr("SecureRandom"),
                            "getInstanceStrong"
                    ));
                    break;
            }
        });
    }

    private static boolean isFileType(MethodCallExpr method) {
        return method.getScope().isPresent() &&
                method.getScope().get().calculateResolvedType().describe().equals("java.io.File");
    }

    private static boolean isClassType(MethodCallExpr method) {
        return method.getScope().isPresent() &&
                method.getScope().get().calculateResolvedType().describe().contains("Class");
    }


    /**
     * Detect potential warnings and issues in the code
     *
     * @param cu CompilationUnit to analyze
     * @return Array of warning messages
     */
    public static String[] detectWarnings(CompilationUnit cu) {
        List<String> warnings = new ArrayList<>();

        // Deprecated method detection
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getAnnotationByName("Deprecated").isPresent()) {
                warnings.add("Deprecated method found: " + method.getName());
            }
        });

        // Potential performance issues
        cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
            String methodName = methodCall.getName().asString();

            // Example warning conditions
            if (methodName.equals("equals") || methodName.equals("hashCode")) {
                warnings.add("Potential performance-sensitive method: " + methodName);
            }
        });

        return warnings.toArray(new String[0]);
    }


}