package com.qust.lab.controller;

import com.qust.lab.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health/database")
    public Result<Map<String, Object>> database() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("connected", connection.isValid(2));
            data.put("database", connection.getCatalog());
            data.put("url", metaData.getURL());
            data.put("username", metaData.getUserName());

            return Result.success(data);
        }
    }
}
