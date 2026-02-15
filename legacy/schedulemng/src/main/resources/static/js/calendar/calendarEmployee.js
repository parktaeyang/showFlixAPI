function drawAdminMonthlyAttendees(isAdmin, monthData, monthLabel) {
    const container = document.getElementById('divAdminMonthlyAttendeeArea');

    if (!container) {
        return;
    }

    if (!isAdmin) {
        container.innerHTML = '';
        container.style.display = 'none';
        return;
    }

    container.style.display = '';

    const attendees = getUniqueMonthlyAttendees(monthData);
    const safeMonthLabel = monthLabel || (document.getElementById('monthYear')?.textContent ?? '');

    if (attendees.length === 0) {
        container.innerHTML = `
            <div class="admin-attendee-container">
                <div class="admin-attendee-header">
                    <h3>이번 달 출근자</h3>
                    <span class="admin-attendee-month">${safeMonthLabel}</span>
                </div>
                <div class="admin-attendee-empty">이번 달 출근 예정자가 없습니다.</div>
            </div>
        `;
        return;
    }

    const attendeeCards = attendees.map(attendee => {
        const roleText = attendee.userRoleText || '-';
        const accountBadge = attendee.accountType ? escapeHtml(attendee.accountType.toUpperCase()) : '';
        const remarksText = attendee.remarks ? escapeHtml(attendee.remarks) : '';
        const nameText = escapeHtml(attendee.userName);
        const roleTextEscaped = escapeHtml(roleText);
        const userId = escapeHtml(attendee.userId);

        return `
            <div class="admin-attendee-card" 
                 data-user-id="${userId}" 
                 data-user-name="${nameText}"
                 style="cursor: pointer;"
                 title="${nameText}${roleTextEscaped ? ` · ${roleTextEscaped}` : ''}${remarksText ? `&#10;${remarksText}` : ''}&#10;클릭하여 상세 정보 보기">
                <div class="admin-attendee-name">
                    ${nameText}
                    ${accountBadge ? `<span class="admin-attendee-badge">${accountBadge}</span>` : ''}
                </div>
                <div class="admin-attendee-role">${roleTextEscaped}</div>
            </div>
        `;
    }).join('');

    container.innerHTML = `
        <div class="admin-attendee-container">
            <div class="admin-attendee-header">
                <h3>이번 달 출근자</h3>
                <span class="admin-attendee-month">${safeMonthLabel}</span>
                <span class="admin-attendee-count">${attendees.length}명</span>
            </div>
            <div class="admin-attendee-scroll">
                ${attendeeCards}
            </div>
        </div>
    `;
    
    // 카드 클릭 이벤트 추가
    const cards = container.querySelectorAll('.admin-attendee-card');
    cards.forEach(card => {
        card.addEventListener('click', function() {
            const userId = this.getAttribute('data-user-id');
            const userName = this.getAttribute('data-user-name');
            showAttendeeStatsModal(userId, userName);
        });
    });
}

function getUniqueMonthlyAttendees(monthData) {
    if (!monthData || typeof monthData !== 'object') {
        return [];
    }

    const collator = new Intl.Collator('ko-KR', { sensitivity: 'base' });
    const attendeeMap = new Map();

    Object.values(monthData).forEach(users => {
        if (!Array.isArray(users)) {
            return;
        }

        users.forEach(user => {
            if (!user || !user.userId) {
                return;
            }

            if (!attendeeMap.has(user.userId)) {
                attendeeMap.set(user.userId, {
                    userId: user.userId,
                    userName: user.userName || '-',
                    userRole: user.userRole || '',
                    userRoleText: user.userRoleText || (typeof roleDisplayNameMap !== 'undefined' ? (roleDisplayNameMap[user.userRole] || '-') : '-'),
                    accountType: user.accountType || '',
                    remarks: user.remarks || ''
                });
            }
        });
    });

    return Array.from(attendeeMap.values()).sort((a, b) => collator.compare(a.userName, b.userName));
}

function escapeHtml(text) {
    if (text === null || text === undefined) {
        return '';
    }

    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/**
 * 출근자 통계 모달 표시
 * @param {string} userId 사용자 ID
 * @param {string} userName 사용자 이름
 */
async function showAttendeeStatsModal(userId, userName) {
    // 기존 모달 제거
    removeAttendeeStatsModal();
    
    // 로딩 상태 모달 생성
    const modalOverlay = document.createElement('div');
    modalOverlay.id = 'attendeeStatsModalOverlay';
    modalOverlay.className = 'modal-overlay';
    
    const modalContent = document.createElement('div');
    modalContent.id = 'attendeeStatsModalContent';
    modalContent.className = 'modal-content attendee-stats-modal';
    modalContent.innerHTML = `
        <div style="text-align: center; padding: 20px;">
            <div class="loading"></div>
            <p style="margin-top: 10px; color: #6b46c1;">데이터를 불러오는 중...</p>
        </div>
    `;
    
    modalOverlay.appendChild(modalContent);
    document.body.appendChild(modalOverlay);
    document.body.classList.add('modal-open');
    
    try {
        // 현재 년월 가져오기 (calendar.js의 전역 변수 사용)
        const year = typeof currentYear !== 'undefined' ? currentYear : new Date().getFullYear();
        const month = typeof currentMonth !== 'undefined' ? currentMonth + 1 : new Date().getMonth() + 1;
        
        // API 호출
        const response = await axios.get('/api/schedule-summary/user-monthly-stats', {
            params: {
                userId: userId,
                year: year,
                month: month
            }
        });
        
        if (response.data.success) {
            const stats = response.data.data;
            const totalDays = stats.totalDays || 0;
            const totalHours = stats.totalHours || '0.0';
            const monthLabel = document.getElementById('monthYear')?.textContent || `${year}년 ${month}월`;
            
            modalContent.innerHTML = `
                <div class="attendee-stats-header">
                    <h2>${escapeHtml(userName)} 출근 통계</h2>
                    <span class="attendee-stats-month">${monthLabel}</span>
                </div>
                <div class="attendee-stats-body">
                    <div class="stat-item-large">
                        <div class="stat-label">총 출근 횟수</div>
                        <div class="stat-value">${totalDays}일</div>
                    </div>
                    <div class="stat-item-large">
                        <div class="stat-label">총 출근 시간</div>
                        <div class="stat-value">${totalHours}시간</div>
                    </div>
                </div>
                <div style="text-align: center; margin-top: 20px;">
                    <button id="closeAttendeeStatsModal" class="close-modal-btn">닫기</button>
                </div>
            `;
        } else {
            modalContent.innerHTML = `
                <div class="attendee-stats-header">
                    <h2>${escapeHtml(userName)} 출근 통계</h2>
                </div>
                <div class="attendee-stats-body">
                    <p style="color: #ef4444; text-align: center; padding: 20px;">
                        출근자 데이터를 불러오는데 실패했습니다.<br>
                        ${response.data.message || ''}
                    </p>
                </div>
                <div style="text-align: center; margin-top: 20px;">
                    <button id="closeAttendeeStatsModal" class="close-modal-btn">닫기</button>
                </div>
            `;
        }
    } catch (error) {
        console.error('출근 통계 조회 실패:', error);
        modalContent.innerHTML = `
            <div class="attendee-stats-header">
                <h2>${escapeHtml(userName)} 출근 통계</h2>
            </div>
            <div class="attendee-stats-body">
                <p style="color: #ef4444; text-align: center; padding: 20px;">
                    출근자 데이터를 불러오는데 실패했습니다.<br>
                    ${error.response?.data?.message || error.message || '알 수 없는 오류'}
                </p>
            </div>
            <div style="text-align: center; margin-top: 20px;">
                <button id="closeAttendeeStatsModal" class="close-modal-btn">닫기</button>
            </div>
        `;
    }
    
    // 닫기 버튼 이벤트 추가
    const closeBtn = modalContent.querySelector('#closeAttendeeStatsModal');
    if (closeBtn) {
        closeBtn.addEventListener('click', removeAttendeeStatsModal);
    }
    
    // 오버레이 클릭 시 닫기
    modalOverlay.addEventListener('click', function(e) {
        if (e.target === modalOverlay) {
            removeAttendeeStatsModal();
        }
    });
}

/**
 * 출근자 통계 모달 제거
 */
function removeAttendeeStatsModal() {
    const modalOverlay = document.getElementById('attendeeStatsModalOverlay');
    if (modalOverlay) {
        modalOverlay.remove();
        document.body.classList.remove('modal-open');
    }
}


