package com.example.sigelic.views;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.vaadin.flow.spring.security.AuthenticationContext;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorityChecker {

    private final AuthenticationContext authContext;

    public boolean has(String authority) {
        return authContext.getAuthenticatedUser(UserDetails.class)
                .map(user -> user.getAuthorities().stream()
                        .anyMatch(granted -> granted.getAuthority().equals(authority)))
                .orElse(false);
    }
}
