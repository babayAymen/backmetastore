package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Comment;

public interface CommentRepository extends BaseRepository<Comment, Long>{

	List<Comment> findAllByArticleId(Long articleId);

}
