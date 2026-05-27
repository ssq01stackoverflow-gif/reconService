package com.recon.service.interfaces.controller;

import com.recon.service.application.service.ChangeEventApplicationService;
import com.recon.service.application.service.ChangeEventProcessResult;
import com.recon.service.common.ApiResponse;
import com.recon.service.domain.model.ChangeEvent;
import com.recon.service.infrastructure.messaging.CanalMessageParser;
import com.recon.service.interfaces.dto.ChangeEventProcessResponse;
import com.recon.service.interfaces.dto.ConsumeCanalMessageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/recon/events")
public class ReconEventController {

    private final CanalMessageParser canalMessageParser;
    private final ChangeEventApplicationService changeEventApplicationService;

    public ReconEventController(CanalMessageParser canalMessageParser,
                                ChangeEventApplicationService changeEventApplicationService) {
        this.canalMessageParser = canalMessageParser;
        this.changeEventApplicationService = changeEventApplicationService;
    }

    @PostMapping("/canal")
    public ApiResponse<ChangeEventProcessResponse> consumeCanalMessage(@Valid @RequestBody ConsumeCanalMessageRequest request) {
        ChangeEvent event = canalMessageParser.parse(request.getRawMessage());
        ChangeEventProcessResult result = changeEventApplicationService.process(event);
        return ApiResponse.success(ChangeEventProcessResponse.from(result));
    }
}
