package org.agh.cppinterpreter;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ValidatorVisitor<T> extends gBaseVisitor<T>{

    List<HashMap<String,Integer>> dfsOrderOfScopes = new ArrayList<>();
    Stack<HashMap<String,Integer>> notFinishedOuterScopes = new Stack<HashMap<String, Integer>>();
    Boolean isError = false;

    @Override public T visitBlockItemList(gParser.BlockItemListContext ctx) {
        System.out.println("OPENING NEW SCOPE");
        notFinishedOuterScopes.add(new HashMap<>());
        T result = visitChildren(ctx);
        System.out.println(notFinishedOuterScopes.pop().keySet().stream().toList());
        return result;
    }
    @Override public T visitCompilationUnit(gParser.CompilationUnitContext ctx) {
        System.out.println("VALIDATION START");
        return visitChildren(ctx);
    }

    @Override
    public T visitDeclarator(gParser.DeclaratorContext ctx) {
        if(ctx.directDeclarator().identifierList() != null && ctx.directDeclarator().identifierList().Identifier() != null)
        for (TerminalNode identif:ctx.directDeclarator().identifierList().Identifier()
             ) {
            String varname =identif.getSymbol().getText();
            System.out.println("VALIDATING DECLARATION OF:"+varname);
            if(findDeclaration(varname))
            {
                System.out.println(varname+ " is declared multiple times");
                isError=true;
            }else
                notFinishedOuterScopes.peek().put(varname,0);
        }
        return super.visitDeclarator(ctx);
    }

    public boolean findDeclaration(String name)
    {
        for (HashMap map: notFinishedOuterScopes.stream().toList()
             ) {
            if(map.containsKey(name))
                return true;
        }
        return false;
    }

    @Override public T visitPrimaryExpression(gParser.PrimaryExpressionContext ctx)
    {
        if(ctx.Identifier() != null) {
            String varname = ctx.Identifier().getText();
            System.out.println("VALIDATING USE OF:" + varname);

            if (!findDeclaration(varname)) {
                System.out.println(varname + " is not declared in this scope");
                isError = true;
            }
        }
        return visitChildren(ctx);
    }

    //blockitemList visit -> new scope
}