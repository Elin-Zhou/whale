package com.xxelin.whale.utils;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: SpelUtils.java , v 0.1 2019-08-07 15:39 ElinZhou Exp $
 */
public class SpelUtils {

    private SpelUtils() {
        throw new IllegalStateException("cant instance");
    }

    public static <T> T parse(String spel, Class<T> returnType, Object object, Method method, Object[] args) {
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(spel);
        LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer =
                new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(object,
                method, args, parameterNameDiscoverer);
        for (int i = 0; i < args.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return expression.getValue(context, returnType);
    }

}
