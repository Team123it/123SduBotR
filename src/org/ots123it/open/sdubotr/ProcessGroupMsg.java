package org.ots123it.open.sdubotr;

import static org.ots123it.open.sdubotr.Global.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import org.meowy.cqp.jcq.entity.CQImage;
import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.meowy.cqp.jcq.message.CQCode;
import org.ots123it.jhlper.CommonHelper;
import org.ots123it.jhlper.DBHelper;
import org.ots123it.jhlper.ExceptionHelper;
import org.ots123it.jhlper.IOHelper;
import org.ots123it.jhlper.JsonHelper;
import org.ots123it.jhlper.OTPHelper;
import org.ots123it.open.sdubotr.picModeUtils.PicsGenerateUtility;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 123 SduBotR 群聊消息处理类<br>
 * 注意:本类中任何方法前的CQ参数请在Start类中直接用CQ即可<br>
 * <i>（若在Start类的main测试方法中调用，请使用你所new的Start实例的getCoolQ方法<br>
 * 如: <pre class="code">ProcessGroupMsg.main(<b>demo.getCoolQ()</b>,123456789L,123456789L,"Hello world");</pre></i>）
 * @since 0.0.1
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
			// 设置时区
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
			// 获取今日日期（格式:yyyyMMdd)
			Date todayDate = Calendar.getInstance().getTime();
			String today = new SimpleDateFormat("yyyyMMdd").format(todayDate).split(" ",2)[0];
			ResultSet thisGroupAllDaysSet = GlobalDatabases.dbgroup_ranking_speaking.
					  executeQuery("SELECT * FROM sqlite_master WHERE type='table' and name='" + String.valueOf(groupId) + ":" + today + "';");
			/* 获取当前表名为"[当前群群号]:[今日日期]"的数据表的表名列表
			 * SELECT name FROM sqlite_master WHERE type='table' and name='[groupId]:[today]';
			 */
			if (thisGroupAllDaysSet.next() == false) { //如果当前群今日排行榜不存在（功能3-1）
				 GlobalDatabases.dbgroup_ranking_speaking.
				 executeNonQuerySync("CREATE TABLE '" + String.valueOf(groupId) + ":" + today + 
							"' (QQId INTEGER PRIMARY KEY NOT NULL ON CONFLICT FAIL UNIQUE ON CONFLICT REPLACE," + 
							" SpeakTime INTEGER NOT NULL ON CONFLICT FAIL UNIQUE ON CONFLICT REPLACE);");
			/* 使用示例表的结构创建表名为"[当前群群号]:[今日日期]"的数据表
			 * CREATE TABLE '[groupId]:[today]' (
    		 *	QQId      INTEGER PRIMARY KEY
          *            			NOT NULL ON CONFLICT FAIL
          *            			UNIQUE ON CONFLICT REPLACE,
    		 *	SpeakTime INTEGER NOT NULL ON CONFLICT FAIL
          *            			UNIQUE ON CONFLICT REPLACE
			 *	);
			 */
			}
			ResultSet thisGroupDayPerson = GlobalDatabases.dbgroup_ranking_speaking.
					  executeQuery("SELECT SpeakTime FROM '" + String.valueOf(groupId) + ":" + today + "' WHERE QQId=" + qqId + ";");
			/* 获取今日当前群当前成员的发言次数行数据
			 * SELECT SpeakTime FROM '[groupId]:[today]' WHERE QQId=[qqId];
			 */
			if (thisGroupDayPerson.next() == false) { //如果该成员今日还未发过言(无该成员今日发言数据)
				GlobalDatabases.dbgroup_ranking_speaking.
						 executeNonQuerySync("INSERT OR REPLACE INTO '" + String.valueOf(groupId) + ":" + today + "' ('QQId','SpeakTime') VALUES " + 
						 "(" + String.valueOf(qqId) + ",1);");
				/* 添加该成员今日发言数据(次数为1)
				 * INSERT OR REPLACE INTO '[groupId]:[today]' ('QQId','SpeakTime') VALUES ([qqId],1);
				 */
		   } else { //如果该成员今日发过言了
				int currentSpeakTimes = thisGroupDayPerson.getInt("SpeakTime"); //获取当前发言次数
				currentSpeakTimes++; //发言次数+1
				GlobalDatabases.dbgroup_ranking_speaking.
						 executeNonQuerySync("INSERT OR REPLACE INTO '" + String.valueOf(groupId) + ":" + today + "' ('QQId','SpeakTime') VALUES " + 
						 "(" + String.valueOf(qqId) + "," + currentSpeakTimes + ");");
				/* 更新该成员今日发言数据 
				 * INSERT OR REPLACE INTO '[groupId]:[today]' ('QQId','SpeakTime') VALUES ([qqId],[currentSpeakTimes]);
				 */
		   }
			// [end]
			// [start] CQ码([CQ:sign]快捷群签到(功能3-2)
			if (msg.startsWith("[CQ:sign,")) { //如果是签到（新版手机QQ(v8.0+)为打卡）消息
				Part3.Func3_2(CQ, groupId, qqId, msg); //执行签到操作
			}
			// [end]
		} catch (Exception e) {
			CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
		Part_Spec.Funny_EasterEgg(CQ, groupId, qqId, msg); //调用滑稽彩蛋(功能S-1)方法 
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
				case "blist" : //功能1-5:群聊黑名单功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part1.Part1_5.Func1_5_main(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 主功能2:群管理辅助功能 */
					//功能2-1:群违禁词提醒功能 已在上面代码处理
				case "swc": //功能2-2:群迎新自定义提示功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part2.Func2_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "seg": //功能2-3:群成员退群自定义提示功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part2.Func2_3(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 主功能3:群增强功能 */
				case "rk": //功能3-1:查看群成员日发言排行榜
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_1(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "sign": //功能3-2:群签到
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "rksign": //功能3-3:查看群签到排行榜(本群Top5成员+所有群Top5群聊)
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_3(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				/* 主功能4:实用功能 */
				case "bf": case "bavid": //功能4-1:Bilibili相关功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part4.Part4_1.Func4_1_main(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "slct": // 功能4-2:随机选择选项功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
				   Part4.Func4_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
				   break;
				/* 主功能5:娱乐功能 */
				case "gm": // 主功能5:娱乐功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
 				   Part5.Func5_Main(CQ, groupId, qqId, msg);
				   new protectAbuse().doProtAbuse(CQ, groupId, qqId);
				   break;
				/* 主功能6:音游相关功能 */
				case "todaymug": //主功能6-1:今日音游推荐功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
	 				  Part6.Func6_1(CQ, groupId, qqId, msg);
				   new protectAbuse().doProtAbuse(CQ, groupId, qqId);
				   break;
				case "arc": //主功能6-2:韵律源点Arcaea查分功能
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
	 				Part6.Part6_2.Func6_2_Main(CQ, groupId, qqId, msg);
				   new protectAbuse().doProtAbuse(CQ, groupId, qqId);
				   break;
			   /* 其它功能 */
				case "about": // 功能O-1:关于
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
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part_Other.FuncO_Report(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
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
				case "签到":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_2(CQ, groupId, qqId, msg);
					new protectAbuse().doProtAbuse(CQ, groupId, qqId);
					break;
				case "签到排行榜":
					if (protectAbuse.doExeProtAbuse(CQ, groupId, qqId)) return;
					Part3.Func3_3(CQ, groupId, qqId, msg);
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
			}
			return; //最终返回
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
			}
			return; //最终返回
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
			}
			return; //最终返回
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
			}
			return; //最终返回
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
					String[] arguments = msg.split(" "); //获取参数（格式类似于"blist add 12345"）
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
					case "eab": //功能1-5-7:切换退群加黑开启状态（!blist eab）
						Func1_5_7(CQ, groupId, qqId, msg);
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
							ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							if (thisGroupBListSet.next() == false) { //如果黑名单未开启（文件夹不存在）
								GlobalDatabases.dbgroup_list.executeNonQuerySync("INSERT INTO BList (GroupId) VALUES (" + groupId + ");");
								/* 执行插入语句
								 * INSERT INTO BList ('GroupId') VALUES ([groupId]);
								 */
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
				} catch (SQLException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"启动失败(java.sql.SQLException)");
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
							ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							if (thisGroupBListSet.next()) { //如果黑名单未关闭（表存在）
								GlobalDatabases.dbgroup_list.executeNonQuerySync("DELETE FROM BList WHERE GroupId=" + groupId + ";");
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
				} catch (SQLException e) {
						CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"关闭失败(java.sql.SQLException)");
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
							ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							if (thisGroupBListSet.next()) { //如果黑名单是开启状态
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
									String originArrayStr = thisGroupBListSet.getString("BListArray"); //获取当前数据库中的原黑名单数组字符串
									ArrayList<String> originArrayList; //定义originArrayList数组集合
									boolean isFirstAdd = false; //定义是否是首次添加的标志
									if (originArrayStr == null) { //如果原来就是空的
										 isFirstAdd = true;
										 originArrayStr = ""; //初始化为空字符串
										 originArrayList = new ArrayList<String>(); //初始化空集合数组
									} else {
										 originArrayList = new ArrayList<String>(Arrays.asList(originArrayStr.split(","))); //使用原黑名单数组初始化集合数组
										 originArrayStr = originArrayStr + ",";
									}
									StringBuilder newArrayStrBuilder = new StringBuilder(originArrayStr); //定义新黑名单数组字符串
									int i = 0;
									for (String readyToAddSingleStr : readyToAddStr) { //遍历每个人员
										 	if ((isFirstAdd) && (readyToAddStr.get(i).equals(readyToAddSingleStr))) { //如果是首次添加且当前为非重复的首个要添加到黑名单的QQ号
										 		if (originArrayList.indexOf(readyToAddSingleStr) == -1) { //如果没有冲突
													newArrayStrBuilder.append(readyToAddSingleStr); //添加
													continue; //继续循环
												} else { //如果冲突了
													succCount--; //成功次数-1
													failCount++; //失败次数+1
													i++; //i+1
													continue; //继续循环
												}
												
										 	} else {
												if (originArrayList.indexOf(readyToAddSingleStr) == -1) { //如果没有冲突
													 newArrayStrBuilder.append(",").append(readyToAddSingleStr); //追加
													 continue; //继续循环
												} else { //如果冲突了
														succCount--; //成功次数-1
														failCount++; //失败次数+1
														continue; //继续循环
													}
										   }
										   
									}
									GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + newArrayStrBuilder.toString() + "' WHERE GroupId=" + groupId + ";");
									/* 执行更新语句
									 * UPDATE BList SET BListArray='[newArrayStrBuilder]' WHERE GroupId=[groupId];
									 */
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
				} catch (SQLException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"添加失败(java.sql.SQLException)");
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
						  if (Global.isGroupAdmin(CQ, groupId, qqId)) { // 如果消息发送人员是管理组成员
								if (Global.isGroupAdmin(CQ, groupId)) { // 如果机器人是管理组成员
									 ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list
												.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
									 if (thisGroupBListSet.next()) { // 如果黑名单是开启状态
										  String[] gotStr = msg.split(" ", 3)[2].trim().split(" ");
										  if (gotStr.equals(null)) { // 如果要移除的人员为空
												throw new IndexOutOfBoundsException("添加人员为空");
										  } else { // 如果要移除的人员不为空
												ArrayList<String> readyToDelStr = new ArrayList<String>(); // 新建保存列表的ArrayList
												int failCount = 0; // 记录失败移除的人员数
												for (String readyToDelSingleStr : gotStr) { // 循环检查要移除的QQ号数组
													 if (readyToDelSingleStr.startsWith("[CQ:at,")) { // 如果是at
														  String trueQQ = Long.valueOf(getCQAt(readyToDelSingleStr.trim()))
																	 .toString();
														  if (trueQQ.equals("-1000")) { // 如果是全体成员
																continue; // 直接循环到下一个
														  } else {
																readyToDelStr.add(trueQQ); // 移除成员
														  }
													 } else if (!CommonHelper.isInteger(readyToDelSingleStr)) { // 如果数组中某一项不是整数
														  continue; // 直接循环到下一个
													 } else { // 如果是整数
														  String trueQQ = readyToDelSingleStr;
														  readyToDelStr.add(trueQQ); // 添加成员
													 }
												}
												int succCount = readyToDelStr.size(); // 记录成功移除的人员数
												String originBListArrayStr = thisGroupBListSet.getString("BListArray"); // 获取原黑名单数组字符串
												if (originBListArrayStr == null) { // 如果为空
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n当前群黑名单为空,无法移除黑名单中不存在的成员");
													 return;
												} else { // 如果不为空
													 ArrayList<String> thisGroupBList = new ArrayList<String>(
																Arrays.asList(originBListArrayStr.split(","))); // 定义本群黑名单数组集合
													 for (String readyToDelSingleStr : readyToDelStr) { // 遍历每个人员
														  if (thisGroupBList.remove(readyToDelSingleStr)) //移除指定人员并判断处理结果
														  { //处理成功
																continue; //继续下一轮循环
														  } else { //处理失败
																succCount--; //成功次数-1
																failCount++; //失败次数+1
														  }
													 }
													 StringBuilder newBListArrayBuilder = new StringBuilder(); //定义新黑名单数组字符串
													 for (String singleBListPerson : thisGroupBList) { //遍历thisGroupBList
														  if (singleBListPerson.equals(thisGroupBList.get(0))) { //如果遍历到的是thisGroupBList中第一个QQ号字符串
																newBListArrayBuilder.append(singleBListPerson); //添加
														  } else {
																newBListArrayBuilder.append(",").append(singleBListPerson); //附带分隔符式添加
														  }
													 }
													 GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + newBListArrayBuilder.toString() + "' WHERE GroupId=" + groupId + ";");
													 /* 执行更新语句
													  * UPDATE BList SET BListArray='[newBListArrayBuilder]' WHERE GroupId='[groupId]';
													  */
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
																 "命令成功完成.\n" + 
																 "成功:" + succCount + " 失败:" + failCount);
												    return;
												}
										  }
									 } else { // 如果黑名单未开启
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "本群黑名单未开启，请输入!blist start开启");
										  return;
									 }
								} else { // 如果机器人不是管理组成员
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "请先将机器人设置成为本群管理员后再进行操作(401)");
									 return;
								}
						  } else { // 如果消息发送人员不是管理组成员
								CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "您没有权限执行该操作(403)");
						  }
					 } catch (IndexOutOfBoundsException e) {
						  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "您输入的指令格式有误，请更正后重试(注意指令间只能存在一个空格):\n"
									 + "格式:!blist del [QQ号/at] {QQ号/at...}");
					 } catch (SQLException e) {
						  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "移除失败(java.sql.SQLException)");
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
							 ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list
										.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							 if (thisGroupBListSet.next()) { // 如果黑名单是开启状态
								String page = "1";
								if (msg.split(" ",3).length == 3) {
									page = msg.split(" ",3)[2]; //获取页码参数
								}
								String result = ""; //定义返回的图片路径变量
								if ((!page.equals("")) && (CommonHelper.isInteger(page))) { //如果页码参数存在且是数字
									result = PicsGenerateUtility.getGroupBlistPic(CQ, GlobalDatabases.dbgroup_list,groupId, Integer.parseInt(page)).trim();
									if (result == null) {
										CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
										return;
									}
									switch (result)
									{
									case "": //获取为空
										CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
									case "invalid": //无效的页码数
										break;
									case "nothing": //黑名单列表为空
										throw new NullPointerException("The black list of this group is empty");
									default: //其它（返回的是文件路径）
										CQImage image = new CQImage(new File(result)); // 获取图片CQ码并发送
										CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
										new File(result).delete();
									}
								}  else { //如果页码参数不存在
									result = PicsGenerateUtility.getGroupBlistPic(CQ,GlobalDatabases.dbgroup_list, groupId, 1).trim();
									if (result == null) {
										CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
										return;
									}
									switch (result)
									{
									case "": //获取为空
										CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
									case "invalid": //无效的页码数
										break;
									case "nothing": //黑名单列表为空
										throw new NullPointerException("The black list of this group is empty");
									default: //其它（返回的是文件路径）
										CQImage image = new CQImage(new File(result)); // 获取图片CQ码并发送
										CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
										new File(result).delete();
									}
								}
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
				} catch (NullPointerException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n本群黑名单为空");
				} catch (NumberFormatException e) {
					 // TODO 自动生成的 catch 块
					 e.printStackTrace();
				} catch (SQLException | IOException  e) {
					 CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
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
							 ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list
										.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							 if (thisGroupBListSet.next()) { // 如果黑名单是开启状态
								boolean isNoPrompt = thisGroupBListSet.getBoolean("RefusePromptStat");
								if (!isNoPrompt) { //如果当前为提醒状态
									GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET RefusePromptStat=0 WHERE GroupId=" + groupId + ";");
									/* 执行更新语句
									 * UPDATE BList SET RefusePromptStat=0 WHERE GroupId=[groupId];
									 * 注:SQLite中boolean是以数值0,1的方式存储,故更新或插入boolean值时需要填0或1而并非true或false(后者会报错)
									 */
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"已切换成:拒绝后不提醒");
									return;
								} else { //如果当前为不提醒状态
									 GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET RefusePromptStat=1 WHERE GroupId=" + groupId + ";");
									 /* 执行更新语句
									  * UPDATE BList SET RefusePromptStat=1 WHERE GroupId=[groupId];
								 	  * 注:SQLite中boolean是以数值0,1的方式存储,故更新或插入boolean值时需要填0或1而并非true或false(后者会报错)
									  */
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
				} catch (SQLException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"设置失败(java.sql.SQLException)");
				}
			}
			/**
			 * 功能1-5-7:切换退群加黑启用状态
			 * @param CQ CQ实例
			 * @param groupId 来源群号
			 * @param qqId 来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func1_5_7(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					if (Global.isGroupAdmin(CQ, groupId, qqId)){ // 如果消息发送人员是管理组成员
						if (Global.isGroupAdmin(CQ, groupId)) { //如果机器人是管理组成员
							 ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list
										.executeQuery("SELECT * FROM BList WHERE GroupId=" + groupId + ";");
							 if (thisGroupBListSet.next()) { // 如果黑名单是开启状态
								boolean isExitAutoAdd = thisGroupBListSet.getBoolean("ExitAutoAddStat");
								if (!isExitAutoAdd) { //如果当前为退群不加黑状态
									GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET ExitAutoAddStat=1 WHERE GroupId=" + groupId + ";");
									/* 执行更新语句
									 * UPDATE BList SET ExitAutoAddStat=1 WHERE GroupId=[groupId];
									 * 注:SQLite中boolean是以数值0,1的方式存储,故更新或插入boolean值时需要填0或1而并非true或false(后者会报错)
									 */
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"已切换成:退群后加黑");
									return;
								} else { //如果当前为退群加黑状态
									 GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET ExitAutoAddStat=0 WHERE GroupId=" + groupId + ";");
									 /* 执行更新语句
									  * UPDATE BList SET ExitAutoAddStat=0 WHERE GroupId=[groupId];
								 	  * 注:SQLite中boolean是以数值0,1的方式存储,故更新或插入boolean值时需要填0或1而并非true或false(后者会报错)
									  */
									CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
											"已切换成:退群后不加黑");
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
							"格式:!blist eab");
				} catch (SQLException e) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
							"设置失败(java.sql.SQLException)");
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
		  try {
				// 判断违禁词列表是否为空
				ResultSet iMGBanResultSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT KeyWord FROM iMGBan");
				
				ArrayList<String> iMGBans = new ArrayList<String>(); //定义iMGBans数组集合
				if(iMGBanResultSet.next() == false)
				{
					return;
				} else {
					 iMGBans.add(iMGBanResultSet.getString("KeyWord"));
				} // 否则
				System.gc(); //通知Java进行垃圾收集
				while (iMGBanResultSet.next()) { //遍历iMGBanResultSet
					 iMGBans.add(iMGBanResultSet.getString("KeyWord")); //添加违禁关键词到iMGBans中
				}
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
		  } catch (Exception e) {
				CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
		  }
		  return;
		}

		/**
		 * 功能2-2:群迎新自定义提示功能
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func2_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				if (isGroupAdmin(CQ, groupId, qqId)) { //如果消息发送者是管理组成员
					String inputStr = msg.split(" ", 2)[1].trim(); //获取要设置的迎新提示内容
					if (!inputStr.equals("")) { //如果内容不为空
						 int bannedObscenitCounts = 0;
						 for (String bannedObscenity : bannedObscenities) {
							  if (inputStr.contains(bannedObscenity)) {
									bannedObscenitCounts++;
							  }
						 }
						 if (bannedObscenitCounts > 0) {
							  CQ.sendGroupMsg(groupId, FriendlyName + "\n错误:无法设置包含不文明用语的内容\n匹配项数:" + bannedObscenitCounts);
							  return;
						 }
						 String queryStr = new StringBuilder().append("INSERT OR REPLACE INTO Prompt ('GroupId', 'WelcomeStr','ExitStr','PromoteAdminStr','CancelAdminStr') VALUES ")
								  .append("(").append(groupId).append(",")
								  .append("'").append(inputStr).append("',")
								  .append("(SELECT ExitStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
								  .append("(SELECT PromoteAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
								  .append("(SELECT CancelAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("));").toString();
						 GlobalDatabases.dbgroup_custom.executeNonQuerySync(queryStr);
						 /* 执行插入语句
						 * INSERT OR REPLACE INTO Prompt ('GroupId','WelcomeStr','ExitStr', 'PromoteAdminStr','CancelAdminStr')
                   *       VALUES (
                   *              [groupId],
                   *              '[inputStr]',
                   *              (SELECT ExitStr FROM Prompt WHERE GroupId = [groupId]),
                   *              (SELECT PromoteAdminStr FROM Prompt WHERE GroupId = [groupId]),
                   *              (SELECT CancelAdminStr FROM Prompt WHERE GroupId = [groupId]));
						 */
						 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"设置完成,共" + inputStr.length() + "字符");
							return;
					} else { //如果内容为空
					    throw new IndexOutOfBoundsException("Input argument 2 is empty!");
					}
				} else { //如果消息发送者不是管理组成员
					CQ.sendGroupMsg(groupId, FriendlyName + "\n权限不足，无法执行操作。(403)");
				}
			} catch (IndexOutOfBoundsException e) { //数组下标越界异常捕获
				 String restoreQueryStr = new StringBuilder().append("INSERT OR REPLACE INTO Prompt ('GroupId','ExitStr','PromoteAdminStr','CancelAdminStr') VALUES ")
							  .append("(").append(groupId).append(",")
							  .append("(SELECT ExitStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
							  .append("(SELECT PromoteAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
							  .append("(SELECT CancelAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("));").toString();
				 /* 执行替换式插入语句并将WelcomeStr字段置为空
				  * INSERT OR REPLACE INTO Prompt ('GroupId','ExitStr', 'PromoteAdminStr','CancelAdminStr')
              *       VALUES (
              *              [groupId],
              *              (SELECT ExitStr FROM Prompt WHERE GroupId = [groupId]),
              *              (SELECT PromoteAdminStr FROM Prompt WHERE GroupId = [groupId]),
              *              (SELECT CancelAdminStr FROM Prompt WHERE GroupId = [groupId]));
				  */
				 try {
					  GlobalDatabases.dbgroup_custom.executeNonQuerySync(restoreQueryStr);

						 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"已恢复成默认迎新提示内容");
				} catch (SQLException e2) {
					 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "设置失败(" + e2.getClass().getName() + ")");
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "设置失败(" + e.getClass().getName() + ")");
			}
		}
		
		/**
		 * 功能2-3:群成员退群自定义提示功能
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func2_3(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				if (isGroupAdmin(CQ, groupId, qqId)) { //如果消息发送者是管理组成员
					String inputStr = msg.split(" ", 2)[1].trim(); //获取要设置的退群提示内容
					if (!inputStr.equals("")) { //如果内容不为空
						 int bannedObscenitCounts = 0;
						 for (String bannedObscenity : bannedObscenities) {
							  if (inputStr.contains(bannedObscenity)) {
									bannedObscenitCounts++;
							  }
						 }
						 if (bannedObscenitCounts > 0) {
							  CQ.sendGroupMsg(groupId, FriendlyName + "\n错误:无法设置包含不文明用语的内容\n匹配项数:" + bannedObscenitCounts);
							  return;
						 }
						 String queryStr = new StringBuilder().append("INSERT OR REPLACE INTO Prompt ('GroupId', 'WelcomeStr','ExitStr','PromoteAdminStr','CancelAdminStr') VALUES ")
									  .append("(").append(groupId).append(",")
									  .append("(SELECT WelcomeStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
									  .append("'").append(inputStr).append("',")
									  .append("(SELECT PromoteAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
									  .append("(SELECT CancelAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("));").toString();
							GlobalDatabases.dbgroup_custom.executeNonQuerySync(queryStr);
							/* 执行插入语句
							 * INSERT OR REPLACE INTO Prompt ('GroupId','WelcomeStr','ExitStr', 'PromoteAdminStr','CancelAdminStr')
	                   *       VALUES (
	                   *              [groupId],
	                   *              (SELECT WelcomeStr FROM Prompt WHERE GroupId = [groupId]),
	                   *              '[inputStr]',
	                   *              (SELECT PromoteAdminStr FROM Prompt WHERE GroupId = [groupId]),
	                   *              (SELECT CancelAdminStr FROM Prompt WHERE GroupId = [groupId]));
							 */
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
									"设置完成,共" + inputStr.length() + "字符");
							return;
					} else { //如果内容为空
						throw new IndexOutOfBoundsException("Input argument 2 is empty!");
					}
				} else { //如果消息发送者不是管理组成员
					CQ.sendGroupMsg(groupId, FriendlyName + "\n权限不足，无法执行操作。(403)");
				}
			} catch (IndexOutOfBoundsException e) { //数组下标越界异常捕获
				 String restoreQueryStr = new StringBuilder().append("INSERT OR REPLACE INTO Prompt ('GroupId','WelcomeStr','PromoteAdminStr','CancelAdminStr') VALUES ")
							  .append("(").append(groupId).append(",")
							  .append("(SELECT WelcomeStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
							  .append("(SELECT PromoteAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("),")
							  .append("(SELECT CancelAdminStr FROM Prompt WHERE GroupId = ").append(groupId).append("));").toString();
				 /* 执行替换式插入语句并将ExitStr字段置为空
				  * INSERT OR REPLACE INTO Prompt ('GroupId','WelcomeStr', 'PromoteAdminStr','CancelAdminStr')
				  *       VALUES (
				  *              [groupId],
				  *              (SELECT WelcomeStr FROM Prompt WHERE GroupId = [groupId]),
				  *              (SELECT PromoteAdminStr FROM Prompt WHERE GroupId = [groupId]),
				  *              (SELECT CancelAdminStr FROM Prompt WHERE GroupId = [groupId]));
				  */
				 try {
						 GlobalDatabases.dbgroup_custom.executeNonQuerySync(restoreQueryStr);
						 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
								"已恢复默认成员退群提示内容");
				} catch (Exception e2) {
					 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "设置失败(" + e2.getClass().getName() + ")");
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" + "设置失败(" + e.getClass().getName() + ")");
			}
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
					TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
					// 获取今日日期（格式:yyyyMMdd)
					Date todayDate = Calendar.getInstance().getTime();
					String today = new SimpleDateFormat("yyyyMMdd").format(todayDate).split(" ",2)[0];
					CQ.logDebug(Global.AppName, "今日日期:" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(todayDate));
					ResultSet todaySpeakRanking = GlobalDatabases.dbgroup_ranking_speaking
							  .executeQuery("SELECT * FROM sqlite_master WHERE type='table' and name='" + groupId + ":" + today + "';");
					/* 获取今日的群聊日发言排行榜数据表名集合
					 * SELECT name FROM sqlite_master WHERE type='table' and name='[groupId]:[today]';
					 */
					if (todaySpeakRanking.next() == true) { //如果今日的发言排行数据表存在
						 ArrayList<String> todaySpeakPersons = new ArrayList<String>(); //定义今日该群发言人员列表
						 ArrayList<Integer> todaySpeakTimes = new ArrayList<Integer>(); //定义今日该群各发言人员发言次数列表
						 ResultSet todaySpeakPersonsSet = GlobalDatabases
									.dbgroup_ranking_speaking.executeQuery("SELECT * FROM '" + groupId + ":" + today + "';");
						 /* 获取今日本群发言的所有人的发言数据
						  * SELECT * FROM '[groupId]:[today]';
						  */
						if (todaySpeakPersonsSet.next()) { //如果今日有人发过言（数据表中有指定项）
								todaySpeakPersons.add(String.valueOf(todaySpeakPersonsSet.getLong("QQId"))); //添加首行中的QQ号到todaySpeakPersons中
								todaySpeakTimes.add(todaySpeakPersonsSet.getInt("SpeakTime")); //添加首行中的该QQ号发言次数到todaySpeakTimes中
							while (todaySpeakPersonsSet.next()) { //遍历todaySpeakPersonsSet
								todaySpeakPersons.add(String.valueOf(todaySpeakPersonsSet.getLong("QQId"))); //添加当前行中的QQ号到todaySpeakPersons中
								todaySpeakTimes.add(todaySpeakPersonsSet.getInt("SpeakTime")); //添加当前行中的该QQ号发言次数到todaySpeakTimes中
						   }
							BufferedImage image = ImageIO.read(new FileInputStream(Global.appDirectory + "/data/pics/SpeakRank.png")); //读取图片文件
							Graphics2D g2 = image.createGraphics(); //从图片创建Graphics2D图像处理实例
							long beginTime = System.nanoTime() / 1000000;
							g2.setFont(new Font("方正喵呜体", Font.PLAIN, 30)); // 设置字体
							g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							g2.setColor(Color.blue);
							g2.drawString("群聊" + groupId + "日发言排行榜", 720, 70);
							String[] todaySpeakCounts = new String[todaySpeakPersons.size()]; //定义今日发言次数数组
							for (int i = 0; i < todaySpeakCounts.length; i++) {
								//将今日发言数组中的索引为i的值赋值为"记录文件名" + "," + "记录文件内容(发言次数)"
								todaySpeakCounts[i] = todaySpeakPersons.get(i) + "," + todaySpeakTimes.get(i);
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
										currentSpeakQQNick = currentSpeakQQ.getCard().replaceAll("\\[CQ:(face|emoji),id=\\d{1,}\\]?", "");
										// 使用正则删除掉Face和Emoji的CQ码，防止出现显示错误
										// 正则表达式为:"\[CQ:(face|emoji),id=\d{1,}\]?"
										// 替换掉的格式为: [CQ:face,id=xxx] 或 [CQ:emoji,id=xxx]
									} else { //否则（成员群内昵称为空）
										currentSpeakQQNick = currentSpeakQQ.getNick().replaceAll("\\[CQ:(face|emoji),id=\\d{1,}\\]?", "");
										// 使用正则删除掉Face和Emoji的CQ码，防止出现显示错误
										// 正则表达式为:"\[CQ:(face|emoji),id=\d{1,}\]?"
										// 替换掉的格式为: [CQ:face,id=xxx] 或 [CQ:emoji,id=xxx]
									}
									String currentQQNickPre20 = CommonHelper.subStringByByte("GBK", currentSpeakQQNick, 20); //获取成员昵称前20字节
									if (!currentQQNickPre20.equals(currentSpeakQQNick)) { //如果成员昵称前20字节不等于完整昵称(成员昵称长度大于20字节)
										currentSpeakQQNick = currentQQNickPre20 + "..."; //取前20字节作为显示成员昵称
									}
								} else { //否则（成员已不在群内或成员是统计的匿名消息）
									if (currentSpeakQQNo == 80000000L) { //如果成员是匿名者
										currentSpeakQQNick = "匿名消息";
									} else { //如果成员已不在群内
										currentSpeakQQNick = "*已退出群员";
									}
								}
								if (i == 0) //如果i等于0（龙王(Rank:1)）
								{
									if (currentSpeakQQNick.equals("已退出群员")) { //如果是已退出群员
										g2.setColor(Color.black); //设置已退出群员文字颜色
									} else { //否则
										g2.setColor(new Color(0,128,255)); //设置第1名文字颜色
									}
									// 绘制第1名字符串
									g2.drawString("1", 55, 175);
									g2.drawString(currentSpeakQQNick, 130, 175);
									g2.drawString(Long.toString(currentSpeakTimes), 570, 175);
								}else  //否则
								{
									if (currentSpeakQQNick.equals("已退出群员")) { //如果是已退出群员
										g2.setColor(Color.black); //设置已退出群员文字颜色
									} else { //否则
										g2.setColor(new Color(255,127,39)); //设置第2~10名文字颜色
									}
									switch (i) //绘制第2~10名字符串
									{
									case 1: //No.2
										g2.drawString(Integer.toString(i + 1), 55, 245);
										g2.drawString(currentSpeakQQNick, 130, 245);
										g2.drawString(Long.toString(currentSpeakTimes), 570, 245);
										break;
									case 2: //No.3
										g2.drawString(Integer.toString(i + 1), 55, 315);
										g2.drawString(currentSpeakQQNick, 130, 315);
										g2.drawString(Long.toString(currentSpeakTimes), 570, 315);
										break;
									case 3: //No.4
										g2.drawString(Integer.toString(i + 1), 55, 385);
										g2.drawString(currentSpeakQQNick, 130, 385);
										g2.drawString(Long.toString(currentSpeakTimes), 570, 385);
										break;
									case 4: //No.5
										g2.drawString(Integer.toString(i + 1), 55, 455);
										g2.drawString(currentSpeakQQNick, 130, 455);
										g2.drawString(Long.toString(currentSpeakTimes), 570, 455);
										break;
									case 5: //No.6
										g2.drawString(Integer.toString(i + 1), 55, 525);
										g2.drawString(currentSpeakQQNick, 130, 525);
										g2.drawString(Long.toString(currentSpeakTimes), 570, 525);
										break;
									case 6: //No.7
										g2.drawString(Integer.toString(i + 1), 660, 175);
										g2.drawString(currentSpeakQQNick, 735, 175);
										g2.drawString(Long.toString(currentSpeakTimes), 1175, 175);
										break;
									case 7: //No.8
										g2.drawString(Integer.toString(i + 1), 660, 245);
										g2.drawString(currentSpeakQQNick, 735, 245);
										g2.drawString(Long.toString(currentSpeakTimes), 1175, 245);
										break;
									case 8: //No.9
										g2.drawString(Integer.toString(i + 1), 660, 315);
										g2.drawString(currentSpeakQQNick, 735, 315);
										g2.drawString(Long.toString(currentSpeakTimes), 1175, 315);
										break;
									case 9: //No.10
										g2.drawString(Integer.toString(i + 1), 660, 385);
										g2.drawString(currentSpeakQQNick, 735, 385);
										g2.drawString(Long.toString(currentSpeakTimes), 1175, 385);
										break;
									default:
										continue;
									}
								}
							}
							g2.setColor(new Color(34,177,76)); //设置消息发送人员文字颜色
							//定义消息发送人员的发言次数和发言名次
							long mySpeakTimes = 0,mySpeakNo = 9999;
							//定义消息发送人员的昵称
							String mySpeakQQNick;
							Member mySpeakQQ = CQ.getGroupMemberInfo(groupId, qqId,true);
							if (mySpeakQQ != null) //如果获取成功（成员在群内）
							{
								if (!mySpeakQQ.getCard().equals("")) //如果成员群内昵称不为空
								{
									mySpeakQQNick = mySpeakQQ.getCard().replaceAll("\\[CQ:(face|emoji),id=\\d{1,}\\]?", "");
									// 使用正则删除掉Face和Emoji的CQ码，防止出现显示错误
									// 正则表达式为:"\[CQ:(face|emoji),id=\d{1,}\]?"
									// 替换掉的格式为: [CQ:face,id=xxx] 或 [CQ:emoji,id=xxx]
								} else { //否则（成员群内昵称为空）
									mySpeakQQNick = mySpeakQQ.getNick().replaceAll("\\[CQ:(face|emoji),id=\\d{1,}\\]?", "");
									// 使用正则删除掉Face和Emoji的CQ码，防止出现显示错误
									// 正则表达式为:"\[CQ:(face|emoji),id=\d{1,}\]?"
									// 替换掉的格式为: [CQ:face,id=xxx] 或 [CQ:emoji,id=xxx]
								}
							} else {
								mySpeakQQNick = "未知昵称";
							}
							String myQQNickPre20 = CommonHelper.subStringByByte("GBK", mySpeakQQNick, 20); //获取成员昵称前20字节
							if (!myQQNickPre20.equals(mySpeakQQNick)) { //如果成员昵称前20字节不等于完整昵称(成员昵称长度大于20字节)
								mySpeakQQNick = myQQNickPre20 + "..."; //取前20字节作为显示成员昵称
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
							// 绘制消息发送人员排名字符串
							g2.drawString(Long.toString(mySpeakNo), 660, 525);
							g2.drawString(mySpeakQQNick, 735, 525);
							g2.drawString(Long.toString(mySpeakTimes), 1175, 525);
							g2.setFont(new Font("方正喵呜体", Font.PLAIN, 30)); // 设置字体
							g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							g2.setColor(new Color(0, 0, 0));
							String notice = Global.getRandAncment();
							String showNotice = "";
							if (notice.length() > 40) {
								showNotice = notice.substring(0, 36) + "...";
							} else {
								showNotice = notice;
							}
							g2.drawString(showNotice, 56, 670);
							g2.setFont(new Font("微软雅黑", Font.PLAIN, 15)); // 设置字体
							g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							String nowDateStr = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
							g2.drawString(nowDateStr, 20, 710);
							long finishTime = System.nanoTime() / 1000000;
							String generateTime = String.valueOf(finishTime - beginTime);
							g2.drawString(Global.AppName + " v" + Global.Version + " ©御坂12456 Generate: " + generateTime + "ms", 880, 710);
							g2.dispose(); // 保存绘图对象
							File tmpImageFile = 
									new File(Global.appDirectory + "/temp/" + new SimpleDateFormat("YYYYMMddHHmmss").format(Calendar.getInstance().getTime()) + ".png");
							// 创建并写入临时图片文件（路径见上）
							ImageIO.write(image, "png", tmpImageFile);
							CQ.sendGroupMsg(groupId, new CQCode().image(tmpImageFile)); //发送群成员日活跃排行榜图片
							tmpImageFile.delete();
							System.gc(); //执行垃圾收集器
						} else { //否则
							CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
									"今日本群的群聊日发言排行榜为空~");
						}
					} else { //否则
						CQ.sendGroupMsg(groupId,Global.FriendlyName + "\n" + 
								"今日本群的群聊日发言排行榜为空~");
					}
			} catch (Exception e) {
				CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
				CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
		  }
		}
		/**
		 * 功能3-2:群签到
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func3_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
				Date todayDate = Calendar.getInstance().getTime();
				String today = new SimpleDateFormat("YYYYMMdd").format(todayDate); //获取今日字符串(格式:YYYYMMdd,如20200101)
				String friendlyToday = new SimpleDateFormat("YYYY-MM-dd").format(todayDate); //获取今日友好字符串(格式:YYYY-MM-dd,如2020-01-01)
				File thisDaySign = new File(Global.appDirectory + "/group/sign/" + today); //新建今日签到文件夹实例
				File thisGroupSign = new File(Global.appDirectory + "/group/sign/" + today + "/" + groupId); //新建今日当前群聊签到文件夹实例
				File thisPersonSign = new File(Global.appDirectory + "/group/sign/" + today + "/" + groupId + "/" + qqId + ".signed"); 
				//新建今日当前群聊当前成员已签到标志文件实例
				if (!thisDaySign.exists()) { //如果今日签到文件夹不存在(全群第一位签到)
					thisDaySign.mkdir(); //新建今日签到文件夹
					thisGroupSign.mkdir(); //新建今日当前群聊签到文件夹
					thisPersonSign.createNewFile(); //新建标志文件
					StringBuilder signResultBuilder = new StringBuilder(FriendlyName).append("\n")
							.append(friendlyToday).append(" ").append("No.1\n")
							.append(new CQCode().at(qqId)).append("\n")
							.append("本群第1位");
					CQ.sendGroupMsg(groupId, signResultBuilder.toString());
					return;
				} else { //如果今日签到文件夹存在(全群已有人签过到)
					if (!thisGroupSign.exists()) { //如果今日当前群签到文件夹不存在(本群第一位签到)
						long signNumber = IOHelper.getFileCounts(thisDaySign) + 1L; //获取今日当前成员签到序号
						thisGroupSign.mkdir(); //新建今日当前群聊签到文件夹
						thisPersonSign.createNewFile(); //新建标志文件
						StringBuilder signResultBuilder = new StringBuilder(FriendlyName).append("\n")
								.append(friendlyToday).append(" ").append("No.").append(signNumber).append("\n")
								.append(new CQCode().at(qqId)).append("\n")
								.append("本群第1位");
						CQ.sendGroupMsg(groupId, signResultBuilder.toString());
						return;
					}
					else { //如果今日当前群签到文件夹存在(本群已有人签到)
						if (!thisPersonSign.exists()) { //如果今日本群当前成员未签到
							long signNumber = IOHelper.getFileCounts(thisDaySign) + 1L; //获取今日当前成员签到序号
							long thisGroupSignNumber = IOHelper.getFileCounts(thisGroupSign) + 1L; //获取今日本群当前成员签到序号
							thisPersonSign.createNewFile(); //新建标志文件
							StringBuilder signResultBuilder = new StringBuilder(FriendlyName).append("\n")
									.append(friendlyToday).append(" ").append("No.").append(signNumber).append("\n")
									.append(new CQCode().at(qqId)).append("\n")
									.append("本群第").append(thisGroupSignNumber).append("位");
							CQ.sendGroupMsg(groupId, signResultBuilder.toString());
							return;
						} else { //如果今日本群当前成员已签到
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "您今天已经签过到了");
							return;
						}
					}
				}
			} catch (IOException e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n签到失败(java.io.IOException)");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n签到失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
		/**
		 * 功能3-3:查看今日群签到排行榜
		 * @param CQ CQ实例
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源QQ号
		 * @param msg 消息内容
		 */
		public static void Func3_3(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai")); //设置时区
				Date todayDate = Calendar.getInstance().getTime();
				String today = new SimpleDateFormat("YYYYMMdd").format(todayDate); //获取今日字符串(格式:YYYYMMdd,如20200101)
				String todayStr = new SimpleDateFormat("YYYY-MM-dd").format(todayDate); //获取今日友好字符串(格式:YYYY-MM-dd,如2020-01-01)
				File thisDaySign = new File(Global.appDirectory + "/group/sign/" + today); //新建今日签到文件夹实例
				File thisGroupSign = new File(Global.appDirectory + "/group/sign/" + today + "/" + groupId); //新建今日当前群聊签到文件夹实例
				boolean allSignEmpty = true, thisGroupSignEmpty = true;
				if (thisDaySign.exists()) {
					allSignEmpty = false;
				}
				if (thisGroupSign.exists()) {
					thisGroupSignEmpty = false;
				}
				ArrayList<File> thisGroupSignList = null, thisDaySignList = null;
				if (thisGroupSign.listFiles() != null)
				{
					thisGroupSignList = new ArrayList<File>(Arrays.asList(thisGroupSign.listFiles())); 
					//获取今日当前群的所有已签到人员文件数组并转换为ArrayList<File>类型
				} else {
					thisGroupSignEmpty = true;
				}
				if (thisDaySign.listFiles() != null) {
					thisDaySignList = new ArrayList<File>(Arrays.asList(thisDaySign.listFiles()));
					//获取今日所有已签到群文件夹数组并转换为ArrayList<File>类型
				} else {
					allSignEmpty = true;
				}
				BufferedImage image = ImageIO.read(new FileInputStream(Global.appDirectory + "/data/pics/sign.png"));
				Graphics2D g2 = image.createGraphics();
				long beginTime = System.nanoTime() / 1000000;
				g2.setFont(new Font("方正喵呜体", Font.PLAIN, 25)); // 设置字体
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.blue);
				g2.drawString(todayStr + "签到排行榜", 750, 70);
				g2.setColor(new Color(255, 127, 39));
				if (allSignEmpty) { //如果今日全群无人签到
					g2.drawString("暂无今日签到数据", 220, 235);
					g2.drawString("暂无今日签到数据", 810, 235);
				} else if (thisGroupSignEmpty) { //如果今日全群有人签到但本群无人签到
					g2.drawString("暂无今日签到数据", 220, 235);
					int i = 0; //定义要取的Top？临时变量(最高为Top5)
					if (thisDaySignList.size() > 5) { //如果今日总签到人数大于5位
						i = 4; //全群Top？设置为Top5
					} else { //否则
						i = thisDaySignList.size() - 1; //全群Top？设置为总签到人数
					}
					//通过匿名的Comparator使数组集合(ArrayList)按照每个文件最后修改的日期时间戳进行数组集合升序排序（下同）
					//本次排序为今日已签到群聊文件夹
					Collections.sort(thisDaySignList, new Comparator<File>()
					{
						@Override
						 public int compare(File o1, File o2)
						 {
							try {
								 /* 你家JVM不判断null心里难受(java.lang.IllegalArgumentException) */
								if (o1 == null)
								{
									return -1;
								} else if (o2 == null) {
									return 1;
								} else
								{
									//按创建日期进行排序
									long dateO1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis();
									long dateO2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis();
									int result = Long.compare(dateO1, dateO2);
									return result;
								}
							} catch (Exception e) {
								e.printStackTrace();
								return 0;
							}
						}
					});
					// 循环写入今日全群Top5
					for (int tempI = 0; tempI <= i; tempI++) {
						switch (tempI)
						{
						case 0: //No.1
							g2.drawString("No.1", 660, 235);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 235);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 235);
							break;
						case 1: //No.2
							g2.drawString("No.2", 660, 285);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 285);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 285);
							break;
						case 2: //No.3
							g2.drawString("No.3", 660, 335);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 335);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 335);
							break;
						case 3: //No.4
							g2.drawString("No.4", 660, 385);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 385);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 385);
							break;
						case 4: //No.5
							g2.drawString("No.5", 660, 435);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 435);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 435);
							break;
						default:
							break;
						}
					}
					g2.setFont(new Font("方正喵呜体", Font.PLAIN, 30)); // 设置字体
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(new Color(0, 0, 0));
					String notice = Global.getRandAncment();
					String showNotice = "";
					if (notice.length() > 40) {
						showNotice = notice.substring(0, 36) + "...";
					} else {
						showNotice = notice;
					}
					g2.drawString(showNotice, 56, 670);
					g2.setFont(new Font("微软雅黑", Font.PLAIN, 15)); // 设置字体
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					String nowDateStr = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					g2.drawString(nowDateStr, 20, 710);
					long finishTime = System.nanoTime() / 1000000;
					String generateTime = String.valueOf(finishTime - beginTime);
					g2.drawString(Global.AppName + " v" + Global.Version + " ©御坂12456 Generate: " + generateTime + "ms", 880, 710);
					g2.dispose(); // 保存绘图对象
					File tmpImageFile = 
							new File(Global.appDirectory + "/temp/" + new SimpleDateFormat("YYYYMMddHHmmss").format(Calendar.getInstance().getTime()) + ".png");
					// 创建并写入临时图片文件（路径见上）
					ImageIO.write(image, "png", tmpImageFile);
					CQ.sendGroupMsg(groupId, new CQCode().image(tmpImageFile));
					tmpImageFile.delete();
					return;
				} else { //如果今日全群和本群都有人签到
					int i = 0,j = 0; //定义要取的Top？临时变量(最高为Top5)
					if (thisDaySignList.size() > 5) { //如果今日总签到人数大于5位
						i = 4; //全群Top？设置为Top5
					} else { //否则
						i = thisDaySignList.size() - 1; //全群Top？设置为总签到人数
					}
					if (thisGroupSignList.size() > 5) { //如果今日本群签到人数大于5位
						j = 4; //本群Top？设置为Top5
					} else { //否则
						j = thisGroupSignList.size() - 1; //本群Top？设置为总签到人数
					}
					//通过匿名的Comparator使数组集合(ArrayList)按照每个文件最后修改的日期时间戳进行数组集合升序排序（下同）
					//本次排序为今日已签到群聊文件夹
					Collections.sort(thisDaySignList, new Comparator<File>()
					{
						@Override
						 public int compare(File o1, File o2)
						 {
							try {
							 /* 你家JVM不判断null心里难受(java.lang.IllegalArgumentException) */
								if (o1 == null)
								{
									return -1;
								} else if (o2 == null) {
									return 1;
								} else
								{
									//按创建日期进行排序
									long dateO1 = Files.readAttributes(o1.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis();
									long dateO2 = Files.readAttributes(o2.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis();
									int result = Long.compare(dateO1, dateO2);
									return result;
								}
							} catch (Exception e) {
									e.printStackTrace();
									return 0;
							}
						}
					});
					//本次排序为今日本群已签到人员文件
					Collections.sort(thisGroupSignList, new Comparator<File>()
					{
						@Override
						 public int compare(File o1, File o2)
						 {
							try {
								 /* 你家JVM不判断null心里难受(java.lang.IllegalArgumentException) */
									if (o1 == null)
									{
										return -1;
									} else if (o2 == null) {
										return 1;
									} else
									{
										//按创建日期进行排序
										long dateO1 = o1.lastModified();
										long dateO2 = o2.lastModified();
										int result = Long.compare(dateO1, dateO2);
										return result;
									}
								} catch (Exception e) {
										e.printStackTrace();
										return 0;
								}
						}
					});
					// 循环写入今日本群Top5
					for (int tempJ = 0; tempJ <= j; tempJ++) {
						switch (tempJ)
						{
						case 0: //No.1
							g2.drawString("No.1", 55, 235);
							g2.drawString(thisGroupSignList.get(tempJ).getName().replace(".signed", ""), 130, 235);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(thisGroupSignList.get(tempJ).lastModified())), 530, 235);
							break;
						case 1: //No.2
							g2.drawString("No.2", 55, 285);
							g2.drawString(thisGroupSignList.get(tempJ).getName().replace(".signed", ""), 130, 285);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(thisGroupSignList.get(tempJ).lastModified())), 530, 285);
							break;
						case 2: //No.3
							g2.drawString("No.3", 55, 335);
							g2.drawString(thisGroupSignList.get(tempJ).getName().replace(".signed", ""), 130, 335);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(thisGroupSignList.get(tempJ).lastModified())), 530, 335);
							break;
						case 3: //No.4
							g2.drawString("No.4", 55, 385);
							g2.drawString(thisGroupSignList.get(tempJ).getName().replace(".signed", ""), 130, 385);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(thisGroupSignList.get(tempJ).lastModified())), 530, 385);
							break;
						case 4: //No.5
							g2.drawString("No.5", 55, 435);
							g2.drawString(thisGroupSignList.get(tempJ).getName().replace(".signed", ""), 130, 435);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(thisGroupSignList.get(tempJ).lastModified())), 530, 435);
							break;
						default:
							break;
						}
					}
					// 循环写入今日全群Top5
					for (int tempI = 0; tempI <= i; tempI++) {
						switch (tempI)
						{
						case 0: //No.1
							g2.drawString("No.1", 660, 235);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 235);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 235);
							break;
						case 1: //No.2
							g2.drawString("No.2", 660, 285);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 285);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 285);
							break;
						case 2: //No.3
							g2.drawString("No.3", 660, 335);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 335);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 335);
							break;
						case 3: //No.4
							g2.drawString("No.4", 660, 385);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 385);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 385);
							break;
						case 4: //No.5
							g2.drawString("No.5", 660, 435);
							g2.drawString(thisDaySignList.get(tempI).getName().replace(".signed", ""), 735, 435);
							g2.drawString(new SimpleDateFormat("HH:mm:ss").format(new Date(Files.readAttributes(thisDaySignList.get(tempI).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).creationTime().toMillis())), 1135, 435);
							break;
						default:
							break;
						}
					}
				}
				g2.setFont(new Font("方正喵呜体", Font.PLAIN, 30)); // 设置字体
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(0, 0, 0));
				String notice = Global.getRandAncment();
				String showNotice = "";
				if (notice.length() > 40) {
					showNotice = notice.substring(0, 36) + "...";
				} else {
					showNotice = notice;
				}
				g2.drawString(showNotice, 56, 670);
				g2.setFont(new Font("微软雅黑", Font.PLAIN, 15)); // 设置字体
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				String nowDateStr = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				g2.drawString(nowDateStr, 20, 710);
				long finishTime = System.nanoTime() / 1000000;
				String generateTime = String.valueOf(finishTime - beginTime);
				g2.drawString(Global.AppName + " v" + Global.Version + " ©御坂12456 Generate: " + generateTime + "ms", 880, 710);
				g2.dispose(); // 保存绘图对象
				File tmpImageFile = 
						new File(Global.appDirectory + "/temp/" + new SimpleDateFormat("YYYYMMddHHmmss").format(Calendar.getInstance().getTime()) + ".png");
				// 创建并写入临时图片文件（路径见上）
				ImageIO.write(image, "png", tmpImageFile);
				CQ.sendGroupMsg(groupId, new CQCode().image(tmpImageFile));
				tmpImageFile.delete();
				return;
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
	}

	/**
	 * 主功能4:实用功能
	 * @author 御坂12456
	 *
	 */
	static class Part4{
		/**
		 * 功能4-1:Bilibili相关功能
		 * @author 御坂12456
		 *
		 */
		static class Part4_1
		{
			public static void Func4_1_main(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					String[] arguments = msg.split(" "); //获取参数（格式类似于"bf 2"）
					String arg1 = arguments[0].toLowerCase(); //获取第1个参数（下标为0）（类似于"bf"）
					switch (arg1)
					{
					case "bf":
						Func4_1_1(CQ, groupId, qqId, msg);
						return;
					case "bavid":
						Func_4_1_2(CQ, groupId, qqId, msg);
						return;
					default:
						return;
					}
				}
				catch (Exception e) { //指令格式错误
					return;
				}
			}

			/**
			 * 功能4-1-1:Bilibili实时粉丝数据
			 * @param CQ CQ实例
			 * @param groupId 消息来源群号
			 * @param qqId 消息来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func4_1_1(CoolQ CQ,long groupId,long qqId,String msg)
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
				}
				return; //最终返回
			}
			/**
			 * 功能4-1-2:Bilibili BV号与AV号互转
			 * @param CQ CQ实例
			 * @param groupId 消息来源群号
			 * @param qqId 消息来源QQ号
			 * @param msg 消息内容
			 */
			public static void Func_4_1_2(CoolQ CQ,long groupId,long qqId,String msg)
			{
				try {
					String arg2 = msg.split(" ", 2)[1].trim();
					if (arg2.toLowerCase().contains("bv")) { //如果是bv号链接(bv转av)
						String bvid = ""; //定义bv号
						if (arg2.contains("BV")) { //如果是大写的BV
							bvid = arg2.split("BV", 2)[1].split("&", 2)[0].split("/",2)[0].split("#",2)[0]; //获取bv号(原理见下)
							/*  原理:
							 * 1.链接格式通常为https://b23.tv/BVa1b2C3D4/p1&xxxxxxxx
							 * 2.按"BV"分隔字符串，得到返回数组下标1的值为"a1b2C3D4/p1&xxxxxxxx"
							 * 3.按"&"再次分隔字符串，得到返回数组下标0的值为"a1b2C3D4/p1"
							 * 4.按"/"再次分隔，得到"a1b2C3D4"
							 * 小写bv和大小写av号同理
							 * 补充:评论区链接还附带#，通过上面的方式同时去掉
							 */
						} else if (arg2.contains("bv")) { //如果是小写的bv
							bvid = arg2.split("bv", 2)[1].split("&", 2)[0].split("/",2)[0].split("#",2)[0]; //获取bv号
						} else {
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"您输入的视频链接格式不正确,请重新输入");
							return;
						}
						JSONObject cidJsonObject = 
								JSONObject.parseObject(JsonHelper.loadJson("https://api.bilibili.com/x/player/pagelist?bvid=" + bvid + "&jsonp=jsonp"));
						//获取包含cid的Json字符串
						if (cidJsonObject.getIntValue("code") == 0) { //成功(0)
							JSONArray videoParts = cidJsonObject.getJSONArray("data"); //获取"data"JSON数组
							long cid = Long.parseLong(videoParts.getJSONObject(0).getString("cid")); //获取视频分P1的cid
							Thread.sleep(50); //暂停50ms线程(防止被B站检测到连续使用api然后导致ip被暂时封禁)
							JSONObject aidJsonObject = 
									JSONObject.parseObject(JsonHelper.loadJson("https://api.bilibili.com/x/web-interface/view?cid=" + cid + "&bvid=" + bvid));
							//获取包含aid(av号)的Json字符串
							if (aidJsonObject.getIntValue("code") == 0) { //成功(0)
								JSONObject videoDetailParts = aidJsonObject.getJSONObject("data"); //获取"data"JSON数组
								long aid = Long.parseLong(videoDetailParts.getString("aid")); //获取视频的av号(aid)
								String title = videoDetailParts.getString("title"); //获取主视频标题("title")
								JSONObject owner = videoDetailParts.getJSONObject("owner"); //获取"owner"JSON对象
								long upUID = owner.getLongValue("mid"); //获取UP主的UID("mid")
								String upName = owner.getString("name"); //获取UP主的昵称("name")
								StringBuilder convertResult = new StringBuilder(FriendlyName).append("\n")
										.append("Bilibili BV转AV号结果\n")
										.append("标题:").append(title).append("\n")
										.append("视频bv号:").append("BV").append(bvid).append("\n")
										.append("视频av号:").append("av").append(aid).append("\n")
										.append("UP主:").append(upName).append("(").append(upUID).append(")"); //定义返回信息
								CQ.sendGroupMsg(groupId, convertResult.toString()); //发送返回信息
								return;
							} else { //失败
								CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
										"转换失败(" + aidJsonObject.getIntValue("code") + ")");
							}
						} else { //失败
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"转换失败(" + cidJsonObject.getIntValue("code") + ")");
						}
					} else if (arg2.toLowerCase().contains("av")) { //如果是av号链接(av转bv)
						String aid = ""; //定义av号
						if (arg2.contains("AV")) { //如果是大写的AV
							aid = arg2.split("AV", 2)[1].split("&", 2)[0].split("/",2)[0].split("#",2)[0]; //获取av号
						} else if (arg2.contains("av")) { //如果是小写的av
							aid = arg2.split("av", 2)[1].split("&", 2)[0].split("/",2)[0].split("#",2)[0]; //获取av号
						} else {
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"您输入的视频链接格式不正确,请重新输入");
							return;
						}
						JSONObject bvidJsonObject = 
								JSONObject.parseObject(JsonHelper.loadJson("https://api.bilibili.com/x/web-interface/view?aid=" + aid));
						//获取包含aid(av号)的Json字符串
						if (bvidJsonObject.getIntValue("code") == 0) { //成功(0)
							JSONObject videoDetailParts = bvidJsonObject.getJSONObject("data"); //获取"data"JSON数组
							String bvid =videoDetailParts.getString("bvid"); //获取视频的av号(aid)
							String title = videoDetailParts.getString("title"); //获取主视频标题("title")
							JSONObject owner = videoDetailParts.getJSONObject("owner"); //获取"owner"JSON对象
							long upUID = owner.getLongValue("mid"); //获取UP主的UID("mid")
							String upName = owner.getString("name"); //获取UP主的昵称("name")
							StringBuilder convertResult = new StringBuilder(FriendlyName).append("\n")
									.append("Bilibili AV转BV号结果\n")
									.append("标题:").append(title).append("\n")
									.append("视频bv号:").append(bvid).append("\n")
									.append("视频av号:").append("av").append(aid).append("\n")
									.append("UP主:").append(upName).append("(").append(upUID).append(")"); //定义返回信息
							CQ.sendGroupMsg(groupId, convertResult.toString()); //发送返回信息
							return;
						} else {
							CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
									"您输入的视频链接格式不正确,请重新输入");
						}
					} else {
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
								"您输入的视频链接格式不正确,请重新输入");
					}
				}catch (IndexOutOfBoundsException e) {
					CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
							"您输入的指令格式有误,请重新输入\n" + 
							"格式:!bavid [视频链接]\n" + 
							"如:!bavid https://www.bilibili.com/av2");
				} catch (Exception e) {
					CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + 
							"获取失败(" + e.getClass().getName() + ")");
					CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
							"详细信息:\n" +
							ExceptionHelper.getStackTrace(e));
				}
				return; //最终返回
			}

		}
		/**
		 * 功能4-2:随机选择选项
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		public static void Func4_2(CoolQ CQ,long groupId,long qqId,String msg)
		{
			try {
				ArrayList<String> selectList = new ArrayList<String>(Arrays.asList(msg.split(" ", 2)[1].split(" "))); //获取待选择的项列表
				Random randSelect = new Random();
				String selectedItemStr = selectList.get(randSelect.nextInt(selectList.size())); //获取随机项
				CQ.sendGroupMsg(groupId, FriendlyName + "\n当然是“" + selectedItemStr + "”咯.");
			} catch (NullPointerException | IllegalArgumentException e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n您输入的指令格式有误,请重新输入\n格式:!slct [选项1] {选项2...}");
			}catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}

	}
	
	/**
	 * 主功能5:娱乐功能
	 * @since 0.5.0
	 * @author 御坂12456
	 *
	 */
	static class Part5
	 {
		  public static void Func5_Main(CoolQ CQ, long groupId, long qqId, String msg)
		  {
				try {
						String[] arguments = msg.split(" "); //获取参数（格式类似于"gm 1 next"）
						String arg2 = arguments[1].toLowerCase(); //获取第2个参数（下标为1）（类似于"1"）
						switch (arg2)
						{
						case "1": //游戏5-1:Internet逃亡拯救行动(Internet Escape Rescue)
							Games.Game5_1(CQ, groupId, qqId, msg);
							break;
						case "list": //游戏列表
							 Func5_GameList(CQ, groupId, qqId, msg);
							 break;
						default: //不存在的黑名单参数2
							throw new IndexOutOfBoundsException("Unknown Func5(Games) Argument 2:" + arg2);
						}
						return;
					}
					catch (IndexOutOfBoundsException e) { //指令格式错误
						CQ.sendGroupMsg(groupId, Global.FriendlyName +  "\n您输入的指令格式有误,请检查后再试\n" +
								"格式:!gm [list/序号]...");
					}
		  }
		  
		  /**
		   * 功能5-List:可用游戏列表
		   * @param CQ CQ实例
		   * @param groupId 消息来源群号
		   * @param qqId 消息来源QQ号
		   * @param msg 消息内容
		   */
		  public static void Func5_GameList(CoolQ CQ,long groupId,long qqId,String msg)
		  {
				StringBuilder resultStr = new StringBuilder(FriendlyName).append("\n")
						  .append("可用游戏列表\n")
						  .append("请前往https://shimo.im/docs/KJcJKyy6kRCHYJjR查看");
				CQ.sendGroupMsg(groupId, resultStr.toString());
				return;
		  }
		  		  
		  /**
		   * 功能5-G:娱乐功能中的所有游戏所在类
		   * @author 御坂12456
		   *
		   */
		  static class Games{

				 /**
				   * 游戏5-G1:Internet逃亡拯救行动(Internet Escape Rescue)
				   * @param CQ      CQ实例
				   * @param groupId 消息来源群号
				   * @param qqId    消息来源QQ号
				   * @param msg     消息内容
				   */
				  public static void Game5_1(CoolQ CQ, long groupId, long qqId, String msg)
				  {
						try {
							 // [start] 起始初始化
							 File gameFolder = new File(Global.appDirectory + "/group/games/1"); //定义全群当前游戏数据目录
							 if (!gameFolder.exists()) { //如果不存在
								  gameFolder.mkdirs(); //创建
							 }
							 File thisGroupFolder = new File(Global.appDirectory + "/group/games/1/" + groupId); //定义当前群当前游戏数据目录
							 if (!thisGroupFolder.exists()) //如果不存在
								  thisGroupFolder.mkdirs(); //创建
							 File thisGroupFinishedStat = new File(Global.appDirectory + "/group/games/1/" + groupId + ".finished"); //定义当前群当前游戏是否完成状态文件
							 // [end]
							 File thisGroupProgressFile = new File(Global.appDirectory + "/group/games/1/" + groupId + "/progress.txt"); //定义当前群当前游戏进度记录文件
							 File pathFile = new File(Global.appDirectory + "/group/games/1/" + groupId + "/nowpath.txt"); //定义当前群当前游戏当前"路径"记录文件
							 if (!thisGroupProgressFile.exists()) { // 如果本群进度记录文件不存在(起始进度-beginning)
								  IOHelper.WriteStr(thisGroupProgressFile, "beginning");
								  CQ.sendGroupMsg(groupId, FriendlyName + "\n游戏正在加载中,请稍候……");
								  Thread.sleep(5000L);
								  IOHelper.WriteStr(thisGroupProgressFile, "1");
								  CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n") 
										  .append("<Internet 逃亡拯救行动>\n")
										  .append("25世纪的某一天,一台计算机里突然发生了异常的混乱.\n")
								  		  .append("混乱的起因并不清楚,但混乱已经引起很多程序开始互相残杀.\n")
								  		  .append("如果不再结束混乱,所有程序最终都将会消失.\n")
								  		  .append("你是一个程序\"1495\".你需要突破重重障碍,去寻找混乱的起因,并通知计算机的管理员huanhuanup协助结束混乱并恢复程序之间的和平.\n") 
								  		  .append("输入!gm 1 next 开始游戏.").toString());
							 } else { // 如果本群进度记录文件存在
								  String arg3;
								  if (!msg.trim().toLowerCase().equals("gm 1")) { //如果存在参数
										arg3 = msg.split(" ")[2];
								  } else { //如果不存在参数
										CQ.sendGroupMsg(groupId, FriendlyName + "\n在游戏过程中无法执行空参数的游戏指令(如\"!gm 1\").");
										return;
								  }
								  String progress = IOHelper.ReadToEnd(thisGroupProgressFile).toLowerCase().trim(); //读取进度
								  switch (arg3)
								  {
								  case "restart":
										IOHelper.DeleteAllFiles(thisGroupFolder); //删除当前群当前游戏数据目录中所有文件
										CQ.sendGroupMsg(groupId, FriendlyName + "\n初始化完毕.\n请输入!gm 1开始游戏.");
										break;
								  case "next":
										// [start] next
										switch (progress)
										{
										case "1": //Step1
											// [start] Step 1
											IOHelper.WriteStr(pathFile, "F:\\Universe\\Network\\Programs\\Unused\\1275\\1495.iso\\recette_chs.exe");
											CQ.sendGroupMsg(groupId,new StringBuilder(FriendlyName).append("\n")
												.append(">>开始\n")
												.append("你发现你在 F:\\Universe\\Network\\Programs\\Unused\\1275\\1495.iso\\recette_chs.exe\n")
												.append("输入 '!gm 1 parent' 尝试进入上一级目录。\n")
												.append("输入 '!gm 1 pwd' 显示你的当前位置。").toString());
											break;
											// [end]
										case "2": //Step2
											// [start] Step 2
											IOHelper.WriteStr(thisGroupProgressFile, "3"); //写入进度
											IOHelper.WriteStr(pathFile,"F:\\Universe\\Network\\Programs\\Unused\\1275\\1495.iso");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>这里有个文件夹!\n")
													.append("你已从'1495.iso'中逃出。.\n")
													.append("现在你在 F:\\Universe\\Network\\Programs\\Unused\\1275\\1495.iso.\n")
													.append("过了一会你发现你现在所在的位置并不是DVD镜像文件,而是一个实实在在的文件夹.\n")
													.append("这个文件夹没受保护!你可以直接前往上级目录。\n")
													.append("输入 '!gm 1 next' 尝试进入上一级目录。")
													.toString());
											break;
											// [end]
										case "3": //Step3
											// [start] Step 3
											IOHelper.WriteStr(pathFile,"F:\\Universe\\Network\\Programs\\Unused\\1275");
											IOHelper.WriteStr(Global.appDirectory + "/group/games/1/" + groupId + "/sign.txt", "Remember, log2 ≈ 0.301\n -By Hana.exe");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>魔鬼追逐赛\n")
													.append("你已从'1275'中逃出.\n")
													.append("现在你在 F:\\Universe\\Network\\Programs\\Unused\\1275.\n")
													.append("突然间,你发现哪里好像有点不对劲……\n")
													.toString());
											Thread.sleep(3000);
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("这个文件夹里有成千上万的程序想跟你一样逃出这个文件夹!\n")
													.append("你发现你确实在他们前面,但是你只有10秒的时间逃离,否则你将会被堵在门的里面.\n")
													.append("与此同时,这里还有一个提示牌.\n")
													.append("输入 '!gm 1 parent' 尝试进入上一级目录。\n")
													.append("输入 '!gm 1 sign' 查看提示牌内容。\n")
													.append("注意:你只有20秒的时间!")
													.toString());
											Thread.sleep(20000);
											if (IOHelper.ReadToEnd(thisGroupProgressFile).equals("3")) {
												IOHelper.DeleteAllFiles(new File(Global.appDirectory + "/group/games/1/" + groupId));
												CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
														.append("哦不,他们来了……\n")
														.append("你被堵在了门的里面。")
														.append("游戏结束\n")
														.append("输入'!gm 1'重新开始游戏.")
														.toString());
												return;
											}
											break;
											// [end]
										case "4":
											// [start] Step 4
											IOHelper.WriteStr(pathFile,"F:\\Universe\\Network\\Programs\\Unused");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>杀毒软件正在扫描中\n").append("你已从'Unused'中逃出.\n")
													.append("现在你在 F:\\Universe\\Network\\Programs\\Unused.\n")
													.append("这破文件夹看起来没那么友好,因为这里有个杀毒软件!").toString());
											Thread.sleep(2000);
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("杀毒软件发现你了.\n")
													.append("现在你必须回答一个问题.\n")
													.append("输入 '!gm 1 parent' 查看问题.").toString());
											break;
											// [end]
										case "5":
											// [start] Step 5
											IOHelper.WriteStr(pathFile,"F:\\Universe\\Network\\Programs");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>我是谁来着?\n")
													.append("你已从'Programs'中逃出.\n")
													.append("现在你在 F:\\Universe\\Network\\Programs, 但....\n")
													.append("你已经忘了你自己是谁了,因为这个文件夹里有很多程序!\n")
													.append("不幸的是,想离开这个文件夹必须正确填写自己的文件名...\n")
													.append("输入 '!gm 1 parent' 尝试想起自己的文件名").toString());
											break;
											// [end]
										case "6":
											// [start] Step 6
											IOHelper.WriteStr(pathFile,"F:\\Universe\\Network");
											CQ.sendGroupMsg(groupId,new StringBuilder(FriendlyName).append("\n")
													.append(">>高危病毒\n")
													.append("你已从'Network'中逃出.\n")
													.append("现在你在 F:\\Universe\\Network.\n")
													.append("然而,这里有一个病毒 WannaCrypt0r v4.1 正在你背后晃悠.\n")
													.append("你必须回答几个问题来增加自己逃跑的速度从而逃出这个病毒的魔掌.\n")
													.append("输入 '!gm 1 parent' 尝试逃进上一级目录。\n")
													.append("你只有60秒的时间.").toString());
											Thread.sleep(60000);
											if (IOHelper.ReadToEnd(thisGroupProgressFile).equals("6")) {
												IOHelper.DeleteAllFiles(new File(Global.appDirectory + "/group/games/1/" + groupId));
												CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
														.append("完了,病毒他来了...\n")
														.append("'Oops, your files has been encrypted!'\n")
														.append("游戏结束\n")
														.append("输入'!gm 1'重新开始游戏.")
														.toString());
												return;
											}
											break;
											// [end]
										case "7":
											// [start] Step 7
											IOHelper.WriteStr(pathFile,"F:\\Universe");
											CQ.sendGroupMsg(groupId,new StringBuilder(FriendlyName).append("\n")
													.append(">>破了BitLocker!\n")
													.append("你已从'Universe'中逃出.\n")
													.append("现在你在 F:\\Universe.\n")
													.append("你现在实际上在一个硬盘分区的根目录(F不是真正的分区),所以BitLocker会拦截你前往上层目录的行为.你需要破了它.\n")
													.append("破解BitLocker需要一定的时间.你将会在3分钟内获知破解成功与否的结果.\n")
													.append("请注意稍后本bot的消息").toString());
											Thread.sleep(90000);
											double r = Math.random();
											if (r >= 0.5D) {
												IOHelper.WriteStr(thisGroupProgressFile, "8"); //写入进度
												CQ.sendGroupMsg(groupId,new StringBuilder(FriendlyName).append("\n")
														.append("破解成功!\n")
														.append("输入 '!gm 1 next' 继续.").toString());
											} else {
												CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
														.append("很遗憾,破解失败了...\n")
														.append("输入 '!4.1 next' 再试一次.")
														.toString());
											}
											break;
											// [end]
										case "8":
											// [start] Step 8
											IOHelper.WriteStr(thisGroupProgressFile, "9");
											IOHelper.WriteStr(pathFile,"\\\\Misaka12456\\F");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>我 在 线 上\n")
													.append("你已从'F'中逃出.\n")
													.append("现在你在 '\\\\Misaka12456\\F'.\n")
													.append("你在线上了(.\n")
													.append("输入 '!gm 1 next' 继续.")
													.toString());
											break;
											// [end]
										case "9":
											// [start] Step 9
											IOHelper.WriteStr(pathFile, "\\\\Misaka12456");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append(">>?\n")
													.append("你已从'Misaka12456'中逃出.\n")
													.append("现在你在 '\\\\Misaka12456'.\n")
													.append("你发现混乱的起因是一个服务'000.exe'在捣乱.\n")
													.append("你现在删不了,但是你可以使用管理员权限删除.\n")
													.append("输入 '!gm 1 ne")
													.toString());
											Thread.sleep(3000);
											IOHelper.WriteStr(thisGroupProgressFile, "10");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("警告!!\n")
													.append("000.exe苏醒了!\n")
													.append("他将会把整个计算机给破坏个一干二净!\n")
													.append("你急需援助才能阻止他的行为!\n")
													.append("你 只 有 2 0 0 秒.\n")
													.append("输入 'gm 1 next' 继续.")
													.toString());
											Thread.sleep(200000);
											if (!new File(Global.appDirectory + "/group/games/1/" + groupId).exists()) {
												return;
											}
											if (!IOHelper.ReadToEnd(thisGroupProgressFile).trim().toLowerCase().equals("ending")) {
												IOHelper.DeleteAllFiles(new File(Global.appDirectory + "/group/games/1/" + groupId));
												CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
														.append("'终于...我醒了...'\n")
														.append("'你个笨蛋!下辈子见吧!哈哈哈哈哈!!'\n")
														.append("000.exe 毁了整个计算机.\n")
														.append("游戏结束\n")
														.append("输入'!gm 1'重新开始游戏.")
														.toString());
												return;
											}
											break;
											// [end]
										case "10":
											// [start] Step 10
											IOHelper.WriteStr(pathFile, "\\\\QQ");
											CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
													"现在你在 \\\\QQ.\n" + 
													"你需要管理员的权限才可删除'000.exe'...\n" + 
													"继续(C) -> 本群任意一管理组成员发送!gm 1 auth");
											break;
											// [end]
										case "11":
											// [start] Step 11
											CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
													"你得到了管理员权限.\n" + 
													"正在尝试删除000.exe...");
											Thread.sleep(2000);
											double r2 = Math.random();
											if (r2 >= 0.6D) {
												IOHelper.WriteStr(thisGroupProgressFile, "ending");
												CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
														"删除成功!\n" + 
														"距离尾声不远了.\n" + 
														"发送 '!gm 1 next' 继续.");
											} else {
												CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
														"删除失败了...\n" + 
														"再试一次? -> 再次发送 '!gm 1 next'");
											}
											break;
											// [end]
										case "ending":
											// [start] ending
											File thisGroupFolderFile = new File(Global.appDirectory + "/group/games/1/" + groupId);
											IOHelper.DeleteAllFiles(thisGroupFolderFile);
											thisGroupFolderFile.delete();
											thisGroupFinishedStat.createNewFile();
											File[] allFinishedGroupFiles = new File(Global.appDirectory + "/group/games/1").listFiles(new FileFilter()
											{
												
												@Override
												public boolean accept(File pathname)
												{
													try {
														String pathString = pathname.getName();
														if (pathString.contains(".finished")) {
															return true;
														} else {
															return false;
														}
													} catch (Exception e) {
														return false;
													}
												}
											});
											int allFinishedGroupsCount = allFinishedGroupFiles.length;
											StringBuilder endStringBuilder = new StringBuilder(FriendlyName).append("\n")
													.append("通关!\n")
													.append("你已完成游戏'Internet逃亡拯救行动'(Internet Escape Rescue).\n")
													.append("感谢游玩!!\n")
													.append("本群是第").append(allFinishedGroupsCount).append("个通关本游戏的群.");
											CQ.sendGroupMsg(groupId, endStringBuilder.toString());
											return;
											// [end]
										}
										// [end]
										break;
									case "parent":
										// [start] parent
										String path = IOHelper.ReadToEnd(pathFile);
										switch (path)
										{
										case "F:\\Universe\\Network\\Programs\\Unused\\1275\\1495.iso\\recette_chs.exe":
											File askingFile = new File(Global.appDirectory + "/group/games/1/" + groupId + "/answer.txt");
											IOHelper.WriteStr(askingFile, "que");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("DVD镜像管理器要求你回答个问题:\n")
													.append("123 SduBotR 宣传标语的第18-20个字符是什么?\n")
													.append("输入 '!gm 1 ans [你的答案]' 回答问题.").toString());
											break;
										case "F:\\Universe\\Network\\Programs\\Unused\\1275":
											new File(Global.appDirectory + "/group/games/1/" + groupId + "/sign.txt").delete();
											IOHelper.WriteStr(thisGroupProgressFile,"4");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("'追不上我,哈哈哈'.\n")
													.append("你逃进了上层目录.")
													.append("输入 '!gm 1 next' 继续.").toString());
											break;
										case "F:\\Universe\\Network\\Programs\\Unused":
											File askingFile2 = new File(Global.appDirectory + "/group/games/1/" + groupId + "/answer.txt");
											IOHelper.WriteStr(askingFile2, "3");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("Windows Defender防病毒程序要求你回答个问题:\n")
													.append("你程序类型是什么?(回答序号即可)\n")
													.append("1.病毒 2.音游 3.Galgame 4.聊天工具\n")
													.append("输入 '!gm 1 ans [你的答案]' 回答问题.").toString());
											break;
										case "F:\\Universe\\Network\\Programs":
											File askingFile3 = new File(Global.appDirectory + "/group/games/1/" + groupId + "/answer.txt");
											IOHelper.WriteStr(askingFile3, "3");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("你需要找出你自己正确的程序文件名.\n")
													.append("下面哪个文件名是正确的?(回答序号即可)\n")
													.append("1.cf.ots123it.open.sdubotr.jar\n")
													.append("2.SiglusEngineCHS.exe\n")
													.append("3.recette_chs.exe\n")
													.append("输入 '!gm 1 ans [你的答案]' 回答问题.").toString());
											break;
										case "F:\\Universe\\Network":
											File askingFile4 = new File(Global.appDirectory + "/group/games/1/" + groupId + "/answer.txt");
											IOHelper.WriteStr(askingFile4, "12");
											CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
													.append("问题 1/3: 下列代码的执行结果是什么?\n")
													.append("int result = (new Date().getYear() > 2037)?153:12\n")
													.append("输入 '!gm 1 ans [你的答案]' 回答问题.")
													.toString());
											break;
										}
										// [end]
										break;
									case "ans":
										// [start] ans
										String answer = msg.split(" ", 4)[3].trim().toLowerCase();
										File ansFile = new File(Global.appDirectory + "/group/games/1/" + groupId + "/answer.txt");
										if (ansFile.exists()) {
											String answerString = IOHelper.ReadToEnd(ansFile);
											if (answer.equals(answerString.toLowerCase().trim())) {
												if (IOHelper.ReadToEnd(pathFile).equals("F:\\Universe\\Network")) {
													switch (answerString)
													{
													case "12":
														IOHelper.WriteStr(ansFile,"1172713");
														CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
																.append("回答正确!")
																.append("问题 2/3: 16进制数'4f5cb'的对应10进制数是多少?\n")
																.append("输入 '!gm 1 ans [你的答案]' 回答问题.")
																.toString());
														break;
													case "1172713":
														IOHelper.WriteStr(ansFile, "Hi!emmm...");
														CQ.sendGroupMsg(groupId, new StringBuilder(FriendlyName).append("\n")
																.append("回答正确!")
																.append("问题 3/3: 下列代码的执行结果是什么?\n")
																.append("String test = \"&#91;CQ:face,id=178&#93;&#91;CQ:emoji,id=128166&#93;emmm...\";\n")
																.append("String outstr = test.replaceAll(\"&#91;CQ:(face|emoji),id=\\d{1,}&#93;?\",\"Hi!\");\n")
																.append("System.out.println(outstr);\n")
																.append("输入 '!gm 1 ans [你的答案]' 回答问题.")
																.toString());
														break;
													case "Hi!emmm...":
														IOHelper.WriteStr(Global.appDirectory + "/group/games/1/" + groupId + "/progress.txt", 
																Long.toString(Long.valueOf(progress) + 1));
														ansFile.delete();
														CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
																"回答正确!\n" + 
																"输入 '!gm 1 next' 继续.");
													default:
														break;
													}
												} else {
													IOHelper.WriteStr(Global.appDirectory + "/group/games/1/" + groupId + "/progress.txt", 
															Long.toString(Long.valueOf(progress) + 1));
													ansFile.delete();
													CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
															  "回答正确!\n" + 
															  "输入 '!gm 1 next' 继续.");
												}
											} else {
												CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
														"抱歉,回答错误....\n" + 
														"再试一次?");
											}
										} else {
											CQ.sendGroupMsg(groupId, FriendlyName + "\nSorry, you don't need to answer any question now.\nPlease check your progress.");
										}
										// [end]
										break;
									case "sign":
										// [start] sign
										File signFile = new File(Global.appDirectory + "/group/games/1/" + groupId + "/sign.txt");
										if (signFile.exists()) {
											String signString = IOHelper.ReadToEnd(signFile);
											signFile.delete();
											CQ.sendGroupMsg(groupId, FriendlyName + "\n你发现了一个提示牌!\n" + signString);
										} else {
											CQ.sendGroupMsg(groupId, FriendlyName + "这里没提示牌...");
										}
										// [end]
										break;
									case "auth":
										// [start] auth
										if (isGroupAdmin(CQ, groupId,qqId)) {
											IOHelper.WriteStr(thisGroupProgressFile, "11");
											CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
													"管理员权限给予成功.\n" + 
													"输入 '!gm 1 next' 继续.");
										} else {
											CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
													"管理员权限给予失败:权限不足.\n" + 
													"请让本群任意一个管理组成员发送'!gm 1 auth'给予权限.");
										}
										// [end]
										break;
									case "pwd":
										// [start] pwd
										String nowPathString = IOHelper.ReadToEnd(pathFile);
										CQ.sendGroupMsg(groupId, FriendlyName + "\n" + 
												"[当前路径] 现在你在:\n" + 
												nowPathString);
										// [end]
										break;
								  default:
										break;
								  }
								  return;
							 }
						} catch (Exception e) {
							 CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
							 CQ.sendGroupMsg(groupId,
										FriendlyName + "\n" + "哦不!出了点问题...(" + e.getClass().getName() + ")");
						}
				  }

		  }
		  
		  
	}

	/**
	 * 主功能6:音游相关功能
	 * @since 0.5.1
	 * @author 御坂12456
	 *
	 */
	static class Part6{
		 
		/**
		 * 功能6-1:音游推荐
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @see ProcessGroupMsg
		 * @author 御坂12456
		 */
		 public static void Func6_1(CoolQ CQ,long groupId,long qqId,String msg)
		 {
				try {
					 DBHelper mugsHelper = new DBHelper(CQ.getAppDirectory() + "/group/mug/mugs.db", DBHelper.SQLite,"SQLite");
					 mugsHelper.Open();
					 ResultSet mugsListSet = mugsHelper.executeQuery("SELECT * FROM muglist ORDER BY RANDOM() limit 1;");
					 String mugName,mugFriendlyName;
					 StringBuilder recommendMugStr = new StringBuilder(FriendlyName).append("\n").append("推荐音游:");
					 mugsListSet.next();
					 mugName = mugsListSet.getString("Name");
					 mugFriendlyName = mugsListSet.getString("FriendlyName");
					 recommendMugStr.append(mugName);
					 if (mugFriendlyName != null) {
						  recommendMugStr.append("(").append(mugFriendlyName).append(")");
					 }
					 CQ.sendGroupMsg(groupId, recommendMugStr.toString());
				} catch (Exception e) {
					 CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
						CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
								"详细信息:\n" +
								ExceptionHelper.getStackTrace(e));
				}
		 }
		 
		 /**
		  * 功能6-2:Arcaea查分相关功能
		  * @author 御坂12456 vs TheSnowfield
		  *
		  */
		 static class Part6_2{
			  public static void Func6_2_Main(CoolQ CQ,long groupId,long qqId,String msg)
			  {
					try {
					 String[] arguments = msg.trim().split(" ");
					 String arg2 = arguments[1];
					 switch (arg2.toLowerCase())
					 {
					 case "bind": //功能6-2-1:绑定Arc账号
						  Func6_2_1(CQ, groupId, qqId, msg);
						  break;
					 case "info": //功能6-2-2:查询最近成绩
						  Func6_2_2(CQ,groupId,qqId,msg);
						  break;
					 case "b30": //功能6-2-3:查询Best30地板
						  Func6_2_3(CQ, groupId, qqId, msg);
						  break;
					 case "connect": //功能6-2-4:查询连接密码
						  Func6_2_4(CQ, groupId, qqId, msg);
						  break;
					 case "help": //功能6-2-Help:查看功能6-2完整帮助
						  Func6_2_Help(CQ, groupId, qqId, msg);
						  break;
					 default: //功能6-2-2:查询最近成绩
							 switch (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg))
							 {
							 case 1: case 2:
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n指令格式错误,格式:!arc [bind|info|b30|connect|help]");
									 break;
								default:
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n指令格式错误,格式:!arc [bind|info|b30|help]");
							 }						  
							 break;
					 }
				} catch (Exception e) {
					 if (msg.trim().toLowerCase().equals("arc")) {
						  Func6_2_2(CQ, groupId, qqId, msg);
					 } else {
							 switch (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg))
							 {
							 case 1: case 2:
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n指令格式错误,格式:!arc [bind|info|b30|connect|help]");
									 break;
								default:
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n指令格式错误,格式:!arc [bind|info|b30|help]");
							 }
					 }
				}
			 }
			 /**
			  * 功能6-2-1:绑定Arc账号
			  * @param CQ CQ实例，详见本大类注释
			  * @param groupId 消息来源群号
			  * @param qqId 消息来源成员QQ号
			  * @param msg 消息内容
			  * @see ProcessGroupMsg
			  * @author 御坂12456
			  */
			 public static void Func6_2_1(CoolQ CQ,long groupId,long qqId,String msg)
				{
					 try {
						  if (checkDependency(CQ)) { //如果依赖项已安装
								String[] arguments = msg.split(" ");
								String arg3 = arguments[2]; //获取参数3(玩家id)
								if (CommonHelper.isInteger(arg3)) { // 如果玩家id参数是数字
									 StringBuilder playerIdBuilder = new StringBuilder("http://127.0.0.1:61666/v2/userinfo?usercode=")
												.append(arg3);
									 String result = JsonHelper.loadJson(playerIdBuilder.toString());
									 JSONObject resultObject = JSONObject.parseObject(result);
									 int status = resultObject.getIntValue("status");
									 switch (status)
									 {
									 case 0:
										  String name = resultObject.getJSONObject("content").getString("name");
										  Global.GlobalDatabases.dbgroup_mug_arcaea.executeNonQuerySync("INSERT OR REPLACE INTO Players ('QQId','Aid') VALUES ('" + qqId + "','" + arg3 + "');");
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "绑定成功:" + name);
										  break;
									 case -1: //Invalid Usercode
											 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "输入的玩家id不正确,应为9位数字id(暂不支持昵称反查id功能),如000000001");
											 break;
									 default:
											 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "绑定失败，未知错误(" + status + ")");
											 break;
									 }
								} else {
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "输入的玩家id不正确,应为9位数字id(暂不支持昵称反查id功能),如000000001");
								}
						  } else {
								CQ.logError(AppName,
										  "尝试执行Func6_2_1时出错: 'BotArcApi' 未运行" + "\n" + "缺少必需的依赖项: Node.js (路径"
													 + CQ.getAppDirectory() + "system\\dependency\\nodejs)" + "\n"
													 + "请使用123SduBotR Installer安装依赖项后重启Bot程序.");
								CQ.sendGroupMsg(groupId,
											 FriendlyName + "\n" + new CQCode().at(qqId) + " 绑定失败(Internal:Require dependency: Node.js)");
						  }
					 } catch (SocketTimeoutException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_1时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 绑定失败(Internal:BotArcApi isn't running)");
					 } catch (IOException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_1时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 绑定失败(Internal:BotArcApi isn't running)");
					 } catch (IndexOutOfBoundsException e) {
						  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "指令格式不正确,格式:!arc bind [玩家9位数字id]"); 
					 } catch (Exception e) {
						  CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
						  CQ.sendGroupMsg(groupId,
									 FriendlyName + "\n" + new CQCode().at(qqId) + " 绑定失败(" + e.getClass().getName() + ")");
					 }
				}
		

			 /**
			  * 功能6-2-2:查询最近成绩
			  * @param CQ CQ实例，详见本大类注释
			  * @param groupId 消息来源群号
			  * @param qqId 消息来源成员QQ号
			  * @param msg 消息内容
			  * @see ProcessGroupMsg
			  * @author 御坂12456
			  */
			 public static void Func6_2_2(CoolQ CQ,long groupId,long qqId,String msg)
			 {
					 try {
						  if (checkDependency(CQ)) { //如果依赖项已安装
								ResultSet playerSet = GlobalDatabases.dbgroup_mug_arcaea.executeQuery("SELECT Aid FROM Players WHERE QQId=" + qqId + ";");
								if (playerSet.next()) {
									 int playerId = playerSet.getInt("Aid");
									 String playerIdStr = Integer.toString(playerId);
									 if (playerIdStr.length() < 9) {
										  int zeroTimes = 9 - playerIdStr.length();
										  for (int i = 0; i < zeroTimes; i++) {
												playerIdStr = "0" + playerIdStr;
										  }
									 }
									 StringBuilder playerIdBuilder = new StringBuilder("http://127.0.0.1:61666/v2/userinfo?usercode=")
												.append(playerIdStr).append("&recent=true");
									 String result = JsonHelper.loadJson(playerIdBuilder.toString());
									 JSONObject resultObject = JSONObject.parseObject(result);
									 int status = resultObject.getIntValue("status");
									 switch (status)
									 {
									 case 0:
										  int argsCount = msg.trim().split(" ").length;
										  if (argsCount > 2) { //玩家查询的不是最近游玩成绩而是指定的曲目及难度的最好游玩成绩
												JSONObject singleSongResultObject = null;
												if ((msg.toLowerCase().contains("ftr")) | (msg.toLowerCase().contains("future"))
															 | (msg.toLowerCase().contains("byn")) | (msg.toLowerCase().contains("byd")) | (msg.toLowerCase().contains("beyond"))
															 | (msg.toLowerCase().contains("prs")) | (msg.toLowerCase().contains("present"))
															 | (msg.toLowerCase().contains("pst")) | (msg.toLowerCase().contains("past"))) { //玩家指定了要查询的曲目的难度
													 String songDiff;
													 if ((msg.toLowerCase().contains("ftr")) | (msg.toLowerCase().contains("future"))) {
														  songDiff = "2";
														  msg = msg.toLowerCase().replace("ftr","").replace("future", "");
													 } else if ((msg.toLowerCase().contains("byn")) | (msg.toLowerCase().contains("byd")) | (msg.toLowerCase().contains("beyond"))) {
														  songDiff = "3";
														  msg = msg.toLowerCase().replace("byn","").replace("byd", "").replace("beyond", "");
													 } else if ((msg.toLowerCase().contains("prs")) | (msg.toLowerCase().contains("present")))
													 {
														  songDiff = "1";
														  msg = msg.toLowerCase().replace("prs","").replace("present", "");
													 } else if ((msg.toLowerCase().contains("pst")) | (msg.toLowerCase().contains("past"))) {
														  songDiff = "0";
														  msg = msg.toLowerCase().replace("pst","").replace("past", "");
													 } else {
														  songDiff = "2";
													 }
													 String songName = msg.split(" ",3)[2];
													 String singleSongresult = JsonHelper.loadJson("http://127.0.0.1:61666/v2/userbest?usercode=" + playerIdStr
																+ "&songname=" + URLEncoder.encode(songName) + "&difficulty=" + songDiff);
													 singleSongResultObject = JSONObject.parseObject(singleSongresult);
												} else { //玩家没有指定难度(默认Future)
													 String songName = msg.split(" ",3)[2];
													 String singleSongresult = JsonHelper.loadJson("http://127.0.0.1:61666/v2/userbest?usercode=" + playerIdStr
																+ "&songname=" + URLEncoder.encode(songName)  + "&difficulty=2");
													 singleSongResultObject = JSONObject.parseObject(singleSongresult);
												}
												int status1 = singleSongResultObject.getIntValue("status");
												switch (status1)
												{
												case 0: //Everything is OK
														String name = resultObject.getJSONObject("content").getString("name"); //玩家昵称
														double potential = resultObject.getJSONObject("content").getBigDecimal("rating").divide(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); //玩家潜力值
														JSONObject selectedScore = singleSongResultObject.getJSONObject("content");
														String selected_songid = selectedScore.getString("song_id");
														String selected_songname;
														String selected_difficulty = "[";
														switch (selectedScore.getIntValue("difficulty"))
														{
														case 0:
															 selected_difficulty += "Past ";
															 break;
														case 1:
															 selected_difficulty += "Present ";
															 break;
														case 2:
															 selected_difficulty += "Future ";
															 break;
														case 3:
															 selected_difficulty += "Beyond ";
															 break;
														}
														int selected_bigpure = selectedScore.getIntValue("shiny_perfect_count");
														int selected_pure = selectedScore.getIntValue("perfect_count");
														int selected_far = selectedScore.getIntValue("near_count"); 
														int selected_lost = selectedScore.getIntValue("miss_count");
														String selected_cleartype = ""; //成绩回忆条类型(评级:PM/FR/HC/NC/EC/TL)
														switch (selectedScore.getIntValue("clear_type"))
														{
														case 0: //Track Lost
															 selected_cleartype = "L";
															 break;
														case 4: //Easy Clear
															 selected_cleartype = "EC";
															 break;
														case 1: //Normal Clear
															 selected_cleartype = "NC";
															 break;
														case 5: //Hard Clear
															 selected_cleartype = "HC";
															 break;
														case 2: //Full Recall
															 selected_cleartype = "F";
															 break;
														case 3: //Pure Memory
															 selected_cleartype = "P";
															 break;
														default:
															 break;
														}
														Date selected_timeplayed_date = new Date(selectedScore.getLongValue("time_played")); //游玩日期时间
														Calendar selected_timeplayed = Calendar.getInstance();
														selected_timeplayed.setTime(selected_timeplayed_date);
														int selected_diffrating = 0; //曲目难度定级
														BigDecimal selected_rating_bigdec = new BigDecimal(selectedScore.getString("rating")); //实际潜力值
														double selected_rating = selected_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); //实际潜力值(四舍五入保留两位小数)
														String selected_level = ""; //成绩评级(EX+/EX/AA/A/B/C/D)
														long selected_score = selectedScore.getLongValue("score"); //成绩得分(如10001540)
														if (selected_score >= 9900000) { //EX+评级(990w+)
															 selected_level = "EX+";
														} else if ((selected_score >= 9800000) & (selected_score < 9900000)) { //EX评级(980w-9899999)
															 selected_level = "EX";
														} else if ((selected_score >= 9500000) & (selected_score < 9800000)) { //AA评级(950w-9799999)
															 selected_level = "AA";
														} else if ((selected_score >= 9200000) & (selected_score < 9500000)) { //A评级(920w-9499999)
															 selected_level = "A";
														} else if ((selected_score >= 8900000) & (selected_score < 9200000)) { //B评级(890w-9199999)
															 selected_level = "B";
														} else if ((selected_score >= 8600000) & (selected_score < 8900000)) { //C评级(860w-8899999)
															 selected_level = "C";
														} else if (selected_score < 8600000) { //D评级(8599999-)
															 selected_level = "D";
														}
														String songdetails = JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + selected_songid);
														JSONObject songDetailsObject = JSONObject.parseObject(songdetails);
														selected_songname = songDetailsObject.getJSONObject("content").getJSONObject("title_localized").getString("en");
														switch (selectedScore.getIntValue("difficulty"))
														{
														case 0: //Past
															 selected_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(0).getIntValue("rating");
															 selected_difficulty = selected_difficulty + selected_diffrating;
																	if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(0).containsKey("ratingPlus")) {
																	 selected_difficulty = selected_difficulty +  "+";
																}
															 break;
														case 1: //Present
															 selected_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(1).getIntValue("rating");
															 selected_difficulty = selected_difficulty + selected_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(1).containsKey("ratingPlus")) {
																	 selected_difficulty = selected_difficulty +  "+";
																}
															 break;
														case 2: //Future
															 selected_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(2).getIntValue("rating");
															 selected_difficulty = selected_difficulty + selected_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(2).containsKey("ratingPlus")) {
																	 selected_difficulty = selected_difficulty +  "+";
																}
															 break;
														case 3: //Beyond
															 selected_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(3).getIntValue("rating");
															 selected_difficulty = selected_difficulty + selected_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(3).containsKey("ratingPlus")) {
																	 selected_difficulty = selected_difficulty +  "+";
																}
															 break;
														default:
															 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "查询失败，未知错误(" + status + ")");
															 break;
														}
														selected_difficulty += "]";
														/* 指定曲目最佳成绩相关变量
														 * name 玩家昵称
														 * potential 玩家潜力值
														 * selected_songname 游玩曲目名
														 * selected_difficulty 难度名(如Beyond 11)
														 * selected_cleartype 回忆条类型(PM/FR/HC/NC/EC/TL)
														 * selected_level 成绩评级(EX+/EX/AA/A/B/C/D)
														 * selected_score 成绩分数
														 * selected_bigpure 大P数
														 * selected_pure P数
														 * selected_far F数
														 * selected_lost L数
														 * selected_timeplayed 游玩日期时间
														 * selected_rating 实际游玩定数(保留两位小数)
														 */
														StringBuilder selectedStr = new StringBuilder(FriendlyName).append("\n")
																  .append("Arcaea - 最佳游玩成绩\n")
																  .append("用户名: ").append(name).append(" (").append(potential).append(")\n")
																  .append("曲名: ").append(selected_songname).append(" ").append(selected_difficulty).append("\n")
																  .append("分数: ").append(selected_score).append(" ").append("(").append(selected_level).append("/").append(selected_cleartype).append(")\n")
																  .append("Pure: ").append(selected_pure).append(" (+").append(selected_bigpure).append(")\n")
																  .append("Far: ").append(selected_far).append("\n")
																  .append("Lost: ").append(selected_lost).append("\n")
																  .append("Potential: ").append(selected_rating).append("\n")
																  .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(selected_timeplayed.getTime()));
														String selectedString = selectedStr.toString()
																  .replace("(-0.01)", "(--)");
														if (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg) != 1) {
															 selectedString = selectedString.replace("Tempestissimo [Past 6]", "? [Past ?]")
																		.replace("Tempestissimo [Present 9]", "? [Present ?]")
																		.replace("Tempestissimo [Future 10]", "? [Future ?]")
																		.replace("Tempestissimo [Beyond 11]", "? [Beyond ?]");
														}
														 	 CQ.sendGroupMsg(groupId, selectedString);
														break;
												case -1: //Invalid Usercode
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "绑定的账号无效,请使用!arc bind [玩家id]重新绑定");
													 break;
												case -2: //Invalid Songname
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "未找到对应曲目信息,请检查曲目名称输入是否正确");
													 break;
												case -3:
												case -4: //Invalid Song Difficulty
													  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "未找到对应难度信息,请检查难度是否键入正确(支持的难度名:Past/Present/Future/Beyond)");
													 break;
												case -5: //The song isnot recorded in the database
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "未找到对应曲目信息,可能暂未收录进数据库");
													 break;
												case -6: //The song alias is the aliases of many songs
													 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "查询结果过多,请提供更准确的曲名");
													 break;
												case -8: //The song has no Beyond level
													  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "该曲目不存在Beyond难度");
													 break;
												case -14: //Not played yet
													  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "您还没有游玩过这个难度呢");
													 break;
												default: //Other Error
													 break;
												}
										  } else {
												  if (result.contains("recent_score")) { //如果存在最近游玩成绩
														String name = resultObject.getJSONObject("content").getString("name"); //玩家昵称
														double potential = resultObject.getJSONObject("content").getBigDecimal("rating").divide(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); //玩家潜力值
														JSONObject recentScore = resultObject.getJSONObject("content").getJSONObject("recent_score"); //最近游玩成绩
														String recent_songid = recentScore.getString("song_id"); //最近游玩曲目id
														String recent_songname; //最近游玩曲目名
														String recent_difficulty = "["; //最近游玩曲目难度
														switch (recentScore.getIntValue("difficulty"))
														{
														case 0:
															 recent_difficulty += "Past ";
															 break;
														case 1:
															 recent_difficulty += "Present ";
															 break;
														case 2:
															 recent_difficulty += "Future ";
															 break;
														case 3:
															 recent_difficulty += "Beyond ";
															 break;
														}
														int recent_bigpure = recentScore.getIntValue("shiny_perfect_count"); //最近游玩成绩大P数
														int recent_pure = recentScore.getIntValue("perfect_count"); //最近游玩成绩Pure数
														int recent_far = recentScore.getIntValue("near_count"); //最近游玩成绩Far数
														int recent_lost = recentScore.getIntValue("miss_count"); //最近游玩成绩Lost数
														String recent_cleartype = ""; //最近游玩成绩回忆条类型(评级:PM/FR/HC/NC/EC/TL)
														switch (recentScore.getIntValue("clear_type"))
														{
														case 0: //Track Lost
															 recent_cleartype = "L";
															 break;
														case 4: //Easy Clear
															 recent_cleartype = "EC";
															 break;
														case 1: //Normal Clear
															 recent_cleartype = "NC";
															 break;
														case 5: //Hard Clear
															 recent_cleartype = "HC";
															 break;
														case 2: //Full Recall
															 recent_cleartype = "F";
															 break;
														case 3: //Pure Memory
															 recent_cleartype = "P";
															 break;
														default:
															 break;
														}
														Date recent_timeplayed_date = new Date(recentScore.getLongValue("time_played")); //最近游玩日期时间
														Calendar recent_timeplayed = Calendar.getInstance();
														recent_timeplayed.setTime(recent_timeplayed_date);
														int recent_diffrating = 0; //最近游玩曲目难度定级
														BigDecimal recent_rating_bigdec = new BigDecimal(recentScore.getString("rating")); //最近游玩成绩实际潜力值
														double recent_rating = recent_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); //最近游玩成绩实际潜力值(四舍五入保留两位小数)
														String recent_level = ""; //最近游玩成绩评级(EX+/EX/AA/A/B/C/D)
														long recent_score = recentScore.getLongValue("score"); //最近游玩成绩得分(如10001540)
														if (recent_score >= 9900000) { //EX+评级(990w+)
															 recent_level = "EX+";
														} else if ((recent_score >= 9800000) & (recent_score < 9900000)) { //EX评级(980w-9899999)
															 recent_level = "EX";
														} else if ((recent_score >= 9500000) & (recent_score < 9800000)) { //AA评级(950w-9799999)
															 recent_level = "AA";
														} else if ((recent_score >= 9200000) & (recent_score < 9500000)) { //A评级(920w-9499999)
															 recent_level = "A";
														} else if ((recent_score >= 8900000) & (recent_score < 9200000)) { //B评级(890w-9199999)
															 recent_level = "B";
														} else if ((recent_score >= 8600000) & (recent_score < 8900000)) { //C评级(860w-8899999)
															 recent_level = "C";
														} else if (recent_score < 8600000) { //D评级(8599999-)
															 recent_level = "D";
														}
														String songdetails = JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + recent_songid);
														JSONObject songDetailsObject = JSONObject.parseObject(songdetails);
														recent_songname = songDetailsObject.getJSONObject("content").getJSONObject("title_localized").getString("en");
														switch (recentScore.getIntValue("difficulty"))
														{
														case 0: //Past
															 recent_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(0).getIntValue("rating");
																recent_difficulty = recent_difficulty + recent_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(0).containsKey("ratingPlus")) {
																	 recent_difficulty = recent_difficulty +  "+";
																}
																break;
														case 1: //Present
															 recent_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(1).getIntValue("rating");
															 recent_difficulty = recent_difficulty + recent_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(1).containsKey("ratingPlus")) {
																	 recent_difficulty = recent_difficulty +  "+";
																}
															 break;
														case 2: //Future
															 recent_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(2).getIntValue("rating");
															 recent_difficulty = recent_difficulty + recent_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(2).containsKey("ratingPlus")) {
																	 recent_difficulty = recent_difficulty +  "+";
																}
															 break;
														case 3: //Beyond
															 recent_diffrating = songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(3).getIntValue("rating");
															 recent_difficulty = recent_difficulty + recent_diffrating;
																if (songDetailsObject.getJSONObject("content").getJSONArray("difficulties").getJSONObject(3).containsKey("ratingPlus")) {
																	 recent_difficulty = recent_difficulty +  "+";
																}
															 break;
														default:
															 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "查询失败，未知错误(" + status + ")");
															 break;
														}
														recent_difficulty = recent_difficulty + "]";
														/* 最近成绩相关变量
														 * name 玩家昵称
														 * potential 玩家潜力值
														 * recent_songname 最近游玩曲目名
														 * recent_difficulty 难度名(如Beyond 11)
														 * recent_cleartype 回忆条类型(PM/FR/HC/NC/EC/TL)
														 * recent_level 成绩评级(EX+/EX/AA/A/B/C/D)
														 * recent_score 成绩分数
														 * recent_bigpure 大P数
														 * recent_pure P数
														 * recent_far F数
														 * recent_lost L数
														 * recent_timeplayed 游玩日期时间
														 * recent_rating 实际游玩定数(保留两位小数)
														 */
														StringBuilder recentStr = new StringBuilder(FriendlyName).append("\n")
																  .append("Arcaea - 最近游玩成绩\n")
																  .append("用户名: ").append(name).append("(").append(potential).append(")\n")
																  .append("曲名: ").append(recent_songname).append(" ").append(recent_difficulty).append("\n")
																  .append("分数: ").append(recent_score).append(" ").append("(").append(recent_level).append("/").append(recent_cleartype).append(")\n")
																  .append("Pure: ").append(recent_pure).append(" (+").append(recent_bigpure).append(")\n")
																  .append("Far: ").append(recent_far).append("\n")
																  .append("Lost: ").append(recent_lost).append("\n")
																  .append("Potential: ").append(recent_rating).append("\n")
																  .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recent_timeplayed.getTime()));
														/* 示例
														 * 【123 SduBotR 1.0.0】
														 * Arcaea - 最近游玩成绩
														 * 用户名: Misaka12456 (11.07)
														 * 曲名: Maze No.9 (Future 8)
														 * 分数: 10000841
														 * Pure: 841 (+841)
														 * Far: 0
														 * Lost: 0
														 * Potential: 10.90
														 * Time: 2020-05-01 15:26:24
														 */
														if (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg) == 0) {
															 String recentString = recentStr.toString().replace("Tempestissimo[Past 6]", "? [Past ?]")
																		.replace("Tempestissimo [Present 9]", "? [Present ?]")
																		.replace("Tempestissimo [Future 10]", "? [Future ?]")
																		.replace("Tempestissimo [Beyond 11]", "? [Beyond ?]")
																		.replace("(-0.01)", "(--)");
															 CQ.sendGroupMsg(groupId, recentString);
														} else {
															 CQ.sendGroupMsg(groupId, recentStr.toString()
																		.replace("(-0.01)", "(--)"));
														}
														Part6_2EasterEgg.Func6_2_UnlockConnect(CQ, groupId, recent_songid, recent_diffrating
																  ,recentScore.getIntValue("clear_type"), recent_bigpure, null);
												  } else {
														 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "您还没有游玩过任何一个曲目呢!");
												  }
										  }
										  break;
									 case -1:
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "绑定的账号无效,请使用!arc bind [玩家id]重新绑定");
									 default:
											 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "查询失败，未知错误(" + status + ")");
											 break;
									 }
								} else {
									 CQ.sendGroupMsg(groupId,
												 FriendlyName + "\n" + new CQCode().at(qqId) + "您还未绑定您的Arcaea帐号,请使用!arc bind [玩家9位数字id]进行绑定");
								}
						  } else {
								CQ.logError(AppName,
										  "尝试执行Func6_2_2时出错: 'BotArcApi' 未运行" + "\n" + "缺少必需的依赖项: Node.js (路径"
													 + CQ.getAppDirectory() + "system\\dependency\\nodejs)" + "\n"
													 + "请使用123SduBotR Installer安装依赖项后重启Bot程序.");
								CQ.sendGroupMsg(groupId,
											 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:Require dependency: Node.js)");
						  }
					 } catch (SocketTimeoutException e) {
						  e.printStackTrace();
							CQ.logError(AppName,
									  "尝试执行Func6_2_2时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:BotArcApi isn't running)");
					 } catch (IOException e) {
						  e.printStackTrace();
							CQ.logError(AppName,
									  "尝试执行Func6_2_2时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:BotArcApi isn't running)");
					 } catch (IndexOutOfBoundsException e) {
						  e.printStackTrace();
						  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "指令格式不正确,格式:!arc info {曲名}"); 
					 } catch (Exception e) {
						  CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
						  CQ.sendGroupMsg(groupId,
									 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(" + e.getClass().getName() + ")");
					 }
				}

			 /**
			  * 功能6-2-3:查询Best30地板
			  * @param CQ CQ实例，详见本大类注释
			  * @param groupId 消息来源群号
			  * @param qqId 消息来源成员QQ号
			  * @param msg 消息内容
			  * @see ProcessGroupMsg
			  * @author 御坂12456
			  */
			 public static void Func6_2_3(CoolQ CQ,long groupId,long qqId,String msg)
			 {

					 try {
						  if (checkDependency(CQ)) { //如果依赖项已安装
								ResultSet playerSet = GlobalDatabases.dbgroup_mug_arcaea.executeQuery("SELECT Aid FROM Players WHERE QQId=" + qqId + ";");
								if (playerSet.next()) {
									 CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "Best30数据正在查询中,由于数据量大,请耐心等待;请不要尝试在bot处理过程中让bot执行任何指令。");
									 int playerId = playerSet.getInt("Aid");
									 String playerIdStr = Integer.toString(playerId);
									 if (playerIdStr.length() < 9) {
										  int zeroTimes = 9 - playerIdStr.length();
										  for (int i = 0; i < zeroTimes; i++) {
												playerIdStr = "0" + playerIdStr;
										  }
									 }
									 String result = JsonHelper.loadJson("http://127.0.0.1:61666/v2/userbest30?usercode=" + playerIdStr); //获取B30数据
									 JSONObject b30Object = JSONObject.parseObject(result);
									 int status = b30Object.getIntValue("status");
									 switch (status)
									 {
									 case 0:
										  double b30_avg = b30Object.getJSONObject("content").getDoubleValue("best30_avg"); //B30平均值
										  double r10_avg = b30Object.getJSONObject("content").getDoubleValue("recent10_avg"); //R10平均值
										  JSONArray b30_list = b30Object.getJSONObject("content").getJSONArray("best30_list"); //B30列表
										  int b30_lastcount = b30_list.size() - 1; //B30最后一项编号
										  int b30_floorStartCount = b30_lastcount - 4; //B30地板第一项编号
										  if (b30_floorStartCount < 0) {
												CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "您的Best30数据不完整,请先游玩几首曲目后再尝试执行查询操作.");
										  } else {
												// [start] B30Floor1
												BigDecimal b30Floor1_rating_bigdec = new BigDecimal( b30_list.getJSONObject(b30_floorStartCount).getString("rating")); 
												double b30Floor1_rating = b30Floor1_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); 
												String b30Floor1_songid = b30_list.getJSONObject(b30_floorStartCount).getString("song_id");
												String b30Floor1_difficulty = "";
												switch (b30_list.getJSONObject(b30_floorStartCount).getIntValue("difficulty"))
												{
												case 0: //Past
													 b30Floor1_difficulty = "[PST]";
													 break;
												case 1: //Present
													 b30Floor1_difficulty = "[PRS]";
													 break;
												case 2: //Future
													 b30Floor1_difficulty = "[FTR]";
													 break;
												case 3: //Beyond
													 b30Floor1_difficulty = "[BYD]";
													 break;
												}
												long b30Floor1_score = b30_list.getJSONObject(b30_floorStartCount).getLongValue("score");
												String b30Floor1_level = "";
												if (b30Floor1_score >= 9900000) { //EX+评级(990w+)
													 b30Floor1_level = "EX+";
												} else if ((b30Floor1_score >= 9800000) & (b30Floor1_score < 9900000)) { //EX评级(980w-9899999)
													 b30Floor1_level = "EX";
												} else if ((b30Floor1_score >= 9500000) & (b30Floor1_score < 9800000)) { //AA评级(950w-9799999)
													 b30Floor1_level = "AA";
												} else if ((b30Floor1_score >= 9200000) & (b30Floor1_score < 9500000)) { //A评级(920w-9499999)
													 b30Floor1_level = "A";
												} else if ((b30Floor1_score >= 8900000) & (b30Floor1_score < 9200000)) { //B评级(890w-9199999)
													 b30Floor1_level = "B";
												} else if ((b30Floor1_score >= 8600000) & (b30Floor1_score < 8900000)) { //C评级(860w-8899999)
													 b30Floor1_level = "C";
												} else if (b30Floor1_score < 8600000) { //D评级(8599999-)
													 b30Floor1_level = "D";
												}
												String b30Floor1_clearType = ""; 
												switch (b30_list.getJSONObject(b30_floorStartCount).getIntValue("clear_type"))
												{
												case 0: //Track Lost
													 b30Floor1_clearType = "L";
													 break;
												case 4: //Easy Clear
													 b30Floor1_clearType = "EC";
													 break;
												case 1: //Normal Clear
													 b30Floor1_clearType = "NC";
													 break;
												case 5: //Hard Clear
													 b30Floor1_clearType = "HC";
													 break;
												case 2: //Full Recall
													 b30Floor1_clearType = "F";
													 break;
												case 3: //Pure Memory
													 b30Floor1_clearType = "P";
													 break;
												default:
													 break;
												}
												int b30Floor1_far = b30_list.getJSONObject(b30_floorStartCount).getIntValue("near_count");
												int b30Floor1_lost = b30_list.getJSONObject(b30_floorStartCount).getIntValue("miss_count");
												// [end]
												// [start] B30Floor2
												BigDecimal b30Floor2_rating_bigdec = new BigDecimal( b30_list.getJSONObject(b30_floorStartCount + 1).getString("rating")); 
												double b30Floor2_rating = b30Floor2_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); 
												String b30Floor2_songid = b30_list.getJSONObject(b30_floorStartCount + 1).getString("song_id");
												String b30Floor2_difficulty = "";
												switch (b30_list.getJSONObject(b30_floorStartCount + 1).getIntValue("difficulty"))
												{
												case 0: //Past
													 b30Floor2_difficulty = "[PST]";
													 break;
												case 1: //Present
													 b30Floor2_difficulty = "[PRS]";
													 break;
												case 2: //Future
													 b30Floor2_difficulty = "[FTR]";
													 break;
												case 3: //Beyond
													 b30Floor2_difficulty = "[BYD]";
													 break;
												}
												long b30Floor2_score = b30_list.getJSONObject(b30_floorStartCount + 1).getLongValue("score");
												String b30Floor2_level = "";
												if (b30Floor2_score >= 9900000) { //EX+评级(990w+)
													 b30Floor2_level = "EX+";
												} else if ((b30Floor2_score >= 9800000) & (b30Floor2_score < 9900000)) { //EX评级(980w-9899999)
													 b30Floor2_level = "EX";
												} else if ((b30Floor2_score >= 9500000) & (b30Floor2_score < 9800000)) { //AA评级(950w-9799999)
													 b30Floor2_level = "AA";
												} else if ((b30Floor2_score >= 9200000) & (b30Floor2_score < 9500000)) { //A评级(920w-9499999)
													 b30Floor2_level = "A";
												} else if ((b30Floor2_score >= 8900000) & (b30Floor2_score < 9200000)) { //B评级(890w-9199999)
													 b30Floor2_level = "B";
												} else if ((b30Floor2_score >= 8600000) & (b30Floor2_score < 8900000)) { //C评级(860w-8899999)
													 b30Floor2_level = "C";
												} else if (b30Floor2_score < 8600000) { //D评级(8599999-)
													 b30Floor2_level = "D";
												}
												String b30Floor2_clearType = ""; 
												switch (b30_list.getJSONObject(b30_floorStartCount + 1).getIntValue("clear_type"))
												{
												case 0: //Track Lost
													 b30Floor2_clearType = "L";
													 break;
												case 4: //Easy Clear
													 b30Floor2_clearType = "EC";
													 break;
												case 1: //Normal Clear
													 b30Floor2_clearType = "NC";
													 break;
												case 5: //Hard Clear
													 b30Floor2_clearType = "HC";
													 break;
												case 2: //Full Recall
													 b30Floor2_clearType = "F";
													 break;
												case 3: //Pure Memory
													 b30Floor2_clearType = "P";
													 break;
												default:
													 break;
												}
												int b30Floor2_far = b30_list.getJSONObject(b30_floorStartCount + 1).getIntValue("near_count");
												int b30Floor2_lost = b30_list.getJSONObject(b30_floorStartCount + 1).getIntValue("miss_count");
												// [end]
												// [start] B30Floor3
												BigDecimal b30Floor3_rating_bigdec = new BigDecimal( b30_list.getJSONObject(b30_floorStartCount + 2).getString("rating")); 
												double b30Floor3_rating = b30Floor3_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); 
												String b30Floor3_songid = b30_list.getJSONObject(b30_floorStartCount + 2).getString("song_id");
												String b30Floor3_difficulty = "";
												switch (b30_list.getJSONObject(b30_floorStartCount + 2).getIntValue("difficulty"))
												{
												case 0: //Past
													 b30Floor3_difficulty = "[PST]";
													 break;
												case 1: //Present
													 b30Floor3_difficulty = "[PRS]";
													 break;
												case 2: //Future
													 b30Floor3_difficulty = "[FTR]";
													 break;
												case 3: //Beyond
													 b30Floor3_difficulty = "[BYD]";
													 break;
												}
												long b30Floor3_score = b30_list.getJSONObject(b30_floorStartCount + 2).getLongValue("score");
												String b30Floor3_level = "";
												if (b30Floor3_score >= 9900000) { //EX+评级(990w+)
													 b30Floor3_level = "EX+";
												} else if ((b30Floor3_score >= 9800000) & (b30Floor3_score < 9900000)) { //EX评级(980w-9899999)
													 b30Floor3_level = "EX";
												} else if ((b30Floor3_score >= 9500000) & (b30Floor3_score < 9800000)) { //AA评级(950w-9799999)
													 b30Floor3_level = "AA";
												} else if ((b30Floor3_score >= 9200000) & (b30Floor3_score < 9500000)) { //A评级(920w-9499999)
													 b30Floor3_level = "A";
												} else if ((b30Floor3_score >= 8900000) & (b30Floor3_score < 9200000)) { //B评级(890w-9199999)
													 b30Floor3_level = "B";
												} else if ((b30Floor3_score >= 8600000) & (b30Floor3_score < 8900000)) { //C评级(860w-8899999)
													 b30Floor3_level = "C";
												} else if (b30Floor3_score < 8600000) { //D评级(8599999-)
													 b30Floor3_level = "D";
												}
												String b30Floor3_clearType = ""; 
												switch (b30_list.getJSONObject(b30_floorStartCount + 2).getIntValue("clear_type"))
												{
												case 0: //Track Lost
													 b30Floor3_clearType = "L";
													 break;
												case 4: //Easy Clear
													 b30Floor3_clearType = "EC";
													 break;
												case 1: //Normal Clear
													 b30Floor3_clearType = "NC";
													 break;
												case 5: //Hard Clear
													 b30Floor3_clearType = "HC";
													 break;
												case 2: //Full Recall
													 b30Floor3_clearType = "F";
													 break;
												case 3: //Pure Memory
													 b30Floor3_clearType = "P";
													 break;
												default:
													 break;
												}
												int b30Floor3_far = b30_list.getJSONObject(b30_floorStartCount + 2).getIntValue("near_count");
												int b30Floor3_lost = b30_list.getJSONObject(b30_floorStartCount + 2).getIntValue("miss_count");
												// [end]
												// [start] B30Floor4
												BigDecimal b30Floor4_rating_bigdec = new BigDecimal( b30_list.getJSONObject(b30_floorStartCount + 3).getString("rating")); 
												double b30Floor4_rating = b30Floor4_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); 
												String b30Floor4_songid = b30_list.getJSONObject(b30_floorStartCount + 3).getString("song_id");
												String b30Floor4_difficulty = "";
												switch (b30_list.getJSONObject(b30_floorStartCount + 3).getIntValue("difficulty"))
												{
												case 0: //Past
													 b30Floor4_difficulty = "[PST]";
													 break;
												case 1: //Present
													 b30Floor4_difficulty = "[PRS]";
													 break;
												case 2: //Future
													 b30Floor4_difficulty = "[FTR]";
													 break;
												case 3: //Beyond
													 b30Floor4_difficulty = "[BYD]";
													 break;
												}
												long b30Floor4_score = b30_list.getJSONObject(b30_floorStartCount + 3).getLongValue("score");
												String b30Floor4_level = "";
												if (b30Floor4_score >= 9900000) { //EX+评级(990w+)
													 b30Floor4_level = "EX+";
												} else if ((b30Floor4_score >= 9800000) & (b30Floor4_score < 9900000)) { //EX评级(980w-9899999)
													 b30Floor4_level = "EX";
												} else if ((b30Floor4_score >= 9500000) & (b30Floor4_score < 9800000)) { //AA评级(950w-9799999)
													 b30Floor4_level = "AA";
												} else if ((b30Floor4_score >= 9200000) & (b30Floor4_score < 9500000)) { //A评级(920w-9499999)
													 b30Floor4_level = "A";
												} else if ((b30Floor4_score >= 8900000) & (b30Floor4_score < 9200000)) { //B评级(890w-9199999)
													 b30Floor4_level = "B";
												} else if ((b30Floor4_score >= 8600000) & (b30Floor4_score < 8900000)) { //C评级(860w-8899999)
													 b30Floor4_level = "C";
												} else if (b30Floor4_score < 8600000) { //D评级(8599999-)
													 b30Floor4_level = "D";
												}
												String b30Floor4_clearType = ""; 
												switch (b30_list.getJSONObject(b30_floorStartCount + 3).getIntValue("clear_type"))
												{
												case 0: //Track Lost
													 b30Floor4_clearType = "L";
													 break;
												case 4: //Easy Clear
													 b30Floor4_clearType = "EC";
													 break;
												case 1: //Normal Clear
													 b30Floor4_clearType = "NC";
													 break;
												case 5: //Hard Clear
													 b30Floor4_clearType = "HC";
													 break;
												case 2: //Full Recall
													 b30Floor4_clearType = "F";
													 break;
												case 3: //Pure Memory
													 b30Floor4_clearType = "P";
													 break;
												default:
													 break;
												}
												int b30Floor4_far = b30_list.getJSONObject(b30_floorStartCount + 3).getIntValue("near_count");
												int b30Floor4_lost = b30_list.getJSONObject(b30_floorStartCount + 3).getIntValue("miss_count");
												// [end]
												// [start] B30Floor5
												BigDecimal b30Floor5_rating_bigdec = new BigDecimal( b30_list.getJSONObject(b30_lastcount).getString("rating")); 
												double b30Floor5_rating = b30Floor5_rating_bigdec.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue(); 
												String b30Floor5_songid = b30_list.getJSONObject(b30_lastcount).getString("song_id");
												String b30Floor5_difficulty = "";
												switch (b30_list.getJSONObject(b30_lastcount).getIntValue("difficulty"))
												{
												case 0: //Past
													 b30Floor5_difficulty = "[PST]";
													 break;
												case 1: //Present
													 b30Floor5_difficulty = "[PRS]";
													 break;
												case 2: //Future
													 b30Floor5_difficulty = "[FTR]";
													 break;
												case 3: //Beyond
													 b30Floor5_difficulty = "[BYD]";
													 break;
												}
												long b30Floor5_score = b30_list.getJSONObject(b30_lastcount).getLongValue("score");
												String b30Floor5_level = "";
												if (b30Floor5_score >= 9900000) { //EX+评级(990w+)
													 b30Floor5_level = "EX+";
												} else if ((b30Floor5_score >= 9800000) & (b30Floor5_score < 9900000)) { //EX评级(980w-9899999)
													 b30Floor5_level = "EX";
												} else if ((b30Floor5_score >= 9500000) & (b30Floor5_score < 9800000)) { //AA评级(950w-9799999)
													 b30Floor5_level = "AA";
												} else if ((b30Floor5_score >= 9200000) & (b30Floor5_score < 9500000)) { //A评级(920w-9499999)
													 b30Floor5_level = "A";
												} else if ((b30Floor5_score >= 8900000) & (b30Floor5_score < 9200000)) { //B评级(890w-9199999)
													 b30Floor5_level = "B";
												} else if ((b30Floor5_score >= 8600000) & (b30Floor5_score < 8900000)) { //C评级(860w-8899999)
													 b30Floor5_level = "C";
												} else if (b30Floor5_score < 8600000) { //D评级(8599999-)
													 b30Floor5_level = "D";
												}
												String b30Floor5_clearType = ""; 
												switch (b30_list.getJSONObject(b30_lastcount).getIntValue("clear_type"))
												{
												case 0: //Track Lost
													 b30Floor5_clearType = "L";
													 break;
												case 4: //Easy Clear
													 b30Floor5_clearType = "EC";
													 break;
												case 1: //Normal Clear
													 b30Floor5_clearType = "NC";
													 break;
												case 5: //Hard Clear
													 b30Floor5_clearType = "HC";
													 break;
												case 2: //Full Recall
													 b30Floor5_clearType = "F";
													 break;
												case 3: //Pure Memory
													 b30Floor5_clearType = "P";
													 break;
												default:
													 break;
												}
												int b30Floor5_far = b30_list.getJSONObject(b30_lastcount).getIntValue("near_count");
												int b30Floor5_lost = b30_list.getJSONObject(b30_lastcount).getIntValue("miss_count");
												// [end]
												String b30Floor1_songname = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + b30Floor1_songid))
														  .getJSONObject("content").getJSONObject("title_localized").getString("en");
												String b30Floor2_songname = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + b30Floor2_songid))
														  .getJSONObject("content").getJSONObject("title_localized").getString("en");
												String b30Floor3_songname = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + b30Floor3_songid))
														  .getJSONObject("content").getJSONObject("title_localized").getString("en");
												String b30Floor4_songname = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + b30Floor4_songid))
														  .getJSONObject("content").getJSONObject("title_localized").getString("en");
												String b30Floor5_songname = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/songinfo?songname=" + b30Floor5_songid))
														  .getJSONObject("content").getJSONObject("title_localized").getString("en");
												if (b30Floor1_songname.length() > 10) {
													 b30Floor1_songname = CommonHelper.subStringByByte(b30Floor1_songname, 9) + "...";
												}
												if (b30Floor2_songname.length() > 10) {
													 b30Floor2_songname = CommonHelper.subStringByByte(b30Floor2_songname, 9) + "...";
												}
												if (b30Floor3_songname.length() > 10) {
													 b30Floor3_songname = CommonHelper.subStringByByte(b30Floor3_songname, 9) + "...";
												}
												if (b30Floor4_songname.length() > 10) {
													 b30Floor4_songname = CommonHelper.subStringByByte(b30Floor4_songname, 9) + "...";
												}
												if (b30Floor5_songname.length() > 10) {
													 b30Floor5_songname = CommonHelper.subStringByByte(b30Floor5_songname, 9) + "...";
												}
												StringBuilder b30floorStr = new StringBuilder(FriendlyName).append("\n")
														  .append("Arcaea - Best30地板查询\n")
														  .append("Best30 平均值:").append(b30_avg).append("\n")
														  .append("Recent10 平均值:").append(r10_avg).append("\n")
														  .append("Best30 地板(倒五成绩):\n")
														  .append(b30Floor1_songname).append(" ").append(b30Floor1_difficulty).append(" ").append(b30Floor1_level).append("/").append(b30Floor1_clearType).append("\n")
														  .append(b30Floor1_score).append(" ").append(b30Floor1_rating).append(" ").append("F").append(b30Floor1_far).append(" L").append(b30Floor1_lost).append("\n")
														  .append(b30Floor2_songname).append(" ").append(b30Floor2_difficulty).append(" ").append(b30Floor2_level).append("/").append(b30Floor2_clearType).append("\n")
														  .append(b30Floor2_score).append(" ").append(b30Floor2_rating).append(" ").append("F").append(b30Floor2_far).append(" L").append(b30Floor2_lost).append("\n")
														  .append(b30Floor3_songname).append(" ").append(b30Floor3_difficulty).append(" ").append(b30Floor3_level).append("/").append(b30Floor3_clearType).append("\n")
														  .append(b30Floor3_score).append(" ").append(b30Floor3_rating).append(" ").append("F").append(b30Floor3_far).append(" L").append(b30Floor3_lost).append("\n")
														  .append(b30Floor4_songname).append(" ").append(b30Floor4_difficulty).append(" ").append(b30Floor4_level).append("/").append(b30Floor4_clearType).append("\n")
														  .append(b30Floor4_score).append(" ").append(b30Floor4_rating).append(" ").append("F").append(b30Floor4_far).append(" L").append(b30Floor4_lost).append("\n")
														  .append(b30Floor5_songname).append(" ").append(b30Floor5_difficulty).append(" ").append(b30Floor5_level).append("/").append(b30Floor5_clearType).append("\n")
														  .append(b30Floor5_score).append(" ").append(b30Floor5_rating).append(" ").append("F").append(b30Floor5_far).append(" L").append(b30Floor5_lost).append("\n");
												CQ.sendGroupMsg(groupId, b30floorStr.toString());
										  }
										  break;
									 case -1:
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "绑定的账号无效,请使用!arc bind [玩家id]重新绑定");
										  break;
									 case -6:
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "您还没有游玩过任何一个曲目呢!");
									 default:
										  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "查询失败，未知错误(" + status + ")");
											 break;
									 }
								} else {
									 CQ.sendGroupMsg(groupId,
												 FriendlyName + "\n" + new CQCode().at(qqId) + "您还未绑定您的Arcaea帐号,请使用!arc bind [玩家9位数字id]进行绑定");
								}
						  } else {
								CQ.logError(AppName,
										  "尝试执行Func6_2_3时出错: 'BotArcApi' 未运行" + "\n" + "缺少必需的依赖项: Node.js (路径"
													 + CQ.getAppDirectory() + "system\\dependency\\nodejs)" + "\n"
													 + "请使用123SduBotR Installer安装依赖项后重启Bot程序.");
								CQ.sendGroupMsg(groupId,
											 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:Require dependency: Node.js)");
						  }
					 } catch (SocketTimeoutException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_3时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:BotArcApi isn't running)");
					 } catch (IOException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_3时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(Internal:BotArcApi isn't running)");
					 } catch (IndexOutOfBoundsException e) {
						  CQ.sendGroupMsg(groupId, FriendlyName + "\n" + new CQCode().at(qqId) + "指令格式不正确,格式:!arc info {曲名}"); 
					 } catch (Exception e) {
						  CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
						  CQ.sendGroupMsg(groupId,
									 FriendlyName + "\n" + new CQCode().at(qqId) + " 查询失败(" + e.getClass().getName() + ")");
					 }
			 }
			 
			 public static void Func6_2_4(CoolQ CQ,long groupId,long qqId,String msg)
			 {
				  try {
						if ((Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg) == 2) || (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg) == 3)) {
							 String arg3 = msg.trim().split(" ",3)[2];
							 Part6_2EasterEgg.Func6_2_UnlockConnect(CQ, groupId, null, 0, 0, 0, arg3);
							 return;
						} else {
						  if (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg) == 1) {
								 JSONObject connResultObject = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/connect"));
								 String connStr = connResultObject.getJSONObject("content").getString("key").toLowerCase();
								 String returnStr = FriendlyName + "\n今日连接密码为[" + connStr + "]";
								 CQ.sendGroupMsg(groupId, returnStr);
						  } else {
								 Part6_2EasterEgg.Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, null);
						  }
					 }
				 } catch (IndexOutOfBoundsException e) {
					  Part6_2EasterEgg.Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, null);
				 } catch (Exception e) {
						CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
						CQ.sendGroupMsg(groupId,
									 FriendlyName + "\n出现异常(" + e.getClass().getName() + ")");
				 }
			 }
			 
			 /**
			  * 功能6-2-Help:查看Arcaea查分模块(功能6-2)文字帮助
			  * @param CQ CQ实例，详见本大类注释
			  * @param groupId 消息来源群号
			  * @param qqId 消息来源成员QQ号
			  * @param msg 消息内容
			  * @see ProcessGroupMsg
			  * @author 御坂12456
			  */
			 public static void Func6_2_Help(CoolQ CQ,long groupId,long qqId,String msg)
			 {
				  try {
					 StringBuilder helpStrBuilder = new StringBuilder(FriendlyName).append("\n")
								.append("功能6-2:Arcaea查分模块帮助\n")
								.append("以下指令中[]代表必填指令,{}代表选填指令\n")
								.append("绑定账号: !arc bind [9位玩家id]\n")
								.append("查最近成绩: !arc 或 !arc info\n")
								.append("查最佳成绩: !arc info [曲名] {难度,默认Ftr}\n")
								.append("查Best30地板: !arc b30");
					 switch (Part6_2EasterEgg.Func6_2_CheckConnectStat(CQ, groupId, qqId, msg))
					 {
					 case 1: //Unlocked Connect Module
						  helpStrBuilder.append("\n获取今日连接字符串: !arc connect");
						  break;
					 case 2: case 3: //Completed Prerequisites for unlocking connect module
						  helpStrBuilder.append("\nᵾnłøȼꝁ sŧħ: !aɍȼ ȼønnɇȼŧ [Sŧɍɨnǥ]");
					 default: //Locked
						  break;
					 }
					 CQ.sendGroupMsg(groupId, helpStrBuilder.toString());
				} catch (Exception e) {
					  CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
					  CQ.sendGroupMsg(groupId,
								 FriendlyName + "\n获取失败(" + e.getClass().getName() + ")");
				}
			 }

			 
			 static class Part6_2EasterEgg{
				  /**
				   * 解锁Connect模块
				   * @param CQ CQ实例，详见本大类注释
				   * @param groupId 消息来源群号
				   * @param songName 完成曲目名
				   * @param songdiff 完成曲目难度
				   * @param clearType 完成状态(0-TL/4-EC/1-NC/5-HC/2-FR/3-PM)
				   * @param bigPure 大Pure数
				   * @param connPass 连接密码
				   * @author 御坂12456
				   */
				  public static void Func6_2_UnlockConnect(CoolQ CQ,long groupId,String songName,int songdiff,int clearType,int bigPure,String connPass)
				  {
						try {
							 File thisGroupUnlockData = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId);
							 if (thisGroupUnlockData.exists()) {
								File thisGroupUnlocked = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/unlocked.stat");
								if (thisGroupUnlocked.exists()) {
									 return;
								} else {
									 if (songName == null) {
										  songName = "notnull";
									 }
									  switch (songName)
									  {
									  case "equilibrium": //前置条件1:以HC及以上状态完成Equilibrium[FTR]
									   	File preCondition3 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/pre3.stat");
											switch (clearType)
										   {
										   case 5: case 2: case 3:
										   	 File Condition1 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat");
										   	 if ((!Condition1.exists()) & (songdiff == 9)) {
													 Condition1.createNewFile();
													 CQ.sendGroupMsg(groupId, "Ŧħɇ ǥɨɍł ɨn wħɨŧɇ Ⱥnđ ŧħɇ ǥɨɍł ɨn ƀłȺȼꝁ ȼȺnnøŧ ɍɇȼønȼɨłɇ.");
													 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, null);
												 }
										   	 IOHelper.WriteStr(preCondition3, "1");
										   	 break;
										   case 1: case 4:
										   	 IOHelper.WriteStr(preCondition3, "1");
										   	 break;
										   case 0:
										   	 preCondition3.delete();
										   }
											break;
									  case "antagonism": //前置条件2:以700及以上大Pure数完成Antagonism[FTR]
											if (bigPure >= 700) {
												File Condition2  = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat");
										   	if (!Condition2.exists()  & (songdiff == 9)) {
													 Condition2.createNewFile();
													 CQ.sendGroupMsg(groupId, "Sħɇ ønȼɇ møɍɇ fȺȼɇs ŧħɇ ǥɨɍł sħɇ wɨsħɇs sħɇ ȼøᵾłđ ƀɇfɍɨɇnđ.");
													 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
												}
										  }
									     File preCondition3_2 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/pre3.stat");
										  if (clearType != 0) {
										   	 if (IOHelper.ReadToEnd(preCondition3_2).equals("1")) {
										   		  IOHelper.WriteStr(preCondition3_2, "2");
												 } else {
													  preCondition3_2.delete();
												 }
										  } else {
												  preCondition3_2.delete();
										  }
										  break;
									  case "dantalion": //前置条件3:依次按照Equilibrium[FTR]->Antagonism[FTR]->Dantalion[FTR]顺序通关
										   File preCondition3_3 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/pre3.stat");
											if (clearType != 0) {
										   	 if ((IOHelper.ReadToEnd(preCondition3_3).equals("2"))  & (songdiff == 10)) {
										   		  preCondition3_3.delete();
										   		  new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat").createNewFile();
										   		  CQ.sendGroupMsg(groupId, "Ⱥnđ ŧħᵾs ɨŧ ɨs ŦȺɨɍɨŧsᵾ's ŧᵾɍn ŧø ǥȺɨn ŧħɇ ᵾᵽᵽɇɍ ħȺnđ.");
														 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
												 } else {
													  preCondition3_3.delete();
												 }
										   } else {
												preCondition3_3.delete();
										  }
											break;
									  case "tempestissimo": //解锁条件:满足所有前置条件后游玩Tempestissimo(任意难度)
											 File Condition_1 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat");
											 File Condition_2 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat");
											 File Condition_3 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat");
											 File Condition_4 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat");
											if ((Condition_1.exists()) & (Condition_2.exists()) & (Condition_3.exists())
													  & (Condition_4.exists())) {
													new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/5.stat").createNewFile();
										  }
											break;
									  default:
											if (connPass != null) { //前置条件4:输入正确的连接密码
												 JSONObject connResultObject = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1:61666/v2/connect"));
												 String connStr = connResultObject.getJSONObject("content").getString("key").toLowerCase();
												 if (connStr.equals(connPass.toLowerCase())) {
													 new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat").createNewFile();
													 CQ.sendGroupMsg(groupId, "Sħɇ sᵽȺɍɇs nø ħɇsɨŧȺŧɨøn. Ŧħɇ sŧɍɨꝁɇ ȼømɇs ɨn Ⱥn ɨnsŧȺnŧ.");
													 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
												 } else {
													 CQ.sendGroupMsg(groupId, "Wrong Password");
												}
											}
											break;
									  }
									  if ((new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat").exists()) &
										   (new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat").exists()) &
										   (new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat").exists()) &
										   (new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat").exists()) &
										   (new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/5.stat").exists()))
									  {
											new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat").delete();
											new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat").delete();
											new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat").delete();
											new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat").delete();
											new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/5.stat").delete();
											thisGroupUnlocked.createNewFile();
											CQ.sendGroupMsg(groupId, "Anomaly Song Unlocked\nAnomaly Function Unlocked");
									 }
								}
							 } else {
								  thisGroupUnlockData.mkdirs();
								  switch (songName)
								  {
								  case "equilibrium": //前置条件1:以HC及以上状态完成Equilibrium[FTR]
								   	File preCondition3 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/pre3.stat");
										switch (clearType)
									   {
									   case 5: case 2: case 3:
									   	 File Condition1 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat");
									   	 if (!Condition1.exists()  & (songdiff == 9)) {
												 Condition1.createNewFile();
												 CQ.sendGroupMsg(groupId, "Ŧħɇ ǥɨɍł ɨn wħɨŧɇ Ⱥnđ ŧħɇ ǥɨɍł ɨn ƀłȺȼꝁ ȼȺnnøŧ ɍɇȼønȼɨłɇ.");
												 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
											 }
									   case 1: case 4:
									   	 IOHelper.WriteStr(preCondition3, "1");
									   	 break;
									   case 0:
									   	 preCondition3.delete();
									   }
										break;
								  case "antagonism": //前置条件2:以700及以上大Pure数完成Antagonism[FTR]
										if (bigPure >= 700) {
											File Condition2  = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat");
									   	if (!Condition2.exists()  & (songdiff == 9)) {
												 Condition2.createNewFile();
												 CQ.sendGroupMsg(groupId, "Sħɇ ønȼɇ møɍɇ fȺȼɇs ŧħɇ ǥɨɍł sħɇ wɨsħɇs sħɇ ȼøᵾłđ ƀɇfɍɨɇnđ.");
												 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
											}
									   	break;
									  }
								     File preCondition3_2 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/pre3.stat");
									  if (clearType != 0) {
									   	 if (IOHelper.ReadToEnd(preCondition3_2).equals("1")) {
									   		  IOHelper.WriteStr(preCondition3_2, "2");
											 } else {
												  preCondition3_2.delete();
											 }
									  } else {
											  preCondition3_2.delete();
									  }
									  break;
								  default:
										if (connPass != null) { //前置条件4:输入正确的连接密码
											 JSONObject connResultObject = JSONObject.parseObject(JsonHelper.loadJson("http://127.0.0.1/v2/connect"));
											 String connStr = connResultObject.getJSONObject("content").getString("key").toLowerCase();
											 if (connStr.equals(connPass.toLowerCase())) {
												 new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat").createNewFile();
												 CQ.sendGroupMsg(groupId, "Sħɇ sᵽȺɍɇs nø ħɇsɨŧȺŧɨøn. Ŧħɇ sŧɍɨꝁɇ ȼømɇs ɨn Ⱥn ɨnsŧȺnŧ.");
												 Func6_2_ShowUnlockStat(CQ, groupId, 0L, null, "check");
											 } else {
													 CQ.sendGroupMsg(groupId, "Wrong Password");
												}
										}
										break;
								  }
							 }
					 } catch (SocketTimeoutException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_UnlockConnect时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n发生异常(Internal:BotArcApi isn't running)");
					 } catch (IOException e) {
							CQ.logError(AppName,
									  "尝试执行Func6_2_UnlockConnect时出错: 'BotArcApi' 未运行" + "\n" + 
									  "请在" + CQ.getAppDirectory() + "system\\dependency\\nodejs\\node_modules\\BotArcApi下执行npm start启动BotArcApi服务.");
							CQ.sendGroupMsg(groupId,
										 FriendlyName + "\n发生异常(Internal:BotArcApi isn't running)");
					 } catch (Exception e) {
						  CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
						  CQ.sendGroupMsg(groupId,
									 FriendlyName + "\n发生异常(" + e.getClass().getName() + ")");
			 }
				  }

				  
				  /**
				   * 显示Connect模块解锁状态
				   * @param CQ CQ实例，详见本大类注释
				   * @param groupId 消息来源群号
				   * @param qqId 消息来源成员QQ号
				   * @param msg 消息内容
				   * @see ProcessGroupMsg
				   * @author 御坂12456
				   */
				  public static void Func6_2_ShowUnlockStat(CoolQ CQ,long groupId,long qqId,String msg,String tag)
				  {
						try {
							 File thisGroupUnlockData = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId);
							 if (!thisGroupUnlockData.exists()) { //如果完全未解锁
								String resultStr = "[Locked]\n[Locked]\n[Main Locked]\n[Locked]\n[Locked]\n";
								CQ.sendGroupMsg(groupId, resultStr);
							
						  } else { //如果至少解锁了一个条件
								StringBuilder resultBuilder = new StringBuilder();
								String resultStr = null;
								 File Condition1 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat");
								 File Condition2 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat");
								 File Condition3 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat");
								 File Condition4 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat");
								 File Condition5 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/5.stat");
								
									 if (Condition1.exists()) {
										  resultBuilder.append("[Equilibrium]使用包含“困难”技能的角色通关");
									 } else {
										  resultBuilder.append("[Locked]");
									 }
									 if (Condition2.exists()) {
										  resultBuilder.append("\n[Antagonism]通关时大Pure数不低于700");
									 } else {
										  resultBuilder.append("\n[Locked]");
									 }
									 resultBuilder.append("\n[Main Locked]");
									 if (Condition3.exists()) {
										  resultBuilder.append("\n[Dantalion]依照如下顺序通关:Equilibrium, Antagonism, Dantalion");
									 } else {
										  resultBuilder.append("\n[Locked]");
									 }
									 if (Condition4.exists()) {
										  resultBuilder.append("\n[#1f1e33]???");
									 } else {
										  resultBuilder.append("\n[Locked]");
									 }
									 if ((Condition1.exists()) & (Condition2.exists()) & (Condition3.exists())
												& (Condition4.exists())){
										  if ((tag != null) & (!Condition5.exists())) {
												Thread.sleep(5000);
												CQ.sendGroupMsg(groupId, resultBuilder.toString());
										  }
										 resultStr = new CQCode().image(new File(CQ.getAppDirectory() + "/data/pics/arcconnunlock.jpg"));
									 } else {
										  resultStr = resultBuilder.toString();
									 }
									 Thread.sleep(5000);
									 CQ.sendGroupMsg(groupId, resultStr);
						  }
					 } catch (Exception e) {
						  // TODO: handle exception
					 }
				  }
				  
					 /**
					  * 检查Connect模块解锁状态
					  * 
					  * @param CQ
					  * @param groupId
					  * @param qqId
					  * @param msg
					  * @return 该群已解锁返回1,已完成前置条件返回2,已完成除前置条件4外前置条件返回3,未解锁返回0
					  */
					 public static int Func6_2_CheckConnectStat(CoolQ CQ, long groupId, long qqId, String msg)
					 {
						  try {
								File thisGroupUnlocked = new File(
										  CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/unlocked.stat");
								if (thisGroupUnlocked.exists()) {
									 return 1;
								} else {
									 File Condition1 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/1.stat");
									 File Condition2 = new File(CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/2.stat");
									 File Condition3 = new File(
												CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/3.stat");
									 File Condition4 = new File(
												CQ.getAppDirectory() + "/group/easteregg/arconnect/" + groupId + "/4.stat");
									 if ((Condition1.exists()) & (Condition2.exists()) & (Condition3.exists())
												& (Condition4.exists())) {
										  return 2;
									 } else if ((Condition1.exists()) & (Condition2.exists()) & (Condition3.exists()) & (!Condition4.exists())) {
										  return 3;
									 }else {
										  return 0;
									 }
								}
						  } catch (Exception e) {
								return 0;
						  }
					 }
				}
		  }

		  /**
		   * 检查依赖项(Node.js)是否安装
		   * 
		   * @return 已安装返回true，未安装返回false
		   */
		 public static boolean checkDependency(CoolQ CQ)
		 {
			  try {
					File dpdcJar = new File(CQ.getAppDirectory() + "/system/dependency/nodejs");
					if (dpdcJar.exists()) {
						 return true;
					} else {
					 return false;
				}
			  } catch (Exception e) {
					e.printStackTrace();
					return false;
			  }
		 }
	}

	/**
	 * 其它功能（注意与"特殊模块"区分开）
	 * @author 御坂12456
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
			try {
				String result = PicsGenerateUtility.getAboutPic();
				if ((result == null) || (result.equals(""))) {
					CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
					return;
				}  else { //其它（返回的是文件路径）
					CQImage image = new CQImage(new File(result)); // 获取图片CQ码并发送
					CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
					new File(result).delete();
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
			}
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
			try {
				if (msg.split(" ",2).length == 2) {
					String funcNo = msg.split(" ",2)[1];
					String result = ""; //定义返回的图片路径变量
					if (!funcNo.equals("")) { //如果主功能序号参数存在
						result = PicsGenerateUtility.getMenuPic(funcNo);
						if ((result == null) || (result.equals(""))) {
							CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
							return;
						} else { //其它（返回的是文件路径）
							CQImage image = new CQImage(new File(result)); // 获取图片CQ码并发送
							CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
							new File(result).delete();
							return;
						}
					} else { //如果主功能序号参数不存在
						String resultPath = PicsGenerateUtility.getMainMenuPic();
						CQImage image = new CQImage(new File(resultPath)); // 获取图片CQ码并发送
						CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
						new File(resultPath).delete();
						return;
					}
				} else {
					String resultPath = PicsGenerateUtility.getMainMenuPic();
					CQImage image = new CQImage(new File(resultPath)); // 获取图片CQ码并发送
					CQ.sendGroupMsg(groupId, new CQCode().image(image)); //发送图片消息
					new File(resultPath).delete();
					return;
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n获取失败");
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
						StringBuilder otpMessage = new StringBuilder(FriendlyName).append("\n").append(new CQCode().at(qqId)).append("[解除防滥用]输入!uab [验证码]").append("\n")
								.append(new CQCode().image(tmpFile));
						CQ.sendGroupMsg(groupId, otpMessage.toString());
						tmpFile.delete(); //删除验证码图片文件
					}
				} else { // 否则（输入了验证码）
					// 定义已滥用标志文件
					File flagFile = new File(
							Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
					if (!flagFile.exists()) { // 如果标志文件不存在
						CQ.sendGroupMsg(groupId, Global.FriendlyName + "\n" + new CQCode().at(qqId) + "未处于防滥用状态");
					} else { // 如果标志文件存在
						// 定义输入的验证码(转小写—不区分大小写)
						String inputOtp = arguments[1].trim().toLowerCase();
						// 定义验证码文件(.unlocking)
						File otpFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".unlocking");
						// 定义执行中标志文件(.using)
						File usingFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".using");
						// 定义已滥用标志文件(.abused)
						File abusedFile = new File(Global.appDirectory + "/protect/group/abuse/" + groupId + "/" + qqId + ".abused");
						// 定义读取到的文件中的验证码(转小写—不区分大小写)
						String realOtp = IOHelper.ReadToEnd(otpFile).toLowerCase();
						if (inputOtp.toLowerCase().equals(realOtp)) { //如果验证码输入正确
							if (otpFile.exists()) otpFile.delete(); //删除验证码文件(.unlocking)
							if (usingFile.exists()) usingFile.delete(); //删除执行中标志文件(.using)
							if (abusedFile.exists()) abusedFile.delete(); //删除已滥用标志文件(.abused)
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
									new CQCode().at(qqId) + 
									"验证成功,防滥用已解除");
						} else { //如果验证码输入错误
							otpFile.delete(); //删除验证码文件(.unlocking)
							CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
									new CQCode().at(qqId) + "验证码输入错误,请重新输入!uab获取验证码");
						}
					}
				}
			} catch (Exception e) {
				CQ.sendGroupMsg(groupId, FriendlyName + "\n" +
						new CQCode().at(qqId) + "防滥用解除失败(" + e.getClass().toString() + ")");
				CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
						"详细信息:\n" +
						ExceptionHelper.getStackTrace(e));
			}
		}
		/**
		 * 功能O-4:反馈
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
		 * @param qqId 消息来源成员QQ号
		 * @param msg 消息内容
		 * @author 御坂12456
		 */
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
		 * 功能S-1:滑稽（斜眼笑）彩蛋
		 * @param CQ CQ实例，详见本大类注释
		 * @param groupId 消息来源群号
	 	 * @param qqId 消息来源QQ号
	    * @param msg 消息内容
		 */
		public static void Funny_EasterEgg(CoolQ CQ,long groupId,long qqId,String msg)
		{
			 try {
				 	ResultSet funnyWhiteListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * From funnyWL");
					//若滑稽彩蛋白名单数据表存在
					if (funnyWhiteListSet.next())
					{
						 ArrayList<Long> funnyWhiteList = new ArrayList<Long>();
						 while (funnyWhiteListSet.next()) { //遍历funnyWhilteListSet
							  funnyWhiteList.add(funnyWhiteListSet.getLong("GroupId")); //添加到funnyWhiteList中
						 }
						//若白名单列表不为空
						if (funnyWhiteList.size() > 0) {
							for (Long funnyWhiteGroup : funnyWhiteList) {
								if (funnyWhiteGroup.longValue() == groupId) //若此群为白名单群
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
			 } catch (Exception e) {
				 // TODO: handle exception
			 }
				System.gc(); //执行垃圾收集器
				return;
			}
		
		/**
		 * 返回打乱的应用友好名称字符串<br>
		 * 如:<br>
		 * <i>uS3o】. 1t【0Bd.25R 0</i><br>
		 * 对应的正常字符串是:<i>【123 SduBotR 0.5.0】</i>
		 * @since 0.5.0
		 * @return 成功返回打乱的字符串，失败返回空字符串（注意不是null）
		 */
		public static String randFriendlyName()
		{
			try {
				ArrayList<Character> friendlyNameCharsList = new ArrayList<Character>();
				for (char tempChar : FriendlyName.toCharArray()) {
					friendlyNameCharsList.add(Character.valueOf(tempChar));
				}
				Collections.shuffle(friendlyNameCharsList, new Random());
				StringBuilder randFriendlyNameBuilder = new StringBuilder();
				for (Character character : friendlyNameCharsList) {
					randFriendlyNameBuilder.append(Character.toString(character));
				}
				return randFriendlyNameBuilder.toString();
			} catch (Exception e) {
				return "";
			}
		}
	}
}