package io.quickledger.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//INFO: This is used to inject the currently authenticated user into the method that it annotates.
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "user")
public @interface UserIdAuth {
}
