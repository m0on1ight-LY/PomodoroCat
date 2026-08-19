# PomodoroCat (番茄猫) Domain Context

番茄猫是一款融合了番茄钟专注计时、白噪音混音与猫咪治愈养成的 Android 原生应用。

## Language

**FocusSession (专注会话)**:
一次完整的专注计时周期，记录起止时间、实际时长、所属任务标签、状态及用户心得。
_Avoid_: TimerRun, FocusRecord, LogEntry

**TaskTag (任务标签)**:
用户给专注会话打上的分类标签（如学习、工作、阅读、编程等），带有专属图标与主题色。
_Avoid_: Category, Project, Label

**DriedFish (小鱼干)**:
通过完成专注会话获得的虚拟奖励货币（标准为 1 分钟 = 1 小鱼干），用于投喂猫咪、解锁新品种与猫舍道具。
_Avoid_: Coin, Point, Currency, Gold

**CatProfile (猫咪图鉴/伙伴)**:
应用中可供选择与解锁的猫咪伙伴模型，包含皮肤配色方案、专属待机/专注/互动动作及特定性格台词。
_Avoid_: Pet, Avatar, Skin

**BondLevel (亲密度等级)**:
用户与某只特定猫咪之间的情感羁绊等级，通过投喂小鱼干提升，用于解锁高级互动动作、特殊台词与成就。
_Avoid_: PetLevel, FriendshipExp, Intimacy

**FocusDiary (专注日记/心得)**:
在专注会话结束后由用户记录的短评、反思体会以及 1~5 星自评专注度。
_Avoid_: Note, Memo, Comment

**Badge (成就徽章)**:
用户达成特定里程碑（如连续打卡、累计专注时长、解锁全图鉴）所获得的荣誉徽章。
_Avoid_: Achievement, Medal, Trophy
