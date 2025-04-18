package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;
	
	public ReviewService(ReviewRepository reviewRepository, HouseRepository houseRepository) {
		this.reviewRepository = reviewRepository;
		this.houseRepository = houseRepository;
	}
	
	@Transactional
	public void create(ReviewRegisterForm reviewRegisterForm, User user) {
		Review review = new Review();
		
		review.setRating(reviewRegisterForm.getRating());
		review.setComment(reviewRegisterForm.getComment());
		review.setUser(user);
		
		House house = houseRepository.findById(reviewRegisterForm.getHouseId()).orElseThrow();
		review.setHouse(house);
		
		reviewRepository.save(review);
	}
	
	@Transactional
	public void update(Integer id, ReviewEditForm form, User user) {
		// レビューの取得(存在しなければ例外)
		Review review = reviewRepository.findById(id).orElseThrow();
		
		// 編集しようとしているユーザーが投稿者本人かチェック(セキュリティのため)
		if (!review.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("このレビューを編集する権限がありません。");
		}
		
		// フォームの内容でレビューを更新
		review.setRating(form.getRating());
		review.setComment(form.getComment());
		
		// 保存(JPAが自動的にUPDATEしてくれる)
		reviewRepository.save(review);
	}
	

}
