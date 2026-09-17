import os
import random
import subprocess
from datetime import datetime, timedelta

def get_positive_int(prompt, default=20):
    while True:
        try:
            user_input = input(f"{prompt} (default {default}): ")
            if not user_input.strip():
                return default
            value = int(user_input)
            if value > 0:
                return value
            else:
                print("Please enter a positive integer.")
        except ValueError:
            print("Invalid input. Please enter a valid integer.")

def get_repo_path(prompt, default="."):
    while True:
        user_input = input(f"{prompt} (default current directory): ")
        if not user_input.strip():
            return default
        if os.path.isdir(user_input):
            return user_input
        else:
            print("Directory does not exist. Please enter a valid path.")

def get_filename(prompt, default="data.txt"):
    user_input = input(f"{prompt} (default {default}): ")
    if not user_input.strip():
        return default
    return user_input

def get_branch_name(prompt, default=None):
    if default:
        user_input = input(f"{prompt} (default {default}): ")
        if not user_input.strip():
            return default
        return user_input.strip()
    return input(f"{prompt}: ").strip() or "main"

def get_current_branch(repo_path):
    result = subprocess.run(
        ["git", "rev-parse", "--abbrev-ref", "HEAD"],
        cwd=repo_path,
        capture_output=True,
        text=True,
    )
    if result.returncode == 0:
        return result.stdout.strip()
    return "main"

def checkout_or_create_branch(repo_path, branch):
    current_branch = get_current_branch(repo_path)
    if current_branch == branch:
        return branch

    result = subprocess.run(
        ["git", "rev-parse", "--verify", branch],
        cwd=repo_path,
        capture_output=True,
        text=True,
    )
    if result.returncode == 0:
        subprocess.run(["git", "checkout", branch], cwd=repo_path)
        return branch

    result = subprocess.run(
        ["git", "ls-remote", "--heads", "origin", branch],
        cwd=repo_path,
        capture_output=True,
        text=True,
    )
    if result.returncode == 0 and result.stdout.strip():
        subprocess.run(["git", "checkout", "-b", branch, f"origin/{branch}"], cwd=repo_path)
        return branch

    subprocess.run(["git", "checkout", "-b", branch], cwd=repo_path)
    return branch

def random_date_in_last_year():
    today = datetime.now()
    start_date = today - timedelta(days=365)
    random_days = random.randint(0, 364)
    random_seconds = random.randint(0, 23*3600 + 3599)
    commit_date = start_date + timedelta(days=random_days, seconds=random_seconds)
    return commit_date


def random_date_in_range(start_dt, end_dt):
    if start_dt > end_dt:
        start_dt, end_dt = end_dt, start_dt
    total_seconds = int((end_dt - start_dt).total_seconds())
    if total_seconds <= 0:
        return start_dt
    offset = random.randint(0, total_seconds)
    return start_dt + timedelta(seconds=offset)


def parse_date_input(date_str):
    """Parse a date in YYYY-MM-DD format, fallback to returning None on empty input."""
    if not date_str or not date_str.strip():
        return None
    for fmt in ("%Y-%m-%d", "%Y/%m/%d", "%d-%m-%Y"):
        try:
            return datetime.strptime(date_str.strip(), fmt)
        except Exception:
            continue
    raise ValueError("Date must be in YYYY-MM-DD (or YYYY/MM/DD or DD-MM-YYYY) format")


def get_date_range(prompt_start="Start date (YYYY-MM-DD)", prompt_end="End date (YYYY-MM-DD)"):
    print("Enter a date range for commits. Leave blank to use the last 365 days.")
    while True:
        start_input = input(f"{prompt_start}: ")
        end_input = input(f"{prompt_end}: ")
        try:
            start_dt = parse_date_input(start_input) if start_input.strip() else None
            end_dt = parse_date_input(end_input) if end_input.strip() else None
        except ValueError as e:
            print(e)
            continue

        if start_dt is None and end_dt is None:
            # default to last year
            end_dt = datetime.now()
            start_dt = end_dt - timedelta(days=365)
            return start_dt, end_dt

        if start_dt is None:
            # if only end provided, set start as one year before end
            start_dt = end_dt - timedelta(days=365)

        if end_dt is None:
            # if only start provided, set end as now
            end_dt = datetime.now()

        if start_dt > end_dt:
            print("Start date is after end date; swapping them.")
            start_dt, end_dt = end_dt, start_dt

        return start_dt, end_dt


def get_commit_message(prompt, default="graph-greener!"):
    user_input = input(f"{prompt} (default: {default}): ")
    if not user_input.strip():
        return default
    return user_input.strip()


def parse_contributor_token(token):
    """Parse a single contributor token of the form 'Name <email>' or 'Name,email' or 'email'."""
    token = token.strip()
    if not token:
        return None
    if "<" in token and ">" in token:
        try:
            name = token.split("<")[0].strip()
            email = token.split("<")[1].split(">")[0].strip()
            return (name if name else None, email)
        except Exception:
            return None
    if "," in token:
        parts = [p.strip() for p in token.split(",", 1)]
        if len(parts) == 2:
            return (parts[0] or None, parts[1] or None)
    # If the token looks like an email address
    if "@" in token:
        return (None, token)
    # Otherwise treat as name only
    return (token, None)


def get_contributors(prompt="Enter contributors (separated by ';') e.g. 'Name <email>; Other <other@example.com>' (leave blank to use git config):"):
    user_input = input(f"{prompt}\n")
    if not user_input.strip():
        return []
    tokens = [t.strip() for t in user_input.split(";") if t.strip()]
    contributors = []
    for t in tokens:
        parsed = parse_contributor_token(t)
        if parsed:
            contributors.append(parsed)
    return contributors


def choose_contributor_for_index(contributors, idx):
    if not contributors:
        return (None, None)
    return contributors[idx % len(contributors)]


def make_commit(date, repo_path, filename, message="graph-greener!", author_name=None, author_email=None, committer_name=None, committer_email=None):
    filepath = os.path.join(repo_path, filename)
    with open(filepath, "a") as f:
        f.write(f"Commit at {date.isoformat()}\n")
    subprocess.run(["git", "add", filename], cwd=repo_path)
    env = os.environ.copy()
    date_str = date.strftime("%Y-%m-%dT%H:%M:%S")
    env["GIT_AUTHOR_DATE"] = date_str
    env["GIT_COMMITTER_DATE"] = date_str
    if author_name:
        env["GIT_AUTHOR_NAME"] = author_name
    if author_email:
        env["GIT_AUTHOR_EMAIL"] = author_email
    if committer_name:
        env["GIT_COMMITTER_NAME"] = committer_name
    if committer_email:
        env["GIT_COMMITTER_EMAIL"] = committer_email
    subprocess.run(["git", "commit", "-m", message], cwd=repo_path, env=env)


def push_branch(repo_path, branch):
    result = subprocess.run(["git", "remote"], cwd=repo_path, capture_output=True, text=True)
    if result.returncode == 0 and result.stdout.strip():
        subprocess.run(["git", "push", "--set-upstream", "origin", branch], cwd=repo_path)
    else:
        print("No git remote configured. Skipping push step.")


def main():
    print("="*60)
    print("🌱 Welcome to graph-greener - GitHub Contribution Graph Commit Generator 🌱")
    print("="*60)
    print("This tool will help you fill your GitHub contribution graph with custom commits.\n")

    num_commits = get_positive_int("How many commits do you want to make", 20)
    repo_path = get_repo_path("Enter the path to your local git repository", ".")
    current_branch = get_current_branch(repo_path)
    branch = get_branch_name("Enter the branch name to commit to", current_branch)
    branch = checkout_or_create_branch(repo_path, branch)
    filename = get_filename("Enter the filename to modify for commits", "data.txt")
    commit_message = get_commit_message("Enter commit message", "graph-greener!")
    start_dt, end_dt = get_date_range()

    # Contributors
    contributors = get_contributors()
    rotate = False
    if contributors:
        resp = input("Rotate contributors across commits? (y/N): ")
        rotate = resp.strip().lower().startswith("y")

    print(f"\nMaking {num_commits} commits in repo: {repo_path}\nBranch: {branch}\nModifying file: {filename}\nDate range: {start_dt.date()} -> {end_dt.date()}\nCommit message: {commit_message}\nContributors: {len(contributors) if contributors else 0}\n")

    for i in range(num_commits):
        commit_date = random_date_in_range(start_dt, end_dt)
        author_name, author_email = choose_contributor_for_index(contributors, i) if contributors else (None, None)
        committer_name, committer_email = (author_name, author_email)
        if not rotate and contributors:
            # if not rotating, always use first contributor
            author_name, author_email = contributors[0]
            committer_name, committer_email = contributors[0]

        print(f"[{i+1}/{num_commits}] Committing at {commit_date.strftime('%Y-%m-%d %H:%M:%S')} as {author_name or 'git-config'} <{author_email or ''}>")
        make_commit(commit_date, repo_path, filename, message=commit_message, author_name=author_name, author_email=author_email, committer_name=committer_name, committer_email=committer_email)

    print(f"\nPushing commits to branch '{branch}' on your remote repository...")
    push_branch(repo_path, branch)
    print("✅ All done! Check your GitHub contribution graph in a few minutes.\n")
    print("Tip: Use a dedicated repository for best results. Happy coding!")

if __name__ == "__main__":
    main()

