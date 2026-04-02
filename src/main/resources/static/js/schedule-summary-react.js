/**
 * schedule-summary-react.js
 * CDN React (no JSX) 기반 출근시간 관리 페이지 (웹 와이드 레이아웃)
 *
 * 구조:
 *  ScheduleSummaryPage
 *  ├── SsHeader  (년/월 선택, 저장, 엑셀, 뒤로가기)
 *  └── SsTable   (출근시간 그리드)
 */

const e = React.createElement;

// ────────────────────────────────────────────────────────────────────
// 상수
// ────────────────────────────────────────────────────────────────────

const DAY_OF_WEEK_KR = ['일', '월', '화', '수', '목', '금', '토'];

// ────────────────────────────────────────────────────────────────────
// 유틸
// ────────────────────────────────────────────────────────────────────

/** fetch wrapper */
async function apiFetch(url, options = {}) {
    const res = await fetch(url, {
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options,
    });
    if (res.status === 401 || res.status === 403) {
        window.location.href = '/';
        return null;
    }
    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw new Error((data && data.message) || '요청 실패');
    }
    if (res.status === 204 || res.headers.get('content-length') === '0') return null;
    return res.json().catch(() => null);
}

/** 날짜 문자열 생성 YYYY-MM-DD */
function dateStr(year, month, day) {
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

/** 요일 인덱스 (일=0, 월=1 ... 토=6) */
function getDayOfWeekIdx(year, month, day) {
    return new Date(year, month - 1, day).getDay();
}

/** 해당 월 일수 */
function getDaysInMonth(year, month) {
    return new Date(year, month, 0).getDate();
}

/** hours 문자열 → 숫자 (파싱 실패 시 0) */
function parseHours(hoursStr) {
    if (!hoursStr || hoursStr.trim() === '') return 0;
    const n = parseFloat(hoursStr);
    return isNaN(n) ? 0 : n;
}

/** hours 값 표시용 포맷 (소수점 불필요 시 정수 표시) */
function formatHours(val) {
    if (!val || val === 0) return '';
    const n = parseFloat(val);
    if (isNaN(n) || n === 0) return '';
    return n % 1 === 0 ? String(n) : n.toFixed(1);
}

// ────────────────────────────────────────────────────────────────────
// SsHeader
// ────────────────────────────────────────────────────────────────────
function SsHeader({ year, month, onYearChange, onMonthChange, changesCount, onSave, onExport, saving }) {

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 5 }, (_, i) => currentYear - 2 + i);
    const months = Array.from({ length: 12 }, (_, i) => i + 1);

    return e('header', { className: 'ss-header' },
        // 로고
        e('div', { className: 'ss-logo' },
            'ShowFlix',
            e('span', null, '출근시간 관리')
        ),

        // 년/월 선택
        e('div', { className: 'ss-header-controls' },
            e('select', {
                className: 'ss-year-select',
                value: year,
                onChange: ev => onYearChange(Number(ev.target.value)),
            },
                years.map(y => e('option', { key: y, value: y }, y + '년'))
            ),
            e('select', {
                className: 'ss-month-select',
                value: month,
                onChange: ev => onMonthChange(Number(ev.target.value)),
            },
                months.map(m => e('option', { key: m, value: m }, m + '월'))
            ),
            changesCount > 0 && e('span', { className: 'ss-changes-badge' },
                `변경 ${changesCount}건`
            )
        ),

        // 액션 버튼
        e('div', { className: 'ss-header-actions' },
            e('button', {
                className: 'ss-btn ss-btn-save',
                onClick: onSave,
                disabled: saving || changesCount === 0,
            }, saving ? '저장 중...' : '저장'),

            e('button', {
                className: 'ss-btn ss-btn-excel',
                onClick: onExport,
                disabled: saving,
            }, '엑셀 다운로드'),

            e('button', {
                className: 'ss-btn ss-btn-back',
                onClick: () => window.location.href = '/admin/',
            }, '← 관리자')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// SsCell - 날짜 셀 단위 컴포넌트
// ────────────────────────────────────────────────────────────────────
function SsCell({ userId, dateKey, hoursValue, isChanged, onCellChange }) {
    const [editing, setEditing] = React.useState(false);
    const [inputVal, setInputVal] = React.useState(formatHours(hoursValue));
    const inputRef = React.useRef(null);

    // 외부 데이터 변경 시 동기화 (다른 월로 이동 후 돌아올 때)
    React.useEffect(() => {
        if (!editing) {
            setInputVal(formatHours(hoursValue));
        }
    }, [hoursValue, editing]);

    function handleEmptyClick() {
        setEditing(true);
        setInputVal('');
        setTimeout(() => inputRef.current && inputRef.current.focus(), 0);
    }

    function handleBlur() {
        setEditing(false);
        const trimmed = inputVal.trim();
        // 소수점 1자리 제한
        let validated = '';
        if (trimmed !== '') {
            const num = parseFloat(trimmed);
            if (!isNaN(num) && num > 0) {
                validated = num % 1 === 0 ? String(num) : num.toFixed(1);
            }
        }
        setInputVal(validated);
        onCellChange(userId, dateKey, validated || '');
    }

    function handleKeyDown(ev) {
        if (ev.key === 'Enter') {
            ev.target.blur();
        }
        if (ev.key === 'Escape') {
            setInputVal(formatHours(hoursValue));
            setEditing(false);
        }
    }

    function handleInputChange(ev) {
        const val = ev.target.value;
        // 숫자와 소수점만 허용
        if (val === '' || /^\d*\.?\d{0,1}$/.test(val)) {
            setInputVal(val);
        }
    }

    const hasValue = hoursValue && parseHours(hoursValue) > 0;

    if (!hasValue && !editing) {
        // '-' 표시 (클릭 시 입력 활성화)
        return e('span', {
            className: 'ss-cell-empty',
            onClick: handleEmptyClick,
            title: '클릭하여 입력',
        }, '-');
    }

    // 입력 필드
    return e('input', {
        ref: inputRef,
        className: 'ss-cell-input',
        type: 'text',
        inputMode: 'decimal',
        value: editing ? inputVal : (formatHours(hoursValue) || ''),
        onChange: handleInputChange,
        onFocus: () => { setEditing(true); setInputVal(formatHours(hoursValue) || ''); },
        onBlur: handleBlur,
        onKeyDown: handleKeyDown,
        placeholder: '',
    });
}

// ────────────────────────────────────────────────────────────────────
// SsTable
// ────────────────────────────────────────────────────────────────────
function SsTable({ year, month, users, gridData, remarksData, remarksChanges, changes, onCellChange, onRemarksChange }) {
    const daysInMonth = getDaysInMonth(year, month);
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    if (!users || users.length === 0) {
        return e('div', { className: 'ss-empty' }, '직원 데이터가 없습니다.');
    }

    return e('div', { className: 'ss-table-wrap' },
        e('table', { className: 'ss-table' },

            // ── 헤더 (날짜 | 출근자1 | 출근자2 | ... | 합계 | 특이사항) ──
            e('thead', null,
                e('tr', null,
                    e('th', { className: 'ss-date-col' }, '날짜'),
                    ...users.map(user =>
                        e('th', { key: user.userId, className: 'ss-user-col' }, user.userName)
                    ),
                    e('th', { className: 'ss-total-col' }, '합계'),
                    e('th', { className: 'ss-remarks-col' }, '특이사항')
                )
            ),

            // ── 바디 (날짜별 행) ──
            e('tbody', null,
                days.map(d => {
                    const dk = dateStr(year, month, d);
                    const dow = getDayOfWeekIdx(year, month, d);
                    const label = `${d}일(${DAY_OF_WEEK_KR[dow]})`;
                    let dayTotal = 0;

                    const userCells = users.map(user => {
                        const userMap = (gridData && gridData[user.userId]) || {};
                        const hoursVal = userMap[dk] || '';
                        const changed = changes.has(`${user.userId}|${dk}`);
                        const hrs = parseHours(hoursVal);
                        if (hrs > 0) dayTotal += hrs;

                        let tdCls = 'ss-user-col';
                        if (changed) tdCls += ' ss-changed';

                        return e('td', { key: user.userId, className: tdCls },
                            e(SsCell, {
                                key: `${user.userId}|${dk}`,
                                userId: user.userId,
                                dateKey: dk,
                                hoursValue: hoursVal,
                                isChanged: changed,
                                onCellChange,
                            })
                        );
                    });

                    let trCls = '';
                    if (dow === 0) trCls = 'ss-sun-row';
                    else if (dow === 6) trCls = 'ss-sat-row';

                    const remarksText = (remarksData && remarksData[dk]) || '';
                    const remarksChanged = remarksChanges && remarksChanges.has(dk);

                    return e('tr', { key: dk, className: trCls },
                        e('td', { className: 'ss-date-col' }, label),
                        ...userCells,
                        e('td', { className: 'ss-total-col' },
                            dayTotal > 0
                                ? (dayTotal % 1 === 0 ? dayTotal : dayTotal.toFixed(1))
                                : '-'
                        ),
                        e('td', { className: 'ss-remarks-col' + (remarksChanged ? ' ss-changed' : '') },
                            e('input', {
                                className: 'ss-remarks-input',
                                type: 'text',
                                value: remarksText,
                                placeholder: '',
                                onChange: ev => onRemarksChange(dk, ev.target.value),
                            })
                        )
                    );
                }),

                // ── 합계 행 ──
                (() => {
                    let grandTotal = 0;
                    const userTotalCells = users.map(user => {
                        const userMap = (gridData && gridData[user.userId]) || {};
                        let userTotal = 0;
                        days.forEach(d => {
                            const dk = dateStr(year, month, d);
                            userTotal += parseHours(userMap[dk] || '');
                        });
                        grandTotal += userTotal;
                        return e('td', { key: user.userId, className: 'ss-total-col' },
                            userTotal > 0
                                ? (userTotal % 1 === 0 ? userTotal : userTotal.toFixed(1))
                                : '-'
                        );
                    });
                    return e('tr', { key: 'total', className: 'ss-total-row' },
                        e('td', { className: 'ss-date-col ss-total-label' }, '합계'),
                        ...userTotalCells,
                        e('td', { className: 'ss-total-col ss-grand-total' },
                            grandTotal > 0
                                ? (grandTotal % 1 === 0 ? grandTotal : grandTotal.toFixed(1))
                                : '-'
                        ),
                        e('td', { className: 'ss-remarks-col' }, '')
                    );
                })()
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// Toast
// ────────────────────────────────────────────────────────────────────
function Toast({ message, type }) {
    if (!message) return null;
    return e('div', { className: `ss-toast ${type || ''}` }, message);
}

// ────────────────────────────────────────────────────────────────────
// ScheduleSummaryPage - 루트 컴포넌트
// ────────────────────────────────────────────────────────────────────
function ScheduleSummaryPage() {
    const now = new Date();
    const [year, setYear] = React.useState(now.getFullYear());
    const [month, setMonth] = React.useState(now.getMonth() + 1);

    const [users, setUsers] = React.useState([]);
    const [gridData, setGridData] = React.useState({});          // { userId: { dateStr: hoursStr } }
    const [remarksData, setRemarksData] = React.useState({});    // { dateStr: remarks }
    const [changes, setChanges] = React.useState(new Set());     // "userId|dateStr"
    const [remarksChanges, setRemarksChanges] = React.useState(new Set()); // dateStr

    const [loading, setLoading] = React.useState(false);
    const [saving, setSaving] = React.useState(false);
    const [toast, setToast] = React.useState(null); // { message, type }
    const toastTimerRef = React.useRef(null);

    // ── 데이터 로드 ──────────────────────────────────────────────
    React.useEffect(() => {
        loadMonthData(year, month);
    }, [year, month]);

    async function loadMonthData(y, m) {
        setLoading(true);
        try {
            const result = await apiFetch(`/api/admin/schedule-summary/month?year=${y}&month=${m}`);
            if (!result) return;
            setUsers(result.users || []);
            setGridData(result.data || {});
            setRemarksData(result.dateRemarks || {});
            setChanges(new Set());
            setRemarksChanges(new Set());
        } catch (err) {
            showToast(err.message || '데이터 조회 실패', 'error');
        } finally {
            setLoading(false);
        }
    }

    // ── 년/월 변경 핸들러 ────────────────────────────────────────
    function handleYearChange(y) {
        if (changes.size > 0 || remarksChanges.size > 0) {
            if (!confirm('저장하지 않은 변경사항이 있습니다. 계속하시겠습니까?')) return;
        }
        setYear(y);
    }

    function handleMonthChange(m) {
        if (changes.size > 0 || remarksChanges.size > 0) {
            if (!confirm('저장하지 않은 변경사항이 있습니다. 계속하시겠습니까?')) return;
        }
        setMonth(m);
    }

    // ── 셀 변경 핸들러 ───────────────────────────────────────────
    function handleCellChange(userId, dateKey, hoursValue) {
        setGridData(prev => {
            const next = { ...prev };
            if (!next[userId]) next[userId] = {};
            next[userId] = { ...next[userId], [dateKey]: hoursValue };
            return next;
        });
        setChanges(prev => {
            const next = new Set(prev);
            next.add(`${userId}|${dateKey}`);
            return next;
        });
    }

    // ── 특이사항 변경 핸들러 ───────────────────────────────────────
    function handleRemarksChange(dateKey, value) {
        setRemarksData(prev => ({ ...prev, [dateKey]: value }));
        setRemarksChanges(prev => {
            const next = new Set(prev);
            next.add(dateKey);
            return next;
        });
    }

    // ── 저장 ─────────────────────────────────────────────────────
    async function handleSave() {
        if (changes.size === 0 && remarksChanges.size === 0) return;

        const items = [];
        for (const key of changes) {
            const [userId, dk] = key.split('|');
            const hours = (gridData[userId] && gridData[userId][dk]) || '';
            items.push({ userId, date: dk, hours, remarks: null });
        }
        // 특이사항 변경분 추가
        for (const dk of remarksChanges) {
            items.push({ userId: '__remarks__', date: dk, hours: '0', remarks: remarksData[dk] || '' });
        }

        setSaving(true);
        try {
            await apiFetch('/api/admin/schedule-summary/save', {
                method: 'POST',
                body: JSON.stringify(items),
            });
            setChanges(new Set());
            setRemarksChanges(new Set());
            showToast('저장되었습니다.', 'success');
        } catch (err) {
            showToast(err.message || '저장 실패', 'error');
        } finally {
            setSaving(false);
        }
    }

    // ── 엑셀 다운로드 ────────────────────────────────────────────
    async function handleExport() {
        try {
            const res = await fetch(
                `/api/admin/schedule-summary/export?year=${year}&month=${month}`,
                { credentials: 'same-origin' }
            );
            if (!res.ok) throw new Error('다운로드 실패');
            const blob = await res.blob();
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${year}년_${month}월_출근시간.xlsx`;
            a.click();
            URL.revokeObjectURL(url);
        } catch (err) {
            showToast(err.message || '다운로드 실패', 'error');
        }
    }

    // ── 토스트 ───────────────────────────────────────────────────
    function showToast(message, type = '') {
        if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
        setToast({ message, type });
        toastTimerRef.current = setTimeout(() => setToast(null), 2800);
    }

    // ── 렌더 ─────────────────────────────────────────────────────
    return e('div', { className: 'ss-wrap' },
        e(SsHeader, {
            year, month,
            onYearChange: handleYearChange,
            onMonthChange: handleMonthChange,
            changesCount: changes.size + remarksChanges.size,
            onSave: handleSave,
            onExport: handleExport,
            saving,
        }),

        e('main', { className: 'ss-content' },
            loading
                ? e('div', { className: 'ss-loading' }, '데이터를 불러오는 중...')
                : e(SsTable, {
                    year, month, users, gridData, remarksData, remarksChanges, changes,
                    onCellChange: handleCellChange,
                    onRemarksChange: handleRemarksChange,
                })
        ),

        toast && e(Toast, { message: toast.message, type: toast.type })
    );
}

// ────────────────────────────────────────────────────────────────────
// Mount
// ────────────────────────────────────────────────────────────────────
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(e(ScheduleSummaryPage));
