package com.xiaogj.service;

import com.xiaogj.vo.DataTable;
import com.xiaogj.vo.DataTableStatus;

import java.sql.SQLException;
import java.util.List;

public interface DbViewer {

	public List<DataTable> getTargetTransfterTables() throws SQLException;

	public List<String> getTargetTransfterTableColumns(String tableName) throws SQLException;

	public List<String> getTargetTransfterTablePrimaryKey(String tableName) throws SQLException;

	public List<DataTableStatus> getSourceTransfterTablesStatus() throws SQLException;

	public long getSourceTransfterTableMigrationCount(String tableName, String whereClause) throws SQLException;

	public long getTargetTransfterTableMigrationFinishedCount(String tableName, String whereClause) throws SQLException;

}