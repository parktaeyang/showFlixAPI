/**
 * work-diary-react.js
 * CDN React (no JSX) 기반 업무일지 관리 페이지
 *
 * 구조:
 *  WorkDiaryPage
 *  ├── WdHeader  (로고, 년/월 선택, 업무 추가, 엑셀, 뒤로가기)
 *  └── WdTable   (업무일지 테이블)
 *      └── WdRow (행 단위: 표시/수정 모드)
 */

const e = React.createElement;

// ────────────────────────────────────────────────────────────────────
// 유틸
// ────────────────────────────────────────────────────────────────────

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

function todayStr() {
    const d = new Date();
    return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0');
}

// ────────────────────────────────────────────────────────────────────
// WdHeader
// ────────────────────────────────────────────────────────────────────
function WdHeader({ year, month, onYearChange, onMonthChange, onAdd, onExport }) {

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 8 }, (_, i) => 2023 + i);
    const months = Array.from({ length: 12 }, (_, i) => i + 1);

    return e('header', { className: 'wd-header' },
        e('div', { className: 'wd-logo' },
            'ShowFlix',
            e('span', null, '업무일지')
        ),
        e('div', { className: 'wd-header-controls' },
            e('select', {
                className: 'wd-year-select',
                value: year,
                onChange: ev => onYearChange(Number(ev.target.value)),
            },
                years.map(y => e('option', { key: y, value: y }, y + '년'))
            ),
            e('select', {
                className: 'wd-month-select',
                value: month,
                onChange: ev => onMonthChange(Number(ev.target.value)),
            },
                months.map(m => e('option', { key: m, value: m }, m + '월'))
            ),
            e('button', { className: 'wd-btn wd-btn-add', onClick: onAdd }, '+ 업무 추가'),
            e('button', { className: 'wd-btn wd-btn-export', onClick: onExport }, '엑셀 내보내기'),
            e('button', {
                className: 'wd-btn wd-btn-back',
                onClick: () => window.location.href = '/admin/',
            }, '← 관리자')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// WdRow
// ────────────────────────────────────────────────────────────────────
function WdRow({ diary, isEditing, isNew, onSave, onCancel, onEdit, onDelete }) {
    const { useState } = React;

    const [form, setForm] = useState({
        date: diary ? diary.date : todayStr(),
        manager: diary ? (diary.manager || '') : '',
        cashPayment: diary ? (diary.cashPayment || '') : '',
        reservations: diary ? (diary.reservations || '') : '',
        event: diary ? (diary.event || '') : '',
        storeRelated: diary ? (diary.storeRelated || '') : '',
        notes: diary ? (diary.notes || '') : '',
    });

    function handleChange(field, value) {
        setForm(prev => ({ ...prev, [field]: value }));
    }

    // 표시 모드
    if (!isEditing && !isNew) {
        const displayDate = diary.date ? diary.date.substring(5).replace('-', '/') : '';
        return e('tr', null,
            e('td', { className: 'wd-col-date' }, displayDate),
            e('td', { className: 'wd-col-manager' }, diary.manager || ''),
            e('td', { className: 'wd-col-cash' }, diary.cashPayment || ''),
            e('td', { className: 'wd-col-reserve' }, diary.reservations || ''),
            e('td', { className: 'wd-col-event' }, diary.event || ''),
            e('td', { className: 'wd-col-store' }, diary.storeRelated || ''),
            e('td', { className: 'wd-col-notes' }, diary.notes || ''),
            e('td', { className: 'wd-col-actions' },
                e('button', { className: 'wd-row-btn wd-row-btn-edit', onClick: onEdit }, '수정'),
                e('button', { className: 'wd-row-btn wd-row-btn-delete', onClick: onDelete }, '삭제')
            )
        );
    }

    // 편집/신규 모드
    return e('tr', null,
        e('td', null,
            e('input', {
                type: 'date', className: 'wd-input', value: form.date,
                onChange: ev => handleChange('date', ev.target.value),
            })
        ),
        e('td', null,
            e('input', {
                type: 'text', className: 'wd-input', placeholder: '담당자', value: form.manager,
                onChange: ev => handleChange('manager', ev.target.value),
            })
        ),
        e('td', null,
            e('textarea', {
                className: 'wd-textarea', rows: 3, value: form.cashPayment,
                onChange: ev => handleChange('cashPayment', ev.target.value),
            })
        ),
        e('td', null,
            e('textarea', {
                className: 'wd-textarea', rows: 3, value: form.reservations,
                onChange: ev => handleChange('reservations', ev.target.value),
            })
        ),
        e('td', null,
            e('textarea', {
                className: 'wd-textarea', rows: 3, value: form.event,
                onChange: ev => handleChange('event', ev.target.value),
            })
        ),
        e('td', null,
            e('textarea', {
                className: 'wd-textarea', rows: 3, value: form.storeRelated,
                onChange: ev => handleChange('storeRelated', ev.target.value),
            })
        ),
        e('td', null,
            e('textarea', {
                className: 'wd-textarea', rows: 3, value: form.notes,
                onChange: ev => handleChange('notes', ev.target.value),
            })
        ),
        e('td', { className: 'wd-col-actions' },
            e('button', {
                className: 'wd-row-btn wd-row-btn-save',
                onClick: () => onSave(form),
            }, '저장'),
            e('button', {
                className: 'wd-row-btn wd-row-btn-cancel',
                onClick: onCancel,
            }, '취소')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// WorkDiaryPage (메인)
// ────────────────────────────────────────────────────────────────────
function WorkDiaryPage() {
    const { useState, useEffect, useCallback } = React;

    const now = new Date();
    const [year, setYear] = useState(now.getFullYear());
    const [month, setMonth] = useState(now.getMonth() + 1);
    const [diaries, setDiaries] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [addingNew, setAddingNew] = useState(false);
    const [loading, setLoading] = useState(false);

    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await apiFetch('/api/admin/work-diary?year=' + year + '&month=' + month);
            if (result && result.success) {
                setDiaries(result.data || []);
            }
        } catch (err) {
            alert('데이터 조회 실패: ' + err.message);
        } finally {
            setLoading(false);
        }
    }, [year, month]);

    useEffect(() => {
        loadData();
        setEditingId(null);
        setAddingNew(false);
    }, [loadData]);

    // 년/월 변경
    function handleYearChange(y) { setYear(y); }
    function handleMonthChange(m) { setMonth(m); }

    // 신규 추가
    function handleAdd() {
        if (addingNew) {
            alert('이미 추가 중인 행이 있습니다.');
            return;
        }
        if (editingId !== null) {
            alert('수정 중인 행이 있습니다. 먼저 저장하거나 취소해주세요.');
            return;
        }
        setAddingNew(true);
    }

    // 신규 저장
    async function handleSaveNew(form) {
        if (!form.date) { alert('날짜는 필수 항목입니다.'); return; }
        try {
            await apiFetch('/api/admin/work-diary', {
                method: 'POST',
                body: JSON.stringify(form),
            });
            setAddingNew(false);
            loadData();
        } catch (err) {
            alert('저장 실패: ' + err.message);
        }
    }

    // 수정 시작
    function handleEdit(id) {
        if (addingNew) {
            alert('추가 중인 행이 있습니다. 먼저 저장하거나 취소해주세요.');
            return;
        }
        setEditingId(id);
    }

    // 수정 저장
    async function handleSaveEdit(id, form) {
        if (!form.date) { alert('날짜는 필수 항목입니다.'); return; }
        try {
            await apiFetch('/api/admin/work-diary/' + id, {
                method: 'PUT',
                body: JSON.stringify(form),
            });
            setEditingId(null);
            loadData();
        } catch (err) {
            alert('수정 실패: ' + err.message);
        }
    }

    // 삭제
    async function handleDelete(id) {
        if (!confirm('정말로 이 업무일지를 삭제하시겠습니까?')) return;
        try {
            await apiFetch('/api/admin/work-diary/' + id, { method: 'DELETE' });
            loadData();
        } catch (err) {
            alert('삭제 실패: ' + err.message);
        }
    }

    // 엑셀
    function handleExport() {
        window.location.href = '/api/admin/work-diary/export?year=' + year + '&month=' + month;
    }

    // 렌더링
    return e('div', { className: 'wd-wrap' },
        e(WdHeader, {
            year: year, month: month,
            onYearChange: handleYearChange,
            onMonthChange: handleMonthChange,
            onAdd: handleAdd,
            onExport: handleExport,
        }),

        e('div', { className: 'wd-body' },
            e('table', { className: 'wd-table' },
                e('thead', null,
                    e('tr', null,
                        e('th', { className: 'wd-col-date' }, '날짜'),
                        e('th', { className: 'wd-col-manager' }, '담당자'),
                        e('th', { className: 'wd-col-cash' }, '현금 결제'),
                        e('th', { className: 'wd-col-reserve' }, '지정석/특수예약/멤버십'),
                        e('th', { className: 'wd-col-event' }, '이벤트'),
                        e('th', { className: 'wd-col-store' }, '가게 관련'),
                        e('th', { className: 'wd-col-notes' }, '특이사항'),
                        e('th', { className: 'wd-col-actions' }, '작업')
                    )
                ),
                e('tbody', null,
                    // 신규 추가 행 (맨 위)
                    addingNew && e(WdRow, {
                        key: '__new__',
                        diary: null,
                        isNew: true,
                        isEditing: false,
                        onSave: handleSaveNew,
                        onCancel: () => setAddingNew(false),
                    }),

                    // 기존 데이터
                    diaries.length === 0 && !addingNew
                        ? e('tr', null,
                            e('td', { colSpan: 8, className: 'wd-empty' },
                                loading ? '로딩 중...' : '등록된 업무일지가 없습니다.'
                            )
                        )
                        : diaries.map(d =>
                            e(WdRow, {
                                key: d.id,
                                diary: d,
                                isEditing: editingId === d.id,
                                isNew: false,
                                onEdit: () => handleEdit(d.id),
                                onDelete: () => handleDelete(d.id),
                                onSave: (form) => handleSaveEdit(d.id, form),
                                onCancel: () => setEditingId(null),
                            })
                        )
                )
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// Mount
// ────────────────────────────────────────────────────────────────────
ReactDOM.createRoot(document.getElementById('root')).render(e(WorkDiaryPage));
