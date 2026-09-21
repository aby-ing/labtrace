package com.qust.lab.srevice;

import com.qust.lab.pojo.vo.SampleAuditLogVO;

import java.util.List;

public interface SampleAuditLogService {

    List<SampleAuditLogVO> listBySampleId(Long sampleId);
}
