package net.gvcc.goffice;

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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * <p>
 * The <code>Application</code> class
 * </p>
 * <p>
 * Data: 28 mar 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
@SpringBootApplication
@PropertySource(value = "classpath:security.properties")

public class Application {

	/**
	 * @param args
	 *            no description
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
