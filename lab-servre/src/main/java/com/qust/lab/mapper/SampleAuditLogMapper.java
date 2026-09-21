package com.qust.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qust.lab.pojo.entity.SampleAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SampleAuditLogMapper
        extends BaseMapper<SampleAuditLog> {

    @Select("""
            SELECT *
            FROM sample_audit_log
            WHERE sample_id = #{sampleId}
            ORDER BY occurred_at ASC, id ASC
            """)
    List<SampleAuditLog> selectBySampleId(
            @Param("sampleId") Long sampleId
    );
}
