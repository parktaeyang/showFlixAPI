/**
 * admin-react.js
 * CDN React (no JSX) 기반 관리자 페이지 - SPA 통합
 *
 * 구조:
 *  AdminPage
 *  ├── AdminHeader
 *  ├── TabBar (고정 9개 + 월별 6개 = 15개 탭, 그룹별)
 *  ├── MonthNavigator (월별 탭 선택 시만 노출)
 *  ├── [고정 탭]
 *  │   ├── AnnualCalendarTab (연간일정)
 *  │   ├── UserManagementTab (계정/복지)
 *  │   ├── SpecialReservationTab (특수예약)
 *  │   ├── AngelCancelTab (엔젤쇼취소)
 *  │   ├── PartnersTab (협력업체)
 *  │   ├── HealthCertTab (보건증확인)
 *  │   ├── MembershipTab (MEMBERSHIP)
 *  │   ├── BeerSelectTab (맥주셀렉)
 *  │   └── XmasSeatsTab (크리스마스석)
 *  └── [월별 탭]
 *      ├── WorkDiaryTab (업무일지)
 *      ├── ActorTab (배우)
 *      ├── VoucherTab (바우처)
 *      ├── TipTab (팁)
 *      ├── StaffTab (스탭)
 *      └── DailyTab (일일)
 */

const e = React.createElement;

// ════════════════════════════════════════════════════════════════════
// 탭 정의
// ════════════════════════════════════════════════════════════════════

const FIXED_TABS = [
    { key: 'annual-calendar', label: '연간일정' },
    { key: 'accounts',        label: '계정' },
    { key: 'special',         label: '특수예약' },
    { key: 'angel-cancel',    label: '엔젤쇼취소' },
    { key: 'partners',        label: '협력업체' },
    { key: 'health-cert',     label: '보건증확인' },
    { key: 'membership',      label: 'MEMBERSHIP' },
    { key: 'beer-select',     label: '맥주셀렉' },
    { key: 'xmas-seats',      label: '크리스마스석' },
];

const MONTHLY_TABS = [
    { key: 'work-diary', label: '업무일지' },
    { key: 'voucher',    label: '바우처' },
    { key: 'tip',        label: '팁' },
    { key: 'staff',      label: '출근시간 관리' },
    { key: 'daily',      label: '일일', hidden: true },
];

const ALL_TAB_KEYS = new Set([
    ...FIXED_TABS.map(t => t.key),
    ...MONTHLY_TABS.map(t => t.key),
]);

const VISIBLE_TAB_KEYS = new Set([
    ...FIXED_TABS.map(t => t.key),
    ...MONTHLY_TABS.filter(t => !t.hidden).map(t => t.key),
]);

const MONTHLY_TAB_KEYS = new Set(MONTHLY_TABS.map(t => t.key));

// ════════════════════════════════════════════════════════════════════
// 유틸
// ════════════════════════════════════════════════════════════════════

const ACCOUNT_TYPES = [
    { value: 'ACTOR',   label: '배우' },
    { value: 'STAFF',   label: '스텝' },
    { value: 'CAPTAIN', label: '캡틴' },
    { value: 'ADMIN',   label: '관리자' },
];

const ACCOUNT_TYPE_DISPLAY = {
    ACTOR:   '배우',
    STAFF:   '스텝',
    CAPTAIN: '캡틴',
    ADMIN:   '관리자',
};

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

const ROLE_FILTER_OPTIONS = [
    { value: 'ALL',  label: '전체' },
    { value: 'NONE', label: '역할없음' },
    ...Object.entries(ROLE_DISPLAY).map(([value, label]) => ({ value, label })),
];

const DAY_OF_WEEK_KR = ['일', '월', '화', '수', '목', '금', '토'];

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
    if (res.status === 204 || res.headers.get('content-length') === '0') return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        throw new Error((data && data.message) || '요청 실패');
    }
    return data;
}

function todayStr() {
    const d = new Date();
    return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0');
}

function dateStr(year, month, day) {
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

function getDayOfWeekIdx(year, month, day) {
    return new Date(year, month - 1, day).getDay();
}

function getDaysInMonth(year, month) {
    return new Date(year, month, 0).getDate();
}

function parseHours(hoursStr) {
    if (!hoursStr || hoursStr.trim() === '') return 0;
    const n = parseFloat(hoursStr);
    return isNaN(n) ? 0 : n;
}

function formatHours(val) {
    if (!val || val === 0) return '';
    const n = parseFloat(val);
    if (isNaN(n) || n === 0) return '';
    return n % 1 === 0 ? String(n) : n.toFixed(1);
}

// ════════════════════════════════════════════════════════════════════
// AdminHeader
// ════════════════════════════════════════════════════════════════════
function AdminHeader({ userName }) {
    return e('header', { className: 'admin-header' },
        e('div', { className: 'admin-logo' },
            'ShowFlix',
            e('span', null, '관리자')
        ),
        e('div', { style: { display: 'flex', alignItems: 'center', gap: '10px' } },
            userName && e('span', { style: { color: 'rgba(255,255,255,0.8)', fontSize: '0.82rem' } }, userName),
            e('button', {
                className: 'admin-back-btn',
                onClick: () => { window.location.href = '/schedule/calendar'; },
            }, '\u2190 달력으로')
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// TabBar - 그룹별 (관리 / 월별)
// ════════════════════════════════════════════════════════════════════
function TabBar({ activeTab, onTabChange }) {
    return e('div', { className: 'admin-tab-bar-wrap' },
        // 고정 탭 그룹
        e('div', { className: 'admin-tab-group' },
            e('span', { className: 'admin-tab-group-label' }, '관리'),
            e('div', { className: 'admin-tab-group-tabs' },
                FIXED_TABS.map(tab =>
                    e('button', {
                        key: tab.key,
                        className: 'admin-tab' + (activeTab === tab.key ? ' active' : ''),
                        onClick: () => onTabChange(tab.key),
                    }, tab.label)
                )
            )
        ),
        // 월별 탭 그룹
        e('div', { className: 'admin-tab-group' },
            e('span', { className: 'admin-tab-group-label monthly' }, '월별'),
            e('div', { className: 'admin-tab-group-tabs' },
                MONTHLY_TABS.filter(tab => !tab.hidden).map(tab =>
                    e('button', {
                        key: tab.key,
                        className: 'admin-tab' + (activeTab === tab.key ? ' active' : ''),
                        onClick: () => onTabChange(tab.key),
                    }, tab.label)
                )
            )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// MonthNavigator - 월별 탭용 네비게이터
// ════════════════════════════════════════════════════════════════════
function MonthNavigator({ year, month, onMonthChange }) {
    function handlePrev() {
        if (month === 1) onMonthChange(year - 1, 12);
        else onMonthChange(year, month - 1);
    }
    function handleNext() {
        if (month === 12) onMonthChange(year + 1, 1);
        else onMonthChange(year, month + 1);
    }

    return e('div', { className: 'month-navigator' },
        e('button', { className: 'month-nav-btn', onClick: handlePrev }, '\u25C0'),
        e('span', { className: 'month-nav-label' }, `${year}\uB144 ${month}\uC6D4`),
        e('button', { className: 'month-nav-btn', onClick: handleNext }, '\u25B6')
    );
}

// ════════════════════════════════════════════════════════════════════
// EditUserPopup - 계정 수정 팝업
// ════════════════════════════════════════════════════════════════════
function EditUserPopup({ user, onClose, onSaved }) {
    const [username, setUsername]           = React.useState(user.username);
    const [accountType, setAccountType]     = React.useState(user.accountType || 'ACTOR');
    const [availableRoles, setAvailableRoles] = React.useState([]);
    const [selectedRole, setSelectedRole]   = React.useState(user.role || '');
    const [error, setError]                 = React.useState('');
    const [saving, setSaving]               = React.useState(false);

    React.useEffect(() => {
        loadRoles(accountType);
    }, []);

    async function loadRoles(type) {
        try {
            const roleData = await apiFetch(`/api/admin/users/available-roles?accountType=${type}`);
            if (roleData) setAvailableRoles(roleData);
            else setAvailableRoles([]);
        } catch {
            setAvailableRoles([]);
        }
    }

    function handleAccountTypeChange(newType) {
        setAccountType(newType);
        setSelectedRole('');
        loadRoles(newType);
    }

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
                body: JSON.stringify({
                    username: username.trim(),
                    accountType,
                    role: selectedRole || null,
                }),
            });
            onSaved();
        } catch (err) {
            setError(err.message || '수정 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    const hasRoles = availableRoles.length > 0;

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, '계정 수정'),
            e('div', { style: { fontSize: '0.78rem', color: '#9ca3af', marginBottom: '12px' } },
                '아이디: ', e('strong', null, user.userid)
            ),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                e('input', {
                    className: 'form-input',
                    type: 'text',
                    placeholder: '이름',
                    value: username,
                    onChange: ev => setUsername(ev.target.value),
                })
            ),
            e('div', { className: 'form-row' },
                e('select', {
                    className: 'form-input',
                    value: accountType,
                    onChange: ev => handleAccountTypeChange(ev.target.value),
                },
                    ACCOUNT_TYPES.map(at =>
                        e('option', { key: at.value, value: at.value }, at.label)
                    )
                ),
                hasRoles
                    ? e('select', {
                        className: 'form-input',
                        value: selectedRole,
                        onChange: ev => setSelectedRole(ev.target.value),
                    },
                        e('option', { value: '' }, '역할 선택'),
                        availableRoles.map(r =>
                            e('option', { key: r.name, value: r.name }, r.displayName)
                        )
                    )
                    : e('div', {
                        className: 'form-input',
                        style: { color: '#9ca3af', display: 'flex', alignItems: 'center', fontSize: '0.85rem' },
                    }, '역할 없음')
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

// ════════════════════════════════════════════════════════════════════
// ChangePasswordPopup
// ════════════════════════════════════════════════════════════════════
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

// ════════════════════════════════════════════════════════════════════
// UserCard
// ════════════════════════════════════════════════════════════════════
function UserCard({ user, currentUserid, onEdit, onChangePassword, onDelete }) {
    const isMe = user.userid === currentUserid;
    const roleDisplay = getRoleDisplay(user.recentRole || user.role);
    const accountTypeDisplay = user.accountType ? ACCOUNT_TYPE_DISPLAY[user.accountType] : null;

    return e('div', { className: 'user-card' },
        e('div', { className: 'user-card-top' },
            e('span', { className: 'user-card-id' }, user.userid),
            e('span', { className: 'user-card-name' }, user.username),
            e('div', { className: 'user-card-badges' },
                accountTypeDisplay && e('span', { className: 'badge badge-account-type' }, accountTypeDisplay),
                roleDisplay && e('span', { className: 'badge badge-role' }, roleDisplay),
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

// ════════════════════════════════════════════════════════════════════
// AddUserForm
// ════════════════════════════════════════════════════════════════════
function AddUserForm({ onAdded }) {
    const [accountType, setAccountType]     = React.useState('ACTOR');
    const [availableRoles, setAvailableRoles] = React.useState([]);
    const [selectedRole, setSelectedRole]   = React.useState('');
    const [userid, setUserid]               = React.useState('');
    const [username, setUsername]           = React.useState('');
    const [error, setError]                 = React.useState('');
    const [success, setSuccess]             = React.useState('');
    const [saving, setSaving]               = React.useState(false);
    const [loadingId, setLoadingId]         = React.useState(false);

    React.useEffect(() => {
        let cancelled = false;
        setSelectedRole('');
        setLoadingId(true);
        Promise.all([
            apiFetch(`/api/admin/users/next-userid?accountType=${accountType}`),
            apiFetch(`/api/admin/users/available-roles?accountType=${accountType}`)
        ]).then(([idData, roleData]) => {
            if (cancelled) return;
            if (idData) setUserid(idData.nextUserid || '');
            if (roleData) setAvailableRoles(roleData);
            else setAvailableRoles([]);
        }).catch(() => {
            if (!cancelled) setAvailableRoles([]);
        }).finally(() => {
            if (!cancelled) setLoadingId(false);
        });
        return () => { cancelled = true; };
    }, [accountType]);

    async function handleAdd() {
        if (!userid.trim() || !username.trim()) {
            setError('아이디와 이름을 모두 입력해주세요.');
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
                    accountType,
                    role: selectedRole || null,
                }),
            });
            setUsername('');
            setSelectedRole('');
            setSuccess('계정이 추가되었습니다. (기본 비밀번호: showflix)');
            onAdded();
            const idData = await apiFetch(`/api/admin/users/next-userid?accountType=${accountType}`);
            if (idData) setUserid(idData.nextUserid || '');
        } catch (err) {
            setError(err.message || '추가 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    const hasRoles = availableRoles.length > 0;

    return e('div', { className: 'add-form-card' },
        e('div', { className: 'add-form-title' }, '+ 신규 계정 추가'),
        error && e('div', { className: 'msg msg-error' }, error),
        success && e('div', { className: 'msg msg-success' }, success),
        e('div', { className: 'form-row' },
            e('select', {
                className: 'form-input',
                value: accountType,
                onChange: ev => setAccountType(ev.target.value),
            },
                ACCOUNT_TYPES.map(at =>
                    e('option', { key: at.value, value: at.value }, at.label)
                )
            ),
            hasRoles
                ? e('select', {
                    className: 'form-input',
                    value: selectedRole,
                    onChange: ev => setSelectedRole(ev.target.value),
                },
                    e('option', { value: '' }, '역할 선택'),
                    availableRoles.map(r =>
                        e('option', { key: r.name, value: r.name }, r.displayName)
                    )
                )
                : e('div', {
                    className: 'form-input',
                    style: { color: '#9ca3af', display: 'flex', alignItems: 'center', fontSize: '0.85rem' },
                }, '역할 없음')
        ),
        e('div', { className: 'form-row' },
            e('input', {
                className: 'form-input',
                type: 'text',
                placeholder: loadingId ? '생성 중...' : '아이디',
                value: userid,
                onChange: ev => setUserid(ev.target.value),
            }),
            e('input', {
                className: 'form-input',
                type: 'text',
                placeholder: '이름',
                value: username,
                onChange: ev => setUsername(ev.target.value),
            })
        ),
        e('div', { style: { fontSize: '0.78rem', color: '#6b7280', marginBottom: '8px' } },
            '기본 비밀번호: ',
            e('strong', null, 'showflix')
        ),
        e('button', {
            className: 'btn-add',
            onClick: handleAdd,
            disabled: saving || loadingId,
        }, saving ? '추가 중...' : '계정 추가')
    );
}

// ════════════════════════════════════════════════════════════════════
// UserManagementTab - 계정/복지 탭
// ════════════════════════════════════════════════════════════════════
function UserManagementTab({ currentUserid }) {
    const [users, setUsers] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [editTarget, setEditTarget] = React.useState(null);
    const [pwTarget, setPwTarget] = React.useState(null);
    const [roleFilter, setRoleFilter] = React.useState('ALL');
    const [sortBy, setSortBy] = React.useState('userid');
    const [sortDir, setSortDir] = React.useState('asc');

    function loadUsers(by, dir) {
        setLoading(true);
        apiFetch('/api/admin/users?sortBy=' + by + '&sortDir=' + dir)
            .then(data => {
                if (data) setUsers(data);
            })
            .catch(err => console.error('사용자 목록 로드 실패:', err))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => {
        loadUsers(sortBy, sortDir);
    }, [sortBy, sortDir]);

    function handleSort(column) {
        if (sortBy === column) {
            const newDir = sortDir === 'asc' ? 'desc' : 'asc';
            setSortDir(newDir);
        } else {
            setSortBy(column);
            setSortDir('asc');
        }
    }

    function getSortIcon(column) {
        if (sortBy !== column) return ' \u2195';
        return sortDir === 'asc' ? ' \u2191' : ' \u2193';
    }

    function handleDelete(user) {
        if (!confirm(`'${user.username}' 계정을 삭제하시겠습니까?`)) return;
        apiFetch(`/api/admin/users/${user.userid}`, { method: 'DELETE' })
            .then(() => loadUsers(sortBy, sortDir))
            .catch(err => alert(err.message || '삭제 실패'));
    }

    function handleEditSaved() {
        setEditTarget(null);
        loadUsers(sortBy, sortDir);
    }

    function handlePwSaved() {
        setPwTarget(null);
        alert('비밀번호가 변경되었습니다.');
    }

    const filteredUsers = users.filter(user => {
        if (roleFilter === 'ALL') return true;
        if (roleFilter === 'NONE') return !user.role;
        return user.role === roleFilter;
    });

    return e('div', { className: 'admin-content' },
        e(AddUserForm, { onAdded: () => loadUsers(sortBy, sortDir) }),
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '계정 목록'),
            e('div', { style: { display: 'flex', alignItems: 'center', gap: '6px' } },
                e('button', {
                    className: 'sort-btn' + (sortBy === 'userid' ? ' sort-btn-active' : ''),
                    onClick: () => handleSort('userid'),
                }, '아이디순' + getSortIcon('userid')),
                e('button', {
                    className: 'sort-btn' + (sortBy === 'username' ? ' sort-btn-active' : ''),
                    onClick: () => handleSort('username'),
                }, '이름순' + getSortIcon('username')),
                e('select', {
                    className: 'role-filter-select',
                    value: roleFilter,
                    onChange: ev => setRoleFilter(ev.target.value),
                },
                    ROLE_FILTER_OPTIONS.map(opt =>
                        e('option', { key: opt.value, value: opt.value }, opt.label)
                    )
                )
            )
        ),
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
        editTarget && e(EditUserPopup, {
            user: editTarget,
            onClose: () => setEditTarget(null),
            onSaved: handleEditSaved,
        }),
        pwTarget && e(ChangePasswordPopup, {
            user: pwTarget,
            onClose: () => setPwTarget(null),
            onSaved: handlePwSaved,
        })
    );
}

// ════════════════════════════════════════════════════════════════════
// AddSpecialPopup
// ════════════════════════════════════════════════════════════════════
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
                    className: 'form-input', type: 'date',
                    value: reservationDate,
                    onChange: ev => setReservationDate(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'time',
                    value: reservationTime,
                    onChange: ev => setReservationTime(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'text', placeholder: '예약자명',
                    value: customerName,
                    onChange: ev => setCustomerName(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'number', placeholder: '인원수',
                    value: peopleCount,
                    onChange: ev => setPeopleCount(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'text', placeholder: '연락처',
                    value: contactInfo,
                    onChange: ev => setContactInfo(ev.target.value),
                }),
                e('textarea', {
                    className: 'form-input form-textarea', placeholder: '비고',
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

// ════════════════════════════════════════════════════════════════════
// EditSpecialPopup
// ════════════════════════════════════════════════════════════════════
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
                    className: 'form-input', type: 'date',
                    value: reservationDate,
                    onChange: ev => setReservationDate(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'time',
                    value: reservationTime,
                    onChange: ev => setReservationTime(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'text', placeholder: '예약자명',
                    value: customerName,
                    onChange: ev => setCustomerName(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'number', placeholder: '인원수',
                    value: peopleCount,
                    onChange: ev => setPeopleCount(ev.target.value),
                }),
                e('input', {
                    className: 'form-input', type: 'text', placeholder: '연락처',
                    value: contactInfo,
                    onChange: ev => setContactInfo(ev.target.value),
                }),
                e('textarea', {
                    className: 'form-input form-textarea', placeholder: '비고',
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

// ════════════════════════════════════════════════════════════════════
// SpecialCard
// ════════════════════════════════════════════════════════════════════
function SpecialCard({ special, onEdit, onDelete }) {
    function formatDate(ds) {
        if (!ds) return '';
        const d = new Date(ds + 'T00:00:00');
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

// ════════════════════════════════════════════════════════════════════
// SpecialReservationTab - 특수예약 탭
// ════════════════════════════════════════════════════════════════════
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
        e('button', {
            className: 'btn-add-special',
            onClick: () => setShowAddPopup(true),
        }, '+ 특수예약 추가'),
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '특수예약 목록')
        ),
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
        showAddPopup && e(AddSpecialPopup, {
            onClose: () => setShowAddPopup(false),
            onSaved: handleAddSaved,
        }),
        editTarget && e(EditSpecialPopup, {
            special: editTarget,
            onClose: () => setEditTarget(null),
            onSaved: handleEditSaved,
        })
    );
}

// ════════════════════════════════════════════════════════════════════
// VoucherTipRow
// ════════════════════════════════════════════════════════════════════
// ════════════════════════════════════════════════════════════════════
// VtCell - 바우처/팁 셀 입력 컴포넌트 (SsCell 패턴)
// ════════════════════════════════════════════════════════════════════

function VtCell({ userId, dateKey, value, isChanged, mode, onCellChange }) {
    const [editing, setEditing] = React.useState(false);
    const [inputVal, setInputVal] = React.useState(value > 0 ? String(value) : '');
    const inputRef = React.useRef(null);

    React.useEffect(() => {
        if (!editing) {
            setInputVal(value > 0 ? String(value) : '');
        }
    }, [value, editing]);

    function handleEmptyClick() {
        setEditing(true);
        setInputVal('');
        setTimeout(() => inputRef.current && inputRef.current.focus(), 0);
    }

    function handleBlur() {
        setEditing(false);
        const trimmed = inputVal.trim();
        let validated = 0;
        if (trimmed !== '') {
            const num = parseInt(trimmed, 10);
            if (!isNaN(num) && num > 0) validated = num;
        }
        setInputVal(validated > 0 ? String(validated) : '');
        onCellChange(userId, dateKey, validated);
    }

    function handleKeyDown(ev) {
        if (ev.key === 'Enter') ev.target.blur();
        if (ev.key === 'Escape') {
            setInputVal(value > 0 ? String(value) : '');
            setEditing(false);
        }
    }

    function handleInputChange(ev) {
        const val = ev.target.value.replace(/[^0-9]/g, '');
        setInputVal(val);
    }

    if ((!value || value === 0) && !editing) {
        return e('span', {
            className: 'ss-cell-empty',
            onClick: handleEmptyClick,
            title: '클릭하여 입력',
        }, '-');
    }

    return e('input', {
        ref: inputRef,
        className: 'ss-cell-input',
        type: 'text',
        inputMode: 'numeric',
        value: editing ? inputVal : (value > 0 ? String(value) : ''),
        onChange: handleInputChange,
        onFocus: () => { setEditing(true); setInputVal(value > 0 ? String(value) : ''); },
        onBlur: handleBlur,
        onKeyDown: handleKeyDown,
        placeholder: '',
    });
}

// ════════════════════════════════════════════════════════════════════
// VtTable - 바우처/팁 엑셀형 그리드 테이블 (SsTable 패턴)
// 행=날짜(1~31), 열=배우별 바우처 또는 팁 입력
// ════════════════════════════════════════════════════════════════════

function VtTable({ year, month, mode, actors, gridData, changes, onCellChange }) {
    const field = mode; // 'voucher' | 'tip'
    const daysInMonth = getDaysInMonth(year, month);
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    if (!actors || actors.length === 0) {
        return e('div', { className: 'ss-empty' }, '배우 데이터가 없습니다.');
    }

    // gridData 구조: date → (userId → {voucher, tip})
    return e('div', { className: 'ss-table-wrap' },
        e('table', { className: 'ss-table' },
            e('thead', null,
                e('tr', null,
                    e('th', { className: 'ss-date-col' }, '날짜'),
                    ...actors.map(actor =>
                        e('th', { key: actor.userId, className: 'ss-user-col' }, actor.userName)
                    ),
                    e('th', { className: 'ss-total-col ss-group-border-left' }, '합계')
                )
            ),
            e('tbody', null,
                days.map(d => {
                    const dk = dateStr(year, month, d);
                    const dow = getDayOfWeekIdx(year, month, d);
                    const label = `${d}일(${DAY_OF_WEEK_KR[dow]})`;
                    let dayTotal = 0;
                    const dateMap = (gridData && gridData[dk]) || {};

                    const actorCells = actors.map(actor => {
                        const cellData = dateMap[actor.userId] || {};
                        const cellVal = cellData[field] || 0;
                        const changed = changes.has(`${actor.userId}|${dk}`);
                        dayTotal += cellVal;
                        let tdCls = 'ss-user-col';
                        if (changed) tdCls += ' ss-changed';
                        return e('td', { key: actor.userId, className: tdCls },
                            e(VtCell, {
                                key: `${actor.userId}|${dk}`,
                                userId: actor.userId, dateKey: dk,
                                value: cellVal, isChanged: changed,
                                mode: field,
                                onCellChange,
                            })
                        );
                    });

                    let trCls = '';
                    if (dow === 0) trCls = 'ss-sun-row';
                    else if (dow === 6) trCls = 'ss-sat-row';

                    return e('tr', { key: dk, className: trCls },
                        e('td', { className: 'ss-date-col' }, label),
                        ...actorCells,
                        e('td', { className: 'ss-total-col ss-group-border-left' },
                            dayTotal > 0 ? dayTotal.toLocaleString('ko-KR') : '-'
                        )
                    );
                }),

                // 합계 행
                (() => {
                    let grandTotal = 0;
                    const actorTotalCells = actors.map(actor => {
                        let userTotal = 0;
                        days.forEach(d => {
                            const dk = dateStr(year, month, d);
                            const dateMap = (gridData && gridData[dk]) || {};
                            const cellData = dateMap[actor.userId] || {};
                            userTotal += cellData[field] || 0;
                        });
                        grandTotal += userTotal;
                        return e('td', { key: actor.userId, className: 'ss-total-col' },
                            userTotal > 0 ? userTotal.toLocaleString('ko-KR') : '-'
                        );
                    });
                    return e('tr', { key: 'total', className: 'ss-total-row' },
                        e('td', { className: 'ss-date-col ss-total-label' }, '합계'),
                        ...actorTotalCells,
                        e('td', { className: 'ss-total-col ss-grand-total ss-group-border-left' },
                            grandTotal > 0 ? grandTotal.toLocaleString('ko-KR') : '-'
                        )
                    );
                })()
            )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// VoucherTipTabInner - 바우처/팁 엑셀형 테이블 (StaffTab 패턴)
// mode: 'voucher' | 'tip'
// API: GET /api/admin/voucher/monthly → {actors, data}
//      POST /api/admin/voucher/monthly/save → [{userId, date, voucher, tip}]
// ════════════════════════════════════════════════════════════════════

function VoucherTipTabInner({ mode, year, month }) {
    const [actors, setActors] = React.useState([]);
    const [gridData, setGridData] = React.useState({});
    const [changes, setChanges] = React.useState(new Set());
    const [loading, setLoading] = React.useState(false);
    const [saving, setSaving] = React.useState(false);
    const [toast, setToast] = React.useState(null);
    const toastTimerRef = React.useRef(null);

    React.useEffect(() => {
        loadMonthData(year, month);
    }, [year, month]);

    async function loadMonthData(y, m) {
        setLoading(true);
        try {
            const result = await apiFetch(`/api/admin/voucher/monthly?year=${y}&month=${m}`);
            if (!result) return;
            setActors(result.actors || []);
            setGridData(result.data || {});
            setChanges(new Set());
        } catch (err) {
            showToast(err.message || '데이터 조회 실패', 'error');
        } finally {
            setLoading(false);
        }
    }

    // gridData 구조: date → (userId → {voucher, tip})
    function handleCellChange(userId, dateKey, value) {
        setGridData(prev => {
            const next = { ...prev };
            if (!next[dateKey]) next[dateKey] = {};
            const cellData = next[dateKey][userId] ? { ...next[dateKey][userId] } : { voucher: 0, tip: 0 };
            cellData[mode] = value;
            next[dateKey] = { ...next[dateKey], [userId]: cellData };
            return next;
        });
        setChanges(prev => {
            const next = new Set(prev);
            next.add(`${userId}|${dateKey}`);
            return next;
        });
    }

    async function handleSave() {
        if (changes.size === 0) return;

        const actorMap = {};
        actors.forEach(a => { actorMap[a.userId] = a.userName; });

        const items = [];
        for (const key of changes) {
            const [userId, dk] = key.split('|');
            const dateMap = (gridData[dk] && gridData[dk][userId]) || { voucher: 0, tip: 0 };
            items.push({
                userId,
                userName: actorMap[userId] || '',
                date: dk,
                voucher: dateMap.voucher || 0,
                tip: dateMap.tip || 0,
            });
        }

        setSaving(true);
        try {
            await apiFetch('/api/admin/voucher/monthly/save', {
                method: 'POST',
                body: JSON.stringify({ entries: items }),
            });
            setChanges(new Set());
            showToast('저장되었습니다.', 'success');
        } catch (err) {
            showToast(err.message || '저장 실패', 'error');
        } finally {
            setSaving(false);
        }
    }

    function handleExport() {
        if (changes.size > 0 && !window.confirm('저장하지 않은 변경사항이 있습니다. 그대로 다운로드하시겠습니까?')) return;
        window.location.href = `/api/admin/voucher/monthly/export?year=${year}&month=${month}`;
    }

    function showToast(message, type) {
        if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
        setToast({ message, type });
        toastTimerRef.current = setTimeout(() => setToast(null), 2800);
    }

    const totalChanges = changes.size;
    const modeLabel = mode === 'voucher' ? '바우처' : '팁';
    const unit = mode === 'voucher' ? '개' : '원';

    return e('div', { className: 'admin-content voucher-content' },
        // 액션바
        e('div', { className: 'staff-action-bar' },
            e('span', { className: 'admin-tab-group-label' }, `${year}년 ${month}월 ${modeLabel} (${unit})`),
            totalChanges > 0 && e('span', { className: 'ss-changes-badge' }, `변경 ${totalChanges}건`),
            e('button', {
                className: 'btn-sm btn-edit',
                onClick: handleSave,
                disabled: saving || totalChanges === 0,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, saving ? '저장 중...' : '저장'),
            e('button', {
                className: 'btn-sm btn-pw',
                onClick: handleExport,
                disabled: saving,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, '엑셀 다운로드')
        ),

        // 테이블
        loading
            ? e('div', { className: 'loading-text' }, '데이터를 불러오는 중...')
            : e(VtTable, {
                year, month, mode, actors, gridData, changes,
                onCellChange: handleCellChange,
            }),

        // 토스트
        toast && e('div', { className: `ss-toast ${toast.type || ''}` }, toast.message)
    );
}

// ════════════════════════════════════════════════════════════════════
// StaffTab - 스탭 출근시간 관리 (schedule-summary 통합)
// ════════════════════════════════════════════════════════════════════

function SsCell({ userId, dateKey, hoursValue, isChanged, onCellChange }) {
    const [editing, setEditing] = React.useState(false);
    const [inputVal, setInputVal] = React.useState(formatHours(hoursValue));
    const inputRef = React.useRef(null);

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
        if (ev.key === 'Enter') ev.target.blur();
        if (ev.key === 'Escape') {
            setInputVal(formatHours(hoursValue));
            setEditing(false);
        }
    }

    function handleInputChange(ev) {
        const val = ev.target.value;
        if (val === '' || /^\d*\.?\d{0,1}$/.test(val)) {
            setInputVal(val);
        }
    }

    const hasValue = hoursValue && parseHours(hoursValue) > 0;

    if (!hasValue && !editing) {
        return e('span', {
            className: 'ss-cell-empty',
            onClick: handleEmptyClick,
            title: '클릭하여 입력',
        }, '-');
    }

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

function SsTable({ year, month, staffUsers, actorUsers, gridData,
    staffRemarksData, actorRemarksData,
    staffRemarksChanges, actorRemarksChanges,
    changes, onCellChange, onStaffRemarksChange, onActorRemarksChange }) {

    const daysInMonth = getDaysInMonth(year, month);
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);

    if ((!staffUsers || staffUsers.length === 0) && (!actorUsers || actorUsers.length === 0)) {
        return e('div', { className: 'ss-empty' }, '직원 데이터가 없습니다.');
    }

    return e('div', { className: 'ss-table-wrap' },
        e('table', { className: 'ss-table' },
            e('thead', null,
                e('tr', null,
                    e('th', { className: 'ss-date-col' }, '날짜'),
                    ...staffUsers.map(user =>
                        e('th', { key: user.userId, className: 'ss-user-col ss-staff-col' }, user.userName)
                    ),
                    e('th', { className: 'ss-remarks-col ss-group-border-left' }, '특이사항(스탭)'),
                    ...actorUsers.map(user =>
                        e('th', { key: user.userId, className: 'ss-user-col ss-actor-col' }, user.userName)
                    ),
                    e('th', { className: 'ss-remarks-col ss-group-border-left' }, '특이사항(배우)'),
                    e('th', { className: 'ss-total-col ss-group-border-left' }, '합계')
                )
            ),
            e('tbody', null,
                days.map(d => {
                    const dk = dateStr(year, month, d);
                    const dow = getDayOfWeekIdx(year, month, d);
                    const label = `${d}일(${DAY_OF_WEEK_KR[dow]})`;
                    let dayTotal = 0;

                    const staffCells = staffUsers.map(user => {
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
                                userId: user.userId, dateKey: dk,
                                hoursValue: hoursVal, isChanged: changed,
                                onCellChange,
                            })
                        );
                    });

                    const actorCells = actorUsers.map(user => {
                        const userMap = (gridData && gridData[user.userId]) || {};
                        const hoursVal = userMap[dk] || '';
                        const changed = changes.has(`${user.userId}|${dk}`);
                        const hrs = parseHours(hoursVal);
                        if (hrs > 0) dayTotal += hrs;
                        let tdCls = 'ss-user-col ss-actor-col';
                        if (changed) tdCls += ' ss-changed';
                        return e('td', { key: user.userId, className: tdCls },
                            e(SsCell, {
                                key: `${user.userId}|${dk}`,
                                userId: user.userId, dateKey: dk,
                                hoursValue: hoursVal, isChanged: changed,
                                onCellChange,
                            })
                        );
                    });

                    let trCls = '';
                    if (dow === 0) trCls = 'ss-sun-row';
                    else if (dow === 6) trCls = 'ss-sat-row';

                    const staffRemarksText = (staffRemarksData && staffRemarksData[dk]) || '';
                    const actorRemarksText = (actorRemarksData && actorRemarksData[dk]) || '';
                    const staffRemarksChanged = staffRemarksChanges && staffRemarksChanges.has(dk);
                    const actorRemarksChanged = actorRemarksChanges && actorRemarksChanges.has(dk);

                    return e('tr', { key: dk, className: trCls },
                        e('td', { className: 'ss-date-col' }, label),
                        ...staffCells,
                        e('td', { className: 'ss-remarks-col ss-group-border-left' + (staffRemarksChanged ? ' ss-changed' : '') },
                            e('input', {
                                className: 'ss-remarks-input',
                                type: 'text',
                                value: staffRemarksText,
                                placeholder: '',
                                onChange: ev => onStaffRemarksChange(dk, ev.target.value),
                            })
                        ),
                        ...actorCells,
                        e('td', { className: 'ss-remarks-col ss-group-border-left' + (actorRemarksChanged ? ' ss-changed' : '') },
                            e('input', {
                                className: 'ss-remarks-input',
                                type: 'text',
                                value: actorRemarksText,
                                placeholder: '',
                                onChange: ev => onActorRemarksChange(dk, ev.target.value),
                            })
                        ),
                        e('td', { className: 'ss-total-col ss-group-border-left' },
                            dayTotal > 0
                                ? (dayTotal % 1 === 0 ? dayTotal : dayTotal.toFixed(1))
                                : '-'
                        )
                    );
                }),

                // 합계 행
                (() => {
                    let grandTotal = 0;
                    const staffTotalCells = staffUsers.map(user => {
                        const userMap = (gridData && gridData[user.userId]) || {};
                        let userTotal = 0;
                        days.forEach(d => {
                            const dk = dateStr(year, month, d);
                            userTotal += parseHours(userMap[dk] || '');
                        });
                        grandTotal += userTotal;
                        return e('td', { key: user.userId, className: 'ss-total-col' },
                            userTotal > 0 ? (userTotal % 1 === 0 ? userTotal : userTotal.toFixed(1)) : '-'
                        );
                    });
                    const actorTotalCells = actorUsers.map(user => {
                        const userMap = (gridData && gridData[user.userId]) || {};
                        let userTotal = 0;
                        days.forEach(d => {
                            const dk = dateStr(year, month, d);
                            userTotal += parseHours(userMap[dk] || '');
                        });
                        grandTotal += userTotal;
                        return e('td', { key: user.userId, className: 'ss-total-col ss-actor-col' },
                            userTotal > 0 ? (userTotal % 1 === 0 ? userTotal : userTotal.toFixed(1)) : '-'
                        );
                    });
                    return e('tr', { key: 'total', className: 'ss-total-row' },
                        e('td', { className: 'ss-date-col ss-total-label' }, '합계'),
                        ...staffTotalCells,
                        e('td', { className: 'ss-remarks-col ss-group-border-left' }, ''),
                        ...actorTotalCells,
                        e('td', { className: 'ss-remarks-col ss-group-border-left' }, ''),
                        e('td', { className: 'ss-total-col ss-grand-total ss-group-border-left' },
                            grandTotal > 0 ? (grandTotal % 1 === 0 ? grandTotal : grandTotal.toFixed(1)) : '-'
                        )
                    );
                })()
            )
        )
    );
}

function StaffTab({ year, month }) {
    const [staffUsers, setStaffUsers] = React.useState([]);
    const [actorUsers, setActorUsers] = React.useState([]);
    const [gridData, setGridData] = React.useState({});
    const [staffRemarksData, setStaffRemarksData] = React.useState({});
    const [actorRemarksData, setActorRemarksData] = React.useState({});
    const [changes, setChanges] = React.useState(new Set());
    const [staffRemarksChanges, setStaffRemarksChanges] = React.useState(new Set());
    const [actorRemarksChanges, setActorRemarksChanges] = React.useState(new Set());

    const [loading, setLoading] = React.useState(false);
    const [saving, setSaving] = React.useState(false);
    const [toast, setToast] = React.useState(null);
    const toastTimerRef = React.useRef(null);

    React.useEffect(() => {
        loadMonthData(year, month);
    }, [year, month]);

    async function loadMonthData(y, m) {
        setLoading(true);
        try {
            const result = await apiFetch(`/api/admin/schedule-summary/month?year=${y}&month=${m}`);
            if (!result) return;
            setStaffUsers(result.staffUsers || []);
            setActorUsers(result.actorUsers || []);
            setGridData(result.data || {});
            setStaffRemarksData(result.staffRemarks || {});
            setActorRemarksData(result.actorRemarks || {});
            setChanges(new Set());
            setStaffRemarksChanges(new Set());
            setActorRemarksChanges(new Set());
        } catch (err) {
            showToast(err.message || '데이터 조회 실패', 'error');
        } finally {
            setLoading(false);
        }
    }

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

    function handleStaffRemarksChange(dateKey, value) {
        setStaffRemarksData(prev => ({ ...prev, [dateKey]: value }));
        setStaffRemarksChanges(prev => {
            const next = new Set(prev);
            next.add(dateKey);
            return next;
        });
    }

    function handleActorRemarksChange(dateKey, value) {
        setActorRemarksData(prev => ({ ...prev, [dateKey]: value }));
        setActorRemarksChanges(prev => {
            const next = new Set(prev);
            next.add(dateKey);
            return next;
        });
    }

    async function handleSave() {
        if (changes.size === 0 && staffRemarksChanges.size === 0 && actorRemarksChanges.size === 0) return;

        const items = [];
        for (const key of changes) {
            const [userId, dk] = key.split('|');
            const hours = (gridData[userId] && gridData[userId][dk]) || '';
            items.push({ userId, date: dk, hours, remarks: null });
        }
        for (const dk of staffRemarksChanges) {
            items.push({ userId: '__remarks_STAFF__', date: dk, hours: '0', remarks: staffRemarksData[dk] || '' });
        }
        for (const dk of actorRemarksChanges) {
            items.push({ userId: '__remarks_ACTOR__', date: dk, hours: '0', remarks: actorRemarksData[dk] || '' });
        }

        setSaving(true);
        try {
            await apiFetch('/api/admin/schedule-summary/save', {
                method: 'POST',
                body: JSON.stringify(items),
            });
            setChanges(new Set());
            setStaffRemarksChanges(new Set());
            setActorRemarksChanges(new Set());
            showToast('저장되었습니다.', 'success');
        } catch (err) {
            showToast(err.message || '저장 실패', 'error');
        } finally {
            setSaving(false);
        }
    }

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

    function showToast(message, type) {
        if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
        setToast({ message, type });
        toastTimerRef.current = setTimeout(() => setToast(null), 2800);
    }

    const totalChanges = changes.size + staffRemarksChanges.size + actorRemarksChanges.size;

    return e('div', { className: 'admin-content staff-content' },
        // 저장/엑셀 액션바
        e('div', { className: 'staff-action-bar' },
            totalChanges > 0 && e('span', { className: 'ss-changes-badge' }, `변경 ${totalChanges}건`),
            e('button', {
                className: 'btn-sm btn-edit',
                onClick: handleSave,
                disabled: saving || totalChanges === 0,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, saving ? '저장 중...' : '저장'),
            e('button', {
                className: 'btn-sm btn-pw',
                onClick: handleExport,
                disabled: saving,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, '엑셀 다운로드')
        ),

        // 테이블
        loading
            ? e('div', { className: 'loading-text' }, '데이터를 불러오는 중...')
            : e(SsTable, {
                year, month, staffUsers, actorUsers, gridData,
                staffRemarksData, actorRemarksData,
                staffRemarksChanges, actorRemarksChanges,
                changes,
                onCellChange: handleCellChange,
                onStaffRemarksChange: handleStaffRemarksChange,
                onActorRemarksChange: handleActorRemarksChange,
            }),

        // 토스트
        toast && e('div', { className: `ss-toast ${toast.type || ''}` }, toast.message)
    );
}

// ════════════════════════════════════════════════════════════════════
// WorkDiaryTab - 업무일지 탭 (work-diary 통합)
// ════════════════════════════════════════════════════════════════════

function WdRow({ diary, isEditing, isNew, onSave, onCancel, onEdit, onDelete }) {
    const [form, setForm] = React.useState({
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

function WorkDiaryTab({ year, month }) {
    const [diaries, setDiaries] = React.useState([]);
    const [editingId, setEditingId] = React.useState(null);
    const [addingNew, setAddingNew] = React.useState(false);
    const [loading, setLoading] = React.useState(false);

    const loadData = React.useCallback(async () => {
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

    React.useEffect(() => {
        loadData();
        setEditingId(null);
        setAddingNew(false);
    }, [loadData]);

    function handleAdd() {
        if (addingNew) { alert('이미 추가 중인 행이 있습니다.'); return; }
        if (editingId !== null) { alert('수정 중인 행이 있습니다. 먼저 저장하거나 취소해주세요.'); return; }
        setAddingNew(true);
    }

    async function handleSaveNew(form) {
        if (!form.date) { alert('날짜는 필수 항목입니다.'); return; }
        try {
            await apiFetch('/api/admin/work-diary', { method: 'POST', body: JSON.stringify(form) });
            setAddingNew(false);
            loadData();
        } catch (err) {
            alert('저장 실패: ' + err.message);
        }
    }

    function handleEdit(id) {
        if (addingNew) { alert('추가 중인 행이 있습니다. 먼저 저장하거나 취소해주세요.'); return; }
        setEditingId(id);
    }

    async function handleSaveEdit(id, form) {
        if (!form.date) { alert('날짜는 필수 항목입니다.'); return; }
        try {
            await apiFetch('/api/admin/work-diary/' + id, { method: 'PUT', body: JSON.stringify(form) });
            setEditingId(null);
            loadData();
        } catch (err) {
            alert('수정 실패: ' + err.message);
        }
    }

    async function handleDelete(id) {
        if (!confirm('정말로 이 업무일지를 삭제하시겠습니까?')) return;
        try {
            await apiFetch('/api/admin/work-diary/' + id, { method: 'DELETE' });
            loadData();
        } catch (err) {
            alert('삭제 실패: ' + err.message);
        }
    }

    function handleExport() {
        window.location.href = '/api/admin/work-diary/export?year=' + year + '&month=' + month;
    }

    return e('div', { className: 'admin-content wd-content' },
        // 액션바
        e('div', { className: 'staff-action-bar' },
            e('button', {
                className: 'btn-sm btn-edit',
                onClick: handleAdd,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, '+ 업무 추가'),
            e('button', {
                className: 'btn-sm btn-pw',
                onClick: handleExport,
                style: { padding: '8px 16px', fontSize: '0.85rem' },
            }, '엑셀 내보내기')
        ),

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
                    addingNew && e(WdRow, {
                        key: '__new__',
                        diary: null,
                        isNew: true,
                        isEditing: false,
                        onSave: handleSaveNew,
                        onCancel: () => setAddingNew(false),
                    }),
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

// ════════════════════════════════════════════════════════════════════
// GenericCrudPopup - 범용 추가/수정 팝업
// ════════════════════════════════════════════════════════════════════
function GenericCrudPopup({ title, fields, initialData, onClose, onSave }) {
    const [form, setForm] = React.useState(() => {
        const init = {};
        fields.forEach(f => {
            init[f.key] = (initialData && initialData[f.key] != null) ? String(initialData[f.key]) : (f.defaultValue || '');
        });
        return init;
    });
    const [error, setError] = React.useState('');
    const [saving, setSaving] = React.useState(false);

    function handleChange(key, value) {
        setForm(prev => ({ ...prev, [key]: value }));
    }

    async function handleSubmit() {
        // 필수 필드 검증
        for (const f of fields) {
            if (f.required && !form[f.key].trim()) {
                setError(`${f.label}을(를) 입력해주세요.`);
                return;
            }
        }
        setSaving(true);
        setError('');
        try {
            // 타입 변환
            const body = {};
            fields.forEach(f => {
                const val = form[f.key];
                if (f.type === 'number') body[f.key] = val ? Number(val) : null;
                else if (f.type === 'checkbox') body[f.key] = val === 'true' || val === true;
                else body[f.key] = val.trim() || null;
            });
            await onSave(body);
        } catch (err) {
            setError(err.message || '저장 중 오류가 발생했습니다.');
        } finally {
            setSaving(false);
        }
    }

    return e('div', { className: 'popup-overlay', onClick: onClose },
        e('div', { className: 'popup-box', onClick: ev => ev.stopPropagation() },
            e('div', { className: 'popup-title' }, title),
            error && e('div', { className: 'msg msg-error' }, error),
            e('div', { className: 'form-row' },
                ...fields.map(f => {
                    if (f.type === 'textarea') {
                        return e('textarea', {
                            key: f.key, className: 'form-input form-textarea',
                            placeholder: f.label, value: form[f.key],
                            onChange: ev => handleChange(f.key, ev.target.value),
                            rows: 3,
                        });
                    }
                    if (f.type === 'checkbox') {
                        return e('label', {
                            key: f.key,
                            style: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.88rem', color: '#4b5563' },
                        },
                            e('input', {
                                type: 'checkbox',
                                checked: form[f.key] === 'true' || form[f.key] === true,
                                onChange: ev => handleChange(f.key, ev.target.checked ? 'true' : 'false'),
                                style: { width: '16px', height: '16px', accentColor: '#6b46c1' },
                            }),
                            f.label
                        );
                    }
                    return e('input', {
                        key: f.key, className: 'form-input',
                        type: f.type || 'text',
                        placeholder: f.label,
                        value: form[f.key],
                        onChange: ev => handleChange(f.key, ev.target.value),
                    });
                })
            ),
            e('div', { className: 'popup-actions' },
                e('button', { className: 'btn-cancel', onClick: onClose }, '취소'),
                e('button', {
                    className: 'btn-save', onClick: handleSubmit, disabled: saving,
                }, saving ? '저장 중...' : '저장')
            )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// GenericCrudTab - 범용 CRUD 탭
// config: { title, apiPath, fields, cardFields, queryParams(items, year, month) }
// ════════════════════════════════════════════════════════════════════
function GenericCrudTab({ config }) {
    const now = new Date();
    const [localYear, setLocalYear] = React.useState(now.getFullYear());
    const [localMonth, setLocalMonth] = React.useState(now.getMonth() + 1);
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [showAddPopup, setShowAddPopup] = React.useState(false);
    const [editTarget, setEditTarget] = React.useState(null);

    function buildUrl() {
        let url = config.apiPath;
        if (config.queryParams) {
            url += '?' + config.queryParams(localYear, localMonth);
        }
        return url;
    }

    function loadData() {
        setLoading(true);
        apiFetch(buildUrl())
            .then(data => { if (data) setItems(data); })
            .catch(err => console.error(config.title + ' 로드 실패:', err))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => {
        loadData();
    }, [localYear, localMonth]);

    async function handleAdd(body) {
        await apiFetch(config.apiPath, { method: 'POST', body: JSON.stringify(body) });
        setShowAddPopup(false);
        loadData();
    }

    async function handleEdit(body) {
        const idField = config.idField || 'id';
        await apiFetch(`${config.apiPath}/${editTarget[idField]}`, { method: 'PUT', body: JSON.stringify(body) });
        setEditTarget(null);
        loadData();
    }

    function handleDelete(item) {
        const displayName = config.getDisplayName ? config.getDisplayName(item) : (item.name || item.id);
        if (!confirm(`'${displayName}'을(를) 삭제하시겠습니까?`)) return;
        const idField = config.idField || 'id';
        apiFetch(`${config.apiPath}/${item[idField]}`, { method: 'DELETE' })
            .then(() => loadData())
            .catch(err => alert(err.message || '삭제 실패'));
    }

    // 카드 렌더링
    function renderCard(item) {
        return e('div', { key: item[config.idField || 'id'], className: 'special-card' },
            e('div', { className: 'special-card-top' },
                ...(config.cardFields || []).map((cf, idx) => {
                    const val = item[cf.key];
                    if (!val && val !== 0) return null;
                    const display = cf.format === 'date' ? formatCardDate(val) : String(val);
                    const cls = idx === 0 ? 'special-date' : (cf.highlight ? 'special-name' : 'special-time');
                    return e('span', { key: cf.key, className: cls }, cf.suffix ? display + cf.suffix : display);
                })
            ),
            item.notes && e('div', { className: 'special-card-notes' }, item.notes),
            item.memo && e('div', { className: 'special-card-notes' }, item.memo),
            (!config.readOnly) && e('div', { className: 'special-card-actions' },
                e('button', { className: 'btn-sm btn-edit', onClick: () => setEditTarget(item) }, '수정'),
                e('button', { className: 'btn-sm btn-del', onClick: () => handleDelete(item) }, '삭제')
            )
        );
    }

    function formatCardDate(ds) {
        if (!ds) return '';
        const d = new Date(ds + 'T00:00:00');
        const days = ['일', '월', '화', '수', '목', '금', '토'];
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return mm + '/' + dd + '(' + days[d.getDay()] + ')';
    }

    const currentYear = now.getFullYear();
    const yearOptions = Array.from({ length: 5 }, (_, i) => currentYear - 2 + i);
    const monthOptions = Array.from({ length: 12 }, (_, i) => i + 1);

    return e('div', { className: 'admin-content' },
        // 월별 필터가 있는 경우 년/월 선택기 표시
        config.queryParams && e('div', {
            style: { display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' },
        },
            e('select', {
                className: 'role-filter-select',
                value: localYear,
                onChange: ev => setLocalYear(Number(ev.target.value)),
                style: { padding: '8px 10px' },
            },
                yearOptions.map(y => e('option', { key: y, value: y }, y + '년'))
            ),
            e('select', {
                className: 'role-filter-select',
                value: localMonth,
                onChange: ev => setLocalMonth(Number(ev.target.value)),
                style: { padding: '8px 10px' },
            },
                monthOptions.map(m => e('option', { key: m, value: m }, m + '월'))
            )
        ),

        // 추가 버튼
        (!config.readOnly) && e('button', {
            className: 'btn-add-special',
            onClick: () => setShowAddPopup(true),
        }, '+ ' + config.title + ' 추가'),

        // 목록 헤더
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, config.title + ' 목록')
        ),

        // 목록
        loading
            ? e('div', { className: 'loading-text' }, '불러오는 중...')
            : items.length === 0
                ? e('div', { className: 'loading-text' }, '등록된 데이터가 없습니다.')
                : e('div', { className: 'special-list' },
                    ...items.map(item => renderCard(item))
                ),

        // 추가 팝업
        showAddPopup && e(GenericCrudPopup, {
            title: config.title + ' 추가',
            fields: config.fields,
            initialData: null,
            onClose: () => setShowAddPopup(false),
            onSave: handleAdd,
        }),

        // 수정 팝업
        editTarget && e(GenericCrudPopup, {
            title: config.title + ' 수정',
            fields: config.fields,
            initialData: editTarget,
            onClose: () => setEditTarget(null),
            onSave: handleEdit,
        })
    );
}

// ════════════════════════════════════════════════════════════════════
// 신규 탭 설정 (CRUD)
// ════════════════════════════════════════════════════════════════════

const ANGEL_CANCEL_CONFIG = {
    title: '엔젤쇼취소',
    apiPath: '/api/admin/angel-cancel',
    queryParams: (y, m) => `year=${y}&month=${m}`,
    fields: [
        { key: 'cancelDate', label: '취소 날짜', type: 'date', required: true },
        { key: 'showTime', label: '공연 시간', type: 'time' },
        { key: 'actorName', label: '배우명', type: 'text' },
        { key: 'reason', label: '취소 사유', type: 'textarea' },
        { key: 'notes', label: '비고', type: 'textarea' },
    ],
    cardFields: [
        { key: 'cancelDate', format: 'date' },
        { key: 'showTime' },
        { key: 'actorName', highlight: true },
        { key: 'reason' },
    ],
    getDisplayName: item => item.actorName || item.cancelDate,
};

const PARTNER_CONFIG = {
    title: '협력업체',
    apiPath: '/api/admin/partners',
    fields: [
        { key: 'category', label: '업종 분류', type: 'text' },
        { key: 'name', label: '업체명', type: 'text', required: true },
        { key: 'contact', label: '연락처', type: 'text' },
        { key: 'manager', label: '담당자', type: 'text' },
        { key: 'notes', label: '비고', type: 'textarea' },
    ],
    cardFields: [
        { key: 'category' },
        { key: 'name', highlight: true },
        { key: 'contact' },
        { key: 'manager' },
    ],
    getDisplayName: item => item.name,
};

const MEMBERSHIP_CONFIG = {
    title: 'MEMBERSHIP',
    apiPath: '/api/admin/membership',
    fields: [
        { key: 'memberName', label: '회원명', type: 'text', required: true },
        { key: 'phone', label: '연락처', type: 'text' },
        { key: 'joinDate', label: '가입일', type: 'date' },
        { key: 'expireDate', label: '만료일', type: 'date' },
        { key: 'memo', label: '메모', type: 'textarea' },
    ],
    cardFields: [
        { key: 'memberName', highlight: true },
        { key: 'phone' },
        { key: 'joinDate', format: 'date' },
        { key: 'expireDate', format: 'date' },
    ],
    getDisplayName: item => item.memberName,
};

const BEER_SELECT_CONFIG = {
    title: '맥주셀렉',
    apiPath: '/api/admin/beer-select',
    fields: [
        { key: 'beerName', label: '맥주명', type: 'text', required: true },
        { key: 'brand', label: '브랜드', type: 'text' },
        { key: 'category', label: '분류 (국산/수입 등)', type: 'text' },
        { key: 'notes', label: '비고', type: 'textarea' },
        { key: 'active', label: '사용여부', type: 'checkbox', defaultValue: 'true' },
    ],
    cardFields: [
        { key: 'category' },
        { key: 'beerName', highlight: true },
        { key: 'brand' },
    ],
    getDisplayName: item => item.beerName,
};

// ════════════════════════════════════════════════════════════════════
// XmasSeatsTab - 크리스마스 지정석 (날짜별 조회 + CRUD)
// ════════════════════════════════════════════════════════════════════
function XmasSeatsTab() {
    const [date, setDate] = React.useState(() => {
        const y = new Date().getFullYear();
        return `${y}-12-24`;
    });
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [showAddPopup, setShowAddPopup] = React.useState(false);
    const [editTarget, setEditTarget] = React.useState(null);

    const xmasFields = [
        { key: 'eventDate', label: '이벤트 날짜', type: 'date', required: true },
        { key: 'seatLabel', label: '좌석 번호/명칭', type: 'text' },
        { key: 'customerName', label: '예약자명', type: 'text' },
        { key: 'phone', label: '연락처', type: 'text' },
        { key: 'peopleCount', label: '인원', type: 'number' },
        { key: 'notes', label: '비고', type: 'textarea' },
    ];

    function loadData(d) {
        setLoading(true);
        apiFetch(`/api/admin/xmas-seats?date=${d}`)
            .then(data => { if (data) setItems(data); else setItems([]); })
            .catch(() => setItems([]))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => { loadData(date); }, [date]);

    async function handleAdd(body) {
        if (!body.eventDate) body.eventDate = date;
        await apiFetch('/api/admin/xmas-seats', { method: 'POST', body: JSON.stringify(body) });
        setShowAddPopup(false);
        loadData(date);
    }

    async function handleEdit(body) {
        await apiFetch(`/api/admin/xmas-seats/${editTarget.id}`, { method: 'PUT', body: JSON.stringify(body) });
        setEditTarget(null);
        loadData(date);
    }

    function handleDelete(item) {
        if (!confirm(`'${item.customerName || item.seatLabel}' 지정석을 삭제하시겠습니까?`)) return;
        apiFetch(`/api/admin/xmas-seats/${item.id}`, { method: 'DELETE' })
            .then(() => loadData(date))
            .catch(err => alert(err.message || '삭제 실패'));
    }

    return e('div', { className: 'admin-content' },
        // 날짜 선택
        e('div', { style: { display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' } },
            e('input', {
                className: 'form-input', type: 'date', value: date,
                onChange: ev => setDate(ev.target.value),
                style: { maxWidth: '200px' },
            })
        ),
        e('button', {
            className: 'btn-add-special',
            onClick: () => setShowAddPopup(true),
        }, '+ 크리스마스 지정석 추가'),
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '크리스마스 지정석 목록')
        ),
        loading
            ? e('div', { className: 'loading-text' }, '불러오는 중...')
            : items.length === 0
                ? e('div', { className: 'loading-text' }, '해당 날짜에 등록된 지정석이 없습니다.')
                : e('div', { className: 'special-list' },
                    ...items.map(item =>
                        e('div', { key: item.id, className: 'special-card' },
                            e('div', { className: 'special-card-top' },
                                e('span', { className: 'special-date' }, item.seatLabel || ''),
                                e('span', { className: 'special-name' }, item.customerName || ''),
                                item.phone && e('span', { className: 'special-time' }, item.phone),
                                item.peopleCount && e('span', { className: 'special-count' }, item.peopleCount + '명')
                            ),
                            item.notes && e('div', { className: 'special-card-notes' }, item.notes),
                            e('div', { className: 'special-card-actions' },
                                e('button', { className: 'btn-sm btn-edit', onClick: () => setEditTarget(item) }, '수정'),
                                e('button', { className: 'btn-sm btn-del', onClick: () => handleDelete(item) }, '삭제')
                            )
                        )
                    )
                ),
        showAddPopup && e(GenericCrudPopup, {
            title: '크리스마스 지정석 추가',
            fields: xmasFields,
            initialData: { eventDate: date },
            onClose: () => setShowAddPopup(false),
            onSave: handleAdd,
        }),
        editTarget && e(GenericCrudPopup, {
            title: '크리스마스 지정석 수정',
            fields: xmasFields,
            initialData: editTarget,
            onClose: () => setEditTarget(null),
            onSave: handleEdit,
        })
    );
}

// ════════════════════════════════════════════════════════════════════
// HealthCertTab - 보건증확인 (특수: UPSERT 방식)
// ════════════════════════════════════════════════════════════════════
function HealthCertTab() {
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);
    const [editTarget, setEditTarget] = React.useState(null);

    function loadData() {
        setLoading(true);
        apiFetch('/api/admin/health-cert')
            .then(data => { if (data) setItems(data); })
            .catch(err => console.error('보건증 로드 실패:', err))
            .finally(() => setLoading(false));
    }

    React.useEffect(() => { loadData(); }, []);

    async function handleSave(body) {
        await apiFetch('/api/admin/health-cert', {
            method: 'PUT',
            body: JSON.stringify({ userId: editTarget.userId, ...body }),
        });
        setEditTarget(null);
        loadData();
    }

    // 만료일 상태 계산
    function getExpireStatus(expireDate) {
        if (!expireDate) return { label: '미등록', cls: 'badge-user' };
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const expire = new Date(expireDate + 'T00:00:00');
        const diffDays = Math.ceil((expire - today) / (1000 * 60 * 60 * 24));
        if (diffDays < 0) return { label: '만료', cls: 'badge-expired' };
        if (diffDays <= 30) return { label: `D-${diffDays}`, cls: 'badge-warning' };
        return { label: '유효', cls: 'badge-valid' };
    }

    return e('div', { className: 'admin-content' },
        e('div', { className: 'list-header' },
            e('div', { className: 'section-title' }, '보건증확인 목록')
        ),
        loading
            ? e('div', { className: 'loading-text' }, '불러오는 중...')
            : items.length === 0
                ? e('div', { className: 'loading-text' }, '등록된 데이터가 없습니다.')
                : e('div', { className: 'special-list' },
                    ...items.map(item => {
                        const status = getExpireStatus(item.expireDate);
                        return e('div', { key: item.userId, className: 'special-card' },
                            e('div', { className: 'special-card-top' },
                                e('span', { className: 'special-name' }, item.userName),
                                e('span', { className: 'special-time' }, item.userId),
                                item.expireDate && e('span', { className: 'special-date' }, item.expireDate),
                                e('span', { className: 'badge ' + status.cls }, status.label)
                            ),
                            item.notes && e('div', { className: 'special-card-notes' }, item.notes),
                            e('div', { className: 'special-card-actions' },
                                e('button', {
                                    className: 'btn-sm btn-edit',
                                    onClick: () => setEditTarget(item),
                                }, '수정')
                            )
                        );
                    })
                ),
        editTarget && e(GenericCrudPopup, {
            title: editTarget.userName + ' 보건증 수정',
            fields: [
                { key: 'expireDate', label: '만료일', type: 'date' },
                { key: 'notes', label: '비고', type: 'textarea' },
            ],
            initialData: editTarget,
            onClose: () => setEditTarget(null),
            onSave: handleSave,
        })
    );
}

// ════════════════════════════════════════════════════════════════════
// DayTextCell - 날짜 셀 텍스트 편집 (contentEditable, 훅 사용을 위해 독립 컴포넌트)
// ════════════════════════════════════════════════════════════════════
function DayTextCell({ dateKey, dailyNoteMap }) {
    const cellRef = React.useRef(null);

    React.useEffect(() => {
        if (cellRef.current && document.activeElement !== cellRef.current) {
            cellRef.current.innerText = dailyNoteMap[dateKey] || '';
        }
    }, [dailyNoteMap, dateKey]);

    function saveDailyNote(noteDate, content) {
        if (content === '' && !dailyNoteMap[noteDate]) return;
        apiFetch('/api/admin/daily-note', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ noteDate, content })
        }).catch(() => {
            const el = document.querySelector(`[data-date="${noteDate}"]`);
            if (el) {
                el.classList.add('ac-day-text--error');
                setTimeout(() => el.classList.remove('ac-day-text--error'), 2000);
            }
        });
    }

    return e('div', {
        ref: cellRef,
        className: 'ac-day-text',
        contentEditable: true,
        suppressContentEditableWarning: true,
        'data-date': dateKey,
        onBlur: ev => saveDailyNote(dateKey, ev.target.innerText.trim()),
        onKeyDown: ev => {
            if (ev.key === 'Escape') {
                ev.target.innerText = dailyNoteMap[dateKey] || '';
                ev.target.blur();
            }
        }
    });
}

// ════════════════════════════════════════════════════════════════════
// MonthBlock - 월별 달력 블록 (훅 규칙 준수를 위해 독립 컴포넌트)
// ════════════════════════════════════════════════════════════════════
function MonthBlock({ year, monthIndex, dailyNoteMap, isCurrentMonth }) {
    const monthNum = monthIndex + 1;
    const WEEKDAYS = ['월', '화', '수', '목', '금', '토', '일'];

    // ③ 현재 월 자동 스크롤용 ref
    const blockRef = React.useRef(null);
    React.useEffect(() => {
        if (isCurrentMonth && blockRef.current) {
            blockRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }, [isCurrentMonth]);

    // 월별 주요사항 state
    const [noteContent, setNoteContent] = React.useState('');
    const [savedMsg, setSavedMsg] = React.useState('');

    // 월 주요사항 로드
    React.useEffect(() => {
        apiFetch(`/api/admin/monthly-note?year=${year}&month=${monthNum}`)
            .then(data => { if (data) setNoteContent(data.content || ''); })
            .catch(() => {});
    }, [year, monthNum]);

    function saveNote() {
        apiFetch('/api/admin/monthly-note', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ year, month: monthNum, content: noteContent })
        }).then(() => {
            setSavedMsg('저장됨');
            setTimeout(() => setSavedMsg(''), 2000);
        }).catch(err => console.error('주요사항 저장 실패:', err));
    }

    // 월요일 시작 달력 셀 계산 (getDay()+6)%7 → 0=월, 6=일
    const firstDow = (new Date(year, monthIndex, 1).getDay() + 6) % 7;
    const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();
    const cells = [];
    for (let i = 0; i < firstDow; i++) cells.push(null);
    for (let d = 1; d <= daysInMonth; d++) cells.push(d);

    return e('div', { ref: blockRef, className: 'ac-month-block' },
        e('div', { className: 'ac-month-title' }, `${year}년 ${monthNum}월`),
        e('div', { className: 'ac-month-body' },
            // 좌측: 달력 그리드
            e('div', { className: 'ac-calendar-area' },
                e('div', { className: 'ac-weekday-row' },
                    WEEKDAYS.map((wd, i) =>
                        e('div', {
                            key: wd,
                            className: `ac-weekday-cell${i === 5 ? ' saturday' : i === 6 ? ' sunday' : ''}`
                        }, wd)
                    )
                ),
                e('div', { className: 'ac-day-grid' },
                    cells.map((d, idx) => {
                        if (d === null) {
                            return e('div', { key: `empty-${idx}`, className: 'ac-day-cell empty' });
                        }
                        const dateKey = `${year}-${String(monthNum).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
                        const dow = (firstDow + d - 1) % 7;  // 0=월,...,5=토,6=일
                        const dayClass = `ac-day-cell${dow === 5 ? ' saturday' : dow === 6 ? ' sunday' : ''}`;

                        return e('div', { key: dateKey, className: dayClass },
                            e('div', { className: 'ac-day-num' }, d),
                            e(DayTextCell, { dateKey, dailyNoteMap })
                        );
                    })
                )
            ),
            // 우측: 이 달의 주요사항 (직접 입력)
            e('div', { className: 'ac-note-area' },
                e('div', { className: 'ac-note-title' }, '이 달의 주요사항'),
                e('textarea', {
                    className: 'ac-note-textarea',
                    rows: 7,
                    value: noteContent,
                    onChange: ev => setNoteContent(ev.target.value),
                    placeholder: '이 달의 주요사항을 입력하세요...'
                }),
                e('button', { className: 'ac-note-save-btn', onClick: saveNote }, '저장'),
                savedMsg ? e('div', { className: 'ac-note-saved-msg' }, savedMsg) : null
            )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// AnnualCalendarTab - 연간일정캘린더 (구글시트 스타일 달력 그리드)
// ════════════════════════════════════════════════════════════════════
function AnnualCalendarTab() {
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth();  // 0-based
    const [year, setYear] = React.useState(currentYear);
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);

    function loadData(y) {
        setLoading(true);
        apiFetch(`/api/admin/daily-note?year=${y}`)
            .then(data => { setItems(data || []); })
            .catch(err => { console.error('연간일정 로드 실패:', err); setItems([]); })
            .finally(() => setLoading(false));
    }

    React.useEffect(() => { loadData(year); }, [year]);

    const years = Array.from({ length: 5 }, (_, i) => currentYear - 2 + i);

    // 날짜 → content 단순 매핑
    function buildDailyNoteMap(items) {
        const result = {};
        items.forEach(item => {
            if (!item.noteDate) return;
            result[item.noteDate] = item.content || '';
        });
        return result;
    }

    const dailyNoteMap = buildDailyNoteMap(items);

    return e('div', { className: 'admin-content staff-content' },
        // 연도 선택 바
        e('div', { className: 'staff-action-bar' },
            e('select', {
                className: 'role-filter-select',
                value: year,
                onChange: ev => setYear(Number(ev.target.value)),
                style: { fontSize: '0.9rem', padding: '8px 12px' },
            },
                years.map(y => e('option', { key: y, value: y }, y + '년'))
            )
        ),
        // 달력 본문 (1~12월)
        e('div', { style: { padding: '16px' } },
            loading
                ? e('div', { className: 'loading-text' }, '불러오는 중...')
                : Array.from({ length: 12 }, (_, i) =>
                    e(MonthBlock, {
                        key: i,
                        year,
                        monthIndex: i,
                        dailyNoteMap,
                        // ③ 현재 연도 선택 시에만 현재 월로 자동 스크롤
                        isCurrentMonth: year === currentYear && i === currentMonth
                    })
                  )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// ActorTab - 배우 스케줄 (월별, 기존 캘린더 API 활용)
// ════════════════════════════════════════════════════════════════════
function ActorTab({ year, month }) {
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);

    React.useEffect(() => {
        setLoading(true);
        const mm = String(month).padStart(2, '0');
        apiFetch(`/api/schedule/calendar?year=${year}&month=${mm}`)
            .then(data => { if (data) setItems(Array.isArray(data) ? data : (data.data || [])); else setItems([]); })
            .catch(() => setItems([]))
            .finally(() => setLoading(false));
    }, [year, month]);

    return e('div', { className: 'admin-content staff-content' },
        e('div', { className: 'list-header', style: { margin: '0 0 12px' } },
            e('div', { className: 'section-title' }, `${year}년 ${month}월 배우 스케줄`)
        ),
        e('div', { className: 'wd-body' },
            loading
                ? e('div', { className: 'loading-text' }, '불러오는 중...')
                : items.length === 0
                    ? e('div', { className: 'loading-text' }, '등록된 스케줄이 없습니다.')
                    : e('table', { className: 'wd-table' },
                        e('thead', null,
                            e('tr', null,
                                e('th', null, '날짜'),
                                e('th', null, '배우'),
                                e('th', null, '역할'),
                                e('th', null, '시간'),
                                e('th', null, '메모')
                            )
                        ),
                        e('tbody', null,
                            ...items.map((item, idx) =>
                                e('tr', { key: idx },
                                    e('td', { className: 'wd-col-date' }, item.scheduleDate || item.date || ''),
                                    e('td', null, item.userName || item.actorName || ''),
                                    e('td', null, getRoleDisplay(item.role) || item.role || ''),
                                    e('td', null, item.timeSlot || item.time || ''),
                                    e('td', null, item.memo || item.notes || '')
                                )
                            )
                        )
                    )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// DailyTab - 일일 현황 (날짜별 읽기 전용)
// ════════════════════════════════════════════════════════════════════
function DailyTab({ year, month }) {
    const defaultDate = `${year}-${String(month).padStart(2, '0')}-01`;
    const [date, setDate] = React.useState(defaultDate);
    const [items, setItems] = React.useState([]);
    const [loading, setLoading] = React.useState(true);

    React.useEffect(() => {
        setDate(`${year}-${String(month).padStart(2, '0')}-01`);
    }, [year, month]);

    React.useEffect(() => {
        setLoading(true);
        apiFetch(`/api/admin/daily-status?date=${date}`)
            .then(data => { if (data) setItems(data); else setItems([]); })
            .catch(() => setItems([]))
            .finally(() => setLoading(false));
    }, [date]);

    return e('div', { className: 'admin-content staff-content' },
        // 날짜 선택
        e('div', { className: 'staff-action-bar' },
            e('input', {
                className: 'form-input',
                type: 'date',
                value: date,
                onChange: ev => setDate(ev.target.value),
                style: { maxWidth: '200px' },
            })
        ),
        e('div', { className: 'wd-body' },
            loading
                ? e('div', { className: 'loading-text' }, '불러오는 중...')
                : items.length === 0
                    ? e('div', { className: 'loading-text' }, '해당 날짜에 데이터가 없습니다.')
                    : e('table', { className: 'wd-table' },
                        e('thead', null,
                            e('tr', null,
                                e('th', null, '이름'),
                                e('th', null, '유형'),
                                e('th', null, '역할'),
                                e('th', null, '확정'),
                                e('th', null, '근무시간'),
                                e('th', null, '메모')
                            )
                        ),
                        e('tbody', null,
                            ...items.map((item, idx) => {
                                const acType = item.accountType ? (ACCOUNT_TYPE_DISPLAY[item.accountType] || item.accountType) : '';
                                return e('tr', { key: idx },
                                    e('td', null, item.userName || ''),
                                    e('td', null, acType),
                                    e('td', null, getRoleDisplay(item.role) || item.role || ''),
                                    e('td', null, item.confirmed ? 'O' : '-'),
                                    e('td', null, item.hours || ''),
                                    e('td', null, item.scheduleMemo || item.remarks || '')
                                );
                            })
                        )
                    )
        )
    );
}

// ════════════════════════════════════════════════════════════════════
// AdminPage - 최상위 컴포넌트
// ════════════════════════════════════════════════════════════════════
function AdminPage() {
    // URL 해시에서 초기 탭 복원
    const initialTab = (() => {
        const hash = window.location.hash.replace('#', '');
        return VISIBLE_TAB_KEYS.has(hash) ? hash : 'annual-calendar';
    })();

    const [activeTab, setActiveTab] = React.useState(initialTab);
    const [userInfo, setUserInfo] = React.useState(null);

    // 월별 탭용 연/월 상태 (sessionStorage에서 복원)
    const now = new Date();
    const [selectedYear, setSelectedYear] = React.useState(
        Number(sessionStorage.getItem('admin_selected_year')) || now.getFullYear()
    );
    const [selectedMonth, setSelectedMonth] = React.useState(
        Number(sessionStorage.getItem('admin_selected_month')) || now.getMonth() + 1
    );

    // 사용자 정보 로드
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

    // URL 해시 변경 대응
    React.useEffect(() => {
        const onHashChange = () => {
            const tab = window.location.hash.replace('#', '');
            if (VISIBLE_TAB_KEYS.has(tab)) setActiveTab(tab);
        };
        window.addEventListener('hashchange', onHashChange);
        return () => window.removeEventListener('hashchange', onHashChange);
    }, []);

    // 탭 변경 핸들러
    function handleTabChange(tabKey) {
        window.location.hash = tabKey;
        setActiveTab(tabKey);
    }

    // 월 변경 핸들러
    function handleMonthChange(year, month) {
        sessionStorage.setItem('admin_selected_year', year);
        sessionStorage.setItem('admin_selected_month', month);
        setSelectedYear(year);
        setSelectedMonth(month);
    }

    const isMonthlyTab = MONTHLY_TAB_KEYS.has(activeTab);

    function renderTabContent() {
        switch (activeTab) {
            // ── 고정 탭 ──
            case 'annual-calendar':
                return e(AnnualCalendarTab);
            case 'accounts':
                return e(UserManagementTab, {
                    currentUserid: userInfo ? userInfo.userid : '',
                });
            case 'special':
                return e(SpecialReservationTab);
            case 'angel-cancel':
                return e(GenericCrudTab, { config: ANGEL_CANCEL_CONFIG });
            case 'partners':
                return e(GenericCrudTab, { config: PARTNER_CONFIG });
            case 'health-cert':
                return e(HealthCertTab);
            case 'membership':
                return e(GenericCrudTab, { config: MEMBERSHIP_CONFIG });
            case 'beer-select':
                return e(GenericCrudTab, { config: BEER_SELECT_CONFIG });
            case 'xmas-seats':
                return e(XmasSeatsTab);

            // ── 월별 탭 ──
            case 'work-diary':
                return e(WorkDiaryTab, { year: selectedYear, month: selectedMonth });
            case 'actor':
                return e(ActorTab, { year: selectedYear, month: selectedMonth });
            case 'voucher':
                return e(VoucherTipTabInner, { mode: 'voucher', year: selectedYear, month: selectedMonth });
            case 'tip':
                return e(VoucherTipTabInner, { mode: 'tip', year: selectedYear, month: selectedMonth });
            case 'staff':
                return e(StaffTab, { year: selectedYear, month: selectedMonth });
            case 'daily':
                return e(DailyTab, { year: selectedYear, month: selectedMonth });
            default:
                return null;
        }
    }

    return e('div', { className: 'admin-wrap' },
        e(AdminHeader, { userName: userInfo ? userInfo.userName : '' }),
        e(TabBar, { activeTab, onTabChange: handleTabChange }),
        isMonthlyTab && e(MonthNavigator, {
            year: selectedYear,
            month: selectedMonth,
            onMonthChange: handleMonthChange,
        }),
        renderTabContent()
    );
}

// ════════════════════════════════════════════════════════════════════
// 렌더링
// ════════════════════════════════════════════════════════════════════
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(e(AdminPage, null));
