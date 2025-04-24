package com.example.samuraitravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.samuraitravel.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer>{
	// houseIdで絞り込み + ページネーションで対応
	@EntityGraph(attributePaths = "user")
	Page<Review> findByHouseId(Integer houseId, Pageable pageable);
	
	boolean existsByUserIdAndHouseId(Integer userId, Integer houseId);
	
	long countByHouseId(Integer houseId);
	

}