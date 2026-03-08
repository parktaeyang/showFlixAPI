package com.showflix.api.auth.application;

import com.showflix.api.auth.domain.AccountType;
import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application Layer - 관리자 계정관리 서비스
 */
@Service
public class AdminUserService {

    private static final String DEFAULT_PASSWORD = "showflix";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 신규 계정 생성
     * - 비밀번호: "showflix" 고정
     * - is_admin: accountType이 ADMIN이면 true, 나머지는 false
     */
    @Transactional
    public void createUser(String userid, String username, String accountType, String role) {
        if (userid == null || userid.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (accountType == null || accountType.isBlank()) {
            throw new IllegalArgumentException("계정유형을 선택해주세요.");
        }

        // 유효한 계정유형인지 검증
        AccountType.valueOf(accountType);

        // 중복 아이디 체크
        userRepository.findByUserid(userid).ifPresent(u -> {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + userid);
        });

        User user = new User();
        user.setUserid(userid);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setAdmin("ADMIN".equals(accountType));
        user.setAccountType(accountType);
        user.setRole((role == null || role.isBlank()) ? null : role);

        userRepository.save(user);
    }

    /**
     * 계정유형별 다음 userid 자동생성
     * 접두사: ACTOR=A, STAFF=S, CAPTAIN=C, ADMIN=W
     * 형식: A0001, A0002 ...
     */
    @Transactional(readOnly = true)
    public String getNextUserId(String accountType) {
        AccountType.valueOf(accountType); // 유효성 검증

        String prefix = getPrefixByAccountType(accountType);
        return userRepository.findLastUseridByAccountType(accountType)
                .map(lastUserid -> {
                    String numberPart = lastUserid.substring(1);
                    int nextNumber = Integer.parseInt(numberPart) + 1;
                    return prefix + String.format("%04d", nextNumber);
                })
                .orElse(prefix + "0001");
    }

    /**
     * 계정유형별 선택 가능한 역할 목록 반환
     * ACTOR → 남1~3, 여1~3
     * STAFF → 도어, 홀맨, 오퍼, 헬퍼, 주방
     * CAPTAIN, ADMIN → 역할 없음 (빈 리스트)
     */
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAvailableRoles(String accountType) {
        AccountType.valueOf(accountType); // 유효성 검증

        List<String[]> roles = switch (accountType) {
            case "STAFF"   -> List.of(
                    new String[]{"DOOR",    "도어"},
                    new String[]{"HOLEMAN", "홀맨"},
                    new String[]{"OPER",    "오퍼"},
                    new String[]{"HELPER",  "헬퍼"},
                    new String[]{"KITCHEN", "주방"}
            );
            case "ACTOR"   -> List.of(
                    new String[]{"MALE1",   "남1"},
                    new String[]{"MALE2",   "남2"},
                    new String[]{"MALE3",   "남3"},
                    new String[]{"FEMALE1", "여1"},
                    new String[]{"FEMALE2", "여2"},
                    new String[]{"FEMALE3", "여3"}
            );
            default -> List.of(); // CAPTAIN, ADMIN은 역할 없음
        };

        return roles.stream()
                .map(r -> {
                    Map<String, String> m = new LinkedHashMap<>();
                    m.put("name", r[0]);
                    m.put("displayName", r[1]);
                    return m;
                })
                .toList();
    }

    @Transactional
    public void updateUser(String userid, String username, boolean admin) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        user.setUsername(username);
        user.setAdmin(admin);

        userRepository.update(user);
    }

    @Transactional
    public void changePassword(String userid, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        userRepository.updatePassword(userid, passwordEncoder.encode(rawPassword));
    }

    @Transactional
    public void deleteUser(String userid, String currentUserid) {
        if (userid.equals(currentUserid)) {
            throw new IllegalArgumentException("본인 계정은 삭제할 수 없습니다.");
        }

        userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        userRepository.delete(userid);
    }

    private String getPrefixByAccountType(String accountType) {
        return switch (accountType) {
            case "ACTOR"   -> "A";
            case "STAFF"   -> "S";
            case "CAPTAIN" -> "C";
            case "ADMIN"   -> "W";
            default -> throw new IllegalArgumentException("잘못된 계정유형: " + accountType);
        };
    }
}
