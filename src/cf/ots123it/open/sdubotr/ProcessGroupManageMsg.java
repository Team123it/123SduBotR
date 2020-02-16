package cf.ots123it.open.sdubotr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.meowy.cqp.jcq.entity.*;
import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.IMsg;
import org.meowy.cqp.jcq.entity.IRequest;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.meowy.cqp.jcq.message.CQCode;

import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;
/**
 * 123 SduBot 群管理代码调用类(以#开始，在群聊中使用)<br>
 * 当前暂时仅Global.java中设置的机器人主人可调用
 *  * 注意:本类中任何方法前的CQ参数请在Start类中直接用CQ即可<br>
 * <i>（若在Start类的main测试方法中调用，请使用你所new的Start实例的getCoolQ方法<br>
 * 如: <pre class="code">ProcessGroupManageMsg.main(<b>demo.getCoolQ()</b>,123456789L,123456789L,"#stat");</pre></i>）
 * @author 御坂12456
 *
 */
@SuppressWarnings("deprecation")
public abstract class ProcessGroupManageMsg extends JcqAppAbstract implements IMsg,IRequest
{
	/**
	 * 主调用处理方法
	 * @param CQ CQ实例，详见本类注释
	 * @param groupId 消息来源群号
	 * @param qqId 消息来源成员QQ号
	 * @param msg 消息内容
	 * @see ProcessGroupManageMsg
	 * @author 御坂12456
	 */
	public static void main(CoolQ CQ,long groupId,long qqId,String msg)
	{
		//去除管理指令前的"#"标记
		msg = msg.substring(1, msg.length());
		try {
			//获得所有参数组成的字符串
			String arguments = msg;
			//获得第一个参数
			String arg1 = arguments.split(" ", 2)[0];
			switch (arg1) //判断第一个参数
			{
			case "stat": //功能M-1:输出123 SduBotR运行状态
				Standalone_Funcs.getRunningStatus(CQ, groupId, qqId, arguments);
				break;
			case "testpic": //功能M-2:用图片测试是否能使用123 SduBotR的全部功能
				Standalone_Funcs.testSendPic(CQ, groupId, qqId, arguments);
				break;
				/* 功能M-3:机器人黑名单添加/删除功能 */
			case "banadd": //功能M-3-1:机器人黑名单添加
				Standalone_Funcs.allBan.addBanPerson(CQ, groupId, qqId, arguments);
				break;
			case "bandel": //功能M-3-2:机器人黑名单删除
				Standalone_Funcs.allBan.delBanPerson(CQ, groupId, qqId, arguments );
				break;
			default:
				break;
			}
		} catch (IndexOutOfBoundsException e) { //指令格式错误
			CQ.logError(Global.AppName, "您输入的指令格式有误,请检查后再试\n" +
							  "指令类型:群管理（机器人主人专用）指令（前缀为#）\n" +
							  "来源群号:" + Global.getGroupName(CQ, groupId) + "(" + groupId + ")\n" +
							  "您输入的指令:" + msg);
		} catch (Exception e)
		{
			CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
		return;
	}
	/**
 * 群管理指令独立处理类
 * @author 御坂12456
 *
 */
	static class Standalone_Funcs{
		
	/**
	 * 功能M-1:输出123 SduBotR运行状态
	 * @param  CQ CQ实例，详见本大类注释
	 * @param groupId 消息来源群号
	 * @param qqId 消息来源成员QQ号
	 * @param msg 消息内容
	 * @author 御坂12456
	 */
		public static void getRunningStatus(CoolQ CQ,long groupId,long qqId,String msg)
		{
			// 设置时区为GMT-8（解决多出8小时的Bug）
			TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
			TimeZone.setDefault(tz);
			// 获取已启动时长
			Date upTimeDate = new Date(ManagementFactory.getRuntimeMXBean().getUptime());
			// 转化为Calendar对象
			Calendar upTime = Calendar.getInstance();
			upTime.setTime(upTimeDate);
			// 对分钟和秒补零并把年、月、日、时、分、秒赋值给upTimeStr数组
			String upYear,upMonth,upDay,upHour,upMinute,upSecond;
			upYear = String.valueOf(upTime.get(Calendar.YEAR) - 1970);
			upMonth = String.valueOf(upTime.get(Calendar.MONTH));
			upDay = String.valueOf(upTime.get(Calendar.DAY_OF_MONTH) - 1);
			upHour = String.valueOf(upTime.get(Calendar.HOUR_OF_DAY));
			if (upTime.get(Calendar.MINUTE) < 10) { //分补零
				upMinute = "0" + String.valueOf(upTime.get(Calendar.MINUTE));
			} else {
				upMinute = String.valueOf(upTime.get(Calendar.MINUTE));
			}
			if (upTime.get(Calendar.SECOND) < 10) { //秒补零
				upSecond = "0" + String.valueOf(upTime.get(Calendar.SECOND));
			} else {
				upSecond = String.valueOf(upTime.get(Calendar.SECOND));
			}
			// 使用upTimeBuilder合并字符串
			StringBuilder upTimeBuilder = new StringBuilder();
			upTimeBuilder.append(upYear).append("y").append(" ").append(upMonth).append("m").append(" ").append(upDay)
			.append("d").append(" ").append(upHour).append(":").append(upMinute).append(":").append(upSecond);
			System.gc(); //通知系统进行垃圾收集
			CQ.sendGroupMsg(groupId, Global.AppName + "\n" + 
					"运行状态\n" + 
					"*本程序完全开源,项目链接:\n" + 
					"https://github.com/Misaka12456/123SduBotR\n" +
					"1.版本信息\n" +
					"程序名:" + Global.AppName + "\n" +
					"版本:" + Global.Version + "(" + Global.AppVersionNumber + ")\n" +
					"2.数据统计\n" + 
					"总共添加群聊数:" + CQ.getGroupList().size() + "\n" +
					"总共添加QQ号数:" + CQ.getFriendList().size() + "\n" +
					"3.运行环境信息\n" +
					"操作系统:" + System.getProperty("os.name") + "\n" +
					"操作系统版本:" + System.getProperty("os.version") + "\n" + 
					"Java运行时（JRE）版本:" + System.getProperty("java.runtime.version") + "\n" + 
					"数据目录:" + Global.appDirectory + "\n" +
					"程序已正常运行时间:" + upTimeBuilder.toString());
		}
		
		/**
		 * 功能M-2:测试当前登录账号是否可以发送图片,从而测试是否可以使用123 SduBotR的全部功能
		 * @param  CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void testSendPic(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				// 设置时区为GMT-8（临时图片文件名会用到）
				TimeZone tz = TimeZone.getTimeZone("ETC/GMT-8");
				TimeZone.setDefault(tz);
				int width = 300,height = 150; //设置测试图片宽高(300x150)
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR); //创建图片对象
				Graphics2D g2 = image.createGraphics(); //使用G2D创建绘图对象
				g2.setColor(Color.white); //将画笔设置为白色
				g2.fillRect(0, 0, width, height); //填充白色（背景色）
				g2.setColor(new Color(0, 128, 255)); //将画笔设置成浅蓝色(R:0,G:128,B:255)
				g2.setFont(new Font("Comic Sans MS", Font.PLAIN, 20)); //设置字体(Comic Sans MS,常规,字号20)
				g2.drawString("123 SduBotR", 72,30); //在72*30起始处绘制测试文字第一行
				g2.setColor(Color.BLACK); //将画笔设置成黑色(R:0,G:0,B:0)
				g2.drawString("This is a test image", 50,70); //在50*70起始处绘制测试文字第二行
				g2.dispose(); //保存绘图对象
				// 创建临时图片文件对象（路径:\temp\[yyyyMMddHHmmss].png）
				File tmptestImageFile = new File(Global.appDirectory + "/temp/" + new SimpleDateFormat("yyyyMMddHHmmss")
						.format(new Date(System.currentTimeMillis())) + ".png");
				// 创建并写入临时图片文件（路径见上）
				ImageIO.write(image, "png", tmptestImageFile);
				// 读取临时图片文件到test变量中
				CQImage test = new CQImage(tmptestImageFile);
				// 获取图片CQ码并发送
				CQ.sendGroupMsg(groupId,new CQCode().image(test));
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + "已发送测试图片,若接收到则证明可以使用123 SduBotR的全部功能\n" +
									"若未接收到则有可能是您运行的是酷Q Air,请使用酷Q Pro后再试");
				tmptestImageFile.delete(); // 删除临时图片文件
				System.gc(); //执行垃圾收集器
				return;
			} catch (Exception e) {
				CQ.logError(Global.AppName, "测试图片发送失败,发生异常:\n" + ExceptionHelper.getStackTrace(e));
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n测试图片发送失败,请查看日志以获取详细信息");
				return;
			}
		}
		/**
		 * 功能M-3:机器人黑名单功能
		 * @author 御坂12456
		 *
		 */
		static class allBan{
			/**
			 * 功能M-3-1:机器人黑名单添加人员
			 * @param  CQ CQ实例，详见本大类注释
			 * @param groupId 消息来源群号
			 * @param qqId 消息来源成员QQ号
			 * @param msg 消息内容
			 * @author 御坂12456
			 */
			public static void addBanPerson(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					String arg2 = msg.split(" ", 2)[1]; //获取参数2（要添加的成员QQ号或at）
					String banPersonQQ;
					if (arg2.startsWith("[")) 
					{ //如果是at
						banPersonQQ = arg2.substring(10, arg2.length() - 1); //读取CQ码中的QQ号
					} else { //否则
						banPersonQQ = arg2; //直接读取输入的QQ号
					}
					if (Long.parseLong(banPersonQQ) == Global.masterQQ) //如果要添加的成员就是机器人主人
					{
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
								"不能把主人加入黑名单的~");
						return; //直接返回
					}
					File AllBanPersonsFile = new File(Global.appDirectory + "/group/list/AllBan.txt");
					if (AllBanPersonsFile.exists()) { //如果列表文件存在
						if (IOHelper.ReadToEnd(AllBanPersonsFile).equals("")) { //如果列表文件为空
							IOHelper.WriteStr(AllBanPersonsFile, banPersonQQ); //直接写入QQ号
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
									"按照主人的意愿,已成功将" + CQ.getStrangerInfo(Long.parseLong(banPersonQQ),true).getNick() + "(" +  banPersonQQ +  ")" + "加入黑名单!");
						} else { //否则（不为空）
							for (String banPerson : IOHelper.ReadAllLines(AllBanPersonsFile)) {
								if (banPersonQQ.equals(banPerson)) { //如果黑名单成员已经在列表里了
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"这个人已经在黑名单里了QwQ");
									return; //直接返回
								}
							}
							IOHelper.AppendWriteStr(AllBanPersonsFile, "\n" + banPersonQQ); //追加写入QQ号(回车符+QQ号)
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
									"按照主人的意愿,已成功将" + CQ.getStrangerInfo(Long.parseLong(banPersonQQ),true).getNick() + "(" +  banPersonQQ +  ")" + "加入黑名单!");
						}
					}
				} catch(IndexOutOfBoundsException e)
				{
					CQ.logError("123 SduBotR", "您输入的指令格式有误,请检查后再试\n" +
							  "指令类型:群管理（机器人主人专用）指令（前缀为#）\n" +
							  "来源群号:" + Global.getGroupName(CQ, groupId) + "(" + groupId + ")\n" +
							  "您输入的指令:" + msg);
				} catch (Exception e) {
					CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
							"详细信息:\n" +
							ExceptionHelper.getStackTrace(e));
				}
			}
			/**
			 * 功能M-3-2:机器人黑名单删除人员
			 * @param  CQ CQ实例，详见本大类注释
			 * @param groupId 消息来源群号
			 * @param qqId 消息来源成员QQ号
			 * @param msg 消息内容
			 * @author 御坂12456
			 */
			public static void delBanPerson(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					String arg2 = msg.split(" ", 2)[1]; //获取参数2（要删除的成员QQ号或at）
					String banPersonQQ;
					if (arg2.startsWith("[")) 
					{ //如果是at
						banPersonQQ = arg2.substring(10, arg2.length() - 1); //读取CQ码中的QQ号
					} else { //否则
						banPersonQQ = arg2; //直接读取输入的QQ号
					}
					File AllBanPersonsFile = new File(Global.appDirectory + "/group/list/AllBan.txt");
					if (AllBanPersonsFile.exists()) { //如果列表文件存在
						if (IOHelper.ReadToEnd(AllBanPersonsFile).equals("")) { //如果列表文件为空
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
										"黑名单列表是空的w");
						} else { //否则（不为空）
							for (String banPerson : IOHelper.ReadAllLines(AllBanPersonsFile)) {
								if (banPersonQQ.equals(banPerson)) { //如果该成员在列表里
									ArrayList<String> AllBanPersons = new ArrayList<String>(); //新建一个ArrayList对象
									Collections.addAll(AllBanPersons, IOHelper.ReadAllLines(AllBanPersonsFile)); //读取原列表所有成员
									/* 由于直接remove(Object o)判断的是地址导致无法remove成功
									 * 故此处直接使用ArrayList的父类迭代器的remove方法来remove掉要移除的成员QQ号
									 * （确定的ArrayList对应的迭代器若发生更改，该ArrayList也会发生更改）
									 * Author:御坂12456 于 2020-2-15 0:36 */
									Iterator<String> it = AllBanPersons.iterator(); //获取迭代器对象
									while (it.hasNext()) { //如果它存在下一个值
										String tmpbanPerson = it.next(); //获取下一个值（下一个黑名单成员QQ号）
										if (tmpbanPerson.equals(banPerson)) { //如果此黑名单成员即当前要移除的成员
											it.remove(); //移除该成员
										}
									}
									AllBanPersonsFile.delete(); //先删除原列表文件
									IOHelper.WriteStr(AllBanPersonsFile, ""); //重新创建该文件
									for (String newBanPerson : AllBanPersons) {
										if (newBanPerson.equals(AllBanPersons.get(AllBanPersons.size() - 1))) { //如果当前循环到的newBanPerson是最后一个
											IOHelper.AppendWriteStr(AllBanPersonsFile, newBanPerson); //直接写入最后一个QQ号
											break; //跳出for循环
										} else {
											IOHelper.AppendWriteStr(AllBanPersonsFile, newBanPerson  + "\n"); //写入后来个换行
										}
									}
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
												"好啦好啦," + CQ.getStrangerInfo(Long.parseLong(banPersonQQ),true).getNick() + "(" +  banPersonQQ +  ")"  + "已经被我删啦xd");
									return; //返回
								}
							}
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
									"这人没在列表中xxx");
						}
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.logError("123 SduBotR","Oops!数组下标居然越界了!看看是哪里出错了:(\n" +
							ExceptionHelper.getStackTrace(e));
				} catch(Exception e)
				{
					CQ.logError("123 SduBotR", "您输入的指令格式有误,请检查后再试\n" +
							"指令类型:群管理（机器人主人专用）指令（前缀为#）\n" +
							"来源群号:" + Global.getGroupName(CQ, groupId) + "(" + groupId + ")\n" +
							"您输入的指令:" + msg);
				}
			}
		}
	}
}
