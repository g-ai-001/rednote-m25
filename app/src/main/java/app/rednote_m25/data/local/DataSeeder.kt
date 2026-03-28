package app.rednote_m25.data.local

import app.rednote_m25.data.local.dao.NoteDao
import app.rednote_m25.data.local.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val noteDao: NoteDao
) {
    fun seedIfEmpty(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val existingNotes = noteDao.getAllNotes().first()
            if (existingNotes.isEmpty()) {
                seedSampleNotes()
            }
        }
    }

    private suspend fun seedSampleNotes() {
        val sampleNotes = listOf(
            NoteEntity(
                title = "春天来了，带上相机去拍照吧📷",
                content = "春天是最适合拍照的季节！给大家分享几个我常用的拍照技巧：\n\n1. 利用自然光\n早晨和傍晚的光线最柔和，拍出来的人像也最好看。\n\n2. 选择背景\n尽量选择简洁的背景，这样主体更突出。\n\n3. 构图技巧\n试试三分法构图，让照片更有美感。\n\n4. 后期调色\n适当调整一下色调，让照片更有氛围感。\n\n大家有什么拍照技巧也可以在评论区分享呀～",
                coverImageUrl = "https://picsum.photos/seed/spring/400/300",
                imageUrls = "https://picsum.photos/seed/spring1/400/500,https://picsum.photos/seed/spring2/400/400,https://picsum.photos/seed/spring3/400/450",
                authorName = "摄影达人小王",
                authorAvatarUrl = "https://picsum.photos/seed/avatar1/100/100",
                likeCount = 2580,
                collectCount = 890,
                commentCount = 156,
                shareCount = 78,
                tags = "摄影,春天,拍照技巧"
            ),
            NoteEntity(
                title = "周末在家做了一顿美美的早餐🥐",
                content = "今天尝试了法式吐司配上新鲜水果，好吃到哭！\n\n做法其实很简单：\n1. 吐司片裹上蛋液\n2. 小火煎至两面金黄\n3. 淋上蜂蜜或枫糖浆\n4. 配上喜欢的水果\n\n我用了蓝莓、草莓和香蕉，营养又美味～\n\n大家早餐都喜欢吃什么呀？",
                coverImageUrl = "https://picsum.photos/seed/breakfast/400/350",
                imageUrls = "https://picsum.photos/seed/breakfast1/400/400,https://picsum.photos/seed/breakfast2/400/350",
                authorName = "美食爱好者",
                authorAvatarUrl = "https://picsum.photos/seed/avatar2/100/100",
                likeCount = 1892,
                collectCount = 567,
                commentCount = 89,
                shareCount = 45,
                tags = "美食,早餐,简单料理"
            ),
            NoteEntity(
                title = "分享我的极简护肤routine✨",
                content = "很多姐妹问我护肤步骤，其实我的护肤很简单：\n\n🌅 晨间：\n1. 温和洁面\n2. 爽肤水\n3. 精华\n4. 面霜\n5. 防晒\n\n🌙 夜间：\n1. 卸妆\n2. 洁面\n3. 爽肤水\n4. 精华\n5. 面霜\n\n重点是：不要过度护肤！给皮肤留点喘息的空间。\n\n选择护肤品要看成分，而不是品牌～",
                coverImageUrl = "https://picsum.photos/seed/skincare/400/400",
                imageUrls = "https://picsum.photos/seed/skincare1/400/450,https://picsum.photos/seed/skincare2/400/400,https://picsum.photos/seed/skincare3/400/380",
                authorName = "护肤笔记",
                authorAvatarUrl = "https://picsum.photos/seed/avatar3/100/100",
                likeCount = 5620,
                collectCount = 2100,
                commentCount = 342,
                shareCount = 120,
                tags = "护肤,极简,日常"
            ),
            NoteEntity(
                title = "租房改造 | 500块让房间焕然一新🏠",
                content = "租的房子也可以很温馨！分享一下我的低成本改造经验：\n\n🛋️ 客厅区域：\n- 换了一个沙发毯（89元）\n- 添加了几个抱枕（45元）\n- 地毯（68元）\n\n💡 灯光氛围：\n- LED串灯（29元）\n- 台灯（59元）\n\n🖼️ 墙面装饰：\n- 装饰画（68元）\n- 照片墙（35元）\n\n总计不到500块，效果却很惊艳！\n\n大家有什么改造心得吗？",
                coverImageUrl = "https://picsum.photos/seed/room/400/320",
                imageUrls = "https://picsum.photos/seed/room1/400/400,https://picsum.photos/seed/room2/400/350,https://picsum.photos/seed/room3/400/380,https://picsum.photos/seed/room4/400/420",
                authorName = "家居控",
                authorAvatarUrl = "https://picsum.photos/seed/avatar4/100/100",
                likeCount = 8900,
                collectCount = 3500,
                commentCount = 456,
                shareCount = 230,
                tags = "租房改造,家居,低成本"
            ),
            NoteEntity(
                title = "周末徒步去山里吸氧🌲",
                content = "逃离城市喧嚣，去山里徒步真的太治愈了！\n\n这次去的是郊外的国家森林公园，全程约10公里。\n\n沿途风景绝美：\n- 清澈的小溪\n- 高大的松树林\n- 山顶的观景台\n\nTips：\n1. 穿防滑的徒步鞋\n2. 带足够的水和干粮\n3. 注意防晒\n4. 不要乱扔垃圾\n\n下山后去吃了当地的农家乐，完美的一天～",
                coverImageUrl = "https://picsum.photos/seed/hiking/400/380",
                imageUrls = "https://picsum.photos/seed/hiking1/400/400,https://picsum.photos/seed/hiking2/400/360,https://picsum.photos/seed/hiking3/400/420",
                authorName = "户外爱好者",
                authorAvatarUrl = "https://picsum.photos/seed/avatar5/100/100",
                likeCount = 3200,
                collectCount = 1200,
                commentCount = 178,
                shareCount = 92,
                tags = "徒步,户外,周末"
            ),
            NoteEntity(
                title = "新手学化妆，这些坑千万别踩💄",
                content = "作为从零开始学化妆的新手，我踩过不少坑，今天来分享一些经验：\n\n❌ 不要盲目种草\n每个人的肤质、脸型都不同，适合别人的不一定适合你。\n\n❌ 不要用太多产品\n新手阶段，精简步骤更重要。\n\n✅ 选择适合自己肤质的底妆\n油皮选控油的，干皮选保湿的。\n\n✅ 眉毛很重要\n画好眉毛整个人气质都不一样。\n\n✅ 多练习\n化妆真的需要多练，不要怕画不好。\n\n大家有什么化妆问题可以在评论区问我呀～",
                coverImageUrl = "https://picsum.photos/seed/makeup/400/340",
                imageUrls = "https://picsum.photos/seed/makeup1/400/400,https://picsum.photos/seed/makeup2/400/380",
                authorName = "美妆小白",
                authorAvatarUrl = "https://picsum.photos/seed/avatar6/100/100",
                likeCount = 4500,
                collectCount = 1800,
                commentCount = 289,
                shareCount = 145,
                tags = "化妆,新手,美妆"
            ),
            NoteEntity(
                title = "咖啡拉花入门，手把手教你☕",
                content = "在家也能做出好看的拉花！今天分享我的拉花练习经验：\n\n🎯 基础设备：\n- 半自动咖啡机\n- 奶泡杯\n- 拉花缸\n\n🥛 奶泡技巧：\n- 牛奶要新鲜\n- 温度控制在60-65度\n- 先打发后整合\n\n🌸 入门图案：\n1. 爱心（最简单）\n2. 叶子\n3. 郁金香\n\n关键是多加练习！我练了大概一个月才慢慢找到感觉～\n\n有没有同样喜欢拉花的朋友？",
                coverImageUrl = "https://picsum.photos/seed/coffee/400/360",
                imageUrls = "https://picsum.photos/seed/coffee1/400/420,https://picsum.photos/seed/coffee2/400/380,https://picsum.photos/seed/coffee3/400/400",
                authorName = "咖啡师小李",
                authorAvatarUrl = "https://picsum.photos/seed/avatar7/100/100",
                likeCount = 2100,
                collectCount = 780,
                commentCount = 123,
                shareCount = 67,
                tags = "咖啡,拉花,教程"
            ),
            NoteEntity(
                title = "养猫日记 | 第一次当铲屎官🐱",
                content = "上个月领养了一只小猫咪，取名叫团子～\n\n养猫后的变化：\n1. 每天早上被踩醒\n2. 键盘上总是长出猫\n3. 衣服上沾满猫毛\n4. 幸福感爆棚\n\n但也有一些烦恼：\n- 沙发被抓坏了\n- 凌晨跑酷\n- 挑食的时候很头疼\n\n新手铲屎官求经验，大家有什么养猫建议吗？",
                coverImageUrl = "https://picsum.photos/seed/cat/400/400",
                imageUrls = "https://picsum.photos/seed/cat1/400/420,https://picsum.photos/seed/cat2/400/380,https://picsum.photos/seed/cat3/400/400,https://picsum.photos/seed/cat4/400/360",
                authorName = "铲屎官日记",
                authorAvatarUrl = "https://picsum.photos/seed/avatar8/100/100",
                likeCount = 9800,
                collectCount = 4200,
                commentCount = 678,
                shareCount = 340,
                tags = "猫咪,宠物,铲屎官"
            )
        )

        noteDao.insertNotes(sampleNotes)
    }
}
