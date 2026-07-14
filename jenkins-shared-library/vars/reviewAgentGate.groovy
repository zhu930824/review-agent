import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

def call(Map config = [:]) {
    String reviewAgentUrl = required(config, 'reviewAgentUrl').toString().replaceAll('/+$', '')
    Long projectId = Long.valueOf(required(config, 'projectId').toString())
    String credentialId = (config.credentialId ?: 'review-agent-webhook-secret').toString()
    String connectorKey = (config.connectorKey ?: 'jenkins-pipeline').toString()
    String sourceBranch = (config.sourceBranch ?: env.CHANGE_BRANCH ?: env.BRANCH_NAME).toString()
    String targetBranch = (config.targetBranch ?: env.CHANGE_TARGET ?: 'main').toString()
    int timeoutMinutes = Integer.valueOf((config.timeoutMinutes ?: 30).toString())
    int pollSeconds = Integer.valueOf((config.pollSeconds ?: 10).toString())
    String humanReviewAction = (config.humanReviewAction ?: 'FAIL').toString().toUpperCase()

    withCredentials([string(credentialsId: credentialId, variable: 'REVIEW_AGENT_TOKEN')]) {
        Map review = triggerReview(reviewAgentUrl, connectorKey, projectId, sourceBranch, targetBranch)
        if (review.triggerStatus != 'PROCESSED' || review.triggerReviewId == null) {
            error("Review Agent trigger failed: ${review.triggerStatus} ${review.triggerMessage ?: ''}".trim())
        }

        Map finalGate = null
        timeout(time: timeoutMinutes, unit: 'MINUTES') {
            waitUntil {
                finalGate = fetchGate(reviewAgentUrl, connectorKey, review.triggerReviewId)
                switch (finalGate.gateStatus) {
                    case 'PASSED':
                        return true
                    case 'BLOCKED':
                        error("Review Agent blocked build #${review.triggerReviewId}: ${blockedReasons(finalGate)}")
                        break
                    case 'NEEDS_HUMAN_REVIEW':
                        if (humanReviewAction == 'UNSTABLE') {
                            unstable("Review Agent requires human review #${review.triggerReviewId}: ${blockedReasons(finalGate)}")
                            return true
                        }
                        error("Review Agent requires human review #${review.triggerReviewId}: ${blockedReasons(finalGate)}")
                        break
                    default:
                        sleep time: pollSeconds, unit: 'SECONDS'
                        return false
                }
            }
        }
        return finalGate
    }
}

private Map triggerReview(
        String reviewAgentUrl,
        String connectorKey,
        Long projectId,
        String sourceBranch,
        String targetBranch) {
    Map payload = [
            projectId   : projectId,
            sourceBranch: sourceBranch,
            targetBranch: targetBranch,
            jobName     : env.JOB_NAME,
            buildNumber : env.BUILD_NUMBER,
            buildUrl    : env.BUILD_URL,
            commitSha   : env.GIT_COMMIT
    ]
    def response = httpRequest(
            httpMode: 'POST',
            url: "${reviewAgentUrl}/api/integration/webhooks/jenkins/reviews",
            customHeaders: reviewHeaders(connectorKey, true),
            contentType: 'APPLICATION_JSON',
            requestBody: JsonOutput.toJson(payload),
            validResponseCodes: '200:299',
            quiet: true)
    return responseData(response.content)
}

private Map fetchGate(String reviewAgentUrl, String connectorKey, Object reviewId) {
    def response = httpRequest(
            httpMode: 'GET',
            url: "${reviewAgentUrl}/api/integration/webhooks/jenkins/reviews/${reviewId}/gate",
            customHeaders: reviewHeaders(connectorKey, false),
            validResponseCodes: '200:299',
            quiet: true)
    return responseData(response.content)
}

private List<Map<String, Object>> reviewHeaders(String connectorKey, boolean includeDelivery) {
    List<Map<String, Object>> headers = [
            [name: 'X-Review-Agent-Token', value: env.REVIEW_AGENT_TOKEN, maskValue: true],
            [name: 'X-Review-Agent-Connector', value: connectorKey]
    ]
    if (includeDelivery) {
        headers.add([name: 'X-Jenkins-Delivery', value: "${env.JOB_NAME}:${env.BUILD_NUMBER}"])
    }
    return headers
}

private Map responseData(String content) {
    Map response = (Map) new JsonSlurperClassic().parseText(content)
    if (response.code != 200 || !(response.data instanceof Map)) {
        error("Unexpected Review Agent response: ${response.message ?: response.code}")
    }
    return (Map) response.data
}

private String blockedReasons(Map gate) {
    List reasons = gate.blockedReasons instanceof List ? (List) gate.blockedReasons : []
    return reasons ? reasons.join('; ') : (gate.summary ?: gate.gateStatus).toString()
}

private Object required(Map config, String key) {
    Object value = config[key]
    if (value == null || value.toString().trim().isEmpty()) {
        error("reviewAgentGate requires '${key}'")
    }
    return value
}
