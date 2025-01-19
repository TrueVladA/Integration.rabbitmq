package ru.bpmcons.sbi_elma.infra.method;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

@Component
public class MethodBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {
    private BeanFactory beanFactory;
    private StringValueResolver valueResolver;

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.valueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        for (Method method : ReflectionUtils.getUniqueDeclaredMethods(targetClass)) {
            ru.bpmcons.sbi_elma.infra.method.Method annotation = AnnotationUtils.findAnnotation(method, ru.bpmcons.sbi_elma.infra.method.Method.class);
            if (annotation != null) {
                String name = valueResolver.resolveStringValue(annotation.value());
                if (name == null) {
                    throw new IllegalStateException("@Method value on " + targetClass.getSimpleName() + "#" + method.getName() + " should not be null");
                }
                beanFactory.getBean(MethodRegistry.class).registerMethod(name, new MethodContainer(method, bean));
            }
        }
        return bean;
    }
}
