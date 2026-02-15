const today = new Date();
let currentMonth = today.getMonth();
let currentYear = today.getFullYear();
let isAdmin = false;

const selectedData = {};
let modalUserData = {};
let allUsersCache = null; // [{userId, userName, accountType}, ...]
let currentUser = null; // 현재 로그인한 사용자 정보

// Role Enum 매핑 (Java의 displayName과 동일하게 작성)
const roleDisplayNameMap = {
    DEFAULT: '-',

    //스탭
    DOOR: "도어",
    HOLEMAN: "홀맨",
    OPER: "오퍼",
    HELPER: "헬퍼",
    KITCHEN: "주방",

    //배우
    MALE1: "남1",
    MALE2: "남2",
    MALE3: "남3",
    FEMALE1: "여1",
    FEMALE2: "여2",
    FEMALE3: "여3"
};

const roleGroups = {
    A: ["DEFAULT", "MALE1", "MALE2", "MALE3", "FEMALE1", "FEMALE2", "FEMALE3"],
    S: ["DEFAULT", "DOOR", "HOLEMAN", "OPER", "HELPER", "KITCHEN"],
    ALL: ["DEFAULT", "DOOR", "HOLEMAN", "OPER", "HELPER", "KITCHEN", "MALE1", "MALE2", "MALE3", "FEMALE1", "FEMALE2", "FEMALE3"],
};

/**
 * 계정 타입에 따른 역할 옵션 반환
 * @param {string} accountType - 계정 타입 (A, S, C, W 등)
 * @returns {Array} 역할 옵션 배열
 */
function getRoleOptions(accountType) {
    if (accountType === "A") return roleGroups.A;
    if (accountType === "S") return roleGroups.S;
    // C, W 또는 그 외는 전부
    return roleGroups.ALL;
}

/**
 * 캘린더를 생성하고 렌더링
 * @param {number} year - 년도
 * @param {number} month - 월 (0-11)
 * @param {Object} data - 날짜별 사용자 데이터
 */
function generateCalendar(year, month, data = {}) {
    console.log('generateCalendar()');
    
    const calendarBody = document.getElementById("calendarBody");
    const monthYear = document.getElementById("monthYear");
    
    if (!calendarBody || !monthYear) {
        console.error("필수 DOM 요소를 찾을 수 없습니다.");
        return;
    }
    
    // 기존 캘린더 내용 초기화
    calendarBody.innerHTML = "";
    
    // 월 제목 설정
    monthYear.innerText = `${year}년 ${month + 1}월`;
    
    // 캘린더 날짜 생성
    createCalendarDates(year, month, data);
}

/**
 * 캘린더 날짜 생성 (기존 tbody에 직접 추가)
 * @param {number} year - 년도
 * @param {number} month - 월 (0-11)
 * @param {Object} data - 날짜별 사용자 데이터
 */
function createCalendarDates(year, month, data = {}) {
    const calendarBody = document.getElementById("calendarBody");
    const firstDay = new Date(year, month, 1);
    const lastDate = new Date(year, month + 1, 0).getDate();
    const startDay = firstDay.getDay();
    
    let date = 1;
    
    // 6주 x 7일 = 42개 셀 생성
    for (let i = 0; i < 6; i++) {
        const row = document.createElement("tr");
        
        for (let j = 0; j < 7; j++) {
            const cell = createCalendarCell(year, month, date, startDay, i, j, data);
            row.appendChild(cell);
            
            if (!(i === 0 && j < startDay) && date <= lastDate) {
                date++;
            }
        }
        
        calendarBody.appendChild(row);
    }
}

/**
 * 개별 캘린더 셀 생성
 * @param {number} year - 년도
 * @param {number} month - 월 (0-11)
 * @param {number} date - 날짜
 * @param {number} startDay - 월 첫째 주 시작 요일
 * @param {number} rowIndex - 행 인덱스
 * @param {number} colIndex - 열 인덱스
 * @param {Object} data - 날짜별 사용자 데이터
 * @returns {HTMLElement} 캘린더 셀
 */
function createCalendarCell(year, month, date, startDay, rowIndex, colIndex, data) {
    const cell = document.createElement("td");
    
    // 첫째 주에서 시작 요일 이전은 빈 셀
    if (rowIndex === 0 && colIndex < startDay) {
        cell.innerHTML = "";
        return cell;
    }
    
    // 날짜가 월의 마지막 날을 넘어가면 빈 셀
    const lastDate = new Date(year, month + 1, 0).getDate();
    if (date > lastDate) {
        cell.innerHTML = "";
        return cell;
    }
    
    const dateStr = formatDateString(year, month, date);
    const cellDate = new Date(year, month, date);
    const isToday = cellDate.toDateString() === today.toDateString();
    
    if (isToday) {
        cell.classList.add("today");
    }
    
    // 셀 내용 생성
    cell.innerHTML = createCellContent(date, dateStr);
    
    // 이벤트 리스너 추가
    addCellEventListeners(cell, dateStr, data);
    
    // 사용자 리스트 추가
    addUserListToCell(cell, dateStr, data);
    
    return cell;
}

/**
 * 날짜 문자열을 YYYY-MM-DD 형식으로 포맷
 * @param {number} year - 년도
 * @param {number} month - 월 (0-11)
 * @param {number} date - 날짜
 * @returns {string} 포맷된 날짜 문자열
 */
function formatDateString(year, month, date) {
    return `${year}-${(month + 1).toString().padStart(2, '0')}-${date.toString().padStart(2, '0')}`;
}

/**
 * 셀 내용 HTML 생성
 * @param {number} date - 날짜
 * @param {string} dateStr - 날짜 문자열
 * @returns {string} 셀 HTML
 */
function createCellContent(date, dateStr) {
    return `
        <strong>${date}</strong>
        <div class="checkbox-wrap">
            <input type="checkbox" class="date-checkbox" data-date="${dateStr}">
        </div>
        <div class="user-list"></div>
    `;
}

/**
 * 셀에 이벤트 리스너 추가
 * @param {HTMLElement} cell - 캘린더 셀
 * @param {string} dateStr - 날짜 문자열
 * @param {Object} data - 날짜별 사용자 데이터
 */
function addCellEventListeners(cell, dateStr, data) {
    const dateCheckbox = cell.querySelector(".date-checkbox");
    
    // 체크박스 변경 이벤트
    dateCheckbox.addEventListener("change", function() {
        if (this.checked) {
            selectedData[dateStr] = {};
        } else {
            delete selectedData[dateStr];
        }
    });
    
    // 셀 클릭 이벤트 (모달 열기)
    cell.addEventListener("click", function(e) {
        if (e.target.closest(".checkbox-wrap")) return;
        createModal(dateStr, data[dateStr] || []);
    });
}

/**
 * 셀에 사용자 리스트 추가
 * @param {HTMLElement} cell - 캘린더 셀
 * @param {string} dateStr - 날짜 문자열
 * @param {Object} data - 날짜별 사용자 데이터
 */
function addUserListToCell(cell, dateStr, data) {
    if (!data[dateStr]) return;
    
    const userListDiv = cell.querySelector(".user-list");
    const users = data[dateStr];
    
    const listHtml = users.map(user => {
        const isMyData = currentUser && user.userId === currentUser.userId;
        const colorClass = isMyData ? 'my-data' : '';
        return `<li class="${colorClass}">${user.userName}</li>`;
    }).join("");
    userListDiv.innerHTML = `<ul style="padding-left: 8px; margin: 0;">${listHtml}</ul>`;
}

/**
 * 월 변경 (이전/다음)
 * @param {number} step - 변경할 월 수 (-1: 이전, 1: 다음)
 */
function changeMonth(step) {
    currentMonth += step;
    
    // 월 범위 조정
    if (currentMonth > 11) {
        currentMonth = 0;
        currentYear++;
    } else if (currentMonth < 0) {
        currentMonth = 11;
        currentYear--;
    }
    
    // 새로운 월 데이터 조회 및 렌더링
    fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
}

/**
 * 선택된 날짜들을 서버에 저장
 * @returns {Promise<void>}
 */
async function saveSelections() {
    try {
        console.log("저장할 데이터:", selectedData);
        
        // 선택된 날짜가 있는지 확인
        if (Object.keys(selectedData).length === 0) {
            alert("저장할 날짜를 선택해주세요.");
            return;
        }

        // 서버에 저장 요청
        await axios.post("/api/dates/save", selectedData);
        
        alert("저장 완료");
        
        // 캘린더 새로고침
        await fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
        
    } catch (error) {
        console.error("저장 실패:", error);
        alert("저장 실패: " + (error.response?.data?.message || error.message || "알 수 없는 오류"));
    }
}

/**
 * 특정 년월의 데이터를 조회하고 캘린더를 렌더링
 * @param {number} year - 년도
 * @param {number} month - 월 (1-12)
 */
async function fetchAndRenderDatesByMonth(year, month) {
    try {
        console.log('fetchAndRenderDatesByMonth()');
        
        // API에서 월별 데이터 조회
        const response = await fetch(`/api/dates/month?year=${year}&month=${month}`);
        const data = await response.json();
        
        // 관리자 권한 설정
        isAdmin = data.isAdmin;
        console.log("관리자 여부:", isAdmin);
        
        // 월별 데이터 처리
        const monthData = processMonthData(data.data);
        modalUserData = monthData;

        debugger;
        // 월 라벨 (YYYY년 M월) 생성 - API 파라미터와 동일한 월 사용
        const monthLabel = `${year}년 ${month}월`;
        
        // 관리자 전용 영역 렌더링
        drawAdminMonthlyAttendees(isAdmin, monthData, monthLabel); // calendarEmployee.js
        drawAdminMemo(isAdmin); // calendarAdmin.js
        
        // 선택된 데이터 초기화
        clearSelectedData();
        
        // 캘린더 생성
        generateCalendar(currentYear, currentMonth, monthData);
        
        // 관리자 노트 조회
        await getAdminNote();
        
    } catch (error) {
        console.error("월별 데이터 조회 실패:", error);
        alert("데이터를 불러오는데 실패했습니다.");
    }
}

/**
 * 월별 데이터를 처리하여 캘린더용 데이터로 변환
 * @param {Array} dateList - API에서 받은 날짜별 데이터
 * @returns {Object} 처리된 월별 데이터
 */
function processMonthData(dateList) {
    const monthData = {};
    
    dateList.forEach(item => {
        if (!monthData[item.date]) {
            monthData[item.date] = [];
        }
        
        monthData[item.date].push({
            userId: item.userId,
            userName: item.userName,
            accountType: item.userId.slice(0, 1),
            userRole: item.role,
            userRoleText: roleDisplayNameMap[item.role] || '-',
            remarks: item.remarks || ''
        });
    });
    
    return monthData;
}

/**
 * 선택된 데이터 초기화
 */
function clearSelectedData() {
    Object.keys(selectedData).forEach(key => delete selectedData[key]);
}

/**
 * 일정 모달 생성
 * @param {string} dateStr - 날짜 문자열
 * @param {Array} users - 사용자 목록
 */
function createModal(dateStr, users) {
    // 기존 모달 제거
    removeExistingModal();
    
    // 모달 요소 생성
    const modalOverlay = createModalOverlay();
    const modalContent = createModalContent(dateStr, users);
    
    // 모달에 내용 추가
    modalOverlay.appendChild(modalContent);
    document.body.appendChild(modalOverlay);
    document.body.classList.add("modal-open");
    
    // 이벤트 리스너 추가
    addModalEventListeners(modalContent, dateStr);
    
    // 시간표 그리기
    drawTimeTableInModal(modalContent, dateStr, users);
    
    // 확정 상태 확인
    checkConfirmStatus(modalContent, dateStr);
}

/**
 * 기존 모달 제거
 */
function removeExistingModal() {
    const existing = document.getElementById("modalOverlay");
    if (existing) existing.remove();
}

/**
 * 모달 오버레이 생성
 * @returns {HTMLElement} 모달 오버레이
 */
function createModalOverlay() {
    const modalOverlay = document.createElement("div");
    modalOverlay.id = "modalOverlay";
    modalOverlay.classList.add("modal-overlay");
    return modalOverlay;
}

/**
 * 모달 내용 생성
 * @param {string} dateStr - 날짜 문자열
 * @param {Array} users - 사용자 목록
 * @returns {HTMLElement} 모달 내용
 */
function createModalContent(dateStr, users) {
    const modalContent = document.createElement("div");
    modalContent.id = "modalContent";
    modalContent.classList.add("modal-content");
    
    // 사용자 테이블 HTML 생성
    const userTableHtml = createUserTableHtml(dateStr, users);
    
    // 모달 내용 설정
    modalContent.innerHTML = `
        ${userTableHtml}
        <div id="confirmStatus" class="confirm-status" style="display: none;">
            <span class="confirm-badge">✅ 확정됨</span>
        </div>
        ${isAdmin ? `<button id="addModalBtn" class="close-modal-btn">사용자추가</button>` : ""}
        ${isAdmin ? `<button id="saveModalBtn" class="close-modal-btn">저장</button>` : ""}
        ${isAdmin ? `<button id="confirmModalBtn" class="confirm-modal-btn">확정</button>` : ""}
        ${isAdmin ? `<button id="unconfirmModalBtn" class="unconfirm-modal-btn" style="display: none;">확정취소</button>` : ""}
        <button id="closeModalBtn" class="close-modal-btn">닫기</button>
    `;
    
    return modalContent;
}

/**
 * 사용자 테이블 HTML 생성
 * @param {string} dateStr - 날짜 문자열
 * @param {Array} users - 사용자 목록
 * @returns {string} 사용자 테이블 HTML
 */
function createUserTableHtml(dateStr, users) {
    return `
        <p><strong>📅 ${dateStr} 일정</strong></p>
        <table class="modal-user-table">
            <thead>
                <tr>
                    <th class="cell-40">이름</th>
                    <th class="cell-40">역할</th>
                    <th class="cell-20">비고</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(user => createUserTableRow(user)).join("")}
            </tbody>
        </table>
        <div id="divTimeTable" class="scroll-box"></div>
    `;
}

/**
 * 사용자 테이블 행 HTML 생성
 * @param {Object} user - 사용자 정보
 * @returns {string} 사용자 테이블 행 HTML
 */
function createUserTableRow(user) {
    const selectedRole = user.userRole ?? 'DEFAULT';
    const roleOptions = getRoleOptions(user.accountType) ?? roleGroups.ALL;
    
    const roleOptionsHtml = roleOptions.map(code => {
        const isSelected = code === selectedRole;
        const label = roleDisplayNameMap[code] ?? code ?? '-';
        return `<option value="${code}" ${isSelected ? "selected" : ""}>${label}</option>`;
    }).join("");
    
    const roleCellHtml = isAdmin
        ? `<select class="role-select" data-user-name="${user.userName}" data-account-type="${user.accountType}">${roleOptionsHtml}</select>`
        : user.userRoleText;

    const remarksCellHtml = isAdmin
        ? `<input type="text" class="remarks-input" placeholder="비고" />`
        : `<span class="remarks-view"></span>`;
    
    const isMyData = currentUser && user.userId === currentUser.userId;
    const rowClass = isMyData ? 'my-data-row' : '';
    
    return `
        <tr class="${rowClass}">
            <td class="cell-40 name-cell" data-user-id="${user.userId}" data-user-name="${user.userName}" title="눌러서 삭제">${user.userName}</td>
            <td class="cell-40" data-user-id="${user.userId}" data-user-name="${user.userName}">${roleCellHtml}</td>
            <td class="cell-20" data-user-id="${user.userId}" data-user-name="${user.userName}">${remarksCellHtml}</td>
        </tr>
    `;
}

/**
 * 모달에 이벤트 리스너 추가
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 */
function addModalEventListeners(modalContent, dateStr) {
    // 사용자 추가 버튼 이벤트
    if (isAdmin) {
        const addBtn = modalContent.querySelector("#addModalBtn");
        if (addBtn) {
            addBtn.addEventListener("click", () => addUserToModal(modalContent));
        }
        
        // 저장 버튼 이벤트
        const saveBtn = modalContent.querySelector("#saveModalBtn");
        if (saveBtn) {
            saveBtn.addEventListener("click", () => saveModalData(modalContent, dateStr));
        }
        
        // 확정 버튼 이벤트
        const confirmBtn = modalContent.querySelector("#confirmModalBtn");
        if (confirmBtn) {
            confirmBtn.addEventListener("click", () => confirmModalData(dateStr));
        }
        
        // 확정 취소 버튼 이벤트
        const unconfirmBtn = modalContent.querySelector("#unconfirmModalBtn");
        if (unconfirmBtn) {
            unconfirmBtn.addEventListener("click", () => unconfirmModalData(dateStr));
        }
    }
    
    // 닫기 버튼 이벤트
    const closeBtn = modalContent.querySelector("#closeModalBtn");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => closeModal());
    }
    
    // 사용자 삭제 이벤트
    modalContent.addEventListener("click", (e) => handleUserDeletion(e, dateStr));
}

/**
 * 모달에 사용자 추가
 * @param {HTMLElement} modalContent - 모달 내용
 */
async function addUserToModal(modalContent) {
    const tableBody = modalContent.querySelector(".modal-user-table tbody");
    if (!tableBody) return;

    // 사용자 목록 캐시 확인
    if (!allUsersCache) {
        try {
            const resp = await fetch("/api/dates/users");
            allUsersCache = await resp.json();
        } catch (e) {
            console.error("사용자 목록 조회 실패:", e);
            alert("사용자 목록을 가져오지 못했습니다.");
            return;
        }
    }

    const newRow = createNewUserRow();
    tableBody.appendChild(newRow);
    
    // 사용자 선택 이벤트 추가
    addUserSelectionEvent(newRow);
}

/**
 * 새로운 사용자 행 생성
 * @returns {HTMLElement} 새로운 사용자 행
 */
function createNewUserRow() {
    const newRow = document.createElement("tr");
    const userSelectId = `user-select-${Date.now()}-${Math.floor(Math.random()*1000)}`;

    const defaultRoleSelect = createDefaultRoleSelect();

    newRow.innerHTML = `
        <td class="cell-40 name-cell" data-user-id="" data-user-name="" title="사용자를 선택하세요">
            <select id="${userSelectId}" class="user-select">
                <option value="">-- 사용자 선택 --</option>
                ${allUsersCache.map(u => `<option value="${u.userId}" data-user-name="${u.userName}" data-account="${u.accountType}">${u.userName}</option>`).join("")}
            </select>
        </td>
        <td class="cell-40" data-user-id="" data-user-name="">
            ${defaultRoleSelect}
        </td>
    `;

    return newRow;
}

/**
 * 기본 역할 선택 셀렉트 생성
 * @returns {string} 역할 선택 HTML
 */
function createDefaultRoleSelect() {
    return `
        <select class="role-select">
            ${roleGroups.ALL.map(code => {
                const label = roleDisplayNameMap[code] || code || "-";
                const selected = code === "DEFAULT" ? "selected" : "";
                return `<option value="${code}" ${selected}>${label}</option>`;
            }).join("")}
        </select>
    `;
}

/**
 * 사용자 선택 이벤트 추가
 * @param {HTMLElement} newRow - 새로운 사용자 행
 */
function addUserSelectionEvent(newRow) {
    const userSelect = newRow.querySelector(".user-select");
    const nameCell = newRow.querySelector("td.name-cell");
    const roleCell = newRow.querySelector("td:nth-child(2)");
    const roleSelect = roleCell.querySelector("select.role-select");

    userSelect.addEventListener("change", () => {
        const sel = userSelect.options[userSelect.selectedIndex];
        const chosenId = userSelect.value;
        const chosenName = sel ? sel.getAttribute("data-user-name") : "";
        const accountType = sel ? (sel.getAttribute("data-account") || "").toUpperCase() : "";

        // 셀 dataset 반영
        nameCell.dataset.userId = chosenId || "";
        nameCell.dataset.userName = chosenName || "";
        roleCell.dataset.userId = chosenId || "";
        roleCell.dataset.userName = chosenName || "";

        // 역할 옵션 재구성
        updateRoleOptions(roleSelect, accountType);
    });
}

/**
 * 역할 옵션 업데이트
 * @param {HTMLElement} roleSelect - 역할 선택 셀렉트
 * @param {string} accountType - 계정 타입
 */
function updateRoleOptions(roleSelect, accountType) {
    const group = getRoleOptions(accountType);
    const optionsHtml = group.map(code => {
        const label = roleDisplayNameMap[code] || code || "-";
        const selected = code === "DEFAULT" ? "selected" : "";
        return `<option value="${code}" ${selected}>${label}</option>`;
    }).join("");
    
    roleSelect.innerHTML = optionsHtml;
}

/**
 * 모달 데이터 저장
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 */
async function saveModalData(modalContent, dateStr) {
    try {
        // 시간표 데이터 저장
        await saveTimeTableData(modalContent, dateStr);
        
        // 역할 데이터 저장
        await saveRoleData(modalContent, dateStr);
        
        alert("시간표 저장 완료");
        closeModal();
        await fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
        
    } catch (error) {
        console.error("저장 실패:", error);
        alert("저장 실패!");
    }
}

/**
 * 모달 데이터 확정
 * @param {string} dateStr - 날짜 문자열
 */
async function confirmModalData(dateStr) {
    await updateModalConfirmation(dateStr, "Y", "확정");
}

/**
 * 모달 데이터 확정 취소
 * @param {string} dateStr - 날짜 문자열
 */
async function unconfirmModalData(dateStr) {
    await updateModalConfirmation(dateStr, "N", "확정 취소");
}

/**
 * 모달 데이터 확정 상태 업데이트 (통합 함수)
 * @param {string} dateStr - 날짜 문자열
 * @param {string} confirmed - 확정 여부 ("Y" 또는 "N")
 * @param {string} actionName - 액션 이름 (확정/확정 취소)
 */
async function updateModalConfirmation(dateStr, confirmed, actionName) {
    try {
        const actionText = confirmed === "Y" ? "확정" : "확정 취소";
        const confirmText = confirmed === "Y" 
            ? `${dateStr} 일정을 확정하시겠습니까? 확정 후에는 수정이 어려울 수 있습니다.`
            : `${dateStr} 일정의 확정을 취소하시겠습니까? 취소 후에는 다시 수정할 수 있습니다.`;
            
        if (!confirm(confirmText)) {
            return;
        }

        // 통합 API 호출
        await axios.post("/api/dates/time-slots/confirm", { 
            date: dateStr, 
            confirmed: confirmed 
        });
        
        alert(`시간표 ${actionText} 완료`);
        closeModal();
        await fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
        
    } catch (error) {
        console.error(`${actionName} 실패:`, error);
        alert(`${actionName} 실패: ` + (error.response?.data?.message || error.message || "알 수 없는 오류"));
    }
}

/**
 * 시간표 데이터 저장
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 */
async function saveTimeTableData(modalContent, dateStr) {
    const timeTableDiv = modalContent.querySelector("#divTimeTable");
    if (!timeTableDiv) return;

    const timeTableDataToSave = [];
    const timeTableRows = timeTableDiv.querySelectorAll("tbody tr");

    timeTableRows.forEach(row => {
        const timeStr = row.cells[0].textContent;
        const themeInput = row.cells[1].querySelector("input");
        const performerCell = row.cells[2];
        const checkedPerformerCheckboxes = performerCell.querySelectorAll('input[type="checkbox"]:checked');
        const selectedPerformers = Array.from(checkedPerformerCheckboxes).map(cb => cb.value);
        const performerString = selectedPerformers.join(',');

        if (themeInput.value || selectedPerformers.length > 0) {
            timeTableDataToSave.push({
                time: timeStr.trim(), // 공백 제거
                theme: themeInput.value,
                performer: performerString
            });
        }
    });

    console.log("저장할 시간표 데이터:", timeTableDataToSave);

    await axios.post("/api/dates/time-slots/save", {
        [dateStr]: timeTableDataToSave
    });
}

/**
 * 역할 데이터 저장
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 */
async function saveRoleData(modalContent, dateStr) {
    const roleUpdates = [];
    
    modalContent.querySelectorAll('tr').forEach(tr => {
        const nameTd = tr.querySelector('td.name-cell');
        if (!nameTd) return;
        const userId = nameTd.dataset.userId;
        const userName = nameTd.dataset.userName;
        const sel = tr.querySelector('select.role-select');
        const remarksInput = tr.querySelector('input.remarks-input');
        if (!sel || !userId) return;
        roleUpdates.push({
            date: dateStr,
            userId: userId,
            userName: userName,
            role: sel.value,
            remarks: (isAdmin ? (remarksInput?.value ?? null) : null)
        });
    });

    if (roleUpdates.length > 0) {
        await axios.post("/api/dates/roles/save", roleUpdates);
    }
}

/**
 * 사용자 삭제 처리
 * @param {Event} e - 클릭 이벤트
 * @param {string} dateStr - 날짜 문자열
 */
async function handleUserDeletion(e, dateStr) {
    if (!isAdmin) return;
    
    const target = e.target;
    if (!(target instanceof Element) || !target.matches("td.name-cell")) return;

    const tr = target.closest("tr");
    if (!tr) return;

    const userId = target.getAttribute("data-user-id");
    const userName = target.getAttribute("data-user-name") || "";
    
    if (!userId) {
        tr.remove();
        return;
    }

    if (!confirm(`'${userName}' 사용자를 ${dateStr} 일정에서 삭제할까요? 즉시 저장됩니다.`)) return;

    try {
        await deleteUserFromSchedule(dateStr, userId);
        tr.remove();
        removeUserFromTimeTable(userName);
        await fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
    } catch (err) {
        console.error("사용자 삭제 실패:", err);
        alert("삭제 중 오류가 발생했습니다.");
    }
}

/**
 * 스케줄에서 사용자 삭제
 * @param {string} dateStr - 날짜 문자열
 * @param {string} userId - 사용자 ID
 */
async function deleteUserFromSchedule(dateStr, userId) {
    await axios.delete("/api/dates/selection", {
        params: { date: dateStr, userId: userId }
    });
}

/**
 * 시간표에서 사용자 제거
 * @param {string} userName - 사용자 이름
 */
function removeUserFromTimeTable(userName) {
    document.querySelectorAll(".performer-cell .performer-item").forEach(item => {
        const label = item.querySelector("label");
        if (label && label.textContent === userName) {
            item.remove();
        }
    });
}

/**
 * 모달 닫기
 */
function closeModal() {
    const modalOverlay = document.getElementById("modalOverlay");
    if (modalOverlay) {
        modalOverlay.remove();
        document.body.classList.remove("modal-open");
    }
}

/**
 * 모달에 시간표 그리기
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 * @param {Array} users - 사용자 목록
 */
function drawTimeTableInModal(modalContent, dateStr, users) {
    const timeTableDiv = modalContent.querySelector("#divTimeTable");
    if (!timeTableDiv) return;

    // 사용자별 비고 값 표시 (users 배열에 remarks 포함됨)
    users.forEach(u => {
        const row = modalContent.querySelector(`tr td.name-cell[data-user-id="${u.userId}"]`)?.closest('tr');
        if (!row) return;
        const input = row.querySelector('input.remarks-input');
        const view = row.querySelector('span.remarks-view');
        if (input) input.value = u.remarks || '';
        if (view) view.textContent = u.remarks || '';
    });

    // 해당 날짜의 시간표 데이터를 조회
    fetch(`/api/dates/time-slots?date=${dateStr}`)
        .then(res => res.json())
        .then(timeTableData => {
            drawTimeTable(timeTableDiv, timeTableData, users);
        })
        .catch(err => console.error("시간표 데이터 조회 실패:", err));
}

/**
 * 확정 상태 확인 및 UI 업데이트
 * @param {HTMLElement} modalContent - 모달 내용
 * @param {string} dateStr - 날짜 문자열
 */
function checkConfirmStatus(modalContent, dateStr) {
    fetch(`/api/dates/time-slots?date=${dateStr}`)
        .then(res => res.json())
        .then(timeTableData => {
            if (timeTableData && timeTableData.length > 0) {
                // 모든 시간표가 확정되었는지 확인
                const allConfirmed = timeTableData.every(slot => slot.confirmed === "Y");
                
                const confirmStatus = modalContent.querySelector("#confirmStatus");
                const confirmBtn = modalContent.querySelector("#confirmModalBtn");
                const saveBtn = modalContent.querySelector("#saveModalBtn");
                const addBtn = modalContent.querySelector("#addModalBtn");
                
                const unconfirmBtn = modalContent.querySelector("#unconfirmModalBtn");
                
                if (allConfirmed) {
                    // 확정된 상태
                    if (confirmStatus) confirmStatus.style.display = "block";
                    if (confirmBtn) confirmBtn.style.display = "none";
                    if (unconfirmBtn) unconfirmBtn.style.display = "inline-block";
                    if (saveBtn) saveBtn.style.display = "none";
                    if (addBtn) addBtn.style.display = "none";
                } else {
                    // 미확정 상태
                    if (confirmStatus) confirmStatus.style.display = "none";
                    if (confirmBtn) confirmBtn.style.display = "inline-block";
                    if (unconfirmBtn) unconfirmBtn.style.display = "none";
                    if (saveBtn) saveBtn.style.display = "inline-block";
                    if (addBtn) addBtn.style.display = "inline-block";
                }
            }
        })
        .catch(err => console.error("확정 상태 확인 실패:", err));
}

/**
 * 시간표 그리기
 * @param {HTMLElement} container - 시간표 컨테이너
 * @param {Array} timeTableData - 시간표 데이터
 * @param {Array} allUsers - 모든 사용자 목록
 */
function drawTimeTable(container, timeTableData = [], allUsers = []) {
    if (!container) return;

    const table = document.createElement("table");
    table.classList.add("time-table");

    const thead = document.createElement("thead");
    thead.innerHTML = `
        <tr>
            <th style="width: 15%;">시간</th>
            <th>테마</th>
            <th>출연</th>
        </tr>
    `;
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    
    // 16:00 ~ 22:00, 30분 간격으로 시간표 생성
    for (let hour = 16; hour <= 22; hour++) {
        for (let min = 0; min < 60; min += 30) {
            if (hour === 22 && min === 30) continue;

            const timeStr = `${hour.toString().padStart(2, "0")}:${min === 0 ? "00" : "30"}`;
            const row = createTimeTableRow(timeStr, timeTableData, allUsers);
            tbody.appendChild(row);
        }
    }
    
    table.appendChild(tbody);
    container.innerHTML = "";
    container.appendChild(table);
}

/**
 * 시간표 행 생성
 * @param {string} timeStr - 시간 문자열
 * @param {Array} timeTableData - 시간표 데이터
 * @param {Array} allUsers - 모든 사용자 목록
 * @returns {HTMLElement} 시간표 행
 */
function createTimeTableRow(timeStr, timeTableData, allUsers) {
    const row = document.createElement("tr");
    
    // 시간 셀
    const timeCell = document.createElement("td");
    timeCell.textContent = timeStr;
    row.appendChild(timeCell);

    // 기존 데이터 찾기
    const existingEntry = timeTableData.find(entry => {
        return entry.id.timeSlot.substring(0, 5) === timeStr;
    });

    if (isAdmin) {
        // 테마 셀 (관리자용 입력 필드)
        const themeCell = createThemeCell(existingEntry);
        row.appendChild(themeCell);

        // 출연자 셀 (관리자용 체크박스)
        const performerCell = createPerformerCell(existingEntry, allUsers, timeStr);
        row.appendChild(performerCell);
    } else {
        // 일반 사용자용 (읽기 전용)
        const themeCell = createReadOnlyThemeCell(existingEntry);
        row.appendChild(themeCell);

        const performerCell = createReadOnlyPerformerCell(existingEntry);
        row.appendChild(performerCell);
    }

    // 본인 데이터인지 확인하여 초록색 표시
    if (currentUser && existingEntry && existingEntry.performer) {
        const performerNames = existingEntry.performer.split(',').map(name => name.trim());
        const isMyData = performerNames.includes(currentUser.userName);
        if (isMyData) {
            row.classList.add('my-time-table-row');
        }
    }

    return row;
}

/**
 * 테마 셀 생성 (관리자용)
 * @param {Object} existingEntry - 기존 데이터
 * @returns {HTMLElement} 테마 셀
 */
function createThemeCell(existingEntry) {
    const themeCell = document.createElement("td");
    const themeInput = document.createElement("input");
    themeInput.type = "text";
    themeInput.classList.add("theme-input");
    
    if (existingEntry) {
        themeInput.value = existingEntry.theme || "";
    }
    
    themeCell.appendChild(themeInput);
    return themeCell;
}

/**
 * 출연자 셀 생성 (관리자용)
 * @param {Object} existingEntry - 기존 데이터
 * @param {Array} allUsers - 모든 사용자 목록
 * @param {string} timeStr - 시간 문자열
 * @returns {HTMLElement} 출연자 셀
 */
function createPerformerCell(existingEntry, allUsers, timeStr) {
    const performerCell = document.createElement("td");
    performerCell.classList.add("performer-cell");

    // 기존 출연자 파싱
    const existingPerformers = existingEntry && existingEntry.performer
        ? existingEntry.performer.split(',').map(name => name.trim())
        : [];

    // 사용자별 체크박스 생성
    allUsers.forEach(user => {
        const checkboxDiv = document.createElement("div");
        checkboxDiv.classList.add("performer-item");

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.value = user.userName;
        checkbox.id = `performer-${timeStr}-${user.userName}`;
        checkbox.classList.add("performer-checkbox");

        if (existingPerformers.includes(user.userName)) {
            checkbox.checked = true;
        }

        const label = document.createElement("label");
        label.htmlFor = checkbox.id;
        label.textContent = user.userName;

        checkboxDiv.appendChild(checkbox);
        checkboxDiv.appendChild(label);
        performerCell.appendChild(checkboxDiv);
    });

    return performerCell;
}

/**
 * 읽기 전용 테마 셀 생성
 * @param {Object} existingEntry - 기존 데이터
 * @returns {HTMLElement} 테마 셀
 */
function createReadOnlyThemeCell(existingEntry) {
    const themeCell = document.createElement("td");
    themeCell.textContent = existingEntry ? (existingEntry.theme || "") : "";
    return themeCell;
}

/**
 * 읽기 전용 출연자 셀 생성
 * @param {Object} existingEntry - 기존 데이터
 * @returns {HTMLElement} 출연자 셀
 */
function createReadOnlyPerformerCell(existingEntry) {
    const performerCell = document.createElement("td");
    performerCell.textContent = existingEntry ? (existingEntry.performer || "") : "";
    return performerCell;
}

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 현재 사용자 정보 로드
    loadCurrentUser().then(() => {
        fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
    });
    
    // 모바일 터치 제스처 지원 추가
    if ('ontouchstart' in window) {
        addTouchGestures();
    }
});

/**
 * 현재 사용자 정보 로드
 */
async function loadCurrentUser() {
    try {
        const response = await fetch('/api/dates/current-user');
        currentUser = await response.json();
        console.log('현재 사용자:', currentUser);
    } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
    }
}

/**
 * 모바일 터치 제스처 추가 (데스크톱에서만 활성화)
 */
function addTouchGestures() {
    // 모바일에서만 스와이프 제스처 비활성화 (달력 크기 문제로 인해)
    if (window.innerWidth <= 768) {
        console.log('모바일 환경에서 스와이프 제스처 비활성화');
        return;
    }
    
    let startX = 0;
    let startY = 0;
    let endX = 0;
    let endY = 0;
    
    const calendar = document.querySelector('.calendar');
    if (!calendar) return;
    
    // 터치 시작
    calendar.addEventListener('touchstart', function(e) {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
    }, { passive: true });
    
    // 터치 종료
    calendar.addEventListener('touchend', function(e) {
        endX = e.changedTouches[0].clientX;
        endY = e.changedTouches[0].clientY;
        
        handleSwipe();
    }, { passive: true });
    
    // 스와이프 처리
    function handleSwipe() {
        const diffX = startX - endX;
        const diffY = startY - endY;
        
        // 최소 스와이프 거리 (50px)
        const minSwipeDistance = 50;
        
        // 가로 스와이프가 세로 스와이프보다 클 때만 처리
        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > minSwipeDistance) {
            if (diffX > 0) {
                // 왼쪽으로 스와이프 = 다음 달
                changeMonth(1);
            } else {
                // 오른쪽으로 스와이프 = 이전 달
                changeMonth(-1);
            }
        }
    }
}
