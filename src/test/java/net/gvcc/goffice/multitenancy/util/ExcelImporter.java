package net.gvcc.goffice.multitenancy.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import net.gvcc.goffice.crypt.CryptManager;
import net.gvcc.goffice.multitenancy.TenantInfo;
import net.gvcc.goffice.multitenancy.TenantMap;
import net.gvcc.goffice.multitenancy.test.bean.D3ProxyTenantInfo;

public class ExcelImporter {

	public static TenantMap importFromExcel(String fileName, CryptManager cryptManager) {
		TenantMap tenants = new TenantMap();

		try (InputStream file = new ClassPathResource(fileName).getInputStream();
				// Create Workbook instance holding reference to .xlsx file
				XSSFWorkbook workbook = new XSSFWorkbook(file);) {
			// Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);
			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			if (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Map<Integer, String> headers = processHeader(row);
				// For each row, iterate through all the columns
				while (rowIterator.hasNext()) {
					row = rowIterator.next();
					TenantInfo tenantInfo = processRow(row, headers, cryptManager);
					String keycloakValue = StringUtils.trimToNull(tenantInfo.getKeycloak());
					if (keycloakValue != null) {
						tenants.getTenants().put(keycloakValue.toLowerCase(), tenantInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tenants;
	}

	private static Map<Integer, String> processHeader(Row row) {
		Map<Integer, String> headers = new HashMap<>();
		Iterator<Cell> cellIterator = row.cellIterator();
		int i = 0;
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (CellType.STRING.equals(cell.getCellType())) {
				headers.put(i, cell.getStringCellValue());
			}
			i++;
		}
		return headers;
	}

	private static TenantInfo processRow(Row row, Map<Integer, String> headers, CryptManager cryptManager) throws Exception {
		Iterator<Cell> cellIterator = row.cellIterator();
		TenantInfo.TenantInfoBuilder builder = TenantInfo.builder();
		D3ProxyTenantInfo.D3ProxyTenantInfoBuilder d3Builder = D3ProxyTenantInfo.builder();
		int i = 0;
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (CellType.STRING.equals(cell.getCellType())) {
				String value = cell.getStringCellValue();
				switch (headers.get(i)) {
					case "LDAP_O":
						builder.ldapO(value);
						break;
					case "ISTAT":
						builder.istat(value);
						break;
					case "iPA_AOO":
						builder.ipaAoo(value);
						break;
					case "COD_CATASTALE":
						builder.codCatastale(value);
						break;
					case "KEYCLOAK":
						builder.keycloak(value);
						break;
					// case "D3_SERVER":
					// d3Builder.dmsUrl(value);
					// break;
					// case "D3_ARCHIVE":
					// d3Builder.dmsLoginUrl(value);
					// break;
					// case "D3_REPOSITORY":
					// d3Builder.dmsRepository(value);
					// break;
					// case "D3_USER":
					// d3Builder.dmsUser(value);
					// break;
					// case "D3_PASSWORD":
					// d3Builder.dmsPassword(cryptManager.encryptJasypt(value));
					// break;
					default:
				}
			}
			i++;
		}

		Map<String, Object> map = new HashMap<>();
		map.put(D3ProxyTenantInfo.class.getName(), d3Builder.build());
		builder.additionalInfo(map);
		return builder.build();
	}
	//
	// private static File getFile(String fileName) throws IOException {
	// ClassLoader classLoader = ExcelImporter.class.getClassLoader();
	// URL resource = classLoader.getResource(fileName);
	//
	// if (resource == null) {
	// throw new IllegalArgumentException("file is not found!");
	// } else {
	// return new File(resource.getFile());
	// }
	// }
}
