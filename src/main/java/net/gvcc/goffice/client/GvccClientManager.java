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
package net.gvcc.goffice.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;

import org.json.JSONObject;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import net.gvcc.goffice.client.interceptor.RestTemplateHeaderModifierInterceptor;
import net.gvcc.goffice.language.ILanguageStorage;
import net.gvcc.goffice.language.ThreadLocalLanguageStorage;
import net.gvcc.goffice.multitenancy.GofficeTenantService;
import net.gvcc.goffice.multitenancy.ITenantService;
import net.gvcc.goffice.opentracing.IOpenTracingStorage;
import net.gvcc.goffice.opentracing.ThreadLocalOpenTracingStorage;
import net.gvcc.goffice.token.ITokenStorage;

/****
 * <p>
 * *The<code>GvccClientManager</code>class*
 * </p>
 * *
 * <p>
 * *Data:22 mar 2022*
 * </p>
 * **
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
/**
 *
 * <p>
 * The <code>GvccClientManager</code> class
 * </p>
 * <p>
 * Data: 28 mar 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
@Component
public class GvccClientManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GvccClientManager.class);

	private static final String SET_BASE_PATH = "setBasePath";
	private static final String GET_AUTHENTICATION = "getAuthentication";
	private static final String GET_API_CLIENT = "getApiClient";
	private static final String SET_BEARER_TOKEN = "setBearerToken";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	private static final String CLIENT_ID = "client_id";
	private static final String GRANT_TYPE = "grant_type";

	@Autowired
	protected Configuration config;

	@Autowired
	ITenantService tenantService;

	@Autowired
	ILanguageStorage languageStorage;

	@Autowired
	ITokenStorage tokenStorage;

	@Autowired
	IOpenTracingStorage openTracingStorage;

	@Value("${goffice.common.logging.client.traceHttpContent:false}")
	private boolean traceHttpContent;

	/**
	 * @return String
	 */
	public String createToken() {
		LOGGER.trace("createToken - START");

		String token = "";

		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			if (config == null) {
				config = new Configuration();
			}

			map.add(GRANT_TYPE, config.getGrantType());
			map.add(CLIENT_ID, config.getClientId());
			map.add(PASSWORD, config.getPassword());
			map.add(USERNAME, config.getUsername());
			map.add(CLIENT_SECRET, config.getClientSecret());

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
			HttpEntity<String> response = restTemplate.exchange(config.getUrlToken(), HttpMethod.POST, request, String.class);
			JSONObject jsonObject = new JSONObject(response.getBody());

			token = (String) jsonObject.get(ACCESS_TOKEN);
		} catch (Exception e) {
			LOGGER.error("createToken", e);
		}

		LOGGER.trace("createToken - END");

		return token;
	}

	/**
	 * @return String
	 */
	public String getAccessToken() {
		LOGGER.trace("getAccessToken - START");

		org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		KeycloakSecurityContext keycloakSecurityContext = new KeycloakSecurityContext();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof KeycloakPrincipal) {
				keycloakSecurityContext = KeycloakPrincipal.class.cast(principal).getKeycloakSecurityContext();
			}
		}

		String token = keycloakSecurityContext.getTokenString();

		if (token == null && tokenStorage != null) {
			token = tokenStorage.getToken();

			// tokenStorage.clear();
			if (token != null) {
				DecodedJWT jwt = JWT.decode(token);
				if (jwt.getExpiresAt().toInstant().isBefore(Instant.now())) {
					token = createToken();
					tokenStorage.setToken(token);
				}
			}
		}

		LOGGER.trace("getAccessToken - END");

		return token;
	}

	/**
	 * @param apiClient
	 * @param securityScheme
	 * @param urlapi
	 * @return T
	 */
	public <T> T build(T apiClient, String securityScheme, String urlapi) {
		LOGGER.trace("build - START");

		try {
			// apiClient = (apiClient == null || getAccessToken() == null) ? (T) apiClient.getClass().getDeclaredConstructor().newInstance() : apiClient;
			apiClient = (T) apiClient.getClass().getDeclaredConstructor().newInstance();
			Method getDeclaredMethod = apiClient.getClass().getDeclaredMethod(GET_API_CLIENT);
			Object api = getDeclaredMethod.invoke(apiClient);

			Field field = api.getClass().getDeclaredField("restTemplate");
			field.setAccessible(true);
			RestTemplate restTemple = (RestTemplate) field.get(api);

			LOGGER.debug("build - adding client interceptors...");
			List<ClientHttpRequestInterceptor> interceptorList = restTemple.getInterceptors();

			LOGGER.debug("build - adding RestTemplateHeaderModifierInterceptor interceptor...");
			RestTemplateHeaderModifierInterceptor restTemplateInterceptor = new RestTemplateHeaderModifierInterceptor( //
					tenantService != null ? tenantService : new GofficeTenantService(), //
					languageStorage != null ? languageStorage : new ThreadLocalLanguageStorage(), //
					openTracingStorage != null ? openTracingStorage : new ThreadLocalOpenTracingStorage() //
			);
			interceptorList.add(restTemplateInterceptor);

			if (traceHttpContent) {
				LOGGER.debug("build - adding content (request/response) logging interceptor...");
				Method setDebugging = api.getClass().getDeclaredMethod("setDebugging", boolean.class);
				setDebugging.invoke(api, true);
			}

			LOGGER.debug("build - adding client interceptors...done");

			// set auth
			Method getAuth = api.getClass().getDeclaredMethod(GET_AUTHENTICATION, String.class);
			Object auth = getAuth.invoke(api, securityScheme);

			// set base url
			Method setBasePath = api.getClass().getDeclaredMethod(SET_BASE_PATH, String.class);
			config = config == null ? new Configuration() : config;
			setBasePath.invoke(api, urlapi);

			// set bearer token
			Method setBearerToken = auth.getClass().getDeclaredMethod(SET_BEARER_TOKEN, String.class);
			String token = getAccessToken();
			setBearerToken.invoke(auth, token != null ? token : createToken());
		} catch (Exception e) {
			LOGGER.error("build", e);
		}

		LOGGER.trace("build - END");

		return apiClient;
	}
}
