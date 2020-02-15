/**
 * 
 */
package cf.ots123it.open.sdubotr;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import cf.ots123it.jhlper.ExceptionHelper;

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
	
	/**
	 * 机器人主人QQ号，如需自定义机器人请改成自己所需要的，默认为本人（御坂12456）的
	 */
	public final static long masterQQ = 770296414L;
	/**
	 * 123 SduBotR 程序名（若自定义机器人请务必更改此名称）
	 */
	public final static String AppName = "123 SduBotR";
	/**
	 * 版本
	 */
	public final static String Version = "0.1.4";
	/**
	 * 发布版本编号（从1开始）
	 */
	public final static String AppVersionNumber = "Alpha-14";
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
	 * 123 SduBotR的完整功能菜单
	 */
	public final static String menuStr = "123 SduBotR 功能菜单\n" + 
			"详细内容:https://github.com/Misaka12456/123SduBotR/blob/master/README.md\n" + 
			"以下[]代表必填参数,{}代表可填参数\n" + 
			"1.群管理核心功能\n" + 
			"1-1.禁言\n" + 
			"!mt [@/QQ号] [时长(单位:分钟)]\n" + 
			"1-2.解禁\n" + 
			"!um [@/QQ号] [时长(单位:分钟)]\n" + 
			"1-3.踢人\n" + 
			"!k [@/QQ号]\n" + 
			"1-4.永踢人（慎用）\n" + 
			"!fk [@/QQ号]\n" + 
			"O.其它功能\n" + 
			"O-1.关于\n" + 
			"!about\n" + 
			"O-2.功能菜单\n" + 
			"!m";
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
		initReady1.delete();
		File initReady2 = new File(appDirectory + "/private");
		initReady2.delete();
		String[] files = {"/temp","/group","/private","/group/list","/group/list/iMG.txt",
				"/group/list/iMGBan.txt","/group/list/funnyWL.txt","/group/list/AllBan.txt","/group/list/AllGBan.txt","/firstopen.stat"};
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
	
}
