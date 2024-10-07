package com.example.meta.store.werehouse.Services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.meta.store.Base.ErrorHandler.RecordNotFoundException;
import com.example.meta.store.Base.Security.Entity.User;
import com.example.meta.store.Base.Service.BaseService;
import com.example.meta.store.werehouse.Controllers.ArticleController;
import com.example.meta.store.werehouse.Dtos.CommentDto;
import com.example.meta.store.werehouse.Entities.Article;
import com.example.meta.store.werehouse.Entities.ArticleCompany;
import com.example.meta.store.werehouse.Entities.Comment;
import com.example.meta.store.werehouse.Entities.Company;
import com.example.meta.store.werehouse.Mappers.CommentMapper;
import com.example.meta.store.werehouse.Repositories.CommentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServcie extends BaseService<Comment, Long> {

	private final CommentRepository commentRepository;
	
	private final CommentMapper commentMapper;
	
	private final InvoiceService invoiceService;
	
	private final EnableToCommentService enableToCommentService;

	private final Logger logger = LoggerFactory.getLogger(CommentServcie.class);
	
	public void addComment(ArticleCompany article , String comment , User user, Company company) {
		Comment commentaire = new Comment();
		commentaire.setArticle(article);
		commentaire.setContent(comment);
		commentaire.setCompany(company);
		commentaire.setUser(user);
		commentRepository.save(commentaire);
		if(user == null) {
			enableToCommentService.makeDisableToCommentArticle(article.getCompany().getId(), company.getId(), null);			
		}
		if(company == null) {
			enableToCommentService.makeDisableToCommentArticle(article.getCompany().getId(), null, user.getId());			
		}
	}

	public List<CommentDto> getAllCommentsByArticleId(Long articleId) {
		List<Comment> comments = commentRepository.findAllByArticleId(articleId);
		if(comments.isEmpty()) {
			throw new RecordNotFoundException("there is no comment yet");
		}
		List<CommentDto> commentsDto = new ArrayList<>();
		for(Comment i : comments) {
			CommentDto commentDto = commentMapper.mapToDto(i);
			commentsDto.add(commentDto);
		}
		return commentsDto;
	}
}
