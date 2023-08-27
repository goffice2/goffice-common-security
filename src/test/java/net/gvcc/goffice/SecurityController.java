package net.gvcc.goffice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.gvcc.goffice.config.GofficeSecurityService;

@RestController
@RequestMapping(value = "/test")
public class SecurityController {

	@Autowired
	GofficeSecurityService service;

	@GetMapping(value = "/getlocale")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> getLocale() {

		return new ResponseEntity<>(service.getLocale(), HttpStatus.OK);
	}

}
