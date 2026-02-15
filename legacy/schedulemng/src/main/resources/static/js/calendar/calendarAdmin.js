function drawAdminMemo(isAdmin) {
    const divAdminMemoArea = document.getElementById('divAdminMemoArea');
    if (!divAdminMemoArea) {
        return;
    }
    divAdminMemoArea.innerHTML = `
        <div style="margin-top: 30px; width: 80%">
            <label for="adminNote"><strong>관리자 메모:</strong></label><br>
            <textarea style="height: 550px;" id="adminNote" cols="50" placeholder="메모를 입력하세요."></textarea>
            ${isAdmin ? `<button id="btnAdminNoteSave" style="margin-top: 20px;">🛠️관리자 메모 저장</button>` : ``}
        </div>
    `;

    const textarea = divAdminMemoArea.querySelector('#adminNote');
    textarea.readOnly = !isAdmin;

    if (isAdmin) {
        const btn = divAdminMemoArea.querySelector('#btnAdminNoteSave');
        if (btn) {
            btn.addEventListener('click', saveAdminNote);
        }
    }

    // 내용 조회 후 채우기
    getAdminNote();
}

function saveAdminNote() {
    const content = document.getElementById("adminNote").value;

    axios.post("/api/dates/saveAdminNote", { content })
        .then(() => {
            alert("관리자 메모 저장");
            fetchAndRenderDatesByMonth(currentYear, currentMonth + 1);
        })
        .catch(error => {
            console.error("관리자 메모 저장 실패:", error);
            alert("관리자 메모 저장 실패!");
        });
}

function getAdminNote() {
    const content = document.getElementById("adminNote").value;

    axios.get("/api/dates/getAdminNote", {})
        .then(res => {
            document.getElementById("adminNote").value = res.data?.content ?? "";
        })
        .catch(error => {
            console.error("관리자 메모 조회 실패:", error);
        });
}