package com.example.meta.store.werehouse.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Config.JwtAuthenticationFilter;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.werehouse.Controllers.CompanyController;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Enums.AccountType;

import jakarta.servlet.ServletContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {


	private final ServletContext context;
	
	private final Logger logger = LoggerFactory.getLogger(ImageService.class);

	
	public String insertImag(MultipartFile file, Long userId,String service){
		boolean isExist = new File(context.getRealPath("/Images/"+service+"/")).exists();
		if(!isExist) {
			new File(context.getRealPath("/Images/"+service+"/")).mkdir();
		}
		
		String fileName = file.getOriginalFilename();
		String newFileName = FilenameUtils.getBaseName(fileName)+"."+FilenameUtils.getExtension(fileName);
		File serverFile = new File (context.getRealPath("/Images/"+service+"/"+File.separator+userId+File.separator+newFileName));
		try {
			FileUtils.writeByteArrayToFile(serverFile, file.getBytes());
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return newFileName;
	}

	public byte[] getImage( String logo, String service, Long id) throws IOException {
		logger.warn("image get");
		logger.warn(context.getRealPath(service));
		return Files.readAllBytes(Paths.get(context.getRealPath("/Images/"+service+"/"+id+"/"+logo)));
	}

	public void updateImage(AccountType accounttype, MultipartFile image, User user, Company company) {
		String service = "";
		logger.warn(image.getOriginalFilename()+" image name and account type is : "+accounttype);
		switch (accounttype) {
		case COMPANY: {
			service = "company";
			String filename = insertImag(image, user.getId(), service);
			company.setLogo(filename);
			break;
		}
		case USER : {
			service = "user";
			String filename = insertImag(image, user.getId(), service);
			user.setImage(filename);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + accounttype);
		}
		
	}
}
