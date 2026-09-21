#!/usr/bin/env python3
"""
Updates the Contributors showcase in README.md by querying merged pull requests
and ranking contributors by the number of merged PRs.
"""

from __future__ import annotations

import html
import json
import os
import re
import shutil
import subprocess  # nosec B404
import sys
import urllib.parse
from pathlib import Path

MAX_CONTRIBUTORS: int = int(os.environ.get("MAX_CONTRIBUTORS", "12"))
REPO_PATTERN = re.compile(r"^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$")
USERNAME_PATTERN = re.compile(r"^[a-zA-Z0-9_.-]+$")


def fetch_merged_prs(repo: str = "") -> list[dict]:
    gh_bin = shutil.which("gh")
    if not gh_bin:
        raise RuntimeError("GitHub CLI 'gh' is required but not found in PATH.")

    cmd = [gh_bin, "pr", "list", "--state", "merged", "--limit", "1000", "--json", "number,author"]
    if repo:
        if not REPO_PATTERN.match(repo):
            raise ValueError(f"Invalid repository format: {repo}")
        cmd.extend(["--repo", repo])

    # Command is fixed and arguments are strictly validated (no shell interpretation)
    result = subprocess.run(  # nosec B603
        cmd,
        capture_output=True,
        text=True,
        check=True,
        timeout=60,
    )
    return json.loads(result.stdout)


def aggregate_contributors(prs: list[dict]) -> list[tuple[str, int]]:
    counts: dict[str, int] = {}

    for pr in prs:
        author = pr.get("author") or {}
        login = author.get("login")
        is_bot = author.get("is_bot", False)

        if not login or not USERNAME_PATTERN.match(login):
            continue
        if is_bot or login.startswith("app/") or login.endswith("[bot]") or login in ("dependabot", "ghost"):
            continue

        counts[login] = counts.get(login, 0) + 1

    return sorted(counts.items(), key=lambda item: (-item[1], item[0].lower()))


def generate_html_grid(contributors: list[tuple[str, int]], limit: int = MAX_CONTRIBUTORS) -> str:
    elements = []
    top_contributors = contributors[:limit] if limit > 0 else contributors

    for user, count in top_contributors:
        plural = "s" if count > 1 else ""
        escaped_user = html.escape(user, quote=True)
        url_user = urllib.parse.quote(user, safe="")
        title = f"{escaped_user} ({count} merged PR{plural})"
        elements.append(
            f'  <a href="https://github.com/{url_user}">'
            f'<img src="https://github.com/{url_user}.png?size=64" width="64" height="64" alt="{escaped_user}" title="{title}" style="border-radius: 50%; margin: 2px;" />'
            f'</a>'
        )

    grid_content = "\n".join(elements)
    return f'<p align="left">\n{grid_content}\n</p>'


def update_readme(readme_path: Path, new_content: str) -> bool:
    content = readme_path.read_text(encoding="utf-8")

    pattern = re.compile(
        r"(<!-- CONTRIBUTORS-START -->)(.*?)(<!-- CONTRIBUTORS-END -->)",
        re.DOTALL,
    )

    if not pattern.search(content):
        print("Error: Could not find <!-- CONTRIBUTORS-START --> and <!-- CONTRIBUTORS-END --> markers in README.md", file=sys.stderr)
        return False

    updated_content = pattern.sub(f"\\1\n{new_content}\n\\3", content)

    if updated_content == content:
        print("README.md contributors section is already up to date.")
        return False

    readme_path.write_text(updated_content, encoding="utf-8")
    print("README.md contributors section successfully updated.")
    return True


def main() -> None:
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    readme_path = Path(__file__).resolve().parent.parent.parent / "README.md"

    if not readme_path.exists():
        print(f"Error: README file not found at {readme_path}", file=sys.stderr)
        sys.exit(1)

    print("Fetching merged pull requests from GitHub...")
    prs = fetch_merged_prs(repo)
    print(f"Retrieved {len(prs)} merged pull requests.")

    contributors = aggregate_contributors(prs)
    print(f"Identified {len(contributors)} unique contributors.")

    for rank, (user, count) in enumerate(contributors, start=1):
        print(f"  {rank:2d}. {user}: {count} merged PR(s)")

    print(f"Generating showcase for top {min(len(contributors), MAX_CONTRIBUTORS)} contributors (limit: {MAX_CONTRIBUTORS})...")
    grid_html = generate_html_grid(contributors, limit=MAX_CONTRIBUTORS)
    update_readme(readme_path, grid_html)


if __name__ == "__main__":
    main()
