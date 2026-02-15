package com.schedulemng.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePhoneNumberDTO {

	@NotBlank(message = "연락처를 입력해주세요.")
	@Pattern(regexp = "^01(?:0|1|[6-9])-\\d{3,4}-\\d{4}$", message = "올바른 연락처 형식(010-1234-5678)이 아닙니다.")
	private String phoneNumber;

}
