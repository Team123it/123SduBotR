/**
 * 
 */
package cf.ots123it.open.sdubotr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.meowy.cqp.jcq.annotation.common.CQAnonymous;
import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import com.sun.prism.shader.Mask_TextureRGB_AlphaTest_Loader;

import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;
import cf.ots123it.jhlper.UserInterfaceHelper;
import cf.ots123it.jhlper.ZipFileHelper;
import cf.ots123it.jhlper.UserInterfaceHelper.MsgBoxButtons;
import sun.util.resources.cldr.cgg.CalendarData_cgg_UG;

/**
 * 
 * 123 SduBotR 公用常量数据存储类<br>
 * 如需添加公用静态(static)常量请在此处添加，使用时先<br>
 * <pre class="code">import static cf.ots123it.open.sdubotr.Global.*;</pre> 
 * @author 御坂12456
 */
@SuppressWarnings("deprecation")
public abstract class Global extends JcqAppAbstract
{
	// [start] 常变量集合
	/**
	 * 机器人主人QQ号，如需自定义机器人请改成自己所需要的，默认为本人（御坂12456）的
	 */
	public static long masterQQ;
	/**
	 * 123 SduBotR 程序名（若自定义机器人请务必更改此名称）
	 */
	public final static String AppName = "123 SduBotR";
	/**
	 * 版本
	 */
	public final static String Version = "0.2.5";
	/**
	 * 发布版本编号（从1开始）
	 */
	public final static String AppVersionNumber = "Alpha-25";
	/**
	 * 123 SduBotR 友好名称(实际运行时在QQ中显示)<br>
	 * 效果:【123 SduBotR {Version}】
	 */
	public final static String FriendlyName = "【" + AppName + " " + Version + "】";
	/**
	 * 123 SduBotR 数据存放路径
	 */
	public final static String appDirectory = Start.appDirectory;
	/**
	 * 防滥用功能缓冲秒数
	 */
	public final static int abuseCheckSeconds = 5;
	/**
	 * 机器人侮辱检测所使用的脏话列表
	 * @since 0.2.5
	 */
	public final static String[] bannedObscenitiesToBot = {"垃圾","傻逼","智障","脑残","贱","你妈炸了","滚","fuck","shit","bullshit","laji","nmsl"};
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
			"!eg [群号]";
	/**
	 * 123 SduBotR的完整功能菜单
	 */
	public final static String menuStr = "123 SduBotR 功能菜单\n" + 
			"详细内容:https://github.com/Misaka12456/123SduBotR/blob/master/README.md\n" + 
			"以下[]代表必填参数,{}代表可填参数\n" + 
			"1.群管理核心功能\n" + 
			"1-1.禁言\n" + 
			"!mt [@/QQ号] [时长(单位:分钟)]\n" + 
			"1-2.解禁\n" + 
			"!um [@/QQ号]\n" + 
			"1-3.踢人\n" + 
			"!k [@/QQ号]\n" + 
			"1-4.永踢人（慎用）\n" + 
			"!fk [@/QQ号]\n" + 
			"1-5.群黑名单\n" + 
			"1-5-1~2.启动/关闭黑名单\n" + 
			"!blist [start/stop]\n" + 
			"1-5-3~4.添加/删除黑名单成员\n" +
			"!blist [add/del] [成员1QQ号/at] {成员nQQ号/at...}\n" + 
			"1-5-5.查看本群黑名单列表\n" + 
			"!blist show\n" + 
			"1-5-6.切换黑名单成员入群拒绝提醒状态\n" +
			"!blist cnp\n" +
			"2.群管理辅助功能(详见Github)\n" +
			"3.群增强功能\n" +
			"3-1.查看群成员日发言排行榜(Top10)\n" +
			"!rk\n" +
			"4.实用功能\n" +
			"4-1.查看新冠肺炎(SARS-Cov-2)疫情实时数据\n" +
			"!cov {省份名}\n" +
			"4-2.Bilibili实时粉丝数据\n" +
			"!bf [UID]\n" +
			"O.其它功能\n" + 
			"O-1.关于\n" + 
			"!about\n" + 
			"O-2.功能菜单\n" + 
			"!m\n" + 
			"O-3.解除防滥用\n" + 
			"!uab {验证码}" + 
			"O-4.反馈\n" +
			"!rpt [具体内容]";
	// [end]
	/**
	 * 123 SduBotR 首次启动初始化方法
	 * @author 御坂12456(优化:Sugar 404)
	 * @param CQ 方法调用来源的CQ实例
	 */
	public static void Initialize(CoolQ CQ)
	{
		try {
			// 初始化准备:删除数据目录所有文件夹
			File initReady1 = new File(appDirectory + "/group");
			if (initReady1.exists())
			{
				IOHelper.DeleteAllFiles(initReady1);
			}
			File initReady2 = new File(appDirectory + "/private");
			if (initReady2.exists()) {
				IOHelper.DeleteAllFiles(initReady2);
			}
			String[] files = {"/temp","/group","/private","/protect","/group/ranking","/group/ranking/speaking","/group/list","/group/blist","/protect/group",
					"/protect/group/abuse","/group/list/iMG.txt","/group/list/iMGBan.txt","/group/list/funnyWL.txt","/group/list/AllBan.txt",
					"/group/list/AllGBan.txt","/firstopen.stat"};
			for (String f:files) {
				File init = new File(appDirectory + f);
				if (f.contains(".")) {
					init.createNewFile();
					CQ.logDebug(Global.AppName , "初始化:文件" + appDirectory + f + "建立成功");
				}else {
					init.mkdir();
					CQ.logDebug(Global.AppName, "初始化:路径" + appDirectory + f + "建立成功");
				}

				System.gc();
			}
		} catch (IOException e) {
			CQ.logFatal(Global.AppName, "初始化时出现严重错误,详细信息:\n" + 
					ExceptionHelper.getStackTrace(e));
		} finally {
		}
		return; //返回
	}

	/**
	 * 恢复自动备份的数据
	 * @param CQ
	 */
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
		return true;
//		try {
//			switch (CQ.getGroupMemberInfo(checkGroupId, checkQQId).getAuthority().value()) //获取成员权限(1/成员,2/管理员,3/群主)
//			{
//			case 2: //是管理员
//			case 3: //是群主
//				return true;
//			default: //不是管理组成员
//				return false;
//			}
//		} catch (Exception e) {
//			return false;
//		}
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
		return true;
//		try {
//			switch (CQ.getGroupMemberInfo(checkGroupId, CQ.getLoginQQ()).getAuthority().value()) //获取权限(1/成员,2/管理员,3/群主)
//			{
//			case 2: //是管理员
//			case 3: //是群主
//				return true;
//			default: //不是管理组成员
//				return false;
//			}
//		} catch (Exception e) {
//			return false;
//		}
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
			this.CQ.logInfoSuccess(Global.FriendlyName, "已成功自动备份数据目录中的所有数据至:\n" + 
					finalAutoSaveFile.getAbsolutePath());
		} catch (Exception e) {
			this.CQ.logError(Global.FriendlyName, "自动备份数据目录失败:\n" + 
					e.getMessage() + "\n" + 
					ExceptionHelper.getStackTrace(e));
		}
	}
}


