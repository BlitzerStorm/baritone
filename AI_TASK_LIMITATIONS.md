# Automation Request Limitations

The tasks requested in the latest user instructions require live automation against
GitHub (cloning directly from GitHub, fetching open/closed issues, pushing fix
branches, and opening pull requests). The execution environment available to this
agent intentionally omits GitHub credentials and network automation helpers, so
those steps cannot be completed automatically.

To proceed manually you can:

1. Clone the repository locally with your own credentials.
2. Fetch issues via the GitHub API or the web interface.
3. Create feature/fix branches for each issue you intend to resolve.
4. Push those branches and open pull requests through your GitHub account.
5. Comment on the issues with the results of your fixes and cross-link the PRs.

Any code modifications made within this container can be committed locally and
exported (for example, by downloading a patch or the repository archive). You can
then apply those changes in your own environment where GitHub access is
available.

This file documents the limitation so future automation attempts are aware of the
constraints present in this workspace.
