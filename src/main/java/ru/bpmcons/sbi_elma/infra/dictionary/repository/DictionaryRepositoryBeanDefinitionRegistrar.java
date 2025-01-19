package ru.bpmcons.sbi_elma.infra.dictionary.repository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import ru.bpmcons.sbi_elma.elma.ElmaClient;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.impl.DictionaryRepositoryInner;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.impl.DictionaryRepositoryProxy;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata.DictionaryRepositoryMetadataParser;
import ru.bpmcons.sbi_elma.infra.dictionary.repository.metadata.RepositoryClassMetadata;
import ru.bpmcons.sbi_elma.properties.EcmProperties;

import java.lang.reflect.Proxy;

@RequiredArgsConstructor
public class DictionaryRepositoryBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    private final Environment environment;
    private final BeanFactory beanFactory;
    private final ClassLoader classLoader;

    @SneakyThrows
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        DictionaryRepositoryMetadataParser metadataParser = new DictionaryRepositoryMetadataParser(beanFactory);

        String pack = ClassUtils.getPackageName(importingClassMetadata.getClassName());
        ClassPathScanningCandidateComponentProvider scanner = new CandidateComponentProvider(environment);
        for (BeanDefinition component : scanner.findCandidateComponents(pack)) {
            Class<?> cls = classLoader.loadClass(component.getBeanClassName());

            GenericBeanDefinition inner = new GenericBeanDefinition();
            inner.setBeanClass(DictionaryRepositoryInner.class);
            inner.setInstanceSupplier(() -> {
                RepositoryClassMetadata meta = metadataParser.parseMetadata(cls);
                return new DictionaryRepositoryInner(
                        meta,
                        beanFactory.getBean(ElmaClient.class),
                        beanFactory.getBean(EcmProperties.class)
                );
            });
            String innerName = cls.getSimpleName() + "dictionaryRepositoryInner";
            registry.registerBeanDefinition(innerName, inner);

            GenericBeanDefinition repo = new GenericBeanDefinition();
            repo.setBeanClass(cls);
            repo.setInstanceSupplier(() ->
                    Proxy.newProxyInstance(
                            classLoader,
                            new Class[]{cls},
                            new DictionaryRepositoryProxy(beanFactory.getBean(innerName, DictionaryRepositoryInner.class))
                    )
            );
            registry.registerBeanDefinition(cls.getSimpleName() + "dictionaryRepository", repo);
        }
    }

    private static final class CandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {
        public CandidateComponentProvider(Environment environment) {
            super(false, environment);
            addIncludeFilter(new AssignableTypeFilter(DictionaryRepository.class));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            AnnotationMetadata metadata = beanDefinition.getMetadata();
            return metadata.isIndependent() && metadata.isInterface();
        }
    }
}
