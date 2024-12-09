package com.example.meta.store.werehouse.Repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.meta.store.Base.Repository.BaseRepository;
import com.example.meta.store.werehouse.Entities.CommandLine;

public interface CommandLineRepository extends BaseRepository<CommandLine, Long> {


	/////////////////////////////////////////////////////// real work ///////////////////////////////////////////////////
	void deleteAllByInvoiceId(Long id);

	Page<CommandLine> findAllByInvoiceId(Long invoiceId, Pageable pageable);

	@Query("SELECT c FROM CommandLine c WHERE c.invoice.id = :id")
	List<CommandLine> findAllByInvoice(Long id);


	

}
