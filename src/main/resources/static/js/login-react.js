// login-react.js - 레거시 login.js 로직을 React 방식으로 변환

const { useState, useEffect } = React;

const SAVED_USERNAME_KEY = 'showflix_saved_username';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [saveId, setSaveId] = useState(false);

  // 이미 로그인된 사용자는 달력 페이지로 자동 이동
  useEffect(() => {
    // redirect: 'manual' → 서버가 302 리다이렉트를 보내도 자동으로 따라가지 않음
    // (미인증 시 /index.html로 리다이렉트되어 res.ok=true가 되는 무한루프 방지)
    fetch('/api/user/info', { credentials: 'same-origin', redirect: 'manual' })
      .then(res => {
        // res.type === 'opaqueredirect': 서버가 302 리다이렉트 → 미로그인 상태
        // res.ok (200): 정상 인증된 상태
        if (res.ok && res.type !== 'opaqueredirect') {
          window.location.href = '/schedule/calendar';
        }
        // 그 외 (401, 403, opaqueredirect 등) → 로그인 페이지 그대로 표시
      })
      .catch(() => {
        // 네트워크 오류 → 로그인 페이지 표시
      });
  }, []);

  // 페이지 로드 시 저장된 아이디 복원
  useEffect(() => {
    try {
      const saved = localStorage.getItem(SAVED_USERNAME_KEY);
      if (saved) {
        setUsername(saved);
        setSaveId(true);
      }
    } catch (e) {
      console.warn('저장된 아이디를 불러올 수 없습니다.');
    }
  }, []);

  const isEmpty = (param) => param == null || param.trim() === '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (isEmpty(username)) {
      setError('아이디를 입력해주세요.');
      return;
    }
    if (isEmpty(password)) {
      setError('비밀번호를 입력해주세요.');
      return;
    }

    try {
      const trimmedUsername = username.trim();

      // 아이디 저장 처리
      try {
        if (saveId && trimmedUsername) {
          localStorage.setItem(SAVED_USERNAME_KEY, trimmedUsername);
        } else {
          localStorage.removeItem(SAVED_USERNAME_KEY);
        }
      } catch (storageErr) {
        console.warn('아이디 저장에 실패했습니다.');
      }

      const payload = new URLSearchParams();
      payload.append('username', trimmedUsername);
      payload.append('password', password.trim());

      const res = await axios.post('/auth/login', payload, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });
      
      // 응답 데이터 확인
      console.log('Login response:', res.data);
      
      const redirectUrl = res.data?.redirect || '/schedule/calendar';
      window.location.href = redirectUrl;
    } catch (err) {
      console.error('Login error:', err);
      const errorMessage =
        err?.response?.data?.errorMessage ||
        '예상치 못한 오류가 발생하였습니다.\n관리자에게 문의해주세요.';
      setError(errorMessage);
    }
  };

  return (
    React.createElement('div', { className: 'login-container' },
      React.createElement('div', { className: 'login-logo' },
        React.createElement('img', { src: '/images/showflix-logo.png', alt: 'SHOWFLIX' })
      ),
      React.createElement('h1', null, '로그인'),
      React.createElement('form', { id: 'login-form', onSubmit: handleSubmit },
        React.createElement('div', { className: 'form-group' },
          React.createElement('label', { htmlFor: 'username' }, '아이디'),
          React.createElement('input', {
            type: 'text',
            id: 'username',
            name: 'username',
            placeholder: '아이디를 입력하세요',
            value: username,
            onChange: (e) => setUsername(e.target.value),
          }),
        ),
        React.createElement('div', {
            className: 'form-group',
            style: { marginBottom: '0.5rem' },
          },
          React.createElement('label', { htmlFor: 'password' }, '비밀번호'),
          React.createElement('input', {
            type: 'password',
            id: 'password',
            name: 'password',
            placeholder: '비밀번호를 입력하세요',
            value: password,
            onChange: (e) => setPassword(e.target.value),
          }),
        ),
        error ? React.createElement('p', { id: 'error-msg', className: 'error-msg' }, error) : null,
        React.createElement('div', { className: 'form-group save-id-wrap' },
          React.createElement('label', { className: 'save-id-label' },
            React.createElement('input', {
              type: 'checkbox',
              checked: saveId,
              onChange: (e) => setSaveId(e.target.checked),
            }),
            ' 아이디 저장'
          )
        ),
        React.createElement('button', { type: 'submit', className: 'login-button' }, '로그인'),
      ),
      React.createElement(
        'p',
        { className: 'help-text' },
        '아이디 또는 비밀번호를 잊으신 경우, 관리자에게 문의해주세요.',
      ),
    )
  );
}

document.addEventListener('DOMContentLoaded', () => {
  const root = document.getElementById('root');
  if (root) {
    const rootElement = ReactDOM.createRoot
      ? ReactDOM.createRoot(root)
      : { render: (el) => ReactDOM.render(el, root) };

    rootElement.render(
      React.createElement(React.StrictMode || React.Fragment, null,
        React.createElement(LoginPage, null),
      ),
    );
  }
});

