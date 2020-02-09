package cf.ots123it.open.sdubotr;

import java.io.File;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import com.sun.javafx.scene.EnteredExitedHandler;

import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;
import static cf.ots123it.open.sdubotr.Global.*;
/**
 * 123 SduBotR 群聊消息处理类<br>
 * 注意:本类中任何方法前的CQ参数请在Start类中直接用CQ即可<br>
 * <i>（若在Start类的main测试方法中调用，请使用你所new的Start实例的getCoolQ方法<br>
 * 如: <pre class="code">ProcessGroupMsg.main(<b>demo.getCoolQ()</b>,123456789L,123456789L,"Hello world");</pre></i>）
 * @author 御坂12456
 */
@SuppressWarnings("deprecation")
public abstract class ProcessGroupMsg extends JcqAppAbstract
{

	/**
	 * 主调用处理方法
	 * @param CQ CQ实例，详见本类注释
	 * @param groupId 消息来源群号
	 * @param qqId 消息来源成员QQ号
	 * @param msg 消息内容
	 * @see ProcessGroupMsg
	 */
	public static void main(CoolQ CQ,long groupId,long qqId,String msg)
	{
		try {
	        // 读取特别监视群聊列表文件(功能1-1)
			File imMonitGroups = new File(Start.appDirectory + "/group/list/iMG.txt");
			if (imMonitGroups.exists()) { //如果列表文件存在
				for (String imMonitGroup : IOHelper.ReadAllLines(imMonitGroups)) {
					if (String.valueOf(groupId).equals(imMonitGroup)) //如果消息来源群为特别监视群
					{
						Part1.Func1_1(CQ,groupId,qqId,msg); //转到功能1-1处理
						break;
					}
				}
			}
		} catch (Exception e) {
			CQ.logError("123 SduBotR", "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
	}
	/**
	 * 主功能1
	 * @author 御坂12456
	 *
	 */
	static class Part1
	{
		/**
		 * 功能1-1:特别监视违禁词提醒功能
		 * @param CQ CQ实例，详见本类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func1_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			// 判断违禁词列表是否为空
			String iMGBanConfirm = IOHelper.ReadToEnd(Global.appDirectory + "/group/list/iMGBan.txt");
			if(iMGBanConfirm.equals(""))
			{
				return;
			} // 否则
			System.gc(); //通知Java进行垃圾收集
			String[] iMGBans = IOHelper.ReadAllLines(Global.appDirectory + "/group/list/iMGBan.txt");
			for (String iMGBanString : iMGBans) {
				if (msg.indexOf(iMGBanString) != -1) { // 若消息内容包含违禁词
					CQ.sendPrivateMsg(Global.masterQQ, 
							Global.FriendlyName + "\n" +
							"检测到有人发布违禁词，请尽快查看\n" +
							"来源群号:" + Global.getGroupName(CQ, groupId) + "(" + String.valueOf(groupId) + ")\n" +
							"来源QQ:" + CQ.getGroupMemberInfo(groupId, qqId).getNick() + "(" + String.valueOf(qqId) + ")\n" + 
							"检测到的违禁词:" + iMGBanString + "\n" +
							"完整消息内容:\n" + 
							msg);
					break;
				}
			}
			return;
		}
	}
}
