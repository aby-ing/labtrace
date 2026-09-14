package com.qust.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qust.lab.pojo.entity.SampleHandover;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SampleHandoverMapper
        extends BaseMapper<SampleHandover> {

    @Select("""
            SELECT id,
                   sample_id,
                   from_user_id,
                   to_user_id,
                   from_status,
                   to_status,
                   remark,
                   handover_time
            FROM sample_handover
            WHERE sample_id = #{sampleId}
            ORDER BY handover_time ASC, id ASC
            """)
    List<SampleHandover> selectBySampleId(
            @Param("sampleId") Long sampleId
    );
}