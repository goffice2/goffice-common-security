package net.gvcc.goffice.multitenancy.test.bean;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
public class DbmsTenantInfo {
	private String owner;
	private String name;
	private boolean main;
	@Setter
	private String url;
	@Setter
	private String username;
	@Setter
	private String password;
	private String driverClassName;
	private String sqlDialect;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("=== DbmsInfo ===") //
				.append("\nowner:............").append(owner) //
				.append("\nname:.............").append(name) //
				.append("\nmain:.............").append(main) //
				.append("\nurl:..............").append(url) //
				.append("\nusername:.........").append(username) //
				.append("\npassword:.........").append(StringUtils.isEmpty(password) ? "" : "******") //
				.append("\ndriverClassName:..").append(driverClassName) //
				.append("\nsqlDialect:.......").append(sqlDialect);

		return builder.toString();
	}
}
