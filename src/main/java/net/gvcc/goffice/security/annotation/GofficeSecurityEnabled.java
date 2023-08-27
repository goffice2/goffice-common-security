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
package net.gvcc.goffice.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import net.gvcc.goffice.client.GvccClientManager;
import net.gvcc.goffice.config.WebConfiguration;
import net.gvcc.goffice.logger.headers.interceptor.RequestHeadersLoggingInterceptor;
import net.gvcc.goffice.opentracing.IOpenTracingStorage;
import net.gvcc.goffice.token.ThreadLocalTokenStorage;

/**
 *
 * <p>
 * The <code>GofficeSecurityEnabled</code> class
 * </p>
 * <p>
 * Data: 29 apr 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@ComponentScan(basePackageClasses = { //
		KeycloakSecurityComponents.class, //
		KeycloakClientRequestFactory.class, //
		WebConfiguration.class, //
		IOpenTracingStorage.class, //
		RequestHeadersLoggingInterceptor.class, //
		ThreadLocalTokenStorage.class, //
		GvccClientManager.class //
})
@PropertySources({ //
		@PropertySource(value = "classpath:security.properties"), //
		@PropertySource(value = "classpath:application.properties"), //
		@PropertySource(value = "file:/var/goffice/config/security.properties", ignoreResourceNotFound = true) //
})
public @interface GofficeSecurityEnabled {
}