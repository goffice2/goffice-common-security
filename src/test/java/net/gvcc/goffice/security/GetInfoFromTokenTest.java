package net.gvcc.goffice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.test.context.support.WithMockUser;

import net.gvcc.goffice.config.GofficeSecurityService;

//@SpringBootTest
// @ContextConfiguration(classes = {GofficeSecurityConfig.class,
// Configuration.class,
// GofficeSecurityService.class
//
// })
@PropertySources({ @PropertySource("classpath:application.properties"), @PropertySource("classpath:security.properties") })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GetInfoFromTokenTest {

	@Autowired
	GofficeSecurityService service;

	@Test
	@WithMockUser(username = "admin", authorities = { "ROLE_ENTI-Bolzano-Contratti-Admin", "ROLE_ENTI-Bolzano-Contratti-Operator", "ROLE_ENTI-Bolzano-Contratti-User", "ROLE_ENTI-Laas-Contratti-Admin",
			"ROLE_ENTI-Laas-Contratti-Operator", "ROLE_ENTI-Laas-Contratti-User", })
	public void getEntiTest() {
		Set<String> enti = service.getEnti();
		Set<String> expectedEnti = new HashSet<String>(Arrays.asList("Bolzano", "Laas"));
		assertNotNull(expectedEnti);
		// enti.forEach(System.out::println);
		assertEquals(expectedEnti.size(), enti.size());
		assertTrue(enti.containsAll(expectedEnti));
	}

	@Test
	@WithMockUser(username = "admin", authorities = { "ROLE_ENTI-Bolzano-Contratti-Admin", "ROLE_ENTI-Bolzano-Contratti-Operator", "ROLE_ENTI-Bolzano-Contratti-User", "ROLE_ENTI-Laas-Contratti-Admin",
			"ROLE_ENTI-Laas-Contratti-Operator", "ROLE_ENTI-Laas-Contratti-User", })
	public void getRuoliTest() {
		List<String> roles = service.getRoles();
		List<String> expectedRoles = Arrays.asList("Bolzano-Contratti-Admin", "Bolzano-Contratti-Operator", "Bolzano-Contratti-User", "Laas-Contratti-Admin", "Laas-Contratti-Operator",
				"Laas-Contratti-User");
		assertNotNull(roles);
		assertEquals(6, roles.size());
		assertTrue(roles.containsAll(expectedRoles));
	}
}
