/*
 * goffice... 
 * https://www.goffice.org
 * 
 * Copyright (c) 2005-2022 Consorzio dei Comuni della Provincia di Bolzano Soc. Coop. <https://www.gvcc.net>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.gvcc.goffice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

/**
 * Performs authentication on a {@link KeycloakAuthenticationToken}.
 *
 * @author <a href="mailto:srossillo@smartling.com">Scott Rossillo</a>
 * @version $Revision: 1 $
 */
public class GofficeKeycloakAuthenticationProvider implements AuthenticationProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(GofficeKeycloakAuthenticationProvider.class);

	private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	public void setGrantedAuthoritiesMapper(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
		this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		LOGGER.info("authenticate - START");

		KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
		List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

		token.getAccount().getRoles().stream().forEach(role -> {
			grantedAuthorities.add(new KeycloakRole(role));
		});

		getRealRoles(token).stream().forEach(role -> {
			LOGGER.trace("authenticate - Real Role: {}", role);
			grantedAuthorities.add(new KeycloakRole(role));
		});

		KeycloakAuthenticationToken authToken = new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), mapAuthorities(grantedAuthorities));

		LOGGER.info("authenticate - END");

		return authToken;
	}

	private Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
		return grantedAuthoritiesMapper != null ? grantedAuthoritiesMapper.mapAuthorities(authorities) : authorities;
	}

	public List<String> getRealRoles(KeycloakAuthenticationToken token) {
		LOGGER.info("getRealRoles - START");

		KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) token.getPrincipal();

		Map<String, Object> claimList = kp.getKeycloakSecurityContext().getToken().getOtherClaims();

		List<String> groupList = claimList == null ? null : (List<String>) claimList.get("groups");

		List<String> realRolesList = Collections.emptyList();

		if (groupList != null) {
			realRolesList = groupList.stream().filter(g -> g.startsWith("/")) //
					.map(g -> g.replaceAll("/", "-").substring(1)) //
					.collect(Collectors.toList());
		}

		LOGGER.info("getRealRoles - END");

		return realRolesList;
	}

	// public List<String> getRealRoles(KeycloakAuthenticationToken token) {
	// OidcKeycloakAccount account = token.getAccount();
	// IDToken idToken = account.getKeycloakSecurityContext().getIdToken();
	// if (idToken != null) {
	// List<String> groups = (List<String>) idToken.getOtherClaims().get("groups");
	// return groups.stream().filter(g -> g.startsWith("/")).map(g -> g.replaceAll("/", "-").substring(1)).collect(Collectors.toList());
	// }
	// return Collections.emptyList();
	// }

	@Override
	public boolean supports(Class<?> aClass) {
		return KeycloakAuthenticationToken.class.isAssignableFrom(aClass);
	}
}
