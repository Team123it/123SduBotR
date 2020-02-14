package cf.ots123it.open.sdubotr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import com.sun.javafx.scene.EnteredExitedHandler;

import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;
import sun.misc.GC;

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
			// 读取机器人黑名单列表文件(功能M-3)
			File AllBanPersons = new File(Start.appDirectory + "/group/list/AllBan.txt");
			if (AllBanPersons.exists()) { //如果列表文件存在
				for (String BanPerson : IOHelper.ReadAllLines(AllBanPersons)) {
					if (String.valueOf(qqId).equals(BanPerson)) //如果消息来源成员为机器人黑名单人员
					{
						return; //不多废话，直接返回（拜拜了您嘞）
					}
				}
			}
		} catch (Exception e) {
			CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
		Part_Spec.Funny_EasterEgg(CQ, groupId, qqId, msg); //调用滑稽彩蛋方法
		if ((msg.startsWith("!")) ||  (msg.startsWith("！"))) // 如果消息开头是"!"或中文"！"
		{
			//去除指令前的"!"标记
			msg = msg.substring(1, msg.length()); 
			try {
				//获得所有参数组成的数组
				String[] arguments = msg.split(" ");
				//获得第一个参数
				String arg1 = arguments[0];
				switch (arg1) // //判断第一个参数
				{
				case "about": //功能O-1:关于
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					break;
				default:
					break;
				}
			}
			catch (ArrayIndexOutOfBoundsException e) { //指令格式错误
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
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
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456(优化: Sugar 404)
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
			List<String> bans = new ArrayList<>();
			for (String iMGBanString : iMGBans) {
				if (msg.indexOf(iMGBanString) != -1) { // 若消息内容包含违禁词
					bans.add(iMGBanString);
				}
			}
			if (!bans.isEmpty()) {
				StringBuilder b = new StringBuilder();
				b.append(Global.FriendlyName).append("\n检测到有人发布违禁词，请尽快查看\n来源群号:")
				.append(Global.getGroupName(CQ, groupId)).append('(').append(groupId).append(")\n来源QQ:")
				.append(CQ.getGroupMemberInfo(groupId, qqId).getNick()).append('(').append(qqId)
				.append(")\n检测到的违禁词:");
				for (String iMGBanString:bans) {
					b.append(iMGBanString).append('.');
				}
				b.append("\n完整消息内容:\n").append(msg);
				CQ.sendPrivateMsg(Global.masterQQ,b.toString());
			}
			return;
		}
	}

	/**
	 * 其它功能（注意与"特殊模块"区分开）
	 * @author 御坂12456
	 *
	 */
	static class Part_Other{
		/**
		 * 功能O-1:关于123 SduBotR
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void FuncO_About(CoolQ CQ,long groupId,long qqId,String msg)
		{
			// 创建"关于"字符串生成器（可变字符串）对象
			StringBuilder aboutStrBuilder = new StringBuilder();
			/*
			 * "关于"内容:
			 * 123 SduBotR
			 * 一个完全开源的酷Q QQ bot程序
			 * 原作者:御坂12456(QQ:770296414)
			 * 官方群:812655602
			 * 当前登录账号:[昵称]([QQ])
			 * Github仓库:https://github.com/Misaka12456/123SduBotR
			 * 建议使用酷Q Pro运行本程序
			 */
			aboutStrBuilder.append("123 SduBotR\n")
					.append("一个完全开源的酷Q QQ bot程序\n")
					.append("原作者:御坂12456(QQ:770296414)\n")
					.append("官方群:812655602\n")
					.append("当前登录账号:").append(CQ.getLoginNick()).append("(").append(String.valueOf(CQ.getLoginQQ())).append(")\n")
					.append("Github仓库:https://github.com/Misaka12456/123SduBotR\n")
					.append("建议使用酷Q Pro运行本程序");
			// 发送消息
			CQ.sendGroupMsg(groupId, aboutStrBuilder.toString());
			return;
		}
	}
	/**
	 * 特殊模块
	 * @author 御坂12456
	 *
	 */
	static class Part_Spec
	{
		/**
		 * 特殊功能:滑稽（斜眼笑）彩蛋
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		static void Funny_EasterEgg(CoolQ CQ,long groupId,long qqId,String msg)
		{
			//若滑稽彩蛋白名单文件存在
			if ((new File(Global.appDirectory + "/group/list/funnyWL.txt").exists()))
			{
				//读取滑稽彩蛋白名单群列表
				String[] funnyWhiteList = IOHelper.ReadAllLines(Global.appDirectory + "/group/list/funnyWL.txt");
				//若白名单列表不为空
				if (funnyWhiteList != null) {
					for (String funnyWhiteGroup : funnyWhiteList) {
						if (String.valueOf(groupId).equals(funnyWhiteGroup)) //若此群为白名单群
						{
							CQ.logDebug(Global.AppName, "当前群聊:" + groupId + "属于滑稽彩蛋白名单群聊,已跳过处理");
							return; //不执行后续代码，直接返回
						}
					}
				}
			}
			String[] funnyStrings = {"[CQ:face,id=178]", //滑稽
					"[CQ:face,id=178][CQ:emoji,id=127166]", //滑稽+水滴
					"[CQ:face,id=178][CQ:face,id=66]", //滑稽+爱心
					"[CQ:face,id=178][CQ:face,id=147]", //滑稽+棒棒糖
					"[CQ:face,id=178][CQ:emoji,id=10068]", //滑稽+问号
					"[CQ:face,id=178][CQ:emoji,id=10069]", //滑稽+叹号
					"[CQ:face,id=178][CQ:face,id=67]" //滑稽+心碎
			};
			// 获取一个0到1000的整数并存储到变量i中
			int i = ThreadLocalRandom.current().nextInt(1000);
			// 获取一个0到1000的整数并存储到变量j中
			int j = ThreadLocalRandom.current().nextInt(1000);
			// 将j与i相减，赋值给k
			int k = j - i;
			if (k < -997) //如果k小于997
			{
				CQ.sendGroupMsg(groupId, funnyStrings[0]); //发送滑稽数组第1个消息
			} else if ((k >= -502) & (k <= -500)) //如果k在-500~-502
			{
				CQ.sendGroupMsg(groupId, funnyStrings[1]); //发送滑稽数组第2个消息
			} else if ((k >= -241) & (k <= -239))  //如果k在-239~-241
			{
				CQ.sendGroupMsg(groupId, funnyStrings[2]);  //发送滑稽数组第3个消息
			} else if ((k >= -1) & (k <= 1)) //如果k在-1~1
			{
				CQ.sendGroupMsg(groupId, funnyStrings[3]);  //发送滑稽数组第4个消息
			} else if ((k >= 299) & (k <= 301)) //如果k在299~301
			{
				CQ.sendGroupMsg(groupId, funnyStrings[4]);  //发送滑稽数组第5个消息
			} else if ((k >= 499) & (k <= 501)) //如果k在499~501
			{
				CQ.sendGroupMsg(groupId, funnyStrings[5]);  //发送滑稽数组第6个消息
			} else if ((k > 997)) //如果k大于997
			{
				CQ.sendGroupMsg(groupId, funnyStrings[6]);  //发送滑稽数组第7个消息
			}
			System.gc(); //执行垃圾收集器
			return;
		}
	}
}
