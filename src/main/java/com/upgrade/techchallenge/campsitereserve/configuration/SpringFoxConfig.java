package com.upgrade.techchallenge.campsitereserve.configuration;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.LocalDate;
import java.util.List;

/**
 * Configuration for swagger document
 */
@Configuration
@EnableWebMvc
public class SpringFoxConfig implements WebMvcConfigurer {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${application.description}")
    private String applicationDescription;

    private TypeResolver typeResolver = new TypeResolver();

    @Bean
    public Docket productApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);

        return docket.select().
                apis(RequestHandlerSelectors.withClassAnnotation(Api.class)).
                paths(PathSelectors.any()).
                build().
                apiInfo(this.apiInfo()).
                directModelSubstitute(LocalDate.class, String.class).
                genericModelSubstitutes(ResponseEntity.class).
                alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(List.class, LocalDate.class),
                                typeResolver.resolve(List.class, String.class))).
                useDefaultResponseMessages(false);

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().
                title(applicationName).
                description(applicationDescription).
                version("1.0").
                build();
    }
}
