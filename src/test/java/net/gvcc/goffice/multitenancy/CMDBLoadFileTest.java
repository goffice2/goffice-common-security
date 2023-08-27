package net.gvcc.goffice.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.gvcc.goffice.multitenancy.TenantInfo;
import net.gvcc.goffice.multitenancy.TenantConfigurator;

@SpringBootTest(properties = {
		"goffice.common.persistence.tenant.file=classpath:test.json"
})
@TestInstance(Lifecycle.PER_CLASS)
public class CMDBLoadFileTest {
	
	@Autowired
	TenantConfigurator tenantService;


	
	@Test
	public void loadContext() {
		Optional<TenantInfo> info=  tenantService.getInfoFromKeycloakId("ALDEIN");
		assertNotNull(info);
		assertTrue(info.isPresent());
		assertEquals("A179", info.get().getCodCatastale());
	}
	
}
