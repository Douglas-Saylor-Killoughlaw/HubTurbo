package backend.github;

import backend.interfaces.Repo;
import backend.interfaces.TaskRunner;

import java.io.IOException;

/**
 * This class represents an async task that edits the state of an issue on GitHub.
 */
public class EditIssueStateTask extends GitHubRepoTask<Boolean> {

    private final String repoId;
    private final int issueId;
    private final boolean open;

    public EditIssueStateTask(TaskRunner taskRunner, Repo repo, String repoId, int issueId, boolean open) {
        super(taskRunner, repo);
        this.repoId = repoId;
        this.issueId = issueId;
        this.open = open;
    }

    @Override
    public void run() {
        try {
            response.complete(
                    repo.editIssueState(repoId, issueId, open)
            );
        } catch (IOException e) {
            response.complete(false);
        }
    }
}
