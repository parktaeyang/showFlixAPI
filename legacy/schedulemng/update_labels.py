import os
import sys
import requests
from datetime import datetime, timedelta, timezone
from urllib.parse import quote_plus

LABEL_DONE = "배포완료"
DEFAULT_TARGET_BRANCH = "develop"

def utcnow():
    return datetime.now(timezone.utc)

def parse_gitlab_time(ts: str) -> datetime:
    # GitLab: "2026-02-05T12:34:56.789Z"
    return datetime.fromisoformat(ts.replace("Z", "+00:00"))

def get_env(name: str, default=None):
    v = os.getenv(name)
    return v if v is not None and v != "" else default

def require_env(name: str) -> str:
    v = os.getenv(name)
    if not v:
        print(f"Error: 환경변수 {name} 가(이) 설정되지 않았습니다.", file=sys.stderr)
        sys.exit(1)
    return v

def fetch_all_merge_requests(api_base: str, project_id: str, headers: dict, params: dict):
    page = 1
    all_items = []
    while True:
        p = dict(params)
        p["page"] = page
        resp = requests.get(
            f"{api_base}/projects/{project_id}/merge_requests",
            headers=headers,
            params=p,
            timeout=30,
        )
        resp.raise_for_status()
        items = resp.json()
        all_items.extend(items)

        next_page = resp.headers.get("X-Next-Page")
        if not next_page:
            break
        page = int(next_page)
    return all_items

def main():
    # CI 환경 우선
    api_base = get_env("CI_API_V4_URL", "https://gitlab.com/api/v4")
    project_id = get_env("CI_PROJECT_ID")
    project_path = get_env("CI_PROJECT_PATH")  # fallback용
    token = require_env("GITLAB_API_TOKEN")

    target_branch = get_env("TARGET_BRANCH", DEFAULT_TARGET_BRANCH)

    headers = {"PRIVATE-TOKEN": token}

    # CI_PROJECT_ID가 없으면 project_path로 조회(로컬 실행 대비)
    if not project_id:
        if not project_path:
            # 사용자 로컬 실행 대비(원하면 여기서 직접 프로젝트 경로를 넣어도 됨)
            project_path = "worktiger/schedulemng"
        encoded = quote_plus(project_path)
        pr = requests.get(f"{api_base}/projects/{encoded}", headers=headers, timeout=30)
        pr.raise_for_status()
        project_id = str(pr.json()["id"])

    since = (utcnow() - timedelta(days=365)).isoformat()

    params = {
        "state": "merged",
        "target_branch": target_branch,
        "updated_after": since,
        "per_page": 100,
        "order_by": "updated_at",
        "sort": "desc",
    }

    print(f"[INFO] project_id={project_id}, target_branch={target_branch}")
    print(f"[INFO] Searching merged MRs updated_after={since}")

    mrs = fetch_all_merge_requests(api_base, project_id, headers, params)
    print(f"[INFO] Found {len(mrs)} merged MRs candidates")

    updated = 0
    skipped = 0

    for mr in mrs:
        iid = mr["iid"]
        title = mr.get("title", "")
        merged_at_str = mr.get("merged_at")
        if not merged_at_str:
            continue

        merged_at = parse_gitlab_time(merged_at_str)
        hours_since = (utcnow() - merged_at).total_seconds() / 3600

        labels = mr.get("labels", []) or []

        print(f"\nMR !{iid}: {title}")
        print(f"  merged_at={merged_at_str} ({hours_since:.1f}h ago)")
        print(f"  labels={labels}")

        if hours_since < 24:
            print("  -> skip (less than 24h)")
            skipped += 1
            continue

        if LABEL_DONE in labels:
            print("  -> skip (already has 배포완료)")
            skipped += 1
            continue

        # ✅ “추가만” 정책: add_labels 사용 (기존 라벨 유지, 경쟁 조건에도 안전)
        r = requests.put(
            f"{api_base}/projects/{project_id}/merge_requests/{iid}",
            headers=headers,
            data={"add_labels": LABEL_DONE},
            timeout=30,
        )

        if r.status_code == 200:
            print("  -> updated (add 배포완료)")
            updated += 1
        else:
            print(f"  -> FAILED status={r.status_code} body={r.text}")

    print("\n" + "=" * 60)
    print("[DONE]")
    print(f"  updated={updated}")
    print(f"  skipped={skipped}")
    print("=" * 60)

if __name__ == "__main__":
    main()