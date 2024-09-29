package com.example.meta.store.werehouse.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.Like;

public interface LikeRepository extends BaseRepository<Like, Long> {

	Optional<Like> findByArticleId(Long articleId) ;

	@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l JOIN l.companies c JOIN l.article a WHERE a.id = :articleId AND c.id = :companyId")
	Boolean existsByArticleIdAndCompanyId(Long articleId, Long companyId);

	@Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l JOIN l.users u JOIN l.article a WHERE a.id = :articleId AND u.id = :userId")
	Boolean existsByArticleIdAndUserId(Long articleId, Long userId);

}
