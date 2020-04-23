package com.xiaogj.dataxserver.service.impl;

import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.service.ISyncDataService;
import com.xiaogj.dataxserver.util.DataXJobFile;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.OperateTargetSqlVO;
import com.xiaogj.dataxserver.vo.TableInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SyncDataServiceImpl implements ISyncDataService {

    @Autowired
    private IDBInfoService dbInfoService;

    @Autowired
    private DataXJobFile dataXJobFile;

    @Autowired
    private JobJsonConfig jobJsonConfig;

    public void syncData(int sourcedbId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sdf.format(new Date());
        DBInfoVO sourceDB = dbInfoService.getDBInfoById(sourcedbId);
        if (sourceDB == null) {
            throw new IllegalArgumentException("未找到，请检查数据库ID ==>> " + sourcedbId);
        }
        List<TableInfoVO> tableInfoList = dbInfoService.getTableInfoJoinSyncList(sourcedbId);
        log.info("sync tables size={} .", tableInfoList.size());
        DBInfoVO targetDB = new DBInfoVO();
        targetDB.setName(jobJsonConfig.getTargetDbName());
        targetDB.setUsername(jobJsonConfig.getTargetDbUsername());
        targetDB.setPassword(jobJsonConfig.getTargetDbPassword());
        targetDB.setJdbcUrl(jobJsonConfig.getTargetDbUrl());
        List<String> targetTableNameList = new ArrayList<>();
        List<String> targetCreateTempTables = new ArrayList<>();
        StringBuffer jobJsonDir = new StringBuffer(jobJsonConfig.getDataxToolFolder()).append("job/").append(sourceDB.getName()).append("/").append(format).append("/");
        for (TableInfoVO tableInfoVO : tableInfoList) {
//            if (tableInfoVO.getSyncVO() != null) {
//                java.sql.Date serviceTimeSync = tableInfoVO.getSyncVO().getServiceTimeSync();
//                if (serviceTimeSync != null) {
//                    if (DateUtils.isSameDay(serviceTimeSync, new Date())) {
//                        log.warn("表 {} 已同步至当前最新。", tableInfoVO.getName());
//                        continue;
//                    }
//                }
//            }
            String createSql = tableInfoVO.getCreateSql();
            StringBuffer targetTableNameSB = new StringBuffer();
            targetTableNameSB.append(targetDB.getName());
            targetTableNameSB.append(".").append(sourceDB.getName());
            targetTableNameSB.append("_").append(tableInfoVO.getName()).append("_");
            String targetTableName = targetTableNameSB.append(format).toString();
            targetTableNameList.add(targetTableName);
            createSql = createSql.replace("{target.db.table.name}", targetTableName);
            targetCreateTempTables.add(createSql);
            dataXJobFile.generateMs2ChJsonJobFile(sourceDB, targetDB, tableInfoVO, targetTableName, jobJsonDir.toString(), format);
        }
        log.info("create temp table in clickhouse start ==>>");
        dbInfoService.createTabel4ClickHouse(targetCreateTempTables);
        log.info("create temp table in clickhouse end...");

        log.info("run datax start ==>>");
        runDatax(sourceDB);
        log.info("run datax end...");

        log.info("join tables on clickhouse start ==>>");
        operateTable4ClickHouse(sourceDB, targetDB, format);
        log.info("join tables on clickhouse end...");

        if (jobJsonConfig.isTempTableDel()) {
            log.info("drop temp tables start ==>>");
            dbInfoService.dropAllTable4ClickHouse(targetTableNameList);
            log.info("drop temp tables end...");
        } else {
            log.info("temp tables not drop in config.");
        }

        log.info("updateServiceTimeSync start ==>>");
        dbInfoService.updateServiceTimeSync(tableInfoList);
        log.info("{} updateServiceTimeSync end...", sourceDB.getName());

        if (jobJsonConfig.isJobJsonDel()) {
            log.info("rm job json start ==>>");
            deletJobJson(sourceDB, jobJsonDir);
            log.info("rm job json end...");
        } else {
            log.info("job json not rm in config.");
        }
    }

    private void deletJobJson(DBInfoVO sourceDB, StringBuffer jobJsonDir) throws IOException {
        for (String jsonPath : sourceDB.getJsonPathList()) {
            Files.deleteIfExists(Paths.get(jsonPath));
        }
        Files.deleteIfExists(Paths.get(jobJsonDir.toString()));
    }

    private void operateTable4ClickHouse(DBInfoVO sourceDB, DBInfoVO targetDB, String format) throws Exception {
        List<OperateTargetSqlVO> operateSqlList = dbInfoService.getOperateTargetSqlList();
        if (!CollectionUtils.isEmpty(operateSqlList)) {
            String tablePrefix = new StringBuffer(targetDB.getName()).append(".").append(sourceDB.getName()).append("_").toString();
            String tableSuffix = new StringBuffer("_").append(format).toString();
            dbInfoService.execOperateSql(operateSqlList, tablePrefix, tableSuffix);
        } else {
            throw new Exception("not found any from operate_target_db");
        }
    }

    private void runDatax(DBInfoVO dbInfoVO) throws Exception {
        String[] cmds = new String[0];
        Process pr = null;
        for (String jsonPath : dbInfoVO.getJsonPathList()) {
            String cmd = null;
            cmds = new String[3];
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmds = new String[]{"cmd", "/c", new StringBuilder("python ").append(jobJsonConfig.getDataxToolFolder()).append("bin/datax.py ").append(jsonPath).toString()};

            } else {
                cmds = new String[]{"/bin/sh", "-c", new StringBuilder("python ").append(jobJsonConfig.getDataxToolFolder()).append("bin/datax.py ").append(jsonPath).toString()};
            }
            log.info(cmds.toString());
            pr = Runtime.getRuntime().exec(cmds);
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                log.info(line);
            }
            in.close();
            pr.waitFor();
            if (pr.exitValue() != 0) {
                throw new Exception("run datax fail, please check linux log.");
            }
            log.info("{}, target db insert end", jsonPath);
        }
    }

}
