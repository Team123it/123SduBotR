/**
 * 
 */
package org.ots123it.open.sdubotr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.ots123it.jhlper.DBHelper;
import org.ots123it.jhlper.ExceptionHelper;
import org.ots123it.jhlper.IOHelper;
import org.ots123it.jhlper.UserInterfaceHelper;
import org.ots123it.jhlper.ZipFileHelper;
import org.ots123it.jhlper.util.LinkedHashTable;
import org.ots123it.jhlper.UserInterfaceHelper.MsgBoxButtons;

/**
 * 
 * 123 SduBotR 公用常量数据存储类<br>
 * 如需添加公用静态(static)常量请在此处添加，使用时先<br>
 * <pre class="code">import static org.ots123it.open.sdubotr.Global.*;</pre> 
 * @author 御坂12456
 */
@SuppressWarnings("deprecation")
public abstract class Global extends JcqAppAbstract
{
	// [start] 常变量集合
	/**
	 * 机器人主人QQ号，如需自定义机器人请改成自己所需要的
	 */
	public static long masterQQ;
	
	/**
	 * 腾讯系统QQ号集合(通常用于提醒主人私聊消息时忽略掉系统QQ号时)
	 */
	public final static Long[] tencentSysAccounts = {2062433139L};
	
	/**
	 * 123 SduBotR 程序名（若自定义机器人请务必更改此名称）
	 */
	public final static String AppName = "123 SduBotR";
	/**
	 * 版本
	 */
	public final static String Version = "0.5.1";
	/**
	 * 发布版本编号（从1开始）
	 */
	public final static String AppVersionNumber = "Build 33";
	/**
	 * 123 SduBotR 友好名称(实际运行时在QQ中显示)<br>
	 * 效果:【123 SduBotR {Version}】
	 */
	public final static String FriendlyName = "【" + AppName + " " + Version + "】";
	/**
	 * 123 SduBotR 数据存放路径
	 */
	public static String appDirectory = Start.appDirectory;
	/**
	 * 防滥用功能缓冲秒数
	 */
	public final static int abuseCheckSeconds = 5;
	/**
	 * 机器人侮辱检测所使用的脏话列表
	 * @since 0.2.5
	 */
	public final static String[] bannedObscenities = {"垃圾","傻逼","智障","脑残","贱","操你","你妈炸了","滚","fuck","shit","bullshit","laji","nmsl"};
	/**
	 * 机器人主人(私聊)功能菜单
	 */
	public final static String masterMenuStr = "123 SduBotR 主人(私聊)功能菜单\n" +
			"详细内容:https://github.com/Misaka12456/123SduBotR/blob/master/README_master.md\n" + 
			"以下[]代表必填参数,{}代表可填参数\n" + 
			"1.发送私聊消息\n" + 
			"!spm [QQ号] [消息内容]\n" + 
			"2.发送群聊消息\n" + 
			"!sgm [QQ号] [消息内容]\n" + 
			"3.退出指定群(慎用)\n" + 
			"!eg [群号]\n" + 
			"4.查看私聊功能菜单\n" + 
			"!pm\n" + 
			"5.机器人受邀入群确认\n" + 
			"!cig [请求标识] [agree/refuse\n]" + 
			"6.警告违反使用协议的群(功能M-4)\n" + 
			"!warn [群号]\n" + 
			"7.查看群聊被警告/永久屏蔽(拉黑)状态\n" + 
			"!gstat [群号]\n";
//	/** 123 SduBotR的完整文字功能菜单(已弃用)
//	 *
//	 */
//	public final static String menuStr = "123 SduBotR 功能菜单\n" + 
//			"详细内容:https://github.com/Misaka12456/123SduBotR/blob/master/README.md\n" + 
//			"以下[]代表必填参数,{}代表可填参数\n" + 
//			"1.群管理核心功能\n" + 
//			"1-1.禁言\n" + 
//			"!mt [@/QQ号] [时长(单位:分钟)]\n" + 
//			"1-2.解禁\n" + 
//			"!um [@/QQ号]\n" + 
//			"1-3.踢人\n" + 
//			"!k [@/QQ号]\n" + 
//			"1-4.永踢人（慎用）\n" + 
//			"!fk [@/QQ号]\n" + 
//			"1-5.群黑名单\n" + 
//			"1-5-1~2.启动/关闭黑名单\n" + 
//			"!blist [start/stop]\n" + 
//			"1-5-3~4.添加/删除黑名单成员\n" +
//			"!blist [add/del] [成员1QQ号/at] {成员nQQ号/at...}\n" + 
//			"1-5-5.查看本群黑名单列表\n" + 
//			"!blist show\n" + 
//			"1-5-6.切换黑名单成员入群拒绝提醒状态\n" +
//			"!blist cnp\n" +
//			"1-5-7.切换退群加黑开启状态\n" +
//			"!blist eab\n" + 
//			"2.群管理辅助功能(详见Github)\n" +
//			"3.群增强功能\n" +
//			"3-1.查看群成员日发言排行榜(Top10)\n" +
//			"!rk\n" +
//			"4.实用功能\n" +
//			"4-1.查看新冠肺炎(SARS-Cov-2)疫情实时数据\n" +
//			"!cov {省份名}\n" +
//			"4-2.Bilibili相关功能\n" +
//			"4-2-1.Bilibili实时粉丝数据\n" +
//			"!bf [UID]\n" +
//			"4-2-2.BV号与AV号互转\n" +
//			"!bavid [视频链接]\n" +
//			"O.其它功能\n" + 
//			"O-1.关于\n" + 
//			"!about\n" + 
//			"O-2.功能菜单\n" + 
//			"!m\n" + 
//			"O-3.解除防滥用\n" + 
//			"!uab {验证码}\n" + 
//			"O-4.反馈\n" +
//			"!rpt [具体内容]\n" + 
//			"O-5.图片消息(Beta)\n" + 
//			"O-5-1~2.启禁用图片消息模式\n" + 
//			"!imm [start/stop]\n" + 
//			"O-5-3.查看图片消息模式启用状态\n" + 
//			"!imm stat";
	// [end]

	/**
	 * 恢复自动备份的数据
	 * @param CQ
	 */
	@SuppressWarnings("rawtypes")
public static void RestoreData(CoolQ CQ)
	{
		try {
			CQ.logInfo(AppName, "准备恢复数据");
			String temp = System.getProperty("java.io.tmpdir"); // 获取临时目录
			File tmpFolder = new File(temp + "/123 SduBotR/autosave"); // 新建临时目录实例
			if (tmpFolder.exists()) { // 如果临时目录已存在
				IOHelper.DeleteAllFiles(tmpFolder); // 删除临时目录里的所有文件
				tmpFolder.delete(); // 删除临时目录
			}
			tmpFolder.mkdirs(); // 创建临时目录
			File restoreSaveFile = new File(temp + "/123 SduBotR/autosave/autosave.zip");
			File lastAutoSaveFile = new File(Global.appDirectory + "/temp/autosave.zip");
			if (lastAutoSaveFile.exists()) { // 如果上一个自动保存的文件存在
				lastAutoSaveFile.renameTo(restoreSaveFile); // 移动到临时目录
				IOHelper.DeleteAllFiles(new File(appDirectory)); // 删除数据目录中所有文件
				ZipFileHelper.extractZipFile(restoreSaveFile.getAbsolutePath(), Global.appDirectory); //解压缩数据文件到指定目录
				String fromZipFileName = restoreSaveFile.getAbsolutePath();
				String toUnzipFolder = Global.appDirectory;
				 ZipFile zfile=new ZipFile(fromZipFileName);  
			        Enumeration zList=zfile.entries();  
			        ZipEntry ze=null;  
			        byte[] buf=new byte[1024];  
			        while(zList.hasMoreElements()){  
			            ze=(ZipEntry)zList.nextElement();         
			            if(ze.isDirectory()){  
			                File f=new File(toUnzipFolder+ze.getName());  
			                f.mkdir();  
			                continue;  
			            } else if (ze.getName().contains(".jar")) { //如果是库文件
							continue;
						}  else if (ze.getName().equals("lib")) { //如果是库目录
							continue;
						}
			            OutputStream os=new BufferedOutputStream(new FileOutputStream(org.ots123it.jhlper.ZipFileHelper.getRealFileName(toUnzipFolder, ze.getName())));  
			            InputStream is=new BufferedInputStream(zfile.getInputStream(ze));  
			            int readLen=0;  
			            while ((readLen=is.read(buf, 0, 1024))!=-1) {  
			                os.write(buf, 0, readLen);  
			            }  
			            is.close();  
			            os.close();   
			        }  
			        zfile.close();  
				CQ.logInfoSuccess(AppName, "数据恢复完成");
				UserInterfaceHelper.MsgBox(AppName, "数据恢复完成，请切换机器人为在线状态后单击确定", MsgBoxButtons.Info);
				return; //返回
			} else {
				UserInterfaceHelper.MsgBox(AppName, "错误:找不到备份的数据文件", MsgBoxButtons.Error);
			}
		} catch (Exception e) {
			UserInterfaceHelper.MsgBox(AppName, "错误:未知\n" + 
					e.getMessage() + "\n" + 
					ExceptionHelper.getStackTrace(e),MsgBoxButtons.Error);
		}
		
	}
	
	/**
	 * 返回指定群组的群名
	 * @param GroupId 指定群组的群号
	 * @return 成功返回群名,失败返回null
	 * @author 御坂12456
	 */
	public static String getGroupName(CoolQ CQ,long GroupId)
	{
		try {
			java.util.List<Group> groupList = CQ.getGroupList();
			for (Group group : groupList) {
				if (group.getId() == GroupId)
				{
					return group.getName();
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 判断是否为指定群的管理组成员（包括管理员和群主）
	 * @param CQ CQ实例
	 * @param checkGroupId 要判断的群号
	 * @param checkQQId 要判断的QQ号
	 * @return 成功返回判断结果，失败返回false
	 */
	public static boolean isGroupAdmin(CoolQ CQ,long checkGroupId,long checkQQId)
	{
		try {
			switch (CQ.getGroupMemberInfo(checkGroupId, checkQQId).getAuthority().value()) //获取成员权限(1/成员,2/管理员,3/群主)
			{
			case 2: //是管理员
			case 3: //是群主
				return true;
			default: //不是管理组成员
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断是否为指定群的管理组成员（包括管理员和群主）
	 * @param CQ CQ实例
	 * @param checkGroupId 要判断的群号
	 * @param checkQQId 要判断的QQ号
	 * @param isLeader 仅判断是否为群主
	 * @return 成功返回判断结果，失败返回false
	 */
	public static boolean isGroupAdmin(CoolQ CQ,long checkGroupId,long checkQQId,boolean isLeader)
	{
		try {
			switch (CQ.getGroupMemberInfo(checkGroupId, checkQQId).getAuthority().value()) //获取成员权限(1/成员,2/管理员,3/群主)
			{
			case 2: //是管理员
				if (isLeader = true) { //如果仅判断是否为群主
					return false;
				} else { //否则
					return true;
				}
			case 3: //是群主
				return true;
			default: //不是管理组成员
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 判断机器人登录QQ号是否为指定群的管理组成员
	 * @param CQ CQ实例
	 * @param checkGroupId 要判断的群号
	 * @return 成功返回判断结果，失败返回false
	 */
	public static boolean isGroupAdmin(CoolQ CQ,long checkGroupId)
	{
		try {
			switch (CQ.getGroupMemberInfo(checkGroupId, CQ.getLoginQQ()).getAuthority().value()) //获取权限(1/成员,2/管理员,3/群主)
			{
			case 2: //是管理员
			case 3: //是群主
				return true;
			default: //不是管理组成员
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 判断机器人登录QQ号是否为指定群的管理组成员
	 * @param CQ CQ实例
	 * @param checkGroupId 要判断的群号
	 * @param isLeader 仅判断是否为群主
	 * @return 成功返回判断结果，失败返回false
	 */
	public static boolean isGroupAdmin(CoolQ CQ,long checkGroupId,boolean isLeader)
	{
		try {
			switch (CQ.getGroupMemberInfo(checkGroupId, CQ.getLoginQQ()).getAuthority().value()) //获取权限(1/成员,2/管理员,3/群主)
			{
			case 2: //是管理员
				if (isLeader = true) { //如果仅判断是否为群主
					return false;
				} else { //否则
					return true;
				}
			case 3: //是群主
				return true;
			default: //不是管理组成员
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 获取at的CQ码中的对应QQ号
	 * @param CQCode
	 * @author 御坂12456
	 * @return 成功返回QQ号，成功（全体成员）返回-1000，失败或格式不正确返回-1
	 */
	public static long getCQAt(String CQCode)
	{
		try {
			if (CQCode.startsWith("[CQ:at,qq=")) { //如果CQCode变量是以"[CQ:at,qq="开始的
				String atString = CQCode.substring(10, CQCode.length() - 1); //获取QQ号字符串
				Pattern numbers = Pattern.compile("[0-9]*");
				if (numbers.matcher(atString).matches()) { //如果QQ号字符串是数字
					long atQQ = Long.parseLong(atString); //转换成长整数
					return atQQ;
				} else { //否则
					if (atString.equals("all")) //如果QQ号字符串是"all"（全体成员）
					{
						return -1000;
					} else {
						throw new Exception("QQ号不是数字");
					}
				}
			} else {
				throw new Exception("CQ码格式不正确");
			}
		} catch (Exception e) {
			return -1;
		}
	}
	/**
	 * 获取图片信息中需要显示的随机公告信息
	 * @return 成功返回随机公告信息，公告为空返回"[公告暂时为空]”，失败返回"[公告读取失败]"
	 */
	public static String getRandAncment()
	{
		try {
			String fullAncMentStr = IOHelper.ReadToEnd(Global.appDirectory + "/data/pics/ancment.txt"); //读取公告文件全部内容
			if ((fullAncMentStr != null) && (!fullAncMentStr.equals(""))) { //如果公告内容不是null且公告文件不为空
				String[] ancMents = IOHelper.ReadAllLines(Global.appDirectory + "/data/pics/ancment.txt"); //读取公告数组
				Random r = new Random(); //新建随机数实例
				String randAncMentStr = ancMents[r.nextInt(ancMents.length)]; //随机获取一个公告内容
				return randAncMentStr; //返回随机获得的公告内容
			} else {
				return "[公告暂时为空]";
			}
		} catch (Exception e) {
			return "[公告读取失败]";
		  }
	 }

	 /**
	  * 判断指定群是否被加入机器人黑名单<br>
	  * 以下两种<font color="blue"><b>之一</b></font>若判断为真则返回真:<br>
	  * <ol>
	  * <li>被永久加黑(加入AllGBan.txt)</li>
	  * <li>被暂时加黑(被警告(加入AllGWarn.txt)且并未脱离限制期)</li>
	  * </ol>
	  * 
	  * @param groupId 要判断的群号
	  * @return 成功返回判断结果，失败返回false(同时在控制台打印出异常堆栈)
	  */
	 public static boolean isGroupBanned(long groupId)
	 {
		  try {
				ArrayList<Long> allGBanList = new ArrayList<Long>(); // 定义allGBanList集合
				LinkedHashTable<String, Object> allGWarnTable = new LinkedHashTable<String, Object>(); // 定义allGWarnTable表格集合
				ResultSet allGBanSet = GlobalDatabases.dbgroup_list
						  .executeQuery("SELECT * FROM AllGBanWarn WHERE Status=-1"); // 获取所有永久加黑的群聊
				ResultSet allGWarnSet = GlobalDatabases.dbgroup_list
						  .executeQuery("SELECT * FROM AllGBanWarn WHERE Status>-1"); // 获取所有暂时加黑(警告)的群聊
				while (allGBanSet.next()) { // 遍历永久加黑群聊列表
					 allGBanList.add(allGBanSet.getLong("GroupId")); // 添加到allGBanList中
				}
				allGWarnTable.addColomn("GroupId"); // 添加列"群号"(GroupId)到allGWarnTable表格中
				allGWarnTable.addColomn("Status"); // 添加列"当前状态"(Status,0-3)到allGWarnTable表格中
				allGWarnTable.addColomn("ExpireDate"); // 添加列"警告解除日期"(ExpireDate)到allGWarnTable表格中
				while (allGWarnSet.next()) { // 遍历暂时加黑(警告)群聊列表
					 long currentGroupId = allGWarnSet.getLong("GroupId"); // 获取当前群聊群号
					 int currentStatus = allGWarnSet.getInt("Status"); // 获取当前群聊状态
					 Date currentExpireDate = allGWarnSet.getDate("WarnEndTime"); // 获取当前群聊警告解除日期
					 allGWarnTable.addRow(currentGroupId, currentStatus, currentExpireDate); // 添加新行到allGWarnTable表格中
				}
				if (allGBanList.size() > 0) { // 如果永久加黑群聊列表不为空
					 for (Long allBanGroup : allGBanList) { // 遍历永久加黑群聊列表
						  if (allBanGroup == groupId) { // 如果当前群聊为永久加黑群聊
								return true; // 返回真
						  }
					 }
				}
				if (allGWarnTable.getRowCounts() > 0) { // 如果临时加黑(警告)群聊表格不为空
					 for (LinkedHashMap<String, Object> allWarnGroup : allGWarnTable.toArrayList()) { // 遍历临时加黑(警告)群聊表格所有行
						  long currentGroupId = (long) allWarnGroup.get("GroupId"); // 获取遍历的群聊群号
						  if (currentGroupId == groupId) { // 如果当前群聊为(曾)临时加黑(警告)群聊
								int currentGroupWarnTime = (int) allWarnGroup.get("Status"); // 获取遍历的群聊警告次数
								if (currentGroupWarnTime > 1) { // 如果警告次数大于1
									 TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区
									 Calendar nowCalendar = Calendar.getInstance(); //获取系统时间
									 Date currentGroupExpireTime = (Date) allWarnGroup.get("ExpireDate"); //获取遍历的群聊警告解除日期
									 Calendar setCalendar = Calendar.getInstance(); //获取系统时间日期实例
									 setCalendar.setTime(currentGroupExpireTime); //设置时间为群聊警告解除日期
									 if (setCalendar.getTime().compareTo(nowCalendar.getTime()) > 0) {
										  return true;
									 } else {
										  return false;
									 }
								} else { // 如果是第一次警告(第一次警告不限制使用)
									 return false;
								}
						  }
					 }
				}
				return false; // 如果两个集合里都不存在该群返回false
		  } catch (Exception e) {
				e.printStackTrace();
				return false;
		  }
	 }

	/**
	 * 123 SduBotR 公用数据库文件集合<br>
	 * 集合中所有数据库文件的起始路径:[{@link appDirectory}]/data
	 * @author 御坂12456
	 *
	 */
	static class GlobalDatabases {
		 /**
		  * 系统设置数据库。<br>
		  * 文件路径:/system/syssettings.db
		  */
		 public static DBHelper dbsystem_syssettings;
		 
		 /**
		  * 群聊(提示)自定义数据库。<br>
		  * 文件路径:/group/custom.db
		  */
		 public static DBHelper dbgroup_custom;
		 
		 /**
		  * 群聊相关列表(如单群黑名单QQ列表)数据库。<br>
		  * 文件路径:/group/list.db
		  */
		 public static DBHelper dbgroup_list;
		 
		 /**
		  * 群聊发言排行榜数据库。<br>
		  * 文件路径:/group/ranking/speaking.db
		  */
		 public static DBHelper dbgroup_ranking_speaking;
		 
		 /**
		  * 音游:Arcaea玩家绑定数据库。<br>
		  * 文件路径:/group/mug/arcaea.db
		  */
		 public static DBHelper dbgroup_mug_arcaea;
		 
		 /**
		  * 123 SduBotR 公用数据库DBHelper实例集合。<br>
		  * 该字段通常用于bot关闭时循环关闭数据库连接时使用。
		  */
		 public static ArrayList<DBHelper> dbArrayList = new ArrayList<DBHelper>();
	}
}
/**
 * 防滥用保护模块
 * @author 御坂12456
 *
 */
class protectAbuse
{
	/**
	 * 执行"指令执行中"防滥用保护
	 * @param CQ CQ实例
	 * @param groupId 来源群号
	 * @param qqId 来源QQ号
	 * @return 成功返回false,检测到滥用(标志文件已存在,同时将直接发送滥用提示)返回true,失败返回false
	 */
	public static boolean doExeProtAbuse(CoolQ CQ,long groupId,long qqId)
	{
		try {
			File flagFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".using");
			if (!flagFile.exists()) { //如果标志文件不存在
				File abusedFlagFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
				if (!abusedFlagFile.exists()) { //如果已滥用标志文件不存在
					flagFile.createNewFile(); //创建标志文件
					return false;
				} else { //如果已滥用标志文件已存在
					return true;
				}
			} else {  //如果标志文件已存在
				if(qqId == Global.masterQQ) return false; //如果是机器人主人的指令，忽视防滥用保护
				File abusedFlagFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
				if (!abusedFlagFile.exists()) { //如果已滥用标志文件不存在
					abusedFlagFile.createNewFile(); //创建已滥用标志文件
					CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
							"[防滥用保护]请勿滥用功能(CD:" + Global.abuseCheckSeconds + "s)\n" + 
							"输入!uab解除滥用状态");
					return true;
				} else { //如果已滥用标志文件已存在
					return true;
				}
			}
		} catch (Exception e) { //发生异常
			return false;
		}
	}
	/**
	 * 执行指令执行后的"x秒缓冲"防滥用保护
	 * @param CQ CQ实例
	 * @param groupId 来源群号
	 * @param qqId 来源QQ号
	 */
	public void doProtAbuse(CoolQ CQ,long groupId,long qqId)
	{
		Thread customProtAbuseThread = new protThread(CQ, groupId, qqId);
		customProtAbuseThread.start();
	}

	/**
	 * 防滥用功能线程
	 * @author 御坂12456
	 *
	 */
	class protThread extends Thread
	{
		CoolQ CQ;
		long groupId;
		long qqId;
		boolean iAmSleeping;
		/**
		 * 创建秒缓冲防滥用功能检测线程的实例。
		 * @param CQ CQ实例
		 * @param groupId 被检测人员的来源群号
		 * @param qqId 被检测人员的QQ号
		 */
		public protThread(CoolQ CQ,long groupId,long qqId)
		{
			this.CQ = CQ;
			this.groupId = groupId;
			this.qqId = qqId;
			this.iAmSleeping = false;
		}
		/**
		 * 运行防滥用功能线程。
		 */
		@Override
		public void run()
		{
			try {
				if (!currentThread().isInterrupted()) { //如果线程未被要求中断
					Thread.sleep(Global.abuseCheckSeconds * 1000); //暂停设定的秒数
					// 定义机器人正在执行成员指令中标志
					File usingFlag = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".using");
					if (usingFlag.exists()) { //如果文件存在
						usingFlag.delete(); //删除文件
					} else { //如果线程被要求中断
						this.iAmSleeping = false;
					}
					//自动退出线程
				}
			} catch (InterruptedException e) { //异常:线程已中断(sleep中中断线程)
				iAmSleeping = true;
			}
			// 退出线程
		}
	}
	}

class autoSave extends TimerTask {
	private CoolQ CQ;

	public autoSave(CoolQ CQ){
		this.CQ = CQ;
	}

	public void run() {
		try {
			String temp = System.getProperty("java.io.tmpdir"); //获取临时目录
			File tmpFolder = new File(temp + "/123 SduBotR/autosave"); //新建临时目录实例
			tmpFolder.mkdirs(); //递归创建临时目录
			File finalAutoSaveFile = new File(Global.appDirectory + "/temp/autosave.zip");
			if (finalAutoSaveFile.exists()) { //如果上一个自动保存的文件存在
				finalAutoSaveFile.delete(); //先删了
			}
			File autoSaveFile = 
					new File(tmpFolder.getAbsolutePath() + "/autosave.zip");
			//新建自动保存文件实例
			ZipFileHelper.createZipFile(Global.appDirectory, autoSaveFile.getAbsolutePath()); //压缩数据目录并保存
			autoSaveFile.renameTo(finalAutoSaveFile); //移动文件至temp目录
			this.CQ.logInfoSuccess(Global.AppName, "已成功自动备份数据目录中的所有数据至:\n" + 
					finalAutoSaveFile.getAbsolutePath());
		} catch (Exception e) {
			this.CQ.logError(Global.AppName, "自动备份数据目录失败:\n" + 
					e.getMessage() + "\n" + 
					ExceptionHelper.getStackTrace(e));
		}
	}
}


