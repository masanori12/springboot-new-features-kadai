package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
	private final HouseRepository houseRepository;
	
	public ReviewController(ReviewRepository reviewRepository, ReviewService reviewService, HouseRepository houseRepository) {
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
		this.houseRepository = houseRepository;
	}
	
	// 投稿フォームの表示
	@GetMapping("/register")
	public String register(@RequestParam("houseId")Integer houseId, Model model) {
		// フォームを準備
		ReviewRegisterForm form = new ReviewRegisterForm();
		form.setHouseId(houseId);
		model.addAttribute("reviewRegisterForm", form);
		
		// houseを取得してmodelに渡す
		House house = houseRepository.findById(houseId).orElseThrow();
		model.addAttribute("house", house);
		
		return "reviews/register";
	}
	
	// 投稿データの保存処理
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		if (bindingResult.hasErrors()) {
			return "reviews/register";
		}
		
		// ログインしていない場合は投稿できないようにする
		if (userDetails == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "ログインが必要です。");
			return "redirect:/login";
		}
		
		User user = userDetails.getUser();
		
		reviewService.create(reviewRegisterForm, user);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを登録しました。");
		
		return "redirect:/houses/" + reviewRegisterForm.getHouseId();
	}
	
	// レビュー一覧表示
		@GetMapping("/house/{houseId}")
		public String index(
			@PathVariable("houseId") Integer houseId, 
			@RequestParam(name = "page", defaultValue = "0") int page,
			Model model
		) {
			Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
			Page<Review> reviewPage = reviewRepository.findByHouseId(houseId, pageable);
					
				// 民宿の情報を取得して渡す
			House house = houseRepository.findById(houseId).orElseThrow();
			model.addAttribute("house", house);
			
			model.addAttribute("reviewPage", reviewPage);
			model.addAttribute("houseId", houseId);
			return "reviews/index";
		}
		
		@GetMapping("/edit/{id}")
		public String edit(@PathVariable("id") Integer id, Model model, @AuthenticationPrincipal UserDetailsImpl userDetails, RedirectAttributes redirectAttributes) {
			// 指定されたIDのレビューを取得（なければ例外）
			Review review = reviewRepository.findById(id).orElseThrow();
			
			// ログインユーザーが投稿者出なければ民宿詳細へリダイレクト
			if (!review.getUser().getId().equals(userDetails.getUser().getId())) {
				redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
				return "redirect:/houses/" + review.getHouse().getId();
				}
			
			// フォームにデータを詰め替え
			ReviewEditForm form = new ReviewEditForm();
			form.setRating(review.getRating());
			form.setComment(review.getComment());
			form.setHouseId(review.getHouse().getId());
			
			// モデルにフォームとレビューの情報を追加してビューに渡す
			model.addAttribute("reviewEditForm", form);
			model.addAttribute("review", review);
			
			// 編集ページ(reviews/edit.html)を表示
			return "reviews/edit";
		}
		
		@PostMapping("/update/{id}")
		public String update(
				@PathVariable("id") Integer id,
				@ModelAttribute @Validated ReviewEditForm reviewEditForm,
				BindingResult bindingResult,
				RedirectAttributes redirectAttributes,
				@AuthenticationPrincipal UserDetailsImpl userDetails,
				Model model
				) {
			if (bindingResult.hasErrors()) {
				Review review = reviewRepository.findById(id).orElseThrow();
				model.addAttribute("review", review);
				return "reviews/edit";
			}
			
			// ログインしてない場合
			if (userDetails == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "ログインが必要です。");
				return "redirect:/login";
			}
			
			User user = userDetails.getUser();
			
			Review review = reviewRepository.findById(id).orElseThrow();

		    // ★ログインユーザーが投稿者でなければ民宿詳細へリダイレクト
		    if (!review.getUser().getId().equals(userDetails.getUser().getId())) {
		        redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
		        return "redirect:/houses/" + reviewEditForm.getHouseId();
		    }
			
			reviewService.update(id, reviewEditForm, user);
			redirectAttributes.addFlashAttribute("successMessage", "レビューを更新しました。");
			
			// 元の民宿詳細ページに戻る
			return "redirect:/houses/" + reviewEditForm.getHouseId();
		}
		
		@PostMapping("/delete/{id}")
		public String delete(
			@PathVariable("id") Integer id,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes
		) {
			// ログインしてない場合はログイン画面に飛ばす
		    if (userDetails == null) {
		        redirectAttributes.addFlashAttribute("errorMessage", "ログインが必要です。");
		        return "redirect:/login";
		    }
		    
			// レビュー取得(存在しなければ404エラー)
			Review review = reviewRepository.findById(id).orElseThrow();
			
			// レビューがログインユーザーのものじゃない場合は削除させない
			if (review.getUser() == null || !review.getUser().getEmail().equals(userDetails.getUser().getEmail())) {
				redirectAttributes.addFlashAttribute("errorMessage", "レビューの削除権限がありません。");
				return "redirect:/houses/" + review.getHouse().getId();
			}
			
			// レビュー削除
			reviewRepository.delete(review);
			redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
			
			// 民宿詳細ページにリダイレクト
			return "redirect:/houses/" + review.getHouse().getId();
		}

}



