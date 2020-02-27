package cf.ots123it.open.sdubotr;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import com.sun.org.apache.bcel.internal.generic.NEW;

import cf.ots123it.jhlper.CommonHelper;
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
			/* 读取并写入成员发言次数(功能3-1) */
			File speakRanking = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId));
			// 获取今日日期（格式:yyyyMMdd)
			Date todayDate = Calendar.getInstance().getTime();
			String today = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(todayDate).split(" ",2)[0];
			File todaySpeakRanking = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId) + "/" + today);
			if (!speakRanking.exists()) { //如果群聊日发言排行榜数据目录不存在（功能3-1）
				speakRanking.mkdir();
			}
			if (!todaySpeakRanking.exists()) { //如果今日群聊日发言排行榜数据目录不存在（功能3-1）
				todaySpeakRanking.mkdir();
			}
			File todaySpeakPerson = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId) + "/" + today 
					+ "/" + String.valueOf(qqId));
			if (!todaySpeakPerson.exists()) { //如果对应成员今日群聊日发言记录文件不存在（功能3-1）
				todaySpeakPerson.createNewFile();
				IOHelper.WriteStr(todaySpeakPerson, "1"); //记录当前成员今日发言次数为1
			} else { //如果对应成员今日群聊日发言记录文件存在（功能3-1）
				long previousSpeakTimes = Long.parseLong(IOHelper.ReadToEnd(todaySpeakPerson)); //获取当前记录次数
				long nowSpeakTimes = previousSpeakTimes + 1; //在当前记录次数上+1
				IOHelper.WriteStr(todaySpeakPerson, String.valueOf(nowSpeakTimes)); //覆盖记录当前成员今日发言次数
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
				/* 主功能1:群管理核心功能 */
				case "mt": //功能1-1:禁言
					Part1.Func1_1(CQ, groupId, qqId, msg);
					break;
				case "um": //功能1-2:解禁
					Part1.Func1_2(CQ, groupId, qqId, msg);
					break;
				case "k": //功能1-3:踢人
					Part1.Func1_3(CQ, groupId, qqId, msg);
					break;
				case "fk": //功能1-4:永踢人（慎用）
					Part1.Func1_4(CQ, groupId, qqId, msg);
					break;
				/* 主功能3:群增强功能 */
				case "rk": //功能3-1:查看群成员日发言排行榜
					Part3.Func3_1(CQ, groupId, qqId, msg);
					break;
				/* 其它功能 */
				case "about": //功能O-1:关于
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					break;
				case "m": //功能O-2:功能菜单
					Part_Other.FuncO_Menu(CQ, groupId, qqId, msg);
					break;
				default:
					break;
				}
			}
			catch (NumberFormatException e) { //指令格式错误(1)
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
			catch (IndexOutOfBoundsException e) { //指令格式错误(2)
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
		} else {
			/* 本部分代码只是为了保留中文指令兼容性，请勿直接在此处增加新功能 */
			try {
				//获得所有参数组成的数组
				String[] arguments = msg.split(" ");
				//获得第一个参数
				String arg1 = arguments[0];
				switch (arg1) // //判断第一个参数
				{
				case "禁言": 
					Part1.Func1_1(CQ, groupId, qqId, msg);
					break;
				case "解禁":
					Part1.Func1_2(CQ, groupId, qqId, msg);
					break;
				case "踢":
					Part1.Func1_3(CQ, groupId, qqId, msg);
					break;
				case "永踢": 
					Part1.Func1_4(CQ, groupId, qqId, msg);
					break;
				case "成员活跃榜":
					Part3.Func3_1(CQ, groupId, qqId, msg);
					break;
				case "关于":
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					break;
				case "菜单":
				case "帮助":
				case "功能":
					Part_Other.FuncO_Menu(CQ, groupId, qqId, msg);
					break;
				default:
					break;
				}
			}
			catch (Exception e) { //指令格式错误(1)
				/* CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
			catch (IndexOutOfBoundsException e) { //指令格式错误(2)
				// CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:"); */
			}
		}
	}
	/**
	 * 主功能1:群管理核心功能
	 * @author 御坂12456
	 *
	 */
	static class Part1{
		/**
		 * 功能1-1:禁言
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 */
		public static void Func1_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被禁言的成员QQ号或at）
				String arg3 = msg.split(" ", 3)[2]; //获取参数3（要禁言的时长(单位:分钟)）
				long muteQQ = 0, muteDuration = 0; //定义要被禁言的QQ号和禁言时长变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					muteQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						muteQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if ((CommonHelper.isInteger(arg3)) && (Integer.valueOf(arg3) >= 1) && (Integer.valueOf(arg3) <= 43200)) { //如果禁言时长为数字且范围在1-43200之间
					if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
						if(CQ.getGroupMemberInfo(groupId, muteQQ) != null) //如果对应成员在群内
						{
							if (!isGroupAdmin(CQ, groupId, muteQQ)) { //如果要被禁言的对象不是管理组成员
								if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
									if (muteQQ == CQ.getLoginQQ()) { // 如果试图禁言机器人QQ
										CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
												"我禁言我自己！？(402)");
									} else { //否则（被禁言对象不是机器人QQ）
										muteDuration = Long.parseLong(arg3); //将禁言时长字符串转成长整型
										CQ.setGroupBan(groupId, muteQQ,muteDuration * 60); //执行禁言操作
										CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
												"处理完成(200)");
									}
								} else { //否则（指令执行者不是管理组成员）
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"出现了！越权操作！(403)");
								}
							} else { //否则（被禁言的对象是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"本是同管理组人，相禁何太急orz(403)");
							}
						} else { //否则（对应成员不在群内）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"这人没在群里啊QwQ(302)");
						}

					} else { //否则（机器人不是管理组成员）
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
								"没这个权限QAQ(401)");
					}
				} else { //否则（禁言时长不是数字或范围不在1-43200内）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" +
							"您输入了无效的禁言时长,请更正后重试(301)\n" + 
							"时长范围(分钟): 1 - 43200\n" + 
							"常用禁言时长:\n" +
							"1小时-60 1天-1440\n" + 
							"30天-43200(QQ手机版极限:43199)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
							"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  
			catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-2:解禁
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 */
		public static void Func1_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被解禁的成员QQ号或at）
				long unMuteQQ = 0; //定义要被解禁的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					unMuteQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						unMuteQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, unMuteQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, unMuteQQ)) { //如果要被禁言的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (unMuteQQ == CQ.getLoginQQ()) { // 如果试图解禁机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"解禁我自己，这……(402)");
								} else { //否则（被解禁对象不是机器人QQ）
									CQ.setGroupBan(groupId, unMuteQQ,0); //执行解禁操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"处理完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被解禁的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"喂喂，要是管理被禁言就别找我了吧(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}

				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!um [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-3:踢人
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 */
		public static void Func1_3(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被踢的成员QQ号或at）
				long kickedQQ = 0; //定义要被踢的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					kickedQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						kickedQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
									"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, kickedQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, kickedQQ)) { //如果要被踢的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (kickedQQ == CQ.getLoginQQ()) { // 如果试图踢掉机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"要是看我不顺眼的话直接手动踢了我吧QAQ(402)");
								} else { //否则（被踢对象不是机器人QQ）
									CQ.setGroupKick(groupId, kickedQQ, false); //执行踢出操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"处理完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被踢的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"别踢管理组成员啊，都是一家人orz(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}
				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!k [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

		/**
		 * 功能1-4:永踢人（慎用）
		 * @author 御坂12456
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 */
		public static void Func1_4(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1]; //获取参数2（要被踢的成员QQ号或at）
				long foreverKickedQQ = 0; //定义要被踢的QQ号变量
				if (arg2.startsWith("[")) 
				{ //如果是at
					foreverKickedQQ = getCQAt(arg2); //读取at的QQ号
				} else { //否则
					if (CommonHelper.isInteger(arg2)) { //如果QQ号是数字
						foreverKickedQQ = Long.parseLong(arg2); //直接读取输入的QQ号
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n"  + 
								"您输入的QQ号不合法，请重新输入(301)");
						return; //直接返回
					}
				}
				if (isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
					if(CQ.getGroupMemberInfo(groupId, foreverKickedQQ) != null) //如果对应成员在群内
					{
						if (!isGroupAdmin(CQ, groupId, foreverKickedQQ)) { //如果要被踢的对象不是管理组成员
							if (isGroupAdmin(CQ, groupId, qqId)) { //如果指令执行者是管理组成员
								if (foreverKickedQQ == CQ.getLoginQQ()) { // 如果试图踢掉机器人QQ
									CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
											"求求不要永踢我！如果实在看不惯就直接手动踢了吧QAQ(402)");
								} else { //否则（被踢对象不是机器人QQ）
									CQ.setGroupKick(groupId, foreverKickedQQ, true); //执行永踢操作
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"处理完成(200)");
								}
							} else { //否则（指令执行者不是管理组成员）
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"出现了！越权操作！(403)");
							}
						} else { //否则（被踢的对象是管理组成员）
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"别踢管理组成员啊，都是一家人orz(403)");
						}
					} else { //否则（对应成员不在群内）
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"这人没在群里啊QwQ(302)");
					}
				} else { //否则（机器人不是管理组成员）
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"没这个权限QAQ(401)");
				}
			} catch (IndexOutOfBoundsException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!fk [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试(注意参数间只能存在一个空格)\n" + 
						"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			}  catch (Exception e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //最终返回
			}
		}

	}

	/**
	 * 主功能2:群管理辅助功能
	 * @author 御坂12456
	 *
	 */
	static class Part2
	{
		/**
		 * 功能2-1:特别监视违禁词提醒功能
		 * @param CQ CQ实例，详见本大类注释
		 * @param msgId 消息id
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456(优化: Sugar 404)
		 */
		public static void Func2_1(CoolQ CQ,int msgId,long groupId,long qqId,String msg)
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
				String handleStat = "撤回+禁言(1h)处理状态:";
				int result = CQ.deleteMsg(msgId); //尝试撤回消息
				if (result < 0) { //撤回失败
					handleStat += "失败(" + result + ")";
				} else { //撤回成功
					int result2 = CQ.setGroupBan(groupId, qqId, 3600L); //尝试禁言1h
					if (result2 < 0) { //禁言失败
						handleStat += "撤回成功,禁言失败(" + result2 + ")";
					} else { //禁言成功
						handleStat += "成功";
					}
				}
				b.append(Global.FriendlyName).append("\n检测到有人发布违禁词，请尽快查看\n来源群号:")
				.append(Global.getGroupName(CQ, groupId)).append('(').append(groupId).append(")\n来源QQ:")
				.append(CQ.getGroupMemberInfo(groupId, qqId).getNick()).append('(').append(qqId)
				.append(")\n检测到的违禁词:");
				for (String iMGBanString:bans) {
					b.append(iMGBanString).append('.');
				}
				b.append(handleStat)
				.append("\n完整消息内容:\n").append(msg);
				CQ.sendPrivateMsg(Global.masterQQ,b.toString());
			}
			return;
		}

	}

	/**
	 * 主功能3:群增强功能
	 * @author 御坂12456
	 *
	 */
	static class Part3{
		/**
		 * 功能3-1:查看群日发言排行榜
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func3_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				// 获取群聊日发言排行榜数据目录
				File speakRanking = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId));
				if (speakRanking.exists()) { //如果群聊日发言排行榜数据目录存在
					System.gc(); //执行垃圾收集器
					// 获取今日日期（格式:yyyyMMdd)
					Date todayDate = Calendar.getInstance().getTime();
					String today = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(todayDate).split(" ",2)[0];
					CQ.logDebug(Global.AppName, "今日日期:" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(todayDate));
					// 获取今日的群聊日发言排行榜数据目录
					File todaySpeakRanking = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId) + "/" + today);
					if (todaySpeakRanking.exists()) { //如果今日的数据目录存在
						String[] todaySpeakPersons = todaySpeakRanking.list(); //获取今日发过言的所有成员QQ号字符串（数据目录中的所有发言条数记录文件的文件名）
						if (todaySpeakPersons.length != 0) { //如果今日有人发过言（数据目录中有记录文件）
							String[] todaySpeakCounts = new String[todaySpeakPersons.length]; //定义今日发言次数数组
							for (int i = 0; i < todaySpeakCounts.length; i++) {
								//将今日发言数组中的索引为i的值赋值为"记录文件名" + "," + "记录文件内容(发言次数)"
								todaySpeakCounts[i] = todaySpeakPersons[i] + "," + IOHelper.ReadToEnd(todaySpeakRanking.getAbsolutePath() + "/" + todaySpeakPersons[i]);
							}
							 	//通过匿名的Comparator使数组按照每个值中","后面的数字的顺序进行数组降序排序
							Arrays.sort(todaySpeakCounts, new Comparator<String>() {
								@Override
								public int compare(String o1, String o2)
								{
									/* 你家JVM不判断null心里难受(java.lang.IllegalArgumentException) */
									if (o1 == null)
									{
										return 1;
									} else if (o2 == null) {
										return -1;
									} else
									{
										//进行反向排序
										switch (Long.compare(Long.parseLong(o1.substring(o1.indexOf(",") +1)),(Long.parseLong((o2.substring(o2.indexOf(",") + 1))))))
										{
										case 1: 
											return -1;
										case -1:
											return 1;
										case 0:
											return 0;
										default:
											return 0;
										}
									}
								}
							});
							StringBuilder todaySpeakRankingStr = new StringBuilder();
							todaySpeakRankingStr.append(FriendlyName).append("\n")
															 .append("群成员日发言排行榜(").append(groupId).append(")").append("\n");
							//定义for循环(初始i=0,当i小于今日发言人数且当i小于10（排行榜仅显示top10时就循环）
							for (int i = 0; ((i < todaySpeakCounts.length) && (i < 10)); i++) {
								//定义当前遍历到的QQ号
								long currentSpeakQQNo = Long.parseLong(todaySpeakCounts[i].split(",", 2)[0]);
								//定义当前遍历到的QQ号的发言次数
								long currentSpeakTimes = Long.parseLong(todaySpeakCounts[i].split(",", 2)[1]);
								//定义当前遍历到的QQ号的昵称
								String currentSpeakQQNick;
								Member currentSpeakQQ = CQ.getGroupMemberInfo(groupId, currentSpeakQQNo,true);
								if (currentSpeakQQ != null) //如果获取成功（成员在群内）
								{
									if (!currentSpeakQQ.getCard().equals("")) //如果成员群内昵称不为空
									{
										currentSpeakQQNick = currentSpeakQQ.getCard();
									} else { //否则（成员群内昵称为空）
										currentSpeakQQNick = currentSpeakQQ.getNick();
									}
								} else { //否则（成员已不在群内）
									currentSpeakQQNick = "*已退出群员";
								}
								if (i == (todaySpeakCounts.length - 1)) { //如果i等于今日发言人数-1（最后一个发言人）
									todaySpeakRankingStr.append("[").append(i + 1).append("]").append(currentSpeakQQNick).append(":").append(currentSpeakTimes).append("条");
								} else if (i == 9) //否则如果i等于9（top10最后一名(Rank:10)）
								{
									todaySpeakRankingStr.append("[").append(i + 1).append("]").append(currentSpeakQQNick).append(":").append(currentSpeakTimes).append("条");
								} else if (i == 0) //否则如果i等于0（龙王(Rank:1)）
								{
									todaySpeakRankingStr.append("[").append("1/龙王").append("]").append(currentSpeakQQNick).append(":").append(currentSpeakTimes).append("条").append("\n");
								}else  //否则
								{
									todaySpeakRankingStr.append("[").append(i + 1).append("]").append(currentSpeakQQNick).append(":").append(currentSpeakTimes).append("条").append("\n");
								}
								
							}
							//定义消息发送人员的发言次数和发言名次
							long mySpeakTimes = 0,mySpeakNo = 9999;
							//定义消息发送人员的昵称
							String mySpeakQQNick;
							Member mySpeakQQ = CQ.getGroupMemberInfo(groupId, qqId,true);
							if (mySpeakQQ != null) //如果获取成功（成员在群内）
							{
								if (!mySpeakQQ.getCard().equals("")) //如果成员群内昵称不为空
								{
									mySpeakQQNick = mySpeakQQ.getCard();
								} else { //否则（成员群内昵称为空）
									mySpeakQQNick = mySpeakQQ.getNick();
								}
							} else {
									mySpeakQQNick = "未知昵称";
							}
							//再次遍历循环，得到消息发送人员的发言次数
							long i = 1; //定义循环次数i
							for (String todayMySpeakCount : todaySpeakCounts) {
								if (todayMySpeakCount.split(",", 2)[0].equals(String.valueOf(qqId))) { //如果todayMySpeakCount中的前半部分（QQ号）为消息发送者QQ号
									mySpeakTimes = Long.parseLong(todayMySpeakCount.split(",",2)[1]);
									mySpeakNo = i;
									break; //跳出循环
								} else { //否则
									i++; //循环次数+1
									continue; //进行下一次循环
								}
							}
							todaySpeakRankingStr.append("\n").append("----------------").append("\n").append("[").append(mySpeakNo).append("]").append(mySpeakQQNick).append(":")
											.append(mySpeakTimes).append("条");
							System.gc(); //执行垃圾收集器
							CQ.sendGroupMsg(groupId, todaySpeakRankingStr.toString()); //发送群成员日发言排行榜
						} else { //否则
							CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
									"今日本群的群聊日发言排行榜为空~");
						}
					} else { //否则
						todaySpeakRanking.mkdir(); //创建
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
									"今日本群的群聊日发言排行榜为空~");
					}
				} else { //如果群聊日发言排行榜数据目录不存在
					speakRanking.mkdir(); //创建
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
								"今日本群的群聊日发言排行榜为空~");
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
						"获取失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
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
			 * 版本 [版本]
			 * 当前登录账号:[昵称]([QQ])
			 */
			aboutStrBuilder.append("123 SduBotR\n")
			.append("版本 ").append(Global.Version).append("\n")
			.append("当前登录账号:").append(CQ.getLoginNick()).append("(").append(String.valueOf(CQ.getLoginQQ())).append(")");
			// 发送消息
			CQ.sendGroupMsg(groupId, aboutStrBuilder.toString());
			return;
		}
		/**
		 * 功能O-2:功能菜单
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void FuncO_Menu(CoolQ CQ,long groupId,long qqId,String msg)
		{
			int result = CQ.sendPrivateMsg(qqId, menuStr); //私聊发送功能菜单
			switch (result) //判断功能菜单发送结果
			{
			case -36: //群主禁止临时会话
				CQ.sendGroupMsg(groupId,FriendlyName + "\n" + 
						"群主设置禁止临时会话了……去https://github.com/Misaka12456/123SduBotR/blob/master/README.md看菜单吧(501:-36)");
				break;
			case -35: //权限不足，可能解除了与对方的好友关系
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
						"bot被屏蔽了呀……怎么发功能菜单啊QAQ(500:-35)");
				break;
			case -30: //消息被服务器拒绝
				CQ.sendGroupMsg(groupId,FriendlyName + "\n" + 
						"TX拒绝了bot发送消息的请求……我也没办法啊(500:-30)");
				break;
			default: //其它情况
				if (String.valueOf(result).startsWith("-")) { //发送消息失败，未知原因
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"TX又炸了，功能发不出去orz(500:" + String.valueOf(result) + ")");
				} else {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
						"功能菜单已发送至私聊(若接收不到请尝试重新发送指令)");
				}
				break;
			}
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
			} else if ((k > 950)) //如果k大于950
			{
				CQ.sendGroupMsg(groupId, funnyStrings[6]);  //发送滑稽数组第7个消息
			}
			System.gc(); //执行垃圾收集器
			return;
		}
	
		
	}
}
