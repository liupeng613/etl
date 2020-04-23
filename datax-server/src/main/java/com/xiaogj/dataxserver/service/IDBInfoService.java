package com.xiaogj.dataxserver.service;


import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.OperateTargetSqlVO;
import com.xiaogj.dataxserver.vo.SyncVO;
import com.xiaogj.dataxserver.vo.TableInfoVO;

import java.sql.SQLException;
import java.util.List;

public interface IDBInfoService {

    List<DBInfoVO> getDBInfoList() throws SQLException;

    void createTabel4ClickHouse(List<String> createSqls) throws SQLException;

    void updateServiceTimeSync(List<TableInfoVO> syncList) throws SQLException;

    DBInfoVO getDBInfoById(int sourcedbId) throws SQLException;

    List<TableInfoVO> getTableInfoJoinSyncList(int sourcedbId) throws SQLException;

    void dropAllTable4ClickHouse(List<String> targetTableNameList) throws SQLException;

    List<OperateTargetSqlVO> getOperateTargetSqlList() throws SQLException;

    void execOperateSql(List<OperateTargetSqlVO> operateSqlList, String tablePrefix, String tableSuffix) throws Exception;

}