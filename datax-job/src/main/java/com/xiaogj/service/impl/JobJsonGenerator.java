package com.xiaogj.service.impl;

import com.xiaogj.config.AppConfig;
import com.xiaogj.service.DbViewer;
import com.xiaogj.util.DataXJobFile;
import com.xiaogj.vo.DBInfoVO;
import com.xiaogj.vo.DataTable;
import com.xiaogj.vo.DataTableStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/**
 * 
 * Generate Datax jobs json files to Datax home/jobs folder
 * @author Jawf Can Li
 * @since 1.0 base on datax 3.0
 * 
 */
@Service
public class JobJsonGenerator {
	protected static final Log log = LogFactory.getLog(JobJsonGenerator.class);

	@Autowired
	private DbViewer viewer;

	@Autowired
	private DataXJobFile jobFile;

	@Autowired
	private AppConfig appConfig;

	public void generateMS2CK(List<DBInfoVO> dbInfoList) throws SQLException {

	}

	public void generateCK2CK(List<DBInfoVO> dbInfoList)
	{

	}

	public void generate() throws SQLException {
		List<DataTableStatus> sourceTables = viewer.getSourceTransfterTablesStatus();
		List<DataTable> targetTables = viewer.getTargetTransfterTables();

		if (sourceTables != null) {
			// int i = 0;
			for (DataTable ta : sourceTables) {
				String sourceTableName = ta.getName();
				if (sourceTableName == null || "".equals(sourceTableName)){
					throw new SQLException("Source Table is empty or not existed!");
				}
				String targetTableName = getTargetTableName(sourceTableName, targetTables);
				List<String> columns = viewer.getTargetTransfterTableColumns(targetTableName);
				List<String> pks = viewer.getTargetTransfterTablePrimaryKey(targetTableName);
				String whereClause = jobFile.getSourceGlobalTableWhereClause(columns);
				long migrationRecords = viewer.getSourceTransfterTableMigrationCount(sourceTableName, whereClause);
				String pk = null;
				if (pks != null && !pks.isEmpty()) {
					if (pks.size() == 1) {
						pk = pks.get(0);
					}
				}
				jobFile.generateJsonJobFile(sourceTableName, targetTableName, columns, pk, whereClause, migrationRecords);
				// i++;
				// if (i==30)
				// break;// remove this line*/
			}
		}
	}

	private String getTargetTableName(String sourceTableName, List<DataTable> targetTables) throws SQLException {
		String result = null; 
		if (sourceTableName!=null && targetTables!=null){
			for (DataTable t:targetTables){
				if (sourceTableName.equalsIgnoreCase(t.getName())){
					result = t.getName();
					break;
				}
			}
		}
		if (result == null){
			String errorMsg = "Target Table for "+sourceTableName+" is empty or not existed!";
			log.error(errorMsg);
			throw new SQLException(errorMsg);
		}
		return result;
	}

}
