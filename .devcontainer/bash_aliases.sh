function dotenv() {
    if [[ -n "$1" ]]
    then
        echo "param"
        set -o allexport
        [ -f $1 ] && . $1
        set +o allexport
        return
    fi

    set -o allexport
    for filename in ~/dotenv-files/.env*; do
        [ -e "$filename" ] || continue
        # source the env-file
        . $filename
    done
    set +o allexport
}

alias chezmoi-init='chezmoi init $CHEZMOI_REPO --apply'
source <(chezmoi completion bash)

# git aliases

git config --global alias.pf "push --force-with-lease"

# allows to rebase the complete feature-branch interactively
git config --global alias.rebase-feature-branch-interactive "! git rebase -i \$(git merge-base main \$(git rev-parse --abbrev-ref HEAD))"

# rebases the feature branch on its first commit (and squashs all other commits)
git config --global alias.rebase-feature-branch "! GIT_EDITOR=\"sed -i '2,/^$/s/^pick\b/squash/'\" git rebase -i \$(git merge-base main \$(git rev-parse --abbrev-ref HEAD))"
