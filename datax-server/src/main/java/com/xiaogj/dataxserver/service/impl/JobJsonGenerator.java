package com.xiaogj.dataxserver.service.impl;

import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.util.DataXJobFile;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.DataTable;
import com.xiaogj.dataxserver.vo.DataTableStatus;
import com.xiaogj.dataxserver.vo.TableInfoVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate Datax jobs json files to Datax home/jobs folder
 *
 * @author Jawf Can Li
 * @since 1.0 base on datax 3.0
 */
@Service
public class JobJsonGenerator {
    protected static final Log log = LogFactory.getLog(JobJsonGenerator.class);

    @Autowired
    private IDBInfoService dbInfoService;

    @Autowired
    private DataXJobFile dataXJobFile;

    public List<String> batchGenerateForDB() {
        List<String> jsonPathList = new ArrayList<>();
		List<DBInfoVO> dbInfoList = dbInfoService.getDBInfoList();
        List<TableInfoVO> tableInfoList = dbInfoService.getTableInfoList();
        DBInfoVO targetDB = new DBInfoVO();
        targetDB.setUsername("default");
        targetDB.setPassword("xiaogj2021");
        targetDB.setJdbcUrl("jdbc:clickhouse://120.26.63.64:8123");
		for (DBInfoVO sourceDB : dbInfoList) {
            for (TableInfoVO tableInfoVO : tableInfoList) {
                dataXJobFile.generateMs2ChJsonJobFile(sourceDB, targetDB, "tClass", jsonPathList);
            }
        }
		return jsonPathList;
    }

    public void generate() throws SQLException {
        List<DataTableStatus> sourceTables = dbInfoService.getSourceTransfterTablesStatus();
        List<DataTable> targetTables = dbInfoService.getTargetTransfterTables();

        if (sourceTables != null) {
            // int i = 0;
            for (DataTable ta : sourceTables) {
                String sourceTableName = ta.getName();
                if (sourceTableName == null || "".equals(sourceTableName)) {
                    throw new SQLException("Source Table is empty or not existed!");
                }
                String targetTableName = getTargetTableName(sourceTableName, targetTables);
                List<String> columns = dbInfoService.getTargetTransfterTableColumns(targetTableName);
                List<String> pks = dbInfoService.getTargetTransfterTablePrimaryKey(targetTableName);
                String whereClause = dataXJobFile.getSourceGlobalTableWhereClause(columns);
                long migrationRecords = dbInfoService.getSourceTransfterTableMigrationCount(sourceTableName, whereClause);
                String pk = null;
                if (pks != null && !pks.isEmpty()) {
                    if (pks.size() == 1) {
                        pk = pks.get(0);
                    }
                }
                dataXJobFile.generateJsonJobFile(sourceTableName, targetTableName, columns, pk, whereClause, migrationRecords);
                // i++;
                // if (i==30)
                // break;// remove this line*/
            }
        }
    }

    private String getTargetTableName(String sourceTableName, List<DataTable> targetTables) throws SQLException {
        String result = null;
        if (sourceTableName != null && targetTables != null) {
            for (DataTable t : targetTables) {
                if (sourceTableName.equalsIgnoreCase(t.getName())) {
                    result = t.getName();
                    break;
                }
            }
        }
        if (result == null) {
            String errorMsg = "Target Table for " + sourceTableName + " is empty or not existed!";
            log.error(errorMsg);
            throw new SQLException(errorMsg);
        }
        return result;
    }

}
