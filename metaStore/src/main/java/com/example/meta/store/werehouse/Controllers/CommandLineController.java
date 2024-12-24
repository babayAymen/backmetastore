package com.example.meta.store.werehouse.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Security.Enums.RoleEnum;
import com.example.meta.store.Base.Security.Service.UserService;
import com.example.meta.store.werehouse.Dtos.CommandLineDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.InvoiceMode;
import com.example.meta.store.werehouse.Services.CommandLineService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.example.meta.store.werehouse.Services.WorkerService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/commandline/")
@RequiredArgsConstructor
@Validated
public class CommandLineController {


	private final CommandLineService commandLineService;
		
	private final CompanyService companyService;
	
	private final UserService userService;
	
	private final WorkerService workerService;

	private final Logger logger = LoggerFactory.getLogger(CommandLineController.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@PostMapping("{type}/{clientid}")
	public ResponseEntity<InputStreamResource> addCommandLine(@RequestBody  List<CommandLineDto> commandLinesDto,
			@RequestParam Long invoiceCode, @RequestParam String type, @PathVariable Long clientid, @RequestParam Double discount,
			@RequestParam AccountType clientType,@RequestParam InvoiceMode invoiceMode)
					throws JsonProcessingException {
		User user = userService.getUser();
		Company company = new Company();
		if(user.getRole() == RoleEnum.WORKER) {
			company = workerService.findCompanyByWorkerId(user.getId()).get();
		}else {
			company = companyService.getCompany();
		}
		return commandLineService.insertLine(commandLinesDto, company,clientid,discount,type,clientType,invoiceMode);
		
	}
	
	@GetMapping("get_command_line/{companyId}")
	public List<CommandLineDto> getCommandLines(@PathVariable Long companyId, @RequestParam Long invoiceId , @RequestParam int page , @RequestParam int pageSize){
	
		return commandLineService.getCommandLines(invoiceId, page, pageSize);
	}
	


}
