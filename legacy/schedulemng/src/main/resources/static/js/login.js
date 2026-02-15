/**
 * <login.js>
 */

// CSRF 토큰 전역 설정
// const csrfToken = document.querySelector('meta[name="_csrf"]').content;
// const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
// axios.defaults.headers.common[csrfHeader] = csrfToken;

/**
 * 공백 체크
 */
function isEmpty(param) {
  return param == null || param.trim() === '';
}

/**
 * 로그인 폼 제출 핸들링
 */
document.querySelector('#login-form').addEventListener('submit', e => {
  e.preventDefault();

  const username = document.querySelector('#username').value.trim();
  const password = document.querySelector('#password').value.trim();
  const msg = document.querySelector('#error-msg');

  // 공백 검증
  if (isEmpty(username)) {
    document.querySelector('#username').focus();
    msg.textContent = "아이디를 입력해주세요.";
    return;
  }
  if (isEmpty(password)) {
    document.querySelector('#password').focus();
    msg.textContent = "비밀번호를 입력해주세요.";
    return;
  }

  // URL-encoded 폼 데이터로 변환 (spring security 때문에)
  const payload = new URLSearchParams();
  payload.append('username', username);
  payload.append('password', password);

  axios.post('/auth/login', payload)
  .then(res => {
    // 성공 시 JSON의 redirect 필드로 이동
    const redirectUrl = res.data.redirect || '/schedule/calendar';  // 기본 경로도 calendar 로
    window.location.href = redirectUrl;
  })
  .catch(err => {
    const errorMessage = err.response?.data?.errorMessage
        || '예상치 못한 오류가 발생하였습니다.\n관리자에게 문의해주세요.';
    msg.textContent = errorMessage;
  })

});