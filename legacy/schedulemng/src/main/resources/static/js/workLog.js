let currentYear = new Date().getFullYear();
let currentMonth = new Date().getMonth() + 1;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 현재 년월을 기본값으로 설정
    const now = new Date();
    const currentYearValue = now.getFullYear();
    const currentMonthValue = now.getMonth() + 1;

    // 년도 선택
    const yearSelect = document.getElementById('yearSelect');
    yearSelect.value = currentYearValue;
    currentYear = currentYearValue;

    // 월 선택
    const monthSelect = document.getElementById('monthSelect');
    monthSelect.value = currentMonthValue;
    currentMonth = currentMonthValue;

    loadWorkLog();
});

/**
 * 업무 일지 데이터 로드
 */
async function loadWorkLog() {
    currentYear = parseInt(document.getElementById('yearSelect').value);
    currentMonth = parseInt(document.getElementById('monthSelect').value);
    const tableBody = document.getElementById('workLogTableBody');

    tableBody.innerHTML = ''; // 테이블 내용 초기화

    try {
        // axios를 사용하면 JSON 파싱과 에러 처리가 더 간결해집니다.
        const response = await fetch(`/api/work-logs?year=${currentYear}&month=${currentMonth}`);

        if (!response.ok) {
            const errorResult = await response.json().catch(() => ({ message: '서버 응답이 올바르지 않습니다.'}));
            throw new Error(errorResult.message);
        }

        const result = await response.json();
        const workLogs = result.data;

        if (!result.success || !workLogs || workLogs.length == 0) {
            // 데이터가 없을 경우, 테이블에 메시지 행을 추가
            const noDataRow = `
                <tr>
                    <td colspan="8" class="text-center p-3">등록된 업무일지가 존재하지 않습니다.</td>
                </tr>
            `;
            tableBody.innerHTML = noDataRow;
        } else {
            // 데이터가 있을 경우, 테이블 행을 생성합니다.
            workLogs.forEach(log => {
                const row = createDisplayRow(log);
                tableBody.appendChild(row);
            });
        };
    } catch (error) {
        console.error('데이터 로드 중 오류:', error);
        const errorMessage = error.message || '데이터를 불러오는 중 오류가 발생했습니다.';
        tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center p-3 text-danger">${errorMessage}</td>
            </tr>
        `;
        alert(errorMessage);
    }
}

/**
 * 새로운 입력 행 추가
 */
function addRow() {
    const tableBody = document.getElementById('workLogTableBody');
    // 이미 추가된 입력 행이 있는지 확인
    if (document.querySelector('.new-row')) {
        alert('이미 추가 중인 행이 있습니다. 먼저 저장하거나 취소해주세요.');
        return;
    }

    const newRow = document.createElement('tr');
    newRow.classList.add('new-row');

    newRow.innerHTML = `
         <td><input type="date" class="form-control form-control-sm" value="${new Date().toISOString().slice(0, 10)}"></td>
         <td><input type="text" class="form-control form-control-sm" placeholder="담당자"></td>
         <td><textarea class="form-control" rows="4"></textarea></td>
         <td><textarea class="form-control" rows="4"></textarea></td>
         <td><textarea class="form-control" rows="4"></textarea></td>
         <td><textarea class="form-control" rows="4"></textarea></td>
         <td><textarea class="form-control" rows="4"></textarea></td>
         <td>
             <button class="btn btn-success btn-sm" onclick="saveNewRow(this)">저장</button>
             <button class="btn btn-danger btn-sm" onclick="this.closest('tr').remove()">취소</button>
         </td>
     `;
    // 데이터 없음 메시지가 있다면 지우고 추가
    if (tableBody.querySelector('td[colspan="8"]')) {
        tableBody.innerHTML = '';
    }
    tableBody.appendChild(newRow);
}

/**
 * 새로 추가된 행을 서버에 저장
 * @param {HTMLButtonElement} buttonEl - 클릭된 저장 버튼
 */
async function saveNewRow(buttonEl) {
    const row = buttonEl.closest('tr');
    const inputs = row.querySelectorAll('input, textarea');

    const payload = {
        date: inputs[0].value,
        manager: inputs[1].value,
        cashPayment: inputs[2].value,
        reservations: inputs[3].value,
        event: inputs[4].value,
        storeRelated: inputs[5].value,
        notes: inputs[6].value,
    };

    if (!payload.date) {
        alert('날짜는 필수 항목입니다.');
        return;
    }

    try {
        const response = await fetch('/api/work-logs/save-workLog', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorResult = await response.json().catch(() => ({ message: '서버 응답이 올바르지 않습니다.'}));
            throw new Error(errorResult.message);
        }

        const result = await response.json();
        const savedLog = result.data;

        // 입력 행을 표시 행으로 교체
        const newDisplayRow = createDisplayRow(savedLog);
        row.replaceWith(newDisplayRow);
        alert('업무일지가 성공적으로 등록되었습니다.');

        loadWorkLog();
    } catch (error) {
        console.error('업무일지 저장 중 오류 발생:', error);
        const errorMessage = error.message || '저장에 실패했습니다.';
        alert(errorMessage);
    }
}

/**
 * 표시용 행을 생성하는 헬퍼 함수
 */
function createDisplayRow(log) {
    const row = document.createElement('tr');
    row.dataset.id = log.id;
    row.dataset.date = log.date;

    // yyyy-MM-dd 형식에서 MM/dd 만 표시
    const displayDate = log.date.substring(5).replace('-', '/');

    row.innerHTML = `
         <td>${displayDate}</td>
         <td>${log.manager || ''}</td>
         <td style="white-space: pre-wrap;">${log.cashPayment || ''}</td>
         <td style="white-space: pre-wrap;">${log.reservations || ''}</td>
         <td style="white-space: pre-wrap;">${log.event || ''}</td>
         <td style="white-space: pre-wrap;">${log.storeRelated || ''}</td>
         <td style="white-space: pre-wrap;">${log.notes || ''}</td>
         <td>
             <button class="btn btn-success btn-sm" onclick="editRow(this)">수정</button>
             <button class="btn btn-danger btn-sm" onclick="deleteRow(this)">삭제</button>
         </td>
     `;
    return row;
}

/**
 * 기존 행을 수정모드로 변경
 */
function editRow(buttonEl) {
    if (document.querySelector('.edit-mode')) {
        alert('이미 수정 중인 행이 있습니다.');
        return;
    }

    const row = buttonEl.closest('tr');
    row.classList.add('edit-mode');
    const originalDate = row.dataset.date;

    const cells = row.querySelectorAll('td');
    const originalData = {
        manager: cells[1].textContent,
        cashPayment: cells[2].textContent,
        reservations: cells[3].textContent,
        event: cells[4].textContent,
        storeRelated: cells[5].textContent,
        notes: cells[6].textContent,
    }

    // 각 셀을 입력 필드로 변경
    cells[0].innerHTML = `<input type="date" class="form-control form-control-sm" value="${originalDate}">`;
    cells[1].innerHTML = `<input type="text" class="form-control form-control-sm" value="${originalData.manager}">`;
    cells[2].innerHTML = `<textarea class="form-control" rows="4">${originalData.cashPayment}</textarea>`;
    cells[3].innerHTML = `<textarea class="form-control" rows="4">${originalData.reservations}</textarea>`;
    cells[4].innerHTML = `<textarea class="form-control" rows="4">${originalData.event}</textarea>`;
    cells[5].innerHTML = `<textarea class="form-control" rows="4">${originalData.storeRelated}</textarea>`;
    cells[6].innerHTML = `<textarea class="form-control" rows="4">${originalData.notes}</textarea>`;

    // 작업 버튼 변경
    cells[7].innerHTML = `
         <button class="btn btn-success btn-sm" onclick="updateRow(this)">저장</button>
         <button class="btn btn-danger btn-sm" onclick="cancelEdit(this)">취소</button>
     `;
}

/**
 * 수정된 행의 내용을 서버에 저장
 */
async function updateRow(buttonEl) {
    const row = buttonEl.closest('tr');
    const id = row.dataset.id;
    const inputs = row.querySelectorAll('input, textarea');

    const payload = {
        id: id,
        date: inputs[0].value,
        manager: inputs[1].value,
        cashPayment: inputs[2].value,
        reservations: inputs[3].value,
        event: inputs[4].value,
        storeRelated: inputs[5].value,
        notes: inputs[6].value,
    };

    try {
        const response = await fetch(`/api/work-logs/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorResult = await response.json().catch(() => ({ message: '서버 응답이 올바르지 않습니다.'}));
            throw new Error(errorResult.message);
        }

        const result = await response.json();
        const updatedLog = result.data;

        const newDisplayRow = createDisplayRow(updatedLog);
        row.replaceWith(newDisplayRow);
        alert('업무일지가 수정되었습니다.');

        loadWorkLog();
    } catch (error) {
        console.error('업무일지 수정 중 오류 발생:', error);
        alert(error.message || '수정에 실패했습니다.');
    }
}

/**
 * 특정 업무일지 삭제
 */
async function deleteRow(buttonEl) {
    const row = buttonEl.closest('tr');
    const id = row.dataset.id;

    if (confirm('정말로 이 업무일지를 삭제하시겠습니까?')) {
        try {
            const response = await fetch(`/api/work-logs/${id}`, { method: 'DELETE' });
            if (!response.ok) {
                const errorResult = await response.json().catch(() => ({ message: '서버 응답이 올바르지 않습니다.'}));
                throw new Error(errorResult.message);
            }
            row.remove();
            alert('업무일지가 삭제되었습니다.');

            loadWorkLog();
        } catch (error) {
            console.error('업무일지 삭제 중 오류 발생:', error);
            alert(error.message || '삭제에 실패했습니다.');
        }
    }
}

/**
 * 행 수정을 취소하고 원래 상태로 복원
 * 전체 목록 리로드
 */
function cancelEdit(buttonEl) {
    loadWorkLog();
}

function exportToExcel() {
    alert('엑셀 내보내기 기능은 준비 중입니다.');
}