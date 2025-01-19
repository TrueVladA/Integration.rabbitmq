package ru.bpmcons.sbi_elma.infra.dictionary.repository.impl;

import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class DictionaryRepositoryProxy implements InvocationHandler {
    private final DictionaryRepositoryInner inner;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            return MethodHandles.lookup()
                    .findSpecial(
                            method.getDeclaringClass(),
                            method.getName(),
                            MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                            method.getDeclaringClass()
                    )
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        } else {
            return inner.find(method, args);
        }
    }
}
