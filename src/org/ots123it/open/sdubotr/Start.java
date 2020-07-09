package org.ots123it.open.sdubotr;

import org.meowy.cqp.jcq.entity.*;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import org.meowy.cqp.jcq.message.CQCode;
import org.ots123it.jhlper.CommonHelper;
import org.ots123it.jhlper.DBHelper;
import org.ots123it.jhlper.ExceptionHelper;
import org.ots123it.open.sdubotr.Global;

import static org.ots123it.open.sdubotr.Global.*;
import static org.ots123it.open.sdubotr.Global.GlobalDatabases.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Timer;

/**
 * 123 SduBotR的主处理类
 * <br>
 * 保留JCQ Demo的基本注释，方便开发
 * @author <a href="mailto:a15951@qq.com">御坂12456</a>
 * @since Alpha 0.0.1
 */
public class Start extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
	/**
	 * 123 SduBotR 数据存放路径(不建议在此调用,请在Global调用)
	 */
	public static String appDirectory;
	public static Timer timer = new Timer(false);
	
	/**
	 * 是否处于调试模式标志
	 */
	public static boolean isDebug = false;
    /**
     * 使用新的方式加载CQ （建议使用这种方式）
     *
     * @param CQ CQ初始化
     */
    public Start(CoolQ CQ) {
        super(CQ);
    }
    /**
     * 兼容性保留
     */
    @SuppressWarnings("deprecation")
	public Start()
	{
    	
	}

	/**
     * 用main方法调试可以最大化的加快开发效率，检测和定位错误位置<br/>
     *
     * @param args 系统参数
	 * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {

        // 要测试主类就先实例化一个主类对象
        Start test = new Start();
        // 获取当前酷Q操作对象
        CoolQ CQ = test.getCoolQ();
        // test.startup();// 程序运行开始 调用应用初始化方法
        // test.enable();// 程序初始化完成后，启用应用，让应用正常工作
        // [start] 123 SduBotR Debug Terminator Initialization
        Scanner in = new Scanner(System.in);
    	System.out.println("Welcome to use 123 SduBotR Debug Terminator.");
    	System.out.println("(C)Copyright 2009-2020 123IT Corporation. All rights reserved.");
    	System.out.println("Tip:If you wants to close the terminator, input 'stop' when you need to input the group number.");
    	isDebug = true;
      Initialization.main(CQ, CQ.getAppDirectory());
      Global.appDirectory = CQ.getAppDirectory();
    	Thread.sleep(2000);
    	System.out.println();
    	// [end]
        // [start] 123 SduBotR Debug Terminator Main Handling Function
    	while (true)
        {
        	in.nextLine();
        	System.out.println("Please input the group number you want to send the message:");
        	String groupString = in.nextLine();
        	if (CommonHelper.isInteger(groupString)) {
				long fromGroup = Long.parseLong(groupString);
				System.out.println("Please input the QQ number you want to send the message:");
				String qqString = in.nextLine();
				if (CommonHelper.isInteger(qqString)) {
					long fromQQ = Long.parseLong(qqString);
					System.out.println("Please input the message you want to send the message(use '#end' to stop inputing)");
					StringBuilder msgBuilder = new StringBuilder();
					while (true) {
						String inputString = in.nextLine();
						if (inputString.equals("#end")) {
							break;
						} else {
							msgBuilder.append(inputString);
						}
					}
					System.out.println("Press any key to test sending message...");
					in.next();
					test.groupMsg(0, 0, fromGroup, fromQQ, null, msgBuilder.toString(), 0);
					System.out.println();
					continue;
				}
			} else if (groupString.toLowerCase().trim().equals("stop")) {
				System.out.println("Terminator will close... press any key to continue.");
				in.next();
				break;
			} {
				System.out.println("You inputed invalid group number, please try again");
				in.next();
			}
        }
    	// [end]
        // [start] 123 SduBotR Debug Terminator Closing Function
    	System.out.println("Now closing...");
    	in.close();
        File runningStatFile = new File(Global.appDirectory + "/running.stat");
        runningStatFile.delete();
        // 以下是收尾触发函数
        // demo.disable();// 实际过程中程序结束不会触发disable，只有用户关闭了此插件才会触发
        // test.exit();// 最后程序运行结束，调用exit方法
        System.out.println("Terminator closed.");
        System.out.println("Thanks for your using.");
        System.exit(0);
        // [end]
    }

    /**
     * 打包后将不会调用 请不要在此事件中写其他代码
     *
     * @return 返回应用的ApiVer、Appid
     */
    public String appInfo() {
    	// 123 SduBotR的AppID（如需自行定制请务必更改AppID）
        String AppID = "org.ots123it.open.sdubotr";
        /**
         * 本函数【禁止】处理其他任何代码，以免发生异常情况。
         * 如需执行初始化代码请在 startup 事件中执行（Type=1001）。
         */
        return CQAPIVER + "," + AppID;
    }

    /**
     * 酷Q启动 (Type=1001)<br>
     * 本方法会在酷Q【主线程】中被调用。<br>
     * 请在这里执行插件初始化代码。<br>
     * 请务必尽快返回本子程序，否则会卡住其他插件以及主程序的加载。
     *
     * @return 请固定返回0
     */
    public int startup() {
        // 获取应用数据目录(无需储存数据时，请将此行注释)
        appDirectory = CQ.getAppDirectory().substring(0, CQ.getAppDirectory().length());
        // 返回如：D:\CoolQ\data\app\org.meowy.cqp.jcq\data\app\org.ots123it.open.sdubotr\
        // 设置时区（功能3-1）
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        CQ.logDebug(AppName, "时区设置完毕:GMT+8");
        return 0;
    }

    /**
     * 酷Q退出 (Type=1002)<br>
     * 本方法会在酷Q【主线程】中被调用。<br>
     * 无论本应用是否被启用，本函数都会在酷Q退出前执行一次，请在这里执行插件关闭代码。
     *
     * @return 请固定返回0，返回后酷Q将很快关闭，请不要再通过线程等方式执行其他代码。
     */
    public int exit() {
    			if (enable) {
    	    		try {
    				CQ.logInfo(AppName, "接收到关闭信号，正在执行关闭操作");
       			timer.purge(); timer.cancel(); //停止自动备份线程
       			CQ.logDebug(AppName, "自动备份线程已停止");
       			for (DBHelper dbHelper : dbArrayList) { //遍历循环数据库集合
   					 dbHelper.Close(); //关闭数据库
   					 CQ.logDebug(AppName, "数据库文件:" + dbHelper.toString() + "已关闭");
   				}
       			File runningStat = new File(Global.appDirectory + "/running.stat"); //新建“正在运行中”标志文件实例
       			runningStat.delete(); //删除“正在运行中”标志文件（正常关闭）
       			CQ.logDebug(AppName, "运行标志文件已删除（正常关闭）");
       			CQ.logInfo(AppName, "应用已正常关闭，等待主程序退出……");
    			} catch (Exception e) {
    				CQ.logFatal(AppName, "执行关闭操作时出现致命异常:\n" + 
    						e.getMessage() + "\n" + ExceptionHelper.getStackTrace(e));
    			}
    		}
			
        return 0;
    }

    /**
     * 应用已被启用 (Type=1003)<br>
     * 当应用被启用后，将收到此事件。<br>
     * 如果酷Q载入时应用已被启用，则在 {@link #startup startup}(Type=1001,酷Q启动) 被调用后，本函数也将被调用一次。<br>
     * 如非必要，不建议在这里加载窗口。
     *
     * @return 请固定返回0。
     */
    public int enable() {
        switch (Initialization.main(CQ,appDirectory))
        {
        case 0:
        	enable = true;
        	break;
        case 1:
        default:
        	enable = false;
        	break;
        }
        return 0;
    }

    /**
     * 应用将被停用 (Type=1004)<br>
     * 当应用被停用前，将收到此事件。<br>
     * 如果酷Q载入时应用已被停用，则本函数【不会】被调用。<br>
     * 无论本应用是否被启用，酷Q关闭前本函数都【不会】被调用。
     *
     * @return 请固定返回0。
     */
    public int disable() {
        enable = false;
        return 0;
    }

    /**
     * 私聊消息 (Type=21)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType 子类型，11/来自好友 1/来自在线状态 2/来自群 3/来自讨论组
     * @param msgId   消息ID
     * @param fromQQ  来源QQ
     * @param msg     消息内容
     * @param font    字体
     * @return 返回值*不能*直接返回文本 如果要回复消息，请调用api发送<br>
     * 这里 返回  {@link IMsg#MSG_INTERCEPT MSG_INTERCEPT} - 截断本条消息，不再继续处理<br>
     * 注意：应用优先级设置为"最高"(10000)时，不得使用本返回值<br>
     * 如果不回复消息，交由之后的应用/过滤器处理，这里 返回  {@link IMsg#MSG_IGNORE MSG_IGNORE} - 忽略本条消息
     */
    public int privateMsg(int subType, int msgId, long fromQQ, String msg, int font) {
       try {
      	  if (fromQQ == masterQQ) { //如果是主人私聊机器人
              	ProcessPrivateManageMsg.main(CQ, fromQQ, msg);
               } else { //如果不是主人私聊机器人
              	// [start] 读取机器人黑名单数据表(功能M-3)
      			ResultSet AllBanSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM AllBan");
      			ArrayList<Long> AllBanList = new ArrayList<Long>(); //使用腾讯系统QQ号数组初始化AllBanList(忽略掉系统QQ号的私聊消息)
      			if (AllBanSet.next()) { //如果黑名单数据表不为空
      				 AllBanSet.beforeFirst(); //移动指针到开头
      				 while (AllBanSet.next()) { //遍历AllBanSet
      					  AllBanList.add(AllBanSet.getLong("QQId")); //将当前遍历到的QQ号添加到AllBanList中
      				 }
      				 for (Long BanPerson : AllBanList) { //遍历AllBanList
      					  if (fromQQ == BanPerson.longValue()) //如果消息来源成员为机器人黑名单人员
      					  {
      							return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
      					  }
      				 }
      			}
      			// [end]
      			if (new QQInfo(fromQQ).getNick().equals(tencentSysAccount)) {
      				 return MSG_IGNORE;
      			}
      			StringBuilder callMasterStr = new StringBuilder(FriendlyName).append("\n") 
      					.append("有人私聊机器人,请处理\n") 
      					.append("来源QQ:").append(CQ.getStrangerInfo(fromQQ).getNick()).append("(").append(fromQQ).append(")\n")
      					.append("消息内容:\n")
      					.append(msg);
      			CQ.sendPrivateMsg(masterQQ,callMasterStr.toString()); //向机器人主人发送提醒消息
      		}
              return MSG_IGNORE;
       } catch (Exception e) {
      	  CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
      	  return MSG_IGNORE;
       }
    }

    /**
     * 群消息 (Type=2)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType       子类型，目前固定为1
     * @param msgId         消息ID
     * @param fromGroup     来源群号
     * @param fromQQ        来源QQ号
     * @param fromAnonymous 来源匿名者
     * @param msg           消息内容
     * @param font          字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */

	public int groupMsg(int subType, int msgId, long fromGroup, long fromQQ, String fromAnonymous, String msg,
                        int font) {
		try {
        // [start] 如果消息来自匿名者
        if (fromQQ == 80000000L && !fromAnonymous.isEmpty()) {
            // 将匿名用户信息放到 anonymous 变量中
            @SuppressWarnings("unused")
			Anonymous anonymous = CQ.getAnonymous(fromAnonymous);
        }
        // [end]
        if (fromQQ==Global.masterQQ&&msg.startsWith("#")) //若是管理命令前缀且为机器人主人发送的
        	{ //转到ProcessGroupManageMsg类处理
        		ProcessGroupManageMsg.main(CQ, fromGroup, fromQQ, msg);
        	} else
        	{
        		// [start] 读取机器人冲突屏蔽数据表(功能M-5)
					 ResultSet conflictWLSet = dbgroup_list.executeQuery("SELECT * FROM conflictWL");
					 ArrayList<Long> conflictWhiteList = new ArrayList<Long>();
					 while (conflictWLSet.next()) {
						  conflictWhiteList.add(conflictWLSet.getLong("OtherBotId"));
					 }
					 for (Long conflictBotQQStr : conflictWhiteList) {
						  if (fromQQ == conflictBotQQStr.longValue()) {
								return MSG_IGNORE;
						  }
					 }
					 // [end]
    	      // [start] 读取特别监视群聊数据表(功能2-1)
						ResultSet iMGSet = dbgroup_list.executeQuery("SELECT * FROM iMG");
						ArrayList<Integer> iMGList = new ArrayList<Integer>();
						while (iMGSet.next()) {
							 iMGList.add(iMGSet.getInt("GroupId"));
						}
						for (Integer imMonitGroup : iMGList) {
							 if (fromGroup == imMonitGroup.longValue()) //如果消息来源群为特别监视群
		    					{
		    						ProcessGroupMsg.Part2.Func2_1(CQ,msgId,fromGroup,fromQQ,msg); //转到功能2-1处理
		    						break;
		    					}
						}
    			// [end]
        		// [start] 机器人防侮辱检测(功能M-A3)
    			if ((msg.toLowerCase().contains("bot")) || (msg.contains("机器人"))) { //如果消息内容有"机器人"或"bot"
					for (String bannedObscenityToBot : bannedObscenities) { //遍历脏话列表
						if ((msg.toLowerCase().contains("http")) || (msg.toLowerCase().contains("ftp"))) break; //如果检测到网址直接跳出循环（忽略）
						if (msg.contains(bannedObscenityToBot)) { //如果消息内容有脏话之一
							CQ.sendPrivateMsg(masterQQ,FriendlyName + "\n" + 
									"主人,刚刚在群聊" + fromGroup + "里有人欺负我QAQ\n" + 
									"坏人QQ:" + CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ")\n" + 
									"Ta发的是" + msg);
							CQ.sendGroupMsg(fromGroup, ".");
							break; //跳出循环
						}
					}
				}
    			// [end]
    			// [start] 读取机器人黑名单数据表(功能M-3)
    			ResultSet AllBanSet = dbgroup_list.executeQuery("SELECT * FROM AllBan");
    			ArrayList<Integer> AllBanList = new ArrayList<Integer>();
    			while (AllBanSet.next()) {
					 AllBanList.add(AllBanSet.getInt("qqId"));
				}
    			for (Integer BanPerson : AllBanList) {
				if (fromQQ == BanPerson.longValue()) // 如果消息来源成员为机器人黑名单人员
					{
						return MSG_INTERCEPT; // 不多废话，直接返回（拜拜了您嘞）
					}
    			}
    			// [end]
    			// [start] 读取机器人群聊黑名单列表文件(功能M-4)
        		if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时(警告)或永久加黑
        		{
        			return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
        		}
        		// [end]
        		ProcessGroupMsg.main(CQ, fromGroup, fromQQ, msg); //转到ProcessGroupMsg类处理
        	}
		} catch (Exception e) {
			CQ.logError(Global.AppName, "发生异常,请及时处理\n" +
					"详细信息:\n" +
					ExceptionHelper.getStackTrace(e));
		}
        return MSG_IGNORE;
    }

    /**
     * 讨论组消息 (Type=4)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype     子类型，目前固定为1
     * @param msgId       消息ID
     * @param fromDiscuss 来源讨论组
     * @param fromQQ      来源QQ号
     * @param msg         消息内容
     * @param font        字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int discussMsg(int subtype, int msgId, long fromDiscuss, long fromQQ, String msg, int font) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 群文件上传事件 (Type=11)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType   子类型，目前固定为1
     * @param sendTime  发送时间(时间戳)// 10位时间戳
     * @param fromGroup 来源群号
     * @param fromQQ    来源QQ号
     * @param file      上传文件信息
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupUpload(int subType, int sendTime, long fromGroup, long fromQQ, String file) {
        GroupFile groupFile = CQ.getGroupFile(file);
        if (groupFile == null) { // 解析群文件信息，如果失败直接忽略该消息
            return MSG_IGNORE;
        }
        // 这里处理消息
        return MSG_IGNORE;
    }

    /**
     * 群事件-管理员变动 (Type=101)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/被取消管理员 2/被设置管理员
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupAdmin(int subtype, int sendTime, long fromGroup, long beingOperateQQ) {
    	// 功能2-A1:群管理员变动提醒
    	try {
    		// 判断消息来源群聊是否已被临时或永久屏蔽(功能M-4)
    		if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时或永久屏蔽
    		{
    			return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
    		}
    		// [end]
    	switch (subtype) //判断事件类型
		{
		case 1: //被取消管理员
			if ((CQ != null) && (beingOperateQQ == CQ.getLoginQQ())) { //如果被操作对象是机器人QQ
				Calendar nowCalendar = Calendar.getInstance(); //获取当前系统时间配置
				String today = new SimpleDateFormat("YYYYMMdd").format(nowCalendar.getTime()); //格式化当前系统日期
				ResultSet thisGroupSpeakRanking = GlobalDatabases.dbgroup_ranking_speaking
						  .executeQuery("SELECT name FROM sqlite_master WHERE type='table' and name='" + fromGroup + ":" + today + "';");
				/* 执行查询当前群聊今日发言排行数据表的表名集合
				 * SELECT name FROM sqlite_master WHERE type='table' and name='[fromGroup]:[today]';
				 */
				if (thisGroupSpeakRanking.next()) { //如果群聊日发言排行榜数据表存在（功能3-1）
					GlobalDatabases.dbgroup_ranking_speaking.executeNonQuerySync("DELETE FROM '" + fromGroup + ":" + today + "';");
				/* 执行表清空语句
				 * DELETE FROM '[fromGroup]:[today]';
				 */
				CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
						"bot被群主取消管理员身份，部分功能已关闭." + "\n" + 
						"本群成员日发言排行榜已清空.");
				} else { //否则
					CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
							"bot被群主取消管理员身份，部分功能已关闭.");
				}
			} else {
				CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
						CQ.getGroupMemberInfo(fromGroup, beingOperateQQ).getNick() + "(" + String.valueOf(beingOperateQQ) + ")被群主取消管理员身份.");
			}
			break;
		case 2: //被设置管理员
			if (beingOperateQQ == CQ.getLoginQQ()) { //如果被操作对象是机器人QQ
				CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
						"bot被群主设置成管理员，所有功能已启用.");
			} else {
				CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
						CQ.getGroupMemberInfo(fromGroup, beingOperateQQ).getNick() + "(" + String.valueOf(beingOperateQQ) + ")被群主设置成管理员.");
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return MSG_IGNORE;
    }

    /**
     * 群事件-群成员减少 (Type=102)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/群员离开 2/群员被踢
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(仅子类型为2时存在)
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
    	// 这里处理消息
    	try
    	{
    		// [start] 判断消息来源群聊是否已被临时或永久屏蔽(功能M-4)
    		if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时或永久屏蔽
    		{
    			return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
    		}
    		// [end]
    		// [start] 退群自动加黑(功能1-5-7)
    		if (isGroupAdmin(CQ, fromGroup)) { //如果机器人是该群管理
    			switch (subtype)
    			{
    			case 1: //主动退群
    				ResultSet thisGroupBListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + fromGroup + ";");
    				/* 执行查询语句
    				 * SELECT * FROM BList WHERE GroupId=[fromGroup];
    				 */
    				if ((thisGroupBListSet.next()) && (thisGroupBListSet.getBoolean("ExitAutoAddStat") == true)) { //如果该群黑名单已开启且退群加黑为启用状态
    					if (thisGroupBListSet.getString("BListArray") != null) { //如果该群黑名单不为空
    						ArrayList<String> thisGroupBList = new ArrayList<String>(Arrays.asList(thisGroupBListSet.getString("BListArray").split(",")));
    						if (!thisGroupBList.contains(String.valueOf(fromQQ))) { //如果退群人不在黑名单里
								thisGroupBList.add(String.valueOf(fromQQ)); //添加
    						}
    						StringBuilder newBListArrayBuilder = new StringBuilder();
    						for (String thisGroupBanPerson : thisGroupBList) { //遍历thisGroupBList
    							 if (thisGroupBanPerson.equals(thisGroupBList.get(0))) { //如果当前遍历到的是第一个
									 newBListArrayBuilder.append(thisGroupBanPerson);
								} else { //不是第一个
									 newBListArrayBuilder.append(",").append(thisGroupBanPerson);
								}
    						}
    						GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + newBListArrayBuilder.toString() + "' WHERE GroupId=" + fromGroup +  ";");
    						/* 执行更新语句
    						 * UPDATE BList SET BListArray='[newBListArrayBuilder]' WHERE GroupId=[fromGroup];
    						 */
    					} else { //如果该群黑名单为空
    						GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + fromQQ + "' WHERE GroupId=" + fromGroup +  ";");
    						/* 执行更新语句
    						 * UPDATE BList SET BListArray='[fromQQ]' WHERE GroupId=[fromGroup];
    						 */
					   }
    					ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
    					/* 执行查询语句
    					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
    					 */
    					if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet.getString("ExitStr")
									.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
									.replace("【操作者】", "null")
									.replace("【退群QQ】", String.valueOf(beingOperateQQ))
									.replace("【操作QQ】", "null");
							CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
						} else {
    					CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")退出本群,已自动加入黑名单。");
						}
    					return 1;
    				} else { //该群黑名单未开启或退群加黑未开启
    					ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
    					/* 执行查询语句
    					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
    					 */
    					if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet.getString("ExitStr")
									.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
									.replace("【操作者】", "null")
									.replace("【退群QQ】", String.valueOf(beingOperateQQ))
									.replace("【操作QQ】", "null");
							CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
						} else {
    					CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")已离开本群。");
						}
    					return 0;
					}
    			case 2: //被踢
    				ResultSet thisGroupBListSet2 = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + fromGroup + ";");
    				/* 执行查询语句
    				 * SELECT * FROM BList WHERE GroupId=[fromGroup];
    				 */
    				if ((thisGroupBListSet2.next()) && (thisGroupBListSet2.getBoolean("ExitAutoAddStat") == true)) { //如果该群黑名单已开启且退群加黑为启用状态
    					if (thisGroupBListSet2.getString("BListArray") != null) { //如果该群黑名单不为空
    						ArrayList<String> thisGroupBList = new ArrayList<String>(Arrays.asList(thisGroupBListSet2.getString("BListArray").split(",")));
    						if (!thisGroupBList.contains(String.valueOf(fromQQ))) { //如果退群人不在黑名单里
								thisGroupBList.add(String.valueOf(fromQQ)); //添加
    						}
    						StringBuilder newBListArrayBuilder = new StringBuilder();
    						for (String thisGroupBanPerson : thisGroupBList) { //遍历thisGroupBList
    							 if (thisGroupBanPerson.equals(thisGroupBList.get(0))) { //如果当前遍历到的是第一个
									 newBListArrayBuilder.append(thisGroupBanPerson);
								} else { //不是第一个
									 newBListArrayBuilder.append(",").append(thisGroupBanPerson);
								}
    						}
    						GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + newBListArrayBuilder.toString() + "' WHERE GroupId=" + fromGroup +  ";");
    						/* 执行更新语句
    						 * UPDATE BList SET BListArray='[newBListArrayBuilder]' WHERE GroupId=[fromGroup];
    						 */
    					} else { //如果该群黑名单为空
    						GlobalDatabases.dbgroup_list.executeNonQuerySync("UPDATE BList SET BListArray='" + fromQQ + "' WHERE GroupId=" + fromGroup +  ";");
    						/* 执行更新语句
    						 * UPDATE BList SET BListArray='[fromQQ]' WHERE GroupId=[fromGroup];
    						 */
					   }
    					ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
    					/* 执行查询语句
    					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
    					 */
    					String fromQQNick = CQ.getStrangerInfo(fromQQ).getNick(); //定义操作者的昵称
    					if (!CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard().equals(""))  //如果操作者群内存在昵称
    						fromQQNick = CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard(); //将操作者昵称设置成群名片昵称
    					if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet.getString("ExitStr")
									.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
									.replace("【操作者】", fromQQNick)
									.replace("【退群QQ】", String.valueOf(beingOperateQQ))
									.replace("【操作QQ】", String.valueOf(fromQQ));
							CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
						} else {
							CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
	    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")被管理员:" + 
	    							fromQQNick + "("  + fromQQ + ")移出本群,已自动加入黑名单。");
						}
    					return 1;
    				} else { //该群黑名单未开启或退群加黑未开启
    					ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
    					/* 执行查询语句
    					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
    					 */
    					String fromQQNick = CQ.getStrangerInfo(fromQQ).getNick(); //定义操作者的昵称
    					if (!CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard().equals(""))  //如果操作者群内存在昵称
    						fromQQNick = CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard(); //将操作者昵称设置成群名片昵称
    					if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet.getString("ExitStr")
									.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
									.replace("【操作者】", fromQQNick)
									.replace("【退群QQ】", String.valueOf(beingOperateQQ))
									.replace("【操作QQ】", String.valueOf(fromQQ));
							CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
						} else {
							CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
	    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")被管理员:" + 
	    							fromQQNick + "("  + fromQQ + ")移出本群。");
						}
    					return 0;
					}
    			}
    		} else { //如果机器人不是该群管理
				switch (subtype)
				{
				case 1: //主动退群
					 ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
  					/* 执行查询语句
  					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
  					 */
  					if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet.getString("ExitStr")
								.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
								.replace("【操作者】", "null")
								.replace("【退群QQ】", String.valueOf(beingOperateQQ))
								.replace("【操作QQ】", "null");
						CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
					} else {
						CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")已离开本群。");
					}
					return 0;
				case 2: //被踢
					 ResultSet thisGroupCustomSet2 = GlobalDatabases.dbgroup_custom.executeQuery("SELECT ExitStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
  					/* 执行查询语句
  					 * SELECT ExitStr FROM Prompt WHERE GroupId=[fromGroup];
  					 */
  					String fromQQNick = CQ.getStrangerInfo(fromQQ).getNick(); //定义操作者的昵称
  					if (!CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard().equals(""))  //如果操作者群内存在昵称
  						fromQQNick = CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard(); //将操作者昵称设置成群名片昵称
  					if ((thisGroupCustomSet2.next()) && (thisGroupCustomSet2.getString("ExitStr") != null)) { //如果该群自定义退群设置不为空
							String customExitTip = thisGroupCustomSet2.getString("ExitStr")
									.replace("【退群者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
									.replace("【操作者】", fromQQNick)
									.replace("【退群QQ】", String.valueOf(beingOperateQQ))
									.replace("【操作QQ】", String.valueOf(fromQQ));
							CQ.sendGroupMsg(fromGroup, customExitTip); //发送自定义退群提醒消息
						} else {
							CQ.sendGroupMsg(fromGroup, FriendlyName + "\n" + 
	    							CQ.getStrangerInfo(beingOperateQQ).getNick() + "(" + beingOperateQQ + ")被管理员:" + 
	    							fromQQNick + "("  + fromQQ + ")移出本群。");
						}
					return 0;
				}
			}
    		// [end]
    	}
    	catch (Exception e) {
    		CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + e.getMessage() + "\n" + ExceptionHelper.getStackTrace(e));
    	}
    	return MSG_IGNORE;
    }

    /**
     * 群事件-群成员增加 (Type=103)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/管理员已同意 2/管理员邀请
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(即管理员QQ)
     * @param beingOperateQQ 被操作QQ(即加群的QQ)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
      try {
      	  // 这里处理消息
        	// 读取机器人黑名单列表文件(功能M-3)
       	  ResultSet AllBanSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM AllBan");
    			ArrayList<Long> AllBanList = new ArrayList<Long>(); //使用腾讯系统QQ号数组初始化AllBanList(忽略掉系统QQ号的私聊消息)
    			if (AllBanSet.next()) { //如果黑名单数据表不为空
    				 AllBanSet.beforeFirst(); //移动指针到开头
    				 while (AllBanSet.next()) { //遍历AllBanSet
    					  AllBanList.add(AllBanSet.getLong("QQId")); //将当前遍历到的QQ号添加到AllBanList中
    				 }
    				 for (Long BanPerson : AllBanList) { //遍历AllBanList
    					  if ((fromQQ == BanPerson.longValue())) //如果消息来源成员为机器人黑名单人员
    					  {
    							return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
    					  } else if (new QQInfo(fromQQ).getNick().equals(tencentSysAccount)) {
								return MSG_IGNORE;
						  }
    				 }
    			}
    		// 判断消息来源群聊是否已被临时或永久屏蔽(功能M-4)
    		if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时或永久屏蔽
    		{
    			return MSG_INTERCEPT; //不多废话，直接返回（拜拜了您嘞）
    		}
    		//群迎新(功能S-A1)
    		if (beingOperateQQ == CQ.getLoginQQ()) { //如果是机器人入群
    			CQ.sendGroupMsg(fromGroup, "大佬们好……\n发送\"!m\"查看帮助~");
    			if (fromQQ != masterQQ) { //如果不是机器人主人邀请入群
    				CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" + 
    						"机器人已受邀入群提醒\n" + 
    						"来源群聊:" + getGroupName(CQ, fromGroup) + "(" + fromGroup + ")\n" + 
    						"邀请人:" + CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ")\n" + 
    						"如果不是主动同意而进群,建议输入!eg " + fromGroup + "退出该群.");
    			}
    		} else { //如果不是机器人入群
    			String fromQQNick = CQ.getStrangerInfo(fromQQ).getNick(); //定义操作者的昵称
    			if (!CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard().equals(""))  //如果操作者群内存在昵称
    				fromQQNick = CQ.getGroupMemberInfo(fromGroup, fromQQ).getCard(); //将操作者昵称设置成群名片昵称
    			ResultSet thisGroupCustomSet = GlobalDatabases.dbgroup_custom.executeQuery("SELECT WelcomeStr FROM Prompt WHERE GroupId=" + fromGroup + ";");
				/* 执行查询语句
				 * SELECT WelcomeStr FROM Prompt WHERE GroupId=[fromGroup];
				 */
    			if ((thisGroupCustomSet.next()) && (thisGroupCustomSet.getString("WelcomeStr") != null)) { //如果该群自定义迎新提示设置存在
    				long memberNo = Long.parseLong(Integer.toString(CQ.getGroupMemberList(fromGroup).size()));
    				String customWelcomeTip = thisGroupCustomSet.getString("WelcomeStr")
    						.replace("【@】", new CQCode().at(beingOperateQQ))
    						.replace("【成员序号】", String.valueOf(memberNo))
    						.replace("【申请者】", CQ.getStrangerInfo(beingOperateQQ, true).getNick())
    						.replace("【操作者】", fromQQNick)
    						.replace("【申请QQ】", String.valueOf(beingOperateQQ))
    						.replace("【操作QQ】", String.valueOf(fromQQ));
    				CQ.sendGroupMsg(fromGroup, customWelcomeTip); //发送自定义迎新提醒消息
    			} else { //如果该群自定义迎新提示不存在
    				long memberNo = Long.parseLong(Integer.toString(CQ.getGroupMemberList(fromGroup).size()));
    				StringBuilder commonWelcomeTipBuilder = new StringBuilder(FriendlyName).append("\n")
    						.append("No.").append(memberNo).append("\n")
    						.append(CQ.getStrangerInfo(beingOperateQQ).getNick()).append("(").append(beingOperateQQ).append(")").append("\n")
    						.append("欢迎加入本群，在本群请注意遵守群规哦~\n")
    						.append("操作者:").append(fromQQNick).append("(").append(fromQQ).append(")");
    				CQ.sendGroupMsg(fromGroup, commonWelcomeTipBuilder.toString()); //发送默认迎新提醒消息
    			}
    		}
    		return MSG_IGNORE;
	 } catch (Exception e) {
		  CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
		  return MSG_IGNORE;
	 	}
    }

    /**
     * 群事件-群禁言 (Type=104)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType        子类型，1/被解禁 2/被禁言
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ
     * @param beingOperateQQ 被操作QQ(若为全群禁言/解禁，则本参数为 0)
     * @param duration       禁言时长(单位 秒，仅子类型为2时可用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupBan(int subType, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ, long duration) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 好友事件-好友已添加 (Type=201)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype  子类型，目前固定为1
     * @param sendTime 发送时间(时间戳)
     * @param fromQQ   来源QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int friendAdd(int subtype, int sendTime, long fromQQ) {
        // 这里处理消息

        return MSG_IGNORE;
    }

    /**
     * 请求-好友添加 (Type=301)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，目前固定为1
     * @param sendTime     发送时间(时间戳)
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddFriend(int subtype, int sendTime, long fromQQ, String msg, String responseFlag) {
        // 这里处理消息
    	try {
    		// 读取机器人黑名单列表文件(功能M-3)
    		ResultSet AllBanSet = dbgroup_list.executeQuery("SELECT * FROM AllBan");
  			ArrayList<Integer> AllBanList = new ArrayList<Integer>();
  			while (AllBanSet.next()) {
  				 AllBanList.add(AllBanSet.getInt("qqId"));
  			}
  			for (Integer BanPerson : AllBanList) {
  			if (fromQQ == BanPerson.longValue()) // 如果消息来源成员为机器人黑名单人员
  				{
  				 	CQ.setFriendAddRequest(msg, REQUEST_REFUSE); // 拒绝好友添加请求
  					return MSG_INTERCEPT;
  				}
  			} 
		   /**
          * REQUEST_ADOPT 通过
          * REQUEST_REFUSE 拒绝
          */
         // CQ.setFriendAddRequest(responseFlag, REQUEST_ADOPT, null); // 同意好友添加请求
         return MSG_IGNORE;
    	} catch (Exception e) {
		   CQ.logError(AppName, "发生异常,请立即处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
		   return MSG_IGNORE;
    	}
        
    }

    /**
     * 请求-群添加 (Type=302)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，1/他人申请入群 2/自己(即登录号)受邀入群
     * @param sendTime     发送时间(时间戳)
     * @param fromGroup    来源群号
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddGroup(int subtype, int sendTime, long fromGroup, long fromQQ, String msg,
                               String responseFlag) {
        // 这里处理消息
    	switch (subtype)
		{
    	case 1: //他人申请入群
    		try {
    			// [start] 读取机器人群聊黑名单列表文件(功能M-4)
    			// 判断消息来源群聊是否已被临时或永久屏蔽(功能M-4)
    			if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时或永久屏蔽
    			{
    				return 1;
    			}
    			// [end]
    			//功能1-5:群聊黑名单
        			ResultSet currentGroupBListSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM BList WHERE GroupId=" + fromGroup + ";");
        			/* 执行查询语句
        			 * SELECT * FROM BList WHERE GroupId=[fromGroup];
        			 */
    				if ((currentGroupBListSet.next()) && (currentGroupBListSet.getString("BListArray") != null)) { //如果当前群黑名单列表不为空
    					for (String bListPerson : currentGroupBListSet.getString("BListArray").split(",")) { //循环遍历当前群黑名单列表
    						if (bListPerson.equals(String.valueOf(fromQQ))) { //如果申请加群人员为当前群黑名单人员
								CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD,REQUEST_REFUSE, "您是本群黑名单人员，无法加入本群!"); //拒绝申请
								if (!currentGroupBListSet.getBoolean("RefusePromptStat")) { //如果拒绝不提醒为false（拒绝提醒）
									StringBuilder result = new StringBuilder(FriendlyName).append("\n")
											.append(CQ.getStrangerInfo(fromQQ, true).getNick() + "(" + fromQQ + ")属于本群黑名单人员,已拒绝其入群。");
									CQ.sendGroupMsg(fromGroup, result.toString());
								}
							}
    					}
    				}
    			} catch (SQLException  e) {
    				 CQ.logError(AppName, "群聊" + fromGroup + "黑名单读取出现异常(java.sql.SQLException)");
    			}
    		break;
		case 2: //机器人QQ受邀入群
			try {
				// 若是机器人主人邀请入群，则同意
			    	if(fromQQ == Global.masterQQ){
						CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT, null);
						return MSG_INTERCEPT;
					}
			    	// [start] 读取机器人黑名单列表文件(功能M-3)
			    	 ResultSet AllBanSet = GlobalDatabases.dbgroup_list.executeQuery("SELECT * FROM AllBan");
		  			ArrayList<Long> AllBanList = new ArrayList<Long>(); //使用腾讯系统QQ号数组初始化AllBanList(忽略掉系统QQ号的私聊消息)
		  			if (AllBanSet.next()) { //如果黑名单数据表不为空
		  				 AllBanSet.beforeFirst(); //移动指针到开头
		  				 while (AllBanSet.next()) { //遍历AllBanSet
		  					  AllBanList.add(AllBanSet.getLong("QQId")); //将当前遍历到的QQ号添加到AllBanList中
		  				 }
		  				 for (Long BanPerson : AllBanList) { //遍历AllBanList
		  					  if (fromQQ == BanPerson.longValue()) //如果消息来源成员为机器人黑名单人员
		  					  {
		  						// 拒绝邀请
		  							CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_REFUSE, "您是机器人黑名单人员，无法邀请机器人入群!");
		  							return 1;
		  					  } else if (new QQInfo(fromQQ).getNick().equals(tencentSysAccount)) {
								
						  }
		  				 }
		  			}
					// [end]
					// [start] 读取机器人群聊黑名单列表文件(功能M-4)
					// 判断消息来源群聊是否已被临时或永久屏蔽(功能M-4)
					if (Global.isGroupBanned(fromGroup)) //如果消息来源群聊已被临时或永久屏蔽
					{
						// 拒绝邀请
						CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_REFUSE, "该群已被临时或永久加黑,无法邀请机器人入群!");
						return 1;
					}
					// [end]
					// [start] 机器人受邀入群确认(私聊功能5)
					CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" + 
							"机器人受邀入群请求提醒\n" + 
							"请求标识:" + responseFlag + "\n" +
							"邀请者:" + CQ.getStrangerInfo(fromQQ).getNick() + "(" + fromQQ + ")\n" + 
							"请求邀请加入的群:" + fromGroup + "\n" + 
							"输入!cig " + responseFlag + " agree通过请求\n" + 
							"输入!cig " + responseFlag + " refuse拒绝请求");
					// [end]
					break;
		  } catch (Exception e) {
				CQ.logError(AppName, "发生异常,请及时处理\n详细信息:\n" + ExceptionHelper.getStackTrace(e));
		  }
		}  	
        /**
         * REQUEST_ADOPT 通过
         * REQUEST_REFUSE 拒绝
         * REQUEST_GROUP_ADD 群添加
         * REQUEST_GROUP_INVITE 群邀请
         */
		/*if(subtype == 1){ // 本号为群管理，判断是否为他人申请入群
			CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD, REQUEST_ADOPT, null);// 同意入群
		}
		*/
        return MSG_IGNORE;
    }

}
