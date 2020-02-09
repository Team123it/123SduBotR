/**
 * 
 */
package cf.ots123it.open.sdubotr;

import java.io.File;
import java.io.IOException;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import cf.ots123it.jhlper.ExceptionHelper;
import sun.misc.GC;

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
	public final static String Version = "0.0.1";
	/**
	 * 发布版本编号（从1开始）
	 */
	public final static String AppVersionNumber = "1";
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
	 * 123 SduBotR 首次启动初始化方法
	 * @author 御坂12456
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
		// 初始化1:创建\group
		File init1 = new File(appDirectory + "/group");
		init1.mkdir();
		System.gc(); //提示系统垃圾收集
		CQ.logDebug("123 SduBotR", "初始化:路径" + appDirectory + "\\group建立成功");
		// 初始化2:创建\private
		File init2 = new File(appDirectory + "/private");
		init2.mkdir();
		System.gc(); //同上,本方法之后不在注释同样的语句
		CQ.logDebug("123 SduBotR", "初始化:路径" + appDirectory + "\\private建立成功");
		// 初始化3:创建\group\list
		File init3 = new File(appDirectory + "/group/list");
		init3.mkdir();
		CQ.logDebug("123 SduBotR", "初始化:路径" + appDirectory + "\\group\\list建立成功");
		System.gc(); 
		// 初始化4(功能1-1):创建\group\list\iMG.txt
		File init4 = new File(appDirectory + "/group/list/iMG.txt");
		init4.createNewFile();
		CQ.logDebug("123 SduBotR", "初始化:文件" + appDirectory + "\\group\\list\\iMG.txt建立成功");
		System.gc();
		// 初始化5(功能1-1):创建\group\list\iMGBan.txt
		File init5 = new File(appDirectory + "/group/list/iMGBan.txt");
		init5.createNewFile();
		CQ.logDebug("123 SduBotR", "初始化:文件" + appDirectory + "\\group\\list\\iMGBan.txt建立成功");
		System.gc();
		// 初始化结尾:创建\firstopen.stat
		File initEnd = new File(appDirectory + "/firstopen.stat");
		initEnd.createNewFile();
		CQ.logDebug("123 SduBotR", "初始化:文件" + appDirectory + "\\firstopen.stat建立成功");
		} catch (IOException e) {
			CQ.logFatal("123 SduBotR", "初始化时出现严重错误,详细信息:\n" + 
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
}
