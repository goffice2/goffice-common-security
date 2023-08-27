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
package net.gvcc.goffice.security;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.gvcc.goffice.config.GofficeKeycloakAuthenticationProvider;

/**
 * spring security config
 *
 * <p>
 * The <code>SecurityConfig</code> class
 * </p>
 * <p>
 * Data: 27.10.2021
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class GofficeSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
	private static Logger LOGGER = LoggerFactory.getLogger(GofficeSecurityConfig.class);

	public static Map<String, String> userRoles = new HashMap<String, String>();

	@Autowired
	public KeycloakClientRequestFactory keycloakClientRequestFactory;

	@Value("${service.publicUrls}")
	private String[] publicUrls;

	@Value("${goffice.common.security.anonymous}")
	private boolean anonymous;

	@Override
	public void configure(WebSecurity web) {
		LOGGER.debug("configure(WebSecurity) - START");

		web.ignoring().antMatchers(
				// Vaadin Flow static resources
				"/VAADIN/**", //
				"/favicon.ico", //
				"/robots.txt",
				// web application manifest
				"/manifest.webmanifest", //
				"/sw.js", //
				"/offline-page.html", //
				"/icons/**", //
				"/images/**", //
				"/frontend/**", //
				"/webjars/**", //
				"/frontend-es5/**", //
				"/frontend-es6/**"//
		);

		LOGGER.debug("configure(WebSecurity) - END");
	}

	/*
	 * Metodo per configurare spring security autorizzando gli url inseriti in variabile publicUrls(non richiedono autorizzazione) e bloccando gli altri (chidedendo quindi autorizzazione)
	 * 
	 * @Param HttpSecurity
	 * 
	 * @Return void
	 */
	@Override
	@SuppressFBWarnings(value = { "SPRING_CSRF_PROTECTION_DISABLED" }, justification = "incompatible with vaadin ui")
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.debug("configure(HttpSecurity) - START");

		if (!anonymous) {
			http.anonymous().disable();
		}
		super.configure(http);

		http.authorizeRequests() //
				.antMatchers(publicUrls) //
				.permitAll() //
				.anyRequest() //
				.authenticated();
		http.formLogin().disable();
		// http.logout().addLogoutHandler(keycloakLogoutHandler()).logoutUrl("/sso/logout").permitAll().logoutSuccessUrl("/");
		http.addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class) //
				.addFilterBefore(keycloakAuthenticationProcessingFilter(), BasicAuthenticationFilter.class);
		http.exceptionHandling() //
				.authenticationEntryPoint(authenticationEntryPoint());
		http.sessionManagement() //
				.sessionAuthenticationStrategy(sessionAuthenticationStrategy());

		// http.csrf().disable();
		http.csrf().disable().headers().frameOptions().disable().and();

		LOGGER.debug("configure(HttpSecurity) - END");
	}

	/**
	 * @param auth
	 * @throws Exception
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		LOGGER.debug("configureGlobal - START");

		GofficeKeycloakAuthenticationProvider keycloakAuthenticationProvider = new GofficeKeycloakAuthenticationProvider();
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
		auth.authenticationProvider(keycloakAuthenticationProvider);

		LOGGER.debug("configureGlobal - END");
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	/**
	 * @return KeycloakSpringBootConfigResolver
	 */
	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	/**
	 * @return KeycloakRestTemplate
	 */
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KeycloakRestTemplate keycloakRestTemplate() {
		return new KeycloakRestTemplate(keycloakClientRequestFactory);
	}

	/**
	 * @param filter
	 * @return
	 */
	@Bean
	public FilterRegistrationBean<KeycloakAuthenticationProcessingFilter> keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter filter) {
		FilterRegistrationBean<KeycloakAuthenticationProcessingFilter> registrationBean = new FilterRegistrationBean<KeycloakAuthenticationProcessingFilter>(filter);
		registrationBean.setEnabled(false);
		return registrationBean;
	}

	/**
	 * @param filter
	 * @return FilterRegistrationBean
	 */
	@Bean
	public FilterRegistrationBean<KeycloakPreAuthActionsFilter> keycloakPreAuthActionsFilterRegistrationBean(KeycloakPreAuthActionsFilter filter) {
		FilterRegistrationBean<KeycloakPreAuthActionsFilter> registrationBean = new FilterRegistrationBean<KeycloakPreAuthActionsFilter>(filter);
		registrationBean.setEnabled(false);
		return registrationBean;
	}

}