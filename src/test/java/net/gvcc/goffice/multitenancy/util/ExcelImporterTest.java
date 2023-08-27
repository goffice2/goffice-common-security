package net.gvcc.goffice.multitenancy.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.gvcc.goffice.crypt.CryptManager;
import net.gvcc.goffice.multitenancy.TenantConfigurator;
import net.gvcc.goffice.multitenancy.TenantInfo;
import net.gvcc.goffice.multitenancy.TenantMap;

@SpringBootTest
public class ExcelImporterTest {
	private static Logger LOGGER = LoggerFactory.getLogger(ExcelImporterTest.class);

	@Autowired
	CryptManager cryptManager;

	@Autowired
	TenantConfigurator tenantService;

	@Test
	public void decrypt() {
		LOGGER.info("decrypt - START");

		String tobeDecrypt = "VbUbdG1z2DlMp4zmDRWDQg==";
		try {
			String value = cryptManager.decryptJasypt(tobeDecrypt);
			assertNotNull(value);
			assertEquals(value, "pwd");
		} catch (RuntimeException e) {
			LOGGER.error("decrypt", e);
		}

		LOGGER.info("decrypt - END");
	}

	@Test
	public void testExcelToJSON() {
		LOGGER.info("testExcelToJSON - START");

		TenantMap tenants = ExcelImporter.importFromExcel("comuni.xlsx", cryptManager);
		assertNotNull(tenants);
		TenantInfo partschins = tenants.getTenants().get("partschins");
		assertNotNull(partschins);
		assertEquals("21062", partschins.getIstat());
		assertEquals("PARTSCHINS", partschins.getLdapO());
		assertEquals("G328", partschins.getCodCatastale());
		// assertEquals("PARTSCHINS", partschins.getNomeDE());
		// assertEquals("PARCINES", partschins.getNomeIT());
		assertEquals("C_G328", partschins.getIpaAoo());
		printFile(tenants);

		LOGGER.info("testExcelToJSON - END");
	}

	private void printFile(TenantMap tenants) {
		LOGGER.info("printFile - START");

		try {
			String json = new ObjectMapper().writeValueAsString(tenants);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/test/resources/test1.json"))) {
				writer.write(json);
			}
		} catch (Exception e) {
			LOGGER.error("printFile", e);
		}

		LOGGER.info("printFile - END");
	}
}
