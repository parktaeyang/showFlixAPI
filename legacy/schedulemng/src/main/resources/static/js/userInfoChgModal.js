document.addEventListener('DOMContentLoaded', () => {
    const userInfoModalEl = document.getElementById('userInfoChgModal');
    if (!userInfoModalEl) return;

    const userInfoModal = new bootstrap.Modal(userInfoModalEl);

    // 모달이 열릴 때 폼과 에러 메시지를 초기화
    // 이후 thymeleaf로 서버에서 데이터 렌더링
    userInfoModalEl.addEventListener('show.bs.modal', () => {
        document.querySelectorAll('form').forEach(form => form.reset());
        document.querySelectorAll('.alert').forEach(alert => alert.classList.add('d-none'));
    });

    // --- 연락처 변경 저장 로직 ---
    const saveContactBtn = document.getElementById('saveContactBtn');
    saveContactBtn.addEventListener('click', () => {
        const phoneNumber = document.getElementById('phoneNumber').value;

        if (!phoneNumber) {
            showError('contactError', '연락처를 입력해주세요.');
            return;
        }

        const payload = { phoneNumber };

        axios.put('/api/user/updatePhoneNumber', payload)
        .then(response => {
            alert(response?.data?.message || '연락처가 변경되었습니다.\n변경된 정보는 다음 로그인 시 반영됩니다.');
            userInfoModal.hide();
        })
        .catch(error => {
            // GlobalExceptionHandler가 반환하는 일관된 'message' 키를 사용합니다.
            const errorMessage = error.response?.data?.message || '서버 오류가 발생했습니다.';
            showError('contactError', errorMessage);
        });
    });


    // --- 비밀번호 변경 로직 ---
    const savePasswordBtn = document.getElementById('savePasswordBtn');
    savePasswordBtn.addEventListener('click', () => {
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmNewPassword = document.getElementById('confirmPassword').value;

        // 유효성 검사
        if (!currentPassword || !newPassword || !confirmNewPassword) {
            showError('passwordError', '모든 영역을 입력해주세요.');
            return;
        }
        if (newPassword !== confirmNewPassword) {
            showError('passwordError', '새 비밀번호가 일치하지 않습니다.');
            return;
        }

        // 서버에 전송할 데이터
        const payload = { currentPassword, newPassword, confirmNewPassword };

        axios.put('/api/user/updatePassword', payload, {
            headers: { 'Content-Type': 'application/json' }
        })
        .then(response => {
            alert(response?.data?.message || '비밀번호가 변경되었습니다.');
            userInfoModal.hide();
        })
        .catch(error => {
            const errorMessage = error.response?.data?.message || '서버 오류가 발생했습니다.';
            showError('passwordError', errorMessage);
        });
    });

    /**
     * 특정 에러 영역에 메시지를 표시하는 함수
     * @param {string} errorDivId - 에러를 표시할 div의 ID
     * @param {string} message - 표시할 에러 메시지
     */
    function showError(errorDivId, message) {
        const errorDiv = document.getElementById(errorDivId);
        if (!errorDiv) return;
        errorDiv.textContent = message;
        errorDiv.classList.remove('d-none');
    }
});