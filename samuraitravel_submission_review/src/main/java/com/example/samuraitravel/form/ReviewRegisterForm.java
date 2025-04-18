package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRegisterForm {
	
	
	private Integer houseId;
	
	private Integer userId;
	
	@NotNull(message = "評価を選択してください。")
	private Integer rating;
	
	@NotBlank(message = "コメントを入力してください。")
	private String comment;
	
	

}
