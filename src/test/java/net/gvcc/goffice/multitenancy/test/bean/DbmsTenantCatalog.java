package net.gvcc.goffice.multitenancy.test.bean;

import java.util.ArrayList;
import java.util.Optional;

public class DbmsTenantCatalog extends ArrayList<DbmsTenantInfo> {

	private static final long serialVersionUID = 1L;

	public Optional<DbmsTenantInfo> getByName(String name) {
		return stream() //
				.filter(info -> name.equalsIgnoreCase(info.getName())) //
				.findFirst();
	}
}
