package org.ots123it.open.sdubotr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.OperationNotSupportedException;
import javax.swing.JOptionPane;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.ots123it.jhlper.CommonHelper;
import org.ots123it.jhlper.DBHelper;
import org.ots123it.jhlper.ExceptionHelper;
import org.ots123it.jhlper.IOHelper;
import org.ots123it.jhlper.UserInterfaceHelper;
import org.ots123it.jhlper.UserInterfaceHelper.MsgBoxButtons;
import org.ots123it.jhlper.UserInterfaceHelper.confirmingBoxButtons;
import org.ots123it.open.sdubotr.Global.GlobalDatabases;


import static org.ots123it.jhlper.DBHelper.*;
import static org.ots123it.open.sdubotr.Global.AppName;
import static org.ots123it.open.sdubotr.Global.FriendlyName;
import static org.ots123it.open.sdubotr.Global.RestoreData;
import static org.ots123it.open.sdubotr.Global.masterQQ;
import static org.ots123it.open.sdubotr.Global.GlobalDatabases.*;

public class Initialization
{

	public static int main(CoolQ CQ,String appDirectory)
	 {
		  try {
				return NormalStart(CQ, appDirectory);
		  } catch (Exception e) {
				e.printStackTrace();
				return 1;
		  }
	 }

	public static int NormalStart(CoolQ CQ,String appDirectory) throws FileNotFoundException, NullPointerException,SQLException,OperationNotSupportedException
	{
		CQ.logInfo(Global.AppName, "获取应用数据目录成功:\n" + "设置目录:" + appDirectory);
        if(!(new File(appDirectory + "/firstopen.stat")).exists()) //若无firstopen.stat文件（即首次打开）
        {
        	CQ.logInfo(Global.AppName, "检测到无firstopen.stat文件，判断为首次启动，正在初始化");
        	Initialize(CQ,appDirectory); //调用初始化方法
         // [start] 初始化数据库DBHelper实例并打开数据库(同时检查数据库完整性)
 		  dbsystem_syssettings = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/system/syssettings.db",
 					 SQLite, "SQLite");
 		  dbgroup_custom = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/custom.db", SQLite , "SQLite");
 		  dbgroup_list = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/list.db", SQLite, "SQLite");
 		  dbgroup_ranking_speaking = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/ranking/speaking.db",SQLite,"SQLite");
 		  dbsystem_syssettings.Open();
 		  dbgroup_custom.Open();
 		  dbgroup_list.Open();
 		  dbgroup_ranking_speaking.Open();
 		  checkDbIntegrity(CQ, appDirectory);
 		  // [end]
        	ResultSet sysSettingsSet = GlobalDatabases.dbsystem_syssettings.executeQuery("SELECT Value FROM Common WHERE Name='MasterQQ'");
        	Global.masterQQ = Long.parseLong(sysSettingsSet.getString("Value"));
        	CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" +
        				"这是一条测试消息,如果接收到了该消息代表已初始化完毕，可以正常使用了\n");
        	return 0;
        } else { //存在firstopen.stat文件（非首次打开） 
      		// [start] 初始化数据库DBHelper实例并打开数据库(同时检查数据库完整性)
    		  dbsystem_syssettings = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/system/syssettings.db",
   					 SQLite, "SQLite");
   		  dbgroup_custom = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/custom.db", SQLite , "SQLite");
   		  dbgroup_list = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/list.db", SQLite, "SQLite");
   		  dbgroup_ranking_speaking = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/group/ranking/speaking.db",SQLite,"SQLite");
   		  dbsystem_syssettings.Open();
   		  dbgroup_custom.Open();
   		  dbgroup_list.Open();
   		  dbgroup_ranking_speaking.Open();
   		  checkDbIntegrity(CQ, appDirectory);
   		  // [end]
           	ResultSet sysSettingsSet = GlobalDatabases.dbsystem_syssettings.executeQuery("SELECT Value FROM Common WHERE Name='MasterQQ'");
           	Global.masterQQ = Long.parseLong(sysSettingsSet.getString("Value"));
        	// [start] 机器人文件夹检测
      		if (!(new File(appDirectory + "/data/pics")).exists()) {
      			CQ.logFatal(Global.AppName, "读取图片信息文件数据失败\n请删除数据目录下的firstopen.stat然后重启酷Q以初始化图片信息文件");
            	return 1;
			} 
        	 if (new File(appDirectory + "/running.stat").exists()) { //如果“正在运行中”文件已存在
        		 CQ.logInfo(AppName, "上次应用的关闭是意外的。");
        		File autoSaveData = new File(appDirectory + "/temp/autosave.zip"); //新建自动备份数据文件实例
        		if (autoSaveData.exists()) { //如果自动备份数据文件存在
        			String date = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
           				 .format(new Date
           						 (autoSaveData.lastModified())); //获取自动保存文件的上一次修改日期
           		 int result = UserInterfaceHelper.confirmingBox("检测到应用未正常关闭 - " + AppName, 
    						"检测到应用未正常关闭，这可能是操作系统突然断电所导致的。\n" + 
    								"应用未正常关闭可能会造成本应用的数据丢失或错乱。" + 
    								"是否使用于" + date + "自动保存的数据目录备份文件恢复数据?",confirmingBoxButtons.Question);
   				if (result == 1) { //是
   					UserInterfaceHelper.MsgBox("准备恢复数据 - " + AppName, "准备恢复数据:\n" + 
   							"数据备份日期:" + date + "\n" + 
   							"请将机器人设置成离线状态后单击”确定“按钮", MsgBoxButtons.Info);
   					RestoreData(CQ); //执行恢复操作
   					try {
   						new File(appDirectory + "/running.stat").createNewFile();
   						CQ.logDebug(AppName, "“运行中”标志文件成功创建");
   					} catch (IOException e) {
   						CQ.logFatal(AppName, "应用启用时出现致命异常:无法创建“运行中”标志文件(running.stat)(java.io.IOException)");
   						return 1;
   					}
   					Start.timer.schedule(new autoSave(CQ), 300000L, 300000L); //启动5分钟自动备份
   					CQ.logInfo(AppName, "自动备份线程已启动");
   					return 0;
   				} else {
   					try {
   						new File(appDirectory + "/running.stat").createNewFile();
   						CQ.logDebug(AppName, "“运行中”标志文件成功创建");
   					} catch (IOException e) {
   						CQ.logFatal(AppName, "应用启用时出现致命异常:无法创建“运行中”标志文件(running.stat)(java.io.IOException)");
   						return 1;
   					}
   					Start.timer.schedule(new autoSave(CQ), 0L, 300000L); //启动5分钟自动备份
   					CQ.logInfo(AppName, "自动备份线程已启动");
   					return 0;
				}
				} else { //如果自动备份数据文件不存在
					UserInterfaceHelper.MsgBox("检测到应用未正常关闭 - " + AppName, 
    						"检测到应用未正常关闭，这可能是操作系统突然断电所导致的。\n" + 
    								"应用未正常关闭可能会造成本应用的数据丢失或错乱。" + 
    								"单击”确定“继续启动应用",MsgBoxButtons.Warning);
					try {
						new File(appDirectory + "/running.stat").createNewFile();
						CQ.logDebug(AppName, "“运行中”标志文件成功创建");
					} catch (IOException e) {
						CQ.logFatal(AppName, "应用启用时出现致命异常:无法创建“运行中”标志文件(running.stat)(java.io.IOException)");
						return 1;
					}
					Start.timer.schedule(new autoSave(CQ), 0L, 300000L); //启动5分钟自动备份
					CQ.logInfo(AppName, "自动备份线程已启动");
					return 0;
				}
			} else { //如果是正常启动
				try {
					new File(appDirectory + "/running.stat").createNewFile();
					CQ.logDebug(AppName, "“运行中”标志文件成功创建");
				} catch (IOException e) {
					CQ.logFatal(AppName, "应用启用时出现致命异常:无法创建“运行中”标志文件(running.stat)(java.io.IOException)");
					return 1;
				}
				Start.timer.schedule(new autoSave(CQ), 0L, 300000L); //启动5分钟自动备份
				CQ.logInfo(AppName, "自动备份线程已启动");
				return 0;
			}
        }
	}
	
	
	public static void Initialize(CoolQ CQ,String appDirectory)
	{
		try {
			// 初始化准备:删除数据目录所有文件夹
			File initReady1 = new File(Start.appDirectory + "/group");
			if (initReady1.exists())
			{
				IOHelper.DeleteAllFiles(initReady1);
				initReady1.delete();
			}
			File initReady2 = new File(Start.appDirectory + "/private");
			if (initReady2.exists()) {
				IOHelper.DeleteAllFiles(initReady2);
				initReady2.delete();
			}
			File initReady3 = new File(Start.appDirectory + "/data");
			if (initReady3.exists()) {
				IOHelper.DeleteAllFiles(initReady3);
				initReady3.delete();
			}
			File initReady4 = new File(Start.appDirectory + "/system");
			if (initReady4.exists()) {
				IOHelper.DeleteAllFiles(initReady4);
				initReady4.delete();
			}
			File initReady5 = new File(Start.appDirectory + "/protect");
			if (initReady5.exists()) {
				IOHelper.DeleteAllFiles(initReady5);
				initReady5.delete();
			}
			String[] files = {"/system","/temp","/data", "/group","/private","/protect",
					"/data/pic","/data/pics/menu",
					"/group/ranking",
					"/protect/group","/protect/group/abuse","/firstopen.stat"};
			for (String f:files) {
				File init = new File(Start.appDirectory + f);
				if (f.contains(".")) {
					init.createNewFile();
					CQ.logDebug(Global.AppName , "初始化:建立文件:" + Start.appDirectory + f);
				}else {
					init.mkdir();
					CQ.logDebug(Global.AppName,  "初始化:创建路径:" + Start.appDirectory+ f);
				}
				System.gc();
			}
			// [start] 初始化数据库文件
			//提取数据库文件
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_list.db", new File(Global.appDirectory + "/group/list.db"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_list.db_shm", new File(Global.appDirectory + "/group/list.db_shm"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_list.db_wal", new File(Global.appDirectory + "/group/list.db_wal"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_custom.db", new File(Global.appDirectory + "/group/custom.db"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_custom.db_shm", new File(Global.appDirectory + "/group/custom.db_shm"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/group_custom.db_wal", new File(Global.appDirectory + "/group/custom.db_wal"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/ranking/group_ranking_speaking.db", new File(Global.appDirectory + "/group/ranking/speaking.db"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/ranking/group_ranking_speaking.db_shm", new File(Global.appDirectory + "/group/ranking/speaking.db_shm"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/group/ranking/group_ranking_speaking.db_wal", new File(Global.appDirectory + "/group/ranking/speaking.db_wal"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/system/system_syssettings.db", new File(Global.appDirectory + "/system/syssettings.db"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/system/system_syssettings.db_shm", new File(Global.appDirectory + "/system/syssettings.db_shm"));
					IOHelper.extractFileFromJar(Global.class, 
							  "/data/databases/system/system_syssettings.db_wal", new File(Global.appDirectory + "/system/syssettings.db_wal"));
					
			// [end]
			// [start] 初始化图片文件数据
			ArrayList<URI> picsUrls = new ArrayList<URI>(); //定义图片文件URI集合
			ArrayList<File> picsFiles = new ArrayList<File>(); //定义图片文件集合
			// 获取功能菜单图片URL
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/main_menu.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/1.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/2.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/3.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/4.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/5.png").toURI());
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/menu/o.png").toURI());
			// 获取关于图片URL
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/about.png").toURI());
			// 获取黑名单列表图片URL
			picsUrls.add(Global.class.getClass().getResource("/data/pictures/GroupMsgPics/blist.png").toURI());
			// 遍历图片URL集合，转换添加到图片文件集合
			for (URI singleURL : picsUrls) {
				picsFiles.add(new File(singleURL));
			}
			// 遍历图片文件集合，提取图片文件
			for (File singleFile : picsFiles) {
				if (singleFile.getName().toLowerCase().contains("menu")) { //如果是功能菜单图片
					IOHelper.copyFile(singleFile.toString(), Global.appDirectory + "/data/pics/menu/" + singleFile.getName()); //提取图片文件
					CQ.logDebug(AppName, "初始化:抽取:" +Global.appDirectory + "/data/pics/menu/" + singleFile.getName());
				} else { //如果不是功能菜单图片
					IOHelper.copyFile(singleFile.toString(), Global.appDirectory + "/data/pics/" + singleFile.getName()); //提取图片文件
					CQ.logDebug(AppName, "初始化:抽取:" +Global.appDirectory + "/data/pics/" + singleFile.getName());
				}
			}
			// [end]
			File announcement = new File(Global.appDirectory + "/data/pics/ancment.txt"); //定义公告文件
			if (!announcement.exists()) { //如果公告文件不存在
				announcement.createNewFile(); //创建公告文件
				CQ.logDebug(AppName, "初始化:建立文件:" + announcement.toString());
			}
			while (true) {
				 String inputMasterQQ = JOptionPane.showInputDialog(null, "请设置bot主人的QQ号,设置后不可更改", AppName, JOptionPane.INFORMATION_MESSAGE);
				 if ((inputMasterQQ != null) && (!inputMasterQQ.isEmpty()) && (!CommonHelper.isInteger(inputMasterQQ))) { //如果输入的是有效数字
					 DBHelper db_tempSystem = new DBHelper(appDirectory.replace("\\", "/").toLowerCase() + "/system/syssettings.db",
		   					 SQLite, "SQLite");
					 db_tempSystem.executeNonQuerySync("INSERT OR REPLACE INTO Common ('Name','Value') VALUES ('MasterQQ','" + inputMasterQQ + "');");
					 /* 执行插入或更新语句
					  * INSERT OR REPLACE INTO Common ('Name','Value') VALUES ('MasterQQ','[inputMasterQQ]');
					  */
					 break;
				 }
			}
			CQ.logInfo(Global.AppName, "初始化完成");
		} catch (IOException e) {
			CQ.logFatal(Global.AppName, "初始化时出现严重错误,详细信息:\n" + 
					ExceptionHelper.getStackTrace(e));
		} catch (URISyntaxException e) {
			CQ.logFatal(Global.AppName, "初始化时出现严重错误,详细信息:\n" + 
					ExceptionHelper.getStackTrace(e));
		} finally {
		}
		return; //返回
	}

	/**
	 * 检查数据库完整性
	 * @param CQ 酷Q操作对象
	 * @param appDirectory bot的数据库存储目录
	 */
	public static void checkDbIntegrity(CoolQ CQ,String appDirectory)
	{
		try {
		  ArrayList<DBHelper> dbsArrayList = new ArrayList<DBHelper>(); //定义一个DBHelper数组集合
		  ArrayList<URI> damagedDBUris = new ArrayList<URI>(); //定义损坏的数据库的文件路径集合
		  boolean isFailed = false; //定义记录是否检查出完整性存在问题
		  // [start] 添加已有的所有DBHelper
		  dbsArrayList.add(dbgroup_ranking_speaking);
		  dbsArrayList.add(dbsystem_syssettings);
		  dbsArrayList.add(dbgroup_custom);
		  dbsArrayList.add(dbgroup_list);
		  // [end]
		  for (DBHelper dbHelper : dbsArrayList) { //遍历DBHelper
				ResultSet checkResultSet = dbHelper.executeQuery("PRAGMA integrity_check;"); //执行完整性检查语句(PRAGMA integrity_check;)并获得检查结果
				String checkResult = checkResultSet.getString("integrity_check").toLowerCase();
				if (checkResult.equals("ok")) { //如果检查通过
					 continue; //遍历到下一个DBHelper
				} else { //如果检查不通过
					 isFailed = true; //检查失败
					 damagedDBUris.add(dbHelper.getDatabasePath()); //添加数据库文件路径到damangedDBUris集合中
				}
		  }
		  if (isFailed) {
				StringBuilder fatalNoticeBuilder = new StringBuilder()
						  .append("数据库完整性校验失败!\n")
						  .append("部分数据库文件可能已损坏,请备份所有数据库文件(.db,.db-shm和.db-wal文件),删除firstopen.stat并重启酷Q以重新初始化bot.\n")
						  .append("注意:重新初始化bot将会丢失所有数据!\n")
						  .append("校验失败的数据库列表:\n");
				for (URI uri : damagedDBUris) {
					 fatalNoticeBuilder.append(uri.getPath()).append("\n");
				}
				CQ.logFatal(AppName, fatalNoticeBuilder.toString());
		  } else {
				return;
		  }
		} catch (Exception e) {
		  // TODO: handle exception
	 }
	}
}
