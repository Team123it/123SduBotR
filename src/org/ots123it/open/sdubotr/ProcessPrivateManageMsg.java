package org.ots123it.open.sdubotr;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.ots123it.jhlper.CommonHelper;
import org.ots123it.jhlper.ExceptionHelper;
import org.ots123it.jhlper.IOHelper;
import org.ots123it.open.sdubotr.Global.GlobalDatabases;
import org.ots123it.open.sdubotr.Utils.ListFileException;
import org.ots123it.open.sdubotr.Utils.ListFileHelper;

import java.io.File;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.ots123it.jhlper.CommonHelper.*;
import static org.ots123it.open.sdubotr.Global.*;

/**
 * 123 SduBotR 机器人主人私聊消息处理类<br>
 * 注意:本类中任何方法前的CQ参数请在Start类中直接用CQ即可<br>
 * <i>（若在Start类的main测试方法中调用，请使用你所new的Start实例的getCoolQ方法<br>
 * 如:
 * <pre class="code">
 * ProcessPrivateManageMsg.main(<b>demo.getCoolQ()</b>,Global.masterQQ,"Hello world");
 * </pre>
 * </i>）
 * @author 御坂12456
 * @since 0.2.5
 */
@SuppressWarnings("deprecation")
public abstract class ProcessPrivateManageMsg extends JcqAppAbstract
{

	public static void main(CoolQ CQ, long qqId, String msg)
	{
		if ((msg.startsWith("!")) || (msg.startsWith("！"))) // 如果消息开头是"!"或中文"！"
		{
			// 去除指令前的"!"标记
			msg = msg.substring(1, msg.length());
			try {
				// 获得所有参数组成的数组
				String[] arguments = msg.split(" ");
				// 获得第一个参数
				String arg1 = arguments[0];
				switch (arg1.toLowerCase()) // //判断第一个参数
				{
				case "spm": //功能1:发送私聊消息
					Standalone_Funcs.Func1_sendPrivateMsg(CQ, qqId, msg);
					return;
				case "sgm": //功能2:发送群聊消息
					Standalone_Funcs.Func2_sendGroupMsg(CQ, qqId, msg);
					return;
				case "eg": //功能3:退出指定群
					Standalone_Funcs.Func3_exitGroup(CQ, qqId, msg);
					return;
				case "pm": //功能4:查看私聊功能菜单
					Standalone_Funcs.Func4_getPrivateMenu(CQ, qqId, msg);
					return;
				case "cig": //功能5:机器人受邀入群确认
					Standalone_Funcs.Func5_inviteGroupRequestCheck(CQ, qqId, msg);
					return;
				case "warn": //功能6:对违反使用协议的群聊设置警告/加黑
					Standalone_Funcs.Func6_warnAbusedGroup(CQ, qqId, msg);
					return;
				case "cwarn": //功能6-2:确认(功能6执行的)警告/加黑操作
					Standalone_Funcs.Func6_2_confirmWarnAbusedGroup(CQ, qqId, msg);
					return;
				case "gstat": //功能7:查看群聊状态
					Standalone_Funcs.Func7_showAbusedStat(CQ, qqId, msg);
					return;
//				case "4.1":
//					String arg2 = arguments[1];
//					switch (arg2.toLowerCase())
//					{
//					case "auth":
//						Standalone_Funcs.FuncS_aprilFoolsDay_EasterEggGame_setAuth(CQ, qqId, msg);
//						return;
//					case "stat":
//						Standalone_Funcs.FuncS_aprilFoolsDay_EasterEggGame_showStat(CQ, qqId, msg);
//						return;
//					}
				default:
					return;
				}
			} catch (NumberFormatException e) { // 指令格式错误(1)
				if ((msg.trim().equals("!")) || (msg.trim().equals("！")))
					return;
				CQ.sendPrivateMsg(qqId, Global.FriendlyName + "\n您输入的指令格式有误,请检查后再试\n" + "您输入的指令:");
			} catch (IndexOutOfBoundsException e) { // 指令格式错误(2)
				if ((msg.trim().equals("!")) || (msg.trim().equals("！")))
					return;
				CQ.sendPrivateMsg(qqId, Global.FriendlyName + "\n您输入的指令格式有误,请检查后再试\n" + "您输入的指令:");
			}
		}

	}
	
	static class Standalone_Funcs{
		/**
		 * 功能1:发送私聊消息
		 * @param CQ CQ实例
		 * @param qqId 来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func1_sendPrivateMsg(CoolQ CQ,long qqId,String msg)
		{
			try {
				String[] arguments = msg.split(" ", 3); //获取参数列表
				String personStr = arguments[1].trim(); //定义要发送到的QQ号
				String sendStr = arguments[2].trim(); //定义要发送的信息
				if (CommonHelper.isInteger(personStr)) { //如果QQ号是数字
					if (sendStr.equals("")) { //如果要发送的信息为空 
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n要发送的信息不能为空");
					} else { //如果要发送的信息不为空
						int result = CQ.sendPrivateMsg(Long.parseLong(personStr), sendStr); //发送消息
						if (result < 0) { //如果发送失败(返回值为负值)
							CQ.sendPrivateMsg(qqId, FriendlyName + "\n发送失败(" + result + ")");
						} else { //如果发送成功
							CQ.sendPrivateMsg(qqId, FriendlyName + "\n消息发送成功");
						}
						return; //返回
					}
				} else { //如果QQ号不是数字
					CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的QQ号不合法,请重新输入");
				}
			} catch (IndexOutOfBoundsException e) { //数组下标越界异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n" + 
						"格式:!spm [QQ号] [消息内容]");
			} catch (Exception e) { //其它异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"发送失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
		/**
		 * 功能2:发送群聊消息
		 * @param CQ CQ实例
		 * @param qqId 来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func2_sendGroupMsg(CoolQ CQ,long qqId,String msg)
		{
			try {
				String[] arguments = msg.split(" ", 3); //获取参数列表
				String groupStr = arguments[1].trim(); //定义要发送到的群号
				String sendStr = arguments[2].trim(); //定义要发送的信息
				if (CommonHelper.isInteger(groupStr)) { //如果群号是数字
					if (sendStr.equals("")) { //如果要发送的信息为空 
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n要发送的信息不能为空");
					} else { //如果要发送的信息不为空
						int result = CQ.sendGroupMsg(Long.parseLong(groupStr), sendStr); //发送消息
						if (result < 0) { //如果发送失败(返回值为负值)
							CQ.sendPrivateMsg(qqId, FriendlyName + "\n发送失败(" + result + ")");
						} else { //如果发送成功
							CQ.sendPrivateMsg(qqId, FriendlyName + "\n消息发送成功");
						}
						return; //返回
					}
				} else { //如果群号不是数字
					CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的群号不合法,请重新输入");
				}
			} catch (IndexOutOfBoundsException e) { //数组下标越界异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n" + 
						"格式:!sgm [群号] [消息内容]");
			} catch (Exception e) { //其它异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"发送失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
		/**
		 * 功能3:退出指定群
		 * @param CQ CQ实例
		 * @param qqId 来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func3_exitGroup(CoolQ CQ,long qqId,String msg)
		{
			try {
				String[] arguments = msg.split(" ", 2); //获取参数列表
				String groupStr = arguments[1].trim(); //定义要退出的群号
				if (CommonHelper.isInteger(groupStr)) { //如果群号是数字
					int result = CQ.setGroupLeave(Long.parseLong(groupStr), false); //退出指定群
					if (result < 0) { //如果退群失败(返回值为负值)
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n发送失败(" + result + ")");
					} else { //如果退群成功
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n退群成功");
					}
					return; //返回
				} else { //如果群号不是数字
					CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的群号不合法,请重新输入");
				}
			} catch (IndexOutOfBoundsException e) { //数组下标越界异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n" + 
						"格式:!eg [群号]");
			} catch (Exception e) { //其它异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"退群失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}

		/**
		 * 功能4:查看私聊功能菜单
		 * @param CQ CQ实例
		 * @param qqId 来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func4_getPrivateMenu(CoolQ CQ,long qqId, String msg)
		{
			try {
				CQ.sendPrivateMsg(qqId, Global.masterMenuStr); //发送私聊帮助
			} catch (Exception e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"获取失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}

		/**
		 * 功能5:机器人受邀入群确认
		 * @param CQ CQ实例
		 * @param qqId 来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func5_inviteGroupRequestCheck(CoolQ CQ,long qqId,String msg)
		{
			try {
				String arg3 = msg.split(" ", 3)[2].toLowerCase(); //获取参数
				switch (arg3) //判断参数
				{
				case "agree": //如果是同意
					String responseFlag_a = msg.split(" ",3)[1]; //反馈标识
					int result = CQ.setGroupAddRequest(responseFlag_a, Start.REQUEST_GROUP_INVITE, Start.REQUEST_ADOPT, null);
					if (result < 0) {
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n邀请入群请求通过失败(" + result + ")");
						return;
					} else {
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
								"已通过该邀请入群请求");
					}
					return;
				case "refuse": //如果是拒绝
					String responseFlag_r = msg.split(" ",3)[1]; //反馈标识
					int result2 = CQ.setGroupAddRequest(responseFlag_r, Start.REQUEST_GROUP_INVITE, Start.REQUEST_REFUSE, null);
					if (result2 < 0) {
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
								"邀请入群请求拒绝失败(" + result2 + ")");
					} else {
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
								"已拒绝该邀请入群请求");
						return;
					}
				}
			} catch (IndexOutOfBoundsException e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请检查后重试\n" + 
						"格式:!cig [请求标识]");
			}
		}
		/**
		 * 功能6:对违反使用协议的群聊设置警告/加黑
		 * @param CQ CQ实例
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see #Func6_2_confirmWarnAbusedGroup(CoolQ, long, String)
		 */
		public static void Func6_warnAbusedGroup(CoolQ CQ,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ",2)[1]; //获取要警告的群号
				if (CommonHelper.isInteger(arg2)) { //如果群号是数字
//					ListFileHelper allGBanHelper = new ListFileHelper(Global.appDirectory + "/group/list/AllGBan.txt"); //新建机器人永久群聊黑名单列表文件
//					ListFileHelper allGWarnHelper = new ListFileHelper(Global.appDirectory + "/group/list/AllGWarn.txt"); //新建机器人警告群聊列表文件

					ResultSet allGBanWarnSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM AllGBanWarn WHERE GroupId=" + arg2);
					if (allGBanWarnSet.next()) {
						 int status = allGBanWarnSet.getInt("Status");
						 if (status == -1) {
								CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
										"该群聊已在机器人永久黑名单中,请勿重复警告或加黑");
								return;
					    } else if ((status != 0)) { //如果群聊已经在警告黑名单中
					   	   int warnTimes = allGBanWarnSet.getInt("Status"); //读取警告次数
					   	   long unBanDateMillis = allGBanWarnSet.getLong("WarnEndTime"); //读取限制解除时间戳
					   	   TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
								Calendar unBanCalendar = Calendar.getInstance(); //获取当前时区Calendar实例
								unBanCalendar.setTimeInMillis(unBanDateMillis); //通过时间戳设置Calendar的时间为限制解除时间
								Date unBanDate = unBanCalendar.getTime(); //将Calendar实例转换成Date实例(限制解除时间实例)
								String unBanDateStr = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(unBanDate); //获取格式化的限制解除时间字符串
								if ((unBanCalendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) >= 0) { //如果限制解除时间晚于当前系统时间
									CQ.sendPrivateMsg(qqId, FriendlyName + "\n" +
											"无法对群聊" + arg2 + "设置警告\n" + 
											"原因:该群仍在限制期内,无法叠加警告\n" + 
											"该群当前状态:\n" + 
											"警告次数:第" + warnTimes + "次\n" + 
											"限制解除时间:" + unBanDateStr);
									return;
								} else { //如果限制解除时间早于当前系统时间(时间上已解除限制)
									File confirmSignFile = new File(Global.appDirectory + "/temp/warn_" + arg2 + ".confirm"); //新建确认标志文件实例
									switch (warnTimes)
									{
									case 1: case 2: //如果上次是第一次第二次警告(本次警告为第二次或第三次)
										IOHelper.WriteStr(confirmSignFile, Long.toString(warnTimes + 1)); //写入本次警告次数
										break;
									case 3: //如果上次是第三次警告(本次为永久加黑)
										IOHelper.WriteStr(confirmSignFile, "forever"); //写入加黑提示
										break;
									default:
										break;
									}
									// [start] 设置返回信息字符串
									StringBuilder resultBuilder = new StringBuilder(FriendlyName).append("\n")
											.append("群聊警告/永久屏蔽(加黑)设置操作确认\n")
											.append("您将要对一群聊设置警告或永久屏蔽\n")
											.append("请确认以下信息:\n")
											.append("要警告/加黑的群聊:").append(arg2).append("\n")
											.append("本次警告/加黑次数:");
										switch (warnTimes)
										{
										case 1: //如果上次是第一次警告(本次为第二次)
											resultBuilder.append("第2次\n")
											.append("本次限制时长:3d\n");
											break;
										case 2: //如果上次是第二次警告(本次为第三次)
											resultBuilder.append("第3次\n")
											.append("本次限制时长:7d\n");
											break;
										case 3: //如果上次是第三次警告(本次为永久加黑)
											resultBuilder.append("[本次为永久加黑]\n")
											.append("本次限制时长:永久\n");
											break;
										}
										resultBuilder.append("若以上信息确认无误,请输入!cwarn [群号] yes {警告原因(仅警告时才会使用)}确认警告/加黑操作\n")
											.append("否则输入!cwarn [群号] no取消本操作.");
										// [end]
									CQ.sendPrivateMsg(qqId, resultBuilder.toString());
									return;
								}
					 }
					}
					File confirmSignFile = new File(Global.appDirectory + "/temp/warn_" + arg2 + ".confirm"); //新建确认标志文件实例
					IOHelper.WriteStr(confirmSignFile,"1");
					// [start] 设置返回信息字符串
					StringBuilder resultBuilder = new StringBuilder(FriendlyName).append("\n")
							.append("群聊警告/永久屏蔽(加黑)设置操作确认\n")
							.append("您将要对一群聊设置警告或永久屏蔽\n")
							.append("请确认以下信息:\n")
							.append("要警告/加黑的群聊:").append(arg2).append("\n")
							.append("本次警告/加黑次数:第1次\n")
							.append("限制解除时间:[本次警告不设置限制期]\n")
							.append("若以上信息确认无误,请输入!cwarn [群号] yes {警告原因(仅警告时才会使用)}确认警告/加黑操作\n")
							.append("否则输入!cwarn [群号] no取消本操作");
					// [end]
					CQ.sendPrivateMsg(qqId, resultBuilder.toString());
				} else { //如果群号不是数字
					throw new NumberFormatException("Group Number Argument is not a valid integer");
				}
			} catch (NumberFormatException e) { //数字格式异常捕获
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的不是有效的群号，请重新输入");
				CQ.logError(AppName, "发生错误,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
			} catch (IndexOutOfBoundsException e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n格式:!warn [群号]");
			} catch (Exception e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"处理失败(" + e.getClass().getName() + ")");
				CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
			}
			return;
		}
		/**
		 * 功能6-2:确认警告/加黑操作
		 * @param CQ CQ实例
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see #Func6_warnAbusedGroup(CoolQ, long, String)
		 */
		public static void Func6_2_confirmWarnAbusedGroup(CoolQ CQ,long qqId,String msg)
		{
			try {
				TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
				String arg2 = msg.split(" ", 4)[1]; //群号参数
				String arg3 = msg.split(" ", 4)[2].toLowerCase(); //是否处理参数(yes/no)
				String arg4 = ""; //警告原因参数
				if (msg.split(" ", 4).length == 4) { //如果参数数量是4个
					arg4 = msg.split(" ", 4)[3];
				}
				if (isInteger(arg2)) { //如果群号是数字
					File confirmSignFile = new File(Global.appDirectory + "/temp/warn_" + arg2 + ".confirm"); //新建确认标志文件实例
					if (confirmSignFile.exists()) { //如果标志文件存在
						switch (arg3) //判断是否处理
						{
						case "yes": //执行操作
							String newTime = IOHelper.ReadToEnd(confirmSignFile); //读取警告次数
							switch (newTime)
							{
							case "1": //如果是首次警告
									GlobalDatabases.dbgroup_list.executeNonQuerySync("INSERT OR REPLACE INTO AllGBanWarn ('GroupId','Status')" + 
											  " VALUES (" + arg2 + ",1,-1)");
								confirmSignFile.delete();
									StringBuilder groupWarnStr = new StringBuilder(FriendlyName).append("\n")
										.append("本群因违反《").append(Global.AppName).append(" 用户协议》，已被警告。\n");
									if (!arg4.equals("")) { //如果存在警告原因参数
										groupWarnStr.append("原因:").append(arg4).append("\n");
									}
									groupWarnStr.append("本次为第1次警告,您仍可以正常使用本机器人,但如果仍被发现存在违反用户协议的情况，将限制该群机器人的使用");
									CQ.sendGroupMsg(Long.parseLong(arg2),groupWarnStr.toString());
									CQ.sendPrivateMsg(qqId, FriendlyName + "\n已对下列群聊设置警告/加黑\n" + 
											"对应群号:" + arg2 + "\n" + 
											"本次警告的次数:第" + newTime + "次");
								break;
							case "2": case "3": //如果不是首次但是是警告
								Calendar unBanCalendar = Calendar.getInstance(); //获取当前时区Calendar实例
								switch (newTime)
								{
								case "2":
									unBanCalendar.add(Calendar.DATE, 3);
									break;
								case "3":
									unBanCalendar.add(Calendar.DATE, 7);
									break;
								}
								Date unBanDate = unBanCalendar.getTime(); //将Calendar实例转换成Date实例(限制解除时间实例)
								String unBanDateStr = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(unBanDate); //获取格式化的限制解除时间字符串
								GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE AllGBanWarn SET Status=" + newTime + ",WarnEndTime=" + unBanCalendar.getTimeInMillis() + " WHERE GroupId=" + arg2 + ";"); //写入警告群聊项
								confirmSignFile.delete();
									StringBuilder groupWarnStr1 = new StringBuilder(FriendlyName).append("\n")
										.append("本群因违反《").append(Global.AppName).append(" 用户协议》，已被警告。\n");
									if (!arg4.equals("")) { //如果存在警告原因参数
										groupWarnStr1.append("原因:").append(arg4).append("\n");
									}
									groupWarnStr1.append("本次为第");
									if (newTime.equals("2")) { //如果是第二次
										groupWarnStr1.append("2次警告,机器人将暂停工作至3天后\n");
									} else if (newTime.equals("3")) { //如果是第三次
										groupWarnStr1.append("3次警告,机器人将暂停工作到7天后\n");
									}
									groupWarnStr1.append("请勿违反协议使用机器人,否则本群可能会被永久屏蔽!");
									CQ.sendGroupMsg(Long.parseLong(arg2), groupWarnStr1.toString());
									CQ.sendPrivateMsg(qqId, FriendlyName + "\n已对下列群聊设置警告/加黑\n" + 
											"对应群号:" + arg2 + "\n" + 
											"本次警告的次数:第" + newTime + "次\n" + 
											"限制解除日期:" + unBanDateStr);
									break;
							case "forever": //如果是加黑
									GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE AllGBanWarn SET Status=-1 WHERE GroupId=" + arg2 + ";"); //写入警告群聊项
								confirmSignFile.delete();
								CQ.sendGroupMsg(Long.parseLong(arg2), FriendlyName + "\n" + 
										"本群因多次违反《" + Global.AppName + " 用户协议》,已被永久屏蔽\n请与机器人管理员联系");
								CQ.sendPrivateMsg(qqId, FriendlyName + "\n已对下列群聊设置警告/加黑\n" + 
										"对应群号:" + arg2 + "\n" + 
										"本次为加黑\n" + 
										"限制解除日期:[永久]");
								break;
							}
							break;
						case "no": //不执行操作
						default:
							confirmSignFile.delete();
							CQ.sendPrivateMsg(qqId, FriendlyName + "\n已取消群聊" + arg2 + "的警告/加黑操作");
							break;
						}
					} else { //如果标志文件不存在
						CQ.sendPrivateMsg(qqId, FriendlyName + "\n该群并未处于待确认警告/加黑操作状态");
					}
				} else { //如果群号不是数字
					throw new NumberFormatException("Group Number Argument is not valid integer");
				}
			} catch (NumberFormatException e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的不是有效的群号，请重新输入");
			} catch (IndexOutOfBoundsException e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n格式:!cwarn [群号] [yes/no] {警告原因}");
			} catch (Exception e) {
				CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + 
						"处理失败(" + e.getClass().getName() + ")");
				CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
			}
		}

		  /**
		   * 功能7:查看群聊当前状态
		   * 
		   * @param CQ   CQ实例
		   * @param qqId 消息来源QQ号
		   * @param msg  消息内容
		   */
		  public static void Func7_showAbusedStat(CoolQ CQ, long qqId, String msg)
		  {
				try {
					 TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区
					 String arg2 = msg.split(" ", 2)[1]; // 获取群号参数
					 if (isInteger(arg2)) { // 如果群号是数字
						  ResultSet resultSet = GlobalDatabases.dbgroup_list
									 .executeQuery("SELECT * FROM AllGBanWarn WHERE GroupId=" + arg2 + ";");
						  if (resultSet.next()) { // 如果黑名单群聊列表不为空
								int status = resultSet.getInt("Status");
								if (status == -1) { // 如果该群是黑名单群聊
									 StringBuilder banGroupResultBuilder = new StringBuilder(FriendlyName).append("\n")
												.append("群聊").append(arg2).append("状态\n").append("是否被警告/加黑:是\n")
												.append("状态:已被永久屏蔽(加黑)");
									 CQ.sendPrivateMsg(qqId, banGroupResultBuilder.toString());
									 return;
								} else if (status != 0) { // 如果该群是警告群聊
									 StringBuilder warnGroupResultBuilder = new StringBuilder(FriendlyName).append("\n")
												.append("群聊").append(arg2).append("状态\n").append("是否被警告/加黑:是\n").append("警告次数:第");
									 long warnTime = resultSet.getLong("Status"), // 获取该群当前被警告次数
												unBanTime = resultSet.getLong("WarnEndTime"); // 获取该群当前警告的限制解除时间戳
									 warnGroupResultBuilder.append(warnTime).append("次\n").append("是否处于限制状态:");
									 if ((unBanTime - Calendar.getInstance().getTimeInMillis()) > 0) { // 如果限制解除时间晚于当前系统时间
										  Calendar unBanCalendar = Calendar.getInstance();
										  unBanCalendar.setTimeInMillis(unBanTime);
										  String unBanString = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
													 .format(unBanCalendar.getTime());
										  warnGroupResultBuilder.append("是\n").append("限制解除日期:").append(unBanString);
									 } else {
										  warnGroupResultBuilder.append("否");
									 }
									 CQ.sendPrivateMsg(qqId, warnGroupResultBuilder.toString());
									 return;
								}
								CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + "群聊" + arg2 + "状态\n是否被警告/加黑:否");
								return;
						  } else {
								CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + "群聊" + arg2 + "状态\n是否被警告/加黑:否");
								return;
						  }
						  } else { // 如果群号不是数字
								throw new NumberFormatException("Group Number Argument is not valid");
						  }
				} catch (NumberFormatException e) {
					 CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + "您输入的不是有效的群号，请重新输入");
				} catch (IndexOutOfBoundsException e) {
					 CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + "您输入的指令格式有误,请重新输入\n格式:!gstat [群号]");
				} catch (Exception e) {
					 CQ.sendPrivateMsg(qqId, FriendlyName + "\n" + "处理失败(" + e.getClass().getName() + ")");
					 CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
				}
		  }

//		public static void FuncS_aprilFoolsDay_EasterEggGame_setAuth(CoolQ CQ,long qqId, String msg)
//		{
//			try {
//				String groupString = msg.split(" ", 3)[2];
//				if (isInteger(groupString)) {
//					File thisGroupFolder = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString);
//					if (thisGroupFolder.exists()) {
//						File thisGroupAuthFile = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString + "/authed.stat");
//						if (!thisGroupAuthFile.exists()) {
//							thisGroupAuthFile.createNewFile();
//							CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" + 
//									"对群聊" + groupString + "设置愚人节彩蛋游戏继续权限成功");
//							CQ.sendGroupMsg(Long.parseLong(groupString), ProcessGroupMsg.Part_Spec.randFriendlyName() + "\n" + 
//									"This group is authed just now!\n" + 
//									"Send '!4.1 auth' to confirm.");
//						} else {
//							CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" + 
//									"该群已经设置过权限了QwQ");
//						}
//					} else {
//						File thisGroupFinishedFile = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString + ".finished");
//						if (thisGroupFinishedFile.exists()) {
//							CQ.sendPrivateMsg(masterQQ,FriendlyName + "\n这群已经通关了愚人节彩蛋游戏哦~");
//						} else {
//							CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n这群还没有激活愚人节彩蛋呢~");
//						}
//					}
//				} else {
//					CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的群号不是有效群号,请重新输入");
//				}
//			}  catch (IndexOutOfBoundsException e) {
//				CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的指令格式有误,请检查后重新输入\n格式:!4.1 auth [群号]");
//			}catch (Exception e) {
//				CQ.logError(AppName, "发生异常，请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
//				CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n处理失败(" + e.getClass().getName() + ")");
//			}
//		}
//		
//		public static void FuncS_aprilFoolsDay_EasterEggGame_showStat(CoolQ CQ,long qqId,String msg)
//		{
//			try {
//				String groupString = msg.split(" ", 3)[2];
//				if (isInteger(groupString)) {
//					File thisGroupFolder = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString);
//					File thisGroupFinishFile = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString + ".finished");
//					StringBuilder statBuilder = new StringBuilder(FriendlyName).append("\n")
//							.append("群聊").append(groupString).append("愚人节彩蛋游戏状态\n")
//							.append("激活状态:");
//					if (thisGroupFolder.exists()) {
//						statBuilder.append("已激活");
//						File thisGroupProgressFile = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString + "/progress.txt");
//						File thisGroupNowPathFile = new File(Global.appDirectory + "/group/easteregg/4.1/" + groupString + "/nowpath.txt");
//						if (thisGroupProgressFile.exists()) {
//							statBuilder.append("\n").append("游玩进度:");
//							String progress = IOHelper.ReadToEnd(thisGroupProgressFile);
//							statBuilder.append(progress);
//							switch (progress)
//							{
//							case "beginning":
//							case "start1":
//							case "start2":
//							case "start3":
//								statBuilder.append("(起始部分)");
//								break;
//							case "1":
//								statBuilder.append("(start)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "2":
//								statBuilder.append("(Here is a folder!)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "3":
//								statBuilder.append("(Time-Chase)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "4":
//								statBuilder.append("(Anti-Virus Program is checking)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "5":
//								statBuilder.append("(Where was I!?)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "6":
//								statBuilder.append("(DANGEROUS VIRUS!?)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "7":
//								statBuilder.append("(Break the BitLocker)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "8":
//								statBuilder.append("(Online!)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "9":
//								statBuilder.append("(? & WARNING!!!(000.exe))");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "10":
//								statBuilder.append("(I got the auth!)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "11":
//								statBuilder.append("(Deleting 000.exe!)");
//								statBuilder.append("\n").append("当前路径:").append(IOHelper.ReadToEnd(thisGroupNowPathFile));
//								break;
//							case "ending":
//								statBuilder.append("(即将通关)");
//								break;
//							default:
//								break;
//							}
//						}
//					} else if (thisGroupFinishFile.exists()) {
//						statBuilder.append("已激活(已通关)");
//					} else {
//						statBuilder.append("未激活");
//					}
//					CQ.sendPrivateMsg(masterQQ, statBuilder.toString());
//				} else {
//					CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的群号不是有效群号,请重新输入");
//				}
//			} catch (IndexOutOfBoundsException e) {
//				CQ.sendPrivateMsg(qqId, FriendlyName + "\n您输入的指令格式有误,请检查后重新输入\n格式:!4.1 stat [群号]");
//			}catch (Exception e) {
//				CQ.logError(AppName, "发生异常，请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
//				CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n处理失败(" + e.getClass().getName() + ")");
//			}
//		}
	}
}