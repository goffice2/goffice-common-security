/*
 * goffice... 
 * https://www.goffice.org
 * 
 * Copyright (c) 2005-2022 Consorzio dei Comuni della Provincia di Bolzano Soc. Coop. <https://www.gvcc.net>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.gvcc.goffice.client;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 
 *
 * <p>
 * The <code>Configuration</code> class
 * </p>
 * <p>
 * Data: 10 mar 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */

@Component
public class Configuration {
	private Properties properties;
	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

	@Value("${goffice.common.security.urltoken}")
	private String urltoken;
	@Value("${goffice.common.security.username}")
	private String username;
	@Value("${goffice.common.security.password}")
	private String password;
	@Value("${goffice.common.security.clientsecret}")
	private String clientSecret;
	@Value("${goffice.common.security.clientid}")
	private String clientid;
	@Value("${goffice.common.security.granttype}")
	private String granttype;
	@Value("${goffice.common.security.createtoken}")
	private String createtoken;
	@Value("${goffice.common.security.server.keycloak}")
	private String urlKeycloak;
	@Value("${goffice.common.security.realm}")
	private String realm;

	public Configuration() {
		properties = new Properties();

		try {
			final String configFileName = "client.properties";

			InputStream configFileInputStream = this.getClass().getClassLoader().getResourceAsStream(configFileName);
			if (configFileInputStream == null) {
				throw new FileNotFoundException(configFileName);
			}

			try (InputStream inputStream = configFileInputStream) {
				properties.load(inputStream);
			}
		} catch (FileNotFoundException e) {
			LOGGER.warn("Configuration - file not found: {}", e.getMessage());
		} catch (Exception e) {
			LOGGER.warn("Configuration", e);
		}
	}

	/**
	 * @return String
	 */
	public String getUrlToken() {
		return StringUtils.defaultIfBlank(urltoken, properties.getProperty("goffice.common.security.urltoken"));
	}

	/**
	 * @return String
	 */
	public String getGrantType() {
		return StringUtils.defaultIfBlank(granttype, properties.getProperty("goffice.common.security.granttype"));
	}

	/**
	 * @return String
	 */
	public String getClientId() {
		return StringUtils.defaultIfBlank(clientid, properties.getProperty("goffice.common.security.clientid"));
	}

	/**
	 * @return String
	 */
	public String getPassword() {
		return StringUtils.defaultIfBlank(password, properties.getProperty("goffice.common.security.password"));
	}

	/**
	 * @return String
	 */
	public String getUsername() {
		return StringUtils.defaultIfBlank(username, properties.getProperty("goffice.common.security.username"));
	}

	/**
	 * @return String
	 */
	public String createToken() {
		return StringUtils.defaultIfBlank(createtoken, properties.getProperty("goffice.common.security.urlkeycloak"));
	}

	/**
	 * @return String
	 */
	public String getClientSecret() {
		return StringUtils.defaultIfBlank(clientSecret, properties.getProperty("goffice.common.security.clientsecret"));
	}

	public String getUrlKeycloak() {
		return StringUtils.defaultIfBlank(urlKeycloak, properties.getProperty("goffice.common.security.clientsecret"));
	}

	public String getRealm() {
		return StringUtils.defaultIfBlank(realm, properties.getProperty("goffice.common.security.realm"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}