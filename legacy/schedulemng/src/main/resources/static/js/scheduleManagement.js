let scheduleData = null;
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
    
    loadScheduleTable();
});

// 스케줄 테이블 로드
async function loadScheduleTable() {
    currentYear = parseInt(document.getElementById('yearSelect').value);
    currentMonth = parseInt(document.getElementById('monthSelect').value);

    try {
        const response = await fetch(`/api/schedule-summary/schedule-table?year=${currentYear}&month=${currentMonth}`);
        const result = await response.json();

        if (result.success) {
            scheduleData = result.data;
            generateScheduleTable();
        } else {
            alert('데이터를 불러오는 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('데이터 로드 중 오류:', error);
        alert('데이터를 불러오는 중 오류가 발생했습니다.');
    }
}

// 스케줄 테이블 생성
function generateScheduleTable() {
    const table = document.getElementById('scheduleTable');
    const thead = table.querySelector('thead tr');

    // 기존 배우 헤더 제거 (첫 번째 2개 컬럼 제외)
    while (thead.children.length > 2) {
        thead.removeChild(thead.lastChild);
    }

    // 배우 헤더 추가
    scheduleData.actorNames.forEach(actorName => {
        const th = document.createElement('th');
        th.textContent = actorName;
        th.className = 'actor-column';
        th.title = actorName; // 툴팁 추가
        thead.appendChild(th);
    });

    // 합계 헤더를 배우들 다음으로 이동하고 스타일 적용
    const totalTh = thead.querySelector('th:nth-child(2)');
    totalTh.className = 'total-column-header';
    totalTh.textContent = '합계';
    thead.appendChild(totalTh);

    // 특이사항 헤더 추가 (맨 오른쪽)
    const remarksTh = document.createElement('th');
    remarksTh.textContent = '특이사항';
    remarksTh.className = 'remarks-column';
    thead.appendChild(remarksTh);

    generateTableRows();
    
    // 기존 합계 행이 있으면 제거
    const existingTotalRow = document.getElementById('totalRow');
    if (existingTotalRow) {
        existingTotalRow.remove();
    }
    
    addTotalRow();
    
    // 테이블 생성 완료 후 합계 재계산
    setTimeout(() => {
        updateTotalRow();
    }, 100);
}

// 테이블 행 생성
function generateTableRows() {
    const tbody = document.getElementById('scheduleTableBody');
    tbody.innerHTML = '';

    scheduleData.rows.forEach(rowData => {
        const row = document.createElement('tr');

        // 날짜 셀 (요일 포함)
        const dateCell = document.createElement('td');
        const dayOfWeek = rowData.dayOfWeek;
        const dayNumber = rowData.date.split('-')[2];
        dateCell.textContent = `${dayNumber}일 ${dayOfWeek}`;
        if (dayOfWeek === '토' || dayOfWeek === '일') {
            row.classList.add('weekend');
        }
        row.appendChild(dateCell);

        // 배우별 시간 입력 셀
        scheduleData.actorNames.forEach(actorName => {
            const cell = document.createElement('td');
            cell.className = 'actor-column';

            const input = document.createElement('input');
            input.type = 'number';
            input.className = 'schedule-input';
            input.step = '0.5';
            input.min = '0';
            input.max = '24';
            input.dataset.date = rowData.date;
            input.dataset.username = actorName;
            input.value = rowData.actorHours[actorName] || '';
            input.title = `${actorName} - ${rowData.date}`; // 툴팁 추가
            input.addEventListener('change', updateRowTotal);
            input.addEventListener('blur', saveSchedule);
            cell.appendChild(input);
            row.appendChild(cell);
        });

        // 합계 셀
        const totalCell = document.createElement('td');
        totalCell.className = 'total-column';
        totalCell.textContent = rowData.rowTotal || '0';
        totalCell.style.fontWeight = 'bold';
        totalCell.style.color = '#f57c00';
        row.appendChild(totalCell);

        // 특이사항 셀
        const remarksCell = document.createElement('td');
        remarksCell.className = 'remarks-column';
        const remarksInput = document.createElement('input');
        remarksInput.type = 'text';
        remarksInput.className = 'remarks-input';
        remarksInput.dataset.date = rowData.date;
        remarksInput.value = rowData.remarks || '';
        remarksInput.addEventListener('blur', saveRemarks);
        remarksCell.appendChild(remarksInput);
        row.appendChild(remarksCell);

        tbody.appendChild(row);
    });
}

// 합계 행 추가
function addTotalRow() {
    const tbody = document.getElementById('scheduleTableBody');
    const totalRow = document.createElement('tr');
    totalRow.className = 'total-row';
    totalRow.id = 'totalRow'; // ID 추가

    // "합계" 셀
    const labelCell = document.createElement('td');
    labelCell.textContent = '합계';
    labelCell.style.fontWeight = 'bold';
    labelCell.style.fontSize = '12px';
    labelCell.style.backgroundColor = '#e3f2fd';
    labelCell.style.color = '#1976d2';
    labelCell.style.border = '2px solid #1976d2';
    totalRow.appendChild(labelCell);

    // 배우별 합계 셀 (서버 데이터 반영)
    scheduleData.actorNames.forEach(actorName => {
        const cell = document.createElement('td');
        cell.className = 'total-column actor-column';
        cell.dataset.actorName = actorName; // 데이터 속성 추가
        
        // 서버에서 받아온 합계 데이터 사용
        let columnTotal = '0';
        if (scheduleData.columnTotals && scheduleData.columnTotals[actorName]) {
            columnTotal = scheduleData.columnTotals[actorName];
        }
        cell.textContent = columnTotal;
        cell.title = `${actorName} 총합: ${columnTotal}`; // 툴팁 추가
        cell.style.backgroundColor = '#fff3e0';
        cell.style.color = '#f57c00';
        cell.style.border = '2px solid #f57c00';
        cell.style.fontWeight = 'bold';
        totalRow.appendChild(cell);
    });

    // 전체 합계 셀 (빈 칸으로 변경)
    const grandTotalCell = document.createElement('td');
    grandTotalCell.className = 'grand-total-cell';
    grandTotalCell.textContent = '';
    grandTotalCell.title = '';
    grandTotalCell.style.backgroundColor = '#e3f2fd';
    grandTotalCell.style.color = '#1976d2';
    grandTotalCell.style.border = '2px solid #1976d2';
    grandTotalCell.style.fontWeight = 'bold';
    grandTotalCell.style.fontSize = '12px';
    totalRow.appendChild(grandTotalCell);

    // 특이사항 합계 셀 (빈 셀)
    const emptyCell = document.createElement('td');
    emptyCell.className = 'remarks-column';
    emptyCell.style.backgroundColor = '#e3f2fd';
    emptyCell.style.border = '2px solid #1976d2';
    totalRow.appendChild(emptyCell);

    tbody.appendChild(totalRow);
    
    // 초기 합계 계산 (실시간 업데이트)
    updateTotalRow();
}

// 행 합계 업데이트
function updateRowTotal(event) {
    const row = event.target.closest('tr');
    const inputs = row.querySelectorAll('.schedule-input');
    let total = 0;

    inputs.forEach(input => {
        const value = parseFloat(input.value) || 0;
        total += value;
    });

    // 합계 셀 찾기 (배우 수 + 2개 고정 컬럼)
    const totalCellIndex = scheduleData.actorNames.length + 2;
    const totalCell = row.querySelector(`td:nth-child(${totalCellIndex})`);
    if (totalCell) {
        totalCell.textContent = total.toFixed(1);
    } else {
        // 대안: 클래스로 찾기
        const totalCellByClass = row.querySelector('.total-column');
        if (totalCellByClass) {
            totalCellByClass.textContent = total.toFixed(1);
        }
    }

    // 맨 아래 합계 행도 실시간 업데이트
    updateTotalRow();
}

// 합계 행 실시간 업데이트 함수
function updateTotalRow() {
    const totalRow = document.getElementById('totalRow');
    if (!totalRow) {
        return;
    }

    // 배우별 합계 계산
    scheduleData.actorNames.forEach(actorName => {
        let columnTotal = 0;
        const inputs = document.querySelectorAll(`input[data-username="${actorName}"]`);
        
        inputs.forEach(input => {
            const value = parseFloat(input.value) || 0;
            columnTotal += value;
        });

        // 해당 배우의 합계 셀 업데이트
        const totalCell = totalRow.querySelector(`td[data-actor-name="${actorName}"]`);
        if (totalCell) {
            totalCell.textContent = columnTotal.toFixed(1);
            totalCell.title = `${actorName} 총합: ${columnTotal.toFixed(1)}`;
        }
    });

    // 전체 합계 셀 업데이트 (빈 칸으로 유지)
    const grandTotalCell = totalRow.querySelector('.grand-total-cell');
    if (grandTotalCell) {
        grandTotalCell.textContent = '';
        grandTotalCell.title = '';
    }
}

// 개별 스케줄 저장
async function saveSchedule(event) {
    const input = event.target;
    const date = input.dataset.date;
    const username = input.dataset.username;
    const hours = parseFloat(input.value) || 0;

    try {
        const response = await fetch('/api/schedule-summary/save-schedule', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                date: date,
                username: username,
                hours: hours,
                remarks: ''
            })
        });

        const result = await response.json();
        if (result.success) {
            console.log('스케줄이 저장되었습니다.');
        }
    } catch (error) {
        console.error('스케줄 저장 중 오류:', error);
    }
}

// 특이사항 저장
async function saveRemarks(event) {
    const input = event.target;
    const date = input.dataset.date;
    const remarks = input.value;

    try {
        const response = await fetch('/api/schedule-summary/update-daily-remarks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                date: date,
                remarks: remarks
            })
        });

        const result = await response.json();
        if (result.success) {
            console.log('특이사항이 저장되었습니다.');
        }
    } catch (error) {
        console.error('특이사항 저장 중 오류:', error);
    }
}

// 전체 스케줄 저장
async function saveAllSchedules() {
    const inputs = document.querySelectorAll('.schedule-input');
    let savedCount = 0;

    for (const input of inputs) {
        const hours = parseFloat(input.value) || 0;
        if (hours > 0) {
            await saveSchedule({ target: input });
            savedCount++;
        }
    }

    alert(`${savedCount}개의 스케줄이 저장되었습니다.`);
    
    // 전체 저장 후에는 테이블을 새로고침하여 서버 데이터와 동기화
    setTimeout(() => {
        loadScheduleTable();
        // 새로고침 후 합계 다시 계산
        setTimeout(() => updateTotalRow(), 100);
    }, 1000);
}

// 엑셀 내보내기
function exportToExcel() {
    // CSV 헤더 생성
    let csv = '날짜,요일,';
    scheduleData.actorNames.forEach(actorName => {
        csv += actorName + ',';
    });
    csv += '합계,특이사항\n';

    // CSV 데이터 생성
    scheduleData.rows.forEach(row => {
        csv += `${row.date.split('-')[2]}일,${row.dayOfWeek},`;
        scheduleData.actorNames.forEach(actorName => {
            csv += (row.actorHours[actorName] || '') + ',';
        });
        csv += `${row.rowTotal || 0},${row.remarks || ''}\n`;
    });

    // BOM (Byte Order Mark) 추가하여 한글 인코딩 문제 해결
    const BOM = '\uFEFF';
    const csvWithBOM = BOM + csv;
    
    // UTF-8 BOM이 포함된 Blob 생성
    const blob = new Blob([csvWithBOM], { 
        type: 'text/csv;charset=utf-8;' 
    });
    
    // 다운로드 링크 생성
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `스케줄_${currentYear}년${currentMonth}월.csv`;
    
    // 링크 클릭하여 다운로드 실행
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // 메모리 정리
    URL.revokeObjectURL(link.href);
} 