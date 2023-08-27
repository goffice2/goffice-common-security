package net.gvcc.goffice.multitenancy.test.bean;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 *
 * <p>
 * The <code>D3ProxyTenantInfo</code> class
 * </p>
 * <p>
 * Copyright: 2000 - 2022 <a href="https://www.gvcc.net">Consorzio dei Comuni della Provincia di Bolzano Società Cooperativa</a>
 * </p>
 * <p>
 * Data: 19.10.2022
 * </p>
 * 
 * @author <a href="mailto:info@gvcc.nett">info</a>
 * @version 1.0
 */

@Builder // only for tests!!!
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class D3ProxyTenantInfo {
	@Setter
	private String dmsUrl;
	@Setter
	private String dmsRepository;
	@Builder.Default // only for tests!!!
	private Map<String, String> userMappings = new HashMap<>();

	/**
	 * 
	 * @param goffice2Account
	 *            the goffice2Account as string
	 * @return the mapped user, if one. Otherwise, the default user
	 */
	public String getMappedUser(String goffice2Account) {
		return userMappings.get(goffice2Account);
	}
}
