package com.schedulemng.controller.api;

import com.schedulemng.dto.user.UpdatePasswordRequestDTO;
import com.schedulemng.dto.user.UpdatePhoneNumberDTO;
import com.schedulemng.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserApiController {

	private final UserService userService;

	/**
	 * 핸드폰 번호 업데이트
	 * @param updatePhoneNumberDTO
	 */
	@RequestMapping("/updatePhoneNumber")
	public ResponseEntity<?> updatePhoneNumber(@Valid @RequestBody UpdatePhoneNumberDTO updatePhoneNumberDTO, BindingResult result) {
		userService.updatePhoneNumber(updatePhoneNumberDTO.getPhoneNumber());
		return ResponseEntity.ok(Map.of("message", "연락처가 변경되었습니다.\n변경된 정보는 다음 로그인 시 반영됩니다."));
	}

	/**
	 * 패스워드 업데이트
	 */
	@RequestMapping("/updatePassword")
	public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequestDTO updatePasswordRequestDTO, BindingResult result) {
		userService.updatePassword(updatePasswordRequestDTO.getCurrentPassword(), updatePasswordRequestDTO.getNewPassword(), updatePasswordRequestDTO.getConfirmNewPassword());
		return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
	}

}
