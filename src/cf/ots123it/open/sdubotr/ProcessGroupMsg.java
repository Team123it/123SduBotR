package cf.ots123it.open.sdubotr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.meowy.cqp.jcq.message.CQCode;

import com.alibaba.fastjson.JSONObject;

import cf.ots123it.jhlper.CommonHelper;
import cf.ots123it.jhlper.ExceptionHelper;
import cf.ots123it.jhlper.IOHelper;
import cf.ots123it.jhlper.JsonHelper;
import cf.ots123it.jhlper.OTPHelper;
import cf.ots123it.open.sdubotr.Utils.ListFileException;
import cf.ots123it.open.sdubotr.Utils.ListFileHelper;

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
			File abuseDataFolder = new File(Global.appDirectory + "/protect/group/abuse/" + groupId);
			if (!abuseDataFolder.exists()) {
				abuseDataFolder.mkdir();
			}
			// [start] 读取并写入成员发言次数(功能3-1)
			File speakRanking = new File(Global.appDirectory + "/group/ranking/speaking/" + String.valueOf(groupId));
			// 设置时区
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
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
			// [end]
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
				switch (arg1.toLowerCase()) // //判断第一个参数
				{
				/* 主功能1:群管理核心功能 */
				case "mt": //功能1-1:禁言
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "um": //功能1-2:解禁
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "k": //功能1-3:踢人
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_3(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "fk": //功能1-4:永踢人（慎用）
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_4(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "blist" : //主功能1-5:群聊黑名单功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Part1_5.Func1_5_main(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 主功能3:群增强功能 */
				case "rk": //功能3-1:查看群成员日发言排行榜
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 主功能4:实用功能 */
				case "cov": //功能4-1:查看新冠肺炎(SARS-Cov-2)疫情实时数据
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part4.Func4_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "bf": //功能4-2:Bilibili实时粉丝数据
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part4.Func4_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 其它功能 */
				case "about": //功能O-1:关于
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "m":  case "help": //功能O-2:功能菜单
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part_Other.FuncO_Menu(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "uab": //功能O-3:解除滥用状态
					Part_Other.FuncO_UnAbuse(CQ, groupId, qqId, msg);
					break;
				case "rpt": //功能O-4:反馈
					Part_Other.FuncO_Report(CQ, groupId, qqId, msg);
					break;
				default:
					break;
				}
			}
			catch (NumberFormatException e) { //指令格式错误(1)
				if((msg.trim().equals("!")) || (msg.trim().equals("！"))) return;
				CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
							"您输入的指令:");
			}
			catch (IndexOutOfBoundsException e) { //指令格式错误(2)
				if((msg.trim().equals("!")) || (msg.trim().equals("！"))) return;
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
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "解禁":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "踢":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_3(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "永踢": 
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Func1_4(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "成员活跃榜":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "疫情":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part4.Func4_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "关于":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part_Other.FuncO_About(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "菜单":
				case "帮助":
				case "功能":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part_Other.FuncO_Menu(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
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
		@SuppressWarnings("finally")
		public static void Func1_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 3)[1].trim(); //获取参数2（要被禁言的成员QQ号或at）
				String arg3 = msg.split(" ", 3)[2].trim(); //获取参数3（要禁言的时长(单位:分钟)）
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
							"您输入的指令格式有误,请更正后重试\n" + 
							"格式:!mt [QQ号/at] [时长(单位:分钟)]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试\n" + 
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
				String arg2 = msg.split(" ", 2)[1].trim(); //获取参数2（要被解禁的成员QQ号或at）
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
						"您输入的指令格式有误,请更正后重试\n" + 
						"格式:!um [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试\n" + 
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
				String arg2 = msg.split(" ", 2)[1].trim(); //获取参数2（要被踢的成员QQ号或at）
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
						"您输入的指令格式有误,请更正后重试\n" + 
						"格式:!k [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试\n" + 
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
				String arg2 = msg.split(" ", 2)[1].trim(); //获取参数2（要被踢的成员QQ号或at）
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
						"您输入的指令格式有误,请更正后重试\n" + 
						"格式:!fk [QQ号/at]");
			} catch (NumberFormatException e) { //若发生数组下标越界异常
				CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请更正后重试\n" + 
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
		 * 功能1-5:群聊黑名单(请与功能M-4区分开)
		 * @author 御坂12456
		 *
		 */
		static class Part1_5{
			
			public static void Func1_5_main(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					String[] arguments = msg.split(" ", 3); //获取参数（格式类似于"blist add 12345"）
					String arg2 = arguments[1].toLowerCase(); //获取第2个参数（下标为1）（类似于"add"）
					switch (arg2)
					{
					case "start": //功能1-5-1:启动黑名单（!blist start）
						Func1_5_1(CQ, groupId, qqId, msg);
						break;
					case "stop": //功能1-5-2:关闭黑名单（!blist stop）
						Func1_5_2(CQ, groupId, qqId, msg);
						break;
					case "add": //功能1-5-3:添加黑名单成员（!blist add...）
						Func1_5_3(CQ, groupId, qqId, msg);
						break;
					case "del": //功能1-5-4:移除黑名单成员（!blist del...）
						Func1_5_4(CQ, groupId, qqId, msg);
						break;
					case "show": //功能1-5-5:查看本群黑名单（!blist show）
						Func1_5_5(CQ, groupId, qqId, msg);
						break;
					case "cnp": //功能1-5-6:切换黑名单成员入群拒绝提醒状态（!blist cnp）
						Func1_5_6(CQ, groupId, qqId, msg);
						break;
					default: //不存在的黑名单参数2
						throw new IndexOutOfBoundsException("Unknown Func1-5(Blist) Argument 2:" + arg2);
					}
					return;
				}
				catch (IndexOutOfBoundsException e) { //指令格式错误
					CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
								"格式:!blist [start/stop/add/del/cnp]...");
				}
			
			}
			
			/**
			 * 功能1-5-1:启动群聊黑名单
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_1(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (!blistFolder.exists()) { //如果黑名单未开启（文件夹不存在）
								blistFolder.mkdir();
								File blistFile = new File(Global.appDirectory + "/group/blist/" + groupId + "/persons.txt");
								blistFile.createNewFile();
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单已开启");
								return;
							} else { //如果黑名单已开启
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单已是开启状态,您可以进行如下操作:\n" + 
										"输入!blist show查看本群黑名单\n" + 
										"输入!blist cnp切换黑名单成员入群拒绝是否提醒的状态\n" + 
										"输入!blist stop关闭本群黑名单。您将会丢失整个黑名单成员列表数据。");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试:\n" + 
							"格式:!blist start");
				} catch (IOException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"启动失败(java.io.IOException)");
				}
			}
			/**
			 * 功能1-5-2:关闭群聊黑名单
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_2(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (blistFolder.exists()) { //如果黑名单未关闭（文件夹存在）
								IOHelper.DeleteAllFiles(blistFolder); //删除该群黑名单文件夹
								blistFolder.delete();
								CQ.sendGroupMsg(groupId,FriendlyName + "\n" + 
										"本群黑名单已关闭,黑名单列表已清空。");
							} else { //如果黑名单已关闭
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单已是关闭状态,您可以进行如下操作:\n" + 
										"输入!blist start启动本群黑名单");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试:\n" + 
							"格式:!blist stop");
				}
			}
			/**
			 * 功能1-5-3:添加群聊黑名单成员
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_3(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (blistFolder.exists()) { //如果黑名单是开启状态（文件夹存在）
								String[] gotStr = msg.split(" ", 3)[2].trim().split(" ");
								if (gotStr.toString().equals("[]")) { //如果要添加的人员为空
									throw new IndexOutOfBoundsException("添加人员为空");
								} else { //如果要添加的人员不为空
									ArrayList<String> readyToAddStr = new ArrayList<String>(); //新建保存列表的ArrayList
									int failCount = 0; //记录失败添加的人员数
									for (String readyToAddSingleStr : gotStr) { //循环检查要添加的QQ号数组
										if (readyToAddSingleStr.startsWith("[CQ:at,")) { //如果是at
											String trueQQ = Long.valueOf(getCQAt(readyToAddSingleStr.trim())).toString();
											if (trueQQ.equals("-1000")) { //如果是全体成员
												continue; //直接循环到下一个
											} else {
												readyToAddStr.add(trueQQ); //添加成员
											}
										} else if (!CommonHelper.isInteger(readyToAddSingleStr)) { //如果数组中某一项不是整数
											continue; //直接循环到下一个
										} else { //如果是整数
											String trueQQ = readyToAddSingleStr;
											readyToAddStr.add(trueQQ); //添加成员
										}
									}
									int succCount = readyToAddStr.size(); //记录成功添加的人员数
									ListFileHelper bListFile = new ListFileHelper(Global.appDirectory + "/group/blist/" + groupId + "/persons.txt"); //新建黑名单列表文件实例
									for (String readyToAddSingleStr : readyToAddStr) { //遍历每个人员
										switch (bListFile.add(readyToAddSingleStr)) //执行添加操作并获取+判断处理状态（返回值）
										{
										case 0: //成功
											continue; //进行下一次循环
										case 1: //人员重复
										case -1: //失败
											succCount--; //成功次数-1
											failCount++; //失败次数+1
											continue; //进行下一次循环
										}
									}
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
											"命令成功完成.\n" + 
											"成功:" + succCount + " 失败:" + failCount);
									return;
								}
							} else { //如果黑名单未开启
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单未开启，请输入!blist start开启");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试(注意指令间只能存在一个空格):\n" + 
							"格式:!blist add [QQ号/at] {QQ号/at...}");
				} catch (ListFileException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"添加失败");
				}
			}
			/**
			 * 功能1-5-4:移除群聊黑名单成员
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_4(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (blistFolder.exists()) { //如果黑名单是开启状态（文件夹存在）
								String[] gotStr = msg.split(" ", 3)[2].trim().split(" ");
								if (gotStr.equals(null)) { //如果要移除的人员为空
									throw new IndexOutOfBoundsException("添加人员为空");
								} else { //如果要移除的人员不为空
									ArrayList<String> readyToDelStr = new ArrayList<String>(); //新建保存列表的ArrayList
									int failCount = 0; //记录失败移除的人员数
									for (String readyToDelSingleStr : gotStr) { //循环检查要移除的QQ号数组
										if (readyToDelSingleStr.startsWith("[CQ:at,")) { //如果是at
											String trueQQ = Long.valueOf(getCQAt(readyToDelSingleStr.trim())).toString();
											if (trueQQ.equals("-1000")) { //如果是全体成员
												continue; //直接循环到下一个
											} else {
												readyToDelStr.add(trueQQ); //移除成员
											}
										} else if (!CommonHelper.isInteger(readyToDelSingleStr)) { //如果数组中某一项不是整数
											continue; //直接循环到下一个
										} else { //如果是整数
											String trueQQ = readyToDelSingleStr;
											readyToDelStr.add(trueQQ); //添加成员
										}
									}
									int succCount = readyToDelStr.size(); //记录成功移除的人员数
									ListFileHelper bListFile = new ListFileHelper(Global.appDirectory + "/group/blist/" + groupId + "/persons.txt"); //新建黑名单列表文件实例
									for (String readyToDelSingleStr : readyToDelStr) { //遍历每个人员
										switch (bListFile.remove(readyToDelSingleStr)) //执行移除操作并获取+判断处理状态（返回值）
										{
										case 0: //成功
											continue; //进行下一次循环
										case 2: //列表文件为空或者不存在
											throw new ListFileException("File cannot be found!");
										case 1: //人员不存在
										case -1: //失败
											succCount--; //成功次数-1
											failCount++; //失败次数+1
											continue; //进行下一次循环
										}
									}
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
											"命令成功完成.\n" + 
											"成功:" + succCount + " 失败:" + failCount);
									return;
								}
							} else { //如果黑名单未开启
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单未开启，请输入!blist start开启");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试(注意指令间只能存在一个空格):\n" + 
							"格式:!blist del [QQ号/at] {QQ号/at...}");
				} catch (ListFileException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"移除失败");
				}
			}
			/**
			 * 功能1-5-5:查看群聊黑名单
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_5(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (blistFolder.exists()) { //如果黑名单是开启状态（文件夹存在）
									ListFileHelper bListFile = new ListFileHelper(Global.appDirectory + "/group/blist/" + groupId + "/persons.txt"); //新建黑名单列表文件实例
									ArrayList<String> returnList = bListFile.getList(); // 获取返回的列表
									if (returnList.equals(null)) { //如果返回的列表为null
										throw new ListFileException("返回列表为null");
									}
									StringBuilder returnBuilder = new StringBuilder(FriendlyName).append("\n")
											.append("群:").append(getGroupName(CQ, groupId)).append("(").append(groupId).append(")黑名单\n")
											.append("人员总数:").append(returnList.size()).append("\n");
									// 定义要发送的消息的列表字符串
									Iterator<String> returnIt = returnList.iterator(); //获取返回列表的迭代器
									while (returnIt.hasNext()) { //如果迭代器中存在下一个项
										String nextPerson = returnIt.next(); //获取下一个项
										if (returnIt.hasNext()) { //如果迭代器中仍有下一项
											returnBuilder.append(nextPerson).append("\n"); //添加下一项+回车符到发送消息字符串中
										} else { //如果迭代器没有下一项了（最后一项了）
											returnBuilder.append(nextPerson); //添加最后一项到发送信息字符串中
										}
									}
									CQ.sendPrivateMsg(qqId, returnBuilder.toString()); //私聊发送黑名单
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +  
											"由于防止在群内刷屏，本群黑名单已私聊发送给指定人员，请查收(如未收到私聊消息请检查本群是否允许临时会话后重试)");
									return;
							} else { //如果黑名单未开启
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单未开启，请输入!blist start开启");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试(注意指令间只能存在一个空格):\n" + 
							"格式:!blist show");
				} catch (ListFileException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"读取失败");
				} catch (NullPointerException e) {
					StringBuilder returnBuilder = new StringBuilder(FriendlyName).append("\n")
							.append("群:").append(getGroupName(CQ, groupId)).append("(").append(groupId).append(")黑名单\n")
							.append("人员总数:0");
					CQ.sendPrivateMsg(qqId, returnBuilder.toString()); //私聊发送黑名单
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +  
							"由于防止在群内刷屏，本群黑名单已私聊发送给指定人员，请查收(如未收到私聊消息请检查本群是否允许临时会话后重试)");
				}
			}
			/**
			 * 功能1-5-6:切换黑名单成员入群拒绝提醒状态
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_6(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							File blistFolder = new File(Global.appDirectory + "/group/blist/" + groupId); //定义本群黑名单数据文件夹
							if (blistFolder.exists()) { //如果黑名单是开启状态（文件夹存在）
								File statFile = new File(Global.appDirectory + "/group/blist/" + groupId + "/noPrompt.stat"); //定义不提醒标志文件
								if (!statFile.exists()) { //如果标志文件不存在
									statFile.createNewFile(); //创建标志文件
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"已切换成:拒绝后不提醒");
									return;
								} else { //如果标志文件存在
									statFile.delete(); //删除标志文件
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"已切换成:拒绝后提醒");
									return;
								}
							} else { //如果黑名单未开启
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
										"本群黑名单未开启，请输入!blist start开启");
								return;
							}
						} else { //如果机器人不是管理组成员
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"请先将机器人设置成为本群管理员后再进行操作(401)");
							return;
						}
					}
					else { //如果消息发送人员不是管理组成员
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"您没有权限执行该操作(403)");
					}
				} catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
							"您输入的指令格式有误，请更正后重试(注意指令间只能存在一个空格):\n" + 
							"格式:!blist cnp");
				} catch (IOException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"设置失败");
				}
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
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n检测到违禁词,已撤回");
				}
				b.append(Global.FriendlyName).append("\n检测到有人发布违禁词，请尽快查看\n来源群号:")
				.append(Global.getGroupName(CQ, groupId)).append('(').append(groupId).append(")\n来源QQ:")
				.append(CQ.getGroupMemberInfo(groupId, qqId).getNick()).append('(').append(qqId)
				.append(")\n检测到的违禁词:");
				for (String iMGBanString:bans) {
					b.append(iMGBanString).append('.');
				}
				b.append("\n").append(handleStat)
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
					TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
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
	
	static class Part4{
		/**
		 * 功能4-1:获取新冠肺炎(SARS-Cov-2)疫情实时数据(数据来源:丁香园/丁香医生)
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func4_1(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
			if ((msg.trim().equals("cov")) || (msg.trim().equals("疫情"))) { //如果无参数(查询全国最新数据)
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
						"正在从丁香园获取数据,请稍候");
				JSONObject resultJson = JSONObject.parseObject(JsonHelper.loadJson("http://lab.isaaclin.cn/nCoV/api/overall?latest=1"));
				// 获取全国数据
				JSONObject results = JSONObject.parseObject(resultJson.getString("results").replace("[", "").replace("]", ""));
				// 累计确诊人数
				int confirmedCount = results.getIntValue("confirmedCount");
				// 累计确诊人数变化量
				String confirmedIncr;
				// 添加正负号
				if (results.getIntValue("confirmedIncr") > 0) {
					confirmedIncr = "+" + results.getIntValue("confirmedIncr");
				} else {
					confirmedIncr = String.valueOf(results.getIntValue("confirmedIncr"));
				}
				// 疑似感染人数
				int suspectedCount = results.getIntValue("suspectedCount");
				// 疑似感染人数变化量
				String suspectedIncr;
				// 添加正负号
				if (results.getIntValue("suspectedIncr") > 0) {
					suspectedIncr = "+" + results.getIntValue("suspectedIncr");
				} else {
					suspectedIncr = String.valueOf(results.getIntValue("suspectedIncr"));
				}
				// 治愈人数
				int curedCount = results.getIntValue("curedCount");
				// 治愈人数变化量
				String curedIncr;
				// 添加正负号
				if (results.getIntValue("curedIncr") > 0) {
					curedIncr = "+" + results.getIntValue("curedIncr");
				} else {
					curedIncr = String.valueOf(results.getIntValue("curedIncr"));
				}
				// 死亡人数
				int deadCount = results.getIntValue("deadCount");
				// 死亡人数变化量
				String deadIncr;
				// 添加正负号
				if (results.getIntValue("deadIncr") > 0) {
					deadIncr = "+" + results.getIntValue("deadIncr");
				} else {
					deadIncr = String.valueOf(results.getIntValue("deadIncr"));
				}
				// 数据更新时间
				Calendar updateCalendar = Calendar.getInstance();
				updateCalendar.setTime(new Date(results.getLongValue("updateTime")));
				String updateTime = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(updateCalendar.getTime());
				// 定义数据字符串
				StringBuilder resultBuilder = new StringBuilder()
						.append(Global.FriendlyName).append("\n")
						.append("全国新冠肺炎疫情实时数据\n")
						.append("数据更新于").append(updateTime).append("\n")
						.append("累计确诊病例:").append(confirmedCount).append("(").append(confirmedIncr).append(")").append("\n")
						.append("疑似病例:").append(suspectedCount).append("(").append(suspectedIncr).append(")").append("\n")
						.append("已治愈出院病例:").append(curedCount).append("(").append(curedIncr).append(")").append("\n")
						.append("死亡病例:").append(deadCount).append("(").append(deadIncr).append(")");
				CQ.sendGroupMsg(groupId, resultBuilder.toString());
			}
			else { //如果有参数
				String[] argStr = msg.split(" ", 2); //获取参数数组(msg内容: "cov [参数]")
				String province = argStr[1].trim();
				if (!province.endsWith("省")) { //如果省份名最后没有 省 字
					province += "省";
				}
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
						"正在从丁香园获取数据,请稍候");
				JSONObject resultJson = JSONObject.parseObject(JsonHelper.loadJson("http://lab.isaaclin.cn/nCoV/api/area?latest=1&province=" + province));
				if (resultJson.toJSONString().equals("{\"results\": [], \"success\": true}")) { //如果JSON字符串是{"results": [], "success": true}(根本就是获取失败好吧!!)
					CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
							"获取数据失败,请检查您输入的省份名是否正确");
				} else {
						// 获取对应省份的数据
					System.out.println();
						JSONObject results = JSONObject.parseObject(resultJson.getString("results").replace("[", "").replace("]", ""));
						// 累计确诊人数
						int confirmedCount = results.getIntValue("confirmedCount");
						// 疑似感染人数
						int suspectedCount = results.getIntValue("suspectedCount");
						// 治愈人数
						int curedCount = results.getIntValue("curedCount");
						// 死亡人数
						int deadCount = results.getIntValue("deadCount");
						// 数据更新时间
						Calendar updateCalendar = Calendar.getInstance();
						updateCalendar.setTime(new Date(results.getLongValue("updateTime")));
						String updateTime = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(updateCalendar.getTime());
						// 定义数据字符串
						StringBuilder resultBuilder = new StringBuilder()
								.append(Global.FriendlyName).append("\n")
								.append("新冠肺炎疫情实时数据(").append(province).append(")\n")
								.append("数据更新于").append(updateTime).append("\n")
								.append("累计确诊病例:").append(confirmedCount).append("\n")
								.append("疑似病例:").append(suspectedCount).append("\n")
								.append("已治愈出院病例:").append(curedCount).append("\n")
								.append("死亡病例:").append(deadCount);
						CQ.sendGroupMsg(groupId, resultBuilder.toString());
				}
			}
			} catch (IndexOutOfBoundsException e) {
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" +
						"您输入的指令格式有误，请重新输入\n" + 
						"格式:!cov {省份名}\n" + 
						"如:\n" + 
						"查询全国最新数据:!cov\n" +
						"查询指定省份:!cov 河北省\n" + 
						"注意:不加\"省\"字的情况下系统会自动添加.");
			}catch (NullPointerException e) {
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
						"获取数据为空，可能是省份名输入错误或该省份无数据，请更换省份后再试");
			}catch (Exception e) {
				CQ.sendGroupMsg(groupId,FriendlyName + "\n获取失败");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}finally {
				return; //返回
			}
		}

		/**
		 * 功能4-2:Bilibili实时粉丝数据
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func4_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String arg2 = msg.split(" ", 2)[1].trim();
				if (CommonHelper.isInteger(arg2)) {
					JSONObject nameAndLevel = JSONObject.parseObject(JsonHelper.loadJson("https://api.bilibili.com/x/space/acc/info?mid=" + arg2 + "&jsonp=jsonp"));
					if (nameAndLevel.getIntValue("code") == -404) { //UID不存在
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
								"您输入的UID不存在,请重新输入");
					} else { //UID存在
						JSONObject fansAndOthers = JSONObject.parseObject(JsonHelper.loadJson("https://api.bilibili.com/x/relation/stat?vmid=" + arg2 + "&jsonp=jsonp")).getJSONObject("data");
						String nickName = nameAndLevel.getJSONObject("data").getString("name"); //昵称
						String level = "Lv" + nameAndLevel.getJSONObject("data").getIntValue("level"); //等级
						int fans = fansAndOthers.getIntValue("follower"); //粉丝数
						StringBuilder fansResult = new StringBuilder(Global.FriendlyName).append("\n")
								.append("Bilibili实时粉丝数据\n")
								.append("UP主:").append(nickName).append("(").append(level).append(")").append("\n")
								.append("UID:").append(arg2).append("\n")
								.append("粉丝数:").append(fans);
						CQ.sendGroupMsg(groupId, fansResult.toString());
					}
				} else {
					throw new NumberFormatException("Wrong bilibili user id!");
				}
			} catch (NumberFormatException e) {
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
						"您输入的UID不合法,请重新输入");
			}catch (IndexOutOfBoundsException e) {
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
						"您输入的指令格式有误,请重新输入\n" + 
						"格式:!bf [UID]\n" + 
						"如:!bf 2");
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
						"获取失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} finally {
				return; //返回
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
		/**
		 * 功能O-3:解除防滥用
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
		public static void FuncO_UnAbuse(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String[] arguments = msg.split(" ", 2);
				if (msg.trim().toLowerCase().equals("uab")) { // 如果只有一个指令
					// 定义已滥用标志文件
					File flagFile = new File(
							Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
					if (!flagFile.exists()) { // 如果标志文件不存在
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + "未处于防滥用状态");
					} else { // 如果标志文件存在
						if (new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".unlocking").exists()) { //如果验证码文件已存在
							return; //忽略（防止二次滥用）
						}
						// 新建验证码图片实例
						OTPHelper checkOtp = new OTPHelper();
						// 定义验证码图片文件
						File tmpFile = new File(Global.appDirectory + "/temp/"
								+ new SimpleDateFormat("YYYYMMddHHmmss").format(Calendar.getInstance().getTime())
								+ ".jpg");
						// 保存验证码图片
						OTPHelper.saveImage(checkOtp.getImage(), tmpFile);
						// 定义验证码字符串
						String otp = checkOtp.getText();
						// 写入验证码字符串
						IOHelper.WriteStr(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".unlocking", otp);
						StringBuilder otpMessage = new StringBuilder(FriendlyName).append("\n").append("[解除防滥用]输入!uab [验证码]").append("\n")
								.append(new CQCode().image(tmpFile));
						CQ.sendGroupMsg(groupId, otpMessage.toString());
						tmpFile.delete(); //删除验证码图片文件
					}
				} else { // 否则（输入了验证码）
					// 定义已滥用标志文件
					File flagFile = new File(
							Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
					if (!flagFile.exists()) { // 如果标志文件不存在
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + "未处于防滥用状态");
					} else { // 如果标志文件存在
						// 定义输入的验证码
						String inputOtp = arguments[1].trim();
						// 定义验证码文件(.unlocking)
						File otpFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".unlocking");
						// 定义执行中标志文件(.using)
						File usingFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".using");
						// 定义已滥用标志文件(.abused)
						File abusedFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
						// 定义读取到的文件中的验证码
						String realOtp = IOHelper.ReadToEnd(otpFile);
						if (inputOtp.equals(realOtp)) { //如果验证码输入正确
							if (otpFile.exists()) otpFile.delete(); //删除验证码文件(.unlocking)
							if (usingFile.exists()) usingFile.delete(); //删除执行中标志文件(.using)
							if (abusedFile.exists()) abusedFile.delete(); //删除已滥用标志文件(.abused)
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
									"验证成功,防滥用已解除");
						} else { //如果验证码输入错误
							otpFile.delete(); //删除验证码文件(.unlocking)
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
									"验证码输入错误,请重新输入!uab获取验证码");
						}
					}
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
						"防滥用解除失败(" + e.getClass().toString() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
	
		public static void FuncO_Report(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				String reportStr = msg.split(" ",2)[1];
				if (!reportStr.equals("")) { //如果反馈字符串不为空
					StringBuilder callMasterStr = new StringBuilder(FriendlyName).append("\n") 
							.append("成员反馈消息提醒\n") 
							.append("来源群聊:").append(Global.getGroupName(CQ, groupId)).append("(").append(groupId).append(")\n")
							.append("来源人员:").append(CQ.getStrangerInfo(qqId).getNick()).append("(").append(qqId).append(")\n")
							.append("反馈信息:\n")
							.append(reportStr);
					CQ.sendPrivateMsg(masterQQ, callMasterStr.toString()); //向机器人主人发送反馈信息
					CQ.sendGroupMsg(groupId, "收到！");
					return;
				}
			} catch (IndexOutOfBoundsException e) {
				// Do nothing (23333
			} catch (Exception e) {
				CQ.logError(AppName, "发生错误,请及时处理\n详细信息:\n" + e.getMessage() + "\n" + ExceptionHelper.getStackTrace(e));
			}
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
