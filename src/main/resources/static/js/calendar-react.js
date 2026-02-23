/**
 * calendar-react.js
 * CDN React (no JSX) 기반 달력 컴포넌트
 *
 * 핵심 흐름:
 *  - 체크박스 선택 + 저장하기  → selected_date 저장 (일반/관리자 공통)
 *  - 날짜 셀 클릭 (관리자 전용) → 어드민 팝업: 출근자 역할/비고 + 시간표 확정
 */

const e = React.createElement;

// ────────────────────────────────────────────────────────────────────
// 유틸
// ────────────────────────────────────────────────────────────────────
function pad2(n) { return String(n).padStart(2, '0'); }

function toDateStr(year, month, day) {
    return `${year}-${pad2(month)}-${pad2(day)}`;
}

function getDaysInMonth(year, month) {
    return new Date(year, month, 0).getDate();
}

// 해당 월 1일의 요일 (0=일,1=월,...,6=토)
function getFirstDayOfWeek(year, month) {
    return new Date(year, month - 1, 1).getDay();
}

const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'];

// 기본 시간표 슬롯
const DEFAULT_TIME_SLOTS = [
    '12:00', '12:30', '13:00', '13:30',
    '14:00', '14:30', '15:00', '15:30',
    '16:00', '16:30', '17:00', '17:30',
    '18:00', '18:30', '19:00', '19:30',
    '20:00', '20:30', '21:00',
];

// ────────────────────────────────────────────────────────────────────
// 사용자 드롭다운
// ────────────────────────────────────────────────────────────────────
function UserDropdown({ userName, isAdmin }) {
    const [open, setOpen] = React.useState(false);

    function handleLogout() {
        fetch('/logout', { method: 'POST', credentials: 'same-origin' })
            .then(() => { window.location.href = '/'; });
    }

    function handleAdminPage() {
        window.location.href = '/admin/';
    }

    return e('div', { className: 'user-dropdown', style: { position: 'relative' } },
        e('button', {
            className: 'user-btn',
            onClick: () => setOpen(v => !v)
        }, userName || '사용자', ' ▾'),
        open && e('div', { className: 'dropdown-menu' },
            isAdmin &&
            e('button', { className: 'dropdown-item', onClick: handleAdminPage }, '관리자페이지'),
            e('button', { className: 'dropdown-item', onClick: handleLogout }, '로그아웃')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// 관리자 팝업
// ────────────────────────────────────────────────────────────────────
function AdminPopup({ date, attendees, roleOptions, onClose, onSaved }) {
    // attendees: SelectedDateResponse[] for this date
    // 각 attendee: { date, userId, userName, role, confirmed, remarks }
    // roleOptions: [{ value: "DOOR", label: "도어" }, ...]

    const [roleMap, setRoleMap] = React.useState(() => {
        const m = {};
        attendees.forEach(a => { m[a.userId] = a.role || ''; });
        return m;
    });
    const [remarksMap, setRemarksMap] = React.useState(() => {
        const m = {};
        attendees.forEach(a => { m[a.userId] = a.remarks || ''; });
        return m;
    });

    const [slots, setSlots] = React.useState([]);
    const [slotLoading, setSlotLoading] = React.useState(true);
    // slotChecks: { [timeSlot]: { [userId]: boolean } }
    const [slotChecks, setSlotChecks] = React.useState({});

    const [saving, setSaving] = React.useState(false);
    const [confirming, setConfirming] = React.useState(false);
    const [msg, setMsg] = React.useState('');

    // 시간표 로드
    React.useEffect(() => {
        setSlotLoading(true);
        fetch(`/api/schedule/dates/time-slots?date=${date}`, { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : [])
            .then(data => {
                const initialChecks = {};
                DEFAULT_TIME_SLOTS.forEach(ts => {
                    const found = data.find(d => d.timeSlot === ts);
                    initialChecks[ts] = {};
                    attendees.forEach(a => {
                        if (found && found.performer) {
                            initialChecks[ts][a.userId] = found.performer.split(',').map(s => s.trim()).includes(a.userName);
                        } else {
                            initialChecks[ts][a.userId] = false;
                        }
                    });
                });
                setSlots(data);
                setSlotChecks(initialChecks);
            })
            .catch(() => {
                const initialChecks = {};
                DEFAULT_TIME_SLOTS.forEach(ts => {
                    initialChecks[ts] = {};
                    attendees.forEach(a => { initialChecks[ts][a.userId] = false; });
                });
                setSlotChecks(initialChecks);
            })
            .finally(() => setSlotLoading(false));
    }, [date]);

    function handleRoleChange(userId, val) {
        setRoleMap(m => ({ ...m, [userId]: val }));
    }
    function handleRemarksChange(userId, val) {
        setRemarksMap(m => ({ ...m, [userId]: val }));
    }
    function handleSlotCheck(timeSlot, userId, checked) {
        setSlotChecks(prev => ({
            ...prev,
            [timeSlot]: { ...(prev[timeSlot] || {}), [userId]: checked }
        }));
    }

    async function handleSaveRoles() {
        setSaving(true);
        setMsg('');
        try {
            const body = attendees.map(a => ({
                date,
                userId: a.userId,
                role: roleMap[a.userId] || '',
                remarks: remarksMap[a.userId] || ''
            }));
            const r = await fetch('/api/schedule/dates/roles/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(body)
            });
            if (!r.ok) throw new Error('저장 실패');
            setMsg('역할/비고 저장 완료');
        } catch (err) {
            setMsg(err.message || '저장 실패');
        } finally {
            setSaving(false);
        }
    }

    async function handleConfirm() {
        setConfirming(true);
        setMsg('');
        try {
            // 1) 시간표 슬롯 저장
            const slotsPayload = DEFAULT_TIME_SLOTS
                .map(ts => {
                    const checkedUsers = attendees
                        .filter(a => slotChecks[ts] && slotChecks[ts][a.userId])
                        .map(a => a.userName);
                    return { timeSlot: ts, theme: '', performer: checkedUsers.join(',') };
                })
                .filter(s => s.performer !== '');

            const r1 = await fetch('/api/schedule/dates/time-slots/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ date, slots: slotsPayload })
            });
            if (!r1.ok) throw new Error('시간표 저장 실패');

            // 2) 역할/비고 저장
            const rolesBody = attendees.map(a => ({
                date,
                userId: a.userId,
                role: roleMap[a.userId] || '',
                remarks: remarksMap[a.userId] || ''
            }));
            const r2 = await fetch('/api/schedule/dates/roles/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(rolesBody)
            });
            if (!r2.ok) throw new Error('역할 저장 실패');

            // 3) 확정
            const r3 = await fetch('/api/schedule/dates/confirm', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ date, confirmed: 'Y' })
            });
            if (!r3.ok) throw new Error('확정 처리 실패');

            setMsg('✓ 스케줄 확정 완료!');
            if (onSaved) onSaved();
        } catch (err) {
            setMsg(err.message || '오류 발생');
        } finally {
            setConfirming(false);
        }
    }

    function handleOverlayClick(ev) {
        if (ev.target === ev.currentTarget) onClose();
    }

    return e('div', { className: 'popup-overlay', onClick: handleOverlayClick },
        e('div', { className: 'popup-box admin-popup' },

            // 헤더
            e('div', { className: 'popup-header' },
                e('h3', { className: 'popup-title' }, `📅 ${date} 스케줄 관리`),
                e('button', { className: 'popup-close', onClick: onClose }, '✕')
            ),

            e('div', { className: 'popup-body' },

                // 출근자 목록
                e('section', { className: 'admin-section' },
                    e('h4', { className: 'section-title' }, '출근자 역할 & 비고'),
                    attendees.length === 0
                        ? e('p', { className: 'no-data' }, '등록된 출근자가 없습니다.')
                        : e('div', { className: 'attendee-table-wrap' },
                            e('table', { className: 'attendee-table' },
                                e('thead', null,
                                    e('tr', null,
                                        e('th', null, '이름'),
                                        e('th', null, '역할'),
                                        e('th', null, '비고')
                                    )
                                ),
                                e('tbody', null,
                                    attendees.map(a =>
                                        e('tr', { key: a.userId },
                                            e('td', { className: 'attendee-name' }, a.userName),
                                            e('td', null,
                                                e('select', {
                                                    className: 'role-select',
                                                    value: roleMap[a.userId] || '',
                                                    onChange: ev => handleRoleChange(a.userId, ev.target.value)
                                                },
                                                    e('option', { value: '' }, '-- 선택 --'),
                                                    ...(roleOptions || []).map(opt =>
                                                        e('option', { key: opt.value, value: opt.value }, opt.label)
                                                    )
                                                )
                                            ),
                                            e('td', null,
                                                e('input', {
                                                    type: 'text',
                                                    className: 'remarks-input',
                                                    value: remarksMap[a.userId] || '',
                                                    onChange: ev => handleRemarksChange(a.userId, ev.target.value),
                                                    placeholder: '비고'
                                                })
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                    e('div', { className: 'section-action' },
                        e('button', {
                            className: 'popup-btn popup-btn-secondary',
                            onClick: handleSaveRoles,
                            disabled: saving || attendees.length === 0
                        }, saving ? '저장중...' : '역할/비고 저장')
                    )
                ),

                e('hr', { className: 'section-divider' }),

                // 시간표
                e('section', { className: 'admin-section' },
                    e('h4', { className: 'section-title' }, '시간표 (출연 체크 → 확정)'),
                    slotLoading
                        ? e('p', { className: 'no-data' }, '불러오는 중...')
                        : attendees.length === 0
                            ? e('p', { className: 'no-data' }, '출근자를 먼저 등록하세요.')
                            : e('div', { className: 'time-slot-table-wrap' },
                                e('table', { className: 'time-slot-table' },
                                    e('thead', null,
                                        e('tr', null,
                                            e('th', { className: 'time-col' }, '시간'),
                                            ...attendees.map(a =>
                                                e('th', { key: a.userId, className: 'person-col' }, a.userName)
                                            )
                                        )
                                    ),
                                    e('tbody', null,
                                        DEFAULT_TIME_SLOTS.map(ts =>
                                            e('tr', { key: ts },
                                                e('td', { className: 'time-cell' }, ts),
                                                ...attendees.map(a =>
                                                    e('td', { key: a.userId, className: 'check-cell' },
                                                        e('input', {
                                                            type: 'checkbox',
                                                            checked: !!(slotChecks[ts] && slotChecks[ts][a.userId]),
                                                            onChange: ev => handleSlotCheck(ts, a.userId, ev.target.checked)
                                                        })
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                )
            ),

            // 푸터
            e('div', { className: 'popup-footer' },
                msg && e('span', { className: msg.includes('완료') ? 'popup-msg success' : 'popup-msg error' }, msg),
                e('button', { className: 'popup-btn popup-btn-danger', onClick: onClose }, '닫기'),
                e('button', {
                    className: 'popup-btn popup-btn-primary',
                    onClick: handleConfirm,
                    disabled: confirming || attendees.length === 0
                }, confirming ? '확정 중...' : '✓ 스케줄 확정')
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// 달력 셀
// ────────────────────────────────────────────────────────────────────
function CalendarCell({ year, month, day, isToday, isSelected, cellData, isAdmin, onCellClick, onCheck }) {
    const dateStr = toDateStr(year, month, day);
    const dow = new Date(year, month - 1, day).getDay();
    const isSunday = dow === 0;
    const isSaturday = dow === 6;

    const confirmedPeople = cellData.filter(d => d.confirmed === 'Y');
    const unconfirmedPeople = cellData.filter(d => d.confirmed !== 'Y');

    let cellClass = 'cal-cell';
    if (isToday) cellClass += ' today';
    if (isSunday) cellClass += ' sunday';
    if (isSaturday) cellClass += ' saturday';

    function handleCellAreaClick(ev) {
        if (isAdmin) onCellClick(dateStr);
    }

    return e('td', {
        className: cellClass,
        onClick: isAdmin ? handleCellAreaClick : undefined,
        style: isAdmin ? { cursor: 'pointer' } : {}
    },
        e('div', { className: 'cell-header' },
            e('span', { className: 'day-num' }, day),
            e('input', {
                type: 'checkbox',
                className: 'date-check',
                checked: isSelected,
                onChange: ev => { ev.stopPropagation(); onCheck(dateStr, ev.target.checked); },
                onClick: ev => ev.stopPropagation()
            })
        ),
        isSelected && e('div', { className: 'my-badge' }, '✓'),
        e('div', { className: 'badges-wrap' },
            confirmedPeople.map(d =>
                e('span', { key: d.userId, className: 'person-badge confirmed', title: d.role || '' }, d.userName)
            ),
            unconfirmedPeople.map(d =>
                e('span', { key: d.userId, className: 'person-badge unconfirmed', title: '미확정' }, d.userName)
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// 공지사항 섹션
// ────────────────────────────────────────────────────────────────────
function AdminNoteSection({ isAdmin }) {
    const [content, setContent] = React.useState('');
    const [originalContent, setOriginalContent] = React.useState('');
    const [updatedBy, setUpdatedBy] = React.useState('');
    const [updatedAt, setUpdatedAt] = React.useState('');
    const [editing, setEditing] = React.useState(false);
    const [saving, setSaving] = React.useState(false);
    const [msg, setMsg] = React.useState('');

    React.useEffect(() => {
        loadNote();
    }, []);

    function loadNote() {
        fetch('/api/schedule/dates/admin-note', { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : { content: '', updatedBy: '', updatedAt: '' })
            .then(data => {
                setContent(data.content || '');
                setOriginalContent(data.content || '');
                setUpdatedBy(data.updatedBy || '');
                setUpdatedAt(data.updatedAt || '');
            })
            .catch(() => {});
    }

    function handleEdit() {
        setEditing(true);
        setMsg('');
    }

    function handleCancel() {
        setContent(originalContent);
        setEditing(false);
        setMsg('');
    }

    async function handleSave() {
        setSaving(true);
        setMsg('');
        try {
            const r = await fetch('/api/schedule/dates/admin-note', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ content })
            });
            if (!r.ok) throw new Error('저장 실패');
            setMsg('공지사항 저장 완료!');
            setOriginalContent(content);
            setEditing(false);
            loadNote();
        } catch (err) {
            setMsg(err.message || '오류 발생');
        } finally {
            setSaving(false);
        }
    }

    // 내용이 없고 관리자도 아니면 표시 안 함
    if (!content && !isAdmin) return null;

    return e('div', { className: 'admin-note-section' },
        e('div', { className: 'admin-note-header' },
            e('h3', { className: 'admin-note-title' }, '📢 공지사항'),
            updatedAt && e('span', { className: 'admin-note-meta' },
                updatedBy ? `${updatedBy} · ${updatedAt}` : updatedAt
            )
        ),
        editing
            ? e('div', { className: 'admin-note-edit' },
                e('textarea', {
                    className: 'admin-note-textarea',
                    value: content,
                    onChange: ev => setContent(ev.target.value),
                    placeholder: '공지사항을 입력하세요.',
                    rows: 6
                }),
                e('div', { className: 'admin-note-actions' },
                    msg && e('span', { className: msg.includes('완료') ? 'admin-note-msg success' : 'admin-note-msg error' }, msg),
                    e('button', {
                        className: 'popup-btn popup-btn-danger',
                        onClick: handleCancel
                    }, '취소'),
                    e('button', {
                        className: 'popup-btn popup-btn-primary',
                        onClick: handleSave,
                        disabled: saving
                    }, saving ? '저장 중...' : '저장')
                )
            )
            : e('div', { className: 'admin-note-view' },
                content
                    ? e('pre', { className: 'admin-note-content' }, content)
                    : e('p', { className: 'admin-note-empty' }, '등록된 공지사항이 없습니다.'),
                isAdmin && e('div', { className: 'admin-note-actions' },
                    msg && e('span', { className: msg.includes('완료') ? 'admin-note-msg success' : 'admin-note-msg error' }, msg),
                    e('button', {
                        className: 'popup-btn popup-btn-secondary',
                        onClick: handleEdit
                    }, '✏️ 수정')
                )
            )
    );
}

// ────────────────────────────────────────────────────────────────────
// 월별 출근자 목록 (하단)
// ────────────────────────────────────────────────────────────────────
function AttendeeSection({ monthData }) {
    const byPerson = {};
    monthData.forEach(d => {
        if (!byPerson[d.userName]) byPerson[d.userName] = { confirmed: [], unconfirmed: [] };
        if (d.confirmed === 'Y') byPerson[d.userName].confirmed.push(d.date);
        else byPerson[d.userName].unconfirmed.push(d.date);
    });

    const names = Object.keys(byPerson).sort();
    if (names.length === 0) return null;

    return e('div', { className: 'attendee-section' },
        e('h3', { className: 'attendee-title' }, '이번 달 스케줄'),
        e('div', { className: 'attendee-list' },
            names.map(name =>
                e('div', { key: name, className: 'attendee-row' },
                    e('span', { className: 'attendee-name-label' }, name),
                    e('div', { className: 'attendee-dates' },
                        byPerson[name].confirmed.map(d =>
                            e('span', { key: d, className: 'date-tag confirmed-tag' }, d.slice(5))
                        ),
                        byPerson[name].unconfirmed.map(d =>
                            e('span', { key: d, className: 'date-tag unconfirmed-tag' }, d.slice(5))
                        )
                    )
                )
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// 메인 CalendarPage
// ────────────────────────────────────────────────────────────────────
function CalendarPage() {
    const today = new Date();
    const [year, setYear] = React.useState(today.getFullYear());
    const [month, setMonth] = React.useState(today.getMonth() + 1);

    const [userInfo, setUserInfo] = React.useState(null);
    const [monthData, setMonthData] = React.useState([]);
    const [isAdmin, setIsAdmin] = React.useState(false);

    const [checkedDates, setCheckedDates] = React.useState(new Set());
    const [myDates, setMyDates] = React.useState(new Set());

    const [roleOptions, setRoleOptions] = React.useState([]);

    const [popupDate, setPopupDate] = React.useState(null);
    const [popupAttendees, setPopupAttendees] = React.useState([]);

    const [loading, setLoading] = React.useState(false);
    const [saving, setSaving] = React.useState(false);
    const [saveMsg, setSaveMsg] = React.useState('');

    // 사용자 정보 로드
    // UserInfoResponse: { userid, username, admin }
    React.useEffect(() => {
        fetch('/api/user/info', { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : null)
            .then(data => {
                if (data) {
                    // 필드명 정규화: userid→userId, username→userName
                    setUserInfo({
                        userId: data.userid || data.userId || '',
                        userName: data.username || data.userName || '',
                        admin: data.admin || false
                    });
                }
            })
            .catch(() => {});
    }, []);

    // 역할 목록 로드 (드롭다운용)
    React.useEffect(() => {
        fetch('/api/schedule/dates/roles', { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : [])
            .then(data => setRoleOptions(data))
            .catch(() => {});
    }, []);

    // 월 데이터 로드
    React.useEffect(() => {
        loadMonthData();
    }, [year, month]);

    function loadMonthData() {
        setLoading(true);
        setSaveMsg('');
        fetch(`/api/schedule/dates/month?year=${year}&month=${month}`, { credentials: 'same-origin' })
            .then(r => r.ok ? r.json() : { admin: false, dates: [] })
            .then(data => {
                // MonthDataResponse: { isAdmin: boolean, data: [...] }
                const dates = data.data || [];
                setMonthData(dates);
                setIsAdmin(data.isAdmin || false);
            })
            .catch(() => {})
            .finally(() => setLoading(false));
    }

    // userInfo 변경 시 내 날짜 갱신
    React.useEffect(() => {
        if (userInfo && monthData.length > 0) {
            syncMyDates(monthData, userInfo.userId);
        }
    }, [userInfo]);

    // monthData 변경 시 내 날짜 갱신
    React.useEffect(() => {
        if (userInfo) {
            syncMyDates(monthData, userInfo.userId);
        }
    }, [monthData]);

    function syncMyDates(data, userId) {
        const mine = new Set(data.filter(d => d.userId === userId).map(d => d.date));
        setMyDates(mine);
        setCheckedDates(new Set(mine));
    }

    function prevMonth() {
        if (month === 1) { setYear(y => y - 1); setMonth(12); }
        else setMonth(m => m - 1);
    }
    function nextMonth() {
        if (month === 12) { setYear(y => y + 1); setMonth(1); }
        else setMonth(m => m + 1);
    }

    function handleCheck(dateStr, checked) {
        setCheckedDates(prev => {
            const next = new Set(prev);
            if (checked) next.add(dateStr);
            else next.delete(dateStr);
            return next;
        });
    }

    async function handleSave() {
        if (!userInfo) return;
        setSaving(true);
        setSaveMsg('');
        try {
            const toAdd = [...checkedDates].filter(d => !myDates.has(d));
            const toRemove = [...myDates].filter(d => !checkedDates.has(d));

            if (toAdd.length > 0) {
                const payload = {};
                toAdd.forEach(d => { payload[d] = {}; });
                const r = await fetch('/api/schedule/dates/save', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'same-origin',
                    body: JSON.stringify(payload)
                });
                if (!r.ok) throw new Error('저장 실패');
            }

            for (const d of toRemove) {
                const r = await fetch(`/api/schedule/dates?date=${d}`, {
                    method: 'DELETE',
                    credentials: 'same-origin'
                });
                if (!r.ok) throw new Error('삭제 실패');
            }

            setSaveMsg('저장 완료!');
            loadMonthData();
        } catch (err) {
            setSaveMsg(err.message || '오류 발생');
        } finally {
            setSaving(false);
        }
    }

    function handleCellClick(dateStr) {
        if (!isAdmin) return;
        const attendees = monthData.filter(d => d.date === dateStr);
        setPopupDate(dateStr);
        setPopupAttendees(attendees);
    }

    function closePopup() {
        setPopupDate(null);
        setPopupAttendees([]);
    }

    function handlePopupSaved() {
        loadMonthData();
    }

    // 달력 계산
    const daysInMonth = getDaysInMonth(year, month);
    const firstDay = getFirstDayOfWeek(year, month);
    const todayStr = toDateStr(today.getFullYear(), today.getMonth() + 1, today.getDate());

    const dataByDate = {};
    monthData.forEach(d => {
        if (!dataByDate[d.date]) dataByDate[d.date] = [];
        dataByDate[d.date].push(d);
    });

    const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;
    const cells = [];
    for (let i = 0; i < totalCells; i++) {
        const dayNum = i - firstDay + 1;
        if (dayNum < 1 || dayNum > daysInMonth) {
            cells.push(e('td', { key: `empty-${i}`, className: 'cal-cell empty' }));
        } else {
            const dateStr = toDateStr(year, month, dayNum);
            cells.push(e(CalendarCell, {
                key: dateStr,
                year, month, day: dayNum,
                isToday: dateStr === todayStr,
                isSelected: checkedDates.has(dateStr),
                cellData: dataByDate[dateStr] || [],
                isAdmin,
                onCellClick: handleCellClick,
                onCheck: handleCheck
            }));
        }
    }

    const rows = [];
    for (let i = 0; i < cells.length; i += 7) {
        rows.push(e('tr', { key: `row-${i}` }, cells.slice(i, i + 7)));
    }

    return e('div', { className: 'cal-wrap' },

        e('div', { className: 'cal-top-bar' },
            e('h1', { className: 'cal-logo' }, 'ShowFlix'),
            userInfo && e(UserDropdown, { userName: userInfo.userName, isAdmin: userInfo.admin })
        ),

        e('div', { className: 'cal-nav' },
            e('button', { className: 'nav-btn', onClick: prevMonth }, '‹'),
            e('span', { className: 'cal-month-label' }, `${year}년 ${month}월`),
            e('button', { className: 'nav-btn', onClick: nextMonth }, '›')
        ),

        loading
            ? e('div', { className: 'loading-wrap' }, '불러오는 중...')
            : e('div', { className: 'cal-table-wrap' },
                e('table', { className: 'cal-table' },
                    e('thead', null,
                        e('tr', null,
                            WEEKDAYS.map((w, i) =>
                                e('th', {
                                    key: w,
                                    className: i === 0 ? 'weekday sunday' : i === 6 ? 'weekday saturday' : 'weekday'
                                }, w)
                            )
                        )
                    ),
                    e('tbody', null, ...rows)
                )
            ),

        e('div', { className: 'save-bar' },
            saveMsg && e('span', { className: 'save-msg' }, saveMsg),
            e('button', {
                className: 'save-btn',
                onClick: handleSave,
                disabled: saving
            }, saving ? '저장 중...' : '저장하기')
        ),

        e(AdminNoteSection, { isAdmin }),

        popupDate && e(AdminPopup, {
            date: popupDate,
            attendees: popupAttendees,
            roleOptions,
            onClose: closePopup,
            onSaved: handlePopupSaved
        }),

        e(AttendeeSection, { monthData })
    );
}

// ────────────────────────────────────────────────────────────────────
// 마운트
// ────────────────────────────────────────────────────────────────────
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(e(CalendarPage));
