package cf.ots123it.open.sdubotr;

import org.meowy.cqp.jcq.entity.CoolQ;
import org.meowy.cqp.jcq.event.JcqAppAbstract;
import static cf.ots123it.open.sdubotr.Global.*;
import cf.ots123it.jhlper.CommonHelper;
import cf.ots123it.jhlper.ExceptionHelper;

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
	}
}