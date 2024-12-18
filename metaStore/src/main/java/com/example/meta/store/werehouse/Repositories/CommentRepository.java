package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Comment;

public interface CommentRepository extends BaseRepository<Comment, Long>{

	Page<Comment> findAllByArticleId(Long articleId, Pageable pageable);

}
