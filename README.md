# Goffice Jwt Lib

libreria comune per JWT

per l'uso di questa libreria è necessario:
1. aggiungere la dipendenza al pom del proprio progetto 
        "
		<dependency>
          <groupId>net.gvcc.goffice.common</groupId>
          <artifactId>goffice-common-security</artifactId>
          <version>2.0.0-SNAPSHOT</version>
		  </dependency>
		"
2. creare nel proprio progetto una classe SecurityCustomConfig.java con questo body

```
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;


import net.gvcc.goffice.SecurityConfig;

@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true, proxyTargetClass = true)
@PropertySource("classpath:security.properties")
@PropertySource("file:/var/goffice/config/security.properties")
public class SecurityCustomConfig extends SecurityConfig {
	static Logger log = LoggerFactory.getLogger(SecurityCustomConfig.class);

public class SecurityCustomConfig extends SecurityConfig {
	static Logger log = LoggerFactory.getLogger(SecurityCustomConfig.class);

	/*
	 * @Override protected void configure(HttpSecurity http) throws Exception { http.anonymous().disable(); log.info("serviceUrl1:" +
	 * Arrays.asList(privateUrls).stream().collect(Collectors.joining(", ")));
	 * 
	 * http.authorizeRequests().antMatchers(privateUrls).authenticated() // .anyRequest().permitAll();
	 * 
	 * http.logout().addLogoutHandler(keycloakLogoutHandler()).logoutUrl("/sso/logout").permitAll().logoutSuccessUrl("/"); http.addFilterBefore(keycloakPreAuthActionsFilter(),
	 * LogoutFilter.class).addFilterBefore(keycloakAuthenticationProcessingFilter(), BasicAuthenticationFilter.class); http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
	 * http.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy());
	 * 
	 * http.csrf().disable(); }
	 */
}
```
3. nel docker-compose.yml del microserivizo aggiungere nel service

```
volumes:
      - /pathto/security.properties (contenente eventuali parametri da sovrascrivere):/var/goffice/config/security.properties
```

