package org.edu_sharing.repository.update;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.edu_sharing.alfrescocontext.gate.AlfAppContextGate;
import org.edu_sharing.repository.client.tools.CCConstants;
import org.edu_sharing.repository.server.RepoFactory;
import org.edu_sharing.service.suggest.ConnectionDBAlfresco;
import org.springframework.context.ApplicationContext;

public class SQLUpdater extends UpdateAbstract {
	
	Logger logger = Logger.getLogger(SQLUpdater.class);
	
	public static final String ID = "SQLUpdater";
	
	public static final String description = "SQLUpdater to run sql scripts defined edu-sharing.proprties " + CCConstants.EDU_SHARING_PROPERTIES_PROPERTY_INITIAL_DBSCRIPTS;
	
	ApplicationContext applicationContext = AlfAppContextGate.getApplicationContext();
	SqlSessionFactory sqlSessionFactoryBean = (SqlSessionFactory)applicationContext.getBean("repoSqlSessionFactory");
	
	Protocol protocol = new Protocol();
	
	@Override
	public void execute() {
		execute(false);
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description;
	}
	
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return ID;
	}
	
	@Override
	public void test() {
		execute(true);
	}
	
	private void execute(boolean test){
		try {
		String sqlScripts = RepoFactory.getEdusharingProperty(CCConstants.EDU_SHARING_PROPERTIES_PROPERTY_INITIAL_DBSCRIPTS);
		if(sqlScripts != null && sqlScripts.trim().length() > 0) {
			String[] scripts = sqlScripts.split(",");
			for(String script : scripts) {
				
				String sysProtocolEntry = ID + "_" + script;
				HashMap<String, Object> entry = protocol.getSysUpdateEntry(sysProtocolEntry);
				if(entry == null && !test) {
					logger.info("running sql script " + script);
					runSQLScript(script);
					protocol.writeSysUpdateEntry(sysProtocolEntry);
				}
			}
		}else {
			logger.info("no scripts to execute defined");
		}
		}catch(Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void runSQLScript(String script) throws Exception{
		
		ConnectionDBAlfresco dbAlf = new ConnectionDBAlfresco();
		Connection connection = dbAlf.getConnection();
		ScriptRunner scriptRunner = new ScriptRunner(connection);
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(script);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setSendFullScript(true);
		//scriptRunner.setEscapeProcessing(true);
		scriptRunner.setStopOnError(true);
		scriptRunner.setErrorLogWriter(new PrintWriter(System.out));
		scriptRunner.runScript(new InputStreamReader(is,"UTF8"));
	
		dbAlf.cleanUp(connection);
	}

}
