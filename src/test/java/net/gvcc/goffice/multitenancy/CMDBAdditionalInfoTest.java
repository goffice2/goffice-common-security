package net.gvcc.goffice.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.gvcc.goffice.multitenancy.test.bean.D3ProxyTenantInfo;

@SpringBootTest(properties = { "goffice.common.persistence.tenant.file=classpath:additionalInfo.json" })
@TestInstance(Lifecycle.PER_CLASS)
public class CMDBAdditionalInfoTest {

	@Autowired
	TenantConfigurator tenantService;

	@Test
	public void loadContext() throws JsonProcessingException {
		Optional<TenantInfo> info = tenantService.getInfoFromKeycloakId("Bolzano");
		assertNotNull(info);
		assertTrue(info.isPresent());
		Map<String, Object> additionalInfo = info.get().getAdditionalInfo();
		assertNotNull(additionalInfo);
		Object dmsTenantInfoMap = additionalInfo.get(D3ProxyTenantInfo.class.getName());
		ObjectMapper mapper = new ObjectMapper();
		String map = mapper.writeValueAsString(dmsTenantInfoMap);
		D3ProxyTenantInfo dmsTenantInfo = mapper.readValue(map, D3ProxyTenantInfo.class);

		assertNotNull(dmsTenantInfo);
		assertEquals("url", dmsTenantInfo.getDmsUrl());
		assertEquals("repository", dmsTenantInfo.getDmsRepository());

		Optional<D3ProxyTenantInfo> dmsTenantInfoOpt = tenantService.getAdditionalInfoFromKey("Bolzano", D3ProxyTenantInfo.class);
		assertNotNull(dmsTenantInfoOpt);
		assertTrue(dmsTenantInfoOpt.isPresent());
		dmsTenantInfo = dmsTenantInfoOpt.get();
		assertEquals("url", dmsTenantInfo.getDmsUrl());
		assertEquals("repository", dmsTenantInfo.getDmsRepository());

		Optional<TenantInfo> tenantInfoOpt = tenantService.getAdditionalInfoFromKey("Bolzano", TenantInfo.class);
		assertNotNull(tenantInfoOpt);
		assertFalse(tenantInfoOpt.isPresent());
	}
}
