package de.technikteam.security;

import de.technikteam.config.Permissions;
import de.technikteam.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUser implements UserDetails, Serializable {

	private static final long serialVersionUID = 1L;
	private final User user;

	public SecurityUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Convert the user's permission strings and role into Spring Security GrantedAuthority objects.
		Set<String> permissionKeys = new HashSet<>(user.getPermissions());

		// If user has the master admin permission, grant them all other permissions programmatically.
		if (permissionKeys.contains(Permissions.ACCESS_ADMIN_PANEL)) {
			Field[] fields = Permissions.class.getDeclaredFields();
			Arrays.stream(fields)
					.filter(field -> java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().equals(String.class))
					.forEach(field -> {
						try {
							permissionKeys.add((String) field.get(null));
						} catch (IllegalAccessException e) {
							// Should not happen for public static final fields
						}
					});
		}

		Set<GrantedAuthority> authorities = permissionKeys.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());

		// Add role as an authority, prefixed with "ROLE_"
		if (user.getRoleName() != null && !user.getRoleName().isBlank()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleName().toUpperCase()));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
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
}