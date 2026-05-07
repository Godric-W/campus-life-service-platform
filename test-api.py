#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园综合生活服务平台 - 接口测试脚本（修正版）
修复Token处理和用户ID解析问题
"""

import requests
import json
import time
from typing import Dict, Any, Optional

class CampusPlatformTester:
    """校园综合生活服务平台测试类"""

    def __init__(self, base_url: str = "http://localhost:9000"):
        self.base_url = base_url
        self.session = requests.Session()  # 使用Session保持连接
        self.session.headers.update({
            "Content-Type": "application/json"
        })
        self.token: Optional[str] = None
        self.user_id: Optional[int] = None
        self.test_results = []

    def print_separator(self, title: str = ""):
        """打印分隔线"""
        print("\n" + "=" * 80)
        if title:
            print(f"  {title}")
            print("=" * 80)

    def print_result(self, test_name: str, response: requests.Response,
                    expected_status: int = 200):
        """打印测试结果"""
        status = "✓ 通过" if response.status_code == expected_status else "✗ 失败"
        print(f"\n📋 测试: {test_name}")
        print(f"   状态码: {response.status_code} (预期: {expected_status}) {status}")
        print(f"   URL: {response.request.method} {response.url}")

        # 打印请求头（调试用）
        print(f"   Authorization: {response.request.headers.get('Authorization', '未设置')[:50]}...")

        try:
            data = response.json()
            # 格式化输出，限制长度
            json_str = json.dumps(data, ensure_ascii=False, indent=2)
            if len(json_str) > 500:
                print(f"   响应数据: {json_str[:500]}...")
            else:
                print(f"   响应数据: {json_str}")

            self.test_results.append({
                "test": test_name,
                "status": status,
                "code": response.status_code,
                "success": response.status_code == expected_status
            })
            return data
        except:
            print(f"   响应体: {response.text[:300]}")
            self.test_results.append({
                "test": test_name,
                "status": status,
                "code": response.status_code,
                "success": False
            })
            return None

    def set_token(self, token_value: str):
        """
        设置Token到Session中
        处理各种可能的Token格式
        """
        if not token_value:
            print("❌ Token为空，无法设置")
            return False

        # 提取纯token
        if token_value.startswith("Bearer "):
            self.token = token_value[7:]  # 去掉 "Bearer " 前缀
        elif token_value.startswith("bearer "):
            self.token = token_value[7:]
        else:
            self.token = token_value

        # 设置到Session的headers中（Bearer + 空格 + token）
        self.session.headers["Authorization"] = f"Bearer {self.token}"

        print(f"✅ Token已设置到Session")
        print(f"   纯token长度: {len(self.token)} 字符")
        print(f"   请求头值: {self.session.headers['Authorization'][:60]}...")
        return True

    def login(self, account: str = "chris", password: str = "123456"):
        """登录获取Token"""
        self.print_separator("1. 用户登录")

        print(f"\n📝 测试用户登录 (账号: {account})...")

        # 使用session发送请求
        response = self.session.post(
            f"{self.base_url}/api/auth/login",
            json={"account": account, "password": password}
        )

        data = self.print_result("用户登录", response)

        if data and response.status_code == 200:
            # 解析响应数据结构: {code, message, data: {token, expireSeconds, user: {userId, ...}}}
            inner_data = data.get("data", {})
            if not inner_data:
                print("❌ 响应中没有data字段")
                return False

            # 获取token
            token_value = inner_data.get("token", "")
            if not token_value:
                print("❌ 响应中没有token字段")
                return False

            print(f"\n📦 原始Token: {token_value[:80]}...")

            # 设置Token
            if not self.set_token(token_value):
                return False

            # 获取用户信息 - 注意嵌套结构！
            user_info = inner_data.get("user", {})
            self.user_id = user_info.get("userId")

            if self.user_id:
                print(f"✅ 用户ID: {self.user_id}")
                print(f"   用户名: {user_info.get('username', '未知')}")
                print(f"   角色: {user_info.get('role', '未知')}")
            else:
                print("⚠️  未能获取用户ID，从user对象中提取")
                # 尝试从JWT中解析
                try:
                    import base64
                    parts = self.token.split('.')
                    if len(parts) >= 2:
                        payload = parts[1]
                        # 添加padding
                        padding = 4 - len(payload) % 4
                        if padding != 4:
                            payload += '=' * padding
                        decoded = base64.urlsafe_b64decode(payload)
                        jwt_data = json.loads(decoded)
                        self.user_id = jwt_data.get("userId")
                        if self.user_id:
                            print(f"✅ 从JWT中获取用户ID: {self.user_id}")
                except Exception as e:
                    print(f"⚠️  无法解析JWT: {e}")

            return True

        print("❌ 登录失败")
        return False

    def register_user(self, student_no: str = None, username: str = None):
        """注册新用户"""
        if not student_no:
            student_no = f"test{int(time.time()) % 100000}"
        if not username:
            username = f"测试用户{int(time.time()) % 1000}"

        print("\n📝 测试用户注册...")

        # 注册不需要token，使用普通请求
        response = requests.post(
            f"{self.base_url}/api/auth/register",
            json={
                "studentNo": student_no,
                "username": username,
                "password": "123456",
                "phone": f"138{int(time.time()) % 100000000:08d}",
                "email": f"{student_no}@example.com",
                "college": "计算机学院",
                "major": "软件工程"
            },
            headers={"Content-Type": "application/json"}
        )

        data = self.print_result("用户注册", response)
        return data

    def test_user_auth_service(self):
        """测试用户认证服务所有接口"""
        self.print_separator("2. 用户认证服务接口测试")

        if not self.token:
            print("❌ 未登录，跳过测试")
            return

        # 测试查询当前用户信息
        print("\n📝 测试查询当前用户信息...")
        response = self.session.get(f"{self.base_url}/api/users/me")
        user_data = self.print_result("查询当前用户信息", response)

        # 测试修改当前用户信息
        if response.status_code == 200:
            print("\n📝 测试修改当前用户信息...")
            response = self.session.put(
                f"{self.base_url}/api/users/me",
                json={
                    "phone": "13900139000",
                    "email": "updated@example.com",
                    "college": "信息工程学院",
                    "major": "人工智能"
                }
            )
            self.print_result("修改当前用户信息", response)

        # 测试查询用户资料
        if self.user_id:
            print(f"\n📝 测试查询用户资料 (ID: {self.user_id})...")
            response = self.session.get(f"{self.base_url}/api/users/{self.user_id}")
            self.print_result("查询用户资料", response)

        # 测试查询用户简要信息
        if self.user_id:
            print(f"\n📝 测试查询用户简要信息 (ID: {self.user_id})...")
            response = self.session.get(f"{self.base_url}/api/users/simple/{self.user_id}")
            self.print_result("查询用户简要信息", response)

        # 测试批量查询用户简要信息
        print("\n📝 测试批量查询用户简要信息...")
        user_ids = [1]
        if self.user_id and self.user_id != 1:
            user_ids.append(self.user_id)

        response = self.session.post(
            f"{self.base_url}/api/users/simple/batch",
            json=user_ids
        )
        self.print_result("批量查询用户简要信息", response)

        # 注意：先不测试登出，否则token会失效
        # 如果需要测试登出，在最后进行

    def test_market_service(self):
        """测试二手交易服务所有接口"""
        self.print_separator("3. 二手交易服务接口测试")

        if not self.token:
            print("❌ 未登录，跳过测试")
            return

        # 测试发布商品
        print("\n📝 测试发布商品...")
        item_data = {
            "title": f"二手自行车-{int(time.time())}",
            "description": "9成新山地车，骑行流畅，送锁具",
            "price": 299.00,
            "category": "交通工具",
            "coverImage": "https://example.com/bike.jpg",
            "images": "https://example.com/1.jpg,https://example.com/2.jpg",
            "contactInfo": "微信：bike123"
        }

        response = self.session.post(
            f"{self.base_url}/api/market/items",
            json=item_data
        )
        created_item = self.print_result("发布商品", response)

        item_id = None
        if created_item and response.status_code == 200:
            # 从响应中获取商品ID
            inner_data = created_item.get("data", {})
            item_id = inner_data.get("id") if isinstance(inner_data, dict) else None
            if item_id:
                print(f"   📌 创建的商品ID: {item_id}")

        # 测试查询商品列表
        print("\n📝 测试查询商品列表...")
        response = self.session.get(
            f"{self.base_url}/api/market/items",
            params={"page": 1, "size": 10}
        )
        self.print_result("查询商品列表", response)

        # 测试查询商品列表（带分类筛选）
        print("\n📝 测试查询商品列表（分类: 交通工具）...")
        response = self.session.get(
            f"{self.base_url}/api/market/items",
            params={"category": "交通工具", "page": 1, "size": 10}
        )
        self.print_result("按分类查询商品列表", response)

        # 如果有商品ID，测试详情和编辑
        if item_id:
            print(f"\n📝 测试查询商品详情 (ID: {item_id})...")
            response = self.session.get(f"{self.base_url}/api/market/items/{item_id}")
            self.print_result("查询商品详情", response)

            print(f"\n📝 测试编辑商品 (ID: {item_id})...")
            response = self.session.put(
                f"{self.base_url}/api/market/items/{item_id}",
                json={
                    "title": f"二手自行车-已编辑-{int(time.time())}",
                    "description": "9成新山地车，送锁具，价格可小刀",
                    "price": 279.00,
                    "category": "交通工具",
                    "contactInfo": "微信：bike123"
                }
            )
            self.print_result("编辑商品", response)

        # 测试查询我的发布
        print("\n📝 测试查询我的发布...")
        response = self.session.get(f"{self.base_url}/api/market/items/my")
        my_items = self.print_result("查询我的发布", response)

        # 测试下架和标记已售出（使用已创建的商品）
        if item_id:
            # 先创建一个新商品用于测试下架
            print("\n📝 创建测试商品用于下架测试...")
            response = self.session.post(
                f"{self.base_url}/api/market/items",
                json={
                    "title": "测试下架商品",
                    "description": "这个商品将被下架",
                    "price": 50.00,
                    "category": "其他",
                    "contactInfo": "微信：test"
                }
            )
            test_item = self.print_result("创建测试商品", response)
            inner_data = test_item.get("data", {}) if test_item else {}
            test_item_id = inner_data.get("id") if isinstance(inner_data, dict) else None

            if test_item_id:
                print(f"\n📝 测试下架商品 (ID: {test_item_id})...")
                response = self.session.put(
                    f"{self.base_url}/api/market/items/{test_item_id}/off-shelf"
                )
                self.print_result("下架商品", response)

            # 创建另一个商品用于测试标记已售出
            print("\n📝 创建测试商品用于标记已售出...")
            response = self.session.post(
                f"{self.base_url}/api/market/items",
                json={
                    "title": "测试标记已售出商品",
                    "description": "这个商品将标记为已售出",
                    "price": 30.00,
                    "category": "书籍",
                    "contactInfo": "微信：test"
                }
            )
            sold_item = self.print_result("创建测试商品", response)
            inner_data = sold_item.get("data", {}) if sold_item else {}
            sold_item_id = inner_data.get("id") if isinstance(inner_data, dict) else None

            if sold_item_id:
                print(f"\n📝 测试标记已售出 (ID: {sold_item_id})...")
                response = self.session.put(
                    f"{self.base_url}/api/market/items/{sold_item_id}/sold"
                )
                self.print_result("标记已售出", response)

    def test_task_service(self):
        """测试跑腿任务服务所有接口"""
        self.print_separator("4. 跑腿任务服务接口测试")

        if not self.token:
            print("❌ 未登录，跳过测试")
            return

        # 测试发布任务
        print("\n📝 测试发布跑腿任务...")
        task_data = {
            "title": f"帮忙取快递-{int(time.time())}",
            "description": "菜鸟驿站有一个快递，请帮忙取到宿舍楼下",
            "taskType": "取快递",
            "reward": 5.00,
            "pickupAddress": "菜鸟驿站",
            "deliveryAddress": "3号宿舍楼201",
            "deadline": "2026-05-15T18:00:00",
            "contactInfo": "微信：abc123"
        }

        response = self.session.post(
            f"{self.base_url}/api/tasks",
            json=task_data
        )
        created_task = self.print_result("发布任务", response)

        task_id = None
        if created_task and response.status_code == 200:
            inner_data = created_task.get("data", {})
            task_id = inner_data.get("id") if isinstance(inner_data, dict) else None
            if task_id:
                print(f"   📌 创建的任务ID: {task_id}")

        # 测试查询任务大厅
        print("\n📝 测试查询任务大厅...")
        response = self.session.get(
            f"{self.base_url}/api/tasks",
            params={"page": 1, "size": 10}
        )
        self.print_result("查询任务大厅", response)

        # 测试查询任务详情
        if task_id:
            print(f"\n📝 测试查询任务详情 (ID: {task_id})...")
            response = self.session.get(f"{self.base_url}/api/tasks/{task_id}")
            self.print_result("查询任务详情", response)

        # 测试查询我发布的任务
        print("\n📝 测试查询我发布的任务...")
        response = self.session.get(f"{self.base_url}/api/tasks/my-published")
        self.print_result("查询我发布的任务", response)

        # 测试查询我接单的任务
        print("\n📝 测试查询我接单的任务...")
        response = self.session.get(f"{self.base_url}/api/tasks/my-accepted")
        self.print_result("查询我接单的任务", response)

        # 创建新任务用于测试接单和完成
        print("\n📝 创建新任务用于接单测试...")
        response = self.session.post(
            f"{self.base_url}/api/tasks",
            json={
                "title": "帮忙买奶茶",
                "description": "帮忙在校门口奶茶店买两杯奶茶送到图书馆",
                "taskType": "代买",
                "reward": 8.00,
                "pickupAddress": "校门口奶茶店",
                "deliveryAddress": "图书馆3楼自习室",
                "deadline": "2026-05-15T20:00:00",
                "contactInfo": "微信：buytea"
            }
        )
        accept_test_task = self.print_result("创建接单测试任务", response)

        if accept_test_task and response.status_code == 200:
            inner_data = accept_test_task.get("data", {})
            accept_task_id = inner_data.get("id") if isinstance(inner_data, dict) else None

            if accept_task_id:
                # 测试接单
                print(f"\n📝 测试接单 (任务ID: {accept_task_id})...")
                response = self.session.put(
                    f"{self.base_url}/api/tasks/{accept_task_id}/accept"
                )
                self.print_result("接单", response)

                # 测试完成任务
                print(f"\n📝 测试完成任务 (任务ID: {accept_task_id})...")
                response = self.session.put(
                    f"{self.base_url}/api/tasks/{accept_task_id}/complete"
                )
                self.print_result("完成任务", response)

        # 创建任务用于测试取消
        print("\n📝 创建新任务用于取消测试...")
        response = self.session.post(
            f"{self.base_url}/api/tasks",
            json={
                "title": "测试取消任务",
                "description": "这个任务将被取消",
                "taskType": "其他",
                "reward": 2.00,
                "pickupAddress": "测试地点A",
                "deliveryAddress": "测试地点B",
                "contactInfo": "微信：cancel"
            }
        )
        cancel_test_task = self.print_result("创建取消测试任务", response)

        if cancel_test_task and response.status_code == 200:
            inner_data = cancel_test_task.get("data", {})
            cancel_task_id = inner_data.get("id") if isinstance(inner_data, dict) else None

            if cancel_task_id:
                print(f"\n📝 测试取消任务 (任务ID: {cancel_task_id})...")
                response = self.session.put(
                    f"{self.base_url}/api/tasks/{cancel_task_id}/cancel"
                )
                self.print_result("取消任务", response)

    def test_activity_service(self):
        """测试活动社团服务所有接口"""
        self.print_separator("5. 活动社团服务接口测试")

        if not self.token:
            print("❌ 未登录，跳过测试")
            return

        # 测试创建社团
        print("\n📝 测试创建社团...")
        club_data = {
            "name": f"计算机协会-{int(time.time())}",
            "description": "致力于推广计算机技术和校园技术交流",
            "logo": "https://example.com/logo.png",
            "contactInfo": "QQ群：123456"
        }

        response = self.session.post(
            f"{self.base_url}/api/clubs",
            json=club_data
        )
        created_club = self.print_result("创建社团", response)

        club_id = None
        if created_club and response.status_code == 200:
            inner_data = created_club.get("data", {})
            club_id = inner_data.get("id") if isinstance(inner_data, dict) else None
            if club_id:
                print(f"   📌 创建的社团ID: {club_id}")

        # 测试查询社团列表
        print("\n📝 测试查询社团列表...")
        response = self.session.get(
            f"{self.base_url}/api/clubs",
            params={"page": 1, "size": 10}
        )
        self.print_result("查询社团列表", response)

        if club_id:
            # 测试查询社团详情
            print(f"\n📝 测试查询社团详情 (ID: {club_id})...")
            response = self.session.get(f"{self.base_url}/api/clubs/{club_id}")
            self.print_result("查询社团详情", response)

            # 测试修改社团信息
            print(f"\n📝 测试修改社团信息 (ID: {club_id})...")
            response = self.session.put(
                f"{self.base_url}/api/clubs/{club_id}",
                json={
                    "name": f"计算机协会-已修改",
                    "description": "致力于推广计算机技术和校园技术交流，欢迎加入",
                    "contactInfo": "QQ群：123456，微信：csclub"
                }
            )
            self.print_result("修改社团信息", response)

            # 测试发布活动
            print(f"\n📝 测试发布活动 (社团ID: {club_id})...")
            activity_data = {
                "clubId": club_id,
                "title": f"编程马拉松-{int(time.time())}",
                "description": "24小时编程挑战赛，三人一组完成项目",
                "location": "图书馆报告厅",
                "coverImage": "https://example.com/activity.png",
                "startTime": "2026-05-15T09:00:00",
                "endTime": "2026-05-16T09:00:00",
                "signupDeadline": "2026-05-14T23:59:59",
                "maxParticipants": 50
            }

            response = self.session.post(
                f"{self.base_url}/api/activities",
                json=activity_data
            )
            created_activity = self.print_result("发布活动", response)

            activity_id = None
            if created_activity and response.status_code == 200:
                inner_data = created_activity.get("data", {})
                activity_id = inner_data.get("id") if isinstance(inner_data, dict) else None
                if activity_id:
                    print(f"   📌 创建的活动ID: {activity_id}")

        # 测试查询活动列表
        print("\n📝 测试查询活动列表...")
        response = self.session.get(
            f"{self.base_url}/api/activities",
            params={"page": 1, "size": 10}
        )
        self.print_result("查询活动列表", response)

        # 测试查询我管理的社团
        print("\n📝 测试查询我管理的社团...")
        response = self.session.get(f"{self.base_url}/api/clubs/my")
        self.print_result("查询我管理的社团", response)

        # 如果有活动ID，继续测试
        if 'activity_id' in locals() and activity_id:
            # 测试查询活动详情
            print(f"\n📝 测试查询活动详情 (ID: {activity_id})...")
            response = self.session.get(f"{self.base_url}/api/activities/{activity_id}")
            self.print_result("查询活动详情", response)

            # 测试活动报名
            print(f"\n📝 测试活动报名 (活动ID: {activity_id})...")
            response = self.session.post(
                f"{self.base_url}/api/activities/{activity_id}/register"
            )
            self.print_result("活动报名", response)

            # 测试查询我的报名
            print("\n📝 测试查询我的报名...")
            response = self.session.get(f"{self.base_url}/api/activities/my-registered")
            self.print_result("查询我的报名", response)

            # 测试取消报名
            print(f"\n📝 测试取消报名 (活动ID: {activity_id})...")
            response = self.session.delete(
                f"{self.base_url}/api/activities/{activity_id}/register"
            )
            self.print_result("取消报名", response)

            # 测试查询我发布的活动
            print("\n📝 测试查询我发布的活动...")
            response = self.session.get(f"{self.base_url}/api/activities/my-published")
            self.print_result("查询我发布的活动", response)

            # 测试取消活动
            print(f"\n📝 测试取消活动 (活动ID: {activity_id})...")
            response = self.session.put(
                f"{self.base_url}/api/activities/{activity_id}/cancel"
            )
            self.print_result("取消活动", response)

    def test_notification_service(self):
        """测试消息通知服务所有接口"""
        self.print_separator("6. 消息通知服务接口测试")

        if not self.token:
            print("❌ 未登录，跳过测试")
            return

        if not self.user_id:
            print("❌ 用户ID不存在，跳过通知服务测试")
            return

        # 测试创建通知
        print("\n📝 测试创建系统通知...")
        response = self.session.post(
            f"{self.base_url}/api/notifications",
            json={
                "receiverId": self.user_id,
                "title": "系统通知",
                "content": "欢迎使用校园综合生活服务平台！",
                "type": "SYSTEM",
                "businessId": None,
                "businessType": "SYSTEM"
            }
        )
        created_notification = self.print_result("创建系统通知", response)

        # 创建任务通知
        print("\n📝 测试创建任务通知...")
        response = self.session.post(
            f"{self.base_url}/api/notifications",
            json={
                "receiverId": self.user_id,
                "title": "任务提醒",
                "content": "你的任务已被接单，请及时联系",
                "type": "TASK",
                "businessId": 1,
                "businessType": "TASK"
            }
        )
        self.print_result("创建任务通知", response)

        # 创建活动通知
        print("\n📝 测试创建活动通知...")
        response = self.session.post(
            f"{self.base_url}/api/notifications",
            json={
                "receiverId": self.user_id,
                "title": "活动提醒",
                "content": "你报名的活动即将开始",
                "type": "ACTIVITY",
                "businessId": 1,
                "businessType": "ACTIVITY"
            }
        )
        self.print_result("创建活动通知", response)

        # 获取通知ID
        notification_id = None
        if created_notification and response.status_code == 200:
            inner_data = created_notification.get("data", {})
            notification_id = inner_data.get("id") if isinstance(inner_data, dict) else None

        # 测试查询我的通知
        print("\n📝 测试查询我的通知...")
        response = self.session.get(f"{self.base_url}/api/notifications")
        notifications_data = self.print_result("查询我的通知", response)

        # 从查询结果中获取通知ID（如果创建时没有获取到）
        if not notification_id and notifications_data:
            inner_data = notifications_data.get("data", {})
            if isinstance(inner_data, dict):
                records = inner_data.get("records", [])
                if records and len(records) > 0:
                    notification_id = records[0].get("id")

        # 测试查询未读通知数量
        print("\n📝 测试查询未读通知数量...")
        response = self.session.get(f"{self.base_url}/api/notifications/unread-count")
        self.print_result("查询未读通知数量", response)

        # 测试标记单条已读
        if notification_id:
            print(f"\n📝 测试标记单条已读 (通知ID: {notification_id})...")
            response = self.session.put(
                f"{self.base_url}/api/notifications/{notification_id}/read"
            )
            self.print_result("标记单条已读", response)

        # 测试标记全部已读
        print("\n📝 测试标记全部已读...")
        response = self.session.put(f"{self.base_url}/api/notifications/read-all")
        self.print_result("标记全部已读", response)

        # 测试删除通知
        if notification_id:
            print(f"\n📝 测试删除通知 (通知ID: {notification_id})...")
            response = self.session.delete(
                f"{self.base_url}/api/notifications/{notification_id}"
            )
            self.print_result("删除通知", response)

    def test_logout(self):
        """测试登出（最后测试）"""
        self.print_separator("7. 用户登出测试")

        print("\n📝 测试用户登出...")
        response = self.session.post(f"{self.base_url}/api/auth/logout")
        self.print_result("用户登出", response)

        if response.status_code == 200:
            print("\n📝 测试登出后访问受保护接口...")
            response = self.session.get(f"{self.base_url}/api/users/me")
            self.print_result("登出后访问用户信息（应返回401）", response, expected_status=401)

    def test_edge_cases(self):
        """测试边界情况和异常处理"""
        self.print_separator("8. 边界情况和异常处理测试")

        # 测试未登录访问受保护接口
        print("\n📝 测试未登录访问受保护接口...")
        response = requests.get(
            f"{self.base_url}/api/users/me",
            headers={"Content-Type": "application/json"}
        )
        self.print_result("未登录访问用户信息", response, expected_status=401)

        # 测试无效Token
        print("\n📝 测试无效Token...")
        response = requests.get(
            f"{self.base_url}/api/users/me",
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer invalid_token_12345"
            }
        )
        self.print_result("无效Token访问", response, expected_status=401)

        # 测试查询不存在的资源
        print("\n📝 测试查询不存在的商品...")
        response = self.session.get(f"{self.base_url}/api/market/items/99999")
        self.print_result("查询不存在的商品", response)

        print("\n📝 测试查询不存在的任务...")
        response = self.session.get(f"{self.base_url}/api/tasks/99999")
        self.print_result("查询不存在的任务", response)

    def generate_summary(self):
        """生成测试总结"""
        self.print_separator("测试总结")

        total = len(self.test_results)
        passed = sum(1 for r in self.test_results if r["success"])
        failed = total - passed

        print(f"\n📊 总测试数: {total}")
        print(f"✅ 通过: {passed}")
        print(f"❌ 失败: {failed}")
        print(f"📈 通过率: {(passed/total*100):.1f}%" if total > 0 else "0%")

        if failed > 0:
            print("\n❌ 失败的测试:")
            for result in self.test_results:
                if not result["success"]:
                    print(f"   - {result['test']} (状态码: {result['code']})")

        print("\n" + "=" * 80)
        print("  测试完成！")
        print("=" * 80)

    def run_all_tests(self):
        """运行所有测试"""
        print("=" * 80)
        print("  校园综合生活服务平台 - 接口自动化测试（修正版）")
        print("=" * 80)
        print(f"  测试目标: {self.base_url}")
        print(f"  开始时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")

        try:
            # 1. 登录（必须先成功）
            if not self.login():
                print("❌ 登录失败，无法继续测试")
                self.generate_summary()
                return

            # 2. 注册新用户（可选）
            # self.register_user()

            # 3. 测试用户认证服务
            self.test_user_auth_service()

            # 4. 测试二手交易服务
            self.test_market_service()

            # 5. 测试跑腿任务服务
            self.test_task_service()

            # 6. 测试活动社团服务
            self.test_activity_service()

            # 7. 测试消息通知服务
            self.test_notification_service()

            # 8. 边界情况测试
            self.test_edge_cases()

            # 9. 最后测试登出
            self.test_logout()

        except requests.exceptions.ConnectionError:
            print(f"\n❌ 连接失败！请确保服务已启动: {self.base_url}")
        except Exception as e:
            print(f"\n❌ 测试过程中出现异常: {str(e)}")
            import traceback
            traceback.print_exc()
        finally:
            # 生成测试总结
            self.generate_summary()
            print(f"\n  结束时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")


def main():
    """主函数"""
    import sys
    
    # 支持自定义base_url
    base_url = "http://localhost:9000"
    if len(sys.argv) > 1:
        base_url = sys.argv[1]
    
    tester = CampusPlatformTester(base_url=base_url)
    tester.run_all_tests()

if __name__ == "__main__":
    main()