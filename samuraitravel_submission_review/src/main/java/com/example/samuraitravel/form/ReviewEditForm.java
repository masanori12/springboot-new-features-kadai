package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewEditForm {
	
	private Integer houseId; 
	
	@NotNull(message = "評価を選択してください。")
	private Integer rating;
	
	@NotBlank(message = "コメントを入力してください。")
	private String comment;

}
