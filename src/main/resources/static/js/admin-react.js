/**
 * admin-react.js
 * CDN React (no JSX) 기반 관리자 페이지
 *
 * 구조:
 *  AdminPage
 *  ├── AdminHeader
 *  ├── TabBar
 *  ├── UserManagementTab (계정관리)
 *  │   ├── AddUserForm (신규 계정 추가 - 상단)
 *  │   ├── 계정 목록 헤더 (제목 + 역할 필터)
 *  │   ├── 사용자 카드 목록
 *  │   ├── EditUserPopup (수정 팝업)
 *  │   └── ChangePasswordPopup (비밀번호 변경 팝업)
 *  └── SpecialReservationTab (특수예약관리)
 *      ├── 특수예약 추가 버튼 → AddSpecialPopup (모달)
 *      ├── 특수예약 목록 (카드형)
 *      └── EditSpecialPopup (수정 모달)
 */

const e = React.createElement;

// ────────────────────────────────────────────────────────────────────
// 유틸
// ────────────────────────────────────────────────────────────────────

// ScheduleRole enum 코드 → 한국어 표시명 매핑
const ROLE_DISPLAY = {
    DOOR:    '도어',
    HOLEMAN: '홀맨',
    OPER:    '오퍼',
    HELPER:  '헬퍼',
    KITCHEN: '주방',
    MALE1:   '남1',
    MALE2:   '남2',
    MALE3:   '남3',
    FEMALE1: '여1',
    FEMALE2: '여2',
    FEMALE3: '여3',
};

function getRoleDisplay(roleCode) {
    if (!roleCode) return null;
    return ROLE_DISPLAY[roleCode] || roleCode;
}

// 필터용 옵션 목록
const ROLE_FILTER_OPTIONS = [
    { value: 'ALL',  label: '전체' },
    { value: 'NONE', label: '역할없음' },
    ...Object.entries(ROLE_DISPLAY).map(([value, label]) => ({ value, label })),
];

/**
 * fetch wrapper - JSON 요청/응답
 */
async function apiFetch(url, options = {}) {
    const res = await fetch(url, {
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options,
    });
    if (res.status === 403) {
        alert('접근 권한이 없습니다. 관리자 계정으로 로그인해주세요.');
        window.location.href = '/';
        return null;
    }
    if (res.status === 401) {
        window.location.href = '/';
        return null;
    }
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        throw new Error((data && data.message) || '요청 실패');
    }
    return data;
}

// ────────────────────────────────────────────────────────────────────
// AdminHeader
// ────────────────────────────────────────────────────────────────────
function AdminHeader({ userName }) {
    function handleBack() {
        window.location.href = '/schedule/calendar';
    }

    return e('header', { className: 'admin-header' },
        e('div', { className: 'admin-logo' },
            'ShowFlix',
            e('span', null, '관리자')
        ),
        e('div', { style: { display: 'flex', alignItems: 'center', gap: '10px' } },
            userName && e('span', { style: { color: 'rgba(255,255,255,0.8)', fontSize: '0.82rem' } }, userName),
            e('button', { className: 'admin-back-btn', onClick: handleBack }, '← 달력으로')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// TabBar
// ────────────────────────────────────────────────────────────────────
const TABS = [
    { key: 'users', label: '계정관리' },
    { key: 'special', label: '특수예약관리' },
];

function TabBar({ activeTab, onTabChange }) {
    return e('div', { className: 'admin-tab-bar' },
        TABS.map(tab =>
            e('button', {
                key: tab.key,
                className: 'admin-tab' + (activeTab === tab.key ? ' active' : ''),
                onClick: () => onTabChange(tab.key),
            }, tab.label)
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// EditUserPopup - 계정 수정 팝업
// ────────────────────────────────────────────────────────────────────
function EditUserPopup({ user, onClose, onSaved }) {
    const [username, setUsername] = React.useState(user.username);
    const [isAdmin, setIsAdmin] = React.useState(user.admin);
    const [error, setError] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    async function handleSave() {
        if (!username.trim()) {
            setError('이름을 입력해주세요.');
            return;
        }
        setSaving(true);
        setError('');
        try {
            await apiFetch(`/api/admin/users/${user.userid}`, {
                method: 'PUT',
                body: JSON.stringify({ username: username.trim(), admin: isAdmin }),
            });
            onSaved();
        } catch (err) {
            setError(err.message || '수정 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, '계정 수정'),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                e('div', { style: { fontSize: '0.78rem', color: '#9ca3af', marginBottom: '4px' } },
                    '아이디: ', e('strong', null, user.userid)
                ),
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '이름',
                    value: username,
                    onChange: ev => setUsername(ev.target.value),
                })
            ),
            e('div', { className: 'form-check-row' },
                e('input', {
                    type: 'checkbox',
                    id: 'edit-is-admin',
                    checked: isAdmin,
                    onChange: ev => setIsAdmin(ev.target.checked),
                }),
                e('label', { htmlFor: 'edit-is-admin' }, '관리자 권한')
            ),
            e('div', { className: 'popup-actions' },
                e('button', { className: 'btn-cancel', onClick: onClose }, '취소'),
                e('button', {
                    className: 'btn-save',
                    onClick: handleSave,
                    disabled: saving,
                }, saving ? '저장 중...' : '저장')
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// ChangePasswordPopup - 비밀번호 변경 팝업
// ────────────────────────────────────────────────────────────────────
function ChangePasswordPopup({ user, onClose, onSaved }) {
    const [newPassword, setNewPassword] = React.useState('');
    const [confirmPassword, setConfirmPassword] = React.useState('');
    const [error, setError] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    async function handleSave() {
        if (!newPassword.trim()) {
            setError('새 비밀번호를 입력해주세요.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }
        setSaving(true);
        setError('');
        try {
            await apiFetch(`/api/admin/users/${user.userid}/password`, {
                method: 'PUT',
                body: JSON.stringify({ newPassword }),
            });
            onSaved();
        } catch (err) {
            setError(err.message || '비밀번호 변경 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, '비밀번호 변경'),
            e('div', { style: { fontSize: '0.82rem', color: '#6b7280', marginBottom: '12px' } },
                user.userid, ' (', user.username, ')'
            ),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                e('input', {
                    className: 'form-input',
                    type: 'password',
                    placeholder: '새 비밀번호',
                    value: newPassword,
                    onChange: ev => setNewPassword(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'password',
                    placeholder: '새 비밀번호 확인',
                    value: confirmPassword,
                    onChange: ev => setConfirmPassword(ev.target.value),
                })
            ),
            e('div', { className: 'popup-actions' },
                e('button', { className: 'btn-cancel', onClick: onClose }, '취소'),
                e('button', {
                    className: 'btn-save',
                    onClick: handleSave,
                    disabled: saving,
                }, saving ? '변경 중...' : '변경')
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// UserCard - 사용자 카드 1개
// ────────────────────────────────────────────────────────────────────
function UserCard({ user, currentUserid, onEdit, onChangePassword, onDelete }) {
    const isMe = user.userid === currentUserid;
    const roleDisplay = getRoleDisplay(user.role);

    return e('div', { className: 'user-card' },
        e('div', { className: 'user-card-top' },
            e('span', { className: 'user-card-id' }, user.userid),
            e('span', { className: 'user-card-name' }, user.username),
            e('div', { className: 'user-card-badges' },
                // 역할 뱃지 (스케줄 역할이 있을 때만)
                roleDisplay && e('span', { className: 'badge badge-role' }, roleDisplay),
                // 관리자/일반 뱃지
                e('span', { className: 'badge ' + (user.admin ? 'badge-admin' : 'badge-user') },
                    user.admin ? '관리자' : '일반'
                )
            )
        ),
        e('div', { className: 'user-card-actions' },
            e('button', { className: 'btn-sm btn-edit', onClick: () => onEdit(user) }, '수정'),
            e('button', { className: 'btn-sm btn-pw', onClick: () => onChangePassword(user) }, '비밀번호'),
            !isMe && e('button', {
                className: 'btn-sm btn-del',
                onClick: () => onDelete(user),
            }, '삭제')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// AddUserForm - 신규 계정 추가 인라인 폼
// ────────────────────────────────────────────────────────────────────
function AddUserForm({ onAdded }) {
    const [userid, setUserid] = React.useState('');
    const [username, setUsername] = React.useState('');
    const [password, setPassword] = React.useState('');
    const [isAdmin, setIsAdmin] = React.useState(false);
    const [error, setError] = React.useState('');
    const [success, setSuccess] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    async function handleAdd() {
        if (!userid.trim() || !username.trim() || !password.trim()) {
            setError('아이디, 이름, 비밀번호를 모두 입력해주세요.');
            return;
        }
        setSaving(true);
        setError('');
        setSuccess('');
        try {
            await apiFetch('/api/admin/users', {
                method: 'POST',
                body: JSON.stringify({
                    userid: userid.trim(),
                    username: username.trim(),
                    password,
                    admin: isAdmin,
                }),
            });
            setUserid('');
            setUsername('');
            setPassword('');
            setIsAdmin(false);
            setSuccess('계정이 추가되었습니다.');
            onAdded();
        } catch (err) {
            setError(err.message || '추가 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'add-form-card' },
        e('div', { className: 'add-form-title' }, '+ 신규 계정 추가'),
        error && e('div', { className: 'msg msg-error' }, error),
        success && e('div', { className: 'msg msg-success' }, success),
        e('div', { className: 'form-row' },
            e('input', {
                className: 'form-input',
                type: 'text',
                placeholder: '아이디',
                value: userid,
                onChange: ev => setUserid(ev.target.value),
            }),
            e('input', {
                className: 'form-input',
                type: 'text',
                placeholder: '이름',
                value: username,
                onChange: ev => setUsername(ev.target.value),
            }),
            e('input', {
                className: 'form-input',
                type: 'password',
                placeholder: '비밀번호',
                value: password,
                onChange: ev => setPassword(ev.target.value),
            })
        ),
        e('div', { className: 'form-check-row' },
            e('input', {
                type: 'checkbox',
                id: 'add-is-admin',
                checked: isAdmin,
                onChange: ev => setIsAdmin(ev.target.checked),
            }),
            e('label', { htmlFor: 'add-is-admin' }, '관리자 권한 부여')
        ),
        e('button', {
            className: 'btn-add',
            onClick: handleAdd,
            disabled: saving,
        }, saving ? '추가 중...' : '계정 추가')
    );
}

// ────────────────────────────────────────────────────────────────────
// UserManagementTab - 계정관리 탭
// ────────────────────────────────────────────────────────────────────
function UserManagementTab({ currentUserid }) {
    const [users, setUsers] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [editTarget, setEditTarget] = React.useState(null);
    const [pwTarget, setPwTarget] = React.useState(null);
    const [roleFilter, setRoleFilter] = React.useState('ALL');

    function loadUsers() {
        setLoading(true);
        apiFetch('/api/admin/users')
            .then(data => {
                if (data) setUsers(data);
            })
            .catch(err => console.error('사용자 목록 로드 실패:', err))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => {
        loadUsers();
    }, []);

    function handleDelete(user) {
        if (!confirm(`'${user.username}' 계정을 삭제하시겠습니까?`)) return;
        apiFetch(`/api/admin/users/${user.userid}`, { method: 'DELETE' })
            .then(() => loadUsers())
            .catch(err => alert(err.message || '삭제 실패'));
    }

    function handleEditSaved() {
        setEditTarget(null);
        loadUsers();
    }

    function handlePwSaved() {
        setPwTarget(null);
        alert('비밀번호가 변경되었습니다.');
    }

    // 역할 필터 적용
    const filteredUsers = users.filter(user => {
        if (roleFilter === 'ALL') return true;
        if (roleFilter === 'NONE') return !user.role;
        return user.role === roleFilter;
    });

    return e('div', { className: 'admin-content' },

        // ① 신규 계정 추가 폼 (최상단)
        e(AddUserForm, { onAdded: loadUsers }),

        // ② 계정 목록 헤더: 제목(좌) + 역할 필터(우)
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '계정 목록'),
            e('select', {
                className: 'role-filter-select',
                value: roleFilter,
                onChange: ev => setRoleFilter(ev.target.value),
            },
                ROLE_FILTER_OPTIONS.map(opt =>
                    e('option', { key: opt.value, value: opt.value }, opt.label)
                )
            )
        ),

        // ③ 사용자 카드 목록
        loading
            ? e('div', { className: 'loading-text' }, '불러오는 중...')
            : filteredUsers.length === 0
                ? e('div', { className: 'loading-text' }, '해당 역할의 계정이 없습니다.')
                : e('div', { className: 'user-list' },
                    ...filteredUsers.map(user =>
                        e(UserCard, {
                            key: user.userid,
                            user,
                            currentUserid,
                            onEdit: u => setEditTarget(u),
                            onChangePassword: u => setPwTarget(u),
                            onDelete: handleDelete,
                        })
                    )
                ),

        // 수정 팝업
        editTarget && e(EditUserPopup, {
            user: editTarget,
            onClose: () => setEditTarget(null),
            onSaved: handleEditSaved,
        }),

        // 비밀번호 변경 팝업
        pwTarget && e(ChangePasswordPopup, {
            user: pwTarget,
            onClose: () => setPwTarget(null),
            onSaved: handlePwSaved,
        })
    );
}

// ────────────────────────────────────────────────────────────────────
// AddSpecialPopup - 특수예약 추가 모달
// ────────────────────────────────────────────────────────────────────
function AddSpecialPopup({ onClose, onSaved }) {
    const [reservationDate, setReservationDate] = React.useState('');
    const [reservationTime, setReservationTime] = React.useState('');
    const [customerName, setCustomerName] = React.useState('');
    const [peopleCount, setPeopleCount] = React.useState('');
    const [contactInfo, setContactInfo] = React.useState('');
    const [notes, setNotes] = React.useState('');
    const [error, setError] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    async function handleSave() {
        if (!reservationDate.trim()) {
            setError('예약 날짜를 입력해주세요.');
            return;
        }
        if (!customerName.trim()) {
            setError('예약자명을 입력해주세요.');
            return;
        }
        setSaving(true);
        setError('');
        try {
            await apiFetch('/api/admin/special', {
                method: 'POST',
                body: JSON.stringify({
                    reservationDate: reservationDate.trim(),
                    reservationTime: reservationTime.trim(),
                    customerName: customerName.trim(),
                    peopleCount: peopleCount ? parseInt(peopleCount, 10) : null,
                    contactInfo: contactInfo.trim(),
                    notes: notes.trim(),
                }),
            });
            onSaved();
        } catch (err) {
            setError(err.message || '추가 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, '특수예약 추가'),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                e('input', {
                    className: 'form-input',
                    type: 'date',
                    placeholder: '예약 날짜',
                    value: reservationDate,
                    onChange: ev => setReservationDate(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'time',
                    placeholder: '예약 시간',
                    value: reservationTime,
                    onChange: ev => setReservationTime(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '예약자명',
                    value: customerName,
                    onChange: ev => setCustomerName(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'number',
                    placeholder: '인원수',
                    value: peopleCount,
                    onChange: ev => setPeopleCount(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '연락처',
                    value: contactInfo,
                    onChange: ev => setContactInfo(ev.target.value),
                }),
                e('textarea', {
                    className: 'form-input form-textarea',
                    placeholder: '비고',
                    value: notes,
                    onChange: ev => setNotes(ev.target.value),
                    rows: 3,
                })
            ),
            e('div', { className: 'popup-actions' },
                e('button', { className: 'btn-cancel', onClick: onClose }, '취소'),
                e('button', {
                    className: 'btn-save',
                    onClick: handleSave,
                    disabled: saving,
                }, saving ? '추가 중...' : '추가')
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// EditSpecialPopup - 특수예약 수정 모달
// ────────────────────────────────────────────────────────────────────
function EditSpecialPopup({ special, onClose, onSaved }) {
    const [reservationDate, setReservationDate] = React.useState(special.reservationDate || '');
    const [reservationTime, setReservationTime] = React.useState(special.reservationTime || '');
    const [customerName, setCustomerName] = React.useState(special.customerName || '');
    const [peopleCount, setPeopleCount] = React.useState(special.peopleCount != null ? String(special.peopleCount) : '');
    const [contactInfo, setContactInfo] = React.useState(special.contactInfo || '');
    const [notes, setNotes] = React.useState(special.notes || '');
    const [error, setError] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    async function handleSave() {
        if (!reservationDate.trim()) {
            setError('예약 날짜를 입력해주세요.');
            return;
        }
        if (!customerName.trim()) {
            setError('예약자명을 입력해주세요.');
            return;
        }
        setSaving(true);
        setError('');
        try {
            await apiFetch(`/api/admin/special/${special.id}`, {
                method: 'PUT',
                body: JSON.stringify({
                    reservationDate: reservationDate.trim(),
                    reservationTime: reservationTime.trim(),
                    customerName: customerName.trim(),
                    peopleCount: peopleCount ? parseInt(peopleCount, 10) : null,
                    contactInfo: contactInfo.trim(),
                    notes: notes.trim(),
                }),
            });
            onSaved();
        } catch (err) {
            setError(err.message || '수정 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, '특수예약 수정'),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                e('input', {
                    className: 'form-input',
                    type: 'date',
                    placeholder: '예약 날짜',
                    value: reservationDate,
                    onChange: ev => setReservationDate(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'time',
                    placeholder: '예약 시간',
                    value: reservationTime,
                    onChange: ev => setReservationTime(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '예약자명',
                    value: customerName,
                    onChange: ev => setCustomerName(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'number',
                    placeholder: '인원수',
                    value: peopleCount,
                    onChange: ev => setPeopleCount(ev.target.value),
                }),
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '연락처',
                    value: contactInfo,
                    onChange: ev => setContactInfo(ev.target.value),
                }),
                e('textarea', {
                    className: 'form-input form-textarea',
                    placeholder: '비고',
                    value: notes,
                    onChange: ev => setNotes(ev.target.value),
                    rows: 3,
                })
            ),
            e('div', { className: 'popup-actions' },
                e('button', { className: 'btn-cancel', onClick: onClose }, '취소'),
                e('button', {
                    className: 'btn-save',
                    onClick: handleSave,
                    disabled: saving,
                }, saving ? '저장 중...' : '저장')
            )
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// SpecialCard - 특수예약 카드 1개
// ────────────────────────────────────────────────────────────────────
function SpecialCard({ special, onEdit, onDelete }) {
    // 날짜 포맷: YYYY-MM-DD → MM/DD(요일)
    function formatDate(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr + 'T00:00:00');
        const days = ['일', '월', '화', '수', '목', '금', '토'];
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return mm + '/' + dd + '(' + days[d.getDay()] + ')';
    }

    return e('div', { className: 'special-card' },
        e('div', { className: 'special-card-top' },
            e('span', { className: 'special-date' }, formatDate(special.reservationDate)),
            special.reservationTime && e('span', { className: 'special-time' }, special.reservationTime),
            e('span', { className: 'special-name' }, special.customerName),
            special.peopleCount != null && e('span', { className: 'special-count' }, special.peopleCount + '명')
        ),
        special.notes && e('div', { className: 'special-card-notes' }, special.notes),
        e('div', { className: 'special-card-actions' },
            e('button', { className: 'btn-sm btn-edit', onClick: () => onEdit(special) }, '수정'),
            e('button', { className: 'btn-sm btn-del', onClick: () => onDelete(special) }, '삭제')
        )
    );
}

// ────────────────────────────────────────────────────────────────────
// SpecialReservationTab - 특수예약관리 탭
// ────────────────────────────────────────────────────────────────────
function SpecialReservationTab() {
    const [specials, setSpecials] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [showAddPopup, setShowAddPopup] = React.useState(false);
    const [editTarget, setEditTarget] = React.useState(null);

    function loadSpecials() {
        setLoading(true);
        apiFetch('/api/admin/special')
            .then(data => {
                if (data) setSpecials(data);
            })
            .catch(err => console.error('특수예약 목록 로드 실패:', err))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => {
        loadSpecials();
    }, []);

    function handleDelete(special) {
        if (!confirm(`'${special.customerName}' 예약을 삭제하시겠습니까?`)) return;
        apiFetch(`/api/admin/special/${special.id}`, { method: 'DELETE' })
            .then(() => loadSpecials())
            .catch(err => alert(err.message || '삭제 실패'));
    }

    function handleAddSaved() {
        setShowAddPopup(false);
        loadSpecials();
    }

    function handleEditSaved() {
        setEditTarget(null);
        loadSpecials();
    }

    return e('div', { className: 'admin-content' },

        // ① 특수예약 추가 버튼
        e('button', {
            className: 'btn-add-special',
            onClick: () => setShowAddPopup(true),
        }, '+ 특수예약 추가'),

        // ② 목록 헤더
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '특수예약 목록')
        ),

        // ③ 특수예약 카드 목록
        loading
            ? e('div', { className: 'loading-text' }, '불러오는 중...')
            : specials.length === 0
                ? e('div', { className: 'loading-text' }, '등록된 특수예약이 없습니다.')
                : e('div', { className: 'special-list' },
                    ...specials.map(s =>
                        e(SpecialCard, {
                            key: s.id,
                            special: s,
                            onEdit: sp => setEditTarget(sp),
                            onDelete: handleDelete,
                        })
                    )
                ),

        // 추가 팝업
        showAddPopup && e(AddSpecialPopup, {
            onClose: () => setShowAddPopup(false),
            onSaved: handleAddSaved,
        }),

        // 수정 팝업
        editTarget && e(EditSpecialPopup, {
            special: editTarget,
            onClose: () => setEditTarget(null),
            onSaved: handleEditSaved,
        })
    );
}

// ────────────────────────────────────────────────────────────────────
// AdminPage - 최상위 컴포넌트
// ────────────────────────────────────────────────────────────────────
function AdminPage() {
    const [activeTab, setActiveTab] = React.useState('users');
    const [userInfo, setUserInfo] = React.useState(null);

    React.useEffect(() => {
        apiFetch('/api/user/info')
            .then(data => {
                if (data) {
                    setUserInfo({
                        userid: data.userid || data.userId || '',
                        userName: data.username || data.userName || '',
                        admin: data.isAdmin !== undefined ? data.isAdmin : (data.admin || false),
                    });
                }
            })
            .catch(() => {
                window.location.href = '/';
            });
    }, []);

    function renderTabContent() {
        switch (activeTab) {
            case 'users':
                return e(UserManagementTab, {
                    currentUserid: userInfo ? userInfo.userid : '',
                });
            case 'special':
                return e(SpecialReservationTab, null);
            default:
                return null;
        }
    }

    return e('div', { className: 'admin-wrap' },
        e(AdminHeader, { userName: userInfo ? userInfo.userName : '' }),
        e(TabBar, { activeTab, onTabChange: setActiveTab }),
        renderTabContent()
    );
}

// ────────────────────────────────────────────────────────────────────
// 렌더링
// ────────────────────────────────────────────────────────────────────
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(e(AdminPage, null));
