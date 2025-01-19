package ru.bpmcons.sbi_elma.validation;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.validation.MessageInterpolatorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

@Configuration
public class ValidationConfiguration {

    @Bean
    @Role(2)
    public static LocalValidatorFactoryBean defaultValidator(ApplicationContext applicationContext) {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.setValidationPropertyMap(Map.of(
                HibernateValidatorConfiguration.PROPERTY_NODE_NAME_PROVIDER_CLASSNAME,
                JsonAwarePropertyNodeNameProvider.class.getCanonicalName()
        ));
        factoryBean.setConfigurationInitializer(configuration -> {
            ((ConfigurationImpl) configuration).propertyNodeNameProvider(new JsonAwarePropertyNodeNameProvider());
        });
        MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory(applicationContext);
        factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
        return factoryBean;
    }
}
