package io.quickledger.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class JwtRequestFilterMock {

    @Bean
    public JwtRequestFilter jwtRequestFilter(UserDetailsService userDetailsService) {
        return mock(JwtRequestFilter.class);
    }
}
