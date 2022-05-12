package it.iet.config.security;

import it.iet.infrastructure.mongo.entity.user.Authority;
import it.iet.infrastructure.mongo.entity.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        return new JwtUser(user.getEmail(), user.getPassword(), mapToGrantedAuthorities(user.getAuthorities()), !user.isBlocked());
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(Set<Authority> authorities) {
        return authorities.stream().map(authority -> new SimpleGrantedAuthority(authority.name())).collect(Collectors.toList());
    }
}
