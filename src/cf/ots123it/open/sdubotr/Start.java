package cf.ots123it.open.sdubotr;

import org.meowy.cqp.jcq.entity.*;
import org.meowy.cqp.jcq.event.JcqAppAbstract;

import cf.ots123it.jhlper.IOHelper;
import cf.ots123it.jhlper.UserInterfaceHelper;

import static cf.ots123it.open.sdubotr.Global.*;

import java.io.File;

/**
 * 123 SduBotR的主处理类
 * <br>
 * 保留JCQ Demo的基本注释，方便开发
 * @author <a href="mailto:a15951@qq.com">御坂12456</a>
 * @version Alpha 0.0.1
 */
public class Start extends JcqAppAbstract implements ICQVer, IMsg, IRequest {
	/**
	 * 123 SduBotR 数据存放路径(不建议在此调用,请在Global调用)
	 */
	public static String appDirectory;
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
    public Start()
	{
	}

	/**
     * 用main方法调试可以最大化的加快开发效率，检测和定位错误位置<br/>
     *
     * @param args 系统参数
     */
    public static void main(String[] args) {

        // 要测试主类就先实例化一个主类对象
        Start test = new Start();
        // 获取当前酷Q操作对象
        CoolQ CQ = test.getCoolQ();
        test.startup();// 程序运行开始 调用应用初始化方法
        test.enable();// 程序初始化完成后，启用应用，让应用正常工作
        // 开始模拟发送消息
        
        // 以下是收尾触发函数
        // demo.disable();// 实际过程中程序结束不会触发disable，只有用户关闭了此插件才会触发
        test.exit();// 最后程序运行结束，调用exit方法
    }

    /**
     * 打包后将不会调用 请不要在此事件中写其他代码
     *
     * @return 返回应用的ApiVer、Appid
     */
    public String appInfo() {
        String AppID = "cf.ots123it.open.sdubotr";
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
        // 返回如：D:\CoolQ\data\app\org.meowy.cqp.jcq\data\app\cf.ots123it.open.sdubotr\

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
        enable = true;
        CQ.logInfo("123 SduBotR", "获取应用数据目录成功:\n" + "设置目录:" + appDirectory);
        if(!(new File(appDirectory + "/firstopen.stat")).exists()) //若无firstopen.stat文件（即首次打开）
        {
        	CQ.logInfo("123 SduBotR", "检测到无firstopen.stat文件，判断为首次启动，正在初始化");
        	Initialize(CQ); //调用初始化方法
        	UserInterfaceHelper.MsgBox("123 SduBotR - 首次启动", "程序将向Global.java中机器人主人QQ号变量masterQQ(" + masterQQ + ")发送测试信息\n" + 
        	"若未接收到消息请注意Global.java中masterQQ的值是否正确(由于是强制Long类型所以数值后需要加L)", UserInterfaceHelper.MsgBoxButtons.Info);
        	CQ.sendPrivateMsg(masterQQ, FriendlyName + "\n" +
        				"这是一条测试消息,如果接收到了该消息代表已初始化完毕，您可以正常使用了\n");
        	
        } else { //存在firstopen.stat文件（非首次打开）
        	 if(!(new File(appDirectory + "/group/list/iMG.txt")).exists()) //[功能1-1]判断重点监视群聊列表文件是否存在
             { //若不存在
             	CQ.logWarning("123 SduBotR", "功能1-1:重点监视群聊列表文件不存在,可能会影响到该功能的正常使用。\n" +
             			"请删除数据目录下的firstopen.stat然后重载插件以重新生成所需文件。");
             }
        	 if(!(new File(appDirectory + "/group/list/iMGBan.txt")).exists()) //[功能1-1]判断违禁词列表是否存在
        	 { //若不存在
        		 CQ.logWarning("123 SduBotR", "功能1-1:重点监视群聊列表文件不存在,可能会影响到该功能的正常使用。\n" +
              			"请删除数据目录下的firstopen.stat然后重载插件以重新生成所需文件。");
        	 }
        	 if(!(new File(appDirectory + "/group/list/funnyWL.txt")).exists()) //[功能S-1]判断滑稽彩蛋白名单文件是否存在
        	 { //若不存在
        		 CQ.logWarning("123 SduBotR", "功能S-1:滑稽彩蛋群聊白名单文件不存在，可能会在不需要应用彩蛋的群聊意外触发彩蛋导致意外情况出现。\n" +
        			    "请删除数据目录下的firstopen.stat然后重载插件以重新生成所需文件。");
        	 }
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
        
        return MSG_IGNORE;
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
        // 如果消息来自匿名者
        if (fromQQ == 80000000L && !fromAnonymous.isEmpty()) {
            // 将匿名用户信息放到 anonymous 变量中
            @SuppressWarnings("unused")
			Anonymous anonymous = CQ.getAnonymous(fromAnonymous);
        }
        if (fromQQ==Global.masterQQ&&msg.startsWith("#")) //若是管理命令前缀且为机器人主人发送的
        	{ //转到ProcessGroupManageMsg类处理
        		ProcessGroupManageMsg.main(CQ, fromGroup, fromQQ, msg);
        	} else
        	{ // 转到ProcessGroupMsg类处理
        		ProcessGroupMsg.main(CQ, fromGroup, fromQQ, msg);
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
        // 这里处理消息

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
        // 这里处理消息
        return MSG_IGNORE;
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

        /**
         * REQUEST_ADOPT 通过
         * REQUEST_REFUSE 拒绝
         */

        // CQ.setFriendAddRequest(responseFlag, REQUEST_ADOPT, null); // 同意好友添加请求
        return MSG_IGNORE;
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

        /**
         * REQUEST_ADOPT 通过
         * REQUEST_REFUSE 拒绝
         * REQUEST_GROUP_ADD 群添加
         * REQUEST_GROUP_INVITE 群邀请
         */
		/*if(subtype == 1){ // 本号为群管理，判断是否为他人申请入群
			CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_ADD, REQUEST_ADOPT, null);// 同意入群
		}
		if(subtype == 2){
			CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT, null);// 同意进受邀群
		}*/
        return MSG_IGNORE;
    }

}
