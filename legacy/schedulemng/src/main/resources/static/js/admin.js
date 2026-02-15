// 관리자 페이지 공통 기능
document.addEventListener('DOMContentLoaded', function() {
    console.log('admin.js 로드됨');
    
    // 현재 페이지에 따른 초기화
    const currentPath = window.location.pathname;

    if (currentPath === '/schedule/calendar') {
        // 캘린더 페이지에서만 관리자 권한 확인
        console.log('캘린더 페이지에서 관리자 권한 확인 시작');
    } else {
        // 관리자 페이지에서는 페이지별 초기화
        console.log('관리자 페이지에서 초기화 시작');
        initializeAdminPage();
    }
});

function initializeAdminPage() {
    // 현재 페이지에 따른 사이드바 활성화
    highlightCurrentPage();
    
    // 페이지별 초기화 함수 호출
    const currentPath = window.location.pathname;
    
    if (currentPath === '/admin') {
        loadDashboardStats();
    } else if (currentPath === '/admin/account') {
        loadUsersList();
        setupAccountForm();
    } else if (currentPath === '/admin/schedule-summary') {
        loadMonthlyData();
    }
}

function highlightCurrentPage() {
    const currentPath = window.location.pathname;
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');
    
    sidebarLinks.forEach(link => {
        link.classList.remove('active');
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}

// 대시보드 통계 로드
async function loadDashboardStats() {
    /*try {
        const [usersResponse, schedulesResponse, selectionsResponse] = await Promise.all([
            axios.get('/api/admin/users'),
            axios.get('/api/schedules'),
            axios.get('/api/admin/monthly-summary', {
                params: {
                    year: new Date().getFullYear(),
                    month: new Date().getMonth() + 1
                }
            })
        ]);

        if (usersResponse.data.success) {
            document.getElementById('totalUsers').textContent = usersResponse.data.users.length;
        }

        if (schedulesResponse.data) {
            document.getElementById('totalSchedules').textContent = schedulesResponse.data.length;
        }

        if (selectionsResponse.data.success) {
            const monthlyData = selectionsResponse.data.data;
            document.getElementById('thisMonthSelections').textContent = monthlyData.length;
        }
    } catch (error) {
        console.error('통계 로드 실패:', error);
        showAlert('통계를 불러오는 중 오류가 발생했습니다.', 'error');
    }*/
}

// 계정 관리 페이지 기능
function setupAccountForm() {
    const form = document.getElementById('createAccountForm');
    if (form) {
        form.addEventListener('submit', handleCreateAccount);
    }
}

async function handleCreateAccount(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const accountData = {
        userid: formData.get('userid'),
        username: formData.get('username'),
        phoneNumber: formData.get('phoneNumber'),
        accountType: formData.get('accountType')
    };

    // 필수 필드 검증
    if (!accountData.userid || !accountData.username || !accountData.phoneNumber || !accountData.accountType) {
        showAlert('모든 필수 항목을 입력해주세요.', 'error');
        return;
    }

    try {
        const response = await axios.post('/api/admin/create-account', accountData);
        
        if (response.data.success) {
            showAlert('계정이 성공적으로 생성되었습니다.', 'success');
            event.target.reset();
            document.getElementById('userid').value = ''; // 아이디 필드도 초기화
            loadUsersList(); // 사용자 목록 새로고침
        } else {
            showAlert(response.data.error || '계정 생성에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('계정 생성 실패:', error);
        const errorMessage = error.response?.data?.error || '계정 생성 중 오류가 발생했습니다.';
        showAlert(errorMessage, 'error');
    }
}

async function loadUsersList() {
    try {
        const response = await axios.get('/api/admin/users');
        
        if (response.data.success) {
            const users = response.data.data.users;
            const tbody = document.getElementById('usersTableBody');
            
            if (tbody && users) {
                tbody.innerHTML = users.map(user => `
                    <tr>
                        <td>${user.userid}</td>
                        <td>${user.username}</td>
                        <td>${user.phoneNumber || '-'}</td>
                        <td>
                            <span class="badge ${getAccountTypeBadgeClass(user.accountType.name)}">
                                ${user.accountType ? user.accountType.displayName : '-'}
                            </span>
                        </td>
                        <td>${formatCreatedAt(user.createdAt)}</td>
                    </tr>
                `).join('');
            }
        }
    } catch (error) {
        console.error('사용자 목록 로드 실패:', error);
        showAlert('사용자 목록을 불러오는 중 오류가 발생했습니다.', 'error');
    }
}

// 계정유형별 배지 클래스 반환
function getAccountTypeBadgeClass(accountType) {
    if (!accountType) return 'bg-secondary';
    
    switch (accountType) {
        case 'ACTOR': return 'bg-primary';
        case 'STAFF': return 'bg-success';
        case 'CAPTAIN': return 'bg-warning';
        case 'ADMIN': return 'bg-danger';
        default: return 'bg-secondary';
    }
}

// 스케줄 집계 페이지 기능
async function loadMonthlyData() {
    const year = document.getElementById('yearSelect')?.value || new Date().getFullYear();
    const month = document.getElementById('monthSelect')?.value || new Date().getMonth() + 1;
    
    try {
        const response = await axios.get('/api/admin/monthly-summary', {
            params: { year, month }
        });
        
        if (response.data.success) {
            const data = response.data.data;
            updateScheduleSummary(data);
            updateScheduleTable(data);
        }
    } catch (error) {
        console.error('월별 데이터 로드 실패:', error);
        showAlert('월별 데이터를 불러오는 중 오류가 발생했습니다.', 'error');
    }
}

function updateScheduleSummary(data) {
    // 통계 계산
    const uniqueUsers = new Set(data.map(item => item.userName)).size;
    const totalSelections = data.length;
    const openHopeCount = data.filter(item => item.openHope).length;
    const avgPerDay = data.length > 0 ? Math.round((data.length / 30) * 10) / 10 : 0;

    // 통계 업데이트
    document.getElementById('totalParticipants').textContent = uniqueUsers;
    document.getElementById('totalSelections').textContent = totalSelections;
    document.getElementById('openHopeCount').textContent = openHopeCount;
    document.getElementById('avgPerDay').textContent = avgPerDay;
}

function updateScheduleTable(data) {
    const tbody = document.getElementById('scheduleTableBody');
    
    if (tbody) {
        tbody.innerHTML = data.map(item => `
            <tr>
                <td>${formatDate(item.date)}</td>
                <td>${item.userName}</td>
                <td>
                    <span class="badge ${item.openHope ? 'bg-warning' : 'bg-secondary'}">
                        ${item.openHope ? '오픈희망' : '일반'}
                    </span>
                </td>
            </tr>
        `).join('');
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function formatCreatedAt(createdAt) {
    if (!createdAt) return '-';
    
    // 이미 문자열 형태의 날짜가 있다면 그대로 사용
    if (typeof createdAt === 'string') {
        // yyyy-MM-dd HH:mm:ss 형식에서 날짜 부분만 추출
        const datePart = createdAt.split(' ')[0];
        return datePart;
    }
    
    // Date 객체인 경우
    const date = new Date(createdAt);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 공통 알림 함수
function showAlert(message, type = 'info') {
    const modalElement = document.getElementById('alertModal');
    const modal = new bootstrap.Modal(document.getElementById('alertModal'));
    const title = document.getElementById('alertModalTitle');
    const body = document.getElementById('alertModalBody');
    
    // 타입에 따른 스타일 설정
    switch (type) {
        case 'success':
            title.textContent = '성공';
            title.className = 'modal-title text-success';
            break;
        case 'error':
            title.textContent = '오류';
            title.className = 'modal-title text-danger';
            break;
        case 'warning':
            title.textContent = '경고';
            title.className = 'modal-title text-warning';
            break;
        default:
            title.textContent = '알림';
            title.className = 'modal-title text-info';
    }
    
    body.textContent = message
    modal.show();

    // 버튼에 이벤트 리스너 추가
    const closeButtons = modalElement.querySelectorAll('[data-bs-dismiss="modal"]');
    closeButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            modal.hide(); // 명시적 hide
        }, { once: true }); // 중복 등록 방지
    });
}

// 서비스 점검 알럿 함수
function showServiceMaintenanceAlert(serviceName) {
    showAlert(`${serviceName} 서비스는 현재 점검 중입니다.`, 'warning');
}

// 모듈화된 페이지별 초기화 함수들
const AdminPageModules = {
    // 대시보드 모듈
    dashboard: {
        init: function() {
            loadDashboardStats();
        }
    },
    
    // 계정 관리 모듈
    account: {
        init: function() {
            loadUsersList();
            setupAccountForm();
        }
    },
    
    // 스케줄 집계 모듈
    scheduleSummary: {
        init: function() {
            loadMonthlyData();
        }
    }
};

// 페이지별 모듈 초기화 함수
function initPageModule(pageName) {
    if (AdminPageModules[pageName] && AdminPageModules[pageName].init) {
        AdminPageModules[pageName].init();
    }
}

// 관리자 페이지로 이동하는 함수
function goToAdminPage() {
    console.log('관리자 페이지로 이동 시도...');
    window.location.href = '/admin';
}

// 사이드바 토글 함수
function toggleSidebar() {
    console.log('toggleSidebar 함수 호출됨');
    
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    
    console.log('사이드바 요소:', sidebar);
    console.log('오버레이 요소:', overlay);
    
    if (sidebar) {
        // 사이드바 표시/숨김 토글
        const isVisible = sidebar.classList.contains('show');
        
        if (isVisible) {
            // 사이드바 숨기기
            sidebar.classList.remove('show');
            sidebar.style.transform = 'translateX(-100%)';
            console.log('사이드바 숨김');
        } else {
            // 사이드바 표시하기
            sidebar.classList.add('show');
            sidebar.style.transform = 'translateX(0)';
            sidebar.style.zIndex = '1000';
            console.log('사이드바 표시');
        }
        
        // 오버레이 생성 및 관리
        if (!overlay) {
            const newOverlay = document.createElement('div');
            newOverlay.className = 'sidebar-overlay';
            newOverlay.onclick = toggleSidebar;
            newOverlay.style.position = 'fixed';
            newOverlay.style.top = '0';
            newOverlay.style.left = '0';
            newOverlay.style.width = '100%';
            newOverlay.style.height = '100%';
            newOverlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
            newOverlay.style.zIndex = '999';
            document.body.appendChild(newOverlay);
            console.log('새 오버레이 생성됨');
        }
        
        const currentOverlay = document.querySelector('.sidebar-overlay');
        if (currentOverlay) {
            if (isVisible) {
                currentOverlay.classList.remove('show');
                currentOverlay.style.opacity = '0';
                currentOverlay.style.visibility = 'hidden';
            } else {
                currentOverlay.classList.add('show');
                currentOverlay.style.opacity = '1';
                currentOverlay.style.visibility = 'visible';
            }
        }
    } else {
        console.error('사이드바 요소를 찾을 수 없습니다!');
    }
}

// 모바일 환경에서 사이드바 자동 숨김
function setupMobileSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    
    if (window.innerWidth <= 768) {
        // 모바일에서 링크 클릭 시 사이드바 자동 숨김
        const sidebarLinks = sidebar?.querySelectorAll('a');
        if (sidebarLinks) {
            sidebarLinks.forEach(link => {
                link.addEventListener('click', () => {
                    if (window.innerWidth <= 768) {
                        toggleSidebar();
                    }
                });
            });
        }
        
        // ESC 키로 사이드바 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && sidebar?.classList.contains('show')) {
                toggleSidebar();
            }
        });
    }
}

// 페이지 로드 시 모바일 사이드바 설정
document.addEventListener('DOMContentLoaded', function() {
    setupMobileSidebar();
    
    // 모바일 메뉴 버튼 강제 표시
    showMobileMenuButton();
    
    // 화면 크기 변경 감지
    window.addEventListener('resize', function() {
        showMobileMenuButton();
        
        if (window.innerWidth > 768) {
            // 데스크톱에서는 사이드바 항상 표시
            const sidebar = document.querySelector('.sidebar');
            const overlay = document.querySelector('.sidebar-overlay');
            if (sidebar) sidebar.classList.remove('show');
            if (overlay) overlay.classList.remove('show');
        }
    });
});

// 모바일 메뉴 버튼 표시 함수
function showMobileMenuButton() {
    const mobileButton = document.querySelector('.mobile-menu-button');
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    
    if (window.innerWidth <= 768) {
        // 모바일에서는 메뉴 버튼 표시
        if (mobileButton) {
            mobileButton.style.display = 'block';
            console.log('모바일 메뉴 버튼 표시됨 - 화면 크기:', window.innerWidth);
        }
        
        // 기존 사이드바 토글 버튼도 표시
        if (sidebarToggle) {
            sidebarToggle.style.display = 'block';
        }
    } else {
        // 데스크톱에서는 메뉴 버튼 숨김
        if (mobileButton) {
            mobileButton.style.display = 'none';
        }
        if (sidebarToggle) {
            sidebarToggle.style.display = 'none';
        }
    }
}