// 계정 관리 전용 스크립트
document.addEventListener('DOMContentLoaded', function() {
    loadUsersList();
    setupAccountForm();
    setupMobileOptimization();
});

// 모바일 환경 최적화 설정
function setupMobileOptimization() {
    // 모바일 환경 감지
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    
    if (isMobile || isTouchDevice) {
        // 모바일 환경에서 테이블 스크롤 최적화
        const tableResponsive = document.querySelector('.table-responsive');
        if (tableResponsive) {
            tableResponsive.style.webkitOverflowScrolling = 'touch';
        }
        
        // 모바일에서 폼 요소 터치 최적화
        const formElements = document.querySelectorAll('input, select, button');
        formElements.forEach(element => {
            element.style.minHeight = '44px';
        });
        
        // 모바일에서 카드 간격 조정
        const cards = document.querySelectorAll('.card');
        cards.forEach(card => {
            card.style.marginBottom = '1rem';
        });
        
        console.log('모바일 환경 최적화 적용됨');
    }
    
    // 화면 크기 변경 감지
    window.addEventListener('resize', handleResize);
    handleResize(); // 초기 실행
}

// 화면 크기 변경 처리
function handleResize() {
    const width = window.innerWidth;
    
    if (width <= 576) {
        // 모바일 환경
        document.body.classList.add('mobile-view');
        document.body.classList.remove('tablet-view', 'desktop-view');
    } else if (width <= 991) {
        // 태블릿 환경
        document.body.classList.add('tablet-view');
        document.body.classList.remove('mobile-view', 'desktop-view');
    } else {
        // 데스크톱 환경
        document.body.classList.add('desktop-view');
        document.body.classList.remove('mobile-view', 'tablet-view');
    }
}

// 폼 설정
function setupAccountForm() {
    // 계정유형 select에 이벤트 리스너 추가
    const accountTypeSelect = document.getElementById('accountType');
    if (accountTypeSelect) {
        accountTypeSelect.addEventListener('change', function() {
            generateUserId();
            loadAvailableRoles();
        });
    }
    
    // 폼 제출 이벤트
    const form = document.getElementById('createAccountForm');
    if (form) {
        form.addEventListener('submit', handleAccountCreation);
    }
}

// 계정유형별 아이디 생성
function generateUserId() {
    const accountType = document.getElementById('accountType').value;
    const useridInput = document.getElementById('userid');
    
    console.log('generateUserId 호출됨, accountType:', accountType);
    
    if (!accountType) {
        useridInput.value = '';
        return;
    }

    // 계정유형별 접두사
    const prefixes = {
        'ACTOR': 'A',
        'STAFF': 'S', 
        'CAPTAIN': 'C',
        'ADMIN': 'W'
    };

    const prefix = prefixes[accountType];
    console.log('prefix:', prefix);
    
    if (prefix) {
        // 서버에서 다음 번호를 가져와서 설정
        getNextUserId(accountType, prefix);
    }
}

// 사용 가능한 역할 목록 로드
async function loadAvailableRoles() {
    const accountType = document.getElementById('accountType').value;
    const roleSelect = document.getElementById('role');
    
    if (!accountType) {
        roleSelect.innerHTML = '<option value="">역할 선택</option>';
        return;
    }
    
    try {
        const response = await axios.get('/api/admin/available-roles', {
            params: { accountType }
        });
        
        if (response.data.success) {
            const roles = response.data.data.roles;
            roleSelect.innerHTML = '<option value="">역할 선택</option>';
            
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.name;
                option.textContent = role.displayName;
                roleSelect.appendChild(option);
            });
            
            // 캡틴이나 관리자는 역할이 없으므로 역할 필드를 비활성화
            if (accountType === 'CAPTAIN' || accountType === 'ADMIN') {
                roleSelect.disabled = true;
                roleSelect.value = '';
            } else {
                roleSelect.disabled = false;
            }
        }
    } catch (error) {
        console.error('사용 가능한 역할 조회 실패:', error);
        roleSelect.innerHTML = '<option value="">역할 선택</option>';
    }
}

// 서버에서 다음 아이디 번호 가져오기
async function getNextUserId(accountType, prefix) {
    console.log('getNextUserId 호출됨, accountType:', accountType, 'prefix:', prefix);
    
    try {
        const response = await axios.get('/api/admin/next-userid', {
            params: { accountType }
        });
        
        console.log('API 응답:', response.data);
        
        if (response.data.success) {
            document.getElementById('userid').value = response.data.data.nextUserId;
            console.log('userid 설정됨:', response.data.data.nextUserId);
        }
    } catch (error) {
        console.error('다음 아이디 조회 실패:', error);
        showAlert('다음 아이디를 가져오는 중 오류가 발생했습니다.', 'error');
    }
}

// 계정 생성 처리
async function handleAccountCreation(event) {
    event.preventDefault();
    
    // 핸드폰번호 유효성 검사
    const phoneNumber = document.getElementById('phoneNumber').value;
    if (!phoneNumber.match(/^[0-9]{10,13}$/)) {
        showAlert('핸드폰번호는 10-13자리의 숫자만 입력 가능합니다.', 'error');
        return;
    }
    
    // 이름 유효성 검사
    const username = document.getElementById('username').value;
    if (!username.match(/^[가-힣a-zA-Z\s]{1,20}$/)) {
        showAlert('이름은 한글, 영문, 공백만 입력 가능하며 최대 20자까지 입력할 수 있습니다.', 'error');
        return;
    }
    
    const formData = new FormData(event.target);
    const accountData = {
        accountType: formData.get('accountType'),
        userid: formData.get('userid'),
        username: formData.get('username'),
        phoneNumber: formData.get('phoneNumber'),
        password: formData.get('password'),
        role: formData.get('role')
    };
    
    try {
        const response = await axios.post('/api/admin/create-account', accountData);
        
        if (response.data.success) {
            showAlert('계정이 성공적으로 생성되었습니다.', 'success');
            event.target.reset();
            document.getElementById('userid').value = '';
            document.getElementById('role').innerHTML = '<option value="">역할 선택</option>';
            document.getElementById('role').disabled = true;
            loadUsersList(); // 사용자 목록 새로고침
        }
    } catch (error) {
        console.error('계정 생성 실패:', error);
        const errorMessage = error.response?.data?.message || '계정 생성 중 오류가 발생했습니다.';
        showAlert(errorMessage, 'error');
    }
}

// 사용자 목록 로드
async function loadUsersList() {
    try {
        const response = await axios.get('/api/admin/users');
        
        console.log('API 응답:', response.data);
        
        if (response.data.success) {
            const users = response.data.data.users; // data.data.users로 변경
            window.allUsers = users; // 전역 변수로 저장
            renderUsersTable(users);
        }
    } catch (error) {
        console.error('사용자 목록 로드 실패:', error);
        showAlert('사용자 목록을 불러오는 중 오류가 발생했습니다.', 'error');
    }
}

// 사용자 테이블 렌더링
function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    
    if (Array.isArray(users)) {
        tbody.innerHTML = users.map(user => {
            const accountTypeClass = getAccountTypeClass(user.accountType?.displayName || user.accountType);
            const accountTypeIcon = getAccountTypeIcon(user.accountType?.displayName || user.accountType);
            const roleDisplay = user.role?.displayName || user.role || '-';
            
            return `
                <tr class="${accountTypeClass}">
                    <td><span class="badge bg-secondary">${user.userid}</span></td>
                    <td>${user.username}</td>
                    <td>${user.phoneNumber || '-'}</td>
                    <td>
                        <span class="badge ${getAccountTypeBadgeClass(user.accountType?.displayName || user.accountType)}">
                            ${accountTypeIcon} ${user.accountType?.displayName || user.accountType || '-'}
                        </span>
                    </td>
                    <td>
                        ${roleDisplay !== '-' ? `<span class="badge bg-info">${roleDisplay}</span>` : '-'}
                    </td>
                    <td>${user.createdAt || '-'}</td>
                    <td>
                        <button class="btn btn-sm btn-warning" onclick="resetPassword('${user.userid}')" title="비밀번호를 1234로 초기화">
                            <i class="fas fa-key"></i> 초기화
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    } else {
        console.error('users가 배열이 아닙니다:', users);
        tbody.innerHTML = '<tr><td colspan="7">데이터를 불러올 수 없습니다.</td></tr>';
    }
}

// 계정유형별 CSS 클래스 반환
function getAccountTypeClass(accountType) {
    switch (accountType) {
        case '배우': return 'table-actor';
        case '스텝': return 'table-staff';
        case '캡틴': return 'table-captain';
        case '관리자': return 'table-admin';
        default: return '';
    }
}

// 계정유형별 아이콘 반환
function getAccountTypeIcon(accountType) {
    switch (accountType) {
        case '배우': return '🎭';
        case '스텝': return '👷';
        case '캡틴': return '👨‍✈️';
        case '관리자': return '👑';
        default: return '👤';
    }
}

// 계정유형별 배지 클래스 반환
function getAccountTypeBadgeClass(accountType) {
    switch (accountType) {
        case '배우': return 'bg-primary';
        case '스텝': return 'bg-success';
        case '캡틴': return 'bg-warning';
        case '관리자': return 'bg-danger';
        default: return 'bg-secondary';
    }
}

// 사용자 필터링
function filterUsers() {
    const filterValue = document.getElementById('filterAccountType').value;
    
    if (!window.allUsers) {
        return;
    }
    
    let filteredUsers;
    if (filterValue === '') {
        filteredUsers = window.allUsers;
    } else {
        filteredUsers = window.allUsers.filter(user => {
            const userAccountType = user.accountType?.displayName || user.accountType;
            return userAccountType === getAccountTypeDisplayName(filterValue);
        });
    }
    
    renderUsersTable(filteredUsers);
}

// 계정유형 코드를 표시명으로 변환
function getAccountTypeDisplayName(accountTypeCode) {
    switch (accountTypeCode) {
        case 'ACTOR': return '배우';
        case 'STAFF': return '스텝';
        case 'CAPTAIN': return '캡틴';
        case 'ADMIN': return '관리자';
        default: return accountTypeCode;
    }
}

// 필터 초기화
function clearFilter() {
    document.getElementById('filterAccountType').value = '';
    filterUsers();
}

// 비밀번호 초기화
async function resetPassword(userid) {
    if (!confirm(`사용자 "${userid}"의 비밀번호를 1234로 초기화하시겠습니까?`)) {
        return;
    }
    
    try {
        const response = await axios.post('/api/admin/reset-password', { userid });
        
        if (response.data.success) {
            showAlert('비밀번호가 성공적으로 초기화되었습니다. (초기 비밀번호: 1234)', 'success');
        }
    } catch (error) {
        console.error('비밀번호 초기화 실패:', error);
        const errorMessage = error.response?.data?.error || error.response?.data?.message || '비밀번호 초기화 중 오류가 발생했습니다.';
        showAlert(errorMessage, 'error');
    }
}

// 공통 알림 함수
function showAlert(message, type = 'info') {
    // 간단한 alert로 대체 (Bootstrap Modal이 없는 경우)
    if (type === 'error') {
        alert('오류: ' + message);
    } else if (type === 'success') {
        alert('성공: ' + message);
    } else {
        alert(message);
    }
} 