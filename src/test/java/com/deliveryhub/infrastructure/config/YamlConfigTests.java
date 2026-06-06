package com.deliveryhub.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class YamlConfigTests {

    @Value("${security.jwt.issuer:not-found}")
    private String jwtIssuer;

    @Value("${spring.datasource.driver-class-name:not-found}")
    private String dbDriver;

    @Test
    void shouldLoadYamlProperties() {
        assertThat(jwtIssuer).isEqualTo("deliveryhub-api");
        assertThat(dbDriver).isEqualTo("org.h2.Driver");
    }
}
