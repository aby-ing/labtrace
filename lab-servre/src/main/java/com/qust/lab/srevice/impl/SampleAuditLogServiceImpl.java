package com.qust.lab.srevice.impl;

import com.qust.lab.mapper.SampleAuditLogMapper;
import com.qust.lab.mapper.SampleMapper;
import com.qust.lab.mapper.UserMapper;
import com.qust.lab.pojo.entity.Sample;
import com.qust.lab.pojo.entity.SampleAuditLog;
import com.qust.lab.pojo.entity.User;
import com.qust.lab.pojo.vo.SampleAuditLogVO;
import com.qust.lab.srevice.SampleAuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

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
            throw new IllegalArgumentException("样品不存在");
        }

        return auditLogMapper.selectBySampleId(sampleId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private SampleAuditLogVO toVO(SampleAuditLog log) {
        SampleAuditLogVO vo = new SampleAuditLogVO();
        vo.setId(log.getId());
        vo.setEventId(log.getEventId());
        vo.setSampleId(log.getSampleId());
        vo.setEventType(log.getEventType());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(getUserName(log.getOperatorId()));
        vo.setToUserId(log.getToUserId());
        vo.setToUserName(getUserName(log.getToUserId()));
        vo.setFromStatus(log.getFromStatus());
        vo.setFromStatusText(getStatusText(log.getFromStatus()));
        vo.setToStatus(log.getToStatus());
        vo.setToStatusText(getStatusText(log.getToStatus()));
        vo.setRemark(log.getRemark());
        vo.setOccurredAt(log.getOccurredAt());
        return vo;
    }

    private String getUserName(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userMapper.selectById(userId);
        return user == null ? "未知用户" : user.getRealName();
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
