# Review Agent Jenkins Shared Library

Register this directory as a Jenkins Global Pipeline Library, or copy `vars/reviewAgentGate.groovy`
into an existing company Shared Library.

Required Jenkins plugins:

- Pipeline
- Credentials Binding
- HTTP Request

Store the Jenkins connector Webhook Secret as a Jenkins Secret Text credential. The default
credential ID is `review-agent-webhook-secret`.

```groovy
@Library('review-agent') _

pipeline {
    agent any

    stages {
        stage('AI Code Review Gate') {
            steps {
                reviewAgentGate(
                    reviewAgentUrl: 'https://review-agent.example.com',
                    projectId: 42,
                    connectorKey: 'jenkins-pipeline:team-a',
                    credentialId: 'review-agent-team-a-secret',
                    targetBranch: env.CHANGE_TARGET ?: 'main',
                    timeoutMinutes: 30,
                    humanReviewAction: 'FAIL'
                )
            }
        }
    }
}
```

`humanReviewAction` supports:

- `FAIL`: fail the build when the Gate returns `NEEDS_HUMAN_REVIEW`.
- `UNSTABLE`: mark the build unstable and allow later stages to continue.

The step derives `sourceBranch`, Job name, build number, build URL, and commit SHA from Jenkins
environment variables. Explicit `sourceBranch` and `targetBranch` values override those defaults.
