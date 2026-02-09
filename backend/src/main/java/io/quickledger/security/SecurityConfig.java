package io.quickledger.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.CsrfDsl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Matchers are relative to context-path (/api), so we remove /api prefix
                .requestMatchers("/stripe/**").permitAll()
                .requestMatchers("/users/auth0-webhook-create").permitAll()
                .requestMatchers("/plans").permitAll()
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, BasicAuthenticationFilter.class)
                // This will add the jwtRequestFilter before the BasicAuthenticationFilter,
                // which is a filter that's always in the filter chain. This ensures that the
                // jwtRequestFilter is always applied, regardless of whether form login is
                // enabled or not.
                // before it was UsernamePasswordAuthenticationFilter
                .csrf(AbstractHttpConfigurer::disable)
                .cors(org.springframework.security.config.Customizer.withDefaults()); // Enable CORS
        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        // Allow localhost for dev and mobile app
        configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:5173", "https://localhost",
                "http://10.0.2.2", "http://localhost"));
        configuration.addAllowedMethod("*"); // Allow all methods
        configuration.addAllowedHeader("*"); // Allow all headers
        configuration.setAllowCredentials(true); // Needed for Auth headers usually

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}