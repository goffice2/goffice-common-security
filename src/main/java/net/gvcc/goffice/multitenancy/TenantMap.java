package net.gvcc.goffice.multitenancy;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TenantMap {
	private Map<String, TenantInfo> tenants = new HashMap<>();

	public void clear() {
		if (tenants != null) {
			tenants.clear();
		}
	}
}
