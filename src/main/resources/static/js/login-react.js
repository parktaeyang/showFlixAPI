// login-react.js - 레거시 login.js 로직을 React 방식으로 변환

const { useState } = React;

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

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
      const payload = new URLSearchParams();
      payload.append('username', username.trim());
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
        React.createElement('p', { id: 'error-msg', className: 'error-msg' }, error),
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

