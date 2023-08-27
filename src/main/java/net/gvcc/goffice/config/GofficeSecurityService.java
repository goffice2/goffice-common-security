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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import net.gvcc.goffice.multitenancy.ITenantService;

/**
 * @author Marco Mancuso
 * @author Renzo Poli
 *
 */
@Component("gofficeSecurityService")
public class GofficeSecurityService implements ISecurityService {
	private static Logger LOGGER = LoggerFactory.getLogger(GofficeSecurityService.class);

	private static final String KEYCLOAK_ROLE_PREFIX = "ROLE_";
	private static final String KEYCLOAK_ROLE_SEPARATOR = "-";

	private static final String GVCC_SERVICEROLE_PREFIX = "SERVICE" + KEYCLOAK_ROLE_SEPARATOR;
	private static final String GVCC_USERROLE_PREFIX = "ENTI" + KEYCLOAK_ROLE_SEPARATOR;

	private static final String ROLE_TEMPLATE_USER = "%s%s%s" + KEYCLOAK_ROLE_SEPARATOR + "%s";
	private static final String ROLE_TEMPLATE_SERVICE = "%s%s%s";

	private static final String USERROLE_WILDCARD = "*";
	private static final String USERROLE_WILDCARD_IDENTIFICATOR = KEYCLOAK_ROLE_SEPARATOR + USERROLE_WILDCARD;

	// ============================================================================================================================================ //

	@Autowired
	private ITenantService tenantService;

	/**
	 * Questo metodo controlla che, fra le autorizzazioni rilevate nel token JWT del corrente contesto di security, vi sia il ruolo richiesto.
	 * <p>
	 * Allo stato attuale, le autorizzazioni provengono dall'assegnazione GRUPPI &lt;--&gt; UTENTI in Keycloak.
	 * 
	 * @param role
	 *            Codice del ruolo richiesto (es. Licenze-User)
	 * @return boolean
	 *         <p>
	 *         true: se l'account appartiene al ruolo richiesto
	 *         <p>
	 *         false: se il ruolo richiesto non è fra quelli assegnati all'account correntemente attivo
	 * 
	 * @see hasPermission(String... roles)
	 * @see hasServicePermission(String roles)
	 * @see hasServicePermission(String... roles)
	 */
	@Override
	public boolean hasPermission(String role) {
		return hasPermission(new String[] { role });
	}

	/**
	 * Questo metodo controlla che, fra le autorizzazioni rilevate nel token JWT del corrente contesto di security, vi sia almeno uno dei ruoli richiesti.
	 * <p>
	 * Allo stato attuale, le autorizzazioni provengono dall'assegnazione GRUPPI &lt;--&gt; UTENTI in Keycloak.
	 * 
	 * @param roles
	 *            Lista codici dei ruoli richiesti (es. "Licenze-User", "CDU-User", ...)
	 * @return boolean
	 *         <p>
	 *         true: se l'account appartiene al ruolo richiesto
	 *         <p>
	 *         false: se il ruolo richiesto non è fra quelli assegnati all'account correntemente attivo
	 * 
	 * @see hasPermission(String role)
	 * @see hasServicePermission(String roles)
	 * @see hasServicePermission(String... roles)
	 */
	@Override
	public boolean hasPermission(String... roles) {
		LOGGER.debug("hasPermission - START");

		String tenant = tenantService.getTenant();
		LOGGER.debug("hasPermission - tenant:..........{}", tenant);
		LOGGER.debug("hasPermission - username:........{}", getUsername());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean hasPermission = false;

		for (String role : roles) {
			LOGGER.debug("hasPermission");
			LOGGER.debug("hasPermission - role:............{}", role);

			boolean wildcardRole = role.endsWith(USERROLE_WILDCARD_IDENTIFICATOR);
			if (wildcardRole) {
				role = role.substring(0, role.length() - USERROLE_WILDCARD.length()); // removed the wildcard part (last chars)
			}

			String canonicalRole = String.format(ROLE_TEMPLATE_USER, KEYCLOAK_ROLE_PREFIX, GVCC_USERROLE_PREFIX, tenant, role).toLowerCase(); // ignore case
			LOGGER.debug("hasPermission - canonicalRole:...{}", canonicalRole);

			hasPermission = authentication.getAuthorities().stream().anyMatch(auth -> {
				boolean matches = false;

				String assignedRole = auth.getAuthority().toLowerCase(); // ignore case
				LOGGER.trace("hasPermission - assignedRole:....{}", assignedRole);

				if (wildcardRole) {
					matches = assignedRole.startsWith(canonicalRole); // e.g.: assigned=ROLE_ENTI-Bolzano-CDU-Admin, canonical=ROLE_ENTI-Bolzano-CDU-* (without '*')
				} else {
					matches = assignedRole.equals(canonicalRole);
				}

				if (matches && LOGGER.isDebugEnabled()) {
					LOGGER.debug("hasPermission - matches:.........true (real role: {})", auth.getAuthority());
				}

				return matches;
			});

			LOGGER.debug("hasPermission - has permission:..{}", hasPermission);

			if (hasPermission) {
				break;
			}
		}

		if (roles.length > 1) {
			LOGGER.debug("hasPermission");
			LOGGER.debug("hasPermission - has permission:..{}", hasPermission);
		}

		LOGGER.debug("hasPermission - END");

		return hasPermission;
	}

	/**
	 * Questo metodo controlla che, fra le autorizzazioni rilevate nel token JWT del corrente contesto di security, vi sia il ruolo richiesto.
	 * <p>
	 * Allo stato attuale, le autorizzazioni provengono dall'assegnazione RUOLI &lt;--&gt; UTENTI in Keycloak.
	 * 
	 * @param role
	 *            Codice del ruolo richiesto (es. Licenze-User)
	 * @return boolean
	 *         <p>
	 *         true: se l'account appartiene al ruolo richiesto
	 *         <p>
	 *         false: se il ruolo richiesto non è fra quelli assegnati all'account correntemente attivo
	 * 
	 * @see hasServicePermission(String... roles)
	 * @see hasPermission(String... roles)
	 * @see hasPermission(String role)
	 */
	@Override
	public boolean hasServicePermission(String role) {
		return hasServicePermission(new String[] { role });
	}

	/**
	 * Questo metodo controlla che, fra le autorizzazioni rilevate nel token JWT del corrente contesto di security, vi sia almeno uno dei ruoli richiesti.
	 * <p>
	 * Allo stato attuale, le autorizzazioni provengono dall'assegnazione RUOLI &lt;--&gt; UTENTI in Keycloak.
	 * 
	 * @param roles
	 *            Lista codici dei ruoli richiesti (es. "Licenze-User", "CDU-User", ...)
	 * @return boolean
	 *         <p>
	 *         true: se l'account appartiene al ruolo richiesto
	 *         <p>
	 *         false: se il ruolo richiesto non è fra quelli assegnati all'account correntemente attivo
	 * 
	 * @see hasServicePermission(String role)
	 * @see hasPermission(String... roles)
	 * @see hasPermission(String role)
	 */

	@Override
	public boolean hasServicePermission(String... roles) {
		LOGGER.debug("hasServicePermission - START");

		LOGGER.debug("hasServicePermission - username:........{}", getUsername());

		boolean hasPermission = false;

		for (String role : roles) {
			LOGGER.debug("hasServicePermission");
			LOGGER.debug("hasServicePermission - role:............{}", role);

			String canonicalRole = String.format(ROLE_TEMPLATE_SERVICE, KEYCLOAK_ROLE_PREFIX, GVCC_SERVICEROLE_PREFIX, role).toLowerCase(); // ignore case
			LOGGER.debug("hasServicePermission - canonicalRole:...{}", canonicalRole);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			hasPermission = authentication.getAuthorities().stream().anyMatch(auth -> {
				String assignedRole = auth.getAuthority().toLowerCase(); // ignore case
				LOGGER.trace("hasServicePermission - assignedRole:....{}", assignedRole);

				boolean matches = assignedRole.equals(canonicalRole);

				if (matches && LOGGER.isDebugEnabled()) {
					LOGGER.debug("hasServicePermission - matches:.........true (real role: {})", auth.getAuthority());
				}

				return matches;
			});

			LOGGER.debug("hasServicePermission - has permission:..{}", hasPermission);

			if (hasPermission) {
				break;
			}
		}

		if (roles.length > 1) {
			LOGGER.debug("hasServicePermission");
			LOGGER.debug("hasServicePermission - has permission:..{}", hasPermission);
		}

		LOGGER.debug("hasServicePermission - END");

		return hasPermission;
	}

	@Override
	public String getCanonicalRole(String ente, String role) {
		String canonicalRole = "";
		if (ente != null) {
			canonicalRole = ente.concat("-").concat(role);
		}
		return canonicalRole;
	}

	/**
	 * @return List
	 */
	@Override
	public List<String> getRoles() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getAuthorities().stream() //
				.filter(auth -> auth.getAuthority().startsWith(KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX)) //
				.map(auth -> auth.getAuthority().substring((KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX).length())) //
				.collect(Collectors.toList());
	}

	@Override
	public Set<String> getModules() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getAuthorities().stream() //
				.filter(auth -> auth.getAuthority().startsWith(KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX)) //
				.map(s -> s.getAuthority().substring((KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX).length())) //
				.filter(s -> s.contains(KEYCLOAK_ROLE_SEPARATOR)).map(s -> s.substring(s.indexOf(KEYCLOAK_ROLE_SEPARATOR) + 1)) //
				.filter(s -> s.contains(KEYCLOAK_ROLE_SEPARATOR)).map(s -> s.substring(0, s.indexOf(KEYCLOAK_ROLE_SEPARATOR))) //
				.collect(Collectors.toSet());
	}

	@Override
	public Set<String> getModules(String ente) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getAuthorities().stream() //
				.filter(auth -> { //
					String authority = StringUtils.defaultString(auth.getAuthority()).trim();
					return authority.startsWith(KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX) //
							&& ente != null //
							&& authority.toLowerCase().contains(ente.toLowerCase()); //
				}) //
				.map(s -> s.getAuthority().substring((KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX).length())) //
				.filter(s -> s.contains(KEYCLOAK_ROLE_SEPARATOR)).map(s -> s.substring(s.indexOf(KEYCLOAK_ROLE_SEPARATOR) + 1)) //
				.filter(s -> s.contains(KEYCLOAK_ROLE_SEPARATOR)).map(s -> s.substring(0, s.indexOf(KEYCLOAK_ROLE_SEPARATOR))) //
				.collect(Collectors.toSet());
	}

	@Override
	public Set<String> getEnti() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication.getAuthorities().stream() //
				.filter(auth -> auth.getAuthority().startsWith(KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX)) //
				.map(s -> s.getAuthority().substring((KEYCLOAK_ROLE_PREFIX + GVCC_USERROLE_PREFIX).length())) //
				.map(s -> {
					if (s.contains(KEYCLOAK_ROLE_SEPARATOR)) {
						s = s.substring(0, s.indexOf(KEYCLOAK_ROLE_SEPARATOR));
					}
					return s;
				}) //
				.collect(Collectors.toSet());
	}

	@Override
	public String getAccessToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		KeycloakSecurityContext keycloakSecurityContext = new KeycloakSecurityContext();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof KeycloakPrincipal) {
				keycloakSecurityContext = KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext();
			}
		}
		return keycloakSecurityContext.getTokenString();
	}

	@Override
	public String getLocale() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String locale = null;
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof KeycloakPrincipal) {
				locale = KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext().getToken().getLocale();
			}
		}
		return locale;
	}

	@Override
	public String getUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}
}
