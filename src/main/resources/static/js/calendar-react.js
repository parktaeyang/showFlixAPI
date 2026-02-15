/**
 * React 기반 캘린더 컴포넌트
 */

const { useState, useEffect, useRef } = React;

/**
 * 사용자 드롭다운 메뉴 컴포넌트
 */
function UserDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [userName, setUserName] = useState('');
  const dropdownRef = useRef(null);

  // 컴포넌트 마운트 시 사용자 정보 가져오기
  useEffect(() => {
    // 세션에서 사용자 정보 확인 (서버에서 제공하는 API가 있다면 사용)
    // 현재는 세션 정보를 직접 가져올 수 없으므로, 간단한 방법으로 처리
    fetch('/api/user/info')
      .then(res => res.json())
      .then(data => {
        if (data.isAdmin !== undefined) setIsAdmin(data.isAdmin);
        if (data.username) setUserName(data.username);
      })
      .catch(() => {
        // API가 없으면 기본값 사용
        console.log('사용자 정보 API가 없습니다.');
      });
  }, []);

  // 외부 클릭 시 드롭다운 닫기
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  const handleLogout = () => {
    // 로그아웃 처리
    if (confirm('로그아웃 하시겠습니까?')) {
      // POST 요청으로 로그아웃
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = '/logout';
      document.body.appendChild(form);
      form.submit();
    }
  };

  return React.createElement('div', { 
    className: 'user-menu-container',
    ref: dropdownRef
  },
    React.createElement('div', { className: 'dropdown' },
      React.createElement('button', {
        className: 'btn user-menu-button',
        type: 'button',
        id: 'userDropdown',
        onClick: toggleDropdown,
        'aria-expanded': isOpen,
        'aria-label': 'User Menu'
      },
        React.createElement('i', { className: 'fas fa-user-circle' })
      ),
      isOpen && React.createElement('ul', {
        className: 'dropdown-menu dropdown-menu-end',
        'aria-labelledby': 'userDropdown'
      },
        isAdmin && React.createElement('li', null,
          React.createElement('a', {
            className: 'dropdown-item',
            href: '/admin'
          },
            React.createElement('i', { className: 'fas fa-cogs me-2' }),
            ' 관리자 페이지'
          )
        ),
        React.createElement('li', null,
          React.createElement('a', {
            className: 'dropdown-item',
            href: '#',
            onClick: (e) => {
              e.preventDefault();
              alert('내 정보 관리 기능은 추후 구현 예정입니다.');
            }
          },
            React.createElement('i', { className: 'fas fa-user-edit me-2' }),
            ' 내 정보 관리'
          )
        ),
        React.createElement('li', null,
          React.createElement('hr', { className: 'dropdown-divider' })
        ),
        React.createElement('li', null,
          React.createElement('a', {
            className: 'dropdown-item',
            href: '#',
            onClick: (e) => {
              e.preventDefault();
              handleLogout();
            }
          },
            React.createElement('i', { className: 'fas fa-sign-out-alt me-2' }),
            ' 로그아웃'
          )
        )
      )
    )
  );
}

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
  return React.createElement(React.Fragment, null,
    React.createElement(UserDropdown, null),
    React.createElement('div', { className: 'calendar-container' },
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
