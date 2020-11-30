package com.barrelsofdata.springexamples.service;

import com.barrelsofdata.springexamples.dto.EventRequestDto;

public interface TelemetryService {
    void receiveTelemetry(EventRequestDto eventRequest);
}
