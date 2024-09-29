package com.example.meta.store.werehouse.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Services.ArticleService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WereHouseStarter implements CommandLineRunner{

	@Autowired ArticleService articleService;

	@Override
	public void run(String... args) throws Exception {
		Boolean exists = articleService.existsOne();
		log.warn("exists in werehouse starter "+exists);
		if(!exists) {
			articleService.addDairyArticles();
			articleService.addButcherArticles();
			articleService.addVegitableArticles();
		}
		
	}

}
