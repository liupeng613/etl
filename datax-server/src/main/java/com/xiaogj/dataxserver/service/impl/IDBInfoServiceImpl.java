package com.xiaogj.dataxserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiaogj.dataxserver.config.JobJsonConfig;
import com.xiaogj.dataxserver.service.IDBInfoService;
import com.xiaogj.dataxserver.util.JdbcHelper;
import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.OperateTargetSqlVO;
import com.xiaogj.dataxserver.vo.SyncVO;
import com.xiaogj.dataxserver.vo.TableInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IDBInfoServiceImpl implements IDBInfoService {

    @Autowired
    private JdbcHelper jdbcHelper;

    @Autowired
    private JobJsonConfig jobJsonConfig;

    @Override
    public List<DBInfoVO> getDBInfoList() throws SQLException {
        StringBuffer sql = new StringBuffer("SELECT * FROM db_info");
        List<DBInfoVO> dbInfoVOList = new ArrayList<>();
        ResultSet rs = jdbcHelper.getDefaultConnection().prepareStatement(sql.toString()).executeQuery();
        while (rs.next()) {
            DBInfoVO temp = new DBInfoVO();
            temp.setId(rs.getInt("id"));
            temp.setName(rs.getString("db_name"));
            temp.setJdbcUrl(rs.getString("jdbc_url"));
            temp.setUsername(rs.getString("user_name"));
            temp.setPassword(rs.getString("pass_word"));
            temp.setBaseServiceTime(rs.getDate("base_service_time"));
            dbInfoVOList.add(temp);
        }
        jdbcHelper.closeDefaultConnection();
        log.debug("getDBInfoList result ==>> {}", JSON.toJSONString(dbInfoVOList));
        return dbInfoVOList;
    }

    @Override
    public void updateServiceTimeSync(List<TableInfoVO> tableInfoList) throws SQLException {
        log.debug("updateServiceTimeSync param ==>> {}", JSON.toJSONString(tableInfoList));
        String insertSql = ("INSERT INTO db_table_sync (db_id, table_id, service_time_sync) VALUES(?,?,?)");
        String updateSql = ("UPDATE db_table_sync SET service_time_sync = ? WHERE id = ?");
        Connection connection = jdbcHelper.getDefaultConnection();
        connection.setAutoCommit(false);
        PreparedStatement preparedStatement = null;
        for (TableInfoVO temp : tableInfoList) {
            SyncVO syncVO = temp.getSyncVO();
            if (syncVO.isInsert()) {
                preparedStatement = connection.prepareStatement(insertSql);
                preparedStatement.setInt(1, syncVO.getDbId());
                preparedStatement.setInt(2, syncVO.getTableId());
                preparedStatement.setDate(3, syncVO.getEndTime());

            } else {
                preparedStatement = connection.prepareStatement(updateSql);
                preparedStatement.setDate(1, syncVO.getEndTime());
                preparedStatement.setInt(2, syncVO.getId());
            }
            int executeResult = preparedStatement.executeUpdate();
            if (executeResult != 1) {
                throw new SQLException("updateServiceTimeSync fail: ");
            }
        }
        connection.commit();
        jdbcHelper.closeDefaultConnection();
    }

    @Override
    public DBInfoVO getDBInfoById(int id) throws SQLException {
        String sql = "SELECT * FROM db_info WHERE id = ?";
        DBInfoVO sourceDB = null;
        PreparedStatement preparedStatement = jdbcHelper.getDefaultConnection().prepareStatement(sql);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            sourceDB = new DBInfoVO();
            sourceDB.setId(rs.getInt("id"));
            sourceDB.setName(rs.getString("db_name"));
            sourceDB.setJdbcUrl(rs.getString("jdbc_url"));
            sourceDB.setUsername(rs.getString("user_name"));
            sourceDB.setPassword(rs.getString("pass_word"));
            sourceDB.setBaseServiceTime(rs.getDate("base_service_time"));
        }
        jdbcHelper.closeDefaultConnection();
        log.debug("getDBInfoById result ==>> {}", JSON.toJSONString(sourceDB));
        return sourceDB;
    }

    @Override
    public List<TableInfoVO> getTableInfoJoinSyncList(int sourcedbId) throws SQLException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append("a.`id` t_id, ");
        sql.append("a.`t_name` t_name, ");
        sql.append("a.`order` `order`, ");
        sql.append("a.`create_sql` create_sql, ");
        sql.append("a.`query_sql` query_sql, ");
        sql.append("a.`write_column` write_column, ");
        sql.append("b.`id` sync_id, ");
        sql.append("b.`db_id` db_id, ");
        sql.append("b.`service_time_sync` service_time_sync ");
        sql.append("FROM table_info a LEFT JOIN db_table_sync b ON a.`id` = b.`table_id`");
        sql.append("WHERE a.`is_valid` = 1 AND (b.`db_id` = ? OR b.`db_id` IS NULL)");
        List<TableInfoVO> tableInfoList = new ArrayList<>();
        PreparedStatement preparedStatement = jdbcHelper.getDefaultConnection().prepareStatement(sql.toString());
        preparedStatement.setInt(1, sourcedbId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            TableInfoVO temp = new TableInfoVO();
            temp.setId(rs.getInt("t_id"));
            temp.setName(rs.getString("t_name"));
            temp.setOrder(rs.getInt("order"));
            temp.setCreateSql(rs.getString("create_sql"));
            temp.setQuerySql(rs.getString("query_sql"));
            temp.setWriteColumn(rs.getString("write_column"));
            int syncId = rs.getInt("sync_id");
            Date serviceTimeSync = rs.getDate("service_time_sync");
            SyncVO syncVO = new SyncVO();
            syncVO.setId(syncId);
            syncVO.setServiceTimeSync(serviceTimeSync);
            syncVO.setDbId(sourcedbId);
            syncVO.setTableId(rs.getInt("t_id"));
            if (syncId == 0) {
                syncVO.setInsert(true);
            }
            temp.setSyncVO(syncVO);
            tableInfoList.add(temp);
        }
        jdbcHelper.closeDefaultConnection();
        log.debug("getTableInfoJoinSyncList result ==>> {}", JSON.toJSONString(tableInfoList));
        return tableInfoList;
    }

    @Override
    public void dropAllTable4ClickHouse(List<String> targetTableNameList) throws SQLException {
        log.debug("dropAllTable4ClickHouse param ==>> {}", JSON.toJSONString(targetTableNameList));
        String sqlTemp = "DROP TABLE %s";
        Connection onlineConnection = jdbcHelper.getOnlineConnection();
        Statement statement = onlineConnection.createStatement();
        for (String temp : targetTableNameList) {
            String sql = String.format(sqlTemp, temp);
            log.debug("Start drop temp table, sql => {}", sql);
            boolean execute = statement.execute(sql);
            //log.info("{} execute result",execute);
        }
        jdbcHelper.closeOnlineConnection();
    }

    @Override
    public List<OperateTargetSqlVO> getOperateTargetSqlList() throws SQLException {
        String sql = "SELECT * FROM operate_target_db WHERE is_valid = 1 ORDER BY `INDEX`";
        List<OperateTargetSqlVO> operateTargetSqlList = new ArrayList<>();
        ResultSet rs = jdbcHelper.getDefaultConnection().prepareStatement(sql).executeQuery();
        while (rs.next()) {
            OperateTargetSqlVO temp = new OperateTargetSqlVO();
            temp.setId(rs.getInt("id"));
            temp.setIndex(rs.getInt("index"));
            temp.setType(rs.getInt("type"));
            temp.setOperateSql(rs.getString("operate_sql"));
            operateTargetSqlList.add(temp);
        }
        jdbcHelper.closeDefaultConnection();
        log.debug("getOperateTargetSqlList result ==>> {}", JSON.toJSONString(operateTargetSqlList));
        return operateTargetSqlList;
    }

    @Override
    public void execOperateSql(List<OperateTargetSqlVO> operateSqlList, String tablePrefix, String tableSuffix) throws
            SQLException {
        {
            int optimizeTimes = jobJsonConfig.getOptimizeTimes();
            Connection onlineConnection = jdbcHelper.getOnlineConnection();
            for (OperateTargetSqlVO temp : operateSqlList) {
                String operateSql = temp.getOperateSql();
                if (StringUtils.startsWith(operateSql, "optimize")) {
                    log.info("operate index => {} , sql => {}", temp.getIndex(), operateSql);
                    for (int i = 0; i < optimizeTimes; i++) {
                        onlineConnection.prepareStatement(operateSql).execute();
                    }
                } else {
                    operateSql = temp.getOperateSql().replace("{target.table.name.prefix}", tablePrefix).replace("{target.table.name.suffix}", tableSuffix);
                    log.info("operate index => {} , sql => {}", temp.getIndex(), operateSql);
                    onlineConnection.prepareStatement(operateSql).execute();
                }
            }
            jdbcHelper.closeOnlineConnection();
        }
    }

    @Override
    public void createTabel4ClickHouse(List<String> createSqls) throws SQLException {
        Connection onlineConnection = jdbcHelper.getOnlineConnection();
        Statement statement = onlineConnection.createStatement();
        for (String createSql : createSqls) {
            log.info(createSql);
            statement.execute(createSql);
        }
        jdbcHelper.closeOnlineConnection();
    }
}
