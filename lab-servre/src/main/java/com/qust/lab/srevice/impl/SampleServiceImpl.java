package com.qust.lab.srevice.impl;
import com.qust.lab.pojo.dto.SampleCreateDTO;
import com.qust.lab.pojo.dto.SampleStatusChangeDTO;
import com.qust.lab.pojo.enums.SampleStatus;
import com.qust.lab.utils.RedisIdempotencyService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.qust.lab.mapper.SampleMapper;
import com.qust.lab.pojo.entity.Sample;
import com.qust.lab.pojo.vo.SampleVO;
import com.qust.lab.srevice.SampleService;
import com.qust.lab.srevice.OutboxMessageService;
import org.springframework.stereotype.Service;
import com.qust.lab.pojo.dto.SampleUpdateDTO;
import java.util.List;
import com.qust.lab.mapper.SampleHandoverMapper;
import com.qust.lab.pojo.dto.SampleHandoverCreateDTO;
import com.qust.lab.pojo.entity.SampleHandover;
import org.springframework.transaction.annotation.Transactional;
import com.qust.lab.pojo.vo.SampleHandoverVO;
import com.qust.lab.mapper.UserMapper;
import com.qust.lab.pojo.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qust.lab.common.result.PageResult;
import com.qust.lab.pojo.dto.SamplePageQueryDTO;
import com.qust.lab.pojo.event.SampleCreatedEvent;
import com.qust.lab.pojo.event.SampleStatusChangedEvent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class SampleServiceImpl implements SampleService {

    private final SampleMapper sampleMapper;
    private final SampleHandoverMapper sampleHandoverMapper;
    private final UserMapper userMapper;
    private final RedisIdempotencyService idempotencyService;
    private final OutboxMessageService outboxMessageService;

    public SampleServiceImpl(
            SampleMapper sampleMapper,
            SampleHandoverMapper sampleHandoverMapper,
            UserMapper userMapper,
            RedisIdempotencyService idempotencyService,
            OutboxMessageService outboxMessageService
    ) {
        this.sampleMapper = sampleMapper;
        this.sampleHandoverMapper = sampleHandoverMapper;
        this.userMapper = userMapper;
        this.idempotencyService = idempotencyService;
        this.outboxMessageService = outboxMessageService;
    }

    @Override
    public List<SampleVO> listAll() {
        List<Sample> samples = sampleMapper.selectList(null);

        return samples.stream()
                .map(this::convertToVO)
                .toList();
    }
    @Override
    @Transactional
    public SampleVO create(
            Long creatorId,
            String idempotencyKey,
            SampleCreateDTO dto
    ) {
        RedisIdempotencyService.LockToken lockToken =
                idempotencyService.acquire(
                        creatorId,
                        "sample:create",
                        idempotencyKey
                );

        boolean completed = false;

        try {
        if (creatorId == null || creatorId <= 0) {
            throw new IllegalArgumentException("当前用户信息无效");
        }

        if (dto == null) {
            throw new IllegalArgumentException("样品数据不能为空");
        }

        Sample sample = new Sample();

        sample.setSampleNo(generateSampleNo());
        sample.setSampleName(dto.getSampleName());
        sample.setSourceLab(dto.getSourceLab());
        sample.setRiskLevel(dto.getRiskLevel());

        // 使用 JWT 中的当前登录用户
        sample.setCreatorId(creatorId);

        // 新样品的初始状态由后端设置
        sample.setStatus(SampleStatus.CREATED.name());

        // 与数据库默认值保持一致，确保创建接口返回 version = 0
        sample.setVersion(0);

        sample.setCreatedAt(LocalDateTime.now());

        sampleMapper.insert(sample);

            outboxMessageService.saveSampleCreated(
                    new SampleCreatedEvent(
                            sample.getId(),
                            sample.getSampleNo(),
                            sample.getSampleName(),
                            sample.getCreatorId(),
                            sample.getStatus(),
                            sample.getRiskLevel(),
                            sample.getCreatedAt()
                    )
            );

            SampleVO result = convertToVO(sample);
            completed = true;
            return result;
        } finally {
            if (!completed) {
                idempotencyService.release(lockToken);
            }
        }
    }
    @Override
    public SampleVO getById(Long id) {
        Sample sample = sampleMapper.selectById(id);

        if (sample == null) {
            return null;
        }

        return convertToVO(sample);
    }
    @Override
    @Transactional
    public SampleVO changeStatus(
            Long operatorId,
            Long id,
            SampleStatusChangeDTO dto
    ) {
        // 1. 查询样品
        Sample sample = sampleMapper.selectById(id);

        if (sample == null) {
            throw new IllegalArgumentException("样品不存在");
        }

        // 2. 校验请求参数
        if (dto == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("目标状态不能为空");
        }

        if (dto.getToUserId() == null) {
            throw new IllegalArgumentException("接收人不能为空");
        }

        if (dto.getToUserId() <= 0) {
            throw new IllegalArgumentException("接收人 ID 不合法");
        }
        User targetUser =
                userMapper.selectById(dto.getToUserId());

        if (targetUser == null) {
            throw new IllegalArgumentException("接收人不存在");
        }

        if (!Integer.valueOf(1).equals(targetUser.getStatus())) {
            throw new IllegalArgumentException("接收人已被禁用");
        }

        // 3. 将数据库中的状态转换为枚举
        SampleStatus currentStatus;

        try {
            currentStatus = SampleStatus.valueOf(sample.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("样品当前状态数据异常");
        }

        // 4. 获取目标状态
        SampleStatus targetStatus = dto.getStatus();

        // 5. 检查状态是否允许流转
        if (!canTransit(currentStatus, targetStatus)) {
            throw new IllegalArgumentException(
                    "不允许从 " + currentStatus + " 流转到 " + targetStatus
            );
        }

        // 6. 确定交出人
        Long expectedOperatorId = sample.getCustodianId();

        if (expectedOperatorId == null) {
            expectedOperatorId = sample.getCreatorId();
        }

        if (!operatorId.equals(expectedOperatorId)) {
            throw new IllegalArgumentException(
                    "当前用户不是该样品的责任人"
            );
        }

        Long fromUserId = operatorId;

        // 7. 创建交接历史记录
        SampleHandover handover = new SampleHandover();

        handover.setSampleId(sample.getId());
        handover.setFromUserId(fromUserId);
        handover.setToUserId(dto.getToUserId());
        handover.setFromStatus(currentStatus.name());
        handover.setToStatus(targetStatus.name());
        handover.setRemark(dto.getRemark());
        handover.setHandoverTime(LocalDateTime.now());

        // 8. 先安全更新样品状态
        Integer currentVersion = sample.getVersion();

        if (currentVersion == null) {
            currentVersion = 0;
        }

        int updatedRows = sampleMapper.updateStatusIfCurrent(
                sample.getId(),
                currentStatus.name(),
                targetStatus.name(),
                operatorId,
                dto.getToUserId(),
                currentVersion
        );

// 更新失败，说明样品可能被别人先操作了
        if (updatedRows == 0) {
            throw new IllegalArgumentException(
                    "样品状态已被其他人修改，请刷新后重试"
            );
        }

        // 9. 状态更新成功后，再保存交接历史
        sampleHandoverMapper.insert(handover);

        outboxMessageService.saveSampleStatusChanged(
                new SampleStatusChangedEvent(
                        sample.getId(),
                        operatorId,
                        dto.getToUserId(),
                        currentStatus.name(),
                        targetStatus.name(),
                        dto.getRemark()
                )
        );

// 10. 更新内存中的对象，用于返回结果
        sample.setStatus(targetStatus.name());
        sample.setCustodianId(dto.getToUserId());
        sample.setVersion(currentVersion + 1);

        // 10. 返回最新样品
        return convertToVO(sample);
    }
    @Override
    public SampleVO update(
            Long operatorId,
            String role,
            Long id,
            SampleUpdateDTO dto
    ) {
        // 1. 查询样品
        Sample sample = sampleMapper.selectById(id);

        if (sample == null) {
            throw new IllegalArgumentException("样品不存在");
        }
        boolean isAdmin = "ADMIN".equals(role);

        boolean isCreator = operatorId != null
                && operatorId.equals(sample.getCreatorId());

        boolean isCustodian = operatorId != null
                && operatorId.equals(sample.getCustodianId());

        if (!isAdmin && !isCreator && !isCustodian) {
            throw new IllegalArgumentException(
                    "只有管理员、创建人或当前保管人可以修改样品"
            );
        }

        // 2. 检查请求数据
        if (dto == null) {
            throw new IllegalArgumentException("修改数据不能为空");
        }

        if (dto.getVersion() == null) {
            throw new IllegalArgumentException(
                    "修改请求缺少 version"
            );
        }

        // 3. 获取数据库当前版本
        Integer dbVersion = sample.getVersion();

        if (dbVersion == null) {
            dbVersion = 0;
        }

        // 4. 前端版本和数据库版本不一致
        if (!dbVersion.equals(dto.getVersion())) {
            throw new IllegalArgumentException(
                    "样品已经被其他人修改，请刷新后重试"
            );
        }

        // 5. 没传的字段保留原值
        String sampleName = dto.getSampleName() != null
                ? dto.getSampleName()
                : sample.getSampleName();

        String sourceLab = dto.getSourceLab() != null
                ? dto.getSourceLab()
                : sample.getSourceLab();

        String riskLevel = dto.getRiskLevel() != null
                ? dto.getRiskLevel()
                : sample.getRiskLevel();

        // 6. 根据 id 和 version 安全更新
        int updatedRows = sampleMapper.updateBasicInfoIfVersion(
                id,
                sampleName,
                sourceLab,
                riskLevel,
                dto.getVersion()
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException(
                    "样品已经被其他人修改，请刷新后重试"
            );
        }

        // 7. 更新内存对象，返回最新数据
        sample.setSampleName(sampleName);
        sample.setSourceLab(sourceLab);
        sample.setRiskLevel(riskLevel);
        sample.setVersion(dbVersion + 1);

        return convertToVO(sample);
    }
    @Override
    public void delete(Long id) {
        // 1. 查询样品
        Sample sample = sampleMapper.selectById(id);

        // 2. 样品不存在
        if (sample == null) {
            throw new IllegalArgumentException("样品不存在");
        }

        // 3. 只有刚创建、尚未流转的样品允许删除
        if (!SampleStatus.CREATED.name().equals(sample.getStatus())) {
            throw new IllegalArgumentException(
                    "当前样品已经发生流转，不能直接删除"
            );
        }

        // 4. 根据主键删除数据库记录
        sampleMapper.deleteById(id);
    }
    @Override
    @Transactional
    public SampleVO handover(
            Long operatorId,
            Long id,
            String idempotencyKey,
            SampleHandoverCreateDTO dto
    ){
        if (dto == null) {
            throw new IllegalArgumentException("交接数据不能为空");
        }

        RedisIdempotencyService.LockToken lockToken =
                idempotencyService.acquire(
                        operatorId,
                        "sample:handover:" + id,
                        idempotencyKey
                );

        boolean completed = false;

        try {
        SampleStatusChangeDTO statusDTO = new SampleStatusChangeDTO();

        statusDTO.setStatus(SampleStatus.HANDED_OVER);
        statusDTO.setToUserId(dto.getToUserId());
        statusDTO.setRemark(dto.getRemark());

            SampleVO result = changeStatus(operatorId, id, statusDTO);
            completed = true;
            return result;
        } finally {
            if (!completed) {
                idempotencyService.release(lockToken);
            }
        }
    }
    @Override
    public List<SampleHandoverVO> listHandoverHistory(Long sampleId) {
        // 1. 检查样品 ID
        if (sampleId == null || sampleId <= 0) {
            throw new IllegalArgumentException("样品 ID 不合法");
        }

        // 2. 先确认样品存在
        Sample sample = sampleMapper.selectById(sampleId);

        if (sample == null) {
            throw new IllegalArgumentException("样品不存在");
        }

        // 3. 查询该样品的所有流转记录
        List<SampleHandover> handovers =
                sampleHandoverMapper.selectBySampleId(sampleId);

        // 4. 转换成前端需要的 VO
        return handovers.stream()
                .map(this::convertToHandoverVO)
                .toList();
    }
    @Override
    public PageResult<SampleVO> page(
            SamplePageQueryDTO queryDTO
    ) {
        if (queryDTO == null) {
            queryDTO = new SamplePageQueryDTO();
        }

        Long page = queryDTO.getPage();
        Long pageSize = queryDTO.getPageSize();

        if (page == null || page < 1) {
            throw new IllegalArgumentException("页码必须大于 0");
        }

        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "每页条数必须在 1 到 100 之间"
            );
        }

        QueryWrapper<Sample> wrapper = new QueryWrapper<>();

        if (queryDTO.getSampleName() != null
                && !queryDTO.getSampleName().isBlank()) {
            wrapper.like(
                    "sample_name",
                    queryDTO.getSampleName().trim()
            );
        }

        if (queryDTO.getStatus() != null
                && !queryDTO.getStatus().isBlank()) {
            wrapper.eq(
                    "status",
                    queryDTO.getStatus().trim()
            );
        }

        if (queryDTO.getRiskLevel() != null
                && !queryDTO.getRiskLevel().isBlank()) {
            wrapper.eq(
                    "risk_level",
                    queryDTO.getRiskLevel().trim()
            );
        }

        wrapper.orderByDesc("created_at");

        Page<Sample> mpPage = new Page<>(page, pageSize);

        Page<Sample> result =
                sampleMapper.selectPage(mpPage, wrapper);

        List<SampleVO> records = result.getRecords()
                .stream()
                .map(this::convertToVO)
                .toList();

        return new PageResult<>(
                records,
                result.getTotal(),
                page,
                pageSize
        );
    }

    private SampleVO convertToVO(Sample sample) {
        SampleVO vo = new SampleVO();
        vo.setId(sample.getId());
        vo.setVersion(sample.getVersion());


        vo.setSampleNo(sample.getSampleNo());
        vo.setSampleName(sample.getSampleName());
        vo.setSourceLab(sample.getSourceLab());

        vo.setCreatorName(getUserRealName(sample.getCreatorId()));
        vo.setCustodianName(getUserRealName(sample.getCustodianId()));

        vo.setStatus(sample.getStatus());
        vo.setStatusText(getStatusText(sample.getStatus()));
        vo.setRiskLevel(sample.getRiskLevel());
        vo.setCreatedAt(sample.getCreatedAt());

        return vo;
    }
    private String getUserRealName(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userMapper.selectById(userId);

        if (user == null) {
            return "未知用户";
        }

        return user.getRealName();
    }

    private String getStatusText(String status) {
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
    private String generateSampleNo() {
        String randomText = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "SAM-" + LocalDate.now() + "-" + randomText;
    }
    private boolean canTransit(
            SampleStatus currentStatus,
            SampleStatus targetStatus
    ) {
        return switch (currentStatus) {
            case CREATED ->
                    targetStatus == SampleStatus.HANDED_OVER
                            || targetStatus == SampleStatus.ABNORMAL;

            case HANDED_OVER ->
                    targetStatus == SampleStatus.STORED
                            || targetStatus == SampleStatus.ABNORMAL;

            case STORED ->
                    targetStatus == SampleStatus.TESTING
                            || targetStatus == SampleStatus.ABNORMAL;

            case TESTING ->
                    targetStatus == SampleStatus.COMPLETED
                            || targetStatus == SampleStatus.ABNORMAL;

            case COMPLETED, ABNORMAL -> false;
        };
    }
    private SampleHandoverVO convertToHandoverVO(
            SampleHandover handover
    ) {
        SampleHandoverVO vo = new SampleHandoverVO();

        vo.setId(handover.getId());
        vo.setSampleId(handover.getSampleId());

        vo.setFromUserId(handover.getFromUserId());
        vo.setFromUserName(
                getUserRealName(handover.getFromUserId())
        );

        vo.setToUserId(handover.getToUserId());
        vo.setToUserName(
                getUserRealName(handover.getToUserId())
        );

        vo.setFromStatus(handover.getFromStatus());
        vo.setFromStatusText(
                getStatusText(handover.getFromStatus())
        );

        vo.setToStatus(handover.getToStatus());
        vo.setToStatusText(
                getStatusText(handover.getToStatus())
        );

        vo.setRemark(handover.getRemark());
        vo.setHandoverTime(handover.getHandoverTime());

        return vo;
    }
}
