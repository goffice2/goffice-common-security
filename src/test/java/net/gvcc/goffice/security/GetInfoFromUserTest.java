package net.gvcc.goffice.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import net.gvcc.goffice.config.GofficeSecurityService;

@PropertySources({ @PropertySource("classpath:application.properties"), @PropertySource("classpath:security.properties") })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GetInfoFromUserTest {
	private static final Logger log = LoggerFactory.getLogger(GetInfoFromUserTest.class);

	@Value("${goffice.common.security.urltoken}")
	private String urltoken;
	@Value("${goffice.common.security.username}")
	private String usernameDS;
	@Value("${goffice.common.security.password}")
	private String password;
	@Value("${goffice.common.security.clientid}")
	private String clientid;
	@Value("${goffice.common.security.clientsecret}")
	private String clientsecret;
	@Value("${goffice.common.security.granttype}")
	private String granttype;
	String baseUrl;
	@Value("${goffice.common.security.server.keycloak}")
	private String serverkeycloack;
	@Value("${goffice.common.security.realm}")
	private String realm;
	String updateUrl = "";
	String userUrl = "";
	String moduleUrl = "";
	@LocalServerPort
	private int port;

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER_TOKEN = "Bearer ";

	@BeforeEach
	public void setUp() {
		baseUrl = "http://localhost".concat(":").concat(port + "").concat("/sample/test");
		// baseUrl = baseUrl.concat("/api/1.0");
		baseUrl = baseUrl.concat("/getlocale");
	}

	@Autowired
	GofficeSecurityService service;

	@Test
	public void getLocaleTest() {
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, getEntity(null), String.class);
		// da validare in base alle securities properties
		log.debug("locale: " + response.getBody());
		System.out.println("locale: " + response.getBody());
		assertNotNull(response);
	}

	private HttpEntity<Object> getEntity(Object req) {
		HttpHeaders headers = new HttpHeaders();
		headers.set(AUTHORIZATION, BEARER_TOKEN + getToken());
		headers.add("tenant", "bolzano");
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> entity = new HttpEntity<>(req, headers);
		return entity;
	}

	public String getToken() {
		Keycloak keycloak = KeycloakBuilder.builder().serverUrl(serverkeycloack).grantType(OAuth2Constants.PASSWORD.equals(granttype) ? OAuth2Constants.PASSWORD : OAuth2Constants.CLIENT_CREDENTIALS)
				.realm(realm).clientId(clientid).clientSecret(clientsecret).username(usernameDS).password(password).resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
		return keycloak.tokenManager().getAccessToken().getToken();
	}
}
