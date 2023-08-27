package net.gvcc.goffice.multitenancy.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.gvcc.goffice.crypt.CryptManager;
import net.gvcc.goffice.multitenancy.TenantConfigurator;
import net.gvcc.goffice.multitenancy.TenantInfo;
import net.gvcc.goffice.multitenancy.test.bean.DbmsTenantCatalog;
import net.gvcc.goffice.multitenancy.test.bean.DbmsTenantInfo;

@SpringBootTest(properties = { "goffice.common.persistence.tenant.file=classpath:additionalInfo.json" })
public class DecryptTenantInfoTest {

	@Autowired
	CryptManager cryptManager;

	@Autowired
	TenantConfigurator tenantService;

	@Test
	public void decrypt() throws Exception {
		// String entryptedPassword = cryptManager.encryptJasypt("pwd");
		final String tenant = "Bolzano";

		tenantService.setTenant(tenant);

		Optional<TenantInfo> infoOpt = tenantService.getInfo();
		assertTrue(infoOpt.isPresent());
		TenantInfo info = infoOpt.get();
		assertNotNull(info);
		Map<String, Object> additionalInfo = info.getAdditionalInfo();
		assertNotNull(additionalInfo);
		assertTrue(tenant.equals(info.getKeycloak()));

		Optional<DbmsTenantCatalog> dmsCatalogsOpt = tenantService.getAdditionalInfo(DbmsTenantCatalog.class);
		assertTrue(dmsCatalogsOpt.isPresent());

		DbmsTenantCatalog dmsCatalogs = dmsCatalogsOpt.get();
		assertNotNull(dmsCatalogs);
		assertFalse(dmsCatalogs.isEmpty());

		DbmsTenantInfo dbmsInfo = dmsCatalogs.stream() //
				.filter(item -> "goffice-connector-goffice1".equals(item.getOwner())) //
				.findFirst() //
				.orElse(null);
		assertNotNull(dbmsInfo);

		String encryptedPassword = StringUtils.trimToNull(dbmsInfo.getPassword());
		assertNotNull(encryptedPassword);

		String decryptedPassword = cryptManager.decryptJasypt(encryptedPassword);
		assertNotNull(decryptedPassword);
		assertEquals("pwd", decryptedPassword);
	}
}
