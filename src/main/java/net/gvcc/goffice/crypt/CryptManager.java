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

package net.gvcc.goffice.crypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;

/**
 *
 * <p>
 * The <code>AESManager</code> class
 * </p>
 * <p>
 * Data: 29 apr 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
@Component
@Setter
public class CryptManager {
	private static final String PBE_WITH_MD5_AND_TRIPLE_DES = "PBEWithMD5AndTripleDES";

	@Value("${enc.password}")
	private String password;

	public String decryptJasypt(String text) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(password);
		encryptor.setAlgorithm(PBE_WITH_MD5_AND_TRIPLE_DES);
		return encryptor.decrypt(text);
	}

	public String encryptJasypt(String text) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(password);
		encryptor.setAlgorithm(PBE_WITH_MD5_AND_TRIPLE_DES);
		return encryptor.encrypt(text);
	}

}
