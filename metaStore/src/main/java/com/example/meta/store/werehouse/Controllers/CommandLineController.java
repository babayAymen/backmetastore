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
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.store.werehouse.Dtos.CommandLineDto;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;
import com.example.meta.store.werehouse.Enums.InvoiceMode;
import com.example.meta.store.werehouse.Services.CommandLineService;
import com.example.meta.store.werehouse.Services.CompanyService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/werehouse/commandline/")
@RequiredArgsConstructor
@Validated
public class CommandLineController {


	private final CommandLineService commandLineService;
		
	private final CompanyService companyService;
	

	private final Logger logger = LoggerFactory.getLogger(CommandLineController.class);

	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	@PostMapping("{type}/{invoicecode}/{clientid}/{discount}/{clientType}/{invoiceMode}")
	public ResponseEntity<InputStreamResource> addCommandLine(@RequestBody  List<CommandLineDto> commandLinesDto,
			@PathVariable Long invoicecode, @PathVariable String type, @PathVariable Long clientid, @PathVariable Double discount,
			@PathVariable AccountType clientType,@PathVariable InvoiceMode invoiceMode)
					throws JsonProcessingException {
		Company company = companyService.getCompany();
		return commandLineService.insertLine(commandLinesDto, company,clientid,discount,type,clientType,invoiceMode);
		
	}
	
	@GetMapping("getcommandline/{invoiceId}")
	public List<CommandLineDto> getCommandLines(@PathVariable Long invoiceId){
		logger.warn("getcommand line by invoice id"+invoiceId);
		return commandLineService.getCommandLines(invoiceId);
	}
	


}
