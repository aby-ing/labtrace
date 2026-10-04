package com.qust.lab.srevice.impl;

import com.qust.lab.exception.NotFoundException;
import com.qust.lab.mapper.SampleAuditLogMapper;
import com.qust.lab.mapper.SampleMapper;
import com.qust.lab.mapper.UserMapper;
import com.qust.lab.pojo.entity.Sample;
import com.qust.lab.pojo.entity.SampleAuditLog;
import com.qust.lab.pojo.entity.User;
import com.qust.lab.pojo.vo.SampleAuditLogVO;
import com.qust.lab.srevice.SampleAuditLogService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SampleAuditLogServiceImpl
        implements SampleAuditLogService {

    private final SampleAuditLogMapper auditLogMapper;
    private final SampleMapper sampleMapper;
    private final UserMapper userMapper;

    public SampleAuditLogServiceImpl(
            SampleAuditLogMapper auditLogMapper,
            SampleMapper sampleMapper,
            UserMapper userMapper
    ) {
        this.auditLogMapper = auditLogMapper;
        this.sampleMapper = sampleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<SampleAuditLogVO> listBySampleId(Long sampleId) {
        if (sampleId == null || sampleId <= 0) {
            throw new IllegalArgumentException("样品 ID 不合法");
        }

        Sample sample = sampleMapper.selectById(sampleId);

        if (sample == null) {
            throw new NotFoundException("样品不存在");
        }

        List<SampleAuditLog> logs =
                auditLogMapper.selectBySampleId(sampleId);
        Map<Long, String> userNames = getUserNames(logs);

        return logs
                .stream()
                .map(log -> toVO(log, userNames))
                .toList();
    }

    private SampleAuditLogVO toVO(
            SampleAuditLog log,
            Map<Long, String> userNames
    ) {
        SampleAuditLogVO vo = new SampleAuditLogVO();
        vo.setId(log.getId());
        vo.setEventId(log.getEventId());
        vo.setSampleId(log.getSampleId());
        vo.setEventType(log.getEventType());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(
                getUserName(log.getOperatorId(), userNames)
        );
        vo.setToUserId(log.getToUserId());
        vo.setToUserName(
                getUserName(log.getToUserId(), userNames)
        );
        vo.setFromStatus(log.getFromStatus());
        vo.setFromStatusText(getStatusText(log.getFromStatus()));
        vo.setToStatus(log.getToStatus());
        vo.setToStatusText(getStatusText(log.getToStatus()));
        vo.setRemark(log.getRemark());
        vo.setOccurredAt(log.getOccurredAt());
        return vo;
    }

    private Map<Long, String> getUserNames(
            Collection<SampleAuditLog> logs
    ) {
        Set<Long> userIds = new HashSet<>();

        for (SampleAuditLog log : logs) {
            if (log.getOperatorId() != null) {
                userIds.add(log.getOperatorId());
            }

            if (log.getToUserId() != null) {
                userIds.add(log.getToUserId());
            }
        }

        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> userNames = new HashMap<>();

        for (User user : userMapper.selectBatchIds(userIds)) {
            userNames.put(user.getId(), user.getRealName());
        }

        return userNames;
    }

    private String getUserName(
            Long userId,
            Map<Long, String> userNames
    ) {
        if (userId == null) {
            return null;
        }

        return userNames.getOrDefault(userId, "未知用户");
    }

    private String getStatusText(String status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case "CREATED" -> "已创建";
            case "HANDED_OVER" -> "已交接";
            case "STORED" -> "已暂存";
            case "TESTING" -> "检测中";
            case "COMPLETED" -> "已完成";
            case "ABNORMAL" -> "异常";
            default -> "未知状态";
        };
    }
}
