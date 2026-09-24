# UltrasCore — دليل عربي مختصر

نظام Core كامل لسيرفر Minecraft (Paper، Java 25)، بالإنجليزية والعربية. للتفاصيل التقنية الكاملة راجع `README.md` (الإنجليزي هو المرجع الأساسي).

## البناء
البيئة اللي كُتب فيها هذا الكود بدون إنترنت، فـ `mvn clean package` ما اشتغل فعليًا هنا. استخدم GitHub Actions المرفق (`.github/workflows/build.yml`) بعد رفع المشروع، أو ابنِه محليًا بـ Maven + JDK 25.

## التثبيت
1. حط ملف الـ jar الناتج في `plugins/`.
2. شغّل السيرفر مرة عشان تتولد `config.yml` و`lang/`.
3. عدّل `config.yml` (فيه شرح عربي جنب كل خيار)، ثم `/ultrascore reload`.

## أهم الأوامر
| الأمر | الوصف |
|---|---|
| `/tpa`, `/tpahere` | طلبات الانتقال |
| `/home`, `/homes`, `/sethome`, `/delhome` | المنازل |
| `/rtp [world]` | انتقال عشوائي آمن |
| `/warp` | الـ Warps (عام/خاص/بكلمة مرور) |
| `/spawn`, `/setspawn` | نقطة الظهور |
| `/setting` | إعداداتك الشخصية (GUI) |
| `/chat`, `/msg`, `/bc` | الشات، الرسائل الخاصة، الإعلانات |
| `/hide`, `/names`, `/ranks`, `/nv` | الخصوصية والمظهر |
| `/hub`, `/sethub` | الـ Hub (محلي أو عبر بروكسي) |
| `/ultrascore reload` | إعادة تحميل كامل بدون إعادة تشغيل السيرفر |

كل أمر ونظام قابل للتعطيل من `config.yml` بدون ما يأثر على الباقي.

## نقاط مهمة بصراحة (قيود موجودة الآن)
- **Ranks:** الإعداد يُخزَّن بس، لكن UltrasCore ما يعرض رتب فعليًا فوق الرأس — هذا عادة شغلة Plugin صلاحيات مثل LuckPerms، وهذا التبديل جاهز لأي نظام خارجي يقرأه.
- **Scoreboard/TAB:** فيه كشف تلقائي لأي Plugin تاب خارجي (زي TAB) لتفادي التعارض، لكن UltrasCore نفسه ما يبني Scoreboard أو Tab List خاص به في هذا الإصدار.
- **رسائل الموت:** تصفية المستلم + الصوت فقط لكل لاعب؛ نص رسالة الموت نفسه يبقى نفس رسالة Minecraft الافتراضية (لتفادي تكرار منطق "سبب الموت" بشكل أسوأ من الأصلي).
- **Warps GUI:** حذف الـ Warp بالـ GUI يصير بـ Shift-click بدل شاشة تأكيد منفصلة.
- **PlaceholderAPI:** مسجّل تلقائيًا إذا PlaceholderAPI موجود، ويغطي: `%ultrascore_player%` `%ultrascore_uuid%` `%ultrascore_world%` `%ultrascore_x/y/z%` `%ultrascore_ping%` `%ultrascore_homes%` `%ultrascore_home_limit%` `%ultrascore_warps%` `%ultrascore_tpa_status%` `%ultrascore_chat_status%` `%ultrascore_pm_status%` `%ultrascore_nv_status%` `%ultrascore_server_name%`.

## Hub عبر بروكسي (Velocity/BungeeCord)
غيّر `systems.hub.mode` إلى `PROXY` وحدد `systems.hub.proxy-server-name` باسم السيرفر المسجل عندك بالبروكسي. UltrasCore يستخدم قناة `BungeeCord` القياسية (تشتغل مع BungeeCord وVelocity بتفعيل legacy forwarding) — ما يحتاج أي Dependency خاص بالبروكسي.
