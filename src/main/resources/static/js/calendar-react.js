/**
 * React 기반 캘린더 컴포넌트
 */

const { useState } = React;

/**
 * 캘린더 메인 컴포넌트
 */
function CalendarPage() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDates, setSelectedDates] = useState(new Set());

  // 현재 년월 추출
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth(); // 0-11

  /**
   * 이전달 이동
   */
  const goToPreviousMonth = () => {
    setCurrentDate(new Date(year, month - 1, 1));
  };

  /**
   * 다음달 이동
   */
  const goToNextMonth = () => {
    setCurrentDate(new Date(year, month + 1, 1));
  };

  /**
   * 날짜 체크박스 토글
   */
  const toggleDate = (dateStr) => {
    const newSelected = new Set(selectedDates);
    if (newSelected.has(dateStr)) {
      newSelected.delete(dateStr);
    } else {
      newSelected.add(dateStr);
    }
    setSelectedDates(newSelected);
  };

  /**
   * 선택된 날짜 저장
   */
  const saveSelections = async () => {
    if (selectedDates.size === 0) {
      alert("저장할 날짜를 선택해주세요.");
      return;
    }

    try {
      // TODO: 실제 API 엔드포인트로 변경 필요
      // await axios.post("/api/dates/save", Array.from(selectedDates));
      
      alert("저장 완료");
      
      // 저장 후 선택 초기화 (선택사항)
      // setSelectedDates(new Set());
    } catch (error) {
      console.error("저장 실패:", error);
      alert("저장 실패: " + (error.response?.data?.message || error.message || "알 수 없는 오류"));
    }
  };

  /**
   * 캘린더 그리드 생성
   */
  const generateCalendar = () => {
    const firstDay = new Date(year, month, 1);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - startDate.getDay()); // 일요일로 맞춤

    const days = [];
    const current = new Date(startDate);

    // 6주 * 7일 = 42일
    for (let i = 0; i < 42; i++) {
      const dateStr = formatDateString(
        current.getFullYear(),
        current.getMonth() + 1,
        current.getDate()
      );
      
      const isCurrentMonth = current.getMonth() === month;
      const isToday = isSameDay(current, new Date());

      days.push({
        date: current.getDate(),
        dateStr,
        isCurrentMonth,
        isToday,
        fullDate: new Date(current)
      });

      current.setDate(current.getDate() + 1);
    }

    return days;
  };

  /**
   * 날짜 문자열 포맷 (YYYY-MM-DD)
   */
  const formatDateString = (year, month, date) => {
    return `${year}-${month.toString().padStart(2, '0')}-${date.toString().padStart(2, '0')}`;
  };

  /**
   * 같은 날인지 확인
   */
  const isSameDay = (date1, date2) => {
    return (
      date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
    );
  };

  /**
   * 년월 표시 문자열
   */
  const getMonthYearString = () => {
    return `${year}년 ${month + 1}월`;
  };

  const calendarDays = generateCalendar();
  const weekDays = ['일', '월', '화', '수', '목', '금', '토'];

  // React.createElement를 사용하여 JSX 대신 작성
  return React.createElement('div', { className: 'calendar-container' },
    React.createElement('div', { className: 'calendar' },
      // 캘린더 헤더
      React.createElement('div', { className: 'calendar-header' },
        React.createElement('button', { onClick: goToPreviousMonth }, '◀ 이전달'),
        React.createElement('h2', { id: 'monthYear' }, getMonthYearString()),
        React.createElement('button', { onClick: goToNextMonth }, '다음달 ▶')
      ),
      // 캘린더 테이블
      React.createElement('table', { className: 'calendar-table' },
        React.createElement('thead', null,
          React.createElement('tr', null,
            weekDays.map((day) => React.createElement('th', { key: day }, day))
          )
        ),
        React.createElement('tbody', null,
          Array.from({ length: 6 }).map((_, weekIndex) =>
            React.createElement('tr', { key: weekIndex },
              calendarDays.slice(weekIndex * 7, (weekIndex + 1) * 7).map((day) =>
                React.createElement('td', {
                  key: day.dateStr,
                  style: {
                    opacity: day.isCurrentMonth ? 1 : 0.3,
                    backgroundColor: day.isToday ? 'rgba(139, 92, 246, 0.1)' : 'transparent'
                  }
                },
                  React.createElement('span', { className: 'date-number' }, day.date),
                  day.isCurrentMonth && React.createElement('div', { className: 'checkbox-wrap' },
                    React.createElement('input', {
                      type: 'checkbox',
                      className: 'date-checkbox',
                      checked: selectedDates.has(day.dateStr),
                      onChange: () => toggleDate(day.dateStr)
                    })
                  )
                )
              )
            )
          )
        )
      ),
      // 저장 버튼
      React.createElement('button', { className: 'save-btn', onClick: saveSelections },
        '✅ 저장하기'
      )
    ),
    // 출근자 섹션
    React.createElement('div', { className: 'attendee-section' },
      React.createElement('div', { className: 'attendee-header' },
        React.createElement('h3', { className: 'attendee-title' }, '이번 달 출근자'),
        React.createElement('span', { className: 'attendee-month' }, getMonthYearString())
      ),
      React.createElement('div', { className: 'attendee-empty' },
        '이번 달 출근 예정자가 없습니다.'
      )
    )
  );
}

// React 렌더링 - DOM이 준비된 후 실행
document.addEventListener('DOMContentLoaded', () => {
  const root = document.getElementById('root');
  if (root) {
    const rootElement = ReactDOM.createRoot
      ? ReactDOM.createRoot(root)
      : { render: (el) => ReactDOM.render(el, root) };

    rootElement.render(
      React.createElement(CalendarPage, null)
    );
  }
});
