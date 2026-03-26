import 'dotenv/config';
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';
import axios from 'axios';

// ─── HTTP Client ──────────────────────────────────────────────────────────────
const gateway = axios.create({
  baseURL: process.env.GATEWAY_URL || 'http://localhost:8080',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
    ...(process.env.PLATFORM_TOKEN
      ? { Authorization: `Bearer ${process.env.PLATFORM_TOKEN}` }
      : {}),
  },
});

// ─── Helper ───────────────────────────────────────────────────────────────────
async function callApi(method, path, params = {}, body = null) {
  try {
    const res = await gateway.request({
      method,
      url: path,
      params: method === 'GET' ? params : undefined,
      data: method !== 'GET' ? body : undefined,
    });
    return res.data;
  } catch (err) {
    const msg = err.response?.data?.message || err.message;
    return { error: true, message: msg };
  }
}

// ─── MCP Server ───────────────────────────────────────────────────────────────
const server = new McpServer({
  name: 'edu-platform',
  version: '1.0.0',
});

// ══════════════════════════════════════════════════════════════════════════════
// COURSE TOOLS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * 查询课程列表（支持关键词、分类、等级、排序、分页）
 */
server.tool(
  'search_courses',
  '搜索课程列表，支持关键词搜索、分类筛选、难度筛选和分页',
  {
    keyword: z.string().optional().describe('搜索关键词，匹配课程标题或描述'),
    categoryId: z.number().int().optional().describe('课程分类ID'),
    level: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']).optional().describe('课程难度等级'),
    sortBy: z.enum(['newest', 'hottest', 'price_asc', 'price_desc']).optional().describe('排序方式'),
    page: z.number().int().min(1).default(1).describe('页码，从1开始'),
    size: z.number().int().min(1).max(50).default(10).describe('每页条数，最大50'),
  },
  async ({ keyword, categoryId, level, sortBy, page, size }) => {
    const data = await callApi('GET', '/course-service/api/courses', {
      keyword, categoryId, level, sortBy, page, size,
    });
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

/**
 * 查询课程详情
 */
server.tool(
  'get_course_detail',
  '根据课程ID获取课程的完整详情，包括简介、大纲、价格、讲师信息',
  {
    courseId: z.number().int().positive().describe('课程ID'),
  },
  async ({ courseId }) => {
    const data = await callApi('GET', `/course-service/api/courses/${courseId}`);
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

/**
 * 获取所有课程分类
 */
server.tool(
  'get_course_categories',
  '获取平台所有课程分类列表',
  {},
  async () => {
    const data = await callApi('GET', '/course-service/api/categories');
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

/**
 * 获取 AI 课程推荐
 */
server.tool(
  'get_ai_recommendations',
  '调用平台 AI 推荐接口，根据用户偏好或热度获取课程推荐列表',
  {
    userId: z.number().int().optional().describe('用户ID，不传则返回热门推荐'),
    limit: z.number().int().min(1).max(20).default(5).describe('推荐数量'),
  },
  async ({ userId, limit }) => {
    const data = await callApi('GET', '/course-service/api/courses/ai-recommendations', {
      userId, limit,
    });
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

// ══════════════════════════════════════════════════════════════════════════════
// ORDER TOOLS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * 查询平台订单统计
 */
server.tool(
  'get_order_stats',
  '获取平台订单统计数据，包括总订单数、总收入、各状态订单数',
  {},
  async () => {
    const data = await callApi('GET', '/order-service/api/orders/stats');
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

/**
 * 查询订单列表（管理员）
 */
server.tool(
  'list_orders',
  '管理员接口：查询所有订单，支持状态筛选和分页',
  {
    status: z.number().int().min(0).max(3).optional()
      .describe('订单状态：0=待支付, 1=已支付, 2=已取消, 3=已退款'),
    page: z.number().int().min(1).default(1).describe('页码'),
    size: z.number().int().min(1).max(50).default(10).describe('每页条数'),
  },
  async ({ status, page, size }) => {
    const data = await callApi('GET', '/order-service/api/orders/all', { status, page, size });
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

// ══════════════════════════════════════════════════════════════════════════════
// USER TOOLS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * 查询用户列表（管理员）
 */
server.tool(
  'list_users',
  '管理员接口：查询平台用户列表，支持关键词搜索和分页',
  {
    keyword: z.string().optional().describe('搜索关键词，匹配用户名或邮箱'),
    page: z.number().int().min(1).default(1).describe('页码'),
    size: z.number().int().min(1).max(50).default(10).describe('每页条数'),
  },
  async ({ keyword, page, size }) => {
    const data = await callApi('GET', '/user-service/api/users', { keyword, page, size });
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

/**
 * 查询用户详情
 */
server.tool(
  'get_user_detail',
  '根据用户ID获取用户详情',
  {
    userId: z.number().int().positive().describe('用户ID'),
  },
  async ({ userId }) => {
    const data = await callApi('GET', `/user-service/api/users/${userId}`);
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

// ══════════════════════════════════════════════════════════════════════════════
// NOTIFICATION TOOLS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * 查询用户通知
 */
server.tool(
  'get_user_notifications',
  '查询指定用户的站内通知列表',
  {
    userId: z.number().int().positive().describe('用户ID'),
    unreadOnly: z.boolean().default(false).describe('是否只查询未读通知'),
    page: z.number().int().min(1).default(1).describe('页码'),
    size: z.number().int().min(1).max(50).default(10).describe('每页条数'),
  },
  async ({ userId, unreadOnly, page, size }) => {
    const data = await callApi('GET', '/notification-service/api/notifications/admin', {
      userId, unreadOnly, page, size,
    });
    return {
      content: [{ type: 'text', text: JSON.stringify(data, null, 2) }],
    };
  },
);

// ══════════════════════════════════════════════════════════════════════════════
// PLATFORM RESOURCE (只读文档)
// ══════════════════════════════════════════════════════════════════════════════

server.resource(
  'platform-api-doc',
  'edu-platform://api-overview',
  async (uri) => ({
    contents: [{
      uri: uri.href,
      mimeType: 'text/markdown',
      text: `# edu-platform API 概览

## 服务列表
| 服务 | 端口 | 说明 |
|------|------|------|
| gateway-service | 8080 | API 网关，所有请求入口 |
| user-service | 8081 | 用户注册/登录/管理 |
| course-service | 8082 | 课程 CRUD / AI 推荐 |
| order-service | 8083 | 订单 / 支付模拟 |
| notification-service | 8084 | 站内通知 |

## 认证方式
- 所有非公开接口需在 Header 中携带：\`Authorization: Bearer <JWT>\`
- 通过 POST /user-service/api/auth/login 获取 JWT

## 可用 MCP 工具
- \`search_courses\` — 搜索课程
- \`get_course_detail\` — 课程详情
- \`get_course_categories\` — 课程分类
- \`get_ai_recommendations\` — AI 推荐
- \`get_order_stats\` — 订单统计
- \`list_orders\` — 订单列表（管理员）
- \`list_users\` — 用户列表（管理员）
- \`get_user_detail\` — 用户详情
- \`get_user_notifications\` — 用户通知
`,
    }],
  }),
);

// ─── Start ────────────────────────────────────────────────────────────────────
const transport = new StdioServerTransport();
await server.connect(transport);
console.error('[edu-platform MCP] Server started (stdio mode)');
