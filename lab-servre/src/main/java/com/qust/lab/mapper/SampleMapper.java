package com.qust.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qust.lab.pojo.entity.Sample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SampleMapper extends BaseMapper<Sample> {

    @Update("""
        UPDATE sample
        SET status = #{toStatus},
            custodian_id = #{toUserId},
            version = version + 1
        WHERE id = #{id}
          AND status = #{fromStatus}
          AND version = #{version}
          AND (
                custodian_id = #{operatorId}
                OR (
                    custodian_id IS NULL
                    AND creator_id = #{operatorId}
                )
              )
        """)
    int updateStatusIfCurrent(
            @Param("id") Long id,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus,
            @Param("operatorId") Long operatorId,
            @Param("toUserId") Long toUserId,
            @Param("version") Integer version
    );
    @Update("""
        UPDATE sample
        SET sample_name = #{sampleName},
            source_lab = #{sourceLab},
            risk_level = #{riskLevel},
            version = version + 1
        WHERE id = #{id}
          AND version = #{version}
          AND (
                #{isAdmin} = TRUE
                OR creator_id = #{operatorId}
                OR custodian_id = #{operatorId}
              )
        """)
    int updateBasicInfoIfVersion(
            @Param("id") Long id,
            @Param("sampleName") String sampleName,
            @Param("sourceLab") String sourceLab,
            @Param("riskLevel") String riskLevel,
            @Param("version") Integer version,
            @Param("operatorId") Long operatorId,
            @Param("isAdmin") Boolean isAdmin
    );

    @Delete("""
        DELETE FROM sample
        WHERE id = #{id}
          AND status = 'CREATED'
          AND version = #{version}
        """)
    int deleteIfCreatedAndVersion(
            @Param("id") Long id,
            @Param("version") Integer version
    );
}
