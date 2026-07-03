package com.review.agent.infrastructure.ci;

public interface ProviderCiStatusReporter extends CiStatusService {

    String connectorKey();
}
