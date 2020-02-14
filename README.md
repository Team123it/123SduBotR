# 123 SduBotR
**Tips:This is a chinese repository.**<br>
**The javadocs and comments in the source file are all chinese.**<br>
**If you want to help us translate this repository to your own language, you can create a new branch, called master-(your language).**<br>
<br>
*'R' means Resuurection!*<br>
<br>
<p><b><font color="red">注意：本程序最佳运行环境为酷Q Pro，您可以通过功能M-3测试是否可以使用所有功能。</font></b></p>
这是一个酷Q的开源QQ机器人项目。<br>
使用时请遵守相关法律法规和GNU 3.0开源协议。<br>
###### 以下用“CQP”简称酷Q Pro，用“CQA”简称酷Q Air，用“CQ”简称酷Q(CQ码除外)。
###### 以下所使用的路径均为相对于程序数据目录的路径。

## 1.项目使用的库
包括下列Java库:<br>
1.JCQ CoolQ Java Developing Plugin (by:Sobte南荒)<br>
2.123 Java Helper (by:御坂12456)<br>
如果你想要帮助增加模块，可以pull your request，看到后会在此处写上你的名字

## 2.项目运行环境
1.CQA/CQP(32位)<br>
2.Java Jre 1.8<br>
3.CQ插件:[JCQ]开发工具

## 3.项目包含的功能
**!注意:下列内容为123 SduBotR原版(Github版)功能。如果你有什么新奇的玩意想添加，可以pull，然后在这里加上你的功能名、操作方法和昵称。**
### 1.群管理辅助功能
#### 1-1.违禁词提醒功能(作者:御坂12456,更新版本:Alpha 0.0.1)
说明：该功能可以在管理员无法直接长时间盯群时，群内有人发违禁词，第一时间获悉并主动处理。<br>
该功能不直接禁言或撤回消息(CQP专用)，如需可自行添加对应模块。<br>
使用方法：<br>
在\group\list\iMG.txt中添加你想要让机器人提醒的群（一行一个，不能有注释）<br>
在\group\list\iMGBan.txt中添加违禁词（一行一个，可以直接输入CQ码）<br>
以下是示例<br>
<code>[CQ:face,id=13] //呲牙</code><br>
<code>[CQ:face,id=107] //快哭了</code><br>
<code>[CQ:emoji,id=128116] //Emoji:爷</code>

### 机器人主人专用功能(A指Auto,即代码在Start.java中就已自动处理,而未转到ProcessGroupManageMsg.java中处理)
#### M-1.查看运行状态功能(作者:御坂12456,灵感来源:孤灯照镜上,更新版本:Alpha 0.0.5)
说明：该功能可在群内发送机器人的运行状态<br>
使用方法：<br>
机器人主人在群内发送<br>
<code>#stat</code>
#### M-2.测试机器人是否为最佳运行环境(作者:御坂12456,更新版本:Alpha 0.1.0)
说明：该功能将在群内发送一个测试图片来测试程序的所有功能是否可以正常运行（是否使用酷Q Pro运行本程序）<br>
使用方法：<br>
机器人主人在群内发送<br>
<code>#testpic</code>
#### M-3:机器人黑名单(作者:御坂12456,更新版本:Alpha 0.1.1)
说明：该功能可以将某些乱玩机器人的人员加入机器人黑名单当中，无法继续使用机器人，从而防止机器人被滥用<br>
使用方法：<br>
机器人主人在群内发送<br>
<code>#banadd [@/QQ号] //添加黑名单人员</code><br>
<code>#bandel [@/QQ号] //删除黑名单人员</code><br>
<b>注意：机器人主人无法添加自己进入黑名单</b>
#### M-4:机器人群聊黑名单(作者:御坂12456,更新版本:Alpha 0.1.3)
说明：该功能可以将某些乱玩机器人的群聊加入机器人黑名单当中，对应群聊除主人外的所有其它人均无法继续使用机器人，从而防止机器人被滥用<br>
使用方法：<br>
在\group\list\AllGBan.txt中添加你想要让机器人拉黑的群（一行一个，不能有注释）
#### M-A1.机器人自动同意主人群聊邀请(作者:御坂12456,更新版本:Alpha 0.0.8)
说明:该功能将会自动同意机器人主人的群聊邀请(任何情况下)<br>
使用方法: <br>
程序运行时邀请机器人即可,机器人会自动进群;如果主人是普通成员还需要对应群管理员的审核.

### 其它功能
#### O-1.关于(作者:御坂12456,更新版本:Alpha 0.1.0)
说明：该功能可在群内发送"关于 123SduBotR"的相关信息<br>
使用方法:<br>
在群内发送<br>
<code>!about</code>

### 特殊功能
#### S-1.滑稽彩蛋(作者:御坂12456,灵感来源:Sugar 404,更新版本:Alpha 0.0.2)
说明：该功能会随机发送滑稽或滑稽+特殊表情（见下）<br>
会发送的表情包括：<br>
<code>[CQ:face,id=178] //滑稽</code><br>
<code>[CQ:face,id=178][CQ:emoji,id=127166] //滑稽+水滴</code><br>
<code>[CQ:face,id=178][CQ:face,id=66] //滑稽+爱心</code><br>
<code>[CQ:face,id=178][CQ:face,id=147] //滑稽+棒棒糖</code><br>
<code>[CQ:face,id=178][CQ:emoji,id=10068] //滑稽+问号</code><br>
<code>[CQ:face,id=178][CQ:emoji,id=10069] //滑稽+叹号</code><br>
<code>[CQ:face,id=178][CQ:face,id=67] //滑稽+心碎</code><br>
对于不需要触发彩蛋的群聊（如工作群），请:<br>
在\group\list\funnyWL.txt中添加你想要让机器永不触发滑稽彩蛋的群聊（一行一个，不能有注释）

###### 本项目由御坂12456(Misaka12456)开发并发布至Github.
###### 感谢名单:
###### Sugar 404(1668385924)
###### 孤灯照镜上(2926704797)
###### 跨越天际线(2086602519)
