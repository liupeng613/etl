package com.xiaogj.dataxserver.service;


import com.xiaogj.dataxserver.vo.DBInfoVO;
import com.xiaogj.dataxserver.vo.DataTable;
import com.xiaogj.dataxserver.vo.DataTableStatus;
import com.xiaogj.dataxserver.vo.TableInfoVO;

import java.sql.SQLException;
import java.util.List;

public interface IDBInfoService {

	List<DataTable> getTargetTransfterTables() throws SQLException;

	List<String> getTargetTransfterTableColumns(String tableName) throws SQLException;

	List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException;

	List<DataTableStatus> getSourceTransfterTablesStatus() throws SQLException;

	long getSourceTransfterTableMigrationCount(String tableName, String whereClause) throws SQLException;

	long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException;

	List<DBInfoVO> getDBInfoList();

	List<TableInfoVO> getTableInfoList();

	void createTabel() throws SQLException;
}