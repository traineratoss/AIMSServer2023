package com.atoss.idea.management.system.repository.dto;

import com.atoss.idea.management.system.repository.entity.Role;
import com.atoss.idea.management.system.repository.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class UserRegisterDTO implements UserDetails {
    private Long id;

    private String username;

    private String email;

    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor for the UserRegisterDTO class.
     *
     * @param id          The unique identifier of the user.
     * @param username    The username of the user.
     * @param email       The email address of the user.
     * @param password    The password of the user.
     * @param authorities The collection of authorities (roles) assigned to the user.
     *
     * @see GrantedAuthority
     */
    public UserRegisterDTO(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Static method to build a UserRegisterDTO object from a User entity.
     *
     * @param user The User entity containing user-specific data.
     *
     * @return A UserRegisterDTO object representing user details extracted from the User entity.
     *
     * @see User
     * @see Role
     * @see SimpleGrantedAuthority
     * @see GrantedAuthority
     */
    public static UserRegisterDTO build(User user) {
        List<GrantedAuthority> authorities = Arrays.asList(Role.STANDARD, Role.ADMIN)
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        return new UserRegisterDTO(user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRegisterDTO user = (UserRegisterDTO) o;
        return Objects.equals(id, user.id);
    }
}
