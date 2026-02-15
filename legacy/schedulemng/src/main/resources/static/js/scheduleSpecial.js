// 특수 예약 관리 JavaScript

// 전역 변수
let specialReservations = [];
let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth() + 1;
let currentUser = 'admin'; // 실제로는 로그인한 사용자 정보를 가져와야 함

// API 기본 URL
const API_BASE_URL = '/api/schedule-special';

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadSpecialReservations();
    updateStatistics();
});

// 페이지 초기화
function initializePage() {
    // 검색 입력 필드에 엔터키 이벤트 추가
    const searchInputs = ['searchCustomerName', 'searchContactInfo', 'searchSpecialRemarks'];
    searchInputs.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    searchReservations();
                }
            });
        }
    });
    
    // 키보드 단축키 설정
    setupKeyboardShortcuts();
    
    // 자동 저장 설정
    setupAutoSave();
}

// 특수 예약 데이터 로드
async function loadSpecialReservations() {
    try {
        showLoading(true);
        
        const response = await fetch(API_BASE_URL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        specialReservations = await response.json();
        renderReservationTable();
        updateStatistics();
        
    } catch (error) {
        console.error('Error loading special reservations:', error);
        showNotification('예약 데이터를 불러오는 중 오류가 발생했습니다.', 'error');
        
        renderReservationTable();
        updateStatistics();
    } finally {
        showLoading(false);
    }
}


// 예약 테이블 렌더링
function renderReservationTable() {
    const tbody = document.getElementById('specialReservationTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // 데이터가 없을 때 빈 상태 표시
    if (specialReservations.length === 0) {
        const emptyRow = document.createElement('tr');
        emptyRow.className = 'empty-row';
        emptyRow.innerHTML = `
            <td colspan="9" class="empty-message">
                <div class="empty-state">
                    <i class="fas fa-calendar-plus"></i>
                    <p>등록된 특수 예약이 없습니다.</p>
                    <button class="btn-special btn-info" onclick="addNewReservation()">
                        <i class="fas fa-plus"></i> 새 예약 추가하기
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(emptyRow);
        return;
    }
    
    specialReservations.forEach(reservation => {
        const row = createReservationRow(reservation);
        tbody.appendChild(row);
    });
}

// 예약 행 생성
function createReservationRow(reservation) {
    const row = document.createElement('tr');
    
    // 하이라이트 클래스 추가
    if (reservation.highlight && reservation.highlight !== 'none') {
        row.classList.add(`highlight-${reservation.highlight}`);
    }
    
    row.innerHTML = `
        <td>
            <input type="text" class="special-input" value="${reservation.reservationDate || ''}" 
                   placeholder="예: 2024-01-15"
                   onchange="updateReservation(${reservation.id}, 'reservationDate', this.value)"
                   title="예약 날짜를 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input" value="${reservation.reservationTime || ''}" 
                   placeholder="예: 19:00"
                   onchange="updateReservation(${reservation.id}, 'reservationTime', this.value)"
                   title="예약 시간을 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input remarks-input" value="${reservation.specialRemarks || ''}" 
                   placeholder="특이사항을 입력하세요"
                   onchange="updateReservation(${reservation.id}, 'specialRemarks', this.value)"
                   title="특이사항을 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input" value="${reservation.customerName || ''}" 
                   placeholder="고객명"
                   onchange="updateReservation(${reservation.id}, 'customerName', this.value)"
                   title="고객명을 입력하세요">
        </td>
        <td>
            <input type="number" class="special-input" value="${reservation.peopleCount || ''}" 
                   placeholder="인원수"
                   min="1"
                   onchange="updateReservation(${reservation.id}, 'peopleCount', this.value)"
                   title="인원수를 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input remarks-input" value="${reservation.paymentStatus || ''}" 
                   placeholder="결제 상태"
                   onchange="updateReservation(${reservation.id}, 'paymentStatus', this.value)"
                   title="결제 상태를 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input" value="${reservation.contactInfo || ''}" 
                   placeholder="연락처"
                   onchange="updateReservation(${reservation.id}, 'contactInfo', this.value)"
                   title="연락처를 입력하세요">
        </td>
        <td>
            <input type="text" class="special-input remarks-input" value="${reservation.notes || ''}" 
                   placeholder="비고"
                   onchange="updateReservation(${reservation.id}, 'notes', this.value)"
                   title="비고를 입력하세요">
        </td>
        <td>
            <div style="display: flex; gap: 8px; justify-content: center; align-items: center;">
                <button class="btn-special btn-sm" onclick="deleteReservation(${reservation.id})" 
                        style="padding: 6px 10px; font-size: 11px; background: #dc2626; min-width: auto;"
                        title="예약 삭제">
                    <i class="fas fa-trash"></i>
                </button>
                <select class="special-input" style="width: 90px; font-size: 11px; padding: 6px 8px;" 
                        onchange="changeReservationStatus(${reservation.id}, this.value)"
                        title="예약 상태 변경">
                    <option value="PENDING" ${reservation.reservationStatus === 'PENDING' ? 'selected' : ''}>대기</option>
                    <option value="CONFIRMED" ${reservation.reservationStatus === 'CONFIRMED' ? 'selected' : ''}>확정</option>
                    <option value="COMPLETED" ${reservation.reservationStatus === 'COMPLETED' ? 'selected' : ''}>완료</option>
                    <option value="CANCELLED" ${reservation.reservationStatus === 'CANCELLED' ? 'selected' : ''}>취소</option>
                </select>
            </div>
        </td>
    `;
    
    return row;
}

// 예약 정보 업데이트
async function updateReservation(id, field, value) {
    try {
        const reservation = specialReservations.find(r => r.id === id);
        if (!reservation) {
            console.error('Reservation not found:', id);
            return;
        }
        
        // 로컬 데이터 업데이트
        reservation[field] = value;
        
        // 서버에 업데이트 요청
        const updateData = {
            reservationDate: reservation.reservationDate,
            reservationTime: reservation.reservationTime,
            specialRemarks: reservation.specialRemarks,
            customerName: reservation.customerName,
            peopleCount: reservation.peopleCount,
            paymentStatus: reservation.paymentStatus,
            contactInfo: reservation.contactInfo,
            notes: reservation.notes,
            reservationStatus: reservation.reservationStatus,
            highlightType: reservation.highlightType,
            expectedRevenue: reservation.expectedRevenue,
            updatedBy: currentUser
        };
        
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const updatedReservation = await response.json();
        // 서버에서 받은 데이터로 로컬 데이터 업데이트
        Object.assign(reservation, updatedReservation);
        
        console.log(`예약 ${id}의 ${field} 필드가 ${value}로 업데이트되었습니다.`);
        
    } catch (error) {
        console.error('Error updating reservation:', error);
        showNotification('예약 정보 업데이트 중 오류가 발생했습니다.', 'error');
    }
}

// 통계 업데이트
async function updateStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const statistics = await response.json();
        
        // DOM 업데이트
        updateStatElement('totalReservations', statistics.totalReservations);
        updateStatElement('pendingReservations', statistics.pendingReservations);
        updateStatElement('confirmedReservations', statistics.confirmedReservations);
        
    } catch (error) {
        console.error('Error loading statistics:', error);
        
        // 오류 발생 시 로컬 데이터로 계산
        const totalReservations = specialReservations.length;
        const pendingReservations = specialReservations.filter(r => r.reservationStatus === 'PENDING').length;
        const confirmedReservations = specialReservations.filter(r => r.reservationStatus === 'CONFIRMED').length;
        
        updateStatElement('totalReservations', totalReservations);
        updateStatElement('pendingReservations', pendingReservations);
        updateStatElement('confirmedReservations', confirmedReservations);
    }
}

// 통계 요소 업데이트
function updateStatElement(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

// 예약 필터링 (기존 함수 - 호환성을 위해 유지)
async function filterReservations() {
    // 새로운 상태별 필터링 함수로 리다이렉트
    const activeStatusBtn = document.querySelector('.status-btn.active');
    const status = activeStatusBtn ? activeStatusBtn.dataset.status : '';
    await filterReservationsByStatus(status);
}

// 새 예약 추가
async function addNewReservation() {
    try {
        const newReservationData = {
            reservationDate: '미정', // 필수 필드이므로 기본값 설정
            reservationTime: '',
            specialRemarks: '',
            customerName: '',
            peopleCount: 1, // 최소값 1
            paymentStatus: '',
            contactInfo: '',
            notes: '',
            reservationStatus: 'PENDING',
            highlightType: 'NONE',
            createdBy: currentUser
        };
        
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(newReservationData)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            const errorMessage = errorData.message || errorData.error || `HTTP error! status: ${response.status}`;
            throw new Error(errorMessage);
        }
        
        const newReservation = await response.json();
        specialReservations.unshift(newReservation);
        renderReservationTable();
        updateStatistics();
        
        showNotification('새 예약이 추가되었습니다.', 'success');
        
        // 새로 추가된 행으로 스크롤
        const tbody = document.getElementById('specialReservationTableBody');
        if (tbody && tbody.firstChild) {
            tbody.firstChild.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        
    } catch (error) {
        console.error('Error adding new reservation:', error);
        console.error('Request data:', newReservationData);
        showNotification(`새 예약 추가 중 오류가 발생했습니다: ${error.message}`, 'error');
    }
}

// 모든 예약 저장
async function saveAllReservations() {
    try {
        showLoading(true);
        
        // 모든 예약을 서버에 저장
        const savePromises = specialReservations.map(async (reservation) => {
            const updateData = {
                reservationDate: reservation.reservationDate,
                reservationTime: reservation.reservationTime,
                specialRemarks: reservation.specialRemarks,
                customerName: reservation.customerName,
                peopleCount: reservation.peopleCount,
                paymentStatus: reservation.paymentStatus,
                contactInfo: reservation.contactInfo,
                notes: reservation.notes,
                reservationStatus: reservation.reservationStatus,
                highlightType: reservation.highlightType,
                expectedRevenue: reservation.expectedRevenue,
                updatedBy: currentUser
            };
            
            const response = await fetch(`${API_BASE_URL}/${reservation.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updateData)
            });
            
            if (!response.ok) {
                throw new Error(`Failed to save reservation ${reservation.id}`);
            }
            
            return response.json();
        });
        
        await Promise.all(savePromises);
        
        showNotification('모든 예약이 저장되었습니다.', 'success');
        
    } catch (error) {
        console.error('Error saving all reservations:', error);
        showNotification('예약 저장 중 오류가 발생했습니다.', 'error');
    } finally {
        showLoading(false);
    }
}

// 엑셀 내보내기
function exportToExcel() {
    try {
        // 현재 테이블에 표시된 데이터를 가져옴
        const currentData = getCurrentTableData();
        
        if (currentData.length === 0) {
            showNotification('다운로드할 데이터가 없습니다.', 'warning');
            return;
        }
        
        const csvContent = generateCSV(currentData);
        const filename = generateFilename();
        downloadCSV(csvContent, filename);
        
        showNotification(`CSV 파일이 다운로드되었습니다. (${currentData.length}건)`, 'success');
        
    } catch (error) {
        console.error('Error exporting to CSV:', error);
        showNotification('CSV 다운로드 중 오류가 발생했습니다.', 'error');
    }
}

// 현재 테이블에 표시된 데이터 가져오기
function getCurrentTableData() {
    const tbody = document.getElementById('specialReservationTableBody');
    if (!tbody) return [];
    
    const rows = tbody.querySelectorAll('tr:not(.empty-row)');
    const currentData = [];
    
    rows.forEach(row => {
        const inputs = row.querySelectorAll('input, select');
        if (inputs.length > 0) {
            const reservation = {
                reservationDate: inputs[0]?.value || '',
                reservationTime: inputs[1]?.value || '',
                specialRemarks: inputs[2]?.value || '',
                customerName: inputs[3]?.value || '',
                peopleCount: inputs[4]?.value || '',
                paymentStatus: inputs[5]?.value || '',
                contactInfo: inputs[6]?.value || '',
                notes: inputs[7]?.value || '',
                reservationStatus: inputs[8]?.value || 'PENDING'
            };
            currentData.push(reservation);
        }
    });
    
    return currentData;
}

// 파일명 생성
function generateFilename() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    return `특수예약관리_${year}${month}${day}_${hours}${minutes}.csv`;
}

// CSV 생성
function generateCSV(data = specialReservations) {
    const headers = ['날짜', '시간', '특이사항', '이름', '인원(명)', '결제여부', '연락처', '비고', '상태'];
    const csvRows = [headers.join(',')];
    
    data.forEach(reservation => {
        const row = [
            escapeCSVField(reservation.reservationDate || ''),
            escapeCSVField(reservation.reservationTime || ''),
            escapeCSVField(reservation.specialRemarks || ''),
            escapeCSVField(reservation.customerName || ''),
            escapeCSVField(reservation.peopleCount || ''),
            escapeCSVField(reservation.paymentStatus || ''),
            escapeCSVField(reservation.contactInfo || ''),
            escapeCSVField(reservation.notes || ''),
            escapeCSVField(getStatusText(reservation.reservationStatus))
        ];
        csvRows.push(row.join(','));
    });
    
    return csvRows.join('\n');
}

// CSV 필드 이스케이프 처리
function escapeCSVField(field) {
    if (field === null || field === undefined) {
        return '""';
    }
    
    const stringField = String(field);
    
    // 쉼표, 따옴표, 줄바꿈이 포함된 경우 따옴표로 감싸고 내부 따옴표는 이스케이프
    if (stringField.includes(',') || stringField.includes('"') || stringField.includes('\n') || stringField.includes('\r')) {
        return `"${stringField.replace(/"/g, '""')}"`;
    }
    
    return stringField;
}

// 상태 텍스트 변환
function getStatusText(status) {
    const statusMap = {
        'PENDING': '대기',
        'CONFIRMED': '확정',
        'COMPLETED': '완료',
        'CANCELLED': '취소'
    };
    return statusMap[status] || status || '';
}

// CSV 다운로드
function downloadCSV(csvContent, filename) {
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    
    if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}

// 알림 표시
function showNotification(message, type = 'info') {
    // 기존 알림 제거
    const existingNotifications = document.querySelectorAll('.notification-toast');
    existingNotifications.forEach(notif => notif.remove());
    
    const notification = document.createElement('div');
    notification.className = 'notification-toast';
    
    const typeConfig = {
        success: { bg: '#10b981', icon: 'fas fa-check-circle' },
        error: { bg: '#ef4444', icon: 'fas fa-exclamation-circle' },
        warning: { bg: '#f59e0b', icon: 'fas fa-exclamation-triangle' },
        info: { bg: '#3b82f6', icon: 'fas fa-info-circle' }
    };
    
    const config = typeConfig[type] || typeConfig.info;
    
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        background: ${config.bg};
        color: white;
        padding: 16px 20px;
        border-radius: 12px;
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
        display: flex;
        align-items: center;
        gap: 12px;
        max-width: 400px;
        transform: translateX(100%);
        transition: transform 0.3s ease;
        font-weight: 500;
        font-size: 14px;
    `;
    
    notification.innerHTML = `
        <i class="${config.icon}" style="font-size: 18px;"></i>
        <span>${message}</span>
        <button onclick="this.parentElement.remove()" style="
            background: none;
            border: none;
            color: white;
            font-size: 18px;
            cursor: pointer;
            margin-left: auto;
            opacity: 0.7;
            transition: opacity 0.2s;
        " onmouseover="this.style.opacity='1'" onmouseout="this.style.opacity='0.7'">×</button>
    `;
    
    document.body.appendChild(notification);
    
    // 애니메이션으로 나타나기
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // 4초 후 자동 제거
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }
    }, 4000);
}

// 행 하이라이트 토글
function toggleRowHighlight(rowId, highlightType) {
    const reservation = specialReservations.find(r => r.id === rowId);
    if (reservation) {
        reservation.highlight = highlightType;
        renderReservationTable();
    }
}

// 예약 상태 변경
async function changeReservationStatus(rowId, status) {
    try {
        const response = await fetch(`${API_BASE_URL}/${rowId}/status?status=${status}`, {
            method: 'PATCH'
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const updatedReservation = await response.json();
        
        // 로컬 데이터 업데이트
        const reservation = specialReservations.find(r => r.id === rowId);
        if (reservation) {
            Object.assign(reservation, updatedReservation);
        }
        
        updateStatistics();
        showNotification(`예약 상태가 ${status}로 변경되었습니다.`, 'success');
        
    } catch (error) {
        console.error('Error changing reservation status:', error);
        showNotification('예약 상태 변경 중 오류가 발생했습니다.', 'error');
    }
}

// 예약 삭제
async function deleteReservation(rowId) {
    if (confirm('정말로 이 예약을 삭제하시겠습니까?')) {
        try {
            const response = await fetch(`${API_BASE_URL}/${rowId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            specialReservations = specialReservations.filter(r => r.id !== rowId);
            renderReservationTable();
            updateStatistics();
            showNotification('예약이 삭제되었습니다.', 'success');
            
        } catch (error) {
            console.error('Error deleting reservation:', error);
            showNotification('예약 삭제 중 오류가 발생했습니다.', 'error');
        }
    }
}

// 키보드 단축키 설정
function setupKeyboardShortcuts() {
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + S: 저장
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            saveAllReservations();
        }
        
        // Ctrl/Cmd + N: 새 예약 추가
        if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
            e.preventDefault();
            addNewReservation();
        }
        
        // Ctrl/Cmd + F: 검색 포커스
        if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
            e.preventDefault();
            const searchInput = document.getElementById('searchCustomerName');
            if (searchInput) {
                searchInput.focus();
                searchInput.select();
            }
        }
        
        // Escape: 검색 필드 초기화
        if (e.key === 'Escape') {
            clearSearchFields();
        }
    });
}

// 자동 저장 설정
function setupAutoSave() {
    // 5분마다 자동 저장
    setInterval(() => {
        if (specialReservations.length > 0) {
            console.log('자동 저장 실행');
            // 실제 저장은 사용자가 변경한 경우에만
        }
    }, 5 * 60 * 1000); // 5분
}

// 상태별 필터링
function filterByStatus(status) {
    // 모든 상태 버튼에서 active 클래스 제거
    document.querySelectorAll('.status-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // 클릭된 버튼에 active 클래스 추가
    const clickedBtn = document.querySelector(`[data-status="${status}"]`);
    if (clickedBtn) {
        clickedBtn.classList.add('active');
    }
    
    // 상태별 필터링 실행
    filterReservationsByStatus(status);
}

// 상태별 예약 필터링
async function filterReservationsByStatus(status) {
    try {
        showLoading(true);
        
        let filteredReservations;
        if (!status) {
            // 전체 조회
            const response = await fetch(API_BASE_URL);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            filteredReservations = await response.json();
        } else {
            // 상태별 조회
            const response = await fetch(`${API_BASE_URL}/status/${status}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            filteredReservations = await response.json();
        }
        
        const tbody = document.getElementById('specialReservationTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '';
        
        // 필터링 결과가 없을 때 빈 상태 표시
        if (filteredReservations.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-row';
            emptyRow.innerHTML = `
                <td colspan="9" class="empty-message">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <p>검색 조건에 맞는 예약이 없습니다.</p>
                        <button class="btn-special btn-info" onclick="clearSearchFields()">
                            <i class="fas fa-eraser"></i> 검색 초기화
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(emptyRow);
            showNotification('검색 조건에 맞는 예약이 없습니다.', 'info');
            return;
        }
        
        filteredReservations.forEach(reservation => {
            const row = createReservationRow(reservation);
            tbody.appendChild(row);
        });
        
        showNotification(`상태별 필터링: ${filteredReservations.length}건`, 'info');
        
    } catch (error) {
        console.error('Error filtering reservations by status:', error);
        showNotification('상태별 필터링 중 오류가 발생했습니다.', 'error');
        
        // 오류 발생 시 로컬 필터링으로 폴백
        const filteredReservations = specialReservations.filter(r => 
            !status || r.reservationStatus === status
        );
        
        const tbody = document.getElementById('specialReservationTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '';
        
        // 로컬 필터링 결과가 없을 때 빈 상태 표시
        if (filteredReservations.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-row';
            emptyRow.innerHTML = `
                <td colspan="9" class="empty-message">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <p>검색 조건에 맞는 예약이 없습니다.</p>
                        <button class="btn-special btn-info" onclick="clearSearchFields()">
                            <i class="fas fa-eraser"></i> 검색 초기화
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(emptyRow);
            return;
        }
        
        filteredReservations.forEach(reservation => {
            const row = createReservationRow(reservation);
            tbody.appendChild(row);
        });
    } finally {
        showLoading(false);
    }
}

// 검색 필드 초기화
function clearSearchFields() {
    const searchInputs = [
        'searchCustomerName',
        'searchContactInfo', 
        'searchSpecialRemarks'
    ];
    
    searchInputs.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.value = '';
        }
    });
    
    // 모든 상태 버튼에서 active 클래스 제거하고 전체 버튼 활성화
    document.querySelectorAll('.status-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector('[data-status=""]').classList.add('active');
    
    loadSpecialReservations();
    showNotification('검색 조건이 초기화되었습니다', 'info');
}

// 입력 필드에 실시간 검증 추가
function addInputValidation() {
    document.addEventListener('input', function(e) {
        if (e.target.classList.contains('special-input')) {
            // 인원수 필드 검증
            if (e.target.type === 'number' && e.target.min) {
                const value = parseInt(e.target.value);
                if (value < 1) {
                    e.target.style.borderColor = '#ef4444';
                    e.target.title = '인원수는 1명 이상이어야 합니다';
                } else {
                    e.target.style.borderColor = '';
                    e.target.title = '';
                }
            }
            
            // 연락처 필드 검증 (간단한 전화번호 형식)
            if (e.target.placeholder === '연락처') {
                const phonePattern = /^[0-9-+\s()]*$/;
                if (e.target.value && !phonePattern.test(e.target.value)) {
                    e.target.style.borderColor = '#f59e0b';
                    e.target.title = '올바른 연락처 형식을 입력하세요';
                } else {
                    e.target.style.borderColor = '';
                    e.target.title = '';
                }
            }
        }
    });
}

// 페이지 초기화 시 검증 기능 추가
document.addEventListener('DOMContentLoaded', function() {
    addInputValidation();
});

// 로딩 상태 표시
function showLoading(show) {
    const loadingElement = document.getElementById('loadingIndicator');
    if (loadingElement) {
        loadingElement.style.display = show ? 'block' : 'none';
    } else if (show) {
        // 로딩 인디케이터가 없으면 생성
        const loading = document.createElement('div');
        loading.id = 'loadingIndicator';
        loading.className = 'loading-indicator';
        loading.innerHTML = '<div class="spinner"></div><span>로딩 중...</span>';
        loading.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: rgba(0, 0, 0, 0.8);
            color: white;
            padding: 20px;
            border-radius: 8px;
            z-index: 9999;
            display: flex;
            align-items: center;
            gap: 10px;
        `;
        document.body.appendChild(loading);
    }
}

// 검색 기능
async function searchReservations() {
    const customerName = document.getElementById('searchCustomerName')?.value || '';
    const contactInfo = document.getElementById('searchContactInfo')?.value || '';
    const specialRemarks = document.getElementById('searchSpecialRemarks')?.value || '';
    const status = document.getElementById('statusSelect')?.value || '';
    
    try {
        showLoading(true);
        
        const params = new URLSearchParams();
        if (customerName) params.append('customerName', customerName);
        if (contactInfo) params.append('contactInfo', contactInfo);
        if (specialRemarks) params.append('specialRemarks', specialRemarks);
        if (status) params.append('status', status);
        
        const response = await fetch(`${API_BASE_URL}/search?${params}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const searchResults = await response.json();
        
        const tbody = document.getElementById('specialReservationTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '';
        
        // 검색 결과가 없을 때 빈 상태 표시
        if (searchResults.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.className = 'empty-row';
            emptyRow.innerHTML = `
                <td colspan="9" class="empty-message">
                    <div class="empty-state">
                        <i class="fas fa-search"></i>
                        <p>검색 조건에 맞는 예약이 없습니다.</p>
                        <button class="btn-special btn-info" onclick="clearSearchFields()">
                            <i class="fas fa-eraser"></i> 검색 초기화
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(emptyRow);
            showNotification('검색 조건에 맞는 예약이 없습니다.', 'info');
            return;
        }
        
        searchResults.forEach(reservation => {
            const row = createReservationRow(reservation);
            tbody.appendChild(row);
        });
        
        showNotification(`검색 결과: ${searchResults.length}건`, 'info');
        
    } catch (error) {
        console.error('Error searching reservations:', error);
        showNotification('검색 중 오류가 발생했습니다.', 'error');
    } finally {
        showLoading(false);
    }
}
