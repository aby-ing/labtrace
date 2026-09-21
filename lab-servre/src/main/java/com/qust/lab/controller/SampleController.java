package com.qust.lab.controller;

import com.qust.lab.common.result.PageResult;
import com.qust.lab.common.result.Result;
import com.qust.lab.pojo.dto.SampleCreateDTO;
import com.qust.lab.pojo.dto.SampleHandoverCreateDTO;
import com.qust.lab.pojo.dto.SamplePageQueryDTO;
import com.qust.lab.pojo.dto.SampleStatusChangeDTO;
import com.qust.lab.pojo.dto.SampleUpdateDTO;
import com.qust.lab.pojo.vo.SampleHandoverVO;
import com.qust.lab.pojo.vo.SampleVO;
import com.qust.lab.pojo.vo.SampleAuditLogVO;
import com.qust.lab.srevice.SampleAuditLogService;
import com.qust.lab.srevice.SampleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/samples")
public class SampleController {

    private final SampleService sampleService;
    private final SampleAuditLogService sampleAuditLogService;

    public SampleController(
            SampleService sampleService,
            SampleAuditLogService sampleAuditLogService
    ) {
        this.sampleService = sampleService;
        this.sampleAuditLogService = sampleAuditLogService;
    }

    @GetMapping
    public Result<List<SampleVO>> listAll() {
        return Result.success(sampleService.listAll());
    }

    @PostMapping
    public Result<SampleVO> create(
            @RequestAttribute("userId") Long userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody SampleCreateDTO dto
    ) {
        return Result.success(
                sampleService.create(userId, idempotencyKey, dto)
        );
    }

    @GetMapping("/page")
    public Result<PageResult<SampleVO>> page(
            @Valid SamplePageQueryDTO queryDTO
    ) {
        return Result.success(
                sampleService.page(queryDTO)
        );
    }

    @GetMapping("/{id}")
    public Result<SampleVO> getById(@PathVariable Long id) {
        SampleVO sampleVO = sampleService.getById(id);

        if (sampleVO == null) {
            throw new IllegalArgumentException("样品不存在");
        }

        return Result.success(sampleVO);
    }

    @PutMapping("/{id}/status")
    public Result<SampleVO> changeStatus(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody SampleStatusChangeDTO dto
    ) {
        return Result.success(
                sampleService.changeStatus(userId, id, dto)
        );
    }

    @PutMapping("/{id}")
    public Result<SampleVO> update(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") String role,
            @PathVariable Long id,
            @Valid @RequestBody SampleUpdateDTO dto
    ) {
        return Result.success(
                sampleService.update(userId, role, id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestAttribute("role") String role,
            @PathVariable Long id
    ) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException(
                    "只有管理员可以删除样品"
            );
        }

        sampleService.delete(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/handover")
    public Result<SampleVO> handover(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody SampleHandoverCreateDTO dto
    ) {
        return Result.success(
                sampleService.handover(
                        userId,
                        id,
                        idempotencyKey,
                        dto
                )
        );
    }

    @GetMapping("/{id}/handovers")
    public Result<List<SampleHandoverVO>> listHandoverHistory(
            @PathVariable Long id
    ) {
        return Result.success(
                sampleService.listHandoverHistory(id)
        );
    }

    @GetMapping("/{id}/audit-logs")
    public Result<List<SampleAuditLogVO>> listAuditLogs(
            @PathVariable Long id
    ) {
        return Result.success(
                sampleAuditLogService.listBySampleId(id)
        );
    }
}
